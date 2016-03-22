
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

import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.editor.InputHandler;
import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import com.versant.core.jdo.tools.workbench.editor.TextUtilities;
import com.versant.core.jdo.tools.workbench.model.DatabaseMetaData;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.sql.lexer.SqlAliasFinderLexer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @keep-all
 */
public class SqlInputHandler extends InputHandler {

    private static SqlAliasFinderLexer finder = new SqlAliasFinderLexer();
    private MdProject project;

    private SqlInsightBody insightBody = null;
    private static final String END_LINE = "[[[END]]]";
    // private members
    private Hashtable bindings;
    private Hashtable currentBindings;
    private JEditTextArea editor = null;
    private static InsightPopup popup = new InsightPopup();


    public SqlInputHandler(JEditTextArea newEditor)  {
        this.editor = newEditor;
        bindings = currentBindings = new Hashtable();
        addDefaultKeyBindings();
        try {
            insightBody = new SqlInsightBody();
        } catch (Exception e) {
        }
        insightBody.getGrid().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                POPUP_ENTER.enter(editor);
            }
        });

    }

    public void setProject(MdProject project) {
        this.project = project;
    }

    public Hashtable getBindings() {
        return bindings;
    }

    public static final PopupEnter POPUP_ENTER = new PopupEnter();

    public static final ActionListener POPUP_NEXT_PAGE = new popup_next_page(false);
    public static final ActionListener POPUP_PREV_PAGE = new popup_prev_page(false);
    public static final ActionListener POPUP_PREV_LINE = new popup_prev_line(false);
    public static final ActionListener POPUP_NEXT_LINE = new popup_next_line(false);
    public static final ActionListener POPUP_NEXT_CHAR = new popup_next_char(false);
    public static final ActionListener POPUP_PREV_CHAR = new popup_prev_char(false);
    public static final ActionListener POPUP_PREV_WORD = new popup_prev_word(false);
    public static final ActionListener POPUP_NEXT_WORD = new popup_next_word(false);

    public static final ActionListener POPUP_SELECT_NEXT_CHAR = new popup_next_char(true);
    public static final ActionListener POPUP_SELECT_NEXT_LINE = new popup_next_line(true);
    public static final ActionListener POPUP_SELECT_NEXT_PAGE = new popup_next_page(true);
    public static final ActionListener POPUP_SELECT_NEXT_WORD = new popup_next_word(true);

    public static final ActionListener POPUP_SELECT_PREV_CHAR = new popup_prev_char(true);
    public static final ActionListener POPUP_SELECT_PREV_LINE = new popup_prev_line(true);
    public static final ActionListener POPUP_SELECT_PREV_PAGE = new popup_prev_page(true);
    public static final ActionListener POPUP_SELECT_PREV_WORD = new popup_prev_word(true);

    public static final ActionListener POPUP_INSERT_BREAK = new popup_insert_break();
    public static final ActionListener POPUP_ESCAPE = new popup_escape();
    public static final ActionListener POPUP_INSERT_TAB = new popup_insert_tab();

    public static final ActionListener POPUP_HOME = new popup_home(false);
    public static final ActionListener POPUP_END = new popup_end(false);
    public static final ActionListener POPUP_SELECT_HOME = new popup_home(true);
    public static final ActionListener POPUP_SELECT_END = new popup_end(true);

    public static final ActionListener POPUP_DOCUMENT_HOME = new popup_document_home(false);
    public static final ActionListener POPUP_DOCUMENT_END = new popup_document_end(false);
    public static final ActionListener POPUP_SELECT_DOC_HOME = new popup_document_home(true);
    public static final ActionListener POPUP_SELECT_DOC_END = new popup_document_end(true);

    public final ActionListener POPUP_BACKSPACE = new popup_backspace();
    public final ActionListener COL_TABLE_POPUP = new col_and_table_insight_popup();
    /**
     * Sets up the default key bindings.
     */
    public void addDefaultKeyBindings() {
        putAction("next-char", POPUP_NEXT_CHAR);
        putAction("prev-char", POPUP_PREV_CHAR);
        putAction("next-line", POPUP_NEXT_LINE);
        putAction("prev-line", POPUP_PREV_LINE);
        putAction("next-page", POPUP_NEXT_PAGE);
        putAction("prev-page", POPUP_PREV_PAGE);
        putAction("next-word", POPUP_NEXT_WORD);
        putAction("prev-word", POPUP_PREV_WORD);

        putAction("select-next-char", POPUP_SELECT_NEXT_CHAR);
        putAction("select-next-line", POPUP_SELECT_NEXT_LINE);
        putAction("select-next-page", POPUP_SELECT_NEXT_PAGE);
        putAction("select-next-word", POPUP_SELECT_NEXT_WORD);

        putAction("select-prev-char", POPUP_SELECT_PREV_CHAR);
        putAction("select-prev-line", POPUP_SELECT_PREV_LINE);
        putAction("select-prev-page", POPUP_SELECT_PREV_PAGE);
        putAction("select-prev-word", POPUP_SELECT_PREV_WORD);

        putAction("end", POPUP_END);
        putAction("home", POPUP_HOME);
        putAction("select-end", POPUP_SELECT_END);
        putAction("select-home", POPUP_SELECT_HOME);

        putAction("document-home", POPUP_DOCUMENT_HOME);
        putAction("select-doc-home", POPUP_SELECT_DOC_HOME);
        putAction("document-end", POPUP_DOCUMENT_END);
        putAction("select-doc-end", POPUP_SELECT_DOC_END);

        putAction("insert-tab", POPUP_INSERT_TAB);
        putAction("insert-break", POPUP_INSERT_BREAK);
        putAction("escape", POPUP_ESCAPE);



        addKeyBinding("C+BACK_SPACE", BACKSPACE_WORD);
        addKeyBinding("DELETE", DELETE);
        addKeyBinding("C+DELETE", DELETE_WORD);


        addKeyBinding("INSERT", OVERWRITE);

        addKeyBinding("HOME", HOME);
        addKeyBinding("END", END);
        addKeyBinding("S+HOME", SELECT_HOME);
        addKeyBinding("S+END", SELECT_END);
        addKeyBinding("C+HOME", DOCUMENT_HOME);
        addKeyBinding("C+END", DOCUMENT_END);
        addKeyBinding("CS+HOME", SELECT_DOC_HOME);
        addKeyBinding("CS+END", SELECT_DOC_END);
        addKeyBinding("PAGE_UP", PREV_PAGE);
        addKeyBinding("PAGE_DOWN", NEXT_PAGE);
        addKeyBinding("S+PAGE_UP", SELECT_PREV_PAGE);
        addKeyBinding("S+PAGE_DOWN", SELECT_NEXT_PAGE);
        addKeyBinding("LEFT", PREV_CHAR);
        addKeyBinding("S+LEFT", SELECT_PREV_CHAR);
        addKeyBinding("C+LEFT", PREV_WORD);
        addKeyBinding("CS+LEFT", SELECT_PREV_WORD);
        addKeyBinding("RIGHT", NEXT_CHAR);
        addKeyBinding("S+RIGHT", SELECT_NEXT_CHAR);
        addKeyBinding("C+RIGHT", NEXT_WORD);
        addKeyBinding("CS+RIGHT", SELECT_NEXT_WORD);
        addKeyBinding("UP", PREV_LINE);
        addKeyBinding("S+UP", SELECT_PREV_LINE);
        addKeyBinding("DOWN", NEXT_LINE);
        addKeyBinding("S+DOWN", SELECT_NEXT_LINE);


        addKeyBinding("C+X", CUT);
        addToPopup("C+X", CUT, "Cut");
        addKeyBinding("C+C", COPY);
        addToPopup("C+C", COPY, "Copy");
        addKeyBinding("C+V", PASTE);
        addToPopup("C+V", PASTE, "Paste");
        addToPopup(null,null,null);

        addKeyBinding("C+SPACE", COL_TABLE_POPUP);
        addToPopup("C+SPACE", COL_TABLE_POPUP,"Code Completions");

        addToPopup(null, null, null);

        addKeyBinding("C+Z", UNDO);
        addToPopup("C+Z", UNDO,"Undo");
        addKeyBinding("CS+Z", REDO);
        addToPopup("CS+Z", REDO, "Redo");
        addToPopup(null, null, null);

        addKeyBinding("C+D", DUP_LINE);
        addToPopup("C+D", DUP_LINE, "Duplicate Line");
        addKeyBinding("C+Y", DELETE_LINE);
        addToPopup("C+Y", DELETE_LINE, "Delete Line");
        addKeyBinding("C+/", COMMENT_LINE);
        addToPopup("C+/", COMMENT_LINE, "Comment Line");
        addKeyBinding("CS+/", COMMENT_ALL);
        addToPopup("CS+/", COMMENT_ALL, "Block Comment");
        addKeyBinding("CS+U", UPPER_LOWER_CASE);
        addToPopup("CS+U", UPPER_LOWER_CASE, "Upper/Lower Case");
        addToPopup(null, null, null);

        addKeyBinding("C+F", FIND);
        addToPopup("C+F", FIND, "Find");
        addKeyBinding("F3", FIND_NEXT);
        addToPopup("F3", FIND_NEXT, "Find Next");
        addKeyBinding("C+R", REPLACE);
        addToPopup("C+R", REPLACE, "Find & Replace");
        addToPopup(null, null, null);

        addKeyBinding("CA+I", FORMAT_TEXT);
        addToPopup("CA+I", FORMAT_TEXT, "Format sql");
        addKeyBinding("C+A", SELECT_ALL);
        addToPopup("C+A", SELECT_ALL, "Select All");

        addKeyBinding("ENTER", INDENT_ON_ENTER);
        addKeyBinding("TAB", INDENT_ON_TAB);
        addKeyBinding("S+TAB", LEFT_INDENT);




        addKeyBinding("ESCAPE", POPUP_ESCAPE);

        addKeyBinding("BACK_SPACE", BACKSPACE);
        addKeyBinding("C+BACK_SPACE", BACKSPACE_WORD);
        addKeyBinding("DELETE", DELETE);
        addKeyBinding("C+DELETE", DELETE_WORD);

        addKeyBinding("ENTER", POPUP_INSERT_BREAK);
        addKeyBinding("TAB", POPUP_INSERT_TAB);

        addKeyBinding("INSERT", OVERWRITE);
//        addKeyBinding("C+\\", TOGGLE_RECT);

        addKeyBinding("HOME", POPUP_HOME);
        addKeyBinding("END", POPUP_END);
        addKeyBinding("S+HOME", POPUP_SELECT_HOME);
        addKeyBinding("S+END", POPUP_SELECT_END);
        addKeyBinding("C+HOME", POPUP_DOCUMENT_HOME);
        addKeyBinding("C+END", POPUP_DOCUMENT_END);
        addKeyBinding("CS+HOME", POPUP_SELECT_DOC_HOME);
        addKeyBinding("CS+END", POPUP_SELECT_DOC_END);

        addKeyBinding("PAGE_UP", POPUP_PREV_PAGE);
        addKeyBinding("PAGE_DOWN", POPUP_NEXT_PAGE);
        addKeyBinding("S+PAGE_UP", POPUP_SELECT_PREV_PAGE);
        addKeyBinding("S+PAGE_DOWN", POPUP_SELECT_NEXT_PAGE);

        addKeyBinding("LEFT", POPUP_PREV_CHAR);
        addKeyBinding("S+LEFT", POPUP_SELECT_PREV_CHAR);
        addKeyBinding("C+LEFT", POPUP_PREV_WORD);
        addKeyBinding("CS+LEFT", POPUP_SELECT_PREV_WORD);
        addKeyBinding("RIGHT", POPUP_NEXT_CHAR);
        addKeyBinding("S+RIGHT", POPUP_SELECT_NEXT_CHAR);
        addKeyBinding("C+RIGHT", POPUP_NEXT_WORD);
        addKeyBinding("CS+RIGHT", POPUP_SELECT_NEXT_WORD);
        addKeyBinding("UP", POPUP_PREV_LINE);
        addKeyBinding("S+UP", POPUP_SELECT_PREV_LINE);
        addKeyBinding("DOWN", POPUP_NEXT_LINE);
        addKeyBinding("S+DOWN", POPUP_SELECT_NEXT_LINE);

        addKeyBinding("BACK_SPACE", POPUP_BACKSPACE);


    }

    /**
     * Adds a key binding to this input handler. The key binding is
     * a list of white space separated key strokes of the form
     * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
     * or S for Shift, and key is either a character (a-z) or a field
     * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
     * @param keyBinding The key binding
     * @param action The action
     */
    public void addKeyBinding(String keyBinding, ActionListener action) {
        Hashtable current = bindings;

        StringTokenizer st = new StringTokenizer(keyBinding);
        while (st.hasMoreTokens()) {
            KeyStroke keyStroke = parseKeyStroke(st.nextToken());
            if (keyStroke == null) return;

            if (st.hasMoreTokens()) {
                Object o = current.get(keyStroke);
                if (o instanceof Hashtable) {
                    current = (Hashtable) o;
                } else {
                    o = new Hashtable();
                    current.put(keyStroke, o);
                    current = (Hashtable) o;
                }
            } else {
                current.put(keyStroke, action);
            }


        }
    }
    ArrayList options = new ArrayList();

    public ArrayList getOptions(){
        return options;
    }
    public void addToPopup(String keyBinding, ActionListener action, String name) {
        if (keyBinding == null) {
            options.add(null);
        } else {
            StringTokenizer st = new StringTokenizer(keyBinding);
            while (st.hasMoreTokens()) {
                KeyStroke keyStroke = parseKeyStroke(st.nextToken());
                if (keyStroke != null && name != null) {
                    JMenuItem item = new JMenuItem(name);
                    item.addActionListener(action);
                    item.setAccelerator(keyStroke);
                    options.add(item);

                }
            }
        }
    }


    /**
     * Removes a key binding from this input handler. This is not yet
     * implemented.
     * @param keyBinding The key binding
     */
    public void removeKeyBinding(String keyBinding) {
        throw new InternalError("Not yet implemented");
    }

    /**
     * Removes all key bindings from this input handler.
     */
    public void removeAllKeyBindings() {
        bindings.clear();
    }

    /**
     * Returns a copy of this input handler that shares the same
     * key bindings. Setting key bindings in the copy will also
     * set them in the original.
     */
    public InputHandler copy() {
        return new SqlInputHandler(editor);
    }

    // ===== Popup methods =====



    /**
     * Is our popup showing?
     */
    public static boolean isPopupShowing() {
        return popup.isVisible();
    }

    /**
     * Get rid of the popup.
     */
    public  void clearPopup() {
        super.clearPopup();
    }

    public static void disposePopup(){
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
    }

    /**
     * Set the list for the popup and show it.
     */
    public void showInsightPopup(SqlInsightBody body, JEditTextArea editPane)
            throws Exception {
        disposePopup();

        popup.setBody(body);
        popup.pack();
        popup.setLocation(TextUtilities.caretPositionToPoint(editPane));
        popup.setVisible(true);

        // The popup should never get the focus, otherwise you won't be able to
        // type while the popup is displayed.
        editPane.setRequestFocusEnabled(true);
        editPane.requestFocus();
    }

    /**
     * Set the word or part of the word that the popup should find in it's
     * list and highlight.
     */
    public static void setPopupFocusWord(JEditTextArea editPane) {
        if (isPopupShowing()) {
            try {
                popup.setInsightWord(TextUtilities.getWordUpToCaret(editPane));
            } catch (BadLocationException e) {
                // do nothing for now
            }
        }
    }

    /**
     * set the popup focus word after every key release
     */
    public void keyReleased(KeyEvent e) {
        if (isPopupShowing()) {
            if (e.getKeyCode() != KeyEvent.VK_UP &&
                    e.getKeyCode() != KeyEvent.VK_DOWN &&
                    e.getKeyCode() != KeyEvent.VK_PAGE_UP &&
                    e.getKeyCode() != KeyEvent.VK_PAGE_DOWN &&
                    e.getKeyCode() != KeyEvent.VK_HOME &&
                    e.getKeyCode() != KeyEvent.VK_END) {
                setPopupFocusWord(getTextArea(e));
            }
        } else {
            if (String.valueOf(e.getKeyChar()).equals(".")) {
                COL_TABLE_POPUP.actionPerformed(null);
            }
        }


    }

    /**
     * Handle a key pressed event. This will look up the binding for
     * the key stroke and execute it.
     */
    public void keyPressed(KeyEvent evt) {
        super.clearPopup();
        int keyCode = evt.getKeyCode();
        int modifiers = evt.getModifiers();

        if (keyCode == KeyEvent.VK_CONTROL ||
                keyCode == KeyEvent.VK_SHIFT ||
                keyCode == KeyEvent.VK_ALT ||
                keyCode == KeyEvent.VK_META)
            return;

        if (evt.isShiftDown()) {
            if (grabAction != null) {
                handleGrabAction(evt);
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            Object o = currentBindings.get(keyStroke);

            if (o == null) {
                // Don't beep if the user presses some
                // key we don't know about unless a
                // prefix is active. Otherwise it will
                // beep when caps lock is pressed, etc.
                if (currentBindings != bindings) {
                    Toolkit.getDefaultToolkit().beep();
                    // F10 should be passed on, but C+e F10
                    // shouldn't
                    repeatCount = 0;
                    repeat = false;
                    evt.consume();



                }
                currentBindings = bindings;
                return;
            } else if (o instanceof ActionListener) {
                currentBindings = bindings;

                (getTextArea(evt)).endCurrentEdit();
                executeAction(((ActionListener) o), evt.getSource(), null);

                evt.consume();
                return;
            } else if (o instanceof Hashtable) {
                currentBindings = (Hashtable) o;
                evt.consume();
                return;
            }
        } else if (!evt.isShiftDown()
                || evt.isActionKey()
                || keyCode == KeyEvent.VK_BACK_SPACE
                || keyCode == KeyEvent.VK_DELETE
                || keyCode == KeyEvent.VK_ENTER
                || keyCode == KeyEvent.VK_TAB
                || keyCode == KeyEvent.VK_ESCAPE) {
            if (grabAction != null) {
                handleGrabAction(evt);
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            Object o = currentBindings.get(keyStroke);

            if (o == null) {
                // Don't beep if the user presses some
                // key we don't know about unless a
                // prefix is active. Otherwise it will
                // beep when caps lock is pressed, etc.
                if (currentBindings != bindings) {
                    Toolkit.getDefaultToolkit().beep();
                    // F10 should be passed on, but C+e F10
                    // shouldn't
                    repeatCount = 0;
                    repeat = false;
                    evt.consume();
                }
                currentBindings = bindings;
                return;
            } else if (o instanceof ActionListener) {
                currentBindings = bindings;

                (getTextArea(evt)).endCurrentEdit();
                executeAction(((ActionListener) o), evt.getSource(), null);

                evt.consume();
                return;
            } else if (o instanceof Hashtable) {
                currentBindings = (Hashtable) o;
                evt.consume();
                return;
            }
        }
    }



    /**
     * Handle a key typed event. This inserts the key into the text area.
     */
    public void keyTyped(KeyEvent evt) {
        int modifiers = evt.getModifiers();
        char c = evt.getKeyChar();
//        if(c != KeyEvent.CHAR_UNDEFINED &&
//                (modifiers & KeyEvent.ALT_MASK) == 0) {
        if (c != KeyEvent.CHAR_UNDEFINED && (modifiers & KeyEvent.ALT_MASK) == 0 &&
                (modifiers & KeyEvent.CTRL_MASK) == 0) {
            if (c >= 0x20 && c != 0x7f) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(c));
                Object o = currentBindings.get(keyStroke);

                if (o instanceof Hashtable) {
                    currentBindings = (Hashtable) o;
                    return;
                } else if (o instanceof ActionListener) {
                    currentBindings = bindings;

                    executeAction((ActionListener) o, evt.getSource(), String.valueOf(c));
                    return;
                }

                currentBindings = bindings;

                if (grabAction != null) {
                    handleGrabAction(evt);
                    return;
                }

                // 0-9 adds another 'digit' to the repeat number
                if (repeat && Character.isDigit(c)) {

                    repeatCount *= 10;
                    repeatCount += (c - '0');
                    return;
                }

                executeAction(INSERT_CHAR, evt.getSource(), String.valueOf(evt.getKeyChar()));


                repeatCount = 0;
                repeat = false;


            }
        }
    }

    /**
     * Converts a string to a keystroke. The string should be of the
     * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
     * is any combination of A for Alt, C for Control, S for Shift
     * or M for Meta, and <i>shortcut</i> is either a single character,
     * or a keycode name from the <code>KeyEvent</code> class, without
     * the <code>VK_</code> prefix.
     * @param keyStroke A string description of the key stroke
     */
    public static KeyStroke parseKeyStroke(String keyStroke) {
        if (keyStroke == null) return null;
        int modifiers = 0;
        int index = keyStroke.indexOf('+');
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                switch (Character.toUpperCase(keyStroke.charAt(i))) {
                    case 'A':
                        modifiers |= InputEvent.ALT_MASK;
                        break;
                    case 'C':
                        modifiers |= InputEvent.CTRL_MASK;
                        break;
                    case 'M':
                        modifiers |= InputEvent.META_MASK;
                        break;
                    case 'S':
                        modifiers |= InputEvent.SHIFT_MASK;
                        break;
                }
            }
        }
        String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            char ch = Character.toUpperCase(key.charAt(0));
            if (modifiers == 0)
                return KeyStroke.getKeyStroke(ch);
            else
                return KeyStroke.getKeyStroke(ch, modifiers);
        } else if (key.length() == 0) {
            System.err.println("Invalid key stroke: " + keyStroke);
            return null;
        } else {
            int ch;

            try {
                ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            } catch (Exception e) {
                System.err.println("Invalid key stroke: "
                        + keyStroke);
                return null;
            }

            return KeyStroke.getKeyStroke(ch, modifiers);
        }
    }

    // ===== Actions =====
    /**
     * Previous line action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_line extends prev_line {

        public popup_prev_line(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (!isPopupShowing()) {
                super.actionPerformed(evt);
            } else {
                popup.moveUp();
            }
        }
    }

    /**
     * Next line action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_line extends next_line {

        public popup_next_line(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (!isPopupShowing()) {
                super.actionPerformed(evt);
            } else {
                popup.moveDown();
            }
        }
    }

    /**
     * Previous page action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_page extends prev_page {

        public popup_prev_page(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (!isPopupShowing()) {
                super.actionPerformed(evt);
            } else {
                popup.movePageUp();
            }
        }
    }

    /**
     * Next page action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_page extends next_page {

        public popup_next_page(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (!isPopupShowing()) {
                super.actionPerformed(evt);
            } else {
                popup.movePageDown();
            }
        }
    }

    /**
     * Insert break (enter) action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_insert_break extends indent_on_enter {

        public popup_insert_break() {
        }

        public void actionPerformed(ActionEvent evt) {
            if (!isPopupShowing()) {
                super.actionPerformed(evt);
            } else {
                TextUtilities.replaceAtCaret(getTextArea(evt),  ((DatabaseMetaData.FieldDisplay)popup.getSelectedValue()).getName());
                disposePopup();
            }
        }
    }

    /**
     * Previous character action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_char extends next_char {

        public popup_next_char(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chAfter = '.';
                String strAfterCaret = TextUtilities.getCharAfterCaret(getTextArea(evt));
                if (strAfterCaret.equals(""))
                    chAfter = ' ';
                else
                    chAfter = strAfterCaret.toCharArray()[0];

                if (Character.isWhitespace(chAfter) || chAfter == '.') disposePopup();
            }
            super.actionPerformed(evt);
        }
    }

    /**
     * Previous character action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_char extends prev_char {

        public popup_prev_char(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chBefore = '.';
                String strBeforeCaret = TextUtilities.getCharBeforeCaret(getTextArea(evt));
                if (strBeforeCaret.equals(""))
                    chBefore = ' ';
                else
                    chBefore = strBeforeCaret.toCharArray()[0];

                if (Character.isWhitespace(chBefore) || chBefore == '.') disposePopup();
            }
            super.actionPerformed(evt);
        }
    }

    public class popup_backspace extends backspace {
        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                if (editor.getSelectionStart() == editor.getSelectionEnd()) {
                    int caret = editor.getCaretPosition();
                    if (caret != 0) {
                        try {
                            String deleted = editor.getDocument().getText(caret - 1, 1);
                            if (" ".equals(deleted) || ".".equals(deleted)){
                                disposePopup();
                            }
                        } catch (BadLocationException bl) { }
                    }
                }
            }
            super.actionPerformed(evt);
        }
    }

    /**
     * Escape from popup action.
     * This is only a popup action.
     */
    public static class popup_escape implements ActionListener {

        public popup_escape() {
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) disposePopup();
        }
    }

    /**
     * Insert character action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_insert_char extends insert_char {

        public popup_insert_char() {
        }

        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
        }
    }

    public static class popup_next_word extends next_word {

        public popup_next_word(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chAfter = '.';
                String strAfterCaret = TextUtilities.getCharAfterCaret(getTextArea(evt));
                if (strAfterCaret.equals(""))
                    chAfter = ' ';
                else
                    chAfter = strAfterCaret.toCharArray()[0];
                if (Character.isWhitespace(chAfter) || chAfter == '.') disposePopup();
            }
            super.actionPerformed(evt);
        }
    }

    public static class popup_prev_word extends prev_word {

        public popup_prev_word(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chBefore = '.';
                String strBeforeCaret = TextUtilities.getCharBeforeCaret(getTextArea(evt));
                if (strBeforeCaret.equals(""))
                    chBefore = ' ';
                else
                    chBefore = strBeforeCaret.toCharArray()[0];

                if (Character.isWhitespace(chBefore) || chBefore == '.') disposePopup();
            }
            super.actionPerformed(evt);
        }
    }

    public static class popup_home extends home {

        public popup_home(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(0);
            } else {
                super.actionPerformed(evt);
            }
        }
    }

    public static class popup_end extends end {

        public popup_end(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(popup.getListSize() - 1);
            } else {
                super.actionPerformed(evt);
            }
        }
    }

    public static class popup_document_home extends document_home {

        public popup_document_home(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(0);
            } else {
                super.actionPerformed(evt);
            }
        }
    }

    public static class popup_document_end extends document_end {

        public popup_document_end(boolean select) {
            super(select);
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(popup.getListSize() - 1);
            } else {
                super.actionPerformed(evt);
            }
        }
    }


    public static class popup_insert_tab extends InputHandler.indent_on_tab {

        public popup_insert_tab() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                TextUtilities.replaceAtCaret(getTextArea(evt), ((DatabaseMetaData.FieldDisplay) popup.getSelectedValue()).getName());
                disposePopup();
            } else {
                super.actionPerformed(evt);
            }
        }
    }

    public static class PopupEnter {
        public void enter(JEditTextArea area){
            TextUtilities.replaceAtCaret(area, ((DatabaseMetaData.FieldDisplay) popup.getSelectedValue()).getName());
            disposePopup();
        }
    }


    /**
     * Show the column and table popup
     */
    public class col_and_table_insight_popup implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            try {

                if (project != null && project.getDatabaseMetaData() != null && editor.isEditable()) {
                    if (isTablePopup(editor)) {

                        String tableHeader = "Table";

                        java.util.List list = null;
                        String name = null;


                        if (project.getDatabaseMetaData().containsTable(TextUtilities.getWordAtCaret(editor))) {
                            list = project.getDatabaseMetaData().getAllTableNamesForDisplay();
                        } else {
                            name = findTableName(editor);
                            if (name != null) {
                                tableHeader = name;
                                list = project.getDatabaseMetaData().getAllColumnNamesForDisplay(name);
                            } else {
                                list = project.getDatabaseMetaData().getAllTableNamesForDisplay();
                            }
                        }


                        if (list == null) {
                            list = project.getDatabaseMetaData().getAllTableNamesForDisplay();
                        }

                        insightBody.setHeader(tableHeader);
                        insightBody.setListData(list.toArray());

                        showInsightPopup(insightBody, editor);
                        setPopupFocusWord(editor);
                    } else {
                        String tableName = getTableName(editor);
                        if (tableName != null) {
                            String aliasTableName = findTableNameForAlias(editor, tableName);
                            if (aliasTableName != null){
                                tableName = aliasTableName;
                            }
                            java.util.List list = project.getDatabaseMetaData().getAllColumnNamesForDisplay(tableName);

                            if (list == null) return;

                            insightBody.setHeader(tableName);
                            insightBody.setListData(list.toArray());

                            showInsightPopup(insightBody, editor);
                            setPopupFocusWord(editor);
                        }
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
                GuiUtils.dispatchException(editor, x);
            }
        }
    }

    /**
     * Is the popup for columns or for tables.
     */
    public static boolean isTablePopup(JEditTextArea editor) {
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        for (; currentIndex > 1;) {
            String s = TextUtilities.charBeforeIndex(editor, currentIndex);
            if (TextUtilities.empty(s)) return true;
            char c = s.toCharArray()[0];
            if (Character.isWhitespace(c)) return true;
            if (c == '.') return false;
            currentIndex--;
        }
        return true;
    }

    /**
     * Get the table name if it's a column popup.
     */
    public static String getTableName(JEditTextArea editor) {
        if (isTablePopup(editor)) return null;

        StringBuffer tbleName = new StringBuffer();
        int caretPos = editor.getCaretPosition();
        int currentIndex = caretPos;
        boolean start = false;
        for (; currentIndex > 0;) {
            String s = TextUtilities.charBeforeIndex(editor, currentIndex);
            if (TextUtilities.empty(s)) break;
            char c = s.toCharArray()[0];
            if (Character.isWhitespace(c)) break;
            if (start) tbleName.append(c);
            if (c == '.') start = true;
            currentIndex--;
        }

        if (tbleName.length() < 1) return null;
        return tbleName.reverse().toString();
    }

    public static String findTableName(JEditTextArea editor) {
        int bottom;
        int top = bottom = editor.getCaretLine();
        int end = editor.getLineCount();

        String tableName = findTableName(editor.getLineText(top));

        String text = null;
        boolean topEnd = false;
        boolean bottomEnd = false;

        if (tableName != null) {
            if (tableName.equals(END_LINE)) {
                bottomEnd = true;
            } else {
                return tableName;
            }
        }
        if (END_LINE.equals(findTableName(editor.getLineText(top)))) {
            topEnd = true;
        }


        while ((top > 0) || (bottom < end)) {
            if (topEnd && bottomEnd) {
                return null;
            }

            if (!topEnd) {
                top--;
                if (!(top < 0)) {
                    text = editor.getLineText(top);
                    if (text.trim().equals("")) {
                        topEnd = true;
                    } else {
                        tableName = findTableName(text);
                        if (tableName != null) {
                            if (tableName.equals(END_LINE)) {
                                topEnd = true;
                            } else {
                                return tableName;
                            }
                        }
                    }
                } else {
                    topEnd = true;
                }
            }
            if (!bottomEnd) {
                bottom++;
                if (!(bottom >= end)) {
                    text = editor.getLineText(bottom);
                    if (text.trim().equals("")) {
                        bottomEnd = true;
                    } else {
                        tableName = findTableName(text);
                        if (tableName != null) {
                            if (tableName.equals(END_LINE)) {
                                bottomEnd = true;
                            } else {
                                return tableName;
                            }
                        }
                    }
                } else {
                    bottomEnd = true;
                }
            }
        }
        return null;

    }

    public static String findTableNameForAlias(JEditTextArea editor, String alias) {
        int bottom;
        int top = bottom = editor.getCaretLine();
        int end = editor.getLineCount();

        String tableName = findTableNameForAlias(editor.getLineText(top), alias, false);

        String text = null;
        boolean topWhite = false;
        boolean bottomWhite = false;
        boolean topEnd = false;
        boolean bottomEnd = false;

        if (tableName != null) {
            if (tableName.equals(END_LINE)) {
                bottomEnd = true;
            } else {
                return tableName;
            }
        }
        if (END_LINE.equals(findTableNameForAlias(editor.getLineText(top), alias, true))) {
            bottomEnd = true;
        }


        while ((top > 0) || (bottom < end)) {
            if (topEnd && bottomEnd) {
                return null;
            }

            if ((topWhite || topEnd) && (bottomWhite || bottomEnd)) {
                topWhite = false;
                bottomWhite = false;
            }
            if (!topEnd) {
                if (!topWhite) {
                    top--;
                    if (!(top < 0)) {
                        text = editor.getLineText(top);
                        if (text.trim().equals("")) {
                            topWhite = true;
                        } else {
                            tableName = findTableNameForAlias(text, alias, true);
                            if (tableName != null) {
                                if (tableName.equals(END_LINE)) {
                                    topEnd = true;
                                } else {
                                    return tableName;
                                }
                            }
                        }
                    } else {
                        topEnd = true;
                    }
                }
            }
            if (!bottomEnd) {
                if (!bottomWhite) {
                    bottom++;
                    if (!(bottom >= end)) {
                        text = editor.getLineText(bottom);
                        if (text.trim().equals("")) {
                            bottomWhite = true;
                        } else {
                            tableName = findTableNameForAlias(text, alias, false);
                            if (tableName != null) {
                                if (tableName.equals(END_LINE)) {
                                    bottomEnd = true;
                                } else {
                                    return tableName;
                                }
                            }
                        }
                    } else {
                        bottomEnd = true;
                    }
                }
            }
        }
        return null;

    }

    private static String findTableNameForAlias(String text, String alias, boolean top) {
        try {
            return finder.findAlias(text, alias, top);
        } catch (IOException e) {
        }
        return null;
    }

    private static String findTableName(String text) {
        try {
            return finder.findTable(text);
        } catch (IOException e) {
        }
        return null;
    }


}
