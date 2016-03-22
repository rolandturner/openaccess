
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
 * FetchOp to retrieve collection fields by means of a parallel fetch. This means
 * that all the collection entries for many owners is fetch in one query.
 */
public class FopParCollectionFetch extends FetchOp {
    private FetchOpData src;
    private JdbcCollectionField colField;
    private SelectExp owningSe;
    private int level;
    private FetchGroupField fgField;
    private FetchFieldPath fetchPath;

    public FopParCollectionFetch(FetchSpec spec, FetchOpData src,
                                 JdbcCollectionField colField, SelectExp owningSe, int level,
                                 FetchGroupField fgField, FetchFieldPath ffPath) {
        super(spec);
        this.src = src;
        this.colField = colField;
        this.owningSe = owningSe;
        this.level = level;
        this.fgField = fgField;

//      //follow the join path back to the root exp
        this.fetchPath = ffPath.getCopy();
    }


    /**
     * Create a copy of the join path that leads to this collection.
     * @param rootOfExp This is the selectExp that will be the root of the returned
     * exp.
     */
    public SelectExp createJoinPath(SelectExp rootOfExp) {
        return fetchPath.createJoinPath(rootOfExp);
    }

    public FetchFieldPath getFetchFieldPathCopy() {
        return fetchPath.getCopy();
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
    }

    public void fetchPass2(FetchResult fetchResult, Object[] params,
            StateContainer stateContainer)
            throws SQLException {
        colField.fetchParFetch(fetchResult, stateContainer, params,
                spec, owningSe, src, fgField, level,
                (ParCollectionFetchResult) fetchResult.getPass2Data(this), this, this, fetchPath);
    }

    public String getDescription() {
        return colField.fmd.name;
    }

    public FetchOpData getOutputData() {
        return null;
    }
}
