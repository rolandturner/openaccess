
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
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.diagram.classdiagram.*;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * View/edit properties for a special field (opt-lock, version).
 *
 * @keep-all
 */
public class VDSSpecialFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formSpecial = new FormPanel("formSpecial");
    private JLabel labHelp = new JLabel();

    public VDSSpecialFieldPropertyForm() throws Exception {
        formSpecial.setBusinessLogic(this);
        formSpecial.setConfig(getConfig());
        labHelp.setName("labHelp");
        labHelp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        formSpecial.add(labHelp);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        formSpecial.setModel(getModel());
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup() {
        super.updateSetup();
        MdField field = getMdField();

        setActive(formSpecial);

        // help
        labHelp.setText(createHtmlForHelp(field.getDialogHelp()));
    }

    private String createHtmlForHelp(String info) {
        StringBuffer s = new StringBuffer();
        s.append("<html><body><p><font face=\"dialog\" size=\"-1\">");
        s.append(info);
        s.append("</font></p></body></html>");
        return s.toString();
    }

    public String getTabTitle() {
        return "Special";
    }

}

