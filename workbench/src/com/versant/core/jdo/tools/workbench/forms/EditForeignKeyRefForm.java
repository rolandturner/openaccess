
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
import com.versant.core.jdbc.metadata.JdbcRefField;
import com.versant.core.jdbc.metadata.JdbcMetaDataEnums;
import com.versant.core.metadata.MetaDataEnums;
import com.versant.core.metadata.parser.JdoExtension;
import za.co.hemtech.gui.FormPanel;
import za.co.hemtech.gui.exp.ExpTableModel;

import javax.swing.*;

/**
 * Form to edit a foreign key reference from one table to another. This is
 * designed to be displayed as a model dialog. Set its properties, display
 * it and read back changed properties if ok was clicked.
 */
public class EditForeignKeyRefForm extends WorkbenchPanel {

    private FormPanel formJoin = new FormPanel("formJoin");
    private JRadioButton radJoinNo;
    private JRadioButton radJoinInner;
    private JRadioButton radJoinOuter;

    private FormPanel formConstraint = new FormPanel("formConstraint");
    private JRadioButton radConstraintNo;
    private JRadioButton radConstraintAuto;
    private JRadioButton radConstraintCustom;

    private final JdbcMetaDataEnums jdbcMDE = new JdbcMetaDataEnums();

    private String constraintName;

    private ExpTableModel modelThis = new ExpTableModel("modelThis");

    public EditForeignKeyRefForm(String title) throws Exception {
        setTitle(title);
        formJoin.setConfig(getConfig());
        formConstraint.setConfig(getConfig());
        modelThis.setConfig(getConfig());

        formJoin.setTitle("SQL Join To Use When Pre-fetching Referenced Data");
        ButtonGroup g = new ButtonGroup();
        radJoinNo = createRad("radJoinNo",
            "Do not join",
            "This a good choice if the instance will be in the level 2 cache",
            formJoin, g);
        radJoinOuter = createRad("radJoinOuter",
            "Use an OUTER join",
            "Safe option usually offering the best performance",
            formJoin, g);
        radJoinInner = createRad("radJoinInner",
            "Use an INNER join",
            "Do not use INNER unless you are sure of your database referential integrity",
            formJoin, g);

        formConstraint.setTitle("Generate SQL Foreign Key Constraint");
        g = new ButtonGroup();
        radConstraintNo = createRad("radConstraintNo",
            "Do not generate constraint",
            "Do not generate a foreign key constraint",
            formConstraint, g);
        radConstraintAuto = createRad("radConstraintAuto",
            "Generate with default name",
            "Generate with an automatically assigned name",
            formConstraint, g);
        radConstraintCustom = createRad("radConstraintCustom",
            "Generate with custom name",
            "Generate a constraint with user specified name",
            formConstraint, g);

        modelThis.add(this);
        setModel(modelThis);
        formConstraint.setModel(modelThis);

        add(formJoin);
        add(formConstraint);
    }

    private JRadioButton createRad(String name, String text, String tip,
            FormPanel fp, ButtonGroup g) {
        JRadioButton b = new JRadioButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        //b.addActionListener(this);
        g.add(b);
        fp.add(b);
        return b;
    }

    public void setJoin(String s) {
        int i;
        if (s == null) {
            i = JdbcRefField.USE_JOIN_OUTER;
        } else {
            Object o = jdbcMDE.USE_JOIN_ENUM.get(s);
            i = o == null ? -1 : ((Integer)o).intValue();
        }
        switch (i) {
            case JdbcRefField.USE_JOIN_NO:
                radJoinNo.setSelected(true);
                break;
            case JdbcRefField.USE_JOIN_INNER:
                radJoinInner.setSelected(true);
                break;
            case JdbcRefField.USE_JOIN_OUTER:
            default:
                radJoinOuter.setSelected(true);
                break;
        }
    }

    public String getJoin() {
        if (radJoinNo.isSelected()) return "no";
        if (radJoinInner.isSelected()) return "inner";
        return null;
    }

    public void setConstraint(String s) {
        constraintName = null;
        if (s == null) {
            radConstraintAuto.setSelected(true);
        } else if (s.equals(JdoExtension.NO_VALUE)) {
            radConstraintNo.setSelected(true);
        } else {
            radConstraintCustom.setSelected(true);
            constraintName = s;
        }
    }

    public String getConstraint() {
        if (radConstraintAuto.isSelected()) return null;
        if (radConstraintNo.isSelected()) return JdoExtension.NO_VALUE;
        return constraintName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
        radConstraintCustom.setSelected(true);
    }

    /**
     * Set this to true to disable the join selection controls.
     */
    public void setIgnoreJoin(boolean on) {
        radJoinInner.setEnabled(!on);
        radJoinNo.setEnabled(!on);
        radJoinOuter.setEnabled(!on);
    }
}
