
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
import javax.jdo.JDOHelper;
import java.awt.*;

/**
 * Dialog for adding new Customers to the system or editing of existing Customers.
 * Uses JDOHelper.isPersistent(Object) to check whether a Customer is new or existing.
 *
 */
public class AddEditCustomerDialog extends DialogBase {

    private Customer customer;
    private JTextField fieldName = new JTextField();
    private JTextField fieldCode = new JTextField();

    public AddEditCustomerDialog(DialogBase owner,Customer customer) {
        /*
         Set our title depending on whether we are working
         with a new customer or editing an existing one.
         */
        super(owner,JDOHelper.isPersistent(customer) ? "Edit Customer: "+customer : "Add Customer", true);
        JDOSupport.commit();
        this.customer = customer;

        fieldName.setColumns(10);
        fieldName.setText(customer.getName());
        fieldCode.setColumns(10);
        fieldCode.setText(customer.getCode());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(createButton("OK", "ok"),new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        buttonPanel.add(createButton("Cancel", "cancel"),new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());
        fieldPanel.add(new JLabel("Name"),new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldName,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(new JLabel("Code"),new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldCode,new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(buttonPanel,new GridBagConstraints(0,2,2,1,1,1,GridBagConstraints.SOUTH,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));

        getContentPane().add(fieldPanel, BorderLayout.CENTER);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void ok() {
        if (fieldName.getText() == null || fieldCode.getText() == null) return;
        customer.setName(fieldName.getText());//@todo Only set if has been changed
        customer.setCode(fieldCode.getText());
        /*
        Check if the customer is a new 1 or an existing 1.
        If the customer is a new 1, make it persitant.
        */
        if (!JDOHelper.isPersistent(customer)) {
            JDOSupport.getPM().makePersistent(customer);
        }
        //Save our changes made...
        JDOSupport.commit();
        dispose();
    }

    public void cancel() {
        //Discard any changes made...
        JDOSupport.rollback();
        customer = null;
        dispose();
    }

}

