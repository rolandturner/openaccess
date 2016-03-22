
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
package com.versant.core.jdo.tools.workbench;

import za.co.hemtech.config.Config;
import za.co.hemtech.gui.BusinessPanel;
import za.co.hemtech.gui.Icons;
import za.co.hemtech.gui.MethodAction;
import za.co.hemtech.gui.ExceptionListener;
import za.co.hemtech.gui.framework.BusinessLogicContainer;
import za.co.hemtech.gui.model.CursorModel;
import za.co.hemtech.gui.model.CursorModelListener;
import za.co.hemtech.gui.util.VetoException;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.event.InputEvent;
import java.awt.*;
import java.io.File;

/**
 * Base class for panels in the workbench.
 */
public class WorkbenchPanel extends BusinessPanel
        implements TableModelListener, CursorModelListener,
        MdProjectListener, MdProjectProvider, ExceptionListener, 
        WorkbenchPanelHelper{

    WorkbenchPanelHelper workbenchPanelHelper;

    public WorkbenchPanel(String name) throws Exception {
        super(name);
        setConfig(Config.getInstance(getClass()));
    }

    public WorkbenchPanel() throws Exception {
        this("main");
    }

    /**
     * Get the global settings.
     */
    public WorkbenchSettings getSettings() {
        return WorkbenchSettings.getInstance();
    }

    /**
     * Get the open project or null if none.
     */
    public MdProject getProject() {
        return getMdProject();
    }

    /**
     * A new project has been opened.
     */
    public void projectOpened() throws Exception {
    }

    /**
     * The project has been closed.
     */
    public void projectClosed() throws Exception {
    }

    /**
     * The project has changed.
     */
    public void projectChanged(MdProjectEvent ev) {
    }

    /**
     * Open a dialog.
     */
    public boolean openDialog(WorkbenchPanel dlg, boolean modal, boolean hasOk,
            boolean hasCancel) throws Exception {
        return Utils.openDialog(this, dlg, modal, hasOk, hasCancel,
                dlg.getClass().getName(), true);
    }

    /**
     * Open a dialog.
     */
    public boolean openDialog(WorkbenchPanel dlg, boolean modal, boolean hasOk,
            boolean hasCancel, boolean isEscapeToCancel) throws Exception {
        return Utils.openDialog(this, dlg, modal, hasOk, hasCancel,
                dlg.getClass().getName(), isEscapeToCancel);
    }

    /**
     * Create a MethodAction.
     */
    protected MethodAction createAction(String method, String text, String tooltip,
            String icon) {
        return createAction(method, text, tooltip, icon, null);
    }

    /**
     * Create a MethodAction.
     */
    protected MethodAction createAction(String method, String text, String tooltip,
            String icon, String acc) {
        MethodAction a = new MethodAction(method, this);
        a.setText(text);
        a.setToolTipText(tooltip);
        if (icon != null) {
            if (icon.equals("Empty16.gif")) {
                a.setIcon(Icons.EMPTY16);
            } else {
                a.setIcon(icon);
            }
        }
        a.setExceptionListener(this);
        if (acc != null) a.setAccelerator(KeyStroke.getKeyStroke(acc));
        return a;
    }

    public void setModel(TableModel model) {
        TableModel old = getModel();
        if (old != null) {
            old.removeTableModelListener(this);
            if (old instanceof CursorModel) {
                ((CursorModel)old).removeCursorModelListener(this);
            }
        }
        super.setModel(model);
        if (model != null) {
            model.addTableModelListener(this);
            if (model instanceof CursorModel) {
                ((CursorModel)model).addCursorModelListener(this);
            }
        }
    }

    /**
     * Our model has changed.
     */
    public void tableChanged(TableModelEvent e) {
    }

    /**
     * From our model.
     *
     * @see CursorModelListener
     */
    public void cursorMoving(CursorModel source, int newRow, int newCol,
            InputEvent inputEvent) throws VetoException {
    }

    /**
     * From our model.
     *
     * @see CursorModelListener
     */
    public void cursorMoved(CursorModel source, int oldRow, int oldCol,
            InputEvent inputEvent) {
    }

    /**
     * From our model.
     *
     * @see CursorModelListener
     */
    public void cursorMoveBlocked(CursorModel source, int vk, int pageSize,
            InputEvent e) {
    }

    /**
     * Log an error message.
     */
    public void error(String msg) {
        Utils.getLogger().error(msg);
    }

    /**
     * Log an info message.
     */
    public void info(String msg) {
        Utils.getLogger().info(msg);
    }

    /**
     * Display a message on the status bar. It is not written to the message
     * log.
     */
    public void status(String msg) {
        Utils.getLogger().status(msg);
    }

    /**
     * Log an exception.
     */
    public void error(Throwable t) {
        Utils.getLogger().error(t);
    }

    /**
     * Get an icon for this form. This is used when forms are displayed
     * in dialogs and internal frames.
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Get rid of this unconditionally.
     */
    public void dispose() throws Exception {
        BusinessLogicContainer plc = getParentLogicContainer();
        if (plc != null) plc.closeBusinessLogic();
    }

    /**
     * This gets called when the system is idle and when our panel is
     * selected. Enable/disable our actions.
     */
    public void enableActions() {
    }

    /**
     * Get the toolbar for this panel or null if none. This must return
     * the same component every time (i.e. don't make a new one each time
     * this is called).
     */
    public JComponent getToolBar() {
        return null;
    }

    /**
     * Display an extra toolbar with buttons for class and field properties and
     * so on?
     */
    public boolean isMetaToolbarEnabled() {
        return false;
    }

    /**
     * A new class and/or field has been selected.
     */
    public void selectedClassChanged(MdClass selectedClass,
            MdField selectedField) {
    }

    /**
     * Get the directory where the project file is or null if there is no
     * open project.
     */
    public File getProjectDir() {
        MdProject p = getProject();
        return p == null ? null : p.getFile().getParentFile();
    }

    /**
     * Get the selected class or null if none.
     */
    public MdClass getSelectedClass() {
        return null;
    }

    /**
     * Get the selected class or interface or null if none.
     */
    public MdClassOrInterface getSelectedClassOrInterface() {
        return getSelectedClass();
    }

    /**
     * Get the selected field or null if none.
     */
    public MdField getSelectedField() {
        return null;
    }

    public MdProject getMdProject() {
        for(Component c = getParent(); c != null; c = c.getParent()){
            if(c instanceof MdProjectProvider){
                return ((MdProjectProvider)c).getMdProject();
            }
        }
        return MdProjectProviderManager.getDefaultProjectProvider().getMdProject();
    }

    public boolean handleException(Object o, Throwable throwable) {
        for(Component c = getParent(); c != null; c = c.getParent()){
            if(c instanceof ExceptionListener){
                return ((ExceptionListener)c).handleException(o, throwable);
            }
        }
        return ExceptionListenerManager.getDefaultExceptionListenerManager().handleException(o, throwable);
    }

    /**
     * Get the MainFrame we belong to.
     */
    public Frame getMainFrame() {
        for(Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame)p;
            }
        }
        return getWorkbenchPanelHelper().getMainFrame();
    }

    public boolean isReloadingClasses() {
        return getWorkbenchPanelHelper().isReloadingClasses();
    }

    public WorkbenchPanelHelper getWorkbenchPanelHelper() {
        if(workbenchPanelHelper != null){
            return workbenchPanelHelper;
        }
        for(Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof WorkbenchPanelHelper) {
                return (WorkbenchPanelHelper)p;
            }
        }
        return WorkbenchPanelHelperManager.getInstance().getWorkbenchPanelHelper();
    }
}
