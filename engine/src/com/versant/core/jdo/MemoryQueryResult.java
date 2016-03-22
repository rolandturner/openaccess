
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
package com.versant.core.jdo;

import com.versant.core.common.Debug;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.query.mem.BCodeQuery;
import com.versant.core.jdo.query.mem.BCodeSorter;
import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.query.mem.BCodeSorter;
import com.versant.core.jdo.query.mem.BCodeQuery;

/**
 * Results of an in memory query.
 */
public class MemoryQueryResult extends QueryResultBase {

    private final List internalList = new ArrayList();
    private boolean closed;
    private final List operIters = new ArrayList();
    boolean toSort = false;
    private BCodeQuery bCodeQuery = null;
    private final Class candidateClass;
    private final boolean checkExtentSubClasses;

    public MemoryQueryResult(PMProxy sm, QueryDetails queryDetails,
            Collection col, Object[] params) {
        checkExtentSubClasses = queryDetails.includeSubClasses();
        this.candidateClass = queryDetails.getCandidateClass();
        createDynamicQuery(sm.getRealPM(), queryDetails, params);

        filter(col, internalList, params, sm);
        int n = internalList.size();
        for (int i = n - 1; i >= 0; i--) {
            internalList.set(i, sm.getObjectById(internalList.get(i), false));
        }
        Collections.reverse(internalList);
    }

    public MemoryQueryResult() {
        candidateClass = null;
        checkExtentSubClasses = false;
    }

    public void filter(Collection toFilter, List results, Object[] params,
            PMProxy pm) {
//        long start = System.currentTimeMillis();
        results.clear();
        PCStateMan pcStateObject = null;
        for (Iterator iterator = toFilter.iterator(); iterator.hasNext();) {
            pcStateObject = (PCStateMan)iterator.next();
            if (checkType(pcStateObject) && checkFilter(pcStateObject, params)) {
                results.add(pcStateObject.oid);
            }
        }
        if (toSort) {
            sort(results, pm);
        }
//        if (Debug.DEBUG) {
//            Debug.out.println("Time for filter = " + (System.currentTimeMillis() - start));
//        }
    }

    private void sort(List list, PMProxy pm) {
        if (list == null || list.isEmpty()) {
            return;
        }
        BCodeSorter bCodeSorter = new BCodeSorter();
        bCodeSorter.sort(list, pm, bCodeQuery);
    }

    /**
     * Check if the type is valid. This will check that the class is of the right type and if it is a
     * sub class if allowed.
     *
     * @param pcStateObject
     */
    public final boolean checkType(PCStateMan pcStateObject) {
        if (checkExtentSubClasses) {
            return candidateClass.isAssignableFrom(/*CHFC*/pcStateObject.pc.getClass()/*RIGHTPAR*/);
        } else {
            return /*CHFC*/pcStateObject.pc.getClass()/*RIGHTPAR*/ == candidateClass;
        }
    }

    /**
     * Check if the instance is valid according to the filter.
     *
     * @param pcStateObject
     */
    public final boolean checkFilter(PCStateMan pcStateObject, Object[] params) {
        if (pcStateObject.isDeleted(null)) return false;
        try {
            boolean result = bCodeQuery.exec(pcStateObject.queryStateWrapper,
                    params);
            return result;
        } catch (Exception e) {
            if (Debug.DEBUG) {
                e.printStackTrace(Debug.OUT);
            }
            return false;
        }
    }

    private void createDynamicQuery(VersantPersistenceManagerImp sm,
            QueryDetails queryParams, Object[] params) {

        bCodeQuery = sm.getMemQueryCompiler().compile(queryParams,
                params);

        toSort = isOrdered(queryParams);
    }

    private final boolean isOrdered(QueryDetails queryParams) {
        return queryParams.getOrdering() != null;
    }

    public int size() {
        return internalList.size();
    }

    public boolean isEmpty() {
        return internalList.isEmpty();
    }

    public boolean contains(Object o) {
        return internalList.contains(o);
    }

    public Object[] toArray() {
        return internalList.toArray();
    }

    public Object[] toArray(Object a[]) {
        return internalList.toArray(a);
    }

    public boolean containsAll(Collection c) {
        return internalList.containsAll(c);
    }

    public Object get(int index) {
        return internalList.get(index);
    }

    public int indexOf(Object o) {
        return internalList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return internalList.lastIndexOf(o);
    }

    public Iterator iterator() {
        Iterator iter = new InternalIter(internalList.listIterator());
        operIters.add(iter);
        return iter;
    }

    public Iterator createInternalIter() {
        return iterator();
    }

    public Iterator createInternalIterNoFlush() {
        return iterator();
    }

    public ListIterator listIterator() {
        ListIterator iter = new InternalIter(internalList.listIterator());
        operIters.add(iter);
        return iter;
    }

    public ListIterator listIterator(int index) {
        ListIterator iter = new InternalIter(internalList.listIterator(index));
        operIters.add(iter);
        return iter;
    }

    public List subList(int fromIndex, int toIndex) {
        throw BindingSupportImpl.getInstance().unsupported("");
    }

    public void close() {
        closed = true;
        for (int i = 0; i < operIters.size(); i++) {
            ((InternalIter)operIters.get(i)).wrapperIter = null;
        }
        internalList.clear();
        operIters.clear();
    }

    public void setParams(Object[] params) {
    }

    public class InternalIter implements ListIterator {

        private ListIterator wrapperIter;

        public InternalIter(ListIterator wrapperIter) {
            this.wrapperIter = wrapperIter;
        }

        public boolean hasNext() {
            if (closed) {
                return false;
            }
            return wrapperIter.hasNext();
        }

        public Object next() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            return wrapperIter.next();
        }

        public boolean hasPrevious() {
            if (closed) {
                return false;
            }
            return wrapperIter.hasPrevious();
        }

        public Object previous() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            return wrapperIter.previous();
        }

        public int nextIndex() {
            if (closed) {
                return -1;
            }
            return wrapperIter.nextIndex();
        }

        public int previousIndex() {
            if (closed) {
                return -1;
            }
            return wrapperIter.previousIndex();
        }

        public void remove() {
            throw BindingSupportImpl.getInstance().invalidOperation("Modification is not allowed");
        }

        public void set(Object o) {
            throw BindingSupportImpl.getInstance().invalidOperation("Modification is not allowed");
        }

        public void add(Object o) {
            throw BindingSupportImpl.getInstance().invalidOperation("Modification is not allowed");
        }
    }
}
