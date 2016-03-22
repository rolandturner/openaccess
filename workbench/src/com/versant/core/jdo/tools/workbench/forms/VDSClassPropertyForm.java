
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
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.model.CursorModel;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.XmlPreviewPanel;
import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * View/edit properties for a class.
 */
public class VDSClassPropertyForm extends WorkbenchPanel {

    private ExpTableModel modelClass;
    private ExpTableModel modelIndexes = new ExpTableModel("modelIndexes");
    private ExpTableModel modelFetchGroups = new ExpTableModel(
            "modelFetchGroups");

    private FormPanel formClass = new FormPanel("formClass");
    private FormPanel formIndexes = new FormPanel("formIndexes");
    private FormPanel formFetchGroups = new FormPanel("formFetchGroups");

    private XmlPreviewPanel xmlPreview = new XmlPreviewPanel("xmlPreview",
            true);

    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
    private JLabel errorLabel = new JLabel();

    public VDSClassPropertyForm() throws Exception {
        modelClass = new ExpTableModel("modelClass");
        modelClass.setConfig(getConfig());
        setModel(modelClass);

        modelIndexes.setConfig(getConfig());
        modelIndexes.setMaster(modelClass);
        modelIndexes.setMasterColumnName("indexList");

        modelFetchGroups.setConfig(getConfig());
        modelFetchGroups.setMaster(modelClass);
        modelFetchGroups.setMasterColumnName("fetchGroupList");

        formClass.setBusinessLogic(this);
        formIndexes.setBusinessLogic(this);
        formFetchGroups.setBusinessLogic(this);

        formClass.setConfig(getConfig());
        formIndexes.setConfig(getConfig());
        formFetchGroups.setConfig(getConfig());

        formClass.setTitle(null);
        formIndexes.setTitle(null);
        formFetchGroups.setTitle(null);

        formClass.setModel(getModel());
        formIndexes.setModel(modelIndexes);
        formFetchGroups.setModel(modelFetchGroups);
        xmlPreview.setModel(getModel());
        xmlPreview.setColumnName("element");

        tabs.setName("tabs");
        tabs.addTab("Class", formClass);
        tabs.addTab("Indexes", formIndexes);
        tabs.addTab("Fetch groups", formFetchGroups);
        tabs.addTab("JDO XML", xmlPreview);

        add(tabs);
        errorLabel.setName("errorLabel");
        add(errorLabel);

        setTitle("Class Properties");

        updateSetup();
    }

    public void setMdClass(MdClass mdClass) {
        if (mdClass != null) {
            modelClass.getList().getList().clear();
            modelClass.add(mdClass);
        }
    }

    /**
     * Get the selected class or null if none.
     */
    public MdClass getSelectedClass() {
        return getMdClass();
    }

    private MdClass getMdClass() {
        return (MdClass)modelClass.getCursorObject();
    }

    public void tableChanged(TableModelEvent e) {
        updateSetup();
    }

    public void cursorMoved(CursorModel source, int oldRow, int oldCol,
            InputEvent inputEvent) {
        updateSetup();
    }

    public void projectChanged(MdProjectEvent ev) {
        switch (ev.getFlags()) {
            case MdProjectEvent.ID_ENGINE_STARTED:
            case MdProjectEvent.ID_ENGINE_STOPPED:
            case MdProjectEvent.ID_PARSED_DATABASE:
            case MdProjectEvent.ID_DIRTY_FLAG:
            case MdProjectEvent.ID_CLASSES_REMOVED:
//            case MdProjectEvent.ID_PARSED_META_DATA:
            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                return;
        }
        updateSetup();
    }

    void updateSetup() {
        MdClass c = getMdClass();
        if (c == null) return;
        if (c != null && c.hasErrors()) {
            errorLabel.setText(c.getErrorText());
            errorLabel.setVisible(true);
        } else {
            errorLabel.setVisible(false);
        }
    }

    public void addConstraintDep() throws Exception {
        MdClass c = (MdClass)modelClass.getCursorObject();
        if (c == null) return;

        MdProject project = getProject();
        if (project == null) return;

        List classList = new ArrayList(project.getAllClassNames());
        classList.remove(c.getClassMetaData().qname);

        ChooseClassForm f = new ChooseClassForm(classList);
        if (!openDialog(f, true, true, true)) return;
        List classes = f.getSelectedClasses();
        if (classes == null) return;
        int n = classes.size();
        if (n == 0) return;

        for (int i = 0; i < classes.size(); i++) {
            String s = (String)classes.get(i);
            if (s == null) continue;
            c.addConstraintDepClass(s);
        }
    }

    public void removeConstraintDep(ExpTableModel aModel) {
        MdClass c = (MdClass)modelClass.getCursorObject();
        if (c == null) return;

        String s = (String)aModel.getCursorObject();
        if (s == null) return;

        c.removeConstraintDepClass(s);
    }

    /**
     * Is a fetch group selected?
     */
    public boolean isFetchGroupAvailable(ExpTableModel model) {
        return model.getCursorObject() != null;
    }

    public void addFetchGroup() throws Exception {
        MdClass c = (MdClass)modelClass.getCursorObject();
        if (c == null) return;
        c.addFetchGroup(new MdFetchGroup(c));
    }

    public void removeFetchGroup(ExpTableModel model) throws Exception {
        MdFetchGroup g = (MdFetchGroup)model.getCursorObject();
        if (g == null) return;
        int op = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove '" + g.getName() + "'\n" +
                "from the meta data ?");
        if (op != JOptionPane.OK_OPTION) return;
        g.getMdClass().removeFetchGroup(g);
    }

    public boolean isAddFetchGroupFieldEnabled(ExpTableModel model) {
        return model.getMaster().getCursorObject() != null;
    }

    public void addFetchGroupField(ExpTableModel model) throws Exception {
        MdFetchGroup g = (MdFetchGroup)model.getMaster().getCursorObject();
        if (g == null) return;
        List all = g.getMdClass().getFieldList();
        int n = all.size();
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)all.get(i);
            if (f instanceof MdSpecialField) continue;
            if (g.findField(f.getName()) == null) a.add(f);
        }
        ChooseFieldForm cf = new ChooseFieldForm(a);
        if (Utils.openDialog(this, cf, true, true, true)) {
            for (Iterator i = cf.getSelectedFields().iterator(); i.hasNext();) {
                MdField f = (MdField)i.next();
                MdFetchGroupField mf = new MdFetchGroupField(g, f.getName());
                g.addField(mf);
            }
        }
    }

    public boolean isRemoveFetchGroupFieldEnabled(ExpTableModel model)
            throws Exception {
        return model.getCursorObject() != null;
    }

    public void removeFetchGroupField(ExpTableModel model) throws Exception {
        MdFetchGroupField f = (MdFetchGroupField)model.getCursorObject();
        if (f == null) return;
        f.getMdFetchGroup().removeField(f);
    }

    /**
     * Is an index selected?
     */
    public boolean isIndexAvailable(ExpTableModel model) {
        return model.getCursorObject() != null;
    }

    public void addIndex() throws Exception {
        MdClass c = getMdClass();
        if (c == null) return;
        MdIndex idx = new MdIndex(c);
        c.addIndex(idx);
        editIndex(idx);
    }

    public void removeIndex(ExpTableModel model) throws Exception {
        MdIndex idx = (MdIndex)model.getCursorObject();
        if (idx == null) return;
        int op = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove index '" + idx.getName() + "'\n" +
                "with fields '" + idx.getFieldSummary() + "'\n" +
                "from the meta data ?");
        if (op != JOptionPane.OK_OPTION) return;
        idx.getMdClass().removeIndex(idx);
    }

    public void editIndexFields(ExpTableModel model) throws Exception {
        MdIndex idx = (MdIndex)model.getCursorObject();
        if (idx == null) return;
        editIndex(idx);
    }

    private void editIndex(MdIndex idx) throws Exception {
        EditIndexFieldsForm f = new EditIndexFieldsForm();
        f.setIndex(idx);
        try {
            Utils.openDialog(this, f, true, true, false);
        } finally {
            modelClass.fireCursorObjectUpdated();
        }
    }
}
