
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
package com.versant.core.jdo.tools.workbench.forms;

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.PropertySaver;
import za.co.hemtech.gui.exp.ExpTableModel;

import javax.swing.*;
import java.util.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 */
public class ProjectFillModePreviewForm extends WorkbenchPanel {

    private ExpTableModel model = new ExpTableModel("model");
    private MdProject project;
    private JTextArea prText = new JTextArea();
    private JTextArea wbText = new JTextArea();
    private JPanel previewTitle = new JPanel(new GridLayout(1, 2));
    private JPanel previewContent = new JPanel(new GridLayout(1, 2));
    private JCheckBox splitBox = new JCheckBox("Split runtime and workbench " +
            "settings into two files (.properties and .workbench).");
    private JRadioButton rbMin = new JRadioButton("<html><b>Minimal:</b><br>" +
            "Properties that are set to their default values are removed " +
            "from the file. " +
            "Comments and properties not recognized by the Workbench are " +
            "left as is.</html>");
    private JRadioButton rbKeep = new JRadioButton("<html><b>Preserve file as is (default):</b><br>" +
            "The workbench will not remove anything from the file. " +
            "Comments, unrecognized properties and properties in the file " +
            "with default values are preserved as is.</html>");
    private JRadioButton rbVerbose = new JRadioButton("<html><b>Verbose:</b><br>" +
            "The workbench will write all possible properties to the file. " +
            "Comments and unrecognized properties are preserved as is.</html>");

    public ProjectFillModePreviewForm(final MdProject project)
            throws Exception {
        super("main");
        setPreferredSize(new Dimension(800, 600));
        prText.setEditable(false);
        wbText.setEditable(false);
        model.add(this);
        splitBox.setName("splitBox");
        add(splitBox);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(
                BorderFactory.createTitledBorder("Project write mode."));
        panel.setName("rbPanel");
        panel.add(rbMin, BorderLayout.NORTH);
        panel.add(rbKeep, BorderLayout.CENTER);
        panel.add(rbVerbose, BorderLayout.SOUTH);
        add(panel);
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Preview"));
        panel.setName("previewPanel");
        panel.add(previewTitle, BorderLayout.NORTH);
        panel.add(previewContent, BorderLayout.CENTER);
        add(panel);
        setProject(project);
        splitBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                project.setSplitPropFile(splitBox.isSelected());
                redoPreview();
            }
        });
        rbMin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                project.setPropFillMode(PropertySaver.PROP_FILL_MODE_MIN);
                redoPreview();
            }
        });
        rbKeep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                project.setPropFillMode(PropertySaver.PROP_FILL_MODE_KEEP);
                redoPreview();
            }
        });
        rbVerbose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                project.setPropFillMode(PropertySaver.PROP_FILL_MODE_VERBOSE);
                redoPreview();
            }
        });

    }

    public void setProject(MdProject project) {
        this.project = project;
        redoPreview();
    }

    public void redoPreview() {
        boolean splitPropFile = project.isSplitPropFile();
        splitBox.setSelected(splitPropFile);
        int propFillMode = project.getPropFillMode();
        rbMin.setSelected(propFillMode == PropertySaver.PROP_FILL_MODE_MIN);
        rbKeep.setSelected(propFillMode == PropertySaver.PROP_FILL_MODE_KEEP);
        rbVerbose.setSelected(
                propFillMode == PropertySaver.PROP_FILL_MODE_VERBOSE);
        previewTitle.removeAll();
        previewTitle.add(new JLabel(".properties"));
        previewContent.removeAll();
        previewContent.add(new JScrollPane(prText));
        java.util.List filePreviewText = null;
        try {
            filePreviewText = project.getFilePreviewText();
        } catch (Exception e) {
            filePreviewText = new ArrayList(2);
            filePreviewText.add("Error");
            filePreviewText.add("Error");
            e.printStackTrace(System.out);
        }
        prText.setText((String)filePreviewText.get(0));
        prText.setCaretPosition(0);
        if (splitPropFile) {
            previewTitle.add(new JLabel(".workbench"));
            previewContent.add(new JScrollPane(wbText));
            wbText.setText((String)filePreviewText.get(1));
            wbText.setCaretPosition(0);
        }
        validate();
        repaint();
    }
}
