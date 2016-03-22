
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
import tut2.model.Country;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is a gui panel used to display and edit Address's <p>
 * The load(PersistenceManager pm) method must be called to load all the Countries to display.
 * <p>
 */
public class AddressPanel extends JPanel {

    private JTextField street = new JTextField();
    private JTextField city = new JTextField();
    private JTextField code = new JTextField();
    private JComboBox country = new JComboBox();

    public AddressPanel() {
        setBorder(BorderFactory.createTitledBorder("Adress"));
        setLayout(new GridBagLayout());
        street.setColumns(15);
        city.setColumns(15);
        code.setColumns(5);
        add(new JLabel("Street", SwingConstants.RIGHT), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 2, 2, 2), 0, 0));
        add(street, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 2, 2), 0, 0));
        add(new JLabel("City", SwingConstants.RIGHT), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        add(city, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(new JLabel("Code", SwingConstants.RIGHT), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        add(code, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        add(new JLabel("Country", SwingConstants.RIGHT), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 2, 5, 2), 0, 0));
        add(country, new GridBagConstraints(1, 3, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 5, 2), 0, 0));
    }

    /**
     * Loads the Countries ordered by name from the persistence to be displayed in the JComboBox
     * @param pm the PersistenceManager to use to load country data
     * @throws Exception if the Country data could not be loaded
     */
    public void load(PersistenceManager pm) throws Exception {
        Query query = pm.newQuery(Country.class);
        query.setOrdering("name descending");
        List persons = null;
        try {
            persons = new ArrayList((Collection) query.execute());
        } finally {
            query.closeAll();
        }
        country.removeAllItems();
        for (Iterator it = persons.iterator(); it.hasNext();) {
            country.addItem(it.next());
        }
    }

    /**
     * Enables or disables all the components on this panel.
     * @param enabled true if the components should be enabled, false otherwise
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        street.setEnabled(enabled);
        city.setEnabled(enabled);
        code.setEnabled(enabled);
        country.setEnabled(enabled);
    }

    /**
     * Create a new Address object from the data in the display
     * @return the new Address
     */
    public Address getAddress() {
        Address address = new Address();
        address.setStreet(street.getText());
        address.setCity(city.getText());
        address.setCode(code.getText());
        address.setCountry((Country) country.getSelectedItem());
        return address;
    }

    /**
     * Sets the data in the display to reflect this address.
     * @param address the Address to display
     */
    public void setAddress(Address address) {
        if (address == null) {
            street.setText("");
            city.setText("");
            code.setText("");
            country.setSelectedItem(null);
            return;
        }
        street.setText(address.getStreet());
        city.setText(address.getCity());
        code.setText(address.getCode());
        country.setSelectedItem(address.getCountry());
    }
}

