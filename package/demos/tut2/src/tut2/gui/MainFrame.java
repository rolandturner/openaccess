
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
package tut2.gui;

import tut2.JDOSupport;
import tut2.model.Person;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;

import com.versant.core.jdo.VersantPersistenceManager;

/**
 * This frame displays a List of Person objects. The user can add, edit and
 * remove persons. A single PersistenceManager is used with optimistic
 * transactions. The GUI makes sure that there is always an active transaction.
 * To save changes commit is done on the transaction.
 *
 */
public class MainFrame extends JFrame implements ListSelectionListener {

    private PersistenceManager pm = JDOSupport.getInstance().getPMF().getPersistenceManager();
    private JList list = new JList();                   // JList to display people
    private PersonPanel personPanel = new PersonPanel();// panel to edit a person
    private Person person;                              // the currently selected person
    private ListModel model = new ListModel();          // model containing the people

    public MainFrame() throws Exception {
        super("JDO Genie Tut2");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.setModel(model);
        JScrollPane scrollPane = new JScrollPane();
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addPerson();
            }
        });
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removePerson();
            }
        });
        JButton reload = new JButton("Reload Data");
        reload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        JButton save = new JButton("Save Data");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        JButton exit = new JButton("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(save);
        buttonPanel.add(reload);
        buttonPanel.add(exit);
        scrollPane.getViewport().add(list);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(personPanel, BorderLayout.EAST);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        load();
    }

    /**
     * Start a transaction and load the list of Persons etc.
     */
    private void load() throws Exception {
        pm.currentTransaction().begin();
        personPanel.load(pm);
        Query query = pm.newQuery(Person.class);
        query.setOrdering("firstName ascending, lastName ascending");
        try {
            model.setData(new ArrayList((Collection)query.execute()));
        } finally {
            query.closeAll();
        }
        list.setSelectedIndex(0);
    }

    /**
     * Have any changes been made since the last save? This uses a JDO Genie
     * specific method to check if there are any dirty objects in the
     * transaction.
     */
    private boolean isDirty() {
        return ((VersantPersistenceManager)pm).isDirty();
    }

    /**
     * Saves all the changes in the current transaction and starts a new one.
     */
    private void save() {
        if (!isDirty()) return;
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
    }

    /**
     * Discards all the changes made in the current transaction and starts
     * a new one.
     */
    private void reload() {
        if (isDirty()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Discard unsaved changes and reload data?",
                    "Confirm Reload", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
        }
        pm.currentTransaction().rollback();
        try {
            load();
        } catch (Exception x) {
            handleException(x);
        }
    }

    /**
     * Creates a new Person object.
     */
    private void addPerson() {
        Person person = new Person();
        try {
            if (new AddDialog(this, person, pm).confirm()) {
                model.add(person);
                list.setSelectedValue(person, true);
                pm.makePersistent(person);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Removes the currently selected user.
     */
    private void removePerson() {
        Person person = (Person)list.getSelectedValue();
        try {
            if (new RemoveDialog(this, person, pm).confirm()) {
                this.person = null;
                model.remove(person);
                pm.deletePersistent(person);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * The JList has been navigated.
     */
    public void valueChanged(ListSelectionEvent e) {
        personPanel.setPersonValues(person);
        person = (Person)list.getSelectedValue();
        personPanel.setPerson(person);
    }

    /**
     * Prompt to save changes before exit.
     */
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) exit();
        super.processWindowEvent(e);
    }

    /**
     * Displays an error message for an exception.
     *
     * @param x the Exception
     */
    public void handleException(Throwable x) {
        x.printStackTrace(System.out);
        getToolkit().beep();
        JOptionPane.showMessageDialog(this,
                x.getMessage() + "\n\nPlease see console for details", "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method does a clean exit.  If there are any unsaved shanges to
     * the persistent objects the user will be prompted to save or not, else
     * the user will get a 'r u sure?'.
     */
    private void exit() {
        if (isDirty()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Save changes?", "Confirm Save",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (option != JOptionPane.CANCEL_OPTION) {
                if (option == JOptionPane.YES_OPTION) save();
                System.exit(0);
            }
        } else {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) System.exit(0);
        }
    }

}

