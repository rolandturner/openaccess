
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
import tut1.model.Order;
import tut1.util.JDOSupport;

import javax.jdo.Query;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;


/**
 * GUI frame for a simple order entry app.
 *
 */
public class MainFrame extends JFrame implements ListSelectionListener {

    private JList orderList = new JList();
    private JList orderItemList = new JList();
    private JLabel orderItemTitle = new JLabel();

    public MainFrame() {
        Action createOrder = createMenuItem("Create new order", 'n', "Create a new order.", "createOrder");
        Action editOrder = createMenuItem("Edit order lines", 'E', "Edit the selected order's lines.", "editOrder");
        Action cancelOrder = createMenuItem("Cancel order", 'C', "Cancel the selected order.", "cancelOrder");
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.add(createMenuItem("Exit", 'x', "Close this Application", "fileExit"));
        bar.add(file);

        JMenu order = new JMenu("Order");
        order.setMnemonic('O');
        order.add(createOrder);
        order.add(editOrder);
        order.addSeparator();
        order.add(cancelOrder);
        bar.add(order);

        JMenu edit = new JMenu("Edit");
        edit.add(createMenuItem("Customers", (char) 0, "Modify Customer list", "viewCustomers"));
        edit.add(createMenuItem("Items", (char) 0, "Modify Items list", "viewItems"));
        edit.add(createMenuItem("Categories", (char) 0, "Modify Category list", "viewCategories"));
        bar.add(edit);

        setJMenuBar(bar);

        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshOrderData();
        orderList.addListSelectionListener(this);


        JPanel orderButtonPanel = new JPanel();
        orderButtonPanel.add(new JButton(createOrder));
        orderButtonPanel.add(new JButton(cancelOrder));

        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JLabel("Orders"), BorderLayout.NORTH);
        orderPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);
        orderPanel.add(orderButtonPanel, BorderLayout.SOUTH);

        JPanel itemsButtonPanel = new JPanel();
        itemsButtonPanel.add(new JButton(editOrder));

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.add(orderItemTitle, BorderLayout.NORTH);
        itemsPanel.add(new JScrollPane(orderItemList), BorderLayout.CENTER);
        itemsPanel.add(itemsButtonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(orderPanel);
        splitPane.setRightComponent(itemsPanel);

        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Selects all the orders and adds them to the list.
     * This method tries to maintain the current selection.
     */
    private void refreshOrderData() {
        JDOSupport.refresh();
        Object o = orderList.getSelectedValue();
        orderList.setListData(getAllOrders());
        if (o != null) {
            orderList.setSelectedValue(o, true);
        }
    }

    /**
     * A helper method to create a Action that will call a given method
     * on the class.
     *
     * @param text        The text to be displayed on the buttons and menu items
     * @param mnemonicKey The mnemonic key for this action
     * @param tooltip     The tooptip text to be used
     * @param method      The method to invoke on this object when the action is performed
     * @return An Action all set up for use on buttons and menu items.
     */
    private Action createMenuItem(String text, char mnemonicKey, String tooltip, final String method) {
        Action ans = new AbstractAction(text) {
            public void actionPerformed(ActionEvent e) {
                invoke(MainFrame.this, method);
            }
        };
        ans.putValue(Action.ACTION_COMMAND_KEY, method);
        ans.putValue(Action.NAME, text);
        ans.putValue(Action.SHORT_DESCRIPTION, tooltip);
        ans.putValue(Action.LONG_DESCRIPTION, tooltip);
        ans.putValue(Action.MNEMONIC_KEY, new Integer(mnemonicKey));
        return ans;
    }

    /**
     * Invoke methodName on target and handle any exceptions.
     */
    public void invoke(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName, (Class[])null);
            m.invoke(target, (Object[])null);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            }
            handleException(t);
        }
    }

    public void handleException(Throwable t) {
        t.printStackTrace(System.out);
        JDOSupport.rollback();
        JOptionPane.showMessageDialog(this, t.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Make sure we exit cleanly
     *
     * @param e The window event
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            fileExit();
        }
    }

    /**
     * Shuts down the JDO server if it was sarted localy and
     * exit the system.
     */
    public void fileExit() {
        JDOSupport.shutdown();
        System.exit(0);
    }

    /**
     * Create a new Order.
     * Prompt the user for Customer and add a new order for that Customer
     */
    public void createOrder() {
        ChooseCustomerDialog dlg = new ChooseCustomerDialog(this);
        dlg.setVisible(true);

        Customer customer = dlg.getCustomer();
        if (customer != null) {
            Order order = new Order(customer);
            JDOSupport.getPM().makePersistent(order);
            JDOSupport.commit();
            refreshOrderData();
            orderList.setSelectedValue(order, true);
            editOrder();
        }
    }

    /**
     * Delete an Order.
     */
    public void cancelOrder() {
        Order order = (Order) orderList.getSelectedValue();
        if (order != null) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel order:" +
                    "\n" + order + "?\n" +
                    "This will delete the order from the system.",
                    "Cancel Order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                JDOSupport.getPM().deletePersistent(order);
                JDOSupport.commit();
                refreshOrderData();
            }
        }
    }

    /**
     * Add, Edit and Delete Order lines
     */
    public void editOrder() {
        Order order = (Order) orderList.getSelectedValue();
        if (order != null) {
            ViewOrderLinesDialog dlg = new ViewOrderLinesDialog(this, order);
            dlg.setVisible(true);
            refreshOrderData();
        }
    }

    /**
     * Query the db for all Orders, ordered by orderDate and customer.name
     */
    private Vector getAllOrders() {
        Query q = JDOSupport.getPM().newQuery(Order.class);
        q.setOrdering("orderDate ascending, customer.name ascending");
        try {
            ArrayList a = new ArrayList((Collection) q.execute());
            return new Vector(a);
        } finally {
            q.closeAll();
        }
    }

    /**
     * Show this order's lines in the detail pane
     * and change the window title.
     *
     * @param order the order to use as current
     */
    private void setCurrentOrder(Order order) {
        setTitle(order == null ? "JDO Genie - tut1 DB and Swing Demo"
                : "Order for " + order);
        Vector items = new Vector();
        if (order != null) {
            setTitle("Order: " + order);
            items.addAll(order.getLines());
            orderItemTitle.setText("Order lines for: " + order);
        } else {
            setTitle("JDO Genie - tut1 DB and Swing Demo");
            orderItemTitle.setText("Order lines");
        }
        orderItemList.setListData(items);
    }

    /**
     * Opens a dialog to view and edit all the Items in the db.
     */
    public void viewItems() {
        ViewItemsDialog dlg = new ViewItemsDialog(this);
        dlg.setVisible(true);
    }

    /**
     * Opens a dialog to view and edit all the Customers in the db.
     */
    public void viewCustomers() {
        ViewCustomersDialog dlg = new ViewCustomersDialog(this);
        dlg.setVisible(true);
    }

    /**
     * Opens a dialog to view and edit all the Categories in the db.
     */
    public void viewCategories() {
        ViewCategoriesDialog dlg = new ViewCategoriesDialog(this);
        dlg.setVisible(true);
    }

    // ListSelectionListener impl ==============================================
    public void valueChanged(ListSelectionEvent e) {
        setCurrentOrder((Order) orderList.getSelectedValue());
    }
}

