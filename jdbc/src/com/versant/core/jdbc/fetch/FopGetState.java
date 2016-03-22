
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

import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.common.State;
import com.versant.core.common.OID;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.server.StateContainer;

import java.sql.SQLException;

/**
 * A fetch of a complete State (Entity) for an existing OID.
 */
public class FopGetState extends FetchOp {

    private final FetchOpData src;
    private final FetchGroup fg;
    private final boolean includeSubClasses;
    private final Data data;

    private SelectExp se;
    private int refLevel;
    private final FopGetType fopGetType;
    private FetchFieldPath fetchFieldPath;

    /**
     * This gets our state from fetchData and delegates to our src for
     * the OID and ResultSet.
     */
    public class Data extends FetchOpDataProxy {

        public Data(FetchOpData src) {
            super(src);
        }

        public void setState(FetchResult fetchResult, State state) {
            fetchResult.setData(FopGetState.this, state);
        }

        public State getState(FetchResult fetchResult) {
            return (State)fetchResult.getData(FopGetState.this);
        }

        public String getDescription() {
            return " [" + getIndex() + "]";
        }
    }

    /**
     * Create a State and populate it according to fg. If includeSubclasses is
     * true then fields for all possible subclasses are fetched. The src must
     * provide the OID of the instance being fetched and the ResultSet.
     */
    public FopGetState(FetchSpec spec, FetchOpData src, FetchGroup fg,
                       boolean includeSubClasses, SelectExp se, int refLevel, ClassMetaData cmd,
                       FetchFieldPath ffPath) {
        super(spec);
        this.fetchFieldPath = ffPath;
        if (fg == null) {
            fg = cmd.fetchGroups[0];
//            throw BindingSupportImpl.getInstance().internal("The supplied fetchGroup is null");
        }

        this.fg = fg;
        this.includeSubClasses = includeSubClasses
                && fg.classMetaData.pcSubclasses != null;

        this.se = se;
        this.refLevel = refLevel;

        if (this.includeSubClasses) {
            fopGetType = new FopGetType(spec, fg.classMetaData, src, fg, se);
            this.src = fopGetType.getOutputData();
        } else {
            fopGetType = null;
            this.src = src;
        }
        data = new Data(this.src);
    }

    /**
     * Adds itself to the FetchSpec. This also provides an opportunity to provide
     * other FetchOps before itself to the FetchSpec.
     */
    public void addToFSpec(boolean includeInResult, boolean prepend) {
        if (fopGetType != null) spec.addFetchOp(fopGetType, false);
        super.addToFSpec(includeInResult, prepend);
    }

    public FetchOpData getOutputData() {
        return data;
    }

    /**
     * Add in whatever columns we need to have in the select list and return
     * the last SqlExp we added.
     *
     * - may add new FetchOp's for superclass fields, subclass fields,
     * prefetched references, collection fields and so on to the plan
     */
    public SqlExp init(SelectExp root) {
        // create ops to fetch all super fetch groups
        for (FetchGroup g = fg; g != null; g = g.superFetchGroup) {
            SelectExp seToUse = se;
            //create join if nec.
            if (!seToUse.table.equals(((JdbcClass)g.classMetaData.storeClass).table)) {
                JdbcTable leftTable = seToUse.table;
                JdbcTable rightTable = ((JdbcClass)g.classMetaData.storeClass).table;
                SelectExp righSe = new SelectExp();
                righSe.table = rightTable;
                //ensure to create outer joins when the previous join is a outer join.
                righSe.outer = se.outer;

                seToUse.addJoin(leftTable.pk, rightTable.pk, righSe);
                seToUse = righSe;
            }
            processFetchGroup(g, seToUse);
        }

        // and recusively groups for all possible subclasses (if needed)
        if (includeSubClasses && fg.subFetchGroups != null) {
            processSubFetchGroups(fg.subFetchGroups, se);
        }
        return null;
    }

    private void processFetchGroup(FetchGroup g, SelectExp se) {
        spec.addFetchOp(new FopGetFetchGroup(spec, data, g, se, refLevel, fetchFieldPath), false);
    }

    private void processSubFetchGroups(FetchGroup[] subs, SelectExp root) {
        if (subs != null) {
            int n = subs.length;

            for (int i = 0; i < n; i++) {

                SelectExp se = root;
                FetchGroup fg = subs[i];
                JdbcTable leftTable = root.table;
                JdbcTable rightTable = ((JdbcClass)fg.classMetaData.storeClass).table;

                if (!leftTable.equals(rightTable)) {
                    //should add join
                    se = new SelectExp();
                    se.table = rightTable;
                    se.outer = true;
                    root.addJoin(leftTable.pk, rightTable.pk, se);
                }

                processFetchGroup(subs[i], se);
                processSubFetchGroups(fg.subFetchGroups, se);
            }
        }
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer)
            throws SQLException {
        OID oid = data.getOID(fetchResult);
        if (oid == null) {
            return; // nothing to fetch
        }
        if (stateContainer.containsKey(oid)) {
            State state = stateContainer.get(oid);
            if (state != null) {
                data.setState(fetchResult, state);
            }
            oid.resolve(state);
            return;
        }

        ClassMetaData cmd = fg.classMetaData;

        if (includeSubClasses) {
            ClassMetaData aCmd = src.getType(fetchResult);
            if (aCmd == null) {
                throw BindingSupportImpl.getInstance().internal("Unable to determine the correct type");
            }
            if (!aCmd.isAncestorOrSelf(cmd)) {
                throw BindingSupportImpl.getInstance().internal("ClassMetaData not determined correctly: "+
                                                            cmd.qname+" vs "+aCmd.qname);
            }
            cmd = aCmd;
        }

        State state = cmd.createState();
        oid.resolve(state);
        data.setState(fetchResult, state);
        stateContainer.add(oid, state);
    }

    public String getDescription() {
        return fg.classMetaData.qname +
                (includeSubClasses ? " and subclasses" : "") +
                data.getDescription();
    }

    public int getResultType() {
        return 0;
    }

    public Object getResult(FetchResult fetchResult) {
        return data.getState(fetchResult);
    }

    protected String toStringImp() {
        return "firstColIndex = " + getFirstColIndex();
    }

}

