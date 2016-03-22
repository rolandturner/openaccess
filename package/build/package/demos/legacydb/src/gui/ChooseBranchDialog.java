
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

import javax.swing.*;
import javax.jdo.Query;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import util.JDOSupport;

/** 
 * Ask the user to select a Branch.
 */
public class ChooseBranchDialog extends DialogBase {

    private Branch branch;
    private JList list;

    public ChooseBranchDialog(MainFrame owner, Branch branch) {
        super(owner, "Choose Branch", true);

        list = new JList(getBranches());
        if (branch != null) list.setSelectedValue(branch, true);
        else list.setSelectedIndex(0);

        JButton ok = createButton(" OK ", "ok");
        ok.registerKeyboardAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ok);

        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(owner);
    }

    public void ok() {
        branch = (Branch)list.getSelectedValue();
        dispose();
    }

    /**
     * Get the selected branch.
     */
    public Branch getBranch() {
        return branch;
    }

    private Object[] getBranches() {
        Query q = JDOSupport.getPM().newQuery(Branch.class);
        q.setOrdering("name ascending");
        try {
            ArrayList a = new ArrayList((Collection)q.execute());
            return a.toArray();
        } finally {
            q.closeAll();
        }
    }

}

