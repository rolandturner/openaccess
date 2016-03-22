
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
 * List that supports insert, update and delete events when objects are
 * added, changed or removed. Delegates all operations to a backing list.
 * Listeners can throw IllegalArgumentExceptions to veto add and remove
 * operations. Bulk operations generate listUpdated events but cannot
 * be vetoed.<p>
 * <p/>
 * This class also maintains sets of dirty and removed objects. Objects are
 * put into the removed set when remove(Object) or remove(int) is called.
 * Objects are put into the dirty set when fireObjectUpdated is called. This
 * method will also remove them from the removed set if they are in it. The
 * application must ensure that fireObjectUpdated is called when objects
 * are modified. Calling clear will empty both sets.<p>
 * <p/>
 * The dirty and removed sets can be disabled completely by calling
 * setNoDirtyChecking(true).<p>
 *
 * @see MdListListener
 */
public class MdObservableList implements List {

    protected List list;
    protected MdEventListenerList listenerList = new MdEventListenerList();
    protected HashSet dirty;
    protected HashSet removed;
    protected boolean noDirtyChecking;

    /**
     * Create to delegate to a new ArrayList.
     */
    public MdObservableList() {
        this(new ArrayList());
    }

    /**
     * Create to delegate to list.
     */
    public MdObservableList(List list) {
        setList(list);
    }

    /**
     * Create to delegate to list created from contents.
     */
    public MdObservableList(Object[] contents) {
        this(Arrays.asList(contents));
    }

    /**
     * Set this to true to disable all dirty checking behaviour.
     */
    public void setNoDirtyChecking(boolean on) {
        noDirtyChecking = on;
    }

    public boolean isNoDirtyChecking() {
        return noDirtyChecking;
    }

    /**
     * Get the set of dirty objects in this list. This may return null or an
     * empty set if there are no dirty objects.
     */
    public HashSet getDirty() {
        return dirty;
    }

    /**
     * Get the set of objects that have been removed from the list. This may
     * return null or an empty set if no objects have been removed.
     */
    public HashSet getRemoved() {
        return removed;
    }

    /**
     * Are there any dirty or removed objects?
     */
    public boolean isDirty() {
        return (dirty != null && !dirty.isEmpty())
                || (removed != null && !removed.isEmpty());
    }

    /**
     * Is the object a dirty object (NOT a removed object). This always
     * returns true if noDirtyChecking is true.
     */
    public boolean isDirty(Object o) {
        return noDirtyChecking || (dirty != null && dirty.contains(o));
    }

    /**
     * Put the object into the dirty set and remove it from the removed set.
     * NOP if dirty checking has been disabled.
     */
    public void makeDirty(Object o) {
        if (o == null) return;
        if (!noDirtyChecking) {
            if (dirty == null) dirty = new HashSet();
            dirty.add(o);
            if (removed != null) removed.remove(o);
        }
    }

    /**
     * Put the object into the removed set and remove it from the dirty set.
     * NOP if dirty checking has been disabled.
     */
    public void makeRemoved(Object o) {
        if (!noDirtyChecking) {
            if (removed == null) removed = new HashSet();
            removed.add(o);
            if (dirty != null) dirty.remove(o);
        }
    }

    /**
     * Remove the object from the dirty and removed sets.
     * NOP if dirty checking has been disabled.
     */
    public void makeClean(Object o) {
        if (!noDirtyChecking) {
            if (dirty != null) dirty.remove(o);
            if (removed != null) removed.remove(o);
        }
    }

    /**
     * Set the list we delegate to. This will fire a listUpdated event to our
     * listeners.
     */
    public void setList(List list) {
        this.list = list;
        dirty = removed = null;
        fireListUpdated();
    }

    /**
     * Get the list we delegate to.
     */
    public List getList() {
        return list;
    }

    /**
     * Add a listener for list events.
     */
    public void addMdListListener(MdListListener x) {
        listenerList.addListener(x);
    }

    /**
     * Remove a listener for list events.
     */
    public void removeMdListListener(MdListListener x) {
        listenerList.removeListener(x);
    }

    /**
     * Send a listUpdated event to all listeners. This is public as it can be
     * called if some outside code has changed many of the objects in the
     * list.
     */
    public void fireListUpdated() {
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.listUpdated(this);
        }
    }

    /**
     * Send a objectUpdated event to all listeners. This will also add the
     * object to the dirty set and remove it from the removed set if dirty
     * checking has not been disabled.
     *
     * @param index Use -1 if the index is not available
     * @param o     The object that has changed
     * @param event Optional information passed to each listener
     */
    public void fireObjectUpdated(int index, Object o, Object event) {
        makeDirty(o);
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.objectUpdated(this, index, o, event);
        }
    }

    /**
     * Send a objectUpdated event to all listeners. This will also add the
     * object to the dirty set and remove it from the removed set if dirty
     * checking has not been disabled.
     *
     * @param index Use -1 if the index is not available
     * @param event Optional information passed to each listener
     */
    public void fireObjectUpdated(int index, Object event) {
        if (index < 0 || index >= list.size()) {
            fireObjectUpdated(index, null, event);
        } else {
            fireObjectUpdated(index, list.get(index), event);
        }
    }

    /**
     * Send a objectUpdated event to all listeners. This will also add the
     * object to the dirty set and remove it from the removed set if dirty
     * checking has not been disabled.
     *
     * @param o     The object that has changed
     * @param event Optional information passed to each listener
     */
    public void fireObjectUpdated(Object o, Object event) {
        fireObjectUpdated(-1, o, event);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public Object[] toArray(Object a[]) {
        return list.toArray(a);
    }

    /**
     * Add the object to the list. Listeners may veto the add by throwing
     * an illegal argument exception.
     */
    public boolean add(Object o) {
        fireObjectInserting(-1, o);
        boolean ans = list.add(o);
        fireObjectInserted(-1, o);
        return ans;
    }

    /**
     * Remove the object from the list. Listeners may veto the remove.
     * This will add the object to the removed set and remove it from the dirty
     * set if dirty checking has not been disabled.
     */
    public boolean remove(Object o) {
        fireObjectDeleting(-1, o);
        boolean ans = list.remove(o);
        if (ans) makeRemoved(o);
        fireObjectDeleted(-1, o);
        return ans;
    }

    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    public boolean addAll(Collection c) {
        boolean ans = list.addAll(c);
        fireListUpdated();
        return ans;
    }

    public boolean addAll(int index, Collection c) {
        boolean ans = list.addAll(index, c);
        fireListUpdated();
        return ans;
    }

    public boolean removeAll(Collection c) {
        boolean ans = list.removeAll(c);
        fireListUpdated();
        return ans;
    }

    public boolean retainAll(Collection c) {
        boolean ans = list.retainAll(c);
        fireListUpdated();
        return ans;
    }

    /**
     * Remove all objects from this list. This will empty the dirty and
     * removed sets as well.
     */
    public void clear() {
        list.clear();
        if (dirty != null) dirty.clear();
        if (removed != null) removed.clear();
        fireListUpdated();
    }

    public boolean equals(Object o) {
        return list.equals(o);
    }

    public int hashCode() {
        return list.hashCode();
    }

    public Object get(int index) {
        return list.get(index);
    }

    /**
     * Replace the object at index with a new one. Fires an objectUpdated
     * event with the object previously at index as the optional event object.
     */
    public Object set(int index, Object element) {
        Object old = list.set(index, element);
        fireObjectUpdated(index, element, old);
        return old;
    }

    public void add(int index, Object element) {
        fireObjectInserting(index, element);
        list.add(index, element);
        fireObjectInserted(index, element);
    }

    protected void fireObjectInserted(int index, Object element) {
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.objectInserted(this, index, element);
        }
    }

    protected void fireObjectInserting(int index, Object element) {
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.objectInserting(this, index, element);
        }
    }

    /**
     * Remove the object at index if no listeners veto the action. This will
     * add the object to the removed set and remove it from the dirty set
     * if dirty checking has not been disabled.
     */
    public Object remove(int index) {
        fireObjectDeleting(index, null);
        Object ans = list.remove(index);
        if (ans != null) makeRemoved(ans);
        fireObjectDeleted(index, ans);
        return ans;
    }

    protected void fireObjectDeleted(int index, Object ans) {
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.objectDeleted(this, index, ans);
        }
    }

    protected void fireObjectDeleting(int index, Object o) {
        Iterator it = listenerList.getListeners(/*CHFC*/MdListListener.class/*RIGHTPAR*/);
        while (it.hasNext()) {
            MdListListener listener = (MdListListener)it.next();
            listener.objectDeleting(this, index, o);
        }
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Sort the underlying list using the natural ordering of the objects.
     */
    public void sort() {
        if (list instanceof MdObservableList) {
            ((MdObservableList)list).sort();
        } else {
            Collections.sort(list);
            fireListUpdated();
        }
    }

    /**
     * Sort the underlying list using the comparator.
     */
    public void sort(Comparator c) {
        if (list instanceof MdObservableList) {
            ((MdObservableList)list).sort(c);
        } else {
            Collections.sort(list, c);
            fireListUpdated();
        }
    }

}
