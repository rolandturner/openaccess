
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
import za.co.hemtech.gui.FormPanel;
import za.co.hemtech.gui.MethodAction;
import za.co.hemtech.gui.model.ListListener;
import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.metadata.MDStatics;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * View/edit properties for an Object or interface field.
 *
 * @keep-all
 */
public class VDSObjectFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radPolyref;
    private JRadioButton radSerialize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private FormPanel formPolyRef = new FormPanel("formPolyRef");

    private FormPanel formClasses = new FormPanel("formClasses");
    private ExpTableModel modelClasses = new ExpTableModel("modelClasses");

    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();

    private JCheckBox chkDependent = new JCheckBox();
    private JCheckBox chkNoNulls = new JCheckBox();

    private MethodAction addRefColumn = createA("addRefColumn",
            "Add Reference Column",
            "Add column holding part of primary key of referenced class",
            "Add16.gif");
    private MethodAction removeRefColumn = createA("removeRefColumn",
            "Remove Reference Column",
            "Remove the last column holding the primary key of referenced class",
            "delete.gif");

    private JPopupMenu rightClickMenu = new JPopupMenu();

    public VDSObjectFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formPolyRef.setBusinessLogic(this);
        formClasses.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formPolyRef.setConfig(getConfig());
        formClasses.setConfig(getConfig());
        modelClasses.setConfig(getConfig());

        radPolyref = createRad("radPolyref", "Polymorphic reference",
                "#fp_polyref");
        radSerialize = createRad("radSerialize", "Serialize to BLOB",
                "#fp_serialize");
        radNotPersistent = createRad("radNotPersistent", "Not persistent",
                "Do not store or manage field");
        radTransactional = createRad("radTransactional", "Transactional",
                "Manage field accross transaction boundaries but do not store");
        radSerialize.setEnabled(false);
        ButtonGroup grpMap = new ButtonGroup();
        grpMap.add(radPolyref);
        grpMap.add(radSerialize);
        grpMap.add(radNotPersistent);
        grpMap.add(radTransactional);
        formMapping.add(radPolyref);
        //formMapping.add(radSerialize);
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.setTitle("Mapping");

        formClasses.setModel(modelClasses);
        formClasses.setTitle(null);
        modelClasses.getList().addListListener(new ListListener() {
            public void objectUpdated(List list, int index, Object o,
                    Object event) {
                fireFieldUpdated();
            }

            public void objectInserting(List list, int index, Object o)
                    throws IllegalArgumentException {
            }

            public void objectInserted(List list, int index, Object o) {
            }

            public void objectDeleting(List list, int index, Object o)
                    throws IllegalArgumentException {
            }

            public void objectDeleted(List list, int index, Object o) {
            }

            public void listUpdated(List list) {
            }
        });

        chkDependent.setName("chkDependent");
        chkDependent.setText("Delete referenced object when referencing " +
                "object is deleted");
        chkDependent.addActionListener(this);

        chkNoNulls.setName("chkNoNulls");
        chkNoNulls.setText("Throw an exception on commit if field is null");
        chkNoNulls.addActionListener(this);

        Utils.populateMenu(rightClickMenu, new Object[]{
            addRefColumn, "A",
            removeRefColumn, "D",
        });

        formPolyRef.add(formFetchGroup);
        formPolyRef.add(chkDependent);
        formPolyRef.add(chkNoNulls);
        formPolyRef.add(formClasses);

        add(formMapping);
    }

    private MethodAction createA(String method, String text, String tooltip,
            String icon) {
        MethodAction a = new MethodAction(method, this);
        a.setText(text);
        a.setToolTipText(tooltip);
        if (icon != null) a.setIcon(icon);
        a.setExceptionListener(this);
        return a;
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
                radPolyref.setSelected(true);
                setActive(formPolyRef);
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
        chkDependent.setText("Delete referenced object when " +
                "owning " + field.getMdClass().getName() + " is deleted");

        // null-value
        String nv = field.getNullValueStr();
        chkNoNulls.setSelected(nv != null && nv.equals("exception"));

        // enable actions
        boolean b = modelClasses.getRowCount() == 0;
        addRefColumn.setEnabled(b);
        removeRefColumn.setEnabled(b);
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        MdField field = getMdField();
        if (o == chkDependent) {
            field.setDependentStr(chkDependent.isSelected() ? "true" : null);
        } else if (o == chkNoNulls) {
            field.setNullValueStr(chkNoNulls.isSelected() ? "exception" : null);
        } else if (o == radPolyref) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
        } else if (o == radNotPersistent) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_NONE);
        } else if (o == radTransactional) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Select classes to add/remove from our class-id-mapping's.
     */
    public void chooseClasses() throws Exception {
//        if (openValidClassesDialog(ref)) fireFieldUpdated();
    }

    /**
     * Select classes to add/remove from our class-id-mapping's. Returns true
     * if any changes were made.
     */
    private boolean openValidClassesDialog(MdJdbcRef ref) throws Exception {
        HashMap map = new HashMap();
        ArrayList a = new ArrayList();
        for (Iterator i = ref.getValidClasses().iterator(); i.hasNext();) {
            MdJdbcRef.ValidClassWrapper m = (MdJdbcRef.ValidClassWrapper)i.next();
            MdClass c = m.getMdClass();
            if (c != null) {
                a.add(c);
                map.put(c, m);
            }
        }
        ChooseObjectsForm f = new ChooseObjectsForm(MdClass.class,
                getProject().getAllClasses(), a);
        f.setTitle("Choose Classes Allowed");
        if (!Utils.openDialog(null, f, true, true, true)) return false;
        for (Iterator i = f.getRemovedObjects().iterator(); i.hasNext();) {
            MdClass c = (MdClass)i.next();
            MdJdbcRef.ValidClassWrapper m = (MdJdbcRef.ValidClassWrapper)map.get(
                    c);
            m.clear();
        }
        for (Iterator i = f.getAddedObjects().iterator(); i.hasNext();) {
            ref.addValidClass((MdClass)i.next());
        }
        return true;
    }

    public String getTabTitle() {
        return "Polyref";
    }

}

