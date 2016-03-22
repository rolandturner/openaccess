
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
import com.versant.core.jdo.tools.workbench.model.MdColumn;
import za.co.hemtech.gui.exp.ExpTableModel;

/**
 * Form to edit a MdColumn.
 * @keep-all
 */
public class EditMdColumnForm extends WorkbenchPanel {

    private ExpTableModel model = new ExpTableModel("model");

    private MdColumn col;

    public EditMdColumnForm(String title) throws Exception {
        model.setConfig(getConfig());
        setModel(model);
        setTitle(title);
    }

    public MdColumn getCol() {
        return col;
    }

    public void setCol(MdColumn col) {
        this.col = col;
        model.clear();
        model.add(col);
    }

}

