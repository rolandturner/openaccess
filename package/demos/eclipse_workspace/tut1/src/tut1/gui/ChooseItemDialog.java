
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

import javax.swing.*;
import javax.jdo.Query;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 *
 */
public class ChooseItemDialog extends DialogBase {
    private Item item;

    private JTextField fieldDescription = new JTextField();
    private JList list = new JList();

    public ChooseItemDialog(DialogBase owner) {
        super(owner, "Choose Item", true);

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(new JLabel("Description"));
        fieldDescription.setActionCommand("search");
        fieldDescription.addActionListener(this);
        fieldDescription.setColumns(10);
        fieldPanel.add(fieldDescription);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("OK", "ok"));
        buttonPanel.add(createButton("Cancel", "cancel"));

        getContentPane().add(fieldPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(owner);
        search();
    }

    public Item getItem() {
        return item;
    }

    public void ok() {
        item = (Item)list.getSelectedValue();
        dispose();
    }

    public void cancel() {
        item = null;
        dispose();
    }

    public void search() {
        String d = fieldDescription.getText();
        Query q = JDOSupport.getPM().newQuery(Item.class,"description.startsWith(d)");
        q.setIgnoreCache(true);
        // Setting the standard JDO option "ignoreCache" to true prevents
        // uncomitted changes from the current transaction from being flushed
        // to the database before the query is run. This is important as
        // once a flush has been done a JDBC connection is pinned to the PM
        // and database locks are accumulated (much like a datastore tx).
        q.declareParameters("String d");
        q.setOrdering("description ascending");
        try {
            list.setListData(new Vector((Collection)q.execute(d)));
        } finally {
            q.closeAll();
        }
        list.setSelectedIndex(0);
    }
}

