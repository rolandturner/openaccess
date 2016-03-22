
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
package gui;

import util.JDOSupport;
import model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * GUI frame.
 */
public class MainFrame extends JFrame implements ActionListener {

    private Branch branch;

    public MainFrame() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.add(createMenuItem("Choose Branch", "chooseBranch"));
        file.add(createMenuItem("Exit", "fileExit"));
        bar.add(file);

        JMenu view = new JMenu("View");
        view.add(createMenuItem("Orders", "viewOrders"));
        view.add(createMenuItem("Suppliers", "viewSuppliers"));
        view.add(createMenuItem("Items", "viewItems"));
        bar.add(view);

        setJMenuBar(bar);

        updateTitle();
    }

    private JMenuItem createMenuItem(String text, String method) {
        JMenuItem ans = new JMenuItem(text);
        ans.setActionCommand(method);
        ans.addActionListener(this);
        return ans;
    }

    private void updateTitle() {
        String t;
        if (branch == null) {
            t = "JDO Genie - Legacy DB and Swing Demo";
        } else {
            t = branch.getName();
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
        JDOSupport.rollback();
        JOptionPane.showMessageDialog(this, t.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void chooseBranch() {
        ChooseBranchDialog dlg = new ChooseBranchDialog(this, branch);
        dlg.setVisible(true);
        branch = dlg.getBranch();
        updateTitle();
    }

    public void fileExit() {
        JDOSupport.shutdown();
        System.exit(0);
    }

    public void viewOrders() {
        ViewOrdersDialog dlg = new ViewOrdersDialog(this, branch);
        dlg.setVisible(true);
    }

    public void viewSuppliers() {
        ViewSuppliersDialog dlg = new ViewSuppliersDialog(this, branch);
        dlg.setVisible(true);
    }

    public void viewItems() {
        ViewItemsDialog dlg = new ViewItemsDialog(this, branch);
        dlg.setVisible(true);
    }

}

