
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

import com.versant.core.jdo.tools.workbench.editor.regexp.RE;
import com.versant.core.jdo.tools.workbench.editor.regexp.RESyntax;
import com.versant.core.jdo.tools.workbench.editor.regexp.REException;
import com.versant.core.jdo.tools.workbench.editor.SyntaxDocument;
import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import com.versant.core.jdo.tools.workbench.editor.TextUtilities;
import com.versant.core.jdo.tools.workbench.editor.Utilities;

import javax.swing.text.Element;
import javax.swing.text.BadLocationException;

/**
 * @keep-all
 */
public class Indent {
    public static boolean indent(JEditTextArea textArea, int lineIndex, boolean canIncreaseIndent,
                                 boolean canDecreaseIndent, String openBrackets, String closeBrackets) {
        if (lineIndex == 0){
            return false;
        }


        // Get properties
//        String openBrackets = textArea.getProperty("indentOpenBrackets");
//        String openBrackets = "{";
////        String closeBrackets = textArea.getProperty("indentCloseBrackets");
//        String closeBrackets = "}";
//        String _indentPrevLine = textArea.getProperty("indentPrevLine");
        String _indentPrevLine = "\\s*(((if|while)\\s*\\(|else|case|default)[^;]*|for\\s*\\(.*)";


        RE indentPrevLineRE = null;
        SyntaxDocument doc = textArea.getDocument();

        if (openBrackets == null)
            openBrackets = "";
        if (closeBrackets == null)
            closeBrackets = "";
        if (_indentPrevLine != null) {
            try {
                indentPrevLineRE = new RE(_indentPrevLine, RE.REG_ICASE,
                        new RESyntax(RESyntax.RE_SYNTAX_PERL5).set(RESyntax.RE_CHAR_CLASSES).setLineSeparator("\n"));
            } catch (REException re) {
            }
        }

        int tabSize = textArea.getTabSize();
        int indentSize = tabSize;
        boolean noTabs = true;

        Element map = doc.getDefaultRootElement();

        String prevLine = null;
        String line = null;

        Element lineElement = map.getElement(lineIndex);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();

        // Get line text
        try {
            line = doc.getText(start, lineElement.getEndOffset() - start - 1);

            for (int i = lineIndex - 1; i >= 0; i--) {
                lineElement = map.getElement(i);
                int lineStart = lineElement.getStartOffset();
                int len = lineElement.getEndOffset() - lineStart - 1;
                if (len != 0) {
                    prevLine = doc.getText(lineStart, len);
                    break;
                }
            }

            if (prevLine == null)
                return false;
        } catch (BadLocationException e) {
            return false;
        }

        /*
        * If 'prevLineIndent' matches a line --> +1
        */
        boolean prevLineMatches = (indentPrevLineRE == null ? false : indentPrevLineRE.isMatch(prevLine));

        /*
        * On the previous line,
        * if(bob) { --> +1
        * if(bob) { } --> 0
        * } else if(bob) { --> +1
        */
        boolean prevLineStart = true; // False after initial indent
        int prevLineIndent = 0; // Indent width (tab expanded)
        int prevLineBrackets = 0; // Additional bracket indent
        for (int i = 0; i < prevLine.length(); i++) {
            char c = prevLine.charAt(i);
            switch (c) {
                case ' ':
                    if (prevLineStart)
                        prevLineIndent++;
                    break;
                case '\t':
                    if (prevLineStart) {
                        prevLineIndent += (tabSize - (prevLineIndent % tabSize));
                    }
                    break;
                default:
                    prevLineStart = false;
                    if (closeBrackets.indexOf(c) != -1) {
                        prevLineBrackets = Math.max(prevLineBrackets - 1, 0);
                    } else if (openBrackets.indexOf(c) != -1) {
                        prevLineMatches = false;
                        prevLineBrackets++;
                    }
                    break;
            }
        }

        /*
        * On the current line,
        * } --> -1
        * } else if(bob) { --> -1
        * if(bob) { } --> 0
        */
        boolean lineStart = true; // False after initial indent
        int lineIndent = 0; // Indent width (tab expanded)
        int lineWidth = 0; // White space count
        int lineBrackets = 0; // Additional bracket indent
        int closeBracketIndex = -1; // For lining up closing
        // and opening brackets
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (c) {
                case ' ':
                    if (lineStart) {
                        lineIndent++;
                        lineWidth++;
                    }
                    break;
                case '\t':
                    if (lineStart) {
                        lineIndent += (tabSize - (lineIndent % tabSize));
                        lineWidth++;
                    }
                    break;
                default:
                    lineStart = false;
                    if (closeBrackets.indexOf(c) != -1) {
                        if (lineBrackets == 0)
                            closeBracketIndex = i;
                        else
                            lineBrackets--;
                    } else if (openBrackets.indexOf(c) != -1) {
                        prevLineMatches = false;
                        lineBrackets++;
                    }
                    break;
            }
        }

        try {
            if (closeBracketIndex != -1) {
                int offset = TextUtilities.findMatchingBracket(doc,
                        map.getElement(lineIndex).getStartOffset() +
                        closeBracketIndex);
                if (offset != -1) {
                    lineElement = map.getElement(map.getElementIndex(offset));
                    int startOffset = lineElement.getStartOffset();
                    String closeLine = doc.getText(startOffset, lineElement.getEndOffset() - startOffset - 1);
                    prevLineIndent = Utilities.getLeadingWhiteSpaceWidth(closeLine, tabSize);
                } else
                    return false;
            } else {
                prevLineIndent += (prevLineBrackets * indentSize);
            }

            if (prevLineMatches) {
                prevLineIndent += indentSize;
            }

            if (!canDecreaseIndent && prevLineIndent <= lineIndent) {
                return false;
            }


            int current = textArea.getCaretPosition();
            int selectionStart = textArea.getSelectionStart();
            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(current);
            String startText = textArea.getSelectedText();

            textArea.setSelectionStart(current);
            textArea.setSelectionEnd(end);
            String endText = textArea.getSelectedText();

            // clean up
            textArea.setSelectionStart(selectionStart);
            textArea.setSelectionEnd(current);
            if ((startText != null &&  startText.trim().equals("")) || (endText != null && endText.trim().equals("")) ){
            } else {
                return false;
            }

            if (!canIncreaseIndent && prevLineIndent >= lineIndent) {
                return false;
            }

            // Do it
            doc.remove(start, lineWidth);
            doc.insertString(start, Utilities.createWhiteSpace(prevLineIndent, (noTabs ? 0 : tabSize)), null);
            return true;
        } catch (BadLocationException bl) {
        }

        return false;
    }
}
