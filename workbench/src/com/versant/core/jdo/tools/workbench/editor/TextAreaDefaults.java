
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
package com.versant.core.jdo.tools.workbench.editor;

import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Encapsulates default settings for a text area. This can be passed
 * to the constructor once the necessary fields have been filled out.
 * The advantage of doing this over calling lots of set() methods after
 * creating the text area is that this method is faster.
 * @keep-all
 */
public class TextAreaDefaults {
    private static TextAreaDefaults DEFAULTS;

    public InputHandler inputHandler;
    public boolean editable;

    public boolean caretVisible;
    public boolean caretBlinks;
    public boolean blockCaret;
    public int electricScroll;

    public boolean gutterCollapsed;
    public int gutterWidth;
    public Color gutterBgColor;
    public Color gutterFgColor;
    public Color gutterHighlightColor;
    public Color gutterBorderColor;
    public Color caretMarkColor;
    public Color anchorMarkColor;
    public Color selectionMarkColor;
    public int gutterBorderWidth;
    public int gutterNumberAlignment;
    public Font gutterFont;

    public int cols;
    public int rows;
    public SyntaxStyle[] styles;
    public Color caretColor;
    public Color selectionColor;
    public Color lineHighlightColor;
    public boolean lineHighlight;
    public Color bracketHighlightColor;
    public boolean bracketHighlight;
    public Color eolMarkerColor;
    public boolean eolMarkers;
    public boolean paintInvalid;

    public boolean wrapGuide;
    public Color wrapGuideColor;
    public int wrapGuideOffset;

    public boolean linesIntervalHighlight;
    public Color linesIntervalColor;
    public int linesInterval;

    public boolean antiAliasing;

    public JPopupMenu popup;

    /**
     * Returns a new TextAreaDefaults object with the default values filled
     * in.
     */
    public static TextAreaDefaults getDefaults() {
        if (DEFAULTS == null) {
            DEFAULTS = new TextAreaDefaults();

            DEFAULTS.editable = true;

            DEFAULTS.caretVisible = true;
            DEFAULTS.caretBlinks = true;
            DEFAULTS.electricScroll = 2;

            DEFAULTS.gutterCollapsed = false;
            DEFAULTS.gutterWidth = 35;
            DEFAULTS.gutterBgColor = new Color(214, 214, 214);
            DEFAULTS.gutterFgColor = new Color(128,0,0);
            DEFAULTS.gutterHighlightColor = new Color(0x8080c0);
            DEFAULTS.gutterBorderColor = Color.white;
//      DEFAULTS.caretMarkColor = Color.green;
            DEFAULTS.caretMarkColor = DEFAULTS.gutterBgColor;
            DEFAULTS.anchorMarkColor = Color.red;
            DEFAULTS.selectionMarkColor = UIManager.getColor("textHighlight").darker();//Color.blue;
            DEFAULTS.gutterBorderWidth = 4;
            DEFAULTS.gutterNumberAlignment = Gutter.RIGHT;
            DEFAULTS.gutterFont = new Font("monospaced", Font.PLAIN, 12);

            DEFAULTS.cols = 5;
            DEFAULTS.rows = 5;
            DEFAULTS.styles = SyntaxUtilities.getDefaultSyntaxStyles();
            DEFAULTS.caretColor = Color.black;
            DEFAULTS.selectionColor = UIManager.getColor("textHighlight");//new Color(0xccccff);
            DEFAULTS.lineHighlightColor = new Color(255, 255, 215);
            DEFAULTS.lineHighlight = true;
            DEFAULTS.bracketHighlightColor = new Color(153,204,255);
            DEFAULTS.bracketHighlight = true;
            DEFAULTS.eolMarkerColor = new Color(0x009999);
            DEFAULTS.eolMarkers = false;
            DEFAULTS.paintInvalid = false;

            DEFAULTS.linesIntervalColor = new Color(0xe6e6ff);
            DEFAULTS.linesIntervalHighlight = false;
            DEFAULTS.linesInterval = 5;

            DEFAULTS.wrapGuideColor = Color.lightGray;
            DEFAULTS.wrapGuide = false;
            DEFAULTS.wrapGuideOffset = 80;
        }

        return DEFAULTS;
    }
}

