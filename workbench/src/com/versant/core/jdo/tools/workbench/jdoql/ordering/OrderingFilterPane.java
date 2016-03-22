
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
package com.versant.core.jdo.tools.workbench.jdoql.ordering;

import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.jdoql.pane.HighLightPane;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.util.HashMap;
import java.awt.*;

/**
 * @keep-all
 */
public class OrderingFilterPane extends HighLightPane {

    public OrderingFilterPane() {
        super();
        this.setLexer(new OrderingLexer());
        this.setHighlightStyles(getHighligtStyles());
    }

    private HashMap getHighligtStyles() {

        setFont(new Font("Monospaced", 0, 12));

        SimpleAttributeSet style;
        HashMap styles = new HashMap();
        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, new Color(0xB03060)/*Color.maroon*/);
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("name", style);


        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.blue);
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("string", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, new Color(20, 20, 152));
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("reservedWord", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.black);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("identifier", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.blue);//new Color(0xB03060)/*Color.maroon*/);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("literal", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, new Color(0x000080)/*Color.navy*/);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("separator", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.black);
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("operator", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.green.darker().darker());
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("comment", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, new Color(0xA020F0).darker()/*Color.purple*/);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("preprocessor", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.black);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("whitespace", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.red);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("error", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.green.darker().darker().darker());
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("variable", style);

        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, new Color(153, 0, 153));
        StyleConstants.setBold(style, true);
        StyleConstants.setItalic(style, false);
        styles.put("param", style);


        style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBackground(style, Color.white);
        StyleConstants.setForeground(style, Color.red);
        StyleConstants.setBold(style, false);
        StyleConstants.setItalic(style, false);
        styles.put("unknown", style);

        return styles;
    }

    public void setMdClass(MdClass mdClass) {
        OrderingLexer lex = (OrderingLexer) getLexer();
        lex.setMdClass(mdClass);
    }

    public void setClassType(String classType) {
        OrderingLexer lex = (OrderingLexer) getLexer();
        lex.setClassType(classType);
    }


    public MdClass getMdClass() {
        OrderingLexer lex = (OrderingLexer) getLexer();
        return lex.getMdClass();
    }

}
