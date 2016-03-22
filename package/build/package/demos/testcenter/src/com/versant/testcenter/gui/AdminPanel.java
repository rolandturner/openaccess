
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
 * Panel for admin of exams.
 *
 */
public class AdminPanel extends PanelBase {

    private JList examList = new JList();
    private JTextField nameField = new JTextField(10);

    public AdminPanel(MainFrame owner) {
        super(owner);

        nameField.setActionCommand("search");
        nameField.addActionListener(this);

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(new JLabel("Search by name"));
        fieldPanel.add(nameField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Search", "search"));
        buttonPanel.add(createButton("Add Exam", "addExam"));

        add(fieldPanel, BorderLayout.NORTH);
        add(new JScrollPane(examList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addExam() {
        ExamDialog dlg = new ExamDialog(mainFrame);
        dlg.setVisible(true);
        if (!dlg.isOk()) return;
        TestCenterService.beginTxn();
        Exam e = TestCenterService.createExam();
        e.setName(dlg.getExamName());
        e.setDescription(dlg.getDescription());
        e.setExamCategory(dlg.getExamCategory());
        TestCenterService.commitTxn();
    }

    public void search() {
        Collection c = TestCenterService.findExamsByName(nameField.getText());
        examList.setListData(c.toArray());
    }

}

