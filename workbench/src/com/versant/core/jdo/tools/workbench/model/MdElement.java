
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

import org.jdom.*;

import java.util.List;

/**
 * <p>This is a JDom Element that keeps track of a virtual parent. If the
 * element becomes empty it will remove itself from its virtual parent.
 * If it later becomes not empty it will add itself back.</p>
 * <p/>
 * <p>These can also be chained together into a linked list. The elements
 * in the list are only considered empty if all of them are empty. So if
 * any of the elements gain content then all of them are added to their
 * virtualParents if their parents are null. Likewise if all of them are
 * empty they will all be removed from their parents.</p>
 *
 * @keep-all
 * @see #link
 */
public class MdElement extends Element {

    private MdElement virtualParent;
    private boolean autoAddRemove = true;
    private boolean emptyIfNoExtValue = true;
    private boolean emptyIfExtValue;
    private MdElement next, prev;
    private int emptyExtValueCount = 2;

    public MdElement() {
    }

    public MdElement(String name) {
        super(name);
    }

    public MdElement(String name, Namespace namespace) {
        super(name, namespace);
    }

    public MdElement(String name, String prefix, String uri) {
        super(name, prefix, uri);
    }

    public MdElement(String name, String uri) {
        super(name, uri);
    }

    protected Content setParent(Element parent) {
        Content ans = super.setParent(parent);
        if (parent != null) virtualParent = (MdElement)parent;
        return ans;
    }

    public Parent addContent(Element element) {
        Parent ans = super.addContent(element);
        addToParent();
        return ans;
    }

    /**
     * Add this element to its virtual parent if required.
     */
    public void addToParent() {
        MdElement e = this;
        for (; e.prev != null; e = e.prev) ;
        for (; e != null; e = e.next) {
            if (e.autoAddRemove && e.parent == null && e.virtualParent != null) {
                e.virtualParent.addContent(e);
            }
        }
    }

    public boolean removeContent(Element element) {
        if (super.removeContent(element)) {
            if (autoAddRemove && isEmpty()) {
                MdElement e = this;
                for (; e.prev != null; e = e.prev) ;
                for (; e != null; e = e.next) {
                    if (e.virtualParent != null) {
                        e.virtualParent.removeContent(e);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Element setAttribute(String name, String value) {
        super.setAttribute(name, value);
        // this element might still be empty e.g. setting the name attribute
        // on a field
        if (!isEmpty()) addToParent();
        return this;
    }

    /**
     * Is this element empty? Note that if this element is part of a list
     * of linked elements then all the others must be empty as well or this
     * method will return false.
     */
    public boolean isEmpty() {
        MdElement e = this;
        for (; e.prev != null; e = e.prev) ;
        for (; e != null && e.isEmptyImp(); e = e.next) ;
        return e == null;
    }

    private boolean isEmptyImp() {
        if (name.equals("extension")) {
            if (!getChildren().isEmpty()) return false;
            if (emptyIfExtValue) {
                return true;
            } else if (emptyIfNoExtValue) {
                return getAttributes().size() <= emptyExtValueCount;
            } else {
                return getAttributes().isEmpty();
            }
        } else if (name.equals("field")) {
            if (!getChildren().isEmpty()) return false;
            return getAttributes().size() <= 1;
        } else {
            return getAttributes().isEmpty() && getChildren().isEmpty();
        }
    }

    /**
     * Change the Element's Document dirty flag if it is an MdDocument.
     */
    public void makeDirty() {
        Document d = getDocument();
        if (d != null && d instanceof MdDocument) {
            ((MdDocument)d).setDirty(true);
        }
    }

    public MdElement getVirtualParent() {
        return virtualParent;
    }

    public void setVirtualParent(MdElement virtualParent) {
        this.virtualParent = virtualParent;
    }

    public boolean isAutoAddRemove() {
        return autoAddRemove;
    }

    public void setAutoAddRemove(boolean autoAddRemove) {
        this.autoAddRemove = autoAddRemove;
    }

    public boolean isEmptyIfNoExtValue() {
        return emptyIfNoExtValue;
    }

    /**
     * Consider this element empty if it is an extension and has no value
     * and no children.
     */
    public void setEmptyIfNoExtValue(boolean emptyIfNoExtValue) {
        this.emptyIfNoExtValue = emptyIfNoExtValue;
    }

    public boolean isEmptyIfExtValue() {
        return emptyIfExtValue;
    }

    /**
     * Consider this element empty if it is an extension and has no children
     * (i.e. even if the value is set it is still considered empty).
     */
    public void setEmptyIfExtValue(boolean emptyIfExtValue) {
        this.emptyIfExtValue = emptyIfExtValue;
    }

    /**
     * Link all the elements in the array together so they are only considered
     * empty if all are empty and add/remove themselves from their parents
     * as a block.
     */
    public static void link(MdElement[] a) {
        int n = a.length;
        if (n == 0) return;
        a[0].prev = null;
        if (n == 1) {
            a[0].next = null;
            return;
        }
        a[0].next = a[1];
        for (int i = 1; i < n - 1; i++) {
            a[i].prev = a[i - 1];
            a[i].next = a[i + 1];
        }
        a[n - 1].prev = a[n - 2];
        a[n - 1].next = null;
    }

    /**
     * Link all the elements in the array together so they are only considered
     * empty if all are empty and add/remove themselves from their parents
     * as a block.
     */
    public static void link(List list) {
        int n = list.size();
        if (n == 0) return;
        MdElement[] a = new MdElement[n];
        list.toArray(a);
        link(a);
    }

    public int getEmptyExtValueCount() {
        return emptyExtValueCount;
    }

    public void setEmptyExtValueCount(int emptyExtValueCount) {
        this.emptyExtValueCount = emptyExtValueCount;
    }
}

