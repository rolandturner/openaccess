
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
package com.versant.core.server;

import com.versant.core.jdo.QueryDetails;

import java.util.Map;
import java.util.HashMap;

/**
 * LRU map of QueryDetails -> CompiledQuery. Also assigns a unique ID to
 * each query added to the cache and supports lookup by this ID.
 */
public final class CompiledQueryCache {

    private final int maxSize;
    private final Map map = new HashMap(); //  QueryDetails -> Entry
    private final Map idMap = new HashMap(); //  Integer ID -> Entry
    private Entry head, tail;
    private int lastID;

    private class Entry {
        CompiledQuery cq;
        Entry prev, next;
    }

    public CompiledQueryCache(int maxSize) {
        this.maxSize = maxSize <= 0 ? 1000 : maxSize;
    }

    /**
     * Get the cached CompiledQuery for q or null if none. This will make it
     * the most recently used query.
     */
    public synchronized CompiledQuery get(QueryDetails q) {
        Entry e = (Entry)map.get(q);
        if (e == null) {
            return null;
        }
        removeFromLRUList(e);
        addToHeadOfLRUList(e);
        return e.cq;
    }

    /**
     * Get the cached CompiledQuery for id or null if none. This will make it
     * the most recently used query.
     */
    public synchronized CompiledQuery get(int id) {
        Entry e = (Entry)idMap.get(new Integer(id));
        if (e == null) {
            return null;
        }
        removeFromLRUList(e);
        addToHeadOfLRUList(e);
        return e.cq;
    }

    /**
     * Add cq to the cache. If there is already an entry for cq in the cache
     * then the current entry is returned and nothing is done. This avoids the
     * need to synchronize on this cache during query compilation. It is
     * possible that two threads might compile the same query after discovering
     * that it is not in cache but this is ok.
     */
    public synchronized CompiledQuery add(CompiledQuery cq) {
        Entry e = (Entry)map.get(cq.getQueryDetails());
        if (e != null) {
            return e.cq;
        }
        e = new Entry();
        e.cq = cq;
        int id = ++lastID;
        cq.setId(id);
        map.put(cq.getQueryDetails(), e);
        idMap.put(new Integer(id), e);
        addToHeadOfLRUList(e);
        for (int c = map.size() - maxSize; c > 0; c--) {
            map.remove(tail.cq.getQueryDetails());
            idMap.remove(new Integer(tail.cq.getId()));
            tail.next.prev = null;
            tail = tail.next;
        }
        return cq;
    }

    /**
     * Empty the cache.
     */
    public synchronized void clear() {
        map.clear();
        head = tail = null;
    }

    /**
     * Remove e from the double linked LRU list.
     */
    private void removeFromLRUList(Entry e) {
        if (e.prev != null) {
            e.prev.next = e.next;
        } else {
            tail = e.next;
        }
        if (e.next != null) {
            e.next.prev = e.prev;
        } else {
            head = e.prev;
        }
        e.next = e.prev = null;
    }

    /**
     * Add e to the head of the double linked LRU list. This will make it the
     * most recently accessed object.
     */
    private void addToHeadOfLRUList(Entry e) {
        e.next = null;
        e.prev = head;
        if (head != null) {
            head.next = e;
        }
        head = e;
        if (tail == null) {
            tail = e;
        }
    }

}

