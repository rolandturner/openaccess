
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

import com.versant.core.jdo.tools.workbench.editor.InputHandler;
import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import com.versant.core.jdo.tools.workbench.editor.OneClickAction;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @keep-all
 */
public class BeanshellInputHandler extends InputHandler {



    // private members
    private Hashtable bindings;
    private Hashtable currentBindings;
    private JEditTextArea editor;




    public BeanshellInputHandler(JEditTextArea newEditor) {
        this.editor = newEditor;
        bindings = currentBindings = new Hashtable();
        addDefaultKeyBindings();
    }


    public Hashtable getBindings() {
        return bindings;
    }

    /**
     * Sets up the default key bindings.
     */
    public void addDefaultKeyBindings() {



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




        addToPopup(null, null, null);

        addKeyBinding("C+Z", UNDO);
        addToPopup("C+Z", UNDO, "Undo");
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
        addKeyBinding("C+A", SELECT_ALL);
        addToPopup("C+A", SELECT_ALL, "Select All");






        addKeyBinding("ENTER", INDENT_ON_ENTER);
        addKeyBinding("TAB", INDENT_ON_TAB);
        addKeyBinding("S+TAB", LEFT_INDENT);




        addKeyBinding("BACK_SPACE", BACKSPACE);
        addKeyBinding("C+BACK_SPACE", BACKSPACE_WORD);
        addKeyBinding("DELETE", DELETE);
        addKeyBinding("C+DELETE", DELETE_WORD);



        addKeyBinding("INSERT", OVERWRITE);

    }

    /**
     * Adds a key binding to this input handler. The key binding is
     * a list of white space separated key strokes of the form
     * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
     * or S for Shift, and key is either a character (a-z) or a field
     * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
     *
     * @param keyBinding The key binding
     * @param action     The action
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

    public ArrayList getOptions() {
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
     * Converts a string to a keystroke. The string should be of the
     * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
     * is any combination of A for Alt, C for Control, S for Shift
     * or M for Meta, and <i>shortcut</i> is either a single character,
     * or a keycode name from the <code>KeyEvent</code> class, without
     * the <code>VK_</code> prefix.
     *
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


    /**
     * Removes a key binding from this input handler. This is not yet
     * implemented.
     *
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
        return new BeanshellInputHandler(editor);
    }


    /**
     * Handle a key pressed event. This will look up the binding for
     * the key stroke and execute it.
     */
    public void keyPressed(KeyEvent evt) {
        clearPopup();
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
        char c = evt.getKeyChar();

        if (c != KeyEvent.CHAR_UNDEFINED && !evt.isAltDown()) {
            if (c >= 0x20 && c != 0x7f) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(c));
                Object o = currentBindings.get(keyStroke);

                if (o instanceof Hashtable) {
                    currentBindings = (Hashtable) o;
                    return;
                } else if (o instanceof OneClickAction) {
                    currentBindings = bindings;
                    (getTextArea(evt)).endCurrentEdit();
                    executeOneClickAction((OneClickAction) o, evt.getSource(), String.valueOf(c));
                    return;
                } else if (o instanceof ActionListener) {
                    currentBindings = bindings;
                    (getTextArea(evt)).endCurrentEdit();
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
                    setRepeatCount(repeatCount * 10 + (c - '0'));
                    evt.consume();
                    return;
                }

                executeAction(inputAction, evt.getSource(), String.valueOf(evt.getKeyChar()));

                //if (!evt.isControlDown())
                //System.out.println(evt.isControlDown());
                (getTextArea(evt)).userInput(c);

                repeatCount = 0;
                repeat = false;
            }
        }
    }
}
