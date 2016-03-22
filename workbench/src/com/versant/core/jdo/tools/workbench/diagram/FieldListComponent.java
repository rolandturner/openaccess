
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
package com.versant.core.jdo.tools.workbench.diagram;

import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;

/**
 * This displays a list of Strings arranged into columns.
 * @keep-all
 */
public class FieldListComponent extends JComponent {

    private static final int SEPARATOR_WIDTH = 6;

    private ArrayList cols = new ArrayList();
    private FieldListColModel colModel;

    public FieldListComponent() {
    }

    public FieldListColModel getColModel() {
        return colModel;
    }

    public void setColModel(FieldListColModel colModel) {
        colModel.addComponent(this);
        this.colModel = colModel;
    }

    /**
     * Add text to the list. The group
     * param is used to decided the alignment of columns.
     * @see #calcColumnAlignments
     */
    public void addColumn(String text, int group) {
        addColumn(text, null, null, group);
    }

    /**
     * Add text to the list. The group
     * param is used to decided the alignment of columns.
     * @see #calcColumnAlignments
     */
    public void addColumn(String text, Font font, Color color, int group) {
        if (text == null) text = "";
        cols.add(new Col(text, font, color, group));
    }

    /**
     * Get the number of columns.
     */
    public int getColumnCount() {
        return cols.size();
    }

    /**
     * Get rid of all columns.
     */
    public void clear() {
        cols.clear();
    }

    /**
     * Calculate column alignments. This should be called once all columns
     * have been added. The alignment of a column depends on the other columns
     * with the same group and on its absolute position in the column order.
     * The alignment of each column is is set according to the following rules:
     * <br>
     *
     * <li>The first column is left justified.
     * <li>The last column is right justified.
     * <li>The first column in a group is left justified if there are others.
     * <li>All other columns are right justified.
     */
    public void calcColumnAlignments() {
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            Col c = (Col)cols.get(i);
            if (i == 0) {
                c.alignment = SwingConstants.LEFT;
            } else if (i == n - 1) {
                c.alignment = SwingConstants.RIGHT;
            } else {
                Col next = (Col)cols.get(i + 1);
                if (c.group == next.group) c.alignment = SwingConstants.LEFT;
                else c.alignment = SwingConstants.RIGHT;
            }
        }
    }

    protected void paintComponent(Graphics g) {
        FontMetrics fm;
        int[] colW = colModel.getColWidths();
        int n = cols.size();
        if (n == 0) return;
        Insets ins = getInsets();
        int height = getHeight() - ins.bottom;
        int x = ins.left - SEPARATOR_WIDTH;
        for (int i = 0; i < n; i++) {
            Col tf = (Col)cols.get(i);
            String s = tf.text;
            Font font = tf.font;
            if (font != null) g.setFont(font);
            Color color = tf.color;
            if (color == null) color = getForeground();
            g.setColor(color);
            fm = g.getFontMetrics();
            if (tf.alignment == SwingConstants.LEFT) {
                x += SEPARATOR_WIDTH;
                g.drawString(s, x, height - fm.getDescent());
                x += colW[i];
            } else {
                x += colW[i];
                int w = fm.stringWidth(s);
                g.drawString(s, x - w, height - fm.getDescent());
            }
        }
    }

    public Dimension getPreferredSize() {
        int n = cols.size();
        int height = 0;
        Insets ins = getInsets();
        int extraWidth = ins.left + ins.right;
        for (int i = 0; i < n; i++) {
            Col tf = (Col)cols.get(i);
            Font font = tf.font;
            if (font == null) font = getFont();
            FontMetrics fm = getToolkit().getFontMetrics(font);
            int h = fm.getHeight();
            if (h > height) height = h;
            if (i > 0 && tf.alignment == SwingConstants.LEFT) {
                extraWidth += SEPARATOR_WIDTH;
            }
        }
        int width = colModel != null ? colModel.getTotWidth() : 100;
        return new Dimension(width + extraWidth, height + ins.top + ins.bottom);
    }

    /**
     * Get the preferred width for each of our columns. Right justified columns
     * have width zero if the previous column was left justified. However they
     * do add to the width of the previous column if it was left justified.
     */
    public int[] getPreferredColWidths() {
        int n = cols.size();
        int[] w = new int[n];
        int prevAlignment = SwingConstants.RIGHT;
        for (int i = 0; i < n; i++) {
            Col tf = (Col)cols.get(i);
            Font font = tf.font;
            if (font == null) font = getFont();
            int sw = getToolkit().getFontMetrics(font).stringWidth(tf.text);
            if (tf.alignment != SwingConstants.RIGHT
                    || prevAlignment != SwingConstants.LEFT) {
                w[i] = sw + SEPARATOR_WIDTH;
            } else {
                w[i - 1] += sw;
            }
            prevAlignment = tf.alignment;
        }
        return w;
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private static class Col {

        private String text;
        private Font font;
        private Color color;
        private int group;
        private int alignment;

        public Col(String text, Font font, Color color, int group) {
            this.text = text;
            this.font = font;
            this.color = color;
            this.group = group;
        }
    }
}
