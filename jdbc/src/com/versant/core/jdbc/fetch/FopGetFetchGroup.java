
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
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.JdbcState;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.server.StateContainer;
import com.versant.core.common.State;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Fetch fields for a FetchGroup into a State that must already exist.
 */
public class FopGetFetchGroup extends FetchOp {

    private final FetchOpData src;
    private final FetchGroup fg;

    /**
     * The selectExp to use for sql generation.
     */
    private SelectExp se;
    private int refLevel;
    private FetchFieldPath fetchFieldPath;

    /**
     * Create new to fetch fg. The src must provide the State and ResultSet.
     */
    public FopGetFetchGroup(FetchSpec spec, FetchOpData src, FetchGroup fg,
                            SelectExp se, int level, FetchFieldPath ffPath) {
        super(spec);
        this.src = src;
        this.fg = fg;
        this.se = se;
        this.refLevel= level;
        this.fetchFieldPath = ffPath;
    }

    public FetchOpData getOutputData() {
        return src;
    }

    public SqlExp init(SelectExp root) {
        SqlExp list = null, pos = null;
        FetchGroupField[] fields = fg.fields;
        int n = fields.length;
        for (int i = 0; i < n; i++) {
            FetchGroupField field = fields[i];
            if (field.fmd.isEmbeddedRef()) continue;

            JdbcField jdbcField = (JdbcField)field.fmd.storeField;
            SqlExp ce = jdbcField.createOwningTableColumnExpList(se);
            if (ce != null) {
                if (list == null) {
                    pos = list = ce;
                } else {
                    pos.setNext(ce);
                }
                //go to the tail
                for (; pos.getNext() != null; pos = pos.getNext());
            }

            if (!field.doNotFetchObject) {
                jdbcField.prepareFetch(spec, spec.getOptions(), se, refLevel + 1,
                        src, field, fetchFieldPath);
            }
        }
        return list;
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer)
            throws SQLException {
        JdbcState state = (JdbcState)src.getState(fetchResult);
        if (state == null) {
            return;     // nothing to fetch
        }
        /**
         * If cmd for this fg is == or a superClass then we should fetch else we
         * must nop
         */
        if (!((State)state).getClassMetaData().isAncestorOrSelf(fg.classMetaData)) {
            return;
        }
//        System.out.println("\n\n\nFopGetFetchGroup.fetch: index " + getIndex());
//        System.out.println("((State)state).getClassMetaData().qname = " + ((State) state).getClassMetaData().qname);
//        System.out.println("fg.classMetaData.qname = " + fg.classMetaData.qname);
        ResultSet rs = src.getResultSet(fetchResult);
        state.copyPass1Fields(rs, fg, spec.getProjectionIndex(this));
    }

    private ClassMetaData getType(FetchResult fetchResult) {
        if (src.getType(fetchResult) == null) return fg.classMetaData;
        return src.getType(fetchResult);
    }

    public String getDescription() {
        return fg.name + " on " + fg.classMetaData.getShortName() + " on table " + se.table.name + src.getDescription();
    }

    protected String toStringImp() {
        return "firstColIndex = " + spec.getProjectionIndex(this);
    }

}

