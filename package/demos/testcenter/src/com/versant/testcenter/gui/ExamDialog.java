
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

import com.versant.testcenter.model.ExamCategory;
import com.versant.testcenter.service.TestCenterService;

import javax.swing.*;
import java.awt.*;

/**
 * Get details for a new exam.
 *
 */
public class ExamDialog extends DialogBase {

    private JTextField fieldName = new JTextField(10);
    private JTextField fieldDescription = new JTextField(20);
    private JComboBox chooseCategory = new JComboBox(
            TestCenterService.findAllExamCategories().toArray());
    private boolean ok;

    public ExamDialog(MainFrame owner) {
        super(owner, "Add Exam", true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton(" OK ", "ok"));
        buttonPanel.add(createButton(" Cancel ", "cancel"));

        JPanel fp = new JPanel(new GridBagLayout());
        addField(fp, 0, "Name", fieldName);
        addField(fp, 1, "Description", fieldDescription);
        addField(fp, 2, "Category", chooseCategory);

        getContentPane().add(fp, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public void ok() {
        if (getExamName().length() == 0 || getDescription().length() == 0
                || getExamCategory() == null) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }
        ok = true;
        dispose();
    }

    public void cancel() {
        dispose();
    }

    public boolean isOk() {
        return ok;
    }

    public String getExamName() {
        return fieldName.getText();
    }

    public String getDescription() {
        return fieldDescription.getText();
    }

    public ExamCategory getExamCategory() {
        return (ExamCategory)chooseCategory.getSelectedItem();
    }

}


