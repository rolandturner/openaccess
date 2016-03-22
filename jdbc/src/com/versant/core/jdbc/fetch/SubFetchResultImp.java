
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
 * This is a FetchResult that shares the resultset with its owner. Most of
 * the methods delegate to the owner, except the ones that keep maintains its
 * own fetchspec data.
 */
public class SubFetchResultImp implements FetchResult {

    protected FetchSpec spec;
    protected Object[] opData;
    private FetchResult owner;
    private Object[] pass2Data;

    public SubFetchResultImp(FetchSpec spec, FetchResult owner) {
        this.spec = spec;
        this.opData = new Object[spec.getFetchOpCount()];
        this.owner = owner;
    }

    public void setPass2Data(FetchOp op, Object val) {
        if (pass2Data == null) {
            pass2Data = new Object[spec.getFetchOpCount()];
        }
        pass2Data[op.getIndex()] = val;
    }

    public void fetchCachedDiscr(FetchOpDiscriminator fetchOpDiscriminator,
            StateContainer stateContainer) {
        owner.fetchCachedDiscr(fetchOpDiscriminator, stateContainer);
    }

    public SqlBuffer.Param getParamList() {
        return owner.getFetchSpec().getParamList();
    }

    public Object getPass2Data(FetchOp op) {
        if (pass2Data == null) return null;
        return pass2Data[op.getIndex()];
    }

    public Object getDiscriminator(StateContainer stateContainer) {
        return owner.getDiscriminator(stateContainer);
    }

    public void setDiscriminator(Object val) {
        if (opData == null) opData = new Object[spec.getFetchOpCount()];
        opData[0] = val;
    }

    public int getAbsoluteCount() {
        throw BindingSupportImpl.getInstance().unsupported(
                "This operation is not supported on a 'SubFetchResult'");
    }

    public void fetchPass2(StateContainer stateContainer) {
        spec.fetchPass2(this, owner.getParams(), stateContainer);
    }

    public void execute() {
        //no-op
    }

    public void cancel() {
        owner.cancel();
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
        return owner.isClosed();
    }

    public boolean skip(int skip) {
        throw BindingSupportImpl.getInstance().unsupported(
                "This operation is not supported on a 'SubFetchResult'");
    }

    public boolean hasNext() {
        return owner.hasNext();
    }

    public Object next(StateContainer stateContainer) {
        if (hasNext()) {
            opData = new Object[spec.getFetchOpCount()];
            Object row = spec.createRow(this, stateContainer);
            owner.setHaveRow(false);
            opData = null;
            return row;
        } else {
            throw BindingSupportImpl.getInstance().internal("no more rows: " +
                    this);
        }
    }

    public void setHaveRow(boolean haveRow) {
    }

    public boolean isScrollable() {
        return false;
    }

    public int size() {
        return owner.size();
    }

    public Object get(int index, StateContainer stateContainer) {
        throw BindingSupportImpl.getInstance().unsupported("This operation is not supported on a 'SubFetchResult'");
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
        return "\n## SubFR" + owner.toString();
//        return "FetchResult 0x" +
//                Integer.toHexString(System.identityHashCode(this)) +
//                (isClosed() ? " CLOSED" :  "") +
//                " spec " + spec;
    }

    public ResultSet getResultSet() {
        return owner.getResultSet();
    }

    public FetchSpec getFetchSpec() {
        return spec;
    }

    public PreparedStatement getPreparedStatement() {
        return owner.getPreparedStatement();
    }

    public JdbcStorageManager getStorageManager() {
        return owner.getStorageManager();
    }

    public Connection getConnection() {
        return owner.getConnection();
    }

    public boolean isForUpdate() {
        return owner.isForUpdate();
    }

    public boolean isForCount() {
        return owner.isForCount();
    }

    public Object[] getParams() {
        return owner.getParams();
    }

    public boolean absolute(int row) {
        throw BindingSupportImpl.getInstance().unsupported("This operation is not supported on a 'SubFetchResult'");
    }
}

