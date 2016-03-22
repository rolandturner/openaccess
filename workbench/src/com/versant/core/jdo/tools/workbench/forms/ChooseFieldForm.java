
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

import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.DecoratedGrid;
import za.co.hemtech.gui.ColumnCaptionHeader;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;

import java.util.*;
import java.lang.reflect.Field;

/**
 * Select one or more fields.
 * @keep-all
 */
public class ChooseFieldForm extends WorkbenchPanel implements Comparator {

    private ExpTableModel modelFields = new ExpTableModel("modelFields");
    private DecoratedGrid gridFields = new DecoratedGrid("gridFields");

    public ChooseFieldForm(List fields) throws Exception {
        modelFields.setConfig(getConfig());
        setModel(modelFields);
        gridFields.setModel(modelFields);
        gridFields.addHeader(new ColumnCaptionHeader());
        gridFields.setSelectRow(true);
        gridFields.setSelectionFollowCursor(false);
        gridFields.setMultiSelect(true);
        gridFields.setReadOnly(true);
        int pr = fields.size();
        if (pr < 10) pr = 10;
        else if (pr > 25) pr = 25;
        gridFields.setPreferredRows(pr);
        add(gridFields);
        //Collections.sort(fields, this);
        modelFields.getList().setList(fields);
    }

    public int compare(Object o1, Object o2) {
        Field a = (Field)o1;
        Field b = (Field)o2;
        return a.getName().compareTo(b.getName());
    }

    public String getTitle() {
        return "Select Field(s)";
    }

    public List getSelectedFields() {
        return gridFields.getSelectedObjects();
    }

}

