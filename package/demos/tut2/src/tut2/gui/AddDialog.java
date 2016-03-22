
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

import tut2.model.Person;

import javax.jdo.PersistenceManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This Dialog adds a new user to the current transaction.
 * <p>
 */
public class AddDialog extends JDialog implements ActionListener {

    protected PersonPanel personPanel = new PersonPanel();
    protected Person person;
    protected boolean ok;

    /**
     * Constructs a new AddDialog
     * @param owner perant frame
     * @param person person object to add
     * @param pm the PersistenceManager to use to load country data and persist the person object
     * @throws Exception if the Country data could not be loaded
     */
    public AddDialog(Frame owner, Person person, PersistenceManager pm) throws Exception {
        super(owner, "Add a new Person", true);
        this.person = person;
        personPanel.load(pm);
        personPanel.setPerson(person);
        getContentPane().add(personPanel, BorderLayout.CENTER);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(this);
        JPanel panel = new JPanel();
        panel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(cancelButton);
        getContentPane().add(panel, BorderLayout.SOUTH);
    }

    /**
     * Packs the dialog and makes it vissible (the dialog is model)
     * @return true if the 'OK' button was clicked false if it was 'Cancel'
     */
    public boolean confirm() {
        pack();
        setVisible(true);
        return ok;
    }

    /**
     * Ok clicked
     */
    public void actionPerformed(ActionEvent e) {
        if (ok = doCheck()) {
            dispose();
        }
    }

    /**
     * Do we have all the information we need?
     * @return false if we need more info; true if everyting is ok
     */
    protected boolean doCheck() {
        personPanel.setPersonValues(person);
        String fName = person.getFirstName();
        if (fName == null || fName.length() <= 0) { //Make sure first name is filled in
            JOptionPane.showMessageDialog(this, "First name must be filled in.",
                    "No first name",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String lName = person.getLastName();
        if (lName == null || lName.length() <= 0) { //Make sure last name is filled in
            JOptionPane.showMessageDialog(this, "Last name must be filled in.",
                    "No last name",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}

