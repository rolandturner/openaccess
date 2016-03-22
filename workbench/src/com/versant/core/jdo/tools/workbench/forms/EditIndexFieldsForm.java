
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
import za.co.hemtech.gui.*;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.MdIndex;
import com.versant.core.jdo.tools.workbench.model.MdField;
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.metadata.MDStatics;

import java.util.List;

/**
 * View / edit fields for an index.
 *
 * @keep-all
 */
public class EditIndexFieldsForm extends WorkbenchPanel {

    private ExpTableModel modelIndex = new ExpTableModel("modelIndex");
    private ExpTableModel modelFields = new ExpTableModel("modelFields");
    private ExpTableModel modelAllFields = new ExpTableModel("modelAllFields");

    private FormPanel formFields = new FormPanel("formFields");
    private FormPanel formAllFields = new FormPanel("formAllFields");
    private DecoratedGrid gridAllFields = new DecoratedGrid("gridAllFields");

    public EditIndexFieldsForm() throws Exception {
        modelIndex.setConfig(getConfig());
        modelFields.setConfig(getConfig());
        modelAllFields.setConfig(getConfig());
        formFields.setConfig(getConfig());
        formAllFields.setConfig(getConfig());

        setModel(modelIndex);
        modelFields.setMaster(modelIndex);
        modelFields.setMasterColumnName("fields");

        formFields.setModel(modelFields);
        formAllFields.setModel(modelAllFields);

        gridAllFields.setModel(modelAllFields);
        gridAllFields.addHeader(new ColumnCaptionHeader());
        gridAllFields.setSelectRow(true);
        gridAllFields.setPreferredRows(15);

        formAllFields.add(gridAllFields);

        add(formFields);
        add(formAllFields);
    }

    public boolean isCancelable() {
        return false;
    }

    public String getTitle() {
        return "Index Fields";
    }

    public MdIndex getIndex() {
        if (modelIndex.getRowCount() == 0) return null;
        return (MdIndex)modelIndex.get(0);
    }

    public void setIndex(MdIndex index) throws Exception {
        modelIndex.clear();
        modelIndex.add(index);

        // find all the possible fields including fields from superclasses
        List flist = modelAllFields.getList().getList();
        flist.clear();
        for (MdClass mdc = index.getMdClass(); mdc != null;
             mdc = mdc.getPcSuperclassMdClass()) {
            List cflist = mdc.getFieldList();
            int n = cflist.size();
            for (int i = 0; i < n; i++) {
                MdField f = (MdField)cflist.get(i);
                switch (f.getCategory()) {
                    case MDStatics.CATEGORY_DATASTORE_PK:
                    case MDStatics.CATEGORY_CLASS_ID:
                    case MDStatics.CATEGORY_OPT_LOCKING:
                    case MDStatics.CATEGORY_REF:
                    case MDStatics.CATEGORY_POLYREF:
                    case MDStatics.CATEGORY_SIMPLE:
                    case MDStatics.CATEGORY_EXTERNALIZED:
                        flist.add(f);
                        break;
                }
            }
        }
        modelAllFields.getList().fireListUpdated();
    }

    public boolean addFieldEnabled() throws Exception {
        MdField f = (MdField)modelAllFields.getCursorObject();
        if (f == null) return false;
        MdIndex index = getIndex();
        String name = f.getName();
        if (index.getFields().contains(name)) return false;
        return true;
    }

    public void addField() throws Exception {
        MdField f = (MdField)modelAllFields.getCursorObject();
        if (f == null) return;
        MdIndex index = getIndex();
        String name = f.getName();
        if (index.getFields().contains(name)) return;
        index.addField(name);
        modelFields.listUpdated();
    }

    public boolean removeFieldEnabled() throws Exception {
        return modelFields.getCursorObject() != null;
    }

    public void removeField() throws Exception {
        String name = (String)modelFields.getCursorObject();
        if (name == null) return;
        getIndex().removeField(name);
        modelFields.listUpdated();
    }

    public void moveFieldUp() throws Exception {
        int i = modelFields.getCursorRow();
        if (i <= 0) return;
        getIndex().moveFieldUp((String)modelFields.getCursorObject());
        modelFields.listUpdated();
    }

    public boolean moveFieldUpEnabled() throws Exception {
        return modelFields.getCursorRow() > 0;
    }

    public void moveFieldDown() throws Exception {
        int i = modelFields.getCursorRow();
        if (i >= modelFields.getRowCount() - 1) return;
        getIndex().moveFieldDown((String)modelFields.getCursorObject());
        modelFields.listUpdated();
    }

    public boolean moveFieldDownEnabled() throws Exception {
        return modelFields.getCursorRow() < modelFields.getRowCount() - 1;
    }
}

