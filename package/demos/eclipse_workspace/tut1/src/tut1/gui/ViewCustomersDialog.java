
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

import javax.jdo.Query;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 */
public class ViewCustomersDialog extends DialogBase {
    private JList list = new JList();

    public ViewCustomersDialog(MainFrame owner) {
        super(owner, "Customers", true);

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addCustomer"));
        buttonPanel.add(createButton("Edit", "editCustomer"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        Query q = JDOSupport.getPM().newQuery(Customer.class);
        q.setOrdering("name ascending");
        try {
            list.setListData(new Vector((Collection) q.execute()));
        } finally {
            q.closeAll();
        }
    }

    public void addCustomer() {
        AddEditCustomerDialog dlg = new AddEditCustomerDialog(this, new Customer());
        dlg.setVisible(true);
        if (dlg.getCustomer() == null) return;
        refresh();
    }

    public void editCustomer() {
        Customer customer = (Customer) list.getSelectedValue();
        if (customer == null) return;
        AddEditCustomerDialog dlg = new AddEditCustomerDialog(this, customer);
        dlg.setVisible(true);
        if (dlg.getCustomer() == null) return;
        refresh();
    }

    public void close() {
        dispose();
    }

}

