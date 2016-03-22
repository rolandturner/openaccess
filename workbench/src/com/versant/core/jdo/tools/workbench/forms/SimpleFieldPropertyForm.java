
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

import za.co.hemtech.gui.FormPanel;
import com.versant.core.jdo.tools.workbench.diagram.classdiagram.*;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.metadata.MDStatics;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * View/edit properties for a simple field.
 */
public class SimpleFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPersistent;
    private JRadioButton radSerialize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private FormPanel formSimple = new FormPanel("formSimple");

    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();

    private JCheckBox chkNoNulls = new JCheckBox();
    private JCheckBox chkPrimaryKey = new JCheckBox();
    private JCheckBox chkAutoCreated = new JCheckBox();
    private JCheckBox chkAutoModified = new JCheckBox();
//    private JCheckBox chkIndex = new JCheckBox();

    private MdClassTable table = new MdClassTable();

    private FieldERGraphForm formGraph = new FieldERGraphForm();

    public SimpleFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formSimple.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formSimple.setConfig(getConfig());

        radPersistent = createRad("radPersistent", "Persistent",
                "Store field in a column based on its type");
        radSerialize = createRad("radSerialize", "Serialize to BLOB",
                "#fp_serialize");
        radNotPersistent = createRad("radNotPersistent", "Not persistent",
                "Do not store or manage field");
        radTransactional = createRad("radTransactional", "Transactional",
                "Manage field accross transaction boundaries but do not store");
        radSerialize.setEnabled(false);
        ButtonGroup grpMap = new ButtonGroup();
        grpMap.add(radPersistent);
        grpMap.add(radSerialize);
        grpMap.add(radNotPersistent);
        grpMap.add(radTransactional);
        formMapping.add(radPersistent);
        //formMapping.add(radSerialize);
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.setTitle("Mapping");

        chkNoNulls.setName("chkNoNulls");
        chkNoNulls.setText("Throw an exception on commit if field is null");
        chkNoNulls.addActionListener(this);

        chkPrimaryKey.setName("chkPrimaryKey");
        chkPrimaryKey.setText("This field is part of the primary key (application identity)");
        chkPrimaryKey.addActionListener(this);

        chkAutoCreated.setName("chkAutoCreated");
        chkAutoCreated.setText("Set this field when new instance is created");
        chkAutoCreated.addActionListener(this);

        chkAutoModified.setName("chkAutoModified");
        chkAutoModified.setText("Set this field when instance is modified");
        chkAutoModified.addActionListener(this);

//        chkIndex.setName("chkIndex");
//        chkIndex.setText("Create an index on this field");
//        chkIndex.setToolTipText("Index this field - Use class properties to " +
//            "create indexes with more than one field");
//        chkIndex.addActionListener(this);

        formGraph.getDiagram().addTable(table);

        formSimple.add(formFetchGroup);
        formSimple.add(chkNoNulls);
        formSimple.add(chkPrimaryKey);
        formSimple.add(chkAutoCreated);
        formSimple.add(chkAutoModified);
        formSimple.add(formGraph);

        add(formMapping);
    }

    private JRadioButton createRad(String name, String text, String tip) {
        JRadioButton b = new JRadioButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        b.addActionListener(this);
        return b;
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        formMapping.setModel(getModel());
        formFetchGroup.setModel(getModel());
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup() {
        MdField field = getMdField();

        // persistence and mapping
        int m = field.getPersistenceModifierInt();
        switch (m) {
            case MDStatics.PERSISTENCE_MODIFIER_PERSISTENT:
                radPersistent.setSelected(true);
                setActive(formSimple);
                break;
            case MDStatics.PERSISTENCE_MODIFIER_NONE:
                radNotPersistent.setSelected(true);
                setActive(null);
                break;
            case MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL:
                radTransactional.setSelected(true);
                setActive(null);
                break;
        }

        // fetch group
        formFetchGroup.updateSetup(field);

        // null-value
        String nv = field.getNullValueStr();
        chkNoNulls.setSelected(nv != null && nv.equals("exception"));

        // primary-key
        chkPrimaryKey.setEnabled(field.isPossiblePrimaryKeyField());
        chkPrimaryKey.setSelected(field.isPrimaryKeyField());

        // auto-set
        boolean va = field.isValidAutoSetField();
        chkAutoCreated.setEnabled(va);
        chkAutoModified.setEnabled(va);
        chkAutoCreated.setSelected(field.isAutoSetCreated());
        chkAutoModified.setSelected(field.isAutoSetModified());
        String msgC;
        String msgM;
        if (va && field.isNumericAutoSetType()) {
            msgC = "Set field to 1";
            msgM = "Increment field";
        } else {
            msgC = msgM = "Set field to current date";
        }
        String cn = field.getMdClass().getName();
        chkAutoCreated.setText(msgC + " when a new instance of " + cn  +
                " is committed");
        chkAutoModified.setText(msgM + " when a modified instance of " + cn  +
                " is committed");

        // table
        table.init(field.getMdClass());
        table.addCol(DotDotDotColumn.INSTANCE);
        table.addCol(field.getCol());
        table.addCol(DotDotDotColumn.INSTANCE);

        // refresh diagram
        if (isReloadingClasses()) {
            formGraph.getGraph().setEnabled(false);
        } else {
            formGraph.getGraph().refresh();
            formGraph.getGraph().setEnabled(true);
        }
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        MdField field = getMdField();
        if (o == chkNoNulls) {
            field.setNullValueStr(chkNoNulls.isSelected() ? "exception" : null);
        } else if (o == chkAutoCreated) {
            field.setAutoSetCreated(chkAutoCreated.isSelected());
        } else if (o == chkAutoModified) {
            field.setAutoSetModified(chkAutoModified.isSelected());
        } else if (o == chkPrimaryKey) {
            field.setPrimaryKeyStr(chkPrimaryKey.isSelected() ? "true" : null);
        } else if (o == radPersistent) {
            field.setPersistenceModifierInt(MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
        } else if (o == radNotPersistent) {
            field.setPersistenceModifierInt(MDStatics.PERSISTENCE_MODIFIER_NONE);
        } else if (o == radTransactional) {
            field.setPersistenceModifierInt(MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL);
        } else {
            return false;
        }
        return true;
    }

    public String getTabTitle() {
        return "Simple";
    }

}

