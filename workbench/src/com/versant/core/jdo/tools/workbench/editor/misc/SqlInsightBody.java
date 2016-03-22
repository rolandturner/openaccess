
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
package com.versant.core.jdo.tools.workbench.editor.misc;

import za.co.hemtech.gui.FormPanel;
import za.co.hemtech.gui.DecoratedGrid;
import za.co.hemtech.gui.model.ColumnModelListener;
import com.versant.core.jdo.tools.workbench.jdoql.insight.QExpTableModel;
import com.versant.core.jdo.tools.workbench.model.DatabaseMetaData;
import za.co.hemtech.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @keep-all
 */
public class SqlInsightBody extends FormPanel {

    private JLabel lblHeader = new JLabel();
    private DecoratedGrid grid = new DecoratedGrid("grid");
    private QExpTableModel model = new QExpTableModel("model");

    public SqlInsightBody() throws Exception {
        super("main");
        setConfig(Config.getInstance(getClass()));
        init();
    }

    public void init() throws Exception {
        model.setConfig(getConfig());
        setModel(model);
        setBorder(BorderFactory.createEmptyBorder());
        grid.setModel(model);

        grid.setSelectRow(true);
        grid.setMultiSelect(false);
        grid.setHorizontalLines(false);
        grid.setVerticalLines(false);
        grid.setReadOnly(true);
        grid.setRequestFocusEnabled(false);
        grid.setFont(Font.decode("DialogInput-Plain-12"));
        grid.setBorder(BorderFactory.createEtchedBorder());


        lblHeader.setName("lblHeader");
        lblHeader.setFont(new Font("DialogInput", 1, 12));
        lblHeader.setForeground(Color.black);
        lblHeader.setOpaque(true);
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeader.setHorizontalTextPosition(SwingConstants.CENTER);
        lblHeader.setText("Header goes here");
        lblHeader.setRequestFocusEnabled(false);
        lblHeader.setBorder(BorderFactory.createEmptyBorder());



        this.add(lblHeader);
        this.add(grid, null);

        setRequestFocusEnabled(false);
    }

    public DecoratedGrid getGrid(){
        return grid;
    }

    /**
     * Set the data for the popup to display.
     */
    public void setListData(Object[] data) {
        model.clear();
        for (int i = 0; i < data.length; i++) {
            model.add(data[i]);
        }
        model.setColumnWidth(0, DatabaseMetaData.FieldDisplay.BIGGEST_LENGHT + 8);
        if (data.length < 12) {
            grid.setPreferredRows(data.length);

        } else {
            grid.setPreferredRows(12);
        }
        grid.validate();
    }

    /**
     * Set the header (table name, etc.)
     */
    public void setHeader(String header) {
        lblHeader.setText(header);
    }

    /**
     * Get the current header text.
     */
    public String getHeader() {
        return lblHeader.getText();
    }

    /**
     * Set the index of the selected object in our data list.
     */
    public void setSelectedIndex(int index) {
        grid.setSelectedIndex(index);
    }

    /**
     * Get the index of the selected object in our data list.
     */
    public int getSelectedIndex() {
        return grid.getSelectedIndex();
    }

    /**
     * Move the selected value in the list up by one.
     */
    public void moveUp() {
        if (getSelectedIndex() < 0) {
            setSelectedIndex(0);
        } else if (getSelectedIndex() > 0) {
            setSelectedIndex(getSelectedIndex() - 1);
        }
    }

    /**
     * Move the selected value in the list down by one.
     */
    public void moveDown() {
        if (getSelectedIndex() < 0) {
            setSelectedIndex(0);
        } else if (getSelectedIndex() < (getListSize() - 1)) {
            setSelectedIndex(getSelectedIndex() + 1);
        }
    }

    /**
     * Move the selected value in the list up by one page.
     */
    public void movePageUp() {
        if (getSelectedIndex() < 0) {
            setSelectedIndex(0);
        } else if (getSelectedIndex() > 0) {
            int a = getSelectedIndex() - 11;
            if (a < 0) a = 0;
            setSelectedIndex(a);
        }
    }

    /**
     * Move the selected value in the list down by one page.
     */
    public void movePageDown() {
        if (getSelectedIndex() < 0) {
            setSelectedIndex(0);
        } else {
            int a = getSelectedIndex() + 11;
            if (a > (getListSize() - 1)) a = getListSize() - 1;
            setSelectedIndex(a);
        }
    }

    /**
     * Get the size of our data list.
     */
    public int getListSize() {
        return grid.getModel().getRowCount();
    }

    /**
     * Get the selected value in the list.
     */
    public Object getSelectedValue() {
        return grid.getSelectedObject();
    }

    /**
     * Jumps to the entry in the list that most resembles String word and
     * highlights it.
     */
    public void setInsightWord(String word) {
        for (int i = 0; i < grid.getModel().getRowCount(); i++) {
            String str = ((QExpTableModel) grid.getModel()).get(i).toString();
            if (word.regionMatches(true, 0, str, 0, word.length())) {
                setSelectedIndex(i);
                break;
            }
        }
    }

}
