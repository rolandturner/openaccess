
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

import tut1.model.Item;
import tut1.util.JDOSupport;

import javax.jdo.Query;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 */
public class ViewItemsDialog extends DialogBase {
    private JList list = new JList();

    public ViewItemsDialog(MainFrame owner) {
        super(owner, "Items", true);

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addItem"));
        buttonPanel.add(createButton("Edit", "editItem"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        Query q = JDOSupport.getPM().newQuery(Item.class);
        q.setOrdering("description ascending");
        try {
            list.setListData(new Vector((Collection) q.execute()));
        } finally {
            q.closeAll();
        }
    }

    public void addItem() {
        AddEditItemDialog dlg = new AddEditItemDialog(this, new Item());
        dlg.setVisible(true);
        if (dlg.getItem() == null) return;
        refresh();
    }

    public void editItem() {
        Item item = (Item) list.getSelectedValue();
        if (item == null) return;
        AddEditItemDialog dlg = new AddEditItemDialog(this, item);
        dlg.setVisible(true);
        if (dlg.getItem() == null) return;
        refresh();
    }

    public void close() {
        dispose();
    }
}

