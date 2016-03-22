
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

import za.co.hemtech.gui.editor.AlternateValueEditor;
import za.co.hemtech.gui.editor.ValueEditSite;
import za.co.hemtech.gui.painter.CellPainter;
import za.co.hemtech.gui.exp.ExpTableModel;
import com.versant.core.jdo.tools.workbench.forms.ChooseClassForm;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdUtils;
import com.versant.core.jdo.tools.workbench.model.MdValue;
import com.versant.core.jdo.tools.workbench.model.MdProjectProviderManager;

import javax.swing.table.TableModel;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MdClassNameValueEditor implements AlternateValueEditor {

    public void startEditing(ValueEditSite site, int x, int y, int width,
            int height, InputEvent e) throws Exception {
        MdValue value = (MdValue)site.getEditValue();
        if (value.isReadOnly()) {
            site.stopEditing(false);
            return;
        }
        String oldClass = value.getText();
        String defClass = value.getDefText();
        if (!MdUtils.isStringNotEmpty(oldClass)) {
            oldClass = defClass;
        }
        ArrayList classList;
        if (value.isOnlyFromPickList()) {
            classList = new ArrayList();
        } else {
            MdProject project;
            TableModel editModel = site.getEditModel();
            if (editModel instanceof ExpTableModel) {
                ExpTableModel exp = (ExpTableModel) editModel;
                Object o = exp.get(site.getEditRow());
                project = MdProjectProviderManager.findProjectProvider(new Object[] {o, site.getEditContainer()}).getMdProject();
            }else{
                project = MdProjectProviderManager.findProjectProvider(new Object[] {site.getEditContainer()}).getMdProject();
            }
            classList = project.getProjectClassLoader().getAllClasses();
            HashSet classSet = new HashSet(project.getAllClassNames());
            classSet.addAll(project.getAllInterfaceNames());
            for (Iterator i = classList.iterator(); i.hasNext();) {
                if (classSet.contains(i.next())) i.remove();
            }
        }
        ChooseClassForm f = new ChooseClassForm(classList);
        try {
            f.setTopList(value.getPickList());
            String[] buttons = null;
            if (!MdUtils.isStringNotEmpty(oldClass)) {
                oldClass = defClass;
            }
            if (MdUtils.isStringNotEmpty(defClass)) {
                buttons = new String[]{
                    "OK", "Ok16.gif", "Accept changes and close the dialog",
                    "Default", null, "Reset Default value",
                    "Cancel", "cancel.gif",
                    "Cancel changes"};
            }
            if (buttons == null) {
                buttons = new String[]{
                    "OK", "Ok16.gif", "Accept changes and close the dialog",
                    "None", "Delete16.gif",
                    "No Class selected",
                    "Cancel", "cancel.gif",
                    "Cancel changes"};
            }
            f.setMultiSelect(false);
            f.setDefaultClass(oldClass);
            String button = Utils.openDialog(site.getEditContainer(), f,
                    true, buttons, f.getName(), true);
            if (!"Cancel".equals(button)) {
                List classes = f.getSelectedClasses();
                String className = null;
                if ("OK".equals(button)) {
                    if (classes.size() > 0) {
                        className = classes.get(0).toString();
                    } else {
                        return;
                    }
                }
                value.setText(className);
                site.setEditValue(value);
            }
        } finally {
            f.dispose();
            site.stopEditing();
        }
    }

    public boolean isOkToStopEditing() throws Exception {
        return true;
    }

    public void stopEditing(boolean saveChanges) throws Exception {

    }

    public boolean isAutoSaveChanges() {
        return false;
    }

    public CellPainter getCellPainter(Object value) {
        return null;
    }

    public void addKeyListener(KeyListener l) {

    }

    public void removeKeyListener(KeyListener l) {

    }

    public boolean isQuickEditor() {
        return false;
    }

    public String getName() {
        return "Choose Class Dialog";
    }

    public String getReturnType() {
        return "MdClassNameValue";
    }
}
