
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
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.server.StateContainer;
import com.versant.core.metadata.FetchGroupField;

import java.sql.SQLException;

/**
 * Fetch a collection by doing a join from the owner.
 */
public class FopOneToManyParCollectionFetch extends FetchOp {
    private FetchOpData src;
    private JdbcCollectionField colField;
    private SelectExp owningSe;
    private int level;
    private FetchGroupField fgField;
    private FetchSpec subSpec;
    private FetchFieldPath fetchPath;

    public FopOneToManyParCollectionFetch(FetchSpec spec, FetchOpData src, JdbcCollectionField colField,
                                          SelectExp owningSe, int level, FetchGroupField fgField, FetchFieldPath ffPath) {
        super(spec);
        if (level != 1) {
            throw BindingSupportImpl.getInstance().internal("This FetchOp can " +
                    "only be used for the first level fetch");
        } else {
            this.level = 1;
        }
        this.src = src;
        this.colField = colField;
        this.owningSe = owningSe;

        this.fgField = fgField;
        this.fetchPath = ffPath.getCopy();
    }

    public Object getResult(FetchResult fetchResult) {
        return super.getResult(fetchResult);
    }

    public void setFirstColIndex(int firstColIndex) {
        super.setFirstColIndex(firstColIndex);
        subSpec.offSetColIndex(firstColIndex - 1);
    }

    public SqlExp init(SelectExp root) {
        SelectExp pos = new SelectExp();

        if (!spec.isInstanceFetch()) {
            //add a marker FetchOp to the original FetchSpec
            //This FetchOp is not added not to be included in the FetchResult
            FopGetOID fopOid = new FopGetOID(spec, src, colField.fmd.classMetaData,
                    owningSe);
            spec.addDiscriminator(new FetchOpDiscriminator(spec, fopOid), false);
            spec.setOneToMany(true);
        }

        subSpec = new FetchSpec(pos, spec.getSqlDriver(), spec.isInstanceFetch());
        subSpec.getOptions().setUseParallelQueries(spec.getOptions().isUseParallelQueries());


        //Must do a one to many join for the collection.
        colField.addOneToManyJoin(subSpec, src, owningSe, fgField, level, spec, fetchPath);
        subSpec.finish(1);
        //Must update the startindex of the subspec
        return pos.selectList;
    }

    /**
     * Use the current result and process up to the end. This only works with a single
     * root at the moment.
     */
    public void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException {
        //the owner of the collection
        OID oid = src.getOID(fetchResult);
        if (oid == null) return;

//        determine if the owning state was fetched. If not the this is a nop
        State state = src.getState(fetchResult);
        if (state == null) return;

        if (!state.getClassMetaData().isAncestorOrSelf(colField.fmd.classMetaData)) {
            return;
        }

        colField.fillStateWithEmpty(state);

        FetchResult subFetchResult = (FetchResult) fetchResult.getData(this);
        if (subFetchResult == null) {
            subFetchResult = subSpec.createFetchResult(fetchResult);
            fetchResult.setData(this, subFetchResult);
        }

        colField.fetchOneToManyParCollection(subSpec, subFetchResult, state,
                fetchResult.getParams(), stateContainer, fetchResult);
        subSpec.fetchPass2(subFetchResult, fetchResult.getParams(), stateContainer);
        subFetchResult.close();
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
