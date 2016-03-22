
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
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.common.OID;
import com.versant.core.server.StateContainer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A fetch of the OID (primary key) for a class. This when there is an object
 * in the ResultSet whose primary key is not available elsewhere or otherwise
 * read as part of a State. Example 'SELECT o FROM Order AS o' will use one
 * of these to get the OID for each Order returned.
 */
public class FopGetOID extends FetchOp {

    private final FetchOpData src;
    private final ClassMetaData cmd;
    private final Data data;

    private SelectExp se;
    private JdbcColumn[] pkCols;

    /**
     * This gets our OID from fetchData and delegates to our src for
     * the OID and ResultSet.
     */
    public class Data extends FetchOpDataProxy {

        public Data(FetchOpData src) {
            super(src);
        }

        public void setOID(FetchResult fetchResult, OID oid) {
            fetchResult.setData(FopGetOID.this, oid);
        }

        public OID getOID(FetchResult fetchResult) {
            return (OID)fetchResult.getData(FopGetOID.this);
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
    public FopGetOID(FetchSpec spec, FetchOpData src, ClassMetaData cmd,
            SelectExp rootSe) {
        super(spec);
        this.src = src;
        this.cmd = cmd.top;
        this.data = new Data(src);
        this.se = rootSe;
        this.pkCols = se.table.pk;
    }

    public FopGetOID(FetchSpec spec, FetchOpData src, ClassMetaData cmd,
            SelectExp rootSe, JdbcColumn[] pkCols) {
        super(spec);
        this.src = src;
        this.cmd = cmd.top;
        data = new Data(src);
        this.se = rootSe;
        this.pkCols = pkCols;
    }

    /**
     * Init this FetchOp and return the expressions that we need to be added
     * to the select list of the query or null if none. The FetchOp may
     * add additional FetchOp's to the spec.
     */
    public SqlExp init(SelectExp root) {
//        JdbcTable table = ((JdbcClass)cmd.storeClass).table;
        return JdbcColumn.toSqlExp(pkCols, se);
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer) throws SQLException {
        ResultSet rs = src.getResultSet(fetchResult);
        if (rs == null) {
            return; // nothing to fetch
        }

        JdbcOID oid = (JdbcOID)cmd.createOID(!cmd.isInHierarchy());
        if (oid.copyKeyFields(rs, spec.getProjectionIndex(this))) {
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

