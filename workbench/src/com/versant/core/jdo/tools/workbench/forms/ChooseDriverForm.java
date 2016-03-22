
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
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.MdDriver;

/**
 * @keep-all
 * Select a driver.
 */
public class ChooseDriverForm extends WorkbenchPanel {

    private ExpTableModel modelDriver = new ExpTableModel("modelDriver");

    public ChooseDriverForm() throws Exception {
        modelDriver.setConfig(getConfig());
        setModel(modelDriver);
        modelDriver.getList().setList(getSettings().getDriverList());
    }

    public String getTitle() {
        return "Select Driver";
    }

    public MdDriver getSelectedDriver() {
        return (MdDriver)modelDriver.getCursorObject();
    }

}

