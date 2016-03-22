
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
package tut2.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model that provides a <code>java.awt.List</code> with its contents using a <code>java.util.List</code>.
 * <p>
 */
public class ListModel extends AbstractListModel {

    List data = new ArrayList();

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
        fireContentsChanged(this, 0, data.size());
    }

    /**
     * Returns the length of the list.
     * @return the length of the list
     */
    public int getSize() {
        return data.size();
    }

    /**
     * Returns the value at the specified index.
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index) {
        return data.get(index);
    }

    /**
     * Add an object to the <code>java.util.List</code> and therefor to the <code>java.awt.List</code>
     * @param o the object to add
     */
    public void add(Object o) {
        if (data.add(o)) {
            int size = data.size();
            fireIntervalAdded(this, size, size);
        }
    }

    /**
     * Remove an object from the <code>java.util.List</code> and therefor from the <code>java.awt.List</code>
     * @param o the object to remove
     */
    public void remove(Object o) {
        int index = data.indexOf(o);
        if (data.remove(o)) {
            fireIntervalRemoved(this, index, index);
        }
    }
}

