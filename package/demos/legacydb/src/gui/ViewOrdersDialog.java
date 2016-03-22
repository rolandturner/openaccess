
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
package gui;

import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.text.SimpleDateFormat;

import util.JDOSupport;

/**
 * Show all the orders for the current branch and allow add/edit.
 */
public class ViewOrdersDialog extends DialogBase {

    private Branch branch;
    private DefaultTableModel tm = new DefaultTableModel() {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ArrayList orders;
    private JTable table;

    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yy");

    private static final String[] COLS = new String[]{
        "Order No", "Order Date", "Supplier"};

    public ViewOrdersDialog(MainFrame owner, Branch branch) {
        super(owner, "Orders for " + branch, true);
        this.branch = branch;

        refresh();

        table = new JTable(tm);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addOrder"));
        buttonPanel.add(createButton("Edit", "editOrder"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        refreshImp();
    }

    private void refreshImp() {
        orders = getOrders();
        int n = orders.size();
        Object[][] data = new Object[n][];
        for (int i = 0; i < n; i++) {
            Order o = (Order)orders.get(i);
            data[i] = new Object[]{
                Integer.toString(o.getOrderNo()),
                DATE_FORMAT.format(o.getOrderDate()),
                o.getSupplier()
            };
        }
        tm.setDataVector(data, COLS);
    }

    private ArrayList getOrders() {
        Query q = JDOSupport.getPM().newQuery(Order.class, "branch == b");
        q.declareParameters("Branch b");
        q.setOrdering("orderDate descending");
        try {
            return new ArrayList((Collection)q.execute(branch));
        } finally {
            q.closeAll();
        }
    }

    public void addOrder() {
        ChooseSupplierDialog csd = new ChooseSupplierDialog(this, branch);
        csd.setVisible(true);
        if (csd.getSupplier() == null) return;

        String orderNo;
        if (orders.isEmpty()) orderNo = "1";
        else orderNo = Integer.toString(((Order)orders.get(0)).getOrderNo() + 1);
        orderNo = showInputDialog("Enter new Order No: ", orderNo);
        if (orderNo == null) return;

        // create the order before the user fills in the lines to trap
        // duplicate orderNo's early
        Order o = new Order(branch, Integer.parseInt(orderNo));
        o.setSupplier(csd.getSupplier());
        JDOSupport.getPM().makePersistent(o);
        JDOSupport.commit();

        editOrder(o);
    }

    public void editOrder() {
        int i = table.getSelectedRow();
        if (i < 0) return;
        editOrder((Order)orders.get(i));
    }

    private void editOrder(Order o) {
        EditOrderDialog dlg = new EditOrderDialog(this, o);
        dlg.setVisible(true);
        if (dlg.isOk()) JDOSupport.commit();
        else JDOSupport.rollback();
        refreshImp();
    }

    public void close() {
        dispose();
    }

}

