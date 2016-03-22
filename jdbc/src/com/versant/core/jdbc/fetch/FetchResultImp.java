
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
import com.versant.core.common.OID;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.server.StateContainer;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * This maintains information about a FetchSpec used during a fetch operation.
 * All the fields of the FetchSpec and FetchOp's themselves are read only
 * once the spec has been prepared. This makes it possible to use the same
 * FetchSpec from different threads simultaneously.
 */
public final class FetchResultImp implements FetchResult {

    protected FetchSpec spec;
    protected String sql;
    private PreparedStatement ps;
    private ResultSet rs;

    protected boolean scrollable;
    protected boolean onRow;
    protected boolean rsNextReturnedFalse;
    protected Object[] opData;
    protected Object[] pass2Data;
    protected JdbcStorageManager sm;
    protected Connection con;
    protected boolean forUpdate;
    protected boolean forCount;
    protected Object[] params;
    private boolean oneToMany;

    private int lastRowNo = -1;
    private Object lastVal;

    public FetchResultImp(FetchSpec spec, PreparedStatement ps, String sql,
            boolean scrollable, JdbcStorageManager sm, Connection con,
            boolean forUpdate, boolean forCount,
            Object[] params) {
        this.spec = spec;
        this.ps = ps;
        this.sql = sql;
        this.scrollable = scrollable;
        this.opData = new Object[spec.getFetchOpCount()];
        this.sm = sm;
        this.con = con;
        this.forUpdate = forUpdate;
        this.forCount = forCount;
        this.params = params;
    }

    public FetchResultImp(FetchSpec spec, PreparedStatement ps, ResultSet rs,
            String sql, boolean scrollable, JdbcStorageManager sm,
            Connection con, boolean forUpdate, boolean forCount,
            Object[] params) {
        this(spec, ps, sql, scrollable, sm, con, forUpdate,  forCount, params);
        this.rs = rs;
    }

    public SqlBuffer.Param getParamList() {
        return spec.getParamList();
    }

    public FetchResultImp(FetchSpec spec, PreparedStatement ps, String sql,
            boolean scrollable, JdbcStorageManager sm,
            Connection con, boolean forUpdate, boolean forCount,
            Object[] params, boolean oneToMany) {
        this(spec, ps, sql, scrollable, sm, con, forUpdate,  forCount, params);
        this.oneToMany = oneToMany;
    }

    /**
     * This method must be used instead of calling rs.next directly to avoid
     * problems with JDBC drivers that fail if rs.next is called again after
     * returning false once.
     */
    private boolean rsNext() throws SQLException {
        if (rsNextReturnedFalse) {
            return false;
        }
        boolean ans = rs.next();
        if (!ans) {
            rsNextReturnedFalse = true;
        }
        return ans;
    }

    public void execute() {
        if (rs != null) {
            throw BindingSupportImpl.getInstance().internal(
                    "query has already been executed");
        }
        checkClosed();
        try {
            rs = ps.executeQuery();
        } catch (Exception e) {
            throw spec.getSqlDriver().mapException(e,
                    "Error executing query: " + e + "\nSQL:\n" + sql,
                    true);
        }
    }

    public void cancel() {
        if (rs != null) {
            try {
                ps.cancel();
            } catch (SQLException e) {
                throw spec.getSqlDriver().mapException(e, e + "\nSQL:\n" + sql,
                        true);
            }
        }
    }

    public void close() {
        if (isClosed()) {
            return;
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // ignore
            }
            rs = null;
        }
        try {
            ps.close();
        } catch (SQLException e) {
            // ignore
        }
        ps = null;
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
        return ps == null && rs == null;
    }
    
    public boolean skip(int skip) {
        opData = null;
        if (!hasNext()) {
            opData = null;
            return false;
        }
        if (onRow) {
            skip--;
        }
        try {
            if (oneToMany) {
                if (!onRow && rsNext()) return false;
                
                Object discr = getDiscriminator(null);
                for (int i = 0; i <= skip; i++) {
                    for (;;) {
                        if (!rsNext()) return false;
                        Object nDiscr = getDiscriminator(null);
                        if (!discr.equals(nDiscr)) {
                            //next logical row
                            discr = nDiscr;
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i <= skip; i++) {
                    if (!rsNext()) return false;
                }
            }
            return hasNext();
        } catch (SQLException e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public boolean absolute(int row) {
        checkScrollable();
        try {
            opData = null;
            return onRow = rs.absolute(row);
        } catch (SQLException e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public int getAbsoluteCount() {
        checkScrollable();
        try {
            checkClosed();
            opData = null;
            if (rs == null) {
                execute();
            }
            rs.last();
            return rs.getRow();
        } catch (SQLException e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public static String getRsRowNo(ResultSet rs) {
        if (rs == null) return "rs.isNull";
        try {
            return "" + rs.getRow();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean hasNext() {
        if (ps == null) {
            return false;
        }
        if (rs == null) {
            execute();
        }
        try {
            return onRow || (onRow = rsNext());
        } catch (Exception e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public void setHaveRow(boolean haveRow) {
        this.onRow = haveRow;
    }

    /**
     * The discrimator is always the first FetchOp in the FetchSpec.
     */
    public Object getDiscriminator(StateContainer stateContainer) {
        fetchDiscr(spec.getDiscriminator(), stateContainer);
        return lastVal;
    }

    public Object next(StateContainer stateContainer) {
        if (onRow || hasNext()) {
            final int nRow = getRsRow();
            try {
                if (opData == null) opData = new Object[spec.getFetchOpCount()];
                return spec.createRow(this, stateContainer);
            } finally {
                opData = null;
                onRow = (nRow != getRsRow()) && !rsNextReturnedFalse;
            }
        } else {
            throw BindingSupportImpl.getInstance().internal("no more rows: " +
                    this);
        }
    }

    public void fetchCachedDiscr(FetchOpDiscriminator fetchOpDiscriminator,
            StateContainer stateContainer) {
        fetchDiscr(fetchOpDiscriminator, stateContainer);
    }

    private void fetchDiscr(FetchOpDiscriminator discr,
            StateContainer stateContainer) {
        int currentRowNo = getRsRow();
        if (currentRowNo != lastRowNo) {
            //must fetch from the
            try {
                if (opData == null) opData = new Object[spec.getFetchOpCount()];
                discr.fetchImp(this, stateContainer);
            } catch (SQLException e) {
                throw spec.getSqlDriver().mapException(e, e.toString() + "\nProcessing " +
                        discr.getDescription(),
                        true);
            }
            onRow = true;
            lastRowNo = currentRowNo;
            lastVal = getData(discr);
        } else {
            if (opData == null) opData = new Object[spec.getFetchOpCount()];
            setData(discr, lastVal);
        }
    }

    private int getRsRow() {
        try {
            return rs.getRow();
        } catch (SQLException e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public void setPass2Data(FetchOp op, Object val) {
        if (pass2Data == null) {
            pass2Data = new Object[spec.getFetchOpCount()];
        }
        pass2Data[op.getIndex()] = val;
    }

    public Object getPass2Data(FetchOp op) {
        if (pass2Data == null) return null;
        return pass2Data[op.getIndex()];
    }

    public void fetchPass2(StateContainer stateContainer) {
        spec.fetchPass2(this, params, stateContainer);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean isScrollable() {
        return scrollable;
    }

    private void checkClosed() {
        if (ps == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "FetchResult '" + this + "' has been closed");
        }
    }

    public int size() {
        if (Debug.DEBUG) {
            checkScrollable();
        }
        try {
            if (rs == null) {
                execute();
            }
            opData = null;
            if (rs.last()) {
                return rs.getRow();
            } else {
                return 0;
            }
        } catch (Exception e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    public Object get(int index, StateContainer stateContainer) {
        if (Debug.DEBUG) {
            checkScrollable();
        }
        try {
            if (rs == null) {
                execute();
            }
            opData = null;
            if (rs.absolute(index + 1)) {
                return spec.createRow(this, stateContainer);
            } else {
                throw BindingSupportImpl.getInstance().internal(
                        "invalud index " + index + ": " + this);
            }
        } catch (Exception e) {
            throw spec.getSqlDriver().mapException(e, e.toString(), true);
        }
    }

    private void checkScrollable() {
        if (!scrollable) {
            throw BindingSupportImpl.getInstance().internal("not scrollable: " +
                    this);
        }
    }

    /**
     * Get data for op.
     */
    public Object getData(FetchOp op) {
//        checkClosed();
        if (op == null) {
            throw new NullPointerException("The FetchOp is null");
        }
        if (Debug.DEBUG) {
            checkOp(op);
        }
        return opData[op.getIndex()];
    }

    public void dumpOpData() {
        System.out.println("\nFetchResultImp.dumpOpData");
        spec.printPlan(System.out, "");
        System.out.println("\n");
        for (int i = 0; i < opData.length; i++) {
            Object o = opData[i];
            if (o instanceof OID) {
                System.out.println("o = " + ((OID)o).toStringImp());
            } else {
                System.out.println("o = " + o);
            }
        }
    }

    /**
     * Set data object for op.
     */
    public void setData(FetchOp op, Object data) {
        if (Debug.DEBUG) {
            checkOp(op);
            if (opData[op.getIndex()] != null
                    && !opData[op.getIndex()].equals(data)
                    && (spec.getDiscriminator().getIndex() != op.getIndex())) {
                throw BindingSupportImpl.getInstance().internal("!!!!!!!!!!!!!!!!! " +
                        "Stomping data: current: " + opData[op.getIndex()]
                        + " new " + data);
            }
        }
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
                " spec " + spec + " onRow " + onRow;
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public FetchSpec getFetchSpec() {
        return spec;
    }

    public PreparedStatement getPreparedStatement() {
        return ps;
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
}

