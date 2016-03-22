
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

import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * This is a Link List implementation of {@link PMCacheEntry} instances that must
 * be processed at the end of the transaction.
 */
public class TransactionalList {
    private final PMCacheEntry header = new PMCacheEntry();
    private int modCount;

    public TransactionalList() {
        header.processListNext = header.processListPrev = header;
    }

    private PMCacheEntry addBefore(PMCacheEntry o, PMCacheEntry e) {
        modCount++;
        o.processListNext = e;
        o.processListPrev = e.processListPrev;

        o.processListPrev.processListNext = o;
        o.processListNext.processListPrev = o;
        return o;
    }

    public boolean contains(PMCacheEntry ce) {
        if (header == ce) return true;
        return (ce.processListPrev != null || ce.processListNext != null);
    }

    public void clear() {
        modCount++;
        PMCacheEntry ce = header.processListNext;
        for (;ce != null && ce != header; ce = ce.processListNext) {
            ce.processListPrev.processListNext = null;
            ce.processListPrev = null;
        }
        header.processListPrev.processListNext = null;
        header.processListNext = header.processListPrev = header;
    }

    public void add(PCStateMan sm) {
        if (!contains(sm.cacheEntry)) {
            addBefore(sm.cacheEntry, header);
        }
    }

    public void remove(PCStateMan sm) {
        if (contains(sm.cacheEntry)) {
            removeImp(sm.cacheEntry);
        }
    }

    private void removeImp(PMCacheEntry ce) {
        if (contains(ce)) {
            modCount++;
            ce.processListPrev.processListNext = ce.processListNext;
            ce.processListNext.processListPrev = ce.processListPrev;
            ce.processListPrev = null;
            ce.processListNext = null;
        }
    }

    public Iterator iterator() {
        return new Iter(header, modCount);
    }

    class Iter implements Iterator {
        PMCacheEntry current;
        private int iterModCount;

        public Iter(PMCacheEntry current, int iterModCount) {
            this.current = current;
            this.iterModCount = iterModCount;
        }

        public void remove() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasNext() {
            checkConcurrent();
            return current.processListNext != header;
        }

        private void checkConcurrent() {
            if (iterModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }

        public Object next() {
            if (hasNext()) {
                current = current.processListNext;
            } else {
                throw new NoSuchElementException();
            }
            return current;
        }
    }
}
