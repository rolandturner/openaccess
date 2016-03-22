
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

import tut1.model.OrderLine;
import tut1.model.Order;
import tut1.util.JDOSupport;

import javax.swing.*;
import javax.jdo.JDOHelper;
import java.awt.*;

/**
 * Dialog for adding new OrderLines to the system or editing of existing OrderLines.
 * Uses JDOHelper.isPersistent(Object) to check whether an OrderLine is new or existing.
 *
 */
public class AddEditOrderLineDialog extends DialogBase {
    private Order order;
    private OrderLine orderLine;
    private JTextField fieldItem = new JTextField();
    private JTextField fieldQuantity = new JTextField();

    public AddEditOrderLineDialog(DialogBase owner,Order order,OrderLine orderLine) {
        /*
         Set our title depending on whether we are working
         with a new OrderLine or editing an existing one.
         */
        super(owner,JDOHelper.isPersistent(orderLine) ? "Edit Order Line: "+orderLine: "Add Order Line", true);
        this.order = order;
        this.orderLine = orderLine;

        JPanel fieldPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());

        refreshItemField();
        fieldItem.setColumns(30);
        fieldItem.setEditable(false);

        fieldQuantity.setColumns(10);
        fieldQuantity.setText(""+orderLine.getQty());

        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(createButton("OK", "ok"),new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        buttonPanel.add(createButton("Cancel", "cancel"),new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(new JLabel("Item"),new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldItem,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(createButton("Change", "changeItem"),new GridBagConstraints(2,0,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(new JLabel("Quantity"),new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldQuantity,new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        fieldPanel.add(buttonPanel,new GridBagConstraints(0,2,3,1,1,1,GridBagConstraints.SOUTH,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        getContentPane().add(fieldPanel, BorderLayout.CENTER);

        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    private void refreshItemField() {
        fieldItem.setText(orderLine.getItem() == null ? "<ITEM NOT SET>" :orderLine.getItem().toString());
    }

    public OrderLine getOrderLine() {
        return orderLine;
    }

    public void changeItem() {
        ChooseItemDialog dlg = new ChooseItemDialog(this);
        dlg.setVisible(true);
        orderLine.setItem(dlg.getItem());
        refreshItemField();
    }

    public void ok() {
        if (orderLine.getItem() == null ||
           fieldQuantity.getText() == null && fieldQuantity.getText().length() <= 0) {
            return;
        }
        try {
            orderLine.setQty(new Integer(fieldQuantity.getText()).intValue());
        }catch (Exception e) {
            return;
            //@todo: Some error handling...
        }

        /*
        Check if the OrderLine is a new 1 or an existing 1.
        If the OrderLine is a new 1, add it to our Order.
        (This will make the OrderLine persistant when we commit)
        */
        if (!JDOHelper.isPersistent(orderLine)) {
            order.addLine(orderLine);
        }
        //Save our changes made...
        JDOSupport.commit();
        dispose();
    }

    public void cancel() {
        //Discard any changes made...
        JDOSupport.rollback();
        orderLine = null;
        dispose();
    }
}

