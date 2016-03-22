
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
 * Prompt for username and password.
 *
 */
public class LoginDialog extends DialogBase {

    private JTextField fieldLogin = new JTextField(10);
    private JPasswordField fieldPassword = new JPasswordField(10);
    private boolean ok, register;

    public LoginDialog(MainFrame owner) {
        super(owner, "Login", true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton(" OK ", "ok"));
        buttonPanel.add(createButton(" Cancel ", "cancel"));
        buttonPanel.add(createButton(" Register ", "register"));

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        addField(fieldPanel, 0, "Login", fieldLogin);
        addField(fieldPanel, 1, "Password", fieldPassword);

        JLabel about = new JLabel("<html><body><table border=0 width=350><tr><td>" +
                "This application manages exam sittings. You can log on as the " +
                "administrator (username admin, password admin) and create exams. Or you " +
                "can register as a student and manage the exams you are registered for." +
                "</td></tr></table></body></html>");

        getContentPane().add(about, BorderLayout.NORTH);
        getContentPane().add(fieldPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public void ok() {
        ok = true;
        dispose();
    }

    public void cancel() {
        dispose();
    }

    public void register() {
        register = true;
        dispose();
    }

    public boolean isOk() {
        return ok;
    }

    public boolean isRegister() {
        return register;
    }

    public String getLogin() {
        return fieldLogin.getText();
    }

    public String getPassword() {
        return new String(fieldPassword.getPassword());
    }

}


