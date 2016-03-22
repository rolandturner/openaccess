
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
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;

/**
 * Select the fetch group for a field.
 * @keep-all
 */
public class FieldFetchGroupForm extends WorkbenchPanel {

    private JRadioButton radNone;
    private JRadioButton radDefault;
    private JRadioButton radCustom;

    private ExpTableModel modelField = new ExpTableModel("modelField");
    private ExpTableModel notificationModel;
    private MdField field;

    private boolean inActionPerformed;
    private boolean customFetchGroup;
    private boolean ignoreTableEvents;

    public FieldFetchGroupForm() throws Exception {
        this(false);
    }

    public FieldFetchGroupForm(boolean vertical) throws Exception {
        this(vertical ? "fieldFetchGroupFormVertical" : "fieldFetchGroupForm");
    }

    public FieldFetchGroupForm(String name) throws Exception {
        super(name);
        modelField.setConfig(getConfig());
        super.setModel(modelField);
        radNone = createRad("radNone", "None",
                "Lazy load field on its own when first touched");
        radDefault = createRad("radDefault", "Default",
                "Load field with other default fetch group fields " +
                "when instance is populated");
        radCustom = createRad("radCustom", "Custom",
                "Load field and others in custom fetch group when touched");
        ButtonGroup grpFetchGroup = new ButtonGroup();
        grpFetchGroup.add(radNone);
        grpFetchGroup.add(radDefault);
        grpFetchGroup.add(radCustom);
        add(radNone);
        add(radDefault);
        add(radCustom);
        setTitle("Fetch group to load when field is touched");
    }

    private JRadioButton createRad(String name, String text, String tip) {
        JRadioButton b = new JRadioButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        b.addActionListener(this);
        return b;
    }

    public void setModel(TableModel model) {
        notificationModel = (ExpTableModel)model;
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup(MdField newField) {

        if (field != newField) {
            customFetchGroup = false;
            field = newField;
            try {
                ignoreTableEvents = true;
                modelField.clear();
                modelField.add(field);
            } finally {
                ignoreTableEvents = false;
            }
        }

        boolean dfgDef = field.isDFGFieldByDefault();

        if (!customFetchGroup && field.getFetchGroup().getText() == null) {
            String s = field.getDefaultFetchGroupStr();
            if (s == null) {
                if (dfgDef) radDefault.setSelected(true);
                else radNone.setSelected(true);
            } else if (s.equals("true")) {
                radDefault.setSelected(true);
            } else {
                radNone.setSelected(true);
            }
        } else {
            radCustom.setSelected(true);
        }
    }

    /**
     * The form editor has been invoked or one of our controls has done
     * something.
     */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == this) {
            super.actionPerformed(e);
            return;
        }
        if (inActionPerformed) return;
        try {
            inActionPerformed = true;
            boolean dfgDef = field.isDFGFieldByDefault();
            if (o == radNone) {
                field.setDefaultFetchGroupStr(dfgDef ? "false" : null);
                field.setFetchGroupStr(null);
                customFetchGroup = false;
            } else if (o == radDefault) {
                field.setDefaultFetchGroupStr(dfgDef ? null : "true");
                field.setFetchGroupStr(null);
                customFetchGroup = false;
            } else if (o == radCustom) {
                field.setDefaultFetchGroupStr(null);
                customFetchGroup = true;
            } else {
                return;
            }
            fireFieldUpdated();
        } catch (Exception x) {
            GuiUtils.dispatchException(this, x);
        } finally {
            inActionPerformed = false;
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (!ignoreTableEvents) fireFieldUpdated();
    }

    private void fireFieldUpdated() {
        notificationModel.fireCursorObjectUpdated();
    }

}

