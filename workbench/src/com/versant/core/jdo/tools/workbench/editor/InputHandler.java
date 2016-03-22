
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

import com.versant.core.jdo.tools.workbench.editor.misc.Indent;
import com.versant.core.jdo.tools.workbench.sql.util.TextUtils;
import com.versant.core.jdo.tools.workbench.sql.formatter.SQLFormatter;
import com.versant.core.jdo.tools.workbench.jdoql.pane.FindReplace;
import com.versant.core.jdo.tools.workbench.WorkbenchPanelHelperManager;

import javax.swing.text.*;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * An input handler converts the user's key strokes into concrete actions.
 * It also takes care of macro recording and action repetition.<p>
 *
 * This class provides all the necessary support code for an input
 * handler, but doesn't actually do any key binding logic. It is up
 * to the implementations of this class to do so.
 * @author Slava Pestov
 * @see DefaultInputHandler
 */
public abstract class InputHandler extends KeyAdapter {
    /**
     * If this client property is set to Boolean.TRUE on the text area,
     * the home/end keys will support 'smart' BRIEF-like behaviour
     * (one press = start/end of line, two presses = start/end of
     * viewscreen, three presses = start/end of document). By default,
     * this property is not set.
     */
    public static final String SMART_HOME_END_PROPERTY = "InputHandler.homeEnd";

    public static FindReplace findReplace;
    public static JPopupMenu popup;


    public static final ActionListener FIND_NEXT = new find_Next();
    public static final ActionListener UPPER_LOWER_CASE = new upperLowerCase();
    public static final ActionListener FIND = new find();
    public static final ActionListener INDENT_ON_ENTER = new indent_on_enter();
    public static final ActionListener RIGHT_INDENT = new right_indent();
    public static final ActionListener LEFT_INDENT = new left_indent();
    public static final ActionListener INDENT_ON_TAB = new indent_on_tab();
    public static final ActionListener FORMAT_TEXT = new format_text();
    public static final ActionListener REPLACE = new replace();
    public static final ActionListener COMMENT_LINE = new comment_line();
    public static final ActionListener COMMENT_ALL = new comment_all();
    public static final ActionListener DUP_LINE = new dup_line();
    public static final ActionListener DELETE_LINE = new delete_line();
    public static final ActionListener TRANSPOSE_LINE = new transpose_line();
    public static final ActionListener UNDO = new undo();
    public static final ActionListener REDO = new redo();
    public static final ActionListener CUT = new cut();
    public static final ActionListener COPY = new copy();
    public static final ActionListener PASTE = new paste();
    public static final ActionListener SELECT_ALL = new select_all();

    public static final ActionListener BACKSPACE = new backspace();
    public static final ActionListener BACKSPACE_WORD = new backspace_word();
    public static final ActionListener DELETE = new delete();
    public static final ActionListener DELETE_WORD = new delete_word();
    public static final ActionListener END = new end(false);
    public static final ActionListener DOCUMENT_END = new document_end(false);
    public static final ActionListener SELECT_END = new end(true);
    public static final ActionListener SELECT_DOC_END = new document_end(true);
//    public static final ActionListener INSERT_BREAK = new insert_break();
//    public static final ActionListener INSERT_TAB = new insert_tab();
    public static final ActionListener HOME = new home(false);
    public static final ActionListener DOCUMENT_HOME = new document_home(false);
    public static final ActionListener SELECT_HOME = new home(true);
    public static final ActionListener SELECT_DOC_HOME = new document_home(true);
    public static final ActionListener NEXT_CHAR = new next_char(false);
    public static final ActionListener NEXT_LINE = new next_line(false);
    public static final ActionListener NEXT_PAGE = new next_page(false);
    public static final ActionListener NEXT_WORD = new next_word(false);
    public static final ActionListener SELECT_NEXT_CHAR = new next_char(true);
    public static final ActionListener SELECT_NEXT_LINE = new next_line(true);
    public static final ActionListener SELECT_NEXT_PAGE = new next_page(true);
    public static final ActionListener SELECT_NEXT_WORD = new next_word(true);
    public static final ActionListener OVERWRITE = new overwrite();
    public static final ActionListener PREV_CHAR = new prev_char(false);
    public static final ActionListener PREV_LINE = new prev_line(false);
    public static final ActionListener PREV_PAGE = new prev_page(false);
    public static final ActionListener PREV_WORD = new prev_word(false);
    public static final ActionListener SELECT_PREV_CHAR = new prev_char(true);
    public static final ActionListener SELECT_PREV_LINE = new prev_line(true);
    public static final ActionListener SELECT_PREV_PAGE = new prev_page(true);
    public static final ActionListener SELECT_PREV_WORD = new prev_word(true);
    public static final ActionListener REPEAT = new repeat();

    // Default action
    public static final ActionListener INSERT_CHAR = new insert_char();

    private static Hashtable actions;

    static {
        actions = new Hashtable();
        actions.put("backspace", BACKSPACE);
        actions.put("upper_lower_case", UPPER_LOWER_CASE);
        actions.put("indent-on-enter", INDENT_ON_ENTER);
        actions.put("left_indent", LEFT_INDENT);
        actions.put("indent-on-tab", INDENT_ON_TAB);
        actions.put("find-next", FIND_NEXT);
        actions.put("find", FIND);
        actions.put("format-text", FORMAT_TEXT);
        actions.put("replace", REPLACE);
        actions.put("dup-line",DUP_LINE);
        actions.put("comment-line", COMMENT_LINE);
        actions.put("comment-all", COMMENT_ALL);
        actions.put("delete-line", DELETE_LINE);
        actions.put("transpose-line",TRANSPOSE_LINE);
        actions.put("select-all", SELECT_ALL);
        actions.put("undo",UNDO);
        actions.put("redo",REDO);
        actions.put("cut",CUT);
        actions.put("copy",COPY);
        actions.put("paste",PASTE);
        actions.put("backspace-word",BACKSPACE_WORD);
        actions.put("delete",DELETE);
        actions.put("delete-word",DELETE_WORD);
        actions.put("end",END);
        actions.put("select-end",SELECT_END);
        actions.put("document-end",DOCUMENT_END);
        actions.put("select-doc-end",SELECT_DOC_END);
//        actions.put("insert-break",INSERT_BREAK);
//        actions.put("insert-tab",INSERT_TAB);
        actions.put("home",HOME);
        actions.put("select-home",SELECT_HOME);
        actions.put("document-home",DOCUMENT_HOME);
        actions.put("select-doc-home",SELECT_DOC_HOME);
        actions.put("next-char",NEXT_CHAR);
        actions.put("next-line",NEXT_LINE);
        actions.put("next-page",NEXT_PAGE);
        actions.put("next-word",NEXT_WORD);
        actions.put("select-next-char",SELECT_NEXT_CHAR);
        actions.put("select-next-line",SELECT_NEXT_LINE);
        actions.put("select-next-page",SELECT_NEXT_PAGE);
        actions.put("select-next-word",SELECT_NEXT_WORD);
        actions.put("overwrite",OVERWRITE);
        actions.put("prev-char",PREV_CHAR);
        actions.put("prev-line",PREV_LINE);
        actions.put("prev-page",PREV_PAGE);
        actions.put("prev-word",PREV_WORD);
        actions.put("select-prev-char",SELECT_PREV_CHAR);
        actions.put("select-prev-line",SELECT_PREV_LINE);
        actions.put("select-prev-page",SELECT_PREV_PAGE);
        actions.put("select-prev-word",SELECT_PREV_WORD);
        actions.put("repeat",REPEAT);
        actions.put("insert-char",INSERT_CHAR);
    }

    public void clearPopup(){
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
    }

    public static void putAction(Object key, ActionListener value) {
        actions.put(key, value);
    }


    /**
     * Returns a named text area action.
     * @param name The action name
     */
    public static ActionListener getAction(String name)
    {
        return (ActionListener)actions.get(name);
    }

    /**
     * Returns the name of the specified text area action.
     * @param listener The action
     */
    public static String getActionName(ActionListener listener)
    {
        Enumeration enumu = getActions();
        while(enumu.hasMoreElements())
        {
            String name = (String)enumu.nextElement();
            ActionListener _listener = getAction(name);
            if(_listener == listener)
                return name;
        }
        return null;
    }

    /**
     * Returns an enumeration of all available actions.
     */
    public static Enumeration getActions()
    {
        return actions.keys();
    }

    /**
     * Adds the default key bindings to this input handler.
     * This should not be called in the constructor of this
     * input handler, because applications might load the
     * key bindings from a file, etc.
     */
    public abstract void addDefaultKeyBindings();

    /**
     * Adds a key binding to this input handler.
     * @param keyBinding The key binding (the format of this is
     * input-handler specific)
     * @param action The action
     */
    public abstract void addKeyBinding(String keyBinding, ActionListener action);

    public abstract Hashtable getBindings();

    /**
     * Removes a key binding from this input handler.
     * @param keyBinding The key binding
     */
    public abstract void removeKeyBinding(String keyBinding);

    /**
     * Removes all key bindings from this input handler.
     */
    public abstract void removeAllKeyBindings();

    /**
     * Grabs the next key typed event and invokes the specified
     * action with the key as a the action command.
     * @param listener The action
     */
    public void grabNextKeyStroke(ActionListener listener)
    {
        grabAction = listener;
    }

    /**
     * Returns if repeating is enabled. When repeating is enabled,
     * actions will be executed multiple times. This is usually
     * invoked with a special key stroke in the input handler.
     */
    public boolean isRepeatEnabled()
    {
        return repeat;
    }

    /**
     * Enables repeating. When repeating is enabled, actions will be
     * executed multiple times. Once repeating is enabled, the input
     * handler should read a number from the keyboard.
     */
    public void setRepeatEnabled(boolean repeat)
    {
        this.repeat = repeat;
        if(!repeat)
            repeatCount = 0;
    }

    /**
     * Returns the number of times the next action will be repeated.
     */
    public int getRepeatCount() {
        return (repeat ? Math.max(1,repeatCount) : 1);
    }

    /**
     * Sets the number of times the next action will be repeated.
     * @param repeatCount The repeat count
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the action used to handle text input.
     */
    public ActionListener getInputAction() {
        return inputAction;
    }

    /**
     * Sets the action used to handle text input.
     * @param inputAction The new input action
     */
    public void setInputAction(ActionListener inputAction) {
        this.inputAction = inputAction;
    }

    /**
     * Returns the macro recorder. If this is non-null, all executed
     * actions should be forwarded to the recorder.
     */
    public InputHandler.MacroRecorder getMacroRecorder() {
        return recorder;
    }

    /**
     * Sets the macro recorder. If this is non-null, all executed
     * actions should be forwarded to the recorder.
     * @param recorder The macro recorder
     */
    public void setMacroRecorder(InputHandler.MacroRecorder recorder) {
        this.recorder = recorder;
    }

    public void executeOneClickAction(OneClickAction listener, Object source,
                                      String actionCommand) {
        // create event
        ActionEvent evt = new ActionEvent(source,
                ActionEvent.ACTION_PERFORMED,
                actionCommand);

        listener.oneClickActionPerformed(evt);
    }

    /**
     * Executes the specified action, repeating and recording it as
     * necessary.
     * @param listener The action listener
     * @param source The event source
     * @param actionCommand The action command
     */
    public void executeAction(ActionListener listener, Object source, String actionCommand) {
        // create event
        ActionEvent evt = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, actionCommand);

        if (listener instanceof EditAction && !getTextArea(evt).isEditable())
            return;

        getTextArea(evt).setOneClick(null);

        // don't do anything if the action is a wrapper
        // (like EditAction.Wrapper)
        if(listener instanceof Wrapper) {
            listener.actionPerformed(evt);
            return;
        }

        // remember old values, in case action changes them
        boolean _repeat = repeat;
        int _repeatCount = getRepeatCount();

        // execute the action
        if(listener instanceof InputHandler.NonRepeatable)
            listener.actionPerformed(evt);
        else {
            for (int i = 0; i < Math.max(1, _repeatCount); i++)
                listener.actionPerformed(evt);
        }

        // do recording. Notice that we do no recording whatsoever
        // for actions that grab keys
        if(grabAction == null) {
            if(recorder != null) {
                if(!(listener instanceof InputHandler.NonRecordable)) {
                    if(_repeatCount != 1){
                        recorder.actionPerformed(REPEAT,String.valueOf(_repeatCount));
                    }

                    recorder.actionPerformed(listener,actionCommand);
                }
            }

            // If repeat was true originally, clear it
            // Otherwise it might have been set by the action, etc
            if(_repeat)
                setRepeatEnabled(false);
        }
    }

    /**
     * Returns the text area that fired the specified event.
     * @param evt The event
     */
    public static JEditTextArea getTextArea(EventObject evt) {
        if(evt != null) {
            Object o = evt.getSource();
            if(o instanceof Component) {
                // find the parent text area
                Component c = (Component)o;
                for(;;) {
                    if(c instanceof JEditTextArea)
                        return (JEditTextArea)c;
                    else if(c == null)
                        break;
                    if(c instanceof JPopupMenu)
                        c = ((JPopupMenu)c).getInvoker();
                    else
                        c = c.getParent();
                }
            }
        }

        // this shouldn't happen
        return null;
    }

    // protected members
    protected ActionListener inputAction = INSERT_CHAR;
    protected ActionListener grabAction;
    protected boolean repeat;
    protected int repeatCount;
    protected InputHandler.MacroRecorder recorder;

    /**
     * If a key is being grabbed, this method should be called with
     * the appropriate key event. It executes the grab action with
     * the typed character as the parameter.
     */
    protected void handleGrabAction(KeyEvent evt) {
        // Clear it *before* it is executed so that executeAction()
        // resets the repeat count
        ActionListener _grabAction = grabAction;
        grabAction = null;

        char keyChar = evt.getKeyChar();
        int keyCode = evt.getKeyCode();

        String arg;

        if(keyChar != KeyEvent.VK_UNDEFINED)
            arg = String.valueOf(keyChar);
        else if(keyCode == KeyEvent.VK_TAB)
            arg = "\t";
        else if(keyCode == KeyEvent.VK_ENTER)
            arg = "\n";
        else
            arg = "\0";

        executeAction(_grabAction,evt.getSource(),arg);
    }

    /**
     * If an action implements this interface, it should not be repeated.
     * Instead, it will handle the repetition itself.
     */
    public interface NonRepeatable { }

    /**
     * If an action implements this interface, it should not be recorded
     * by the macro recorder. Instead, it will do its own recording.
     */
    public interface NonRecordable { }

    /**
     * For use by EditAction.Wrapper only.
     * @since jEdit 2.2final
     */
    public interface Wrapper { }

    /**
     * Macro recorder.
     */
    public interface MacroRecorder {
        void actionPerformed(ActionListener listener,
                             String actionCommand);
    }

    public static class backspace implements ActionListener {
        public void actionPerformed(ActionEvent evt)  {
            JEditTextArea textArea = getTextArea(evt);

            if(!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            if(textArea.getSelectionStart() != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            } else {
                int caret = textArea.getCaretPosition();
                if(caret == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                try {
                    textArea.getDocument().remove(caret - 1,1);
                } catch(BadLocationException bl) {
//                    bl.printStackTrace();
                }
            }
        }
    }

    public static class undo implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            textArea.showWaitCursor();
            UndoManager undo = textArea.getUndo();
            textArea.setUndoing(true);
            try {
                if (undo.canUndo()){
                    undo.undo();
                }
                if (!undo.canUndo() &&  textArea.isDirty()){
                    textArea.clean();
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }

            textArea.grabFocus();
            textArea.requestFocus();
            textArea.setUndoing(false);
            textArea.hideWaitCursor();
        }
    }

    public static class redo implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }


            textArea.showWaitCursor();
            UndoManager redo = textArea.getUndo();
            textArea.setUndoing(true);
            try {
                if (redo.canRedo()) {
                    redo.redo();
                }
            } catch (Exception e) {
            }

            textArea.grabFocus();
            textArea.requestFocus();
            textArea.setUndoing(false);
            textArea.hideWaitCursor();
        }
    }

    public static class backspace_word implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int start = textArea.getSelectionStart();
            if(start != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
                return;
            }

            int line = textArea.getCaretLine();
            int lineStart = textArea.getLineStartOffset(line);
            int caret = start - lineStart;

            String lineText = textArea.getLineText(textArea
                    .getCaretLine());

            if(caret == 0) {
                if(lineStart == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                caret--;
            } else {
//        String noWordSep = ((JextTextArea) textArea).getProperty("noWordSep");
                caret = TextUtilities.findWordStart(lineText,
                        caret-1,"_");
            }

            try {
                textArea.getDocument().remove(
                        caret + lineStart,
                        start - (caret + lineStart));
            } catch(BadLocationException bl) {
//                bl.printStackTrace();
            }
        }
    }

    public static class delete implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if(!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            if(textArea.getSelectionStart()
                    != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            } else {
                int caret = textArea.getCaretPosition();
                if(caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                try {
                    textArea.getDocument().remove(caret,1);
                } catch(BadLocationException bl) {
//                    bl.printStackTrace();
                }
            }
        }
    }

    public static class delete_word implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int start = textArea.getSelectionStart();
            if(start != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
                return;
            }

            int line = textArea.getCaretLine();
            int lineStart = textArea.getLineStartOffset(line);
            int caret = start - lineStart;

            String lineText = textArea.getLineText(textArea
                    .getCaretLine());

            if(caret == lineText.length()) {
                if(lineStart + caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                caret++;
            } else {
//        String noWordSep = ((JextTextArea) textArea).getProperty("noWordSep");
                caret = TextUtilities.findWordEnd(lineText,
                        caret+1,"_");
            }

            try {
                textArea.getDocument().remove(start,
                        (caret + lineStart) - start);
            } catch(BadLocationException bl) {
//                bl.printStackTrace();
            }
        }
    }

    public static class end implements ActionListener {
        private boolean select;

        public end(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            int caret = textArea.getCaretPosition();

            int lastOfLine = textArea.getLineEndOffset(
                    textArea.getCaretLine()) - 1;
            int lastVisibleLine = textArea.getFirstLine()
                    + textArea.getVisibleLines();
            if(lastVisibleLine >= textArea.getLineCount()) {
                lastVisibleLine = Math.min(textArea.getLineCount() - 1,
                        lastVisibleLine);
            } else
                lastVisibleLine -= (textArea.getElectricScroll() + 1);

            int lastVisible = textArea.getLineEndOffset(lastVisibleLine) - 1;
            int lastDocument = textArea.getDocumentLength();

            if(caret == lastDocument) {
                textArea.getToolkit().beep();
                return;
            }
            else if(!Boolean.TRUE.equals(textArea.getClientProperty(
                    SMART_HOME_END_PROPERTY)))
                caret = lastOfLine;
            else if(caret == lastVisible)
                caret = lastDocument;
            else if(caret == lastOfLine)
                caret = lastVisible;
            else
                caret = lastOfLine;

            if(select)
                textArea.select(textArea.getMarkPosition(),caret);
            else
                textArea.setCaretPosition(caret);
        }
    }

    public static class document_end implements ActionListener {
        private boolean select;

        public document_end(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if(select)
                textArea.select(textArea.getMarkPosition(),
                        textArea.getDocumentLength());
            else
                textArea.setCaretPosition(textArea
                        .getDocumentLength());
        }
    }

    public static class home implements ActionListener {
        private boolean select;

        public home(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            int caret = textArea.getCaretPosition();

            int firstLine = textArea.getFirstLine();

            int firstOfLine = textArea.getLineStartOffset(textArea.getCaretLine());
            int firstVisibleLine = (firstLine == 0 ? 0 : firstLine + textArea.getElectricScroll());
            int firstVisible = textArea.getLineStartOffset(firstVisibleLine);

            if(caret == 0) {
                textArea.getToolkit().beep();
                return;
            } else if (!Boolean.TRUE.equals(textArea.getClientProperty(SMART_HOME_END_PROPERTY))) {
                int textPosition = Utilities.getLeadingWhiteSpace(textArea.getLineText(textArea.getCaretLine()));
                textPosition += firstOfLine;

                if (caret == textPosition)
                    caret = firstOfLine;
                else
                    caret = textPosition;
            } else if (caret == firstVisible)
                caret = 0;
            else if (caret == firstOfLine){
                caret = firstVisible;
            } else {
                int textPosition = Utilities.getLeadingWhiteSpace(textArea.getLineText(textArea.getCaretLine()));
                textPosition += firstOfLine;

                if (caret == textPosition)
                    caret = firstOfLine;
                else
                    caret = textPosition;
            }

            if (select)
                textArea.select(textArea.getMarkPosition(), caret);
            else
                textArea.setCaretPosition(caret);
        }
    }

    public static class document_home implements ActionListener {
        private boolean select;

        public document_home(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if(select)
                textArea.select(textArea.getMarkPosition(),0);
            else
                textArea.setCaretPosition(0);
        }
    }


    public static class next_char implements ActionListener {
        private boolean select;

        public next_char(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            if(caret == textArea.getDocumentLength()) {
                textArea.getToolkit().beep();
                return;
            }

            if(select)
                textArea.select(textArea.getMarkPosition(),
                        caret + 1);
            else
                textArea.setCaretPosition(caret + 1);
        }
    }

    public static class next_line implements ActionListener {
        private boolean select;

        public next_line(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            int line = textArea.getCaretLine();

            if(line == textArea.getLineCount() - 1) {
                textArea.getToolkit().beep();
                return;
            }

            int magic = textArea.getMagicCaretPosition();
            if(magic == -1) {
                magic = textArea.offsetToX(line,
                        caret - textArea.getLineStartOffset(line));
            }

            caret = textArea.getLineStartOffset(line + 1)
                    + textArea.xToOffset(line + 1,magic + 1);
            if(select)
                textArea.select(textArea.getMarkPosition(),caret);
            else
                textArea.setCaretPosition(caret);
            textArea.setMagicCaretPosition(magic);
        }
    }

    public static class next_page implements ActionListener {
        private boolean select;

        public next_page(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int lineCount = textArea.getLineCount();
            int firstLine = textArea.getFirstLine();
            int visibleLines = textArea.getVisibleLines();
            int line = textArea.getCaretLine();

            firstLine += visibleLines;

            if(firstLine + visibleLines >= lineCount - 1)
                firstLine = lineCount - visibleLines;

            textArea.setFirstLine(firstLine);

            int caret = textArea.getLineStartOffset(
                    Math.min(textArea.getLineCount() - 1,
                            line + visibleLines));
            if(select)
                textArea.select(textArea.getMarkPosition(),caret);
            else
                textArea.setCaretPosition(caret);
        }
    }

    public static class next_word implements ActionListener {
        private boolean select;

        public next_word(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            int line = textArea.getCaretLine();
            int lineStart = textArea.getLineStartOffset(line);
            String lineText = textArea.getLineText(line);
            int len = lineText.length();

            boolean skipSpacesAfter = true,
                    enabled = true;

            caret -= lineStart;

            if(caret == len) {
                if(lineStart + caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                caret++;
            } else {
                //MODIFIED by Blaisorblade, Paolo Giarrusso
                //We must always skip spaces before jumping over a word, if the patch is enabled
                if (enabled) {
                    while (caret < len && lineText.charAt(caret) == ' ')
                        caret++;
                }
                if (caret < len) {
//          String noWordSep = ((JextTextArea) textArea).getProperty("noWordSep");
                    caret = TextUtilities.findWordEnd(lineText,caret + 1,"_");
                    //MODIFIED
                    if (skipSpacesAfter) {
                        while (caret < len && lineText.charAt(caret) == ' ')
                            caret++;
                    }
                }
            }

            if(select)
                textArea.select(textArea.getMarkPosition(),
                        lineStart + caret);
            else
                textArea.setCaretPosition(lineStart + caret);
        }
    }

    public static class overwrite implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            textArea.setOverwriteEnabled(
                    !textArea.isOverwriteEnabled());
        }
    }

    public static class prev_char implements ActionListener {
        private boolean select;

        public prev_char(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            if(caret == 0) {
                textArea.getToolkit().beep();
                return;
            }

            if(select)
                textArea.select(textArea.getMarkPosition(),
                        caret - 1);
            else
                textArea.setCaretPosition(caret - 1);
        }
    }

    public static class prev_line implements ActionListener {
        private boolean select;

        public prev_line(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            int line = textArea.getCaretLine();

            if(line == 0) {
                textArea.getToolkit().beep();
                return;
            }

            int magic = textArea.getMagicCaretPosition();
            if(magic == -1) {
                magic = textArea.offsetToX(line,
                        caret - textArea.getLineStartOffset(line));
            }

            caret = textArea.getLineStartOffset(line - 1)
                    + textArea.xToOffset(line - 1,magic + 1);
            if(select)
                textArea.select(textArea.getMarkPosition(),caret);
            else
                textArea.setCaretPosition(caret);
            textArea.setMagicCaretPosition(magic);
        }
    }

    public static class prev_page implements ActionListener {
        private boolean select;

        public prev_page(boolean select)  {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int firstLine = textArea.getFirstLine();
            int visibleLines = textArea.getVisibleLines();
            int line = textArea.getCaretLine();

            if(firstLine < visibleLines)
                firstLine = visibleLines;

            textArea.setFirstLine(firstLine - visibleLines);

            int caret = textArea.getLineStartOffset(
                    Math.max(0,line - visibleLines));
            if(select)
                textArea.select(textArea.getMarkPosition(),caret);
            else
                textArea.setCaretPosition(caret);
        }
    }

    public static class prev_word implements ActionListener {
        private boolean select;

        public prev_word(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            int caret = textArea.getCaretPosition();
            int line = textArea.getCaretLine();
            int lineStart = textArea.getLineStartOffset(line);
            String lineText = textArea.getLineText(line);

            boolean skipSpacesBefore = false,
                    enabled = true;

            caret -= lineStart;

            if(caret == 0) {
                if(lineStart == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                caret--;
            } else {
                //MODIFIED by Blaisorblade, Paolo Giarrusso
                //We must always skip spaces before jumping over a word, if the patch is enabled
                if (enabled)
                    while (caret > 0 && lineText.charAt(caret - 1) == ' ')
                        caret--;

                if (caret > 0) {
//          String noWordSep = ((JextTextArea) textArea).getProperty("noWordSep");
                    caret = TextUtilities.findWordStart(lineText,caret - 1,"_");
                    if (skipSpacesBefore)
                        while (caret > 0 && lineText.charAt(caret - 1) == ' ')
                            caret--;
                }
            }

            if(select)
                textArea.select(textArea.getMarkPosition(),
                        lineStart + caret);
            else
                textArea.setCaretPosition(lineStart + caret);
        }
    }

    public static class repeat implements ActionListener,
            InputHandler.NonRecordable {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            textArea.getInputHandler().setRepeatEnabled(true);
            String actionCommand = evt.getActionCommand();
            if (actionCommand != null) {
                textArea.getInputHandler().setRepeatCount(Integer.parseInt(actionCommand));
            }
        }
    }

    public static class insert_char implements ActionListener, InputHandler.NonRepeatable {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            String str = evt.getActionCommand();
            int repeatCount = textArea.getInputHandler().getRepeatCount();

            if(textArea.isEditable()) {
                StringBuffer buf = new StringBuffer();
                for(int i = 0; i < repeatCount; i++)
                    buf.append(str);
                textArea.overwriteSetSelectedText(buf.toString());
            } else {
                textArea.getToolkit().beep();
            }
        }
    }

    public static class cut implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            textArea.cut();
            textArea.grabFocus();
            textArea.requestFocus();

        }
    }

    public static class copy implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            textArea.copy();
            textArea.grabFocus();
            textArea.requestFocus();
        }
    }

    public static class paste implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            textArea.paste();
            textArea.grabFocus();
            textArea.requestFocus();
        }
    }

    public static class delete_line implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            int line = textArea.getCaretLine();
            int start = textArea.getLineStartOffset(line);
            int end = textArea.getLineEndOffset(line);
            try {
                if(end == textArea.getLength() + 1){
                    end = end - 1;
                }
                textArea.getDocument().remove(start, end - start);

            } catch (BadLocationException e) {
            }

        }
    }

    public static class transpose_line implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            try {
                int line = textArea.getCaretLine();
                if(line != 0){
                    int start = textArea.getLineStartOffset(line);
                    int end = textArea.getLineEndOffset(line) - 1 ;
                    int prevStart = textArea.getLineStartOffset(line - 1);
                    int prevEnd = textArea.getLineEndOffset(line - 1) - 1;
                    String lineText = textArea.getLineText(line);
                    String prevLineText = textArea.getLineText(line - 1);
                    Document doc = textArea.getDocument();
                    doc.remove(start, end - start);
                    doc.insertString(start, prevLineText, null);
                    doc.remove(prevStart, prevEnd - prevStart);
                    doc.insertString(prevStart, lineText, null);
                }
            } catch (BadLocationException e) {
            }

        }
    }

    public static class dup_line implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            try {
                int line = textArea.getCaretLine();
                int start = textArea.getLineStartOffset(line);
                int end = textArea.getLineEndOffset(line);
                String lineText = textArea.getLineText(line);
                Document doc = textArea.getDocument();
                doc.insertString(end, lineText+"\n", null);
                textArea.setCaretPosition(textArea.getCaretPosition()+ (end - start));
            } catch (BadLocationException e) {
            }

        }
    }

    public static class upperLowerCase implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            int start = textArea.getSelectionStart();
            int end = textArea.getSelectionEnd();
            if (start != end){
                boolean isLower = true;
                char[] chars = textArea.getSelectedText().toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (Character.isLetter(chars[i]) && !Character.isLowerCase(chars[i])){
                        isLower = false;
                    }
                }
                if (isLower){
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = Character.toUpperCase(chars[i]);
                    }
                } else {
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = Character.toLowerCase(chars[i]);
                    }
                }
                textArea.setSelectedText(String.valueOf(chars));
                textArea.setSelectionEnd(end);
                textArea.setSelectionStart(start);
            }

        }
    }


    public static class comment_line implements ActionListener {
        String comment = "--";
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            comment = textArea.getComment();

            try {
                if (textArea.getSelectionStart() == textArea.getSelectionEnd()){
                    int line = textArea.getCaretLine();
                    int offset = textArea.getCaretPosition() - textArea.getLineStartOffset(line);
                    commentLine(textArea,line);

                    String nextText = textArea.getLineText(line + 1);
                    if (nextText != null) {
                        int nextLineStart = textArea.getLineStartOffset(line + 1);
                        int nextLineEnd = textArea.getLineEndOffset(line + 1);

                        if (offset > nextText.length()) {
                            textArea.setCaretPosition(nextLineEnd - 1);
                        } else {
                            textArea.setCaretPosition(nextLineStart + offset);
                        }
                    }
                } else {
                    int startLine = textArea.getLineOfOffset(textArea.getSelectionStart());
                    int endLine = textArea.getLineOfOffset(textArea.getSelectionEnd());
                    boolean mustComment = false;
                    for (int i = startLine; i <= endLine; i++) {
                        if (mustCommentLine(textArea, i)){
                            mustComment = true;
                        }
                    }

                    for (int i = startLine; i <= endLine; i++) {
                        commentLine(textArea, i, mustComment);
                    }
//                    textArea.setCaretPosition(textArea.getSelectionEnd());
                }
            } catch (BadLocationException e) {
            }

        }

        public void commentLine(JEditTextArea textArea,int line) throws BadLocationException {
            int start = textArea.getLineStartOffset(line);
            String lineText = textArea.getLineText(line);

            if (lineText.trim().startsWith(comment)) {
                // we have to take it out
                int index = lineText.indexOf(comment);
                Document doc = textArea.getDocument();
                doc.remove(start + index, 2);

            } else {
                // we have to comment it
                Document doc = textArea.getDocument();
                doc.insertString(start, comment, null);
            }
        }

        public void commentLine(JEditTextArea textArea, int line, boolean mustComment) throws BadLocationException {
            int start = textArea.getLineStartOffset(line);
            String lineText = textArea.getLineText(line);

            if (!mustComment) {
                // we have to take it out
                int index = lineText.indexOf(comment);
                Document doc = textArea.getDocument();
                doc.remove(start + index, 2);

            } else {
                // we have to comment it
                Document doc = textArea.getDocument();
                doc.insertString(start, comment, null);
            }
        }

        public boolean mustCommentLine(JEditTextArea textArea, int line) throws BadLocationException {
            String lineText = textArea.getLineText(line);
            if (lineText.trim().startsWith(comment)) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static class comment_all implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);

            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            try {
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();
                if (start == end) {
                    Document doc = textArea.getDocument();
                    doc.insertString(start, "/**/", null);
                } else {
                    Document doc = textArea.getDocument();
                    doc.insertString(start, "/*", null);
                    doc.insertString(end+2, "*/", null);
                }
            } catch (BadLocationException e) {
            }
        }
    }


    public static class select_all implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            textArea.selectAll();
            textArea.grabFocus();
            textArea.requestFocus();
        }
    }



    public static class find_Next implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (findReplace != null){
                JEditTextArea textArea = getTextArea(evt);
                if (!findReplace.processFindNext()){
                    JToolTip tip = new JToolTip();
                    tip.setTipText(findReplace.getMessage());
                    popup = new JPopupMenu();
                    popup.setBorderPainted(false);
                    popup.add(tip);
                    popup.pack();
                    Point location = null;
                    try {
                        location = TextUtilities.caretPositionToPoint(textArea);
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                    popup.setLocation(location);
                    popup.setVisible(true);

                }
            }
        }
    }

    public static class find implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            findReplace = FindReplace.getSharedInstance(getMainFrame());
            findReplace.showFind(textArea);
        }
    }

    public static class replace implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            findReplace = FindReplace.getSharedInstance(getMainFrame());
            findReplace.showReplace(textArea);

        }
    }

    private static Frame getMainFrame() {
        return WorkbenchPanelHelperManager.getInstance().getWorkbenchPanelHelper().getMainFrame();
    }

    public static class format_text implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            String text = textArea.getSelectedText();
            if (TextUtils.empty(text)) {
                return;
            }

            String formattedSQL = null;
            try {
                formattedSQL = SQLFormatter.format(text);
                textArea.replaceSelection(formattedSQL);
            } catch (Exception e) {
            }
        }
    }

    public static class indent_on_enter implements ActionListener {


        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            textArea.beginCompoundEdit();
            textArea.setSelectedText("\n");
            if (textArea.getEnterIndent()) {
                Indent.indent(textArea, textArea.getCaretLine(), true, false,
                        textArea.getIndentOpenBrackets(), textArea.getIndentCloseBrackets());
            }
            textArea.endCompoundEdit();
        }
    }

    public static class left_indent implements ActionListener {


        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            Document doc = textArea.getDocument();
            textArea.beginCompoundEdit();
            try {
                int tabSize = textArea.getTabSize();
                boolean noTabs = textArea.getSoftTab();
                Element map = textArea.getDocument().getDefaultRootElement();
                int start = map.getElementIndex(textArea.getSelectionStart());
                int end = map.getElementIndex(textArea.getSelectionEnd());
                for (int i = start; i <= end; i++) {
                    Element lineElement = map.getElement(i);
                    int lineStart = lineElement.getStartOffset();
                    String line = doc.getText(lineStart, lineElement.getEndOffset() - lineStart - 1);
                    int whiteSpace = Utilities.getLeadingWhiteSpace(line);
                    if (whiteSpace == 0) continue;
                    int whiteSpaceWidth = Math.max(0, Utilities.getLeadingWhiteSpaceWidth(line, tabSize)
                            - tabSize);
                    doc.remove(lineStart, whiteSpace);
                    doc.insertString(lineStart, Utilities.createWhiteSpace(whiteSpaceWidth,
                            (noTabs ? 0 : tabSize)), null);
                }
            } catch (BadLocationException ble) {
//                ble.printStackTrace();
            }
            textArea.endCompoundEdit();
        }
    }

    public static class right_indent implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            Document doc = textArea.getDocument();
            textArea.beginCompoundEdit();
            try {
                int tabSize = textArea.getTabSize();
                boolean noTabs = textArea.getSoftTab();
                Element map = textArea.getDocument().getDefaultRootElement();
                int start = map.getElementIndex(textArea.getSelectionStart());
                int end = map.getElementIndex(textArea.getSelectionEnd());
                for (int i = start; i <= end; i++) {
                    Element lineElement = map.getElement(i);
                    int lineStart = lineElement.getStartOffset();
                    String line = doc.getText(lineStart, lineElement.getEndOffset() - lineStart - 1);
                    int whiteSpace = Utilities.getLeadingWhiteSpace(line);
                    int whiteSpaceWidth = Utilities.getLeadingWhiteSpaceWidth(line, tabSize) + tabSize;
                    doc.remove(lineStart, whiteSpace);
                    doc.insertString(lineStart, Utilities.createWhiteSpace(whiteSpaceWidth,
                            (noTabs ? 0 : tabSize)), null);
                }
            } catch (BadLocationException ble) {
//                ble.printStackTrace();
            }
            textArea.endCompoundEdit();
        }
    }

    public static class indent_on_tab implements ActionListener {


        public void actionPerformed(ActionEvent evt) {
            JEditTextArea textArea = getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }

            Document doc = textArea.getDocument();
            Element map = doc.getDefaultRootElement();
            int start = map.getElementIndex(textArea.getSelectionStart());
            int end = map.getElementIndex(textArea.getSelectionEnd());

            if (end - start != 0) {
                RIGHT_INDENT.actionPerformed(evt);
                return;
            }

            textArea.beginCompoundEdit();
            int len;
            int tabSize = textArea.getTabSize();
            int currLine = textArea.getCaretLine();

            if (true) {// todo check this
                try {
                    Element lineElement = map.getElement(currLine);
                    int off = Utilities.getRealLength(doc.getText(lineElement.getStartOffset(),
                            textArea.getCaretPosition() - lineElement.getStartOffset()),
                            tabSize);
                    len = tabSize - (off % tabSize);
                } catch (BadLocationException ble) {
                    len = tabSize;
                }
            } else
                len = tabSize;

            if (textArea.getTabIndent()) {
                if (!Indent.indent(textArea, currLine, true, false, textArea.getIndentOpenBrackets(), textArea.getIndentCloseBrackets())){
                    textArea.setSelectedText(Utilities.createWhiteSpace(len, textArea.getSoftTab() ? 0 : tabSize));
                }
            } else {
                textArea.setSelectedText(Utilities.createWhiteSpace(len, textArea.getSoftTab() ? 0 : tabSize));
            }
            textArea.endCompoundEdit();
        }
    }
}

