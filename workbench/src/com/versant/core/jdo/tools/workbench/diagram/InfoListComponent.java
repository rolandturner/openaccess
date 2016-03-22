
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
 * This displays a list of Strings with each line centered horizontally.
 * @keep-all
 */
public class InfoListComponent extends JComponent {

    private static final int SEPARATOR_HIGHT = 3;
    private ArrayList lines = new ArrayList();

    public InfoListComponent() {
    }

    /**
     * Add line to the list. Null and empty lines are ignored.
     */
    public void addLine(String line) {
        if (line == null || line.length() == 0) return;
        lines.add(new TextFont(line, null));
    }

    /**
     * Add line to the list. Null and empty lines are ignored.
     */
    public void addLine(String line, Font font) {
        if (line == null || line.length() == 0) return;
        lines.add(new TextFont(line,font));
    }

    /**
     * Add line to the list created from label and data. If data is null or
     * empty nothing is done.
     */
    public void addLine(String label, String data) {
        if (data == null || data.length() == 0) return;
        lines.add(new TextFont(label + data, null));
    }

    /**
     * Add a Space
     */
    public void addSeparator() {
        lines.add(null);
    }

    /**
     * Add line to the list created from label and data. If data is null or
     * empty nothing is done.
     */
    public void addLine(String label, String data, Font font) {
        if (data == null || data.length() == 0) return;
        lines.add(new TextFont(label + data, font));
    }

    public void clear() {
        lines.clear();
    }

    protected void paintComponent(Graphics g) {
        FontMetrics fm;
        int n = lines.size();
        if (n == 0) return;
        int width = getWidth();
        Insets ins = getInsets();
        width -= ins.left + ins.right;
        int y = ins.top;
        for (int i = 0; i < n; i++) {
            TextFont tf = (TextFont)lines.get(i);
            if(tf == null){
                y += SEPARATOR_HIGHT;
                continue;
            }
            String s = tf.text;
            Font font = tf.font;
            if(font == null){
                font = getFont();
            }
            g.setFont(font);
            fm = g.getFontMetrics();
            int w = fm.stringWidth(s);
            y += fm.getLeading() + fm.getAscent();
            g.drawString(s, ins.left + (width - w) / 2, y);
            y += fm.getDescent();
        }
    }

    public Dimension getPreferredSize() {
        FontMetrics fm;
        int n = lines.size();
        int maxw = 0;
        int height = 0;
        for (int i = 0; i < n; i++) {
            TextFont tf = (TextFont)lines.get(i);
            if(tf == null){
                height += SEPARATOR_HIGHT;
                continue;
            }
            String s = tf.text;
            Font font = tf.font;
            if(font == null){
                font = getFont();
            }
            fm = getToolkit().getFontMetrics(font);
            int w = fm.stringWidth(s);
            height += fm.getHeight();
            if (w > maxw) maxw = w;
        }
        Insets ins = getInsets();
        return new Dimension(maxw + ins.left + ins.right,
                height + ins.top + ins.bottom);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private static class TextFont{

        private String text;
        private Font font;

        public TextFont(String text, Font font) {
            this.text = text;
            this.font = font;
        }
    }
}
