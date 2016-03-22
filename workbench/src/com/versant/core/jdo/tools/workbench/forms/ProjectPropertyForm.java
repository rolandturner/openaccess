
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
import za.co.hemtech.gui.form.FormTreeTab;
import za.co.hemtech.gui.util.GuiUtils;
import za.co.hemtech.gui.util.VetoException;
import com.versant.core.jdbc.JdbcConverterFactoryRegistry;
import com.versant.core.jdo.tools.workbench.*;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * View/edit project properties.
 */
public class ProjectPropertyForm extends WorkbenchPanel
        implements MdDsTypeChangedListener {

    public static final int TAB_JDO_FILES = 2;
    public static final int TAB_ANT = 3;
    public static final int TAB_JDO_QUERY_FILES = 4;

    private ExpTableModel modelProject = new ExpTableModel("modelProject");

    private FormTreeTab tabs;
    private FormPanel tabJdoFiles;
    private FormPanel tabJdoQueryFiles;
    private FormPanel tabAnt;
    private FormPanel fillModePreviewHolder;
    private MdProject project;
    private ProjectFillModePreviewForm fillModePreview;

    private static final String[] JDBC_DS_FORMS = new String[]{
        "formDsConnection", "formDsPoolSettings", "formDsGeneral",
        "formDsJavaMapping", "formDsJdbcMapping", "scoFactoryMapping",
        "formDsNameGen", "formDsSchemaMigration"};

    private static final String[] VDS_DS_FORMS = new String[]{
        "formDsConnectionVDS", "formDsPoolSettingsVDS", "formDsGeneralVDS",
        "scoFactoryMapping"};

    public ProjectPropertyForm(MdProject project) throws Exception {
        this.project = project;
        fillModePreview = new ProjectFillModePreviewForm(project);
        modelProject.setConfig(getConfig());
        setModel(modelProject);
        project.addMdDsTypeChangedListener(this);
        modelProject.add(project);
        redoDsTabs();
    }

    public MdProject getProject() {
        return project;
    }

    public void dataStoreTypeChanged(MdDsTypeChangedEvent event) {
        try {
            redoDsTabs();
        } catch (Exception e) {
            za.co.hemtech.gui.util.GuiUtils.dispatchException(this, e);
        }
    }

    private void redoDsTabs() throws Exception {
        if(fillModePreviewHolder.getComponentCount() == 0){
            fillModePreviewHolder.add(fillModePreview);
        }
        ExpTableModel modelDs = new ExpTableModel("modelDs");
        modelDs.setConfig(getConfig());
        MdDataStore ds = getProject().getDataStore();
        modelDs.add(ds);
        String[] delForms = !ds.isJDBC() ? JDBC_DS_FORMS : VDS_DS_FORMS;
        for (int j = delForms.length - 1; j >= 0; j--) {
            String name = delForms[j];
            Component component = tabs.getComponent(name);
            if (component != null) {
                tabs.removeContent(component);
            }
        }
        FormPanel root = null;
        String[] addForms = ds.isJDBC() ? JDBC_DS_FORMS : VDS_DS_FORMS;
        for (int j = 0; j < addForms.length; j++) {
            FormPanel p = new FormPanel(addForms[j]);
            p.setBusinessLogic(this);
            p.setConfig(getConfig());
            p.setModel(modelDs);
            if (j == 0) {
                p.setTitle("Datastore");
                tabs.addContent(root = p);
            } else {
                tabs.addContent(root, p);
            }
        }
        tabs.setSelectedComponent(root);
    }

    public void closed() throws Exception {
        super.closed();
        getProject().removeMdDsTypeChangedListener(this);
    }

    public String getTitle() {
        return "Project Properties";
    }

    public boolean isCancelable() {
        return false;
    }

    public void setTabs(JComponent tabs) {
        this.tabs = (FormTreeTab)tabs;
    }

    public void setTabJdoFiles(JComponent tabJdoFiles) {
        this.tabJdoFiles = (FormPanel)tabJdoFiles;
    }

    public void setTabJdoQueryFiles(JComponent tabJdoQueryFiles) {
        this.tabJdoQueryFiles = (FormPanel)tabJdoQueryFiles;
    }

    public void setTabAnt(JComponent tabAnt) {
        this.tabAnt = (FormPanel)tabAnt;
    }

    public void setProjectFillModePane(JComponent component) {
        fillModePreviewHolder = (FormPanel)component;
        component.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                fillModePreview.redoPreview();
            }
        });
    }

    /**
     * Activate the tab.
     *
     * @see #TAB_JDO_FILES
     */
    public void setSelectedTab(int tab) {
        Component c;
        switch (tab) {
            case TAB_ANT:
                c = tabAnt;
                break;
            case TAB_JDO_FILES:
                c = tabJdoFiles;
                break;
            case TAB_JDO_QUERY_FILES:
                c = tabJdoQueryFiles;
                break;
            default:
                throw new IllegalArgumentException("Invalid tab: " + tab);
        }
        if (c != null && tabs != null) tabs.setSelectedComponent(c);
    }

    public void browseForAntBuildFile() throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getProject().getFile().getParentFile());
        fc.setDialogTitle("Select Ant Build File");
        fc.setFileFilter(new AntBuildFileFilter());
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        getProject().setBuildFile(fc.getSelectedFile());
        modelProject.listUpdated();
    }

    public void browseForAntCommand() throws Exception {
        String c = browseForAntCommand(this,
                getProject().getFile().getParentFile());
        if (c != null) {
            WorkbenchSettings.getInstance().setAntCommand(c);
            modelProject.listUpdated();
        }
    }

    /**
     * Open a JFileChooser to locate the command to run Ant.
     */
    public static String browseForAntCommand(Component parent, File currentDir)
            throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(currentDir);
        fc.setDialogTitle("Select Command To Run Ant");
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        return fc.getSelectedFile().toString();
    }

    public boolean isReloadAntBuildFileEnabled() {
        return getProject().getBuildFile() != null;
    }

    public void reloadAntBuildFile() throws Exception {
        if (getProject().getBuildFile() == null) return;
        getProject().getAntRunner().reloadBuildFile();
        modelProject.listUpdated();
    }

    public void browseForPropertiesFile() throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getProject().getFile().getParentFile());
        fc.setDialogTitle("Select Properties File");
        fc.setFileFilter(new PropertyFileFilter());
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        getProject().setTokenPropsFile(fc.getSelectedFile());
        modelProject.listUpdated();
        repaint();
    }

    public boolean isReloadPropertiesFileEnabled() {
        return getProject().getTokenPropsFile() != null;
    }

    public void reloadPropertiesFile() throws Exception {
        MdProject p = getProject();
        File f = p.getTokenPropsFile();
        if (f == null) return;
        p.setTokenPropsFile(f);
        modelProject.listUpdated();
        repaint();
    }

    //= Classpath ========================================================

    public void addClasspathJar() throws Exception {
        JFileChooser fc = new JFileChooser(getProject().getLastClasspathDir());
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Add Jar to Classpath");
        fc.setFileFilter(new JarFileFilter());
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        addToClasspath(fc.getSelectedFiles());
        getProject().setLastClasspathDir(fc.getCurrentDirectory());
    }

    private void addToClasspath(File[] a) {
        List list = getProject().getClassPathList();
        for (int i = a.length - 1; i >= 0; i--) {
            File f = a[i];
            if (!list.contains(f)) getProject().addClassPathFile(f);
        }
        modelProject.listUpdated();
    }

    public void addClasspathDir() throws Exception {
        JFileChooser fc = new JFileChooser(getProject().getLastClasspathDir());
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Add Directory to Classpath");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(new DirFileFilter());
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        addToClasspath(fc.getSelectedFiles());
        getProject().setLastClasspathDir(fc.getCurrentDirectory());
    }

    public void removeClasspathEntry(ExpTableModel model) {
        File f = (File)model.getCursorObject();
        if (f == null) return;
        getProject().removeClassPathFile(f);
        modelProject.listUpdated();
    }

    public boolean isRemoveClasspathEntryEnabled(ExpTableModel model) {
        return model.getCursorObject() != null;
    }

    public void moveClasspathEntryUp(ExpTableModel model) {
        int i = model.getCursorRow();
        if (i < 1) return;
        List list = model.getList();
        Object t = list.get(i - 1);
        list.set(i - 1, list.get(i));
        list.set(i, t);
        model.setCursorPosition(i - 1, 0, null);
        getProject().setDirty(true);
    }

    public boolean isMoveClasspathEntryUpEnabled(ExpTableModel model) {
        return model.getCursorRow() > 0;
    }

    public void moveClasspathEntryDown(ExpTableModel model) {
        int i = model.getCursorRow();
        List list = model.getList();
        if (i >= list.size() - 1) return;
        Object t = list.get(i + 1);
        list.set(i + 1, list.get(i));
        list.set(i, t);
        model.setCursorPosition(i + 1, 0, null);
        getProject().setDirty(true);
    }

    public boolean isMoveClasspathEntryDownEnabled(ExpTableModel model) {
        return model.getCursorRow() < model.getList().size() - 1;
    }

    public boolean isDataStoreSelected(ExpTableModel model) throws Exception {
        return model.getCursorObject() != null;
    }

    public void editDataStoreProperties(ExpTableModel model) throws Exception {
//        MdDataStore ds = (MdDataStore)model.getCursorObject();
//        if (ds == null) return;
//        DataStorePropertyForm f = new DataStorePropertyForm(ds);
//        openDialog(f, true, true, false);
//        ds.updateForMiscChanges();
//        ds.getDataStorePanel().repaint();
    }

    //= JDO Meta Data Files ==============================================

    public void addJdoFile() {
        JFileChooser fc = new JFileChooser(getProject().getLastJdoFileDir());
        fc.setDialogTitle("Add .jdo meta data file to project");
        fc.setFileFilter(new JdoFileFilter());
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File[] a = fc.getSelectedFiles();
        MdProject project = getProject();

        // try and find each file on the classpath and add it to the project
        int n = a.length;
        for (int i = 0; i < n; i++) {
            String res = project.toResourceName(a[i]);
            if (res == null) {
                throw new VetoException(
                        "Not found on project classpath: " + a[i]);
            }
            MdJdoFile f = new MdJdoFile(project, res);
            project.addJdoFile(f);
            try {
                f.getDocument();
            } catch (Exception e) {
                project.getLogger().error(e);
            }
        }

        project.setLastJdoFileDir(fc.getCurrentDirectory());
        modelProject.listUpdated();
    }

    public void createJdoFile(ExpTableModel model) throws Exception {
        JFileChooser fc = new JFileChooser(getProject().getLastJdoFileDir());
        fc.setDialogTitle("Choose name for new .jdo meta data file");
        fc.setFileFilter(new JdoFileFilter());
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        String name = file.getPath();
        if (!name.endsWith(".jdo")) file = new File(name + ".jdo");
        if (file.exists()) {
            int op = JOptionPane.showConfirmDialog(this,
                    "Overwrite " + file + "?");
            if (op != JOptionPane.YES_OPTION) return;
        }
        MdProject project = getProject();
        String res = project.toResourceName(file);
        if (res == null) {
            throw new VetoException("Not found on project classpath: " + file);
        }
        MdJdoFile f = new MdJdoFile(project, res);
        f.createDocument(file);
        project.addJdoFile(f);
        project.setLastJdoFileDir(fc.getCurrentDirectory());
        modelProject.listUpdated();
        repaint();
    }

    public void removeJdoFile(ExpTableModel model) {
        MdJdoFile f = (MdJdoFile)model.getCursorObject();
        if (f == null) return;
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to " +
                "remove " + f + " ?") != JOptionPane.YES_OPTION) {
            return;
        }
        getProject().removeJdoFile(f);
        modelProject.listUpdated();
    }

    public boolean isJdoFileSelected(ExpTableModel model) {
        return model.getCursorObject() != null;
    }

    private static class JdoFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String n = f.getName();
            return n.endsWith(".jdo");
        }

        public String getDescription() {
            return "JDO Meta Data Files (*.jdo)";
        }
    }

    //= Datastore ========================================================

    public void testDataStore(ExpTableModel model) throws Exception {
        if (!canTestCon(model)) return;
        try {
            ClassLoader cl = new MdClassLoader(
                    MdUtils.toURLArray(getProject().getClassPathList()));
            Connection con = null;
            try {
                MdDataStore ds = (MdDataStore)model.getCursorObject();
                con = ds.connect(cl);
            } finally {
                if (con != null) con.close();
            }
            JOptionPane.showMessageDialog(this, "Connected OK!");
        } catch (Exception e) {
            GuiUtils.dispatchException(this, e);
        }
    }

    public boolean canTestCon(ExpTableModel model) {
        MdDataStore ds = (MdDataStore)model.getCursorObject();
        return ds.isJDBC();
    }

    public void chooseDriver(ExpTableModel model) throws Exception {
        MdDataStore ds = (MdDataStore)model.getCursorObject();
        if (ds == null) return;
        ChooseDriverForm f = new ChooseDriverForm();
        if (!Utils.openDialog(this, f, true, true, true)) return;
        MdDriver d = f.getSelectedDriver();
        if (d != null) {
            ds.fillFromDriver(d);
            modelProject.listUpdated();
        }
        repaint();
    }

    public void fillWithAntTokens(ExpTableModel model) throws Exception {
        MdDataStore ds = (MdDataStore)model.getCursorObject();
        int op = JOptionPane.showConfirmDialog(this,
                "This will replace all connection fields with filter tokens\n" +
                "for Ant (e.g. the URL field becomes @" + ds.getName().toUpperCase() + ".URL@).\n" +
                "The current connection field values are written to a properties\n" +
                "file suitable for use with the Ant 'filter' task. This file is\n" +
                "also used to resolve the tokens while in the Workbench.\n\n" +
                "Continue?",
                "Ant Filter Tokens", JOptionPane.YES_NO_CANCEL_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        File f = null;
        PropertySaver p = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select File for Token Properties");
            fc.setFileFilter(new PropertyFileFilter());
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                f = fc.getSelectedFile();
                String s = f.toString();
                if (!s.endsWith(PropertyFileFilter.EXT)) {
                    f = new File(s + PropertyFileFilter.EXT);
                }
                if (f.exists()) {
                    op = JOptionPane.showConfirmDialog(this,
                            "Overwrite " + f + " ?");
                    if (op != JOptionPane.OK_OPTION) return;
                }
                p = new PropertySaver();
            }
            ds.fillWithAntTokens(ds.getName().toUpperCase() + ".", p);
        } finally {
            if (p != null) p.store(new FileOutputStream(f));
        }

        getProject().setTokenPropsFile(f);
        modelProject.listUpdated();
    }

    public void removeUserAndPassword(ExpTableModel model) {
        MdDataStore ds = (MdDataStore)model.getCursorObject();
        ds.setUserT(null);
        ds.setPasswordT(null);
        modelProject.listUpdated();
        repaint();
    }

    //= User metrics =====================================================

    public void addUserBaseMetric(ExpTableModel model) throws Exception {
        MdUserBaseMetric u = new MdUserBaseMetric();
        u.setName("User" + (model.getRowCount() + 1));
        u.setDisplayName(u.getName());
        u.setDescription("");
        u.setCategory("User");
        model.add(u);
        model.setSelected(u);
    }

    public boolean isRemoveUserBaseMetricEnabled(ExpTableModel model) {
        return model.getCursorObject() != null;
    }

    public void removeUserBaseMetric(ExpTableModel model) throws Exception {
        MdUserBaseMetric u = (MdUserBaseMetric)model.getCursorObject();
        if (u == null) return;
        int op = JOptionPane.showConfirmDialog(this, "Delete " + u + "?");
        if (op == JOptionPane.YES_OPTION) model.remove(u);
    }

    //= Java type mappings ===============================================

    public void addJavaTypeMapping(ExpTableModel model) throws Exception {
        MdProject project = getProject();
        String cname = getJavaTypeMappingClass(model);
        if (MdUtils.isStringNotEmpty(cname)) {
            try {
                Class.forName(cname);
            } catch (ClassNotFoundException e) {
                try {
                    project.getProjectClassLoader().loadClass(cname);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(this,
                            "Could not load class: " + cname);
                    return;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No Class entered.");
            return;
        }
        MdDataStore ds = project.getDataStore();
        MdJdbcJavaTypeMapping tm = new MdJdbcJavaTypeMapping(ds);
        tm.setCustom(true);
        tm.setDatabase(ds.getSelectedDBImp());
        tm.setEnabled(new MdValue("true"));
        tm.setJavaType(cname);
        String conv = chooseConverter(cname);
        tm.setConverter(new MdValue(conv));
        if (conv == JdbcConverterFactoryRegistry.BYTES_CONVERTER_NAME) {
            tm.setJdbcType("BLOB");
        } else {
            tm.setJdbcType("VARCHAR");
        }
        ds.addJavaTypeMapping(tm);
        model.fireTableModelEvent();
        model.setSelected(tm);
    }

    private String getJavaTypeMappingClass(final ExpTableModel model)
            throws Exception {
        String msg = "Please enter the full name of the class you wish to map.";
        JButton b = new JButton("Project Classes");
        final JTextField f = new JTextField();
        f.setColumns(25);
        JPanel p = new JPanel();
        p.add(f);
        p.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String s = getJavaTypeMappingProjectClass(model);
                    if (s != null) {
                        f.setText(s);
                    }
                } catch (Exception e1) {
                    za.co.hemtech.gui.util.GuiUtils.dispatchException(f, e1);
                }
            }
        });
        JOptionPane.showMessageDialog(this,
                new Object[]{msg, p}, "Select Class",
                JOptionPane.QUESTION_MESSAGE);
        return f.getText();
    }

    private String getJavaTypeMappingProjectClass(ExpTableModel model)
            throws Exception {
        MdProject project = getProject();
        ArrayList a = project.getProjectClassLoader().getAllClasses();
        HashSet toRemove = new HashSet(project.getAllClassNames());
        toRemove.addAll(project.getAllInterfaceNames());
        for (Iterator i = model.getList().iterator(); i.hasNext();) {
            MdJdbcJavaTypeMapping tm = (MdJdbcJavaTypeMapping)i.next();
            toRemove.add(tm.getJavaType());
        }
        for (Iterator i = a.iterator(); i.hasNext();) {
            if (toRemove.contains(i.next())) i.remove();
        }
        ChooseClassForm f = new ChooseClassForm(a);
        f.setMultiSelect(false);
        if (!openDialog(f, true, true, true)) return null;
        List l = f.getSelectedClasses();
        if (l == null || l.isEmpty()) return null;
        return (String)l.get(0);
    }

    /**
     * See if we have a generic converter for class cname.
     */
    private String chooseConverter(String cname) {
        Class cls = getProject().loadClass(cname);
        if (cls == null) return null;
        try {
            cls.getConstructor(new Class[]{String.class});
            return JdbcConverterFactoryRegistry.STRING_CONVERTER_NAME;
        } catch (NoSuchMethodException e) {
        }
        try {
            cls.getConstructor(new Class[]{byte[].class});
            cls.getMethod("toBytes", null);
            return JdbcConverterFactoryRegistry.BYTES_CONVERTER_NAME;
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    public boolean isRemoveJavaTypeMappingEnabled(ExpTableModel model)
            throws Exception {
        MdJdbcJavaTypeMapping tm = (MdJdbcJavaTypeMapping)model.getCursorObject();
        return tm != null && tm.isCustom();
    }

    public void removeJavaTypeMapping(ExpTableModel model) throws Exception {
        MdJdbcJavaTypeMapping tm = (MdJdbcJavaTypeMapping)model.getCursorObject();
        if (tm == null || !tm.isCustom()) return;
        MdDataStore ds = getProject().getDataStore();
        ds.removeJavaTypeMapping(tm);
        model.fireTableModelEvent();
    }

    //= Externalizers ===============================================

    public void addExternalizer(ExpTableModel model) throws Exception {
        MdProject project = getProject();
        String cname = getExternalizerTypeClass(model);
        if (MdUtils.isStringNotEmpty(cname)) {
            try {
                Class.forName(cname);
            } catch (ClassNotFoundException e) {
                try {
                    project.getProjectClassLoader().loadClass(cname);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(this,
                            "Could not load class: " + cname);
                    return;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No Class entered.");
            return;
        }
        MdExternalizer e = new MdExternalizer();
        e.setTypeName(cname);
        e.setEnabled(true);
        e.setExternalizerStr(chooseExternalizer(cname));
        project.addExternalizer(e);
        model.fireTableModelEvent();
        model.setSelected(e);
    }

    private String getExternalizerTypeClass(final ExpTableModel model)
            throws Exception {
        String msg = "Please enter the full name of the class you wish to externalize:";
        JButton b = new JButton("Project Classes");
        final JTextField f = new JTextField();
        f.setColumns(25);
        JPanel p = new JPanel();
        p.add(f);
        p.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String s = getExternalizerProjectClass(model);
                    if (s != null) {
                        f.setText(s);
                    }
                } catch (Exception e1) {
                    za.co.hemtech.gui.util.GuiUtils.dispatchException(f, e1);
                }
            }
        });
        JOptionPane.showMessageDialog(this,
                new Object[]{msg, p}, "Select Class",
                JOptionPane.QUESTION_MESSAGE);
        return f.getText();
    }

    private String getExternalizerProjectClass(ExpTableModel model)
            throws Exception {
        MdProject project = getProject();
        ArrayList a = project.getProjectClassLoader().getAllClasses();
        HashSet toRemove = new HashSet(project.getAllClassNames());
        toRemove.addAll(project.getAllInterfaceNames());
        for (Iterator i = model.getList().iterator(); i.hasNext();) {
            MdExternalizer e = (MdExternalizer)i.next();
            toRemove.add(e.getTypeName());
        }
        for (Iterator i = a.iterator(); i.hasNext();) {
            if (toRemove.contains(i.next())) i.remove();
        }
        ChooseClassForm f = new ChooseClassForm(a);
        f.setMultiSelect(false);
        if (!openDialog(f, true, true, true)) return null;
        List l = f.getSelectedClasses();
        if (l == null || l.isEmpty()) return null;
        return (String)l.get(0);
    }

    /**
     * See if we have a generic externalizer for class cname.
     */
    private String chooseExternalizer(String cname) {
//        Class cls = getProject().loadClass(cname);
//        if (cls == null) return null;
//        try {
//            cls.getConstructor(new Class[]{String.class});
//            return JdbcConverterFactoryRegistry.STRING_CONVERTER_NAME;
//        } catch (NoSuchMethodException e) {
//        }
//        try {
//            cls.getConstructor(new Class[]{byte[].class});
//            cls.getMethod("toBytes", null);
//            return JdbcConverterFactoryRegistry.BYTES_CONVERTER_NAME;
//        } catch (NoSuchMethodException e) {
//        }
        return null;
    }

    public void removeExternalizer(ExpTableModel model) throws Exception {
        MdExternalizer e = (MdExternalizer)model.getCursorObject();
        if (e == null) return;
        getProject().removeExternalizer(e);
        model.fireTableModelEvent();
    }

    public boolean isRemoveExternalizerEnabled(ExpTableModel model)
            throws Exception {
        return model.getCursorObject() != null;
    }

}

