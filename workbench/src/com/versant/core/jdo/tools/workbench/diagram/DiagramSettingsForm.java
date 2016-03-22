
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
package com.versant.core.jdo.tools.workbench.diagram;

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import za.co.hemtech.gui.exp.ExpTableModel;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Edit settings for a Diagram. Fires ChangeEvent's if the user clicks apply
 * or ok.
 *
 * @keep-all
 */
public class DiagramSettingsForm extends WorkbenchPanel {

    private ClassDiagram.Settings original;
    private ExpTableModel modelSettings = new ExpTableModel("modelSettings");

    public DiagramSettingsForm() throws Exception {
        modelSettings.setConfig(getConfig());
        setModel(modelSettings);
    }

    public void setDiagramSettings(ClassDiagram.Settings s) {
        original = s;
        modelSettings.clear();
        modelSettings.add(new ClassDiagram.Settings(s));
    }

    public ClassDiagram.Settings getDiagramSettings() {
        if (modelSettings.getRowCount() == 0) return null;
        return (ClassDiagram.Settings)modelSettings.get(0);
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireStateChanged() {
        ChangeEvent ev = new ChangeEvent(this);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i + 1]).stateChanged(ev);
            }
        }
    }

    public void apply() {
        original.fillFrom(getDiagramSettings());
        fireStateChanged();
    }

    public void ok() throws Exception {
        apply();
        dispose();
    }

    public void cancel() throws Exception {
        dispose();
    }

    public void saveToDefaults() throws Exception {
        getSettings().getDiagramSettings().fillFrom(getDiagramSettings());
    }

    public void loadFromDefaults() throws Exception {
        getDiagramSettings().fillFrom(getSettings().getDiagramSettings());
        repaint();
    }

}

