
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
package com.versant.core.jdo.tools.workbench.jdoql.insight;

import javax.swing.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.EventObject;
import java.awt.event.*;
import java.awt.*;

import com.versant.core.jdo.tools.workbench.jdoql.util.JDOQLTextUtil;
import com.versant.core.jdo.tools.workbench.jdoql.pane.HighLightPane;
import com.versant.core.common.Debug;

/**
 * @keep-all
 * Handles the input for our the insight popup
 *
 */
public class PopupInputHandler extends KeyAdapter {

    // private members
    private Hashtable bindings;
    private Hashtable currentBindings;
    private boolean exitOnEnter = false;
    private HighLightPane pane;

    private PopupInputHandler(PopupInputHandler copy) {
        bindings = currentBindings = copy.bindings;
    }

    public PopupInputHandler() {
        bindings = currentBindings = new Hashtable();
    }

    public boolean isExitOnEnter() {
        return exitOnEnter;
    }

    public void setExitOnEnter(boolean exitOnEnter) {
        this.exitOnEnter = exitOnEnter;
    }

    public HighLightPane getPane() {
        return pane;
    }

    public void setPane(HighLightPane pane) {
        this.pane = pane;
    }

    public static final ActionListener POPUP_NEXT_PAGE = new popup_next_page();
    public static final ActionListener POPUP_PREV_PAGE = new popup_prev_page();
    public static final ActionListener POPUP_PREV_LINE = new popup_prev_line();
    public static final ActionListener POPUP_NEXT_LINE = new popup_next_line();
    public static final ActionListener POPUP_NEXT_CHAR = new popup_next_char();
    public static final ActionListener POPUP_PREV_CHAR = new popup_prev_char();
    public static final ActionListener POPUP_PREV_WORD = new popup_prev_word();
    public static final ActionListener POPUP_NEXT_WORD = new popup_next_word();

    public static final ActionListener POPUP_SELECT_NEXT_CHAR = new popup_next_char();
	public static final ActionListener POPUP_SELECT_NEXT_LINE = new popup_next_line();
	public static final ActionListener POPUP_SELECT_NEXT_PAGE = new popup_next_page();
	public static final ActionListener POPUP_SELECT_NEXT_WORD = new popup_next_word();

    public static final ActionListener POPUP_SELECT_PREV_CHAR = new popup_prev_char();
	public static final ActionListener POPUP_SELECT_PREV_LINE = new popup_prev_line();
	public static final ActionListener POPUP_SELECT_PREV_PAGE = new popup_prev_page();
	public static final ActionListener POPUP_SELECT_PREV_WORD = new popup_prev_word();

    public static final ActionListener POPUP_INSERT_BREAK = new popup_insert_break();
    public static final ActionListener POPUP_ESCAPE = new popup_escape();
    public static final ActionListener POPUP_INSERT_TAB = new popup_insert_tab();

    public static final ActionListener POPUP_HOME = new popup_home();
    public static final ActionListener POPUP_END = new popup_end();
    public static final ActionListener POPUP_SELECT_HOME = new popup_home();
    public static final ActionListener POPUP_SELECT_END = new popup_end();

    public static final ActionListener POPUP_DOCUMENT_HOME = new popup_document_home();
    public static final ActionListener POPUP_DOCUMENT_END = new popup_document_end();
    public static final ActionListener POPUP_SELECT_DOC_HOME = new popup_document_home();
    public static final ActionListener POPUP_SELECT_DOC_END = new popup_document_end();


    public static final PopupEnter POPUP_ENTER = new PopupEnter();


    /**
     * Sets up the default key bindings.
     */
    public void addDefaultKeyBindings() {


        addKeyBinding("ESCAPE", POPUP_ESCAPE);

        addKeyBinding("ENTER", POPUP_INSERT_BREAK);
        addKeyBinding("TAB", POPUP_INSERT_TAB);

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

//        addKeyBinding("LEFT", POPUP_PREV_CHAR);
        addKeyBinding("S+LEFT", POPUP_SELECT_PREV_CHAR);
        addKeyBinding("C+LEFT", POPUP_PREV_WORD);
        addKeyBinding("CS+LEFT", POPUP_SELECT_PREV_WORD);
//        addKeyBinding("RIGHT", POPUP_NEXT_CHAR);
        addKeyBinding("S+RIGHT", POPUP_SELECT_NEXT_CHAR);
        addKeyBinding("C+RIGHT", POPUP_NEXT_WORD);
        addKeyBinding("CS+RIGHT", POPUP_SELECT_NEXT_WORD);
        addKeyBinding("UP", POPUP_PREV_LINE);
        addKeyBinding("S+UP", POPUP_SELECT_PREV_LINE);
        addKeyBinding("DOWN", POPUP_NEXT_LINE);
        addKeyBinding("S+DOWN", POPUP_SELECT_NEXT_LINE);

        //addKeyBinding("C+ENTER",REPEAT);
    }



    public static HighLightPane getArea(EventObject evt){
            if (evt != null){
                Object o = evt.getSource();
                if (o instanceof Component){
                    Component c = (Component)o;
                    do {
                        if (c instanceof HighLightPane){
                            return (HighLightPane)c;
                        }
                        if (c == null){
                            break;
                        }
                        if (c instanceof JPopupMenu){
                            c = ((JPopupMenu)c).getInvoker();
                        } else {
                            c = c.getParent();

                        }

                    } while (true);

                }
            }
            return null;

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
        while(st.hasMoreTokens()) {
            KeyStroke keyStroke = parseKeyStroke(st.nextToken());
            if(keyStroke == null) return;

            if(st.hasMoreTokens()) {
                Object o = current.get(keyStroke);
                if(o instanceof Hashtable) {
                    current = (Hashtable)o;
                } else {
                    o = new Hashtable();
                    current.put(keyStroke,o);
                    current = (Hashtable)o;
                }
            } else
                current.put(keyStroke,action);
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
    public PopupInputHandler copy() {
        return new PopupInputHandler(this);
    }

    // ===== Popup methods =====
    private static InsightPopup popup = new InsightPopup();

    /**
     * Is our popup showing?
     */
    public static boolean isPopupShowing() { return popup.isVisible(); }

    /**
     * Get rid of the popup.
     */
    public static void disposePopup () {
        popup.setVisible(false);
    }

    /**
     * Set the list for the popup and show it.
     */
    public void showInsightPopup(InsightBody body, HighLightPane editPane) throws Exception {
        if (isPopupShowing()) {
            disposePopup();
        }
        popup.setBody(body);
        popup.pack();
        popup.setLocation(JDOQLTextUtil.caretPositionToPoint(editPane));
        popup.setVisible(true);
        popup.revalidate();
        popup.pack();

        // The popup should never get the focus, otherwise you won't be able to
        // type while the popup is displayed.
        editPane.setRequestFocusEnabled(true);
        editPane.requestFocus();
    }

    /**
     * Set the word or part of the word that the popup should find in it's
     * list and highlight.
     */
    public static void setPopupFocusWord(HighLightPane editPane) {
        if (isPopupShowing()) {
            popup.setInsightWord(JDOQLTextUtil.getWordUpToCaret(editPane));
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
                setPopupFocusWord(getArea(e));
            }
        }
    }



    /**
     * Handle a key pressed event. This will look up the binding for
     * the key stroke and execute it.
     */
    public void keyPressed(KeyEvent evt) {
        if (isPopupShowing()){
            int keyCode = evt.getKeyCode();
            int modifiers = evt.getModifiers();

            if(keyCode == KeyEvent.VK_CONTROL ||
                    keyCode == KeyEvent.VK_SHIFT ||
                    keyCode == KeyEvent.VK_ALT ||
                    keyCode == KeyEvent.VK_META) {
                return;
            }

            if((modifiers & ~KeyEvent.SHIFT_MASK) != 0
                    || evt.isActionKey()
                    || keyCode == KeyEvent.VK_BACK_SPACE
                    || keyCode == KeyEvent.VK_DELETE
                    || keyCode == KeyEvent.VK_ENTER
                    || keyCode == KeyEvent.VK_TAB
                    || keyCode == KeyEvent.VK_ESCAPE) {
//            if(grabAction != null) {
//                handleGrabAction(evt);
//                return;
//            }

                KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
                Object o = currentBindings.get(keyStroke);
                if(o == null) {
                    // Don't beep if the user presses some
                    // key we don't know about unless a
                    // prefix is active. Otherwise it will
                    // beep when caps lock is pressed, etc.
                    if(currentBindings != bindings) {
                        Toolkit.getDefaultToolkit().beep();
                        // F10 should be passed on, but C+e F10
                        // shouldn't
//                    repeatCount = 0;
//                    repeat = false;
//                        System.out.println("in o == null && currentBindings != bindings");

                        evt.consume();
                    }
                    currentBindings = bindings;
                    return;
                } else if(o instanceof ActionListener) {
                    currentBindings = bindings;

                    executeAction(((ActionListener)o), evt.getSource(),null);

                    evt.consume();
                    return;
                } else if(o instanceof Hashtable) {
                    currentBindings = (Hashtable)o;
                    evt.consume();
                    return;
                }
            }
        } else if (exitOnEnter &&
                pane != null
                && (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_TAB)) {
            evt.consume();
            FocusManager.getCurrentManager().focusNextComponent(pane);
        }
    }

    public void executeAction(ActionListener listener, Object source, String actionCommand){
        // create event
        ActionEvent evt = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, actionCommand);
        listener.actionPerformed(evt);
    }


    /**
     * Handle a key typed event. This inserts the key into the text area.
     */
    public void keyTyped(KeyEvent evt) {
        int modifiers = evt.getModifiers();
        char c = evt.getKeyChar();
        if(c != KeyEvent.CHAR_UNDEFINED && (modifiers & KeyEvent.ALT_MASK) == 0 &&
                                           (modifiers & KeyEvent.CTRL_MASK) == 0) {
            if(c >= 0x20 && c != 0x7f) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(c));
                Object o = currentBindings.get(keyStroke);

                if(o instanceof Hashtable) {
                    currentBindings = (Hashtable)o;
                    return;
                } else if(o instanceof ActionListener) {
                    currentBindings = bindings;

                    executeAction((ActionListener)o, evt.getSource(), String.valueOf(c));
                    return;
                }

                currentBindings = bindings;

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
        if(keyStroke == null) return null;
        int modifiers = 0;
        int index = keyStroke.indexOf('+');
        if(index != -1) {
            for(int i = 0; i < index; i++) {
                switch(Character.toUpperCase(keyStroke.charAt(i))) {
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
        if(key.length() == 1) {
            char ch = Character.toUpperCase(key.charAt(0));
            if(modifiers == 0) return KeyStroke.getKeyStroke(ch);
            else return KeyStroke.getKeyStroke(ch,modifiers);
        }
        else if(key.length() == 0) {
            System.err.println("Invalid key stroke: " + keyStroke);
            return null;
        } else {
            int ch;

            try {
                ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            } catch(Exception e) {
                System.err.println("Invalid key stroke: "
                    + keyStroke);
                return null;
            }

            return KeyStroke.getKeyStroke(ch,modifiers);
        }
    }

    // ===== Actions =====
    /**
     * Previous line action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_line implements ActionListener{

		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.moveUp();
            }
        }
	}

    /**
     * Next line action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_line implements ActionListener{

		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.moveDown();
            }
        }
	}

    /**
     * Previous page action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_page implements ActionListener{


		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.movePageUp();
            }
        }
	}

    /**
     * Next page action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_page implements ActionListener{


		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.movePageDown();
            }
        }
	}

    /**
     * Insert break (enter) action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_insert_break implements ActionListener{

		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                JDOQLTextUtil.replaceAtCaret(getArea(evt), popup.getSelectedValue().toString());
                disposePopup();

            }
        }
	}

    /**
     * Insert break (enter) action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class PopupEnter {

        public static void enter(HighLightPane pane){
            if (isPopupShowing()) {
                JDOQLTextUtil.replaceAtCaret(pane, popup.getSelectedValue().toString());
                disposePopup();

            }
        }
    }

    /**
     * Previous character action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_next_char implements ActionListener{



		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chAfter = '.';
                String strAfterCaret = JDOQLTextUtil.getCharAfterCaret(getArea(evt));
                if (strAfterCaret.equals("")) chAfter = ' ';
                else chAfter = strAfterCaret.toCharArray()[0];

                if (Character.isWhitespace(chAfter) || chAfter == '.') disposePopup();
            }
//            super.actionPerformed(evt);
        }
	}

    /**
     * Previous character action.
     * This doubles as a popup action when the popup is showing.
     */
    public static class popup_prev_char implements ActionListener{

		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chBefore = '.';
                String strBeforeCaret = JDOQLTextUtil.getCharBeforeCaret(getArea(evt));
                if (strBeforeCaret.equals("")) chBefore = ' ';
                else chBefore = strBeforeCaret.toCharArray()[0];

                if (Character.isWhitespace(chBefore) || chBefore == '.') disposePopup();
            }
//            super.actionPerformed(evt);
        }
	}

    /**
     * Escape from popup action.
     * This is only a popup action.
     */
    public static class popup_escape implements ActionListener {

        public popup_escape() { }

		public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) disposePopup();
        }
	}



    public static class popup_next_word implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chAfter = '.';
                String strAfterCaret = JDOQLTextUtil.getCharAfterCaret(getArea(evt));
                if (strAfterCaret.equals("")) chAfter = ' ';
                else chAfter = strAfterCaret.toCharArray()[0];
                if (Character.isWhitespace(chAfter) || chAfter == '.') disposePopup();
            }
//            super.actionPerformed(evt);
        }
    }

    public static class popup_prev_word implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                char chBefore = '.';
                String strBeforeCaret = JDOQLTextUtil.getCharBeforeCaret(getArea(evt));
                if (strBeforeCaret.equals("")) chBefore = ' ';
                else chBefore = strBeforeCaret.toCharArray()[0];

                if (Character.isWhitespace(chBefore) || chBefore == '.') disposePopup();
            }
//            super.actionPerformed(evt);
        }
    }

    public static class popup_home implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(0);
            }
        }
    }

    public static class popup_end implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(popup.getListSize() - 1);
            }
        }
    }

    public static class popup_document_home implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(0);
            }
        }
    }

    public static class popup_document_end implements ActionListener{
        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                popup.setSelectedIndex(popup.getListSize() - 1);
            }
        }
    }


    public static class popup_insert_tab implements ActionListener{

        public void actionPerformed(ActionEvent evt) {
            if (isPopupShowing()) {
                JDOQLTextUtil.replaceAtCaret(getArea(evt), popup.getSelectedValue().toString());
                disposePopup();
            }
        }
    }

}

