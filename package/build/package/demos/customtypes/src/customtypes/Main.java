
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
package customtypes;

import customtypes.model.Contact;
import customtypes.model.PhoneNumber;

import javax.swing.*;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple Swing GUI listing all the contacts and allowing edits.
 */
public class Main extends JFrame implements ActionListener {

    public static void main(String[] args) {
        Main main = new Main();
        main.setSize(600, 400);
        main.setVisible(true);
    }

    private PersistenceManager pm = Sys.pmf().getPersistenceManager();
    private JList list = new JList();

    public Main() {
        super("JDO Genie Custom Types demo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        list.setCellRenderer(new ContactCellRenderer());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Add", "add"));
        buttonPanel.add(createButton("Edit", "edit"));
        buttonPanel.add(createButton("Delete", "delete"));
        buttonPanel.add(createButton("Exit", "dispose"));
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        refresh();
    }

    /**
     * Refresh the list of contacts.
     */
    public void refresh() {
        list.setListData(new Object[0]);
        pm.evictAll();
        Query q = pm.newQuery(Contact.class);
        q.setOrdering("name ascending");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        list.setListData(a.toArray());
    }

    /**
     * Edit the selected contact.
     */
    public void edit() {
        Contact c = (Contact)list.getSelectedValue();
        if (c == null) return;
        try {
            pm.currentTransaction().begin();
            pm.refresh(c);
            EditContactDialog dlg = new EditContactDialog(this, c,
                    "Edit " + c.getName());
            if (dlg.display()) pm.currentTransaction().commit();
        } finally {
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
            refresh();
        }
    }

    /**
     * Add a new contact.
     */
    public void add() {
        Contact c = new Contact();
        c.setName("");
        c.setEmail("");
        c.setPhone(new PhoneNumber("", "", ""));
        EditContactDialog dlg = new EditContactDialog(this, c, "Add Contact");
        if (dlg.display()) {
            pm.currentTransaction().begin();
            pm.makePersistent(c);
            pm.currentTransaction().commit();
            refresh();
        }
    }

    /**
     * Delete the selected contact.
     */
    public void delete() {
        Contact c = (Contact)list.getSelectedValue();
        if (c == null) return;
        pm.currentTransaction().begin();
        pm.deletePersistent(c);
        pm.currentTransaction().commit();
        refresh();
    }

    private JButton createButton(String text, String method) {
        JButton b = new JButton(text);
        b.addActionListener(this);
        b.setActionCommand(method);
        return b;
    }

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
                t = ((InvocationTargetException) t).getTargetException();
            }
            handleException(t);
        }
    }

    public void handleException(Throwable t) {
        t.printStackTrace(System.out);
        JOptionPane.showMessageDialog(this, t.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            Sys.shutdown();
            System.exit(0);
        }
    }

}

