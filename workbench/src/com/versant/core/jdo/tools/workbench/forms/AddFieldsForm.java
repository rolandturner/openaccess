
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
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdField;
import com.versant.core.jdo.tools.workbench.model.MdElement;
import com.versant.core.jdo.externalizer.SerializedExternalizer;
import com.versant.core.metadata.MDStatics;
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.DecoratedGrid;
import za.co.hemtech.gui.framework.BusinessLogicContainer;
import za.co.hemtech.gui.model.ObservableList;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.Serializable;

/**
 * Allows user to select fields to add to a class. The user can enter the
 * name of a field or choose one or more found by reflection.
 *
 * @keep-all
 */
public class AddFieldsForm extends WorkbenchPanel {

    private ExpTableModel modelThis = new ExpTableModel("modelThis");

    private MdClass mdClass;
    private List fields = new ObservableList();

    private DecoratedGrid fieldsGrid;

    public AddFieldsForm(MdClass mdClass) throws Exception {
        this.mdClass = mdClass;
        modelThis.setConfig(getConfig());
        setModel(modelThis);
        modelThis.add(this);

        // find all fields not already in class
        Class cls = mdClass.getCls();
        if (cls != null) {
            MdProject project = mdClass.getMdProject();
            Field[] all = cls.getDeclaredFields();
            int n = all.length;
            for (int i = 0; i < n; i++) {
                Field f = all[i];
                if (mdClass.findField(f.getName()) == null
                        && project.isPersistableField(
                                mdClass.getMdDataStore().getName(), f)) {
                    fields.add(new FieldWrapper(f));
                }
            }
        }
    }

    public String getTitle() {
        return "Add fields to " + mdClass.getName();
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public void setFieldsGrid(JComponent fieldsGrid) {
        this.fieldsGrid = (DecoratedGrid)fieldsGrid;
    }

    public List getFields() {
        return fields;
    }

    public String getFieldName() {
        return "";
    }

    public void setFieldName(String name) throws Exception {
        name = name.trim();
        if (name.length() == 0) return;
        if (mdClass.findField(name) != null) {
            String msg = "Field '" + name + "' is already in the " +
                    "meta data for " + mdClass.getQName();
            JOptionPane.showMessageDialog(this, msg, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Class cls = mdClass.getCls();
        if (cls != null) {
            Field rf = findField(cls, name);
            String msg = null;
            if (rf != null) {
                if (rf.getDeclaringClass() != cls) {
                    msg = "Field '" + name + "' is declared in " + cls.getName() + "\n" +
                            "Fields from superclasses may only be persisted by making the " +
                            "superclass persistent.";
                } else {
                    if (!mdClass.getMdProject().isPersistableField(
                            mdClass.getMdDataStore().getName(), rf)) {
                        if (name.startsWith("jdo")) {
                            msg = "Fields with names starting with jdo may not be persisted.";
                        } else {
                            int m = rf.getModifiers();
                            if (Modifier.isStatic(m)) {
                                msg = "'static' fields may not be persisted";
                            } else if (Modifier.isFinal(m)) {
                                msg = "'final' fields may not be persisted";
                            } else {
                                Class t = rf.getType();
                                if (!Serializable.class.isAssignableFrom(t)) {
                                    msg = "Field '" + name + "' of type " + t.getName() + "\n" +
                                            "has no Java type mapping and is not Serializable";
                                } else {
                                    msg = "Field '" + name + "' of type " + t.getName() + "\n" +
                                            "has no Java type mapping";
                                }
                            }
                        }
                    } else {
                        MdField f = new MdField();
                        f.init(mdClass, rf);
                        mdClass.addField(f);
                        f.setPersistenceModifierInt(
                                MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
                        for (int i = fields.size() - 1; i >= 0; i--) {
                            FieldWrapper w = (FieldWrapper)fields.get(i);
                            if (w.field.getName() == rf.getName()) {
                                fields.remove(i);
                                break;
                            }
                        }
                    }
                }
            } else {
                msg = "Field '" + name + "' does not exist in " +
                        mdClass.getQName();
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(this, msg, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            MdElement e = new MdElement("field");
            e.setAttribute("name", name);
            MdField f = new MdField();
            f.init(mdClass, e);
            f.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
            mdClass.addField(f);
        }
    }

    private Field findField(Class cls, String fname) throws SecurityException {
        for (; ;) {
            try {
                return cls.getDeclaredField(fname);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
                if (cls == null) return null;
            }
        }
    }

    public boolean isAddFieldsEnabled() {
        return fieldsGrid.getSelectedObject() != null;
    }

    public void addFields() throws Exception {
        ArrayList a = fieldsGrid.getSelectedObjects();
        int n = a.size();
        if (n == 0) return;
        for (int i = 0; i < n; i++) {
            FieldWrapper w = (FieldWrapper)a.get(i);
            mdClass.addField(w.getName());
            fields.remove(w);
        }
    }

    public void close() throws Exception {
        BusinessLogicContainer plc = getParentLogicContainer();
        if (plc != null) plc.closeBusinessLogic();
    }

    public static class FieldWrapper {

        public Field field;

        public FieldWrapper(Field field) {
            this.field = field;
        }

        public String getName() {
            return field.getName();
        }

        public String getType() {
            return field.getType().getName();
        }

        public String getMods() {
            return Modifier.toString(field.getModifiers());
        }

    }

}

