
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

import model.Order;
import model.OrderLine;
import model.Item;
import model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.jdo.JDOHelper;
import java.util.List;
import java.awt.*;

/**
 * Edit the fields and lines of an order.
 */
public class EditOrderDialog extends DialogBase {

    private Order order;
    private boolean ok;

    private JLabel orderInfo = new JLabel();
    private DefaultTableModel lineModel = new DefaultTableModel() {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable lineTable = new JTable(lineModel);

    private static final String[] COLS = new String[]{
        "Line", "Item Code", "Description", "Quantity"};

    public EditOrderDialog(DialogBase owner, Order order) {
        super(owner, JDOHelper.isNew(order) ? "Add Order" : "Edit Order", true);
        this.order = order;

        updateFromOrder();

        orderInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addLine"));
        buttonPanel.add(createButton("Item", "changeItem"));
        buttonPanel.add(createButton("Qty", "changeQty"));
        buttonPanel.add(createButton("Delete", "deleteLine"));
        buttonPanel.add(createButton("Supplier", "changeSupplier"));
        buttonPanel.add(createButton("OK", "ok"));
        buttonPanel.add(createButton("Cancel", "cancel"));

        getContentPane().add(orderInfo, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(lineTable), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(550, 420);
        setLocationRelativeTo(owner);
    }

    /**
     * Fill in our widgets to match current order.
     */
    private void updateFromOrder() {
        orderInfo.setText(
            "Order No: " + order.getOrderNo() +
            "  Date: " +
                ViewOrdersDialog.DATE_FORMAT.format(order.getOrderDate()) +
            "  Supplier: " + order.getSupplier());
        List a = order.getLines();
        int n = a.size();
        Object[][] data = new Object[n][];
        for (int i = 0; i < n; i++) {
            OrderLine line = (OrderLine)a.get(i);
            data[i] = new Object[]{
                Integer.toString(line.getLineNo()),
                line.getItem().getItemCode(),
                line.getItem().getDescription(),
                Integer.toString(line.getQty())
            };
        }
        lineModel.setDataVector(data, COLS);
        if (n > 0) lineTable.getSelectionModel().setLeadSelectionIndex(0);
    }

    public boolean isOk() {
        return ok;
    }

    public void ok() {
        ok = true;
        dispose();
    }

    public void cancel() {
        dispose();
    }

    public void addLine() {
        ChooseItemDialog dlg = new ChooseItemDialog(this, order.getBranch());
        dlg.setVisible(true);
        Item item = dlg.getItem();
        if (item != null) {
            order.addOrderLine(item, 1);
            updateFromOrder();
        }
    }

    public void changeItem() {
        OrderLine line = getSelectedLine();
        if (line == null) return;
        ChooseItemDialog dlg = new ChooseItemDialog(this, order.getBranch());
        dlg.setVisible(true);
        Item item = dlg.getItem();
        if (item != null) line.setItem(item);
    }

    private OrderLine getSelectedLine() {
        int i = lineTable.getSelectedRow();
        if (i < 0) return null;
        return (OrderLine)order.getLines().get(i);
    }

    public void changeQty() {
        OrderLine line = getSelectedLine();
        if (line == null) return;
        String s = showInputDialog("Enter Qty: ", new Integer(line.getQty()));
        if (s != null) {
            line.setQty(Integer.parseInt(s));
            updateFromOrder();
        }
    }

    public void deleteLine() {
        OrderLine line = getSelectedLine();
        if (line == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete Line: " + line + " ?")
                == JOptionPane.YES_OPTION) {
            line.getOrder().removeOrderLine(line);
            updateFromOrder();
        }
    }

    public void changeSupplier() {
        ChooseSupplierDialog dlg = new ChooseSupplierDialog(this, order.getBranch());
        dlg.setVisible(true);
        Supplier s = dlg.getSupplier();
        if (s != null) {
            order.setSupplier(s);
            updateFromOrder();
        }
    }

}

