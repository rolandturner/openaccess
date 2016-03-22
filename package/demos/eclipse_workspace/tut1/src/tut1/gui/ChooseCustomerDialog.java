
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
package tut1.gui;

import tut1.model.Customer;
import tut1.util.JDOSupport;

import javax.swing.*;
import javax.jdo.Query;
import java.awt.*;
import java.util.Vector;
import java.util.Collection;

/**
 *
 */
public class ChooseCustomerDialog extends DialogBase {
    private Customer customer;

    private JTextField fieldName = new JTextField();
    private JList list = new JList();

    public ChooseCustomerDialog(MainFrame owner) {
        super(owner, "Create order for Customer", true);

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(new JLabel("Name"));
        fieldName.setActionCommand("search");
        fieldName.addActionListener(this);
        fieldName.setColumns(10);
        fieldPanel.add(fieldName);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addCustomer"));
        buttonPanel.add(createButton("Edit", "editCustomer"));
        buttonPanel.add(createButton("OK", "ok"));
        buttonPanel.add(createButton("Cancel", "cancel"));

        getContentPane().add(fieldPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(owner);
        search();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void ok() {
        customer = (Customer)list.getSelectedValue();
        dispose();
    }

    public void cancel() {
        customer = null;
        dispose();
    }

    public void addCustomer() {
        AddEditCustomerDialog dlg = new AddEditCustomerDialog(this,new Customer());
        dlg.setVisible(true);
        if (dlg.getCustomer() == null) return;
        customer = dlg.getCustomer();
        dispose();
    }

    public void editCustomer() {
        customer = (Customer)list.getSelectedValue();
        if (customer == null) return;
        AddEditCustomerDialog dlg = new AddEditCustomerDialog(this,customer);
        dlg.setVisible(true);
        if (dlg.getCustomer() == null) return;
        search();
    }

    public void search() {
        String n = fieldName.getText();
        Query q = JDOSupport.getPM().newQuery(Customer.class,"name.startsWith(n)");
        q.setIgnoreCache(true);
        // Setting the standard JDO option "ignoreCache" to true prevents
        // uncomitted changes from the current transaction from being flushed
        // to the database before the query is run. This is important as
        // once a flush has been done a JDBC connection is pinned to the PM
        // and database locks are accumulated (much like a datastore tx).
        q.declareParameters("String n");
        q.setOrdering("name ascending");
        try {
            list.setListData(new Vector((Collection)q.execute(n)));
        } finally {
            q.closeAll();
        }
        list.setSelectedIndex(0);
    }
}
