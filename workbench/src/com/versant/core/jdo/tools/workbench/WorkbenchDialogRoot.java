
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

import za.co.hemtech.config.Config;
import za.co.hemtech.gui.MethodAction;
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.framework.BusinessLogic;
import za.co.hemtech.gui.framework.BusinessLogicContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * JRootPane wrapper for a WorkbenchPanel.
 */
public class WorkbenchDialogRoot extends JRootPane
        implements BusinessLogicContainer {

    private DialogOpener dialogOpener;
    private WorkbenchPanel form;
    private JPanel buttonPanel = new JPanel();
    private String command;

    public WorkbenchDialogRoot(DialogOpener dialogOpener, WorkbenchPanel form, String[] buttons, boolean isEscapeToCancel) {
        this.form = form;
        this.dialogOpener = dialogOpener;
        getContentPane().add(form, BorderLayout.CENTER);
        if (buttons.length > 0) {
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        }
        for (int i = 0; i < buttons.length;) {
            MethodAction ma = new MethodAction("closeDialog", this);
            ma.setText(buttons[i++]);
            ma.setIcon(buttons[i++]);
            ma.setToolTipText(buttons[i++]);
            JButton button = new JButton(ma);
            button.setActionCommand(ma.getText());
            buttonPanel.add(button);
        }
        if (isEscapeToCancel) {
            ((JComponent)getContentPane()).registerKeyboardAction(new MethodAction("closeDialog", this), "Cancel",
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        form.setBorder(null);
        form.setParentLogicContainer(this);
        ((JComponent)getContentPane()).setMinimumSize(new Dimension(1, 1));
    }

    public String  getTitle(){
        return form.getTitle();
    }

    public void closeDialog(ActionEvent ev, Object param) throws Exception {
        if (ev != null) {
            command = ev.getActionCommand();
        }
        if(!closeBusinessLogic()){
            command = null;
        };
    }

    public String getCommand() {
        return command;
    }

    /**
     * Open a new BusinessLogic.
     */
    public Container openBusinessLogic(BusinessLogic businessLogic) throws Exception {
        return null;
    }

    /**
     * Close the active BusinessLogic. This will close it unconditionally
     * and will not prompt to save changes or whatever.
     */
    public boolean closeBusinessLogic() throws Exception {
        if(form.closing()){
            dialogOpener.dispose();
            form.closed();
            return true;
        }
        return false;
    }

    public boolean closeBusinessLogic(BusinessLogic businessLogic) throws Exception {
        return closeBusinessLogic();
    }

    public boolean closeAllBusinessLogics() throws Exception {
        return  closeBusinessLogic();
    }

    public boolean containsBusinessLogic(BusinessLogic businessLogic) {
        return form == businessLogic;
    }

    /**
     * The active logic's title or icon has changed or it has become dirty
     * etc.
     */
    public void businessLogicChanged(BusinessLogic businessLogic) {
    }

    /**
     * Change the object on the status bar with an optional timeout.
     */
    public void setStatus(Object status, int ms) {
    }

    /**
     * The current BusinessLogic
     */
    public BusinessLogic getCurrent() {
        return form;
    }

    /**
     * Get out parent container
     */
    public BusinessLogicContainer getParentLogicContainer() {
        return null;
    }

    /**
     * Set the default logic to display.
     */
    public void setDefaultLogic(BusinessLogic defaultLogic) throws Exception {
    }

    public void init() throws Exception {
    }

    public void decorateModel(ExpTableModel model) throws Exception {
    }

    public void activated() throws Exception {
        form.activated();
    }

    public void deactivated() throws Exception {
    }

    /**
     * The user is attemting to close. Throw an exception or return false
     * to prevent this.
     */
    public boolean closing() throws Exception {
        return false;
    }

    public boolean isClosableBusiness() {
        return false;
    }

    public Icon getIcon() {
        return null;
    }

    public void closed() throws Exception {
    }

    public void setParentLogicContainer(BusinessLogicContainer container) {
    }

    public void setConfig(Config config) {
    }

    public Config getConfig() {
        return null;
    }

    public ExpTableModel getTableModel() {
        return null;
    }

    /**
     * Change the object on the status bar with an default timeout.
     */
    public void setStatus(Object status) {
    }

}

