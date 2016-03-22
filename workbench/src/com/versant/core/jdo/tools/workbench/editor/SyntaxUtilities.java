
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

import javax.swing.text.*;
import java.awt.*;


/**
 * Class with several utility functions used by jEdit's syntax colorizing
 * subsystem.
 * @keep-all
 * @author Slava Pestov
 */
public class SyntaxUtilities {
	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * string.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The string to match
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
					    int offset, String match) {
		int length = offset + match.length();
		char[] textArray = text.array;
		if(length > text.offset + text.count)
			return false;
		for(int i = offset, j = 0; i < length; i++, j++) {
			char c1 = textArray[i];
			char c2 = match.charAt(j);
			if(ignoreCase) {
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * character array.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The character array to match
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
					    int offset, char[] match) {
		int length = offset + match.length;
		char[] textArray = text.array;
		if(length > text.offset + text.count)
			return false;
		for(int i = offset, j = 0; i < length; i++, j++) {
			char c1 = textArray[i];
			char c2 = match[j];
			if(ignoreCase) {
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	}

	/**
	 * Returns the default style table. This can be passed to the
	 * <code>setStyles()</code> method of <code>SyntaxDocument</code>
	 * to use the default syntax styles.
	 */
	public static SyntaxStyle[] getDefaultSyntaxStyles() {
		SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];


		styles[Token.COMMENT1] = new SyntaxStyle(Color.green.darker().darker(),false,false);
		styles[Token.COMMENT2] = new SyntaxStyle(Color.green.darker().darker(),false,false);
		styles[Token.KEYWORD1] = new SyntaxStyle(new Color(20, 20, 152),false,true);
		styles[Token.KEYWORD2] = new SyntaxStyle(Color.magenta,false,false);
		styles[Token.KEYWORD3] = new SyntaxStyle(new Color(20, 20, 152),false, true);
		styles[Token.LITERAL1] = new SyntaxStyle(Color.blue,false,true);
		styles[Token.LITERAL2] = new SyntaxStyle(Color.blue,false,false);
		styles[Token.LABEL] = new SyntaxStyle(new Color(0x990033),false,true);
		styles[Token.OPERATOR] = new SyntaxStyle(Color.black,false,true);
		styles[Token.INVALID] = new SyntaxStyle(Color.red,false,false);
		styles[Token.METHOD] = new SyntaxStyle(Color.black,false,true);

		return styles;
	}

	/**
	 * Paints the specified line onto the graphics context. Note that this
	 * method munges the offset and count values of the segment.
	 * @param line The line segment
	 * @param tokens The token list for the line
	 * @param styles The syntax style list
	 * @param expander The tab expander used to determine tab stops. May
	 * be null
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The x co-ordinate, plus the width of the painted string
	 */
	public static int paintSyntaxLine(Segment line, Token tokens,
		SyntaxStyle[] styles, TabExpander expander, Graphics gfx,
		int x, int y) {
		Font defaultFont = gfx.getFont();
		Color defaultColor = gfx.getColor();

		int offset = 0;
		for(;;) {
			byte id = tokens.id;
			if(id == Token.END)
				break;

			int length = tokens.length;

			if(id == Token.NULL) {
				if(!defaultColor.equals(gfx.getColor()))
					gfx.setColor(defaultColor);
				if(!defaultFont.equals(gfx.getFont()))
					gfx.setFont(defaultFont);
			} else {
				styles[id].setGraphicsFlags(gfx,defaultFont);
            }

			line.count = length;

			x = drawTabbedText(line,x,y,gfx,expander,0);
			line.offset += length;
			offset += length;

			tokens = tokens.next;
		}

		return x;
	}

    /**
     * Draws the given text, expanding any tabs that are contained
     * using the given tab expansion technique.  This particular
     * implementation renders in a 1.1 style coordinate system
     * where ints are used and 72dpi is assumed.
     *
     * @param s  the source of the text
     * @param x  the X origin >= 0
     * @param y  the Y origin >= 0
     * @param g  the graphics context
     * @param e  how to expand the tabs.  If this value is null,
     *   tabs will be expanded as a space character.
     * @param startOffset starting offset of the text in the document >= 0
     * @return  the X location at the end of the rendered text
     */
    public static final int drawTabbedText(Segment s, int x, int y, Graphics g,
                                           TabExpander e, int startOffset) {
        FontMetrics metrics = g.getFontMetrics();
        int nextX = x;
        char[] txt = s.array;
        int txtOffset = s.offset;
        int flushLen = 0;
        int flushIndex = s.offset;
        int n = s.offset + s.count;
        for (int i = txtOffset; i < n; i++) {
            if (txt[i] == '\t') {
                if (flushLen > 0) {
                    g.drawChars(txt, flushIndex, flushLen, x, y);
                    flushLen = 0;
                }
                flushIndex = i + 1;
                if (e != null) {
                    nextX = (int) e.nextTabStop((float) nextX, startOffset + i - txtOffset);
                } else {
                    nextX += metrics.charWidth(' ');
                }
                x = nextX;
            } else if ((txt[i] == '\n') || (txt[i] == '\r')) {
                if (flushLen > 0) {
                    g.drawChars(txt, flushIndex, flushLen, x, y);
                    flushLen = 0;
                }
                flushIndex = i + 1;
                x = nextX;
            } else {
                flushLen += 1;
                nextX += metrics.charWidth(txt[i]);
            }
        }
        if (flushLen > 0) {
            g.drawChars(txt, flushIndex, flushLen, x, y);
        }
        return nextX;
    }

	// private members
	private SyntaxUtilities() {}
}
