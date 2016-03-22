
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.jdbc.fetch;

import com.versant.core.server.StateContainer;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.ProjectionQueryDecoder;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.jdbc.JdbcUtils;
import com.versant.core.jdbc.JdbcGenericState;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.query.JDOQLNodeToSqlExp;
import com.versant.core.jdo.query.ResultNode;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.BindingSupportImpl;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This is a fetchOp that is responsible for fetching the projection column for a
 * projection query. It does not fetch the 'this' value.
 */
public class FopGetProjection extends FetchOp {
    private ProjectionQueryDecoder decoder;
    private JDOQLNodeToSqlExp visitor;
    private ResultNode resultNode;
    private FetchOpData src;

    public FopGetProjection(FetchSpec spec, ProjectionQueryDecoder decoder,
            JDOQLNodeToSqlExp visitor, ResultNode resultNode, FetchOpData src) {
        super(spec);
        this.decoder = decoder;
        this.visitor = visitor;
        this.resultNode = resultNode;
        this.src = src;
    }

    public SqlExp init(SelectExp root) {
        return visitor.toSqlExp(resultNode, root, null, 0, null);
    }

    public void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException {
        final Object[] resArray = src.getProjectionResultArray(fetchResult);

        final Object[] typeObjectArray = decoder.getFmdArray();
        final int[] typeArray = decoder.getResultTypeArray();
        final int[] typeCodes = decoder.getTypeCodes();

        ResultSet rs = src.getResultSet(fetchResult);
        int currentRsIndex = getFirstColIndex();

        for (int i = 0; i < typeArray.length; i++) {
            int type = typeArray[i];
            switch (type) {
                case ProjectionQueryDecoder.TYPE_THIS:      //this
                    resArray[i] = src.getOID(fetchResult);
                    break;
                case ProjectionQueryDecoder.TYPE_FIELD:     //field
                    currentRsIndex = getFieldData(
                            (FieldMetaData)typeObjectArray[i], rs,
                            currentRsIndex, resArray, i);
                    break;
                case ProjectionQueryDecoder.TYPE_AGGRETATE: //aggregate or computed field

                    currentRsIndex = getFieldData(typeCodes[i], rs,
                            currentRsIndex, resArray, i);


                    break;
                case ProjectionQueryDecoder.TYPE_EXP:
                    currentRsIndex = getFieldData(typeCodes[i], rs,
                            currentRsIndex, resArray, i);
                    break;
                case ProjectionQueryDecoder.TYPE_VAR:
                    ClassMetaData cmd = (ClassMetaData) typeObjectArray[i];
                    JdbcOID oid =  (JdbcOID) cmd.createOID(false);
                    if (oid.copyKeyFields(rs, currentRsIndex)) {
                        resArray[i] = oid;
                    } else {
                        resArray[i] = null;
                    }
                    currentRsIndex += ((JdbcClass)cmd.storeClass).table.pk.length;
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("");
            }
        }
    }

    private static int getFieldData(int typeCode, ResultSet rs, int rsIndex,
            Object[] dataRow, int dataIndex) throws SQLException {
        switch (typeCode) {
            case MDStatics.BYTEW:
                dataRow[dataIndex] = new Byte(rs.getByte(rsIndex));
                break;
            case MDStatics.SHORTW:
                dataRow[dataIndex] = new Short(rs.getShort(rsIndex));
                break;
            case MDStatics.INTW:
                dataRow[dataIndex] = new Integer(rs.getInt(rsIndex));
                break;
            case MDStatics.LONGW:
                dataRow[dataIndex] = new Long(rs.getLong(rsIndex));
                break;
            case MDStatics.FLOATW:
                dataRow[dataIndex] = new Float(rs.getFloat(rsIndex));
                break;
            case MDStatics.DOUBLEW:
                dataRow[dataIndex] = new Double(rs.getDouble(rsIndex));
                break;
            case MDStatics.STRING:
                dataRow[dataIndex] = rs.getString(rsIndex);
                break;
            case MDStatics.BIGDECIMAL:
                dataRow[dataIndex] = new BigDecimal(rs.getDouble(rsIndex));
                break;
            case MDStatics.BIGINTEGER:
                dataRow[dataIndex] = new BigInteger(rs.getString(rsIndex));
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Unhandled type '" + typeCode + "'");
        }
        return rsIndex + 1;
    }

    public int getFieldData(FieldMetaData fmd, ResultSet rs, int firstCol,
            Object[] dataRow, int dataIndex) throws SQLException {
        JdbcField f = (JdbcField)fmd.storeField;
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField)f).col;
            if (c.converter != null) {
                dataRow[dataIndex] = c.converter.get(rs, firstCol++, c );
            } else {
                dataRow[dataIndex] = JdbcUtils.get(rs, firstCol++, c.javaTypeCode,
                        c.scale );
                if (rs.wasNull()) {
                    dataRow[dataIndex] = null;
                }
            }
        } else if (f instanceof JdbcRefField) {
            JdbcRefField rf = (JdbcRefField)f;
            JdbcOID oid = (JdbcOID)rf.targetClass.createOID(false);
            if (oid.copyKeyFields(rs, firstCol)) {
                dataRow[dataIndex] = oid;
            } else {
                dataRow[dataIndex] = null;
            }
            firstCol += rf.cols.length;
        } else if (f instanceof JdbcPolyRefField) {
            dataRow[dataIndex] = JdbcGenericState.getPolyRefOID(f, rs, firstCol);
            firstCol += ((JdbcPolyRefField)f).cols.length;
        } else {
            throw BindingSupportImpl.getInstance().internal("not implemented");
        }
        return firstCol;
    }

    public String getDescription() {
        return src.getDescription();
    }

    public FetchOpData getOutputData() {
        return null;
    }
}
