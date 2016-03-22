
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
import java.util.Iterator;

import util.JDOSupport;

/**
 * Show all the items for the current branch and allow add/edit.
 */
public class ViewItemsDialog extends DialogBase {

    private Branch branch;
    private JList list = new JList();

    public ViewItemsDialog(MainFrame owner, Branch branch) {
        super(owner, "Items for " + branch, true);
        this.branch = branch;

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addItem"));
        buttonPanel.add(createButton("Description", "changeDescription"));
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
        Query q = JDOSupport.getPM().newQuery(Item.class, "branch == b");
        q.declareParameters("Branch b");
        q.setOrdering("itemCode ascending");
        try {
            list.setListData(new Vector((Collection)q.execute(branch)));
        } finally {
            q.closeAll();
        }
    }

    public void addItem() {
        String code = showInputDialog("Enter new item code: ", "");
        if (code == null) return;
        JDOSupport.getPM().makePersistent(new Item(branch, code, "Item" + code));
        JDOSupport.commit();
        refreshImp();
    }

    public void changeDescription() {
        Item i = (Item)list.getSelectedValue();
        if (i == null) return;
        String des = showInputDialog("Enter new description: ",
                i.getDescription());
        if (des != null) {
            i.setDescription(des);
            JDOSupport.commit();
            refreshImp();
        }
    }

    public void close() {
        dispose();
    }

}

