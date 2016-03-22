
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
package com.versant.core.jdo.tools.workbench.model;

import java.util.*;


/**
 *
 */
public class MdEventListenerList {

    private List listenerList = new ArrayList();

    public void addListener(MdEventListener listener) {
        if (listener == null) return;
        listenerList.add(/*CHFC*/listener.getClass()/*RIGHTPAR*/);
        listenerList.add(listener);
    }

    public void removeListener(MdEventListener listener) {
        if (listener == null) return;
        int index;
        while ((index = listenerList.indexOf(listener)) > 0) {
            listenerList.remove(index);
            listenerList.remove(index - 1);
        }
    }

    public Iterator getListeners(Class listenerClass) {
        Set list = new HashSet(listenerList.size());
        int size = listenerList.size();
        for (int i = size - 2; i >= 0; i -= 2) {
            Class o = (Class)listenerList.get(i);
            if (listenerClass.isAssignableFrom(o)) {
                Object listener = listenerList.get(i + 1);
                if (!list.contains(listener)) {
                    list.add(listener);
                }
            }
        }
        return new MdEventListenerIterator(list.iterator());
    }

    private class MdEventListenerIterator implements Iterator {

        Iterator iterator;
        Object last;

        public MdEventListenerIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            last = iterator.next();
            return last;
        }

        public void remove() {
            removeListener((MdEventListener)last);
        }
    }
}
