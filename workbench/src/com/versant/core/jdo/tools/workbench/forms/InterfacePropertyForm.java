
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
import za.co.hemtech.gui.model.CursorModel;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.XmlPreviewPanel;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.InputEvent;

/**
 * View/edit properties for an interface.
 * @keep-all
 */
public class InterfacePropertyForm extends WorkbenchPanel {

    private ExpTableModel modelInterface = new ExpTableModel("modelInterface");

    private FormPanel formInterface = new FormPanel("formInterface");
    private XmlPreviewPanel xmlPreview = new XmlPreviewPanel("xmlPreview",true);

    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);

    public InterfacePropertyForm() throws Exception {
        modelInterface.setConfig(getConfig());
        setModel(modelInterface);

        formInterface.setBusinessLogic(this);
        formInterface.setConfig(getConfig());
        formInterface.setTitle(null);
        formInterface.setModel(getModel());

        xmlPreview.setModel(getModel());
        xmlPreview.setColumnName("element");
        tabs.setName("tabs");
        tabs.addTab("Interface", formInterface);
        tabs.addTab("JDO XML", xmlPreview);

        add(tabs);

        setTitle("Interface Properties");

        updateSetup();
    }

    public void setMdInterface(MdInterface iface) {
        modelInterface.getList().getList().clear();
        if (iface != null) modelInterface.getList().getList().add(iface);
        modelInterface.getList().fireListUpdated();
    }

    public MdInterface getMdInterface() {
        return (MdInterface)modelInterface.getCursorObject();
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

    private void updateSetup() {
        // nothing to do yet
    }

}

