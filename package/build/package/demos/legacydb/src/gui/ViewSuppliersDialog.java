
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
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

import util.JDOSupport;

/**
 * Show all the suppliers for the current branch and allow add/edit.
 */
public class ViewSuppliersDialog extends DialogBase {

    private Branch branch;
    private JList list = new JList();

    public ViewSuppliersDialog(MainFrame owner, Branch branch) {
        super(owner, "Suppliers for " + branch, true);
        this.branch = branch;

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addSupplier"));
        buttonPanel.add(createButton("Name", "changeName"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        refreshImp();
    }

    private void refreshImp() {
        Query q = JDOSupport.getPM().newQuery(Supplier.class, "branch == b");
        q.declareParameters("Branch b");
        q.setOrdering("name ascending");
        try {
            list.setListData(new Vector((Collection)q.execute(branch)));
        } finally {
            q.closeAll();
        }
    }

    public void addSupplier() {
        String no = showInputDialog("Enter new Supplier No: ", "");
        if (no == null) return;
        Supplier s = new Supplier(branch, Integer.parseInt(no), "Supplier" + no);
        JDOSupport.getPM().makePersistent(s);
        JDOSupport.commit();
        refreshImp();
    }

    public void changeName() {
        Supplier s = (Supplier)list.getSelectedValue();
        if (s == null) return;
        String name = showInputDialog("Enter new name: ", s.getName());
        if (name != null) {
            s.setName(name);
            JDOSupport.commit();
        }
        refreshImp();
    }

    public void close() {
        dispose();
    }

}

