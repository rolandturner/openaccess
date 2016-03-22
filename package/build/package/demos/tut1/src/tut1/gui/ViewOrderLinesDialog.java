
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

import tut1.model.Order;
import tut1.model.OrderLine;
import tut1.util.JDOSupport;

import javax.jdo.Query;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 */
public class ViewOrderLinesDialog extends DialogBase {
    private Order order;
    private JList list = new JList();

    public ViewOrderLinesDialog(MainFrame owner, Order order) {
        super(owner, "Order Lines for " + order, true);
        this.order = order;

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addOrderLine"));
        buttonPanel.add(createButton("Edit", "editOrderLine"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        Query q = JDOSupport.getPM().newQuery(OrderLine.class, "order == o");
        q.declareParameters("Order o");
        q.setOrdering("item.code ascending");
        try {
            list.setListData(new Vector((Collection) q.execute(order)));
        } finally {
            q.closeAll();
        }
    }

    public void addOrderLine() {
        AddEditOrderLineDialog dlg = new AddEditOrderLineDialog(this, order, new OrderLine());
        dlg.setVisible(true);
        if (dlg.getOrderLine() == null) return;
        refresh();
    }

    public void editOrderLine() {
        OrderLine orderLine = (OrderLine) list.getSelectedValue();
        if (orderLine == null) return;
        AddEditOrderLineDialog dlg = new AddEditOrderLineDialog(this, orderLine.getOrder(), orderLine);
        dlg.setVisible(true);
        if (dlg.getOrderLine() == null) return;
        refresh();
    }

    public void close() {
        dispose();
    }
}

