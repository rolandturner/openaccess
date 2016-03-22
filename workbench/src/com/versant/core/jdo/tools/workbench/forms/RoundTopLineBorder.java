
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

import javax.swing.border.AbstractBorder;
import java.awt.*;

/** 
 * Border that draws a line with rounded ends pointing down along the top.
 * This can be combined with a TitledBorder to title a 'column' of controls
 * on a form.
 * @keep-all
 */
public class RoundTopLineBorder extends AbstractBorder {

    private Color color;
    protected int curve = 8;

    public RoundTopLineBorder() {
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 0, 0, 0);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getCurve() {
        return curve;
    }

    public void setCurve(int curve) {
        this.curve = curve;
    }

    public Color getColor(Component c) {
        return color != null? color : c.getBackground().darker();
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Shape oldClip = g.getClip();
        Color oldColor = g.getColor();
        g.translate(x, y);
        try {
            g.setClip(x, y, w, 2);
            g.setColor(getColor(c));
            g.drawRoundRect(1, 1, w - 1, h - 1, curve, curve);
        } finally {
            g.setColor(oldColor);
            g.translate(-x, -y);
            g.setClip(oldClip);
        }
    }

}
