
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
import com.versant.core.jdo.tools.workbench.model.MdField;
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.model.CursorModelListener;
import za.co.hemtech.gui.model.CursorModel;
import za.co.hemtech.gui.util.VetoException;

import javax.swing.*;
import java.util.List;
import java.awt.event.InputEvent;

/**
 * @keep-all
 */
public class CollectionErrorFieldForm extends WorkbenchPanel {

    private ExpTableModel modelFields = new ExpTableModel("modelFields");

    private JSplitPane splitter;
    private FieldPropertyForm formFieldProps;

    public CollectionErrorFieldForm(List fields) throws Exception {

        modelFields.setConfig(getConfig());
        modelFields.getList().setList(fields);
        setTitle("Collection and Map Errors");

        setModel(modelFields);
        formFieldProps = new FieldPropertyForm();
        formFieldProps.setTitle(null);
        if (splitter != null) splitter.setRightComponent(formFieldProps);
        modelFields.addCursorModelListener(new CursorModelListener() {
            public void cursorMoving(CursorModel cursorModel, int i, int i1,
                    InputEvent event) throws VetoException {}

            public void cursorMoveBlocked(CursorModel cursorModel, int i,
                    int i1, InputEvent event) {}

            public void cursorMoved(CursorModel cursorModel, int i, int i1,
                    InputEvent event) {
                try {
                    formFieldProps.setMdField(getSelectedField());
                } catch (Exception e) {
                    za.co.hemtech.gui.util.GuiUtils.dispatchException(
                            CollectionErrorFieldForm.this, e);
                }
            }
        });
        formFieldProps.setMdField(getSelectedField());
    }

    public void setSplitter(JSplitPane splitter) {
        this.splitter = splitter;
    }

    public MdField getSelectedField() {
        MdField mdField = (MdField)modelFields.getCursorObject();
        if (mdField == null && modelFields.getList().size() > 0) {
            mdField = (MdField)modelFields.getList().get(0);
        }
        return mdField;
    }
}
