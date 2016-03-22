
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

import tut2.model.Address;
import tut2.model.Person;

import javax.jdo.PersistenceManager;
import javax.swing.*;
import java.awt.*;

/**
 * This is a gui panel used to display and edit Persons<p>
 * The load(PersistenceManager pm) method must be called before it is displayed.
 * <p>
 */
public class PersonPanel extends JPanel {
    private JTextField firstName = new JTextField();
    private JTextField lastName = new JTextField();
    private AddressPanel address = new AddressPanel();

    public PersonPanel() {
        setLayout(new GridBagLayout());
        firstName.setColumns(20);
        lastName.setColumns(20);
        add(new JLabel("First Name", SwingConstants.RIGHT), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 2, 2, 2), 0, 0));
        add(firstName, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 2, 2), 0, 0));
        add(new JLabel("Last Name", SwingConstants.RIGHT), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        add(lastName, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(address, new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 5, 2), 0, 0));
    }

    public void load(PersistenceManager pm) throws Exception {
        address.load(pm);
    }

    /**
     * Enables or disables all the components on this panel.
     * @param enabled true if the components should be enabled, false otherwise
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        firstName.setEnabled(enabled);
        lastName.setEnabled(enabled);
        address.setEnabled(enabled);
    }

    /**
     * Set the dagta in the display on a given Person
     * @param person to set display values on
     */
    public void setPersonValues(Person person) {
        if (person == null) return;
        String value = this.firstName.getText();
        if (!value.equals(person.getFirstName())) {
            person.setFirstName(value);
        }
        value = this.lastName.getText();
        if (!value.equals(person.getLastName())) {
            person.setLastName(value);
        }
        Address address = this.address.getAddress();
        if (!address.equals(person.getAddress())) {
            person.setAddress(address);
        }
    }

    /**
     * Sets the data in the display to reflect this Person.
     * @param person the Person to display
     */
    public void setPerson(Person person) {
        if (person == null) {
            firstName.setText("");
            lastName.setText("");
            address.setAddress(null);
            return;
        }
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        address.setAddress(person.getAddress());
    }
}

