
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

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.*;
import za.co.hemtech.gui.exp.ExpTableModel;

import java.awt.*;

/**
 * Form to edit a MdTable.
 * @keep-all
 */
public class EditMdTableForm extends WorkbenchPanel {

    private ExpTableModel modelThis = new ExpTableModel("modelThis");

    public EditMdTableForm(MdTable table) throws Exception {
        modelThis.setConfig(getConfig());
        modelThis.add(table);
        setModel(modelThis);
        setTitle("Edit Table");
        setPreferredSize(new Dimension(500, 300));
    }

}

