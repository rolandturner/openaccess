
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
package com.versant.core.jdo.tools.workbench.forms;

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.DecoratedGrid;

import java.util.List;
import java.util.ArrayList;

/** 
 * Displays two lists of Object's. The user can move objects from the
 * available side to the selected side etc. The class of the objects
 * must be supplied. It is used as the name for the models.
 * @keep-all
 */
public class ChooseObjectsForm extends WorkbenchPanel {

    private List origAvail, origSel;

    private ExpTableModel available = new ExpTableModel("available");
    private ExpTableModel selected = new ExpTableModel("selected");

    private DecoratedGrid gridAvailable = new DecoratedGrid("gridAvailable");
    private DecoratedGrid gridSelected = new DecoratedGrid("gridSelected");

    /**
     * The lists of Object's are copied and not modified. Objects in both lists
     * are removed from the available list. Both lists are sorted if cls is
     * Comparable.
     */
    public ChooseObjectsForm(Class cls, List avail, List sel) throws Exception {
        origAvail = avail;
        origSel = sel;

        String n = cls.getName();
        int i = n.lastIndexOf('.');
        if (i >= 0) n = n.substring(i + 1);
        available.setName(n + "_available");
        selected.setName(n + "_selected");
        available.setConfig(getConfig());
        selected.setConfig(getConfig());

        available.getList().getList().addAll(avail);
        available.getList().getList().removeAll(sel);
        selected.getList().getList().addAll(sel);

        if (Comparable.class.isAssignableFrom(cls)) {
            available.sort();
            selected.sort();
        }

        gridAvailable.setModel(available);
        gridAvailable.setMultiSelect(true);
        gridAvailable.setSelectionFollowCursor(false);
        gridAvailable.setPreferredRows(20);

        gridSelected.setModel(selected);
        gridSelected.setMultiSelect(true);
        gridSelected.setSelectionFollowCursor(false);
        gridSelected.setPreferredRows(20);

        add(gridAvailable);
        add(gridSelected);
    }

    public void setTitle(String string) {
        super.setTitle(string);
        setBorder(null);
    }

    public boolean isAddObjectsEnabled() {
        return gridAvailable.getSelectedObject() != null;
    }

    public void addObjects() throws Exception {
        List a = gridAvailable.getSelectedObjects();
        if (a == null || a.isEmpty()) return;
        selected.getList().addAll(a);
        available.getList().removeAll(a);
    }

    public boolean isRemoveObjectsEnabled() {
        return gridSelected.getSelectedObject() != null;
    }

    public void removeObjects() throws Exception {
        List a = gridSelected.getSelectedObjects();
        if (a == null || a.isEmpty()) return;
        selected.getList().removeAll(a);
        available.getList().addAll(a);
    }

    public boolean isAddAllObjectsEnabled() {
        return !available.getList().isEmpty();
    }

    public void addAllObjects() throws Exception {
        if (available.getList().isEmpty()) return;
        selected.getList().addAll(available.getList());
        available.clear();
    }

    public boolean isRemoveAllObjectsEnabled() {
        return !selected.getList().isEmpty();
    }

    public void removeAllObjects() throws Exception {
        if (selected.getList().isEmpty()) return;
        available.getList().addAll(selected.getList());
        selected.clear();
    }

    /**
     * Get all the objects in the selected list.
     */
    public List getSelectedObjects() {
        return selected.getList().getList();
    }

    /**
     * Get all the objects added.
     */
    public List getAddedObjects() {
        ArrayList a = new ArrayList(selected.getList().getList());
        a.removeAll(origSel);
        return a;
    }

    /**
     * Get all the objects removed.
     */
    public List getRemovedObjects() {
        ArrayList a = new ArrayList(origSel);
        a.removeAll(selected.getList().getList());
        return a;
    }

}

