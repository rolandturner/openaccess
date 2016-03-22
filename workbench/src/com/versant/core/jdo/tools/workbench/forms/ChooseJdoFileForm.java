
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
import com.versant.core.jdo.tools.workbench.model.MdJdoFile;

/**
 * Select a .jdo file.
 */
public class ChooseJdoFileForm extends WorkbenchPanel {

    private ExpTableModel modelJdoFiles = new ExpTableModel("modelJdoFiles");
    private String title;

    public ChooseJdoFileForm(String title) throws Exception {
        this.title = title;
        modelJdoFiles.setConfig(getConfig());
        setModel(modelJdoFiles);
        updateJdoFileList();
    }

    private void updateJdoFileList() {
        modelJdoFiles.getList().setList(getProject().getJdoFileList());
    }

    public String getTitle() {
        return title;
    }

    public MdJdoFile getSelectedFile() {
        return (MdJdoFile)modelJdoFiles.getCursorObject();
    }

    public void maintainJdoFiles() throws Exception {
        ProjectPropertyForm f = new ProjectPropertyForm(getProject());
        f.setSelectedTab(ProjectPropertyForm.TAB_JDO_FILES);
        openDialog(f, true, true, false);
        updateJdoFileList();
    }

}

