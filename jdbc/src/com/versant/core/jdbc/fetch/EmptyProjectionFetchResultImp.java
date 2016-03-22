
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
import com.versant.core.common.Debug;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.server.StateContainer;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;

/**
 * This maintains information about a FetchSpec used during a fetch operation.
 * All the fields of the FetchSpec and FetchOp's themselves are read only
 * once the spec has been prepared. This makes it possible to use the same
 * FetchSpec from different threads simultaneously.
 *
 *
 * This fetchResult is for a FetchSpec that did not add any columns to the
 * project/selectList. This is valid for the case where only the pass2Fields
 * is required.
 */
public class EmptyProjectionFetchResultImp implements FetchResult {
    private boolean haveRow = true;
    private Object[] opData;
    private FetchSpec spec;
    private JdbcStorageManager sm;
    private Connection con;
    private boolean forUpdate;
    private boolean forCount;
    private Object[] params;
    private Object[] pass2Data;


    public EmptyProjectionFetchResultImp(FetchSpec spec,
            boolean scrollable, JdbcStorageManager sm, Connection con,
            boolean forUpdate, boolean forCount, Object[] params) {
        this.spec = spec;

        this.sm = sm;
        this.con = con;
        this.forUpdate = forUpdate;
        this.forCount = forCount;
        this.params = params;
    }

    public void setPass2Data(FetchOp op, Object val) {
        if (pass2Data == null) {
            pass2Data = new Object[spec.getFetchOpCount()];
        }
        pass2Data[op.getIndex()] = val;
    }

    public SqlBuffer.Param getParamList() {
        return spec.getParamList();
    }

    public void setHaveRow(boolean haveRow) {
        this.haveRow = haveRow;
    }

    public Object getDiscriminator(StateContainer stateContainer) {
        return spec.fetchDiscriminator(this, stateContainer);
    }

    public void fetchCachedDiscr(FetchOpDiscriminator fetchOpDiscriminator,
            StateContainer stateContainer) {
        throw new RuntimeException();
    }

    public Object getPass2Data(FetchOp op) {
        if (pass2Data == null) return null;
        return pass2Data[op.getIndex()];
    }

    public int getAbsoluteCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fetchPass2(StateContainer stateContainer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute() {
        //no-op
    }

    public boolean skip(int skip) {
        throw new RuntimeException("");
    }

    public void cancel() {
    }

    public void close() {
        spec.fetchResultClosed(this);
        opData = null;

        if (pass2Data != null) {
            for (int i = 0; i < pass2Data.length; i++) {
                Object o = pass2Data[i];
                if (o != null && o instanceof ParCollectionFetchResult) {
                    FetchResult fr =  ((ParCollectionFetchResult)o).getFetchResult();
                    if (fr != null) fr.close();
                }
            }
        }
    }

    public boolean isClosed() {
//        return ps == null;
        return false;
    }

    public boolean hasNext() {
        return haveRow;
    }

    public Object next(StateContainer stateContainer) {
        if (haveRow) {
            opData = new Object[spec.getFetchOpCount()];
            try {
                return spec.createRow(this, stateContainer);
            } finally {
                opData = null;
                haveRow = false;
            }
        } else {
            throw BindingSupportImpl.getInstance().internal("no more rows: " +
                    this);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean isScrollable() {
        return false;
    }

    public int size() {
        return 1;
    }

    public Object get(int index, StateContainer stateContainer) {
        throw BindingSupportImpl.getInstance().unsupported();
    }

    /**
     * Get data for op.
     */
    public Object getData(FetchOp op) {
        if (Debug.DEBUG) {
            checkOp(op);
        }
        return opData[op.getIndex()];
    }

    /**
     * Set data object for op.
     */
    public void setData(FetchOp op, Object data) {
        if (Debug.DEBUG) {
            checkOp(op);
        }
//        if (opData[op.getIndex()] != null) {
//            String val = "!!!!!!!!!!!!!!!!! Stomping data: current: " + opData[op.getIndex()] + " new " + data;
//            System.out.println("val = " + val);
//            new Exception().printStackTrace(System.out);
////            throw new RuntimeException(val);
//        }
        opData[op.getIndex()] = data;
    }

    private void checkOp(FetchOp op) {
        if (op == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "op is null: " + this);
        }
        if (op.getSpec() != spec) {
            throw BindingSupportImpl.getInstance().internal(
                    "spec mismatch: " + this + ", op.getSpec() = " +
                    op.getSpec() + ", op " + op);
        }
    }

    public String toString() {
        return "FetchResult 0x" +
                Integer.toHexString(System.identityHashCode(this)) +
                (isClosed() ? " CLOSED" :  "") +
                " spec " + spec;
    }

    public ResultSet getResultSet() {
        return null;
    }

    public FetchSpec getFetchSpec() {
        return spec;
    }

    public PreparedStatement getPreparedStatement() {
        return null;
    }

    public JdbcStorageManager getStorageManager() {
        return sm;
    }

    public Connection getConnection() {
        return con;
    }

    public boolean isForUpdate() {
        return forUpdate;
    }

    public boolean isForCount() {
        return forCount;
    }

    public Object[] getParams() {
        return params;
    }

    public boolean absolute(int row) {
        throw new RuntimeException("Not Implemented");
    }
}


