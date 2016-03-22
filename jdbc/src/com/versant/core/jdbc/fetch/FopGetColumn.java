
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

import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.server.StateContainer;

import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * FetchOp for a single column.
 */
public class FopGetColumn extends FetchOp {
    private JdbcColumn column;
    private SelectExp se;


    public Object getResult(FetchResult fetchResult) {
        return fetchResult.getData(this);
    }




    public FopGetColumn(FetchSpec spec, JdbcColumn col, SelectExp se) {
        super(spec);
        this.column = col;
        this.se = se;
    }


    public SqlExp init(SelectExp root) {
        return column.toSqlExp(se);
    }

    public void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException {
        ResultSet rs = fetchResult.getResultSet();
        if (rs == null) return;
        fetchResult.setData(this, column.get(rs,
                spec.getProjectionIndex(this)
                ));

    }

    public String getDescription() {
        return null;
    }

    public FetchOpData getOutputData() {
        return null;
    }
}
