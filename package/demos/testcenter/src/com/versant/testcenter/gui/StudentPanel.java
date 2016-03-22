
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

import com.versant.testcenter.model.Student;
import com.versant.testcenter.model.Exam;
import com.versant.testcenter.service.Context;
import com.versant.testcenter.service.TestCenterService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Panel to allow the currently logged on student to maintain exam
 * registrations.
 *
 */
public class StudentPanel extends PanelBase {

    private Student student;
    private JList examList = new JList();

    public StudentPanel(MainFrame owner) {
        super(owner);
        student = (Student)Context.getContext().getCurrentUser();

        examList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Refresh", "refresh"));
        buttonPanel.add(createButton("Deregister", "deregister"));
        buttonPanel.add(createButton("Register", "register"));

        add(new JLabel("Registered Exams"), BorderLayout.NORTH);
        add(new JScrollPane(examList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        Context.getContext().getPersistenceManager().refresh(student);
        examList.setListData(student.getExams().toArray());
    }

    public void deregister() {
        Object[] a = examList.getSelectedValues();
        if (a.length == 0) return;
        TestCenterService.beginTxn();
        student.removeAllExams(Arrays.asList(a));
        TestCenterService.commitTxn();
        refresh();
    }

    public void register() {
        ExamSearchDialog dlg = new ExamSearchDialog(mainFrame);
        dlg.setVisible(true);
        if (!dlg.isOk()) return;
        Object[] a = dlg.getSelectedExams();
        if (a.length == 0) return;
        TestCenterService.beginTxn();
        for (int i = 0; i < a.length; i++) student.addExam((Exam)a[i]);
        TestCenterService.commitTxn();
        refresh();
    }

}

