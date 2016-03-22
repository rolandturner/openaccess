
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

import za.co.hemtech.config.Config;
import za.co.hemtech.gui.FormPanel;
import za.co.hemtech.gui.form.FormLayout;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.jdo.externalizer.SerializedExternalizer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * View/edit properties for a collection or array[] field.
 */
public class VDSCollectionFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPersistent;
    private JRadioButton radExternalize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private static final int PERSISTENT = 1;
    private static final int NOT_PERSISTENT = 2;
    private static final int TRANSACTIONAL = 3;

    private MdField field;

    private FormPanel formElementType = new FormPanel("formElementType");
    private FormPanel formScoFactory = new FormPanel("formScoFactory");

    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();
    private JCheckBox chkDependent = new JCheckBox();
    private JCheckBox chkEmbedded = new JCheckBox();
    private FormPanel formPersistent = new FormPanel("formPersistent");

    public VDSCollectionFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formPersistent.setBusinessLogic(this);
        formElementType.setBusinessLogic(this);
        formScoFactory.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formPersistent.setConfig(getConfig());
        formElementType.setConfig(getConfig());
        formScoFactory.setConfig(getConfig());

        radPersistent = createRad("radPersistent", "Persistent",
                "Store field");
        radExternalize = createRad("radExternalize", "Externalized",
                "Convert to/from byte[] or other type for storage (e.g. using serialization)");
        radNotPersistent = createRad("radNotPersistent", "Not persistent",
                "Do not store field");
        radTransactional = createRad("radTransactional", "Transactional",
                "Manage field accross transaction boundaries but do not store");
        ButtonGroup grpMap = new ButtonGroup();
        grpMap.add(radPersistent);
        grpMap.add(radNotPersistent);
        grpMap.add(radExternalize);
        grpMap.add(radTransactional);
        formMapping.add(radPersistent);
        formMapping.add(radExternalize);
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.setTitle("Mapping");

        chkDependent.setName("chkDependent");
        chkDependent.setText("Delete objects in collection/array when " +
                "owning object is deleted");
        chkDependent.addActionListener(this);

        chkEmbedded.setName("chkEmbedded");
        chkEmbedded.setText("Embedded");
        chkEmbedded.addActionListener(this);

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
        formPersistent.setModel(getModel());
        formMapping.setModel(getModel());
        formFetchGroup.setModel(getModel());
        formElementType.setModel(getModel());
        formScoFactory.setModel(getModel());
    }

    /**
     * Decide how our field is mapped by looking at the meta data. In the case
     * of a many-to-many this will also set fieldMM and fieldMMinv before
     * returning. If not many-to-many then fieldMM and fieldMMinv are set
     * to null.
     */
    private int analyzeFieldMapping() {
        switch (field.getPersistenceModifierInt()) {
            case MDStatics.PERSISTENCE_MODIFIER_PERSISTENT:
                return PERSISTENT;
            case MDStatics.PERSISTENCE_MODIFIER_NONE:
                return NOT_PERSISTENT;
            case MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL:
                return TRANSACTIONAL;
        }
        return PERSISTENT;
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup() {

        MdField newField = getMdField();
        if (field != newField) {
            field = newField;
        }

        // persistence and mapping
        int mapping = analyzeFieldMapping();
        switch (mapping) {
            case PERSISTENT:
                radPersistent.setSelected(true);
                updateSetupPersistent();
                setActive(formPersistent);
                break;
            case NOT_PERSISTENT:
                radNotPersistent.setSelected(true);
                setActive(null);
                break;
            case TRANSACTIONAL:
                radTransactional.setSelected(true);
                setActive(null);
                break;
        }
    }

    private void updateSetupPersistent() {

        // grab shared components
        moveComponents(formPersistent, new Component[]{
            formFetchGroup, chkDependent, chkEmbedded,
            formElementType, formScoFactory});

        hideComponentsIfArray();

        formFetchGroup.updateSetup(field);
        updateChkDependent();
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
    }

    private void moveComponents(FormPanel dest, Component[] a) {
        boolean changed = false;
        for (int i = 0; i < a.length; i++) {
            Component c = a[i];
            if (c.getParent() != dest) {
                dest.add(c);
                changed = true;
            }
            if (!c.isVisible()) {
                c.setVisible(true);
                changed = true;
            }
        }
        if (changed) {
            Config cfg = config.getSubConfig(dest.getName());
            // load the layout
            FormLayout l = (FormLayout)dest.getLayout();
            l.load(cfg);
            dest.revalidate(); // reload layout etc.

        }
    }

    private void hideComponentsIfArray() {
        if (field.getCategory() == MDStatics.CATEGORY_ARRAY) {
            formElementType.setVisible(false);
            formScoFactory.setVisible(false);
        }
    }

    private void updateChkDependent() {
        chkDependent.setText("Delete objects in " +
                (field.getCategory() == MDStatics.CATEGORY_ARRAY
                ? "array"
                : "collection") +
                " when owning " + field.getMdClass().getName() + " is deleted");
        boolean canChkDependent = field.getElementTypeMdClass() != null;
        MdProject project = getProject();
        if (!canChkDependent && project.isVds()) {
            Class clazz = project.loadClass(field.getElementQType());
            canChkDependent = clazz != null && clazz.isInterface();
        }
        chkDependent.setEnabled(canChkDependent);
        chkDependent.setSelected(field.getDependentBool() || !canChkDependent);
    }

    public boolean hasValidElementType() {
        return field.getColElementTypeMdClass() != null;
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        if (o == chkDependent) {
            field.setDependentStr(chkDependent.isSelected() ? "true" : null);
        } else if (o == chkEmbedded) {
            field.setEmbedded(chkEmbedded.isSelected());
        } else if (o == radPersistent) {
            changeToPersistent();
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

    /**
     * Break the one-to-many or many-to-many inverse link.
     */
    private void changeToPersistent() {
        field.setInverseStr(null);
        field.setPersistenceModifierInt(MDStatics.NOT_SET);
    }

    public String getTabTitle() {
        return "Collection";
    }
}
