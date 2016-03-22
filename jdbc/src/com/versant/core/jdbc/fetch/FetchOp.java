
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.server.StateContainer;

import java.sql.SQLException;
import java.io.PrintStream;

/**
 * On object or field fetch in an EJBQL query. An array of these is contructed
 * for the select list of an EJBQL query and any eager fetched fields (i.e.
 * fields in the default fetch group).
 */
public abstract class FetchOp {

    protected final FetchSpec spec;
    private int index;
    private int firstColIndex;  // the index of our first select list col
    protected SqlExp seExp;     // the exp that this fetchOp brings to the selectList


    public FetchOp(FetchSpec spec) {
        this.spec = spec;
    }

    public void setFirstColIndex(int firstColIndex) {
        this.firstColIndex = firstColIndex;
    }

    public void offsetFirstColIndex(int offset) {
        this.firstColIndex += offset;
    }

    public int getFirstColIndex() {
        return firstColIndex;
    }

    /**
     * Init this FetchOp and return the expressions that we need to be added
     * to the select list of the query or null if none. The FetchOp may
     * add additional FetchOp's to the spec.
     */
    public abstract SqlExp init(SelectExp root);

    /**
     * Return a FetchOpData that accesses the output we produce. This can be
     * used when creating other FetchOp's that need this output.
     */
    public abstract FetchOpData getOutputData();

    public void addToFSpec(boolean includeInResult, boolean prepend) {
        spec.addFetchOpImp(this, includeInResult, prepend);
    }

    /**
     * Fetch our data.
     */
    public abstract void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException;

    /**
     * Fetch the pass2 Data.
     */
    public void fetchPass2(FetchResult fetchResult, Object[] params,
            StateContainer stateContainer)
            throws SQLException {
    }

    /**
     * Get a one line user understandable description of this operation.
     */
    public abstract String getDescription();

    /**
     * Print a user understandable description of this operation.
     */
    public void printPlan(PrintStream p, String indent) {
        p.println(indent + getIndex() +  ": " + getName() + " " +
                getDescription());
    }

    /**
     * Get the name of this op. The default is the unqualified class name with
     * any Fop prefix removed.
     */
    public String getName() {
        String cname = getClass().getName();
        int i = cname.lastIndexOf(".Fop");
        if (i < 0) {
            i = cname.lastIndexOf('.') + 1;
        } else {
            i += 4;
        }
        return cname.substring(i);
    }

    /**
     * This is called after our spec has generated its SQL. Generate SQL for
     * any nested FetchSpec's and clear fields no longer needed (e.g. any
     * SqlExp type fields).
     */
    public void generateSQL() {
    }

    /**
     * This is invoked when a FetchResult from our FetchSpec is closed.
     * Closed any nested queries etc.
     */
    public void fetchResultClosed(FetchResult fetchResult) {
    }

    /**
     * Get an object to be included in fetch projection.
     */
    public Object getResult(FetchResult fetchResult) {
        throw notImplemented();
    }

    /**
     * Get the type of our result (INTW, OID, STRING etc).
     *
     * @see com.versant.core.metadata.MDStatics.OID
     */
    public int getResultType() {
        throw notImplemented();
    }

    public FetchSpec getSpec() {
        return spec;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public RuntimeException notImplemented() {
        return notImplemented("Not implemented for " + toString());
    }

    public RuntimeException notImplemented(String msg) {
        return internal(msg);
    }

    public RuntimeException internal(String msg) {
        return BindingSupportImpl.getInstance().internal(msg);
    }

    public String toString() {
        String s;
        try {
            s = toStringImp();
            s = s + " bla";
        } catch (Exception e) {
            s = "<toStringImp failed: " + e + ">";
        }
        return index + ": " + getName() + " " + getDescription() + "###### " +
                (s == null ? "" : " " + s);
    }

    /**
     * Override this to provide subclass specific info for toString that is
     * not already provided by getDescription.
     */
    protected String toStringImp() {
        return null;
    }

}

