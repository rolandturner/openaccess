
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
import com.versant.core.jdbc.metadata.JdbcCollectionField;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.server.StateContainer;
import com.versant.core.metadata.FetchGroupField;

import java.sql.SQLException;

/**
 * FetchOp to fetch a collection field with a separate query for each collection.
 */
public class FopGetCollection extends FetchOp {
    private FetchOpData src;
    private JdbcCollectionField colField;
    private SelectExp owningSe;
    private int refLevel;
    private FetchGroupField fgField;

    public FopGetCollection(FetchSpec spec, FetchOpData src,
                            JdbcCollectionField colField, SelectExp owningSe, int refLevel, FetchGroupField fgField) {
        super(spec);
        this.src = src;
        this.colField = colField;
        this.owningSe = owningSe;
        this.refLevel = refLevel;
        this.fgField = fgField;
    }

    public Object getResult(FetchResult fetchResult) {
        return super.getResult(fetchResult);
    }

    public SqlExp init(SelectExp root) {
        return null;
    }

    /**
     * Must create a resultset and fetch all the data.
     */
    public void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException {
        OID oid = src.getOID(fetchResult);
        if (oid == null) return;

//        determine if the owning state was fetched. If not the this is a nop
        State state = src.getState(fetchResult);
        if (state == null) return;

        if (!state.getClassMetaData().isAncestorOrSelf(colField.fmd.classMetaData)) {
            return;
        }

        colField.fillStateWithEmpty(state);
        colField.fetchSingleFetch(fetchResult, stateContainer,
                new Object[] {oid}, state, owningSe, refLevel, src, fgField);
    }

    public void fetchPass2(FetchResult fetchResult, Object[] params,
            StateContainer stateContainer)
            throws SQLException {
    }

    public String getDescription() {
        return colField.fmd.name;
    }

    public FetchOpData getOutputData() {
        return null;
    }
}
