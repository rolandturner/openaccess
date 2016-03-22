
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
package com.versant.core.ejb;

import com.versant.core.ejb.common.EntrySet;
import com.versant.core.common.BindingSupportImpl;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is a linkedList EntrySet to be used by the LocalPMCache.
 */
public class LinkedListEntrySet extends EntrySet {
    private final LinkedEntry HEAD_POINTER = new LinkedEntry("TAIL");
    private LinkedEntry tail = HEAD_POINTER;

    public LinkedListEntrySet() {
    }

    public final Entry createEntry(Object key) {
        return new LinkedEntry(key);
    }

    public void clear() {
        super.clear(); 
        HEAD_POINTER.next = null;
        tail = HEAD_POINTER;
    }

    protected void entryAdded(Entry e) {
        LinkedEntry le = (LinkedEntry) e;
//        le.prev = tail;
        tail.next = le;
        tail = le;
    }

    public static class LinkedEntry extends Entry {
        private LinkedEntry next;

        public LinkedEntry(Object key) {
            super(key);
        }
    }

    /**
     * Create an iterator that will iterate over the entries in the order that they
     * were added. This iterator will not throw an ConcurrentUpdate exception when
     * entries are added while iterating.
     */
    public Iterator createLinkedListIterator() {
        return new LLIter(HEAD_POINTER);
    }

    private class LLIter implements Iterator {
        private LinkedEntry le;

        public LLIter(LinkedEntry le) {
            this.le = le;
        }

        public boolean hasNext() {
            return le != null && le.next != null;
        }

        public Object next() {
            if (le == null) {
                throw new NoSuchElementException();
            }
            le = le.next;
            return le;
        }

        public void remove() {
            throw BindingSupportImpl.getInstance().internal("Not Supported");
        }
    }
}
