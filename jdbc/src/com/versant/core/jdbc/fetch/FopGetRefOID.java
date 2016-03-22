
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.common.OID;
import com.versant.core.server.StateContainer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A fetch the reference oid to a class.
 */
public class FopGetRefOID extends FetchOp {

    private final FetchOpData src;
    private final Data data;
    private final JdbcField jdbcField;
    private final ClassMetaData cmd;
    private final JdbcClass jdbcClass;

    private SelectExp se;


    /**
     * This gets our OID from fetchData and delegates to our src for
     * the OID and ResultSet.
     */
    public class Data extends FetchOpDataProxy {

        public Data(FetchOpData src) {
            super(src);
        }

        public void setOID(FetchResult fetchResult, OID oid) {
            fetchResult.setData(FopGetRefOID.this, oid);
        }

        public OID getOID(FetchResult fetchResult) {
            return (OID)fetchResult.getData(FopGetRefOID.this);
        }

        public String getDescription() {
            return " [" + getIndex() + "]";
        }
    }

    public FetchOpData getOutputData() {
        return data;
    }

    /**
     * Fetch an OID for the hierarchy based on cmd.
     */
    public FopGetRefOID(FetchSpec spec, FetchOpData src, JdbcRefField refField,
            SelectExp se) {
        super(spec);
        this.src = src;
        data = new Data(src);
        this.jdbcField = refField;
        this.cmd = refField.targetClass;
        this.jdbcClass = (JdbcClass)cmd.storeClass;
        this.se = se;
    }

    /**
     * Init this FetchOp and return the expressions that we need to be added
     * to the select list of the query or null if none. The FetchOp may
     * add additional FetchOp's to the spec.
     */
    public SqlExp init(SelectExp expRoot) {
        //add the join
//        SelectExp se = addJoin(joinFromSe, jdbcClass.table);
        return JdbcColumn.toSqlExp(jdbcClass.table.pk, se);
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer)
            throws SQLException {
        ResultSet rs = src.getResultSet(fetchResult);
        if (rs == null) {
            return; // nothing to fetch
        }

        JdbcOID oid = (JdbcOID)cmd.createOID(!cmd.isInHierarchy());
        if (oid.copyKeyFields(rs, getFirstColIndex())) {
            data.setOID(fetchResult, oid);
        }
    }

    public String getDescription() {
        return cmd.qname + data.getDescription();
    }

    public int getResultType() {
        return MDStatics.OID;
    }

    public Object getResult(FetchResult fetchResult) {
        return data.getOID(fetchResult);
    }

}

