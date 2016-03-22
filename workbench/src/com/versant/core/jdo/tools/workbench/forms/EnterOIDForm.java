
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
import com.versant.core.jdo.tools.workbench.WorkbenchDialog;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.MdClass;

/**
 * Form to capture an OID.
 */
public class EnterOIDForm extends WorkbenchPanel {

    private ExpTableModel modelThis = new ExpTableModel("modelThis");

    private MdClass selectedClass;
    private String oidString;

    public EnterOIDForm(MdClass currentClass) throws Exception {
        this.selectedClass = currentClass;
        modelThis.setConfig(getConfig());
        modelThis.getList().add(this);
        setTitle("Enter OID for " + currentClass.getName());
        setBorder(null);
        setModel(modelThis);
        if (!currentClass.isApplicationIdentity()) {
            oidString = currentClass.getDefJdbcClassId() + "-";
        }
    }

    public MdClass getSelectedClass() {
        return selectedClass;
    }

    public String getOidString() {
        return oidString;
    }

    public void setOidString(String oidString) throws Exception {
        this.oidString = oidString;
        getParentLogicContainer().closeBusinessLogic();
    }

}
