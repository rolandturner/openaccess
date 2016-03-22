
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
import java.util.ArrayList;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;


/**
 * Class with several utility functions used by the text area component.
 * This is a special version based on v 1.8 adapted by Matt Benson for Jext.
 * @keep-all
 * @author Slava Pestov
 * @author Matt Benson
 */
public class TextUtilities {
	public static final String BRACKETS = "([{}])";

	public static final int FORWARD = 1;
	public static final int BACKWARD = -1;
	

/**
 * Returns an <CODE>ArrayList</CODE> filled with <CODE>Token</CODE>s
 * linked to the argument token, in the order specified.
 * @param token   The initial <CODE>Token</CODE> that links to the others.
 * @param dir     The <CODE>int</CODE> representation of the ordering to be
 *                used when filling the <CODE>ArrayList</CODE>.
 */	
	private static ArrayList getTokenList(Token token, int dir) {
		ArrayList tokenList = new ArrayList();
		while (token != null) {
			if (token.id == Token.END) {
				token = null;
			} else {
             //end if token.id == Token.END
// The following is just a trick--size * 1 >= 0 but size * -1 <= 0
				tokenList.add(Math.max(0, (tokenList.size() * dir)), token);
				token = token.next;
			}//end else
		}//end while token != null
		return tokenList;
	}//end getTokenList

		
/**
 * Returns the offset of the bracket matching the one at the
 * specified offset of the document, or -1 if the bracket is
 * unmatched (or if the character is not a bracket).
 * @param doc The document
 * @param offset The offset
 * @exception BadLocationException If an out-of-bounds access
 * was attempted on the document text
 * @since jEdit 3.0pre1
 */
	public static int findMatchingBracket(SyntaxDocument doc, int offset)
	 throws BadLocationException {
		if (doc.getLength() == 0)
			return -1;

		Element map = doc.getDefaultRootElement();
		Element lineElement = doc.getParagraphElement(offset);
		Segment lineText = new Segment();
		int lineStart = lineElement.getStartOffset();
		int lineLength = lineElement.getEndOffset() - lineStart - 1;
		int line = map.getElementIndex(lineStart);
		doc.getText(lineStart, lineLength, lineText);
		offset-= lineStart;

		char c;
		try {
			c = lineText.array[lineText.offset + offset];//the character
		} catch(ArrayIndexOutOfBoundsException e) {
                //for when cursor is positioned at offset 0.
			c = (char)0;
		}//end catch ArrayIndexOutOfBoundsException
		
		int whichBracket = BRACKETS.indexOf(c);
		if (whichBracket == -1) {
			return whichBracket;
		}//end if whichBracket == -1
		char cprime = //corresponding character
		 BRACKETS.charAt(BRACKETS.length() - 1 - whichBracket);
		int direction = (whichBracket < BRACKETS.length() / 2) ? FORWARD : BACKWARD;
		
/* I didn't like the old switch statement.
 * Also I changed direction to an int which should be pos/neg 1 for ease in counting
 * with merged search logic as compared to SP versions which search bkwd & fwd separately.
 * Actually the merged search is pretty sloppy but Slava included the comment in an earlier
 * version that he was leaving the merge as an exercise to the reader, so I took him up on it.
 */
		TokenMarker tokenMarker = doc.getTokenMarker();
		
		if (tokenMarker == null)
		{
			return -1;
		}//end if tokenMarker == null
		
		ArrayList tokenList =
		 getTokenList(tokenMarker.markTokens(lineText, line), direction);
		
// Get the syntax token at 'offset'
// only tokens with the same type will be checked for
// the corresponding bracket
		byte idOfBracket = Token.INVALID;//default to invalid
		
		int tokenListOffset = 0;
		int tok = ((direction == FORWARD) ? 0 : tokenList.size() - 1);
		boolean foundBracket = false;
		do {
			Token testToken = null;
			try {
				testToken = (Token)tokenList.get(tok);
			} catch (IndexOutOfBoundsException oob) {
				return -1;
			}//end catch IndexOutOfBoundsException
			tokenListOffset+= testToken.length;
			if (tokenListOffset > offset) {
				idOfBracket = testToken.id;
				if (direction == FORWARD) {
					tokenListOffset-= testToken.length;
				}//end if FORWARD
				foundBracket = true;
			} else {
				tok+= direction;
			}//end else
		} while (!foundBracket);//end do while loop
		
		if (idOfBracket == Token.INVALID) {
			return -1;
		}//end if idOfBracket == Token.INVALID
		
		int count = 0;
		int repetitions =
		 ((direction == FORWARD) ? map.getElementCount() - line : line + 1);

		for (int i = 0; i < repetitions; i++) {
// get text
			int index = line + (i * direction);
			lineElement = map.getElement(index);
			lineStart = lineElement.getStartOffset();
			lineLength = lineElement.getEndOffset() - lineStart - 1;
			doc.getText(lineStart, lineLength, lineText);

			int scanStartOffset;
			if (index != line) {
				tokenList =
				 getTokenList(tokenMarker.markTokens(lineText, line), direction);
				tok = 0;
				if (direction == FORWARD)
				{
					scanStartOffset = tokenListOffset = 0;
				}//end if direction == FORWARD
				else
				{
					tokenListOffset = lineLength;
					scanStartOffset = tokenListOffset - 1;
				}//end else
				
			}//end if not original line
			else
			{
				scanStartOffset = offset;
			}//end else 

			for (; tok < tokenList.size(); tok++)
			{
				Token currTok = (Token)(tokenList.get(tok));
				byte id = currTok.id;
				int len = currTok.length;
				
// only check tokens with id 'idOfBracket'
				if (id == idOfBracket)
				{
					char[] word = new char[len];
					int wordOffset = tokenListOffset +
						 ((direction == FORWARD) ? 0 : (direction * word.length));
					
					for (int j = 0; j < word.length; j++)
					{
						word[j] = lineText.array[lineText.offset + wordOffset + j];
					}//end for thru word
					
					int oppositeEnd = ((direction == FORWARD) ? 0 : word.length -1);
					int wordSearch = scanStartOffset - wordOffset - direction;
//set it 1 different than what we really want b/c we increment in the beginning of the loop
					do
					{
						wordSearch+= direction;
						char ch = word[wordSearch];
						if (ch == c)
						{
							count++;
						}//end if ch == c
							
						else if (ch == cprime)
						{
								if (--count == 0)
								{
									return lineStart + wordOffset + wordSearch;
								}
						}//end else
					} while ((wordSearch + oppositeEnd + 1) != word.length);
				}//end if id == idOfBracket

//the following skips to the next token offset by adding len of curr. token
					tokenListOffset+= (len * direction);
					scanStartOffset = tokenListOffset;
					if (direction == BACKWARD)
					{
						scanStartOffset--;
					}//end if 
			}//end for tok...
		}//end for i

// Nothing found
		return -1;
	}//end getMatchingBracket

	
/**
 * Locates the start of the word at the specified position.
 * @param line The text
 * @param pos The position
 * @param noWordSep Characters that are non-alphanumeric, but
 * should be treated as word characters anyway
 */
	public static int findWordStart(String line, int pos, String noWordSep) {
		char ch = line.charAt(pos);

		if (noWordSep == null)
			noWordSep = "";
		boolean selectNoLetter = (!Character.isLetterOrDigit(ch)
		 && noWordSep.indexOf(ch) == -1);

		int wordStart = 0;
		for(int i = pos; i >= 0; i--) {
			ch = line.charAt(i);
			if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
			 noWordSep.indexOf(ch) == -1)) {
				wordStart = i + 1;
				break;
			}//end if 
		}//end for i

		return wordStart;
	}//end findWordStart

    /**
     * Locates the end of the word at the specified position.
     * @param line The text
     * @param pos The position
     * @param noWordSep Characters that are non-alphanumeric, but
     * should be treated as word characters anyway
     */
    public static int findWordEnd(String line, int pos, String noWordSep) {
        if (pos != 0) {
            pos--;
        }//end if pos != 0

        char ch = line.charAt(pos);

        if (noWordSep == null) {
            noWordSep = "";
        }//end if noWordSep == null
        boolean selectNoLetter = (!Character.isLetterOrDigit(ch)
                && noWordSep.indexOf(ch) == -1);

        int wordEnd = line.length();
        for(int i = pos; i < line.length(); i++) {
            ch = line.charAt(i);
            if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
                    noWordSep.indexOf(ch) == -1)) {
                wordEnd = i;
                break;
            }//end if
        }//end for i
        return wordEnd;
    }//end findWordEnd

	
/**
* Locates the next character type change searching in the specified direction.
* Included for use with Jext's CsWord Action.
* @param line        The text.
* @param pos         The position.
* @param direction   The direction in which the search should be made.
*/
	public static int findTypeChange(String line, int pos, int direction) {
		int type = Character.getType(line.charAt(pos));

		for (int i = pos + direction; ; i+= direction) {
			try {
				if (Character.getType(line.charAt(i)) != type) {
					return i;
				}//end if
			}//end try 
			catch(IndexOutOfBoundsException oobe) {
				return i - direction;
			}//end catch
		}//end for i
	}//end findWordEnd

    /**
     * Get the point on the screen of the caret.
     * This is used to set the position of the popup.
     *
     */
    public static Point caretPositionToPoint(JEditTextArea editor) throws Exception {
        int line = editor.getCaretLine();
        int x = editor.offsetToX(line, editor.getCaretPosition() - editor.getLineStartOffset(line)) + editor.getGutter().getGutterWidth();
        int y =  editor.lineToY(editor.getCaretLine()) + (editor.getFontMetrics(editor.getFont()).getHeight()) + 4;
        Point editorOrigin = editor.getLocationOnScreen();

        return new Point(x + editorOrigin.x, y + editorOrigin.y);
    }

    /**
     * Get the start index of the text at the caret.
     */
    public static int getStartIndex(JEditTextArea editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        for (int lngth = 0; ; lngth++) {
            if (currentIndex == 0) break;
            String s = charBeforeIndex(editor, currentIndex);
            if (empty(s)) break;
            char c = s.toCharArray()[0];
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '$')) break;
            currentIndex -= 1;
        }
        return currentIndex;
    }

    /**
     * Get the end index of the text at the caret.
     */
    public static int getEndIndex(JEditTextArea editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        int lngth = editor.getDocument().getLength() - 1;
        for (; ;) {
            if (currentIndex > lngth) break;
            String s = charAtIndex(editor, currentIndex);
            if (empty(s)) break;
            char c = s.toCharArray()[0];
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '$')) break;
            currentIndex += 1;
        }
        return currentIndex;
    }

    /**
     * Get the char (as a string) before the caret.
     */
    public static String getCharBeforeCaret(JEditTextArea editor) {
        int caretPos = editor.getCaretPosition();
        String result = charBeforeIndex(editor, caretPos);
        if (result != null) return result;
        return "";
    }

    /**
     * Get the char (as a string) after the caret.
     */
    public static String getCharAfterCaret(JEditTextArea editor) {
        int caretPos = editor.getCaretPosition();
        String result = charAtIndex(editor, caretPos);
        if (result != null) return result;
        return "";
    }

    /**
     * Inserts or replaces (depending on what is before and after the caret)
     * text at the caret with String text.
     */
    public static void replaceAtCaret(JEditTextArea editor, String text) {
        editor.setSelectionStart(getStartIndex(editor));
        editor.setSelectionEnd(getEndIndex(editor));
        editor.replaceSelection(text);
//        StringSelection selection = new StringSelection(text);
//        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//        clipboard.setContents(selection, selection);
//        editor.paste();
        //editor.overwriteSetSelectedText(text);
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
    public static String charBeforeIndex(JEditTextArea editor, int index) {
        if (index < 1) return null;
        return editor.getText(index - 1, 1);
    }

    /**
     * Get the char at the index as a string.
     */
    public static String charAtIndex(JEditTextArea editor, int index) {
        if (index > editor.getDocumentLength() - 1) return null;
        return editor.getText(index, 1);
    }

    /**
     * Get the string from the beginning of the word at the caret up to the caret position.
     */
    public static String getWordUpToCaret(JEditTextArea editor) throws BadLocationException {
        int caretPos = editor.getCaretPosition();
        int strt = getStartIndex(editor);
        String str = editor.getText(strt, caretPos - strt);
        if (str != null) return str;
        return "";
    }

    /**
     * Get the word that the caret is on.
     */
    public static String getWordAtCaret(JEditTextArea editor) {
        return getWordAtPosition(editor, editor.getCaretPosition());
    }

    /**
     * Get the word that the position is on.
     */
    public static String getWordAtPosition(JEditTextArea editor, int pos) {
        int strt = getStartIndex(editor);
        int end = getEndIndex(editor);
        String result = editor.getText(strt, end - strt);
        if (result != null) return result;
        return "";
    }

}//end class TextUtilities
