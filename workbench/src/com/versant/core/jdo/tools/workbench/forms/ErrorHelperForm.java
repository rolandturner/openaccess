
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
import com.versant.core.jdo.tools.workbench.WorkbenchDialog;
import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import za.co.hemtech.gui.util.GuiUtils;
import za.co.hemtech.gui.framework.BusinessLogicContainer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Displays information about errors in the meta data. Form some errors
 * extra info is displayed with an option to open a dialog to fix the
 * problem (e.g. collection and map element types not set).
 */
public class ErrorHelperForm extends WorkbenchPanel {

    private JPanel messagePanel = new JPanel();

    private List colAndMapList;

    public ErrorHelperForm(Throwable e) throws Exception {
        e.printStackTrace(System.out);
        setConfig(getConfig());
        setTitle("Metadata parse error");
        messagePanel.setName("message");
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        JPanel exceptionMessage = new JPanel();
        exceptionMessage.setLayout(new GridBagLayout());
        exceptionMessage.setBorder(new TitledBorder("Error message"));
        JTextArea message = new JTextArea(e.toString());
        message.setWrapStyleWord(true);
        message.setColumns(60);
        message.setEditable(false);
        message.setForeground(Color.red);
        exceptionMessage.add(message,
                new GridBagConstraints(0, 0, 1, 1, 1, 1,
                        GridBagConstraints.WEST,
                        GridBagConstraints.BOTH,
                        new Insets(4, 4, 4, 4), 0, 0));
        messagePanel.add(exceptionMessage);

        MdProject project = getProject();

        colAndMapList = project.getCollectionAndMapFieldsInError();
        if (!colAndMapList.isEmpty()) {
            String colAndMapMessage =
                    "Collections and maps in Java may hold instances of any class. To generate the JDBC schema Open \n" +
                    "Access needs to know what type will be stored in the collection or map. You must specify the \n" +
                    "element-type for collections and the key-type and value-type for maps in the meta data.";
            JButton colAndMapButton = createPanel("Collections and Maps",
                    colAndMapMessage);

            colAndMapButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        CollectionErrorFieldForm f = new CollectionErrorFieldForm(
                                colAndMapList);
                        openDialog(f, true, true, false);
                        getParentLogicContainer().closeBusinessLogic();
                        MdProject project = getMdProject();
                        if (project != null){
                            try {
                                project.compileMetaData(false, true);
                            } catch (Exception ex) {
                                // open Open Access assistant
                                Utils.openDialog(ErrorHelperForm.this, new ErrorHelperForm(ex), true, true, false);
                            }
                        }
                    } catch (Exception e1) {
                        GuiUtils.dispatchException(ErrorHelperForm.this, e1);
                    }
                }
            });
        }
        add(messagePanel);
    }

    public void activated() throws Exception {
        super.activated();
        BusinessLogicContainer container = getParentLogicContainer();
        if (container != null && container instanceof WorkbenchDialog) {
            ((WorkbenchDialog)container).pack();
        }
    }

    private JButton createPanel(String tytle, String message) {
        JPanel collectionMapErrorPanel = new JPanel();
        collectionMapErrorPanel.setLayout(new GridBagLayout());
        collectionMapErrorPanel.setBorder(new TitledBorder(tytle));

        JTextArea area = new JTextArea(message);
        area.setWrapStyleWord(true);
        area.setColumns(60);
        area.setEditable(false);
        collectionMapErrorPanel.add(area,
                new GridBagConstraints(0, 0, 1, 1, 0, 1,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(4, 4, 4, 4), 0, 0));
        JButton colAndMapButton = new JButton("Fix");
        collectionMapErrorPanel.add(colAndMapButton,
                new GridBagConstraints(0, 1, 1, 1, 1, 1,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE,
                        new Insets(4, 4, 4, 4), 0, 0));
        messagePanel.add(collectionMapErrorPanel);
        return colAndMapButton;
    }

}
