
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
 * View/edit properties for a map field.
 */
public class VDSMapFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPersistent;
    private JRadioButton radExternalize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private FormPanel formMap = new FormPanel("formMap");

    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();

    private JCheckBox chkKeysDependent = new JCheckBox();
    private JCheckBox chkValueDependent = new JCheckBox();
    private JCheckBox chkEmbedded = new JCheckBox();

    public VDSMapFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formMap.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formMap.setConfig(getConfig());

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

        chkKeysDependent.setName("chkKeysDependent");
        chkKeysDependent.setText("Delete referenced key object when referencing " +
                "object is deleted");
        chkKeysDependent.addActionListener(this);

        chkValueDependent.setName("chkValueDependent");
        chkValueDependent.setText("Delete referenced value object when referencing " +
                "object is deleted");
        chkValueDependent.addActionListener(this);

        chkEmbedded.setName("chkEmbedded");
        chkEmbedded.setText("Embedded");
        chkEmbedded.addActionListener(this);

        formMap.add(formFetchGroup);
        formMap.add(chkKeysDependent);
        formMap.add(chkValueDependent);
        formMap.add(chkEmbedded);
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
        formMap.setModel(getModel());
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
                setActive(formMap);
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
        chkValueDependent.setSelected(field.getDependentBool());
        if (field.isAlwaysEmbedded()) {
            chkEmbedded.setEnabled(false);
            chkEmbedded.setSelected(true);
            chkEmbedded.setText(
                    "Embedded (Fields of this type is always embedded)");
            revalidate();
        } else {
            chkEmbedded.setEnabled(true);
            chkEmbedded.setSelected(field.isEmbedded());
            chkEmbedded.setText("Embedded");
            revalidate();
        }
        chkKeysDependent.setSelected(field.getKeysDependentBool());
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        MdField field = getMdField();
        if (o == chkValueDependent) {
            field.setDependentStr(
                    chkValueDependent.isSelected() ? "true" : null);
        } else if (o == chkKeysDependent) {
            field.setKeysDependentStr(
                    chkKeysDependent.isSelected() ? "true" : null);
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
        return "Map";
    }

}

