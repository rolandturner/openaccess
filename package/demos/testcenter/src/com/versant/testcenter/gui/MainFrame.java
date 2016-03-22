
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
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.versant.testcenter.service.Context;
import com.versant.testcenter.service.TestCenterService;
import com.versant.testcenter.model.SystemUser;
import com.versant.testcenter.model.Student;
import com.versant.testcenter.model.Administrator;

/**
 * GUI frame. This uses the same service layer as the web application. It
 * connects to the JDO Genie server running in the web tier.
 *
 * @see Main
 */
public class MainFrame extends JFrame implements ActionListener {

    private JPanel holder = new JPanel(new BorderLayout());

    private static final Border DISPLAY_BORDER =
            BorderFactory.createEmptyBorder(5, 5, 5, 5);

    public MainFrame() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.add(createMenuItem("Register", "register"));
        file.add(createMenuItem("Login", "login"));
        file.add(createMenuItem("Logout", "logout"));
        file.add(createMenuItem("Exit", "fileExit"));
        bar.add(file);
        setJMenuBar(bar);

        getContentPane().add(holder, BorderLayout.CENTER);

        updateTitle();
    }

    private JMenuItem createMenuItem(String text, String method) {
        JMenuItem ans = new JMenuItem(text);
        ans.setActionCommand(method);
        ans.addActionListener(this);
        return ans;
    }

    private void updateTitle() {
        SystemUser u = Context.getContext().getCurrentUser();
        String t;
        if (u == null) {
            t = "JDO Genie - Testcenter Swing Client Demo";
        } else {
            t = "JDO Genie - Testcenter: " + u.getFirstName() + " " + u.getSurname();
        }
        setTitle(t);
    }

    /**
     * All the menu options dispatch events here when they are used. The
     * actionCommand is expected to be the name of the method to invoke to
     * handle the action. This avoids creating millions of anonymous listener
     * classes.
     */
    public void actionPerformed(ActionEvent e) {
        invoke(this, e.getActionCommand());
    }

    /**
     * Invoke methodName on target and handle any exceptions.
     */
    public void invoke(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName, null);
            m.invoke(target, null);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException)t).getTargetException();
            }
            handleException(t);
        }
    }

    public void handleException(Throwable t) {
        t.printStackTrace(System.out);
        JOptionPane.showMessageDialog(this, t.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void display(JPanel p) {
        holder.removeAll();
        if (p != null) {
            p.setBorder(DISPLAY_BORDER);
            holder.add(p, BorderLayout.CENTER);
        }
        holder.revalidate();
        repaint();
    }

    public void login() {
        LoginDialog dlg = new LoginDialog(this);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            logout();
            if (!TestCenterService.login(dlg.getLogin(), dlg.getPassword())) {
                JOptionPane.showMessageDialog(this, "Invalid login or password",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                updateForCurrentUser();
            }
        } else if (dlg.isRegister()) {
            register();
        }
    }

    private void updateForCurrentUser() {
        updateTitle();
        SystemUser u = Context.getContext().getCurrentUser();
        if (u instanceof Student) {
            display(new StudentPanel(this));
        } else if (u instanceof Administrator) {
            display(new AdminPanel(this));
        }
    }

    public void logout() {
        Context.getContext().setCurrentUser(null);
        display(null);
        updateTitle();
    }

    public void register() {
        RegisterDialog dlg = new RegisterDialog(this);
        dlg.setVisible(true);
        if (!dlg.isOk()) return;
        TestCenterService.beginTxn();
        Student student = TestCenterService.createStudent();
        student.setFirstName(dlg.getFirstName());
        student.setSurname(dlg.getSurname());
        student.setLogin(dlg.getLogin());
        student.setPassword(dlg.getPassword());
        TestCenterService.commitTxn();
        logout();
        Context.getContext().setCurrentUser(student);
        updateForCurrentUser();
    }

    public void fileExit() {
        System.exit(0);
    }

}

