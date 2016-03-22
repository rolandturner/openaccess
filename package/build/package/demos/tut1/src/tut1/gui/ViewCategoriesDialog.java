
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

import tut1.model.Category;
import tut1.util.JDOSupport;

import javax.jdo.Query;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 */
public class ViewCategoriesDialog extends DialogBase {
    private JList list = new JList();

    public ViewCategoriesDialog(MainFrame owner) {
        super(owner, "Categories", true);

        refresh();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Add", "addCategory"));
        buttonPanel.add(createButton("Edit", "changeName"));
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Close", "close"));

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    public void refresh() {
        JDOSupport.refresh();
        Query q = JDOSupport.getPM().newQuery(Category.class);
        q.setOrdering("name ascending");
        try {
            list.setListData(new Vector((Collection) q.execute()));
        } finally {
            q.closeAll();
        }
    }

    public void addCategory() {
        String name = showInputDialog("Enter new Category name: ", "");
        if (name == null) return;
        JDOSupport.getPM().makePersistent(new Category(name));
        JDOSupport.commit();
        refresh();
    }

    public void changeName() {
        Category c = (Category) list.getSelectedValue();
        if (c == null) return;
        String name = showInputDialog("Enter new name: ", c.getName());
        if (name != null) {
            c.setName(name);
            JDOSupport.commit();
            refresh();
        }
    }

    public void close() {
        dispose();
    }
}

