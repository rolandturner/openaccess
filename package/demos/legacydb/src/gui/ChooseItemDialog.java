
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

import model.Branch;
import model.Item;

import javax.swing.*;
import javax.jdo.Query;
import java.awt.*;
import java.util.Vector;
import java.util.Collection;

import util.JDOSupport;

/** 
 * Dialog to select an Item by searching on description.
 */
public class ChooseItemDialog extends DialogBase {

    private Branch branch;
    private Item item;

    private JTextField fieldDescr = new JTextField();
    private JList list = new JList();

    public ChooseItemDialog(DialogBase owner, Branch branch) {
        super(owner, "Choose Item", true);
        this.branch = branch;

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(new JLabel("Description"));
        fieldDescr.setActionCommand("search");
        fieldDescr.addActionListener(this);
        fieldDescr.setColumns(10);
        fieldPanel.add(fieldDescr);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("OK", "ok"));
        buttonPanel.add(createButton("Cancel", "cancel"));

        getContentPane().add(fieldPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(owner);
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
        String d = fieldDescr.getText();
        Query q = JDOSupport.getPM().newQuery(Item.class,
            "branch == b && description.startsWith(d)");
        q.setIgnoreCache(true);
        // Setting the standard JDO option "ignoreCache" to true prevents
        // uncomitted changes from the current transaction from being flushed
        // to the database before the query is run. This is important as
        // once a flush has been done a JDBC connection is pinned to the PM
        // and database locks are accumulated (much like a datastore tx).
        q.declareParameters("Branch b, String d");
        q.setOrdering("description ascending");
        try {
            list.setListData(new Vector((Collection)q.execute(branch, d)));
        } finally {
            q.closeAll();
        }
        list.setSelectedIndex(0);
    }

}

