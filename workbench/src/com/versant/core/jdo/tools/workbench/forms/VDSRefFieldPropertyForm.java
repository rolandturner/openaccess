
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
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.jdo.externalizer.SerializedExternalizer;
import com.versant.core.metadata.MDStatics;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * View/edit properties for a reference field.
 */
public class VDSRefFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPersistent;
    private JRadioButton radExternalize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private FormPanel formRef = new FormPanel("formRef");

    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();

    private JCheckBox chkDependent = new JCheckBox();
    private JCheckBox chkNoNulls = new JCheckBox();
    private JCheckBox chkEmbedded = new JCheckBox();

    public VDSRefFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formRef.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formRef.setConfig(getConfig());

        radPersistent = createRad("radPersistent", "Persistent",
                "Store field in database");
        radExternalize = createRad("radExternalize", "Externalized",
                "Convert to/from byte[] or other type for storage (e.g. using serialization)");
        radNotPersistent = createRad("radNotPersistent", "Not persistent",
                "Do not store or manage field");
        radTransactional = createRad("radTransactional", "Transactional",
                "Manage field accross transaction boundaries but do not store");
        ButtonGroup grpMap = new ButtonGroup();
        grpMap.add(radPersistent);
        grpMap.add(radExternalize);
        grpMap.add(radNotPersistent);
        grpMap.add(radTransactional);
        formMapping.add(radPersistent);
        formMapping.add(radExternalize);
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.setTitle("Mapping");

        chkDependent.setName("chkDependent");
        chkDependent.setText("Delete referenced object when referencing " +
                "object is deleted");
        chkDependent.addActionListener(this);

        chkNoNulls.setName("chkNoNulls");
        chkNoNulls.setText("Throw an exception on commit if field is null");
        chkNoNulls.addActionListener(this);

        chkEmbedded.setName("chkEmbedded");
        chkEmbedded.setText("Embedded");
        chkEmbedded.addActionListener(this);

        formRef.add(formFetchGroup);
        formRef.add(chkDependent);
        formRef.add(chkNoNulls);
//        formRef.add(chkEmbedded);

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
        MdClass srcClass = field.getMdClass();
        MdClass refClass = field.getRefClass();

        // persistence and mapping
        int m = field.getPersistenceModifierInt();
        switch (m) {
            case MDStatics.PERSISTENCE_MODIFIER_PERSISTENT:
                radPersistent.setSelected(true);
                setActive(formRef);
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

        // dependent
        chkDependent.setSelected(field.getDependentBool());
        chkDependent.setText("Cascade delete: Delete referenced " +
                (refClass == null ? "class" : refClass.getName()) +
                " when owning " + srcClass.getName() + " is deleted");

        // null-value
        String nv = field.getNullValueStr();
        chkNoNulls.setSelected(nv != null && nv.equals("exception"));
        if (field.isAlwaysEmbedded()) {
            chkEmbedded.setEnabled(false);
            chkEmbedded.setSelected(true);
            chkEmbedded.setText(
                    "Embedded (Fields of this type is always embedded)");
        } else {
            chkEmbedded.setEnabled(true);
            chkEmbedded.setSelected(field.isEmbedded());
            chkEmbedded.setText("Embedded");
        }
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        MdField field = getMdField();
        if (o == chkDependent) {
            field.setDependentStr(chkDependent.isSelected() ? "true" : null);
        } else if (o == chkNoNulls) {
            field.setNullValueStr(chkNoNulls.isSelected() ? "exception" : null);
        } else if (o == chkEmbedded) {
            field.setEmbedded(chkEmbedded.isSelected());
        } else if (o == radPersistent) {
            field.setPersistenceModifierInt(MDStatics.NOT_SET);
        } else if (o == radNotPersistent) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_NONE);
        } else if (o == radTransactional) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL);
        } else if (o == radExternalize) {
            field.setExternalizerStr(SerializedExternalizer.SHORT_NAME);
        } else {
            return false;
        }
        return true;
    }

    public String getTabTitle() {
        return "Reference";
    }

}

