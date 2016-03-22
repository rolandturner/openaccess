
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
package com.versant.core.jdo.tools.workbench.jdoql.util;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.StringTokenizer;

import com.versant.core.jdo.tools.workbench.jdoql.pane.HighLightPane;

/**
 * @keep-all
 */
public class JDOQLTextUtil {

    public JDOQLTextUtil() { }

    /**
     * Get the point on the screen of the caret.
     * This is used to set the position of the popup.
     */
    public static Point caretPositionToPoint(HighLightPane editor) throws Exception {
        Point textPoint = editor.modelToView(editor.getCaretPosition()).getLocation();
        Point componentPoint = editor.getLocationOnScreen();
        return new Point(textPoint.x+componentPoint.x,textPoint.y+componentPoint.y+15);

    }




    /**
     * Get the end index of the text at the caret.
     */
    public static boolean containsMethod(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        int lngth = editor.getDocument().getLength() - 1;
        while (true) {
            if (currentIndex > lngth) {
                return false;
            }
            String s = charAtIndex(editor, currentIndex);
            if (empty(s)) {
                return false;
            }
            char c = s.toCharArray()[0];
            if (Character.isWhitespace(c) || c == '.' || c == ')') {
                return false;
            }
            if (c == '(') {
                return true;
            }
            currentIndex += 1;
        }
    }

    /**
     * Get the start index of the text at the caret.
     */
    public static int getStartIndex(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        while (true) {
            if (currentIndex == 0) break;
            String s = charBeforeIndex(editor, currentIndex);
            if (empty(s)) break;
            char c = s.toCharArray()[0];
            if (Character.isWhitespace(c) || c == '.' || c == ',' || c == '(' || c == ',' || c == ')') break;
            currentIndex -= 1;
        }
        return currentIndex;
    }

    /**
     * Get the end index of the text at the caret.
     */
    public static int getEndIndex(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        int lngth = editor.getDocument().getLength() - 1;
        while (true) {
            if (currentIndex > lngth){
                break;
            }
            String s = charAtIndex(editor, currentIndex);
            if (empty(s)){
                break;
            }
            char c = s.toCharArray()[0];
            if (Character.isWhitespace(c) || c == '.' || c == ',' || c == '(' || c == ')') {
                break;
            }
            currentIndex += 1;
        }
        return currentIndex;
    }

    /**
     * Get the char (as a string) before the caret.
     */
    public static String getCharBeforeCaret(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        String result = charBeforeIndex(editor, caretPos);
        if (result != null) return result;
        return "";
    }

    /**
     * Get the char (as a string) after the caret.
     */
    public static String getCharAfterCaret(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        String result = charAtIndex(editor, caretPos);
        if (result != null) return result;
        return "";
    }

    /**
     * Inserts or replaces (depending on what is before and after the caret)
     * text at the caret with String text.
     */
    public static void replaceAtCaret(HighLightPane editor, String text) {
        if (text.endsWith("()")){
            // we have a method
            if (containsMethod(editor)){
                text = text.substring(0,text.indexOf('('));
            }
        }
        editor.setSelectionStart(getStartIndex(editor));
        editor.setSelectionEnd(getEndIndex(editor));
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        editor.paste();

    }

    /**
     * Check if a string is empty or null.
     */
    public static boolean empty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Get the char before the index as a string.
     */
    public static String charBeforeIndex(HighLightPane editor, int index) {
        if (index < 1) return null;
        try {
            return editor.getText(index - 1, 1);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
         return "";
    }

    /**
     * Get the char at the index as a string.
     */
    public static String charAtIndex(HighLightPane editor, int index) {
        if (index > editor.getDocument().getLength() - 1) return null;
        try {
            return editor.getText(index, 1);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
         return "";
    }

    /**
     * Get the string from the beginning of the word at the caret up to the caret position.
     */
    public static String getWordUpToCaret(HighLightPane editor) {
        int caretPos = editor.getCaretPosition();
        int strt = getStartIndex(editor);
        String str = null;
        try {
            str = editor.getText(strt, caretPos - strt);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        if (str != null) return str;
        return "";
    }

    /**
     * Get the word that the caret is on.
     */
    public static String getWordAtCaret(HighLightPane editor) {
        return getWordAtPosition(editor, editor.getCaretPosition());
    }

    /**
     * Get the word that the position is on.
     */
    public static String getWordAtPosition(HighLightPane editor, int pos) {
        int strt = getStartIndex(editor);
        int end = getEndIndex(editor);
        String result = null;
        try {
            result = editor.getText(strt, end - strt);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        if (result != null) return result;
        return "";
    }

    /**
     * Format our tooltip text into an html table so it can
     * display on multiple lines.
     *
     * @param txt String to format
     * @return Html formatted string
     */
    public static String formatToolTip(String txt) {
        StringTokenizer t = new StringTokenizer(txt, "\n", false);

        StringBuffer toolTip = new StringBuffer();
        toolTip.append("<html><table>");

        for (; t.hasMoreElements();) {
            String s = (String)t.nextElement();
            toolTip.append("<tr>" + s + "</tr>");
        }

        toolTip.append("</table></html>");
        return toolTip.toString();
    }
}

