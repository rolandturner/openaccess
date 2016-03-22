
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
package com.versant.testcenter.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Get registration details for a new user.
 *
 */
public class RegisterDialog extends DialogBase {

    private JTextField fieldFirstName = new JTextField(10);
    private JTextField fieldSurname = new JTextField(10);
    private JTextField fieldLogin = new JTextField(10);
    private JPasswordField fieldPassword = new JPasswordField(10);
    private boolean ok;

    public RegisterDialog(MainFrame owner) {
        super(owner, "Login", true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton(" OK ", "ok"));
        buttonPanel.add(createButton(" Cancel ", "cancel"));

        JPanel fp = new JPanel(new GridBagLayout());
        addField(fp, 0, "First name", fieldFirstName);
        addField(fp, 1, "Surname", fieldSurname);
        addField(fp, 2, "Login", fieldLogin);
        addField(fp, 3, "Password", fieldPassword);

        getContentPane().add(fp, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public void ok() {
        if (getFirstName().length() == 0 || getSurname().length() == 0
                || getLogin().length() == 0 || getPassword().length() == 0) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }
        ok = true;
        dispose();
    }

    public void cancel() {
        dispose();
    }

    public boolean isOk() {
        return ok;
    }

    public String getFirstName() {
        return fieldFirstName.getText();
    }

    public String getSurname() {
        return fieldSurname.getText();
    }

    public String getLogin() {
        return fieldLogin.getText();
    }

    public String getPassword() {
        return new String(fieldPassword.getPassword());
    }

}


