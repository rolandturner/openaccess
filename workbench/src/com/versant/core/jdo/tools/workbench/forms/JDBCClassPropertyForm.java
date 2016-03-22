
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
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.XmlPreviewPanel;
import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.sql.lexer.SQLFilterPane;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * View/edit properties for a class.
 */
public class JDBCClassPropertyForm extends WorkbenchPanel {

    private ExpTableModel modelClass;
    private ExpTableModel modelIndexes = new ExpTableModel("modelIndexes");
    private ExpTableModel modelFetchGroups = new ExpTableModel(
            "modelFetchGroups");

    private FormPanel formClass = new FormPanel("formClass");
    private FormPanel formInheritance = new FormPanel("formInheritance");
    private FormPanel formIndexes = new FormPanel("formIndexes");
    private FormPanel formFetchGroups = new FormPanel("formFetchGroups");
    private FormPanel formConstraintDep = new FormPanel("formConstraintDep");

    private XmlPreviewPanel xmlPreview = new XmlPreviewPanel("xmlPreview",
            true);
    private SQLFilterPane ddlPreview;

    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
    private JLabel errorLabel = new JLabel();

    private MdClassTable table = new MdClassTable();
    private FieldERGraphForm formGraph = new FieldERGraphForm();
    private JScrollPane scroller = new JScrollPane(formGraph);

    private JCheckBox chkNoJdoClassCol = new JCheckBox();

    private boolean inActionPerformed;

    public JDBCClassPropertyForm() throws Exception {
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
        formInheritance.setBusinessLogic(this);
        formIndexes.setBusinessLogic(this);
        formFetchGroups.setBusinessLogic(this);
        formConstraintDep.setBusinessLogic(this);

        formClass.setConfig(getConfig());
        formInheritance.setConfig(getConfig());
        formIndexes.setConfig(getConfig());
        formFetchGroups.setConfig(getConfig());
        formConstraintDep.setConfig(getConfig());

        formClass.setTitle(null);
        formIndexes.setTitle(null);
        formFetchGroups.setTitle(null);
        formConstraintDep.setTitle(null);

        formClass.setModel(getModel());
        formInheritance.setModel(getModel());
        formIndexes.setModel(modelIndexes);
        formFetchGroups.setModel(modelFetchGroups);
        formConstraintDep.setModel(getModel());
        xmlPreview.setModel(getModel());
        xmlPreview.setColumnName("element");

        scroller.setName("scroller");
        scroller.setBorder(null);

        formGraph.getGraph().setAutoSpacingX(22);

        chkNoJdoClassCol.setName("chkNoJdoClassCol");
        chkNoJdoClassCol.setText("Do not use a discriminator (jdo_class) " +
                "column");
        chkNoJdoClassCol.setToolTipText("Tick this box if the hierarchy uses " +
                "only vertical inheritance and you do not want a jdo_class " +
                "column");
        chkNoJdoClassCol.addActionListener(this);
        formInheritance.add(chkNoJdoClassCol);

        formClass.add(scroller);
        formClass.add(formInheritance);

        ddlPreview = new SQLFilterPane(getMdProject());

        tabs.setName("tabs");
        tabs.addTab("Class", formClass);
        tabs.addTab("Indexes", formIndexes);
        tabs.addTab("Fetch groups", formFetchGroups);
        tabs.addTab("Persist order", formConstraintDep);
        tabs.addTab("JDO XML", xmlPreview);
        tabs.addTab("Schema", ddlPreview);

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

        chkNoJdoClassCol.setSelected(c.isJdbcClassIdNo());
        chkNoJdoClassCol.setEnabled(c.hasSubclasses() && c.isNoPCSuperclass());
        try {
            ddlPreview.setText(c.getDDLText());
        } catch (Exception e) {
            ByteArrayOutputStream out = null;
            PrintWriter pw = null;
            try {
                out = new ByteArrayOutputStream(1024);
                pw = new PrintWriter(out, false);
                e.printStackTrace(pw);
                pw.flush();
                out.flush();
                ddlPreview.setText(out.toString());
                out.close();
                pw.close();
            } catch (IOException e1) {
                e1.printStackTrace(System.out);
            }
        }

        formGraph.getDiagram().clear();
        table.init(c, true);
        addTreeToDiagram(table, formGraph.getDiagram());
        if (isReloadingClasses()) {
            formGraph.getGraph().setEnabled(false);
        } else {
            formGraph.getGraph().refresh();
            formGraph.getGraph().setEnabled(true);
        }
        if (c != null && c.hasErrors()) {
            errorLabel.setText(c.getErrorText());
            errorLabel.setVisible(true);
        } else {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Add all our ancestors and ourselves to the diagram.
     */
    private void addTreeToDiagram(MdClassTable table, ClassDiagram diagram) {
        MdClassTable superTable = table.getSuperTable();
        if (superTable != null) addTreeToDiagram(superTable, diagram);
        diagram.addTable(table);
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
        modelClass.fireCursorObjectUpdated();
    }

    public void removeConstraintDep(ExpTableModel aModel) {
        MdClass c = (MdClass)modelClass.getCursorObject();
        if (c == null) return;

        String s = (String)aModel.getCursorObject();
        if (s == null) return;

        c.removeConstraintDepClass(s);
        modelClass.fireCursorObjectUpdated();
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
        modelClass.fireCursorObjectUpdated();
    }

    public void removeFetchGroup(ExpTableModel model) throws Exception {
        MdFetchGroup g = (MdFetchGroup)model.getCursorObject();
        if (g == null) return;
        int op = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove '" + g.getName() + "'\n" +
                "from the meta data ?");
        if (op != JOptionPane.OK_OPTION) return;
        g.getMdClass().removeFetchGroup(g);
        model.fireCursorObjectUpdated();
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
        model.fireCursorObjectUpdated();
    }

    public boolean isRemoveFetchGroupFieldEnabled(ExpTableModel model)
            throws Exception {
        return model.getCursorObject() != null;
    }

    public void removeFetchGroupField(ExpTableModel model) throws Exception {
        MdFetchGroupField f = (MdFetchGroupField)model.getCursorObject();
        if (f == null) return;
        f.getMdFetchGroup().removeField(f);
        model.fireCursorObjectUpdated();
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
            if (o == chkNoJdoClassCol) {
                getMdClass().setJdbcClassIdNo(chkNoJdoClassCol.isSelected());
            }
        } catch (Exception x) {
            GuiUtils.dispatchException(this, x);
        } finally {
            inActionPerformed = false;
        }
    }

}
