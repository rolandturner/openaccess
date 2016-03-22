
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
import com.versant.core.metadata.MDStatics;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * View/edit properties for an externalized field.
 */
public class VDSExternalizedFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPersistent;
    private JRadioButton radExternalize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private FormPanel formExternalized = new FormPanel("formExternalized");
    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();
    private JCheckBox chkNoNulls = new JCheckBox();

    public VDSExternalizedFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formExternalized.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formExternalized.setConfig(getConfig());

        radPersistent = createRad("radPersistent", "Persistent",
                "Store field in a column based on its type");
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
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.add(radExternalize);
        formMapping.setTitle("Mapping");

        chkNoNulls.setName("chkNoNulls");
        chkNoNulls.setText("Throw an exception on commit if field is null");
        chkNoNulls.addActionListener(this);

        formExternalized.add(formFetchGroup);
        formExternalized.add(chkNoNulls);

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
        formExternalized.setModel(getModel());
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
                radExternalize.setSelected(true);
                radPersistent.setEnabled(
                        !field.isPersistableOnlyUsingExternalization());
                setActive(formExternalized);
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
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        MdField field = getMdField();
        if (o == chkNoNulls) {
            field.setNullValueStr(chkNoNulls.isSelected() ? "exception" : null);
        } else if (o == radPersistent || o == radExternalize) {
            field.setPersistenceModifierInt(MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
            field.setExternalizerStr(null);
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
        return "Externalized";
    }

}

