
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
package com.versant.core.common;

import com.versant.core.jdo.QueryResultBase;
import com.versant.core.metadata.MDStatics;
import com.versant.core.server.CompiledQuery;
import com.versant.core.server.CachedQueryResult;
import com.versant.core.storagemanager.ApplicationContext;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.FastExternalizable;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * This is used to transport the query results to the client.
 */
public final class QueryResultContainer implements FastExternalizable {

    public static final Object[] EMPTY_ARRAY = new Object[0];
    /**
     * The compiled qeury that this results is for.
     */
    private CompiledQuery cq;

    public StatesReturned container;
    /**
     * A flag to indicate that this is the last results from the results.
     * i.e. for a forward only result set this will be the end.
     */
    public boolean qFinished;
    /**
     * The query results.
     */
    private Object[] data;
    private int size;
    private static final int DEFAULT_SIZE = 20;
    public boolean allResults;

    public QueryResultContainer() {
    }

    public QueryResultContainer(StatesReturned container) {
        this.container = container;
    }

    public QueryResultContainer(ApplicationContext context,
            CompiledQuery cq) {
        container = new StatesReturned(context);
        init(cq);
    }

    /**
     * Add a row to the results.
     *
     * @param val
     */
    public void addRow(Object val) {
        if (val == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "Adding a 'Null' value to query result is not supported.s");
        }
        checkSize();
        data[size++] = val;
    }

    private void checkSize() {
        if (data == null) {
            data = new Object[DEFAULT_SIZE];
        } else if (size == data.length) {
            Object[] t = new Object[size * 2];
            System.arraycopy(data, 0, t, 0, size);
            data = t;
        }
    }

    public boolean isqFinished() {
        return qFinished;
    }

    public void setqFinished(boolean qFinished) {
        this.qFinished = qFinished;
    }

    /**
     * This must be called after use.
     * This clears all the internal datastructures.
     */
    public void reset() {
        data = null;
        container.clear();
        qFinished = false;
        size = 0;
        allResults = false;
    }

    /**
     * This must be called on a query before use.
     */
    public void init(CompiledQuery cq, int capacity) {
        reset();
        this.cq = cq;
        if (capacity < DEFAULT_SIZE) {
            data = new Object[capacity];
        } else {
            data = new Object[DEFAULT_SIZE];
        }
    }

    /**
     * This must be called on a query before use.
     * This will not init the data array.
     */
    public void init(CompiledQuery cq) {
        reset();
        this.cq = cq;
    }

    /**
     * Add this query results to the stack.
     * The container will be in a 'reset' state after calling this.
     */
    public void addToQueryStack(Stack stack) {
        if (size != 0) {
            stack.add(data, size);
        }
        data = null;
        size = 0;
    }

    /**
     * This return's the backing array of the container itself.
     * You must use size() or iterate to first null to determine the amount of valid entries.
     */
    public Object[] toResolvedObject(VersantPMProxy pm) {
        int size = this.size;
        if (size == 0) {
            return EMPTY_ARRAY;
        }
        Object[] res = data;
        for (int i = 0; i < size; i++) {
            res[i] = QueryResultBase.resolveRow(res[i], pm);
        }
        return res;
    }

    public int size() {
        return size;
    }

    public Object getUnique() {
        if (data == null) return null;
        else return data[0];
    }

    public Object get(int index) {
        return data[index];
    }

    /**
     * Add the query results to the list and resolve any oids while doing so.
     */
    public void resolveAndAddTo(List l, VersantPMProxy pm) {
        int size = this.size;
        for (int i = 0; i < size; i++) {
            l.add(QueryResultBase.resolveRow(data[i], pm));
        }
    }

    /**
     * Add all our results to the cache container. This is used when the query results
     * can be cached.
     */
    public void addResultsTo(CachedQueryResult cacheResults,
            boolean copy) {
        int size = this.size;
        if (cacheResults.results == null) {
            cacheResults.results = new ArrayList(size);
        }

        if (copy && size > 0 && data[0] instanceof Object[]) {
            // Duplicate the Object[] returned for each result for a projection
            // query as the client might write to the Object[] and hence change
            // the data in the level 2 cache.
            for (int i = 0; i < size; i++) {
                Object[] val = (Object[])data[i];
                Object[] cop = new Object[val.length];
                System.arraycopy(val, 0, cop, 0, val.length);
                cacheResults.results.add(cop);
            }                
        } else {
            for (int i = 0; i < size; i++) {
                if (Debug.DEBUG) {
                    if (data[i] == null) {
                        throw BindingSupportImpl.getInstance().internal("");
                    }
                }
                cacheResults.results.add(data[i]);
            }
        }
        container.addIndirectOIDs(cacheResults);
    }

    /**
     * This is used to fill this container from cached data.
     */
    public void fillFrom(CachedQueryResult qCacheContainer) {
        final ArrayList lData = qCacheContainer.results;
        final int n = size = lData.size();
        if (n == 0) {
            data = null;
            return;
        }
        
        if (data == null || n > data.length) {
            data = new Object[n];
            for (int i = 0; i < n; i++) {
                data[i] = lData.get(i);
            }
        }
        size = n;
        for (int i = 0; i < n; i++) {
            data[i] = lData.get(i);
        }

    }

    public void dump() {
        System.out.println(
                "\n\n$$$$$$$$$$$$$ START QueryResultContainer.dump $$$$$$$$$$$$$");
        System.out.println("results = " + size);
        int n = size;
        for (int i = 0; i < n; i++) {
            System.out.println("results.get(i) = " + data[i]);
        }
        System.out.println(
                "$$$$$$$$$$$$$ END QueryResultContainer.dump $$$$$$$$$$$$$\n\n");
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        if (cq.isDefaultResult()) {
            container.ensureDirect(data);
        }
        container.writeExternal(out);
        if (allResults) {
            out.writeByte(1);
        } else {
            out.writeByte(0);
        }
        out.writeBoolean(qFinished);

        if (data == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(size);
            if (!cq.isDefaultResult()) {
                out.writeBoolean(false);
                int[] typeCodes = cq.getResultTypeCodes();
                if (typeCodes == null || typeCodes.length == 0) {
                    out.writeInt(0);
                    for (int i = 0; i < size; i++) {
                        out.writeObject(data[i]);
                    }
                } else {
                    out.writeInt(typeCodes.length);
                    for (int i = 0; i < typeCodes.length; i++) {
                        out.writeInt(typeCodes[i]);
                    }
                    if (typeCodes.length == 1) {
                        int typeCode = typeCodes[0];
                        for (int i = 0; i < size; i++) {
                            Object o = data[i];
                            if (typeCode == MDStatics.OID) {
                                OID oid = (OID)o;
                                out.write(oid);
                            } else {
                                SerUtils.writeSimpleField(typeCode, out, o);
                            }
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            Object[] row = (Object[])data[i];
                            for (int j = 0; j < row.length; j++) {
                                Object o = row[j];
                                if (typeCodes[j] == MDStatics.OID) {
                                    OID oid = (OID)o;
                                    out.write(oid);
                                } else {
                                    SerUtils.writeSimpleField(typeCodes[j], out, o);
                                }
                            }
                        }
                    }
                }
            } else {
                out.writeBoolean(true);
                for (int i = 0; i < size; i++) {
                    out.writeObject(data[i]);
                }
            }
        }
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        container = new StatesReturned();
        container.readExternal(in);
        allResults = in.readByte() == 1;
        qFinished = in.readBoolean();
        size = in.readInt();
        if (size < 0) {
            data = null;
            size = 0;
        } else {
            data = new Object[size];
            if (!in.readBoolean()) {
                int tcLen = in.readInt();
                if (tcLen == 0) {
                    for (int i = 0; i < size; i++) {
                        data[i] = in.readObject();
                    }
                } else {
                    int[] typeCodes = new int[tcLen];
                    for (int i = 0; i < typeCodes.length; i++) {
                        typeCodes[i] = in.readInt();
                    }
                    if (typeCodes.length == 1) {
                        int typeCode = typeCodes[0];
                        for (int i = 0; i < size; i++) {
                            if (typeCode == MDStatics.OID) {
                                data[i] = in.readOID();
                            } else {
                                data[i] = SerUtils.readSimpleField(typeCode, in);
                            }
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            Object[] row = new Object[typeCodes.length];
                            data[i] = row;
                            for (int j = 0; j < row.length; j++) {
                                if (typeCodes[j] == MDStatics.OID) {
                                    row[j] = in.readOID();
                                } else {
                                    row[j] = SerUtils.readSimpleField(typeCodes[j], in);
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    data[i] = in.readObject();
                }
            }
        }
    }

    /**
     * Will this be null if there is no data?
     */
    public Object[] getDataArray() {
        return data;
    }
}
