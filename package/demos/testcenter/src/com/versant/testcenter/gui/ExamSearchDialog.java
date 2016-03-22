
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
package com.versant.testcenter.gui;

import com.versant.testcenter.service.TestCenterService;
import com.versant.testcenter.model.Exam;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Search for Exam's by name.
 *
 */
public class ExamSearchDialog extends DialogBase {

    private JTextField nameField = new JTextField(10);
    private JList list = new JList();
    private boolean ok;

    public ExamSearchDialog(MainFrame owner) {
        super(owner, "Select Exams", true);

        nameField.setActionCommand("search");
        nameField.addActionListener(this);

        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("Exam Name"));
        namePanel.add(nameField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton(" OK ", "ok"));
        buttonPanel.add(createButton(" Cancel ", "dispose"));

        getContentPane().add(namePanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(owner);
    }

    public void search() {
        Collection c = TestCenterService.findExamsByName(nameField.getText());
        list.setListData(c.toArray());
    }

    public void ok() {
        ok = true;
        dispose();
    }

    public boolean isOk() {
        return ok;
    }

    public Object[] getSelectedExams() {
        return list.getSelectedValues();
    }

}

