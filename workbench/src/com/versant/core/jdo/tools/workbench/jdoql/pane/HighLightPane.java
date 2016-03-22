
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
package com.versant.core.jdo.tools.workbench.jdoql.pane;

import com.versant.core.jdo.tools.workbench.jdoql.lexer.Lexer;
import com.versant.core.jdo.tools.workbench.jdoql.lexer.Token;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @keep-all
 */
public class HighLightPane extends JTextPane {

    private boolean wrap = false;
    /**
     * the styled document that is the model for
     * the textPane
     */
    protected HighLightedDocument document;
    /**
     * A reader wrapped around the document
     * so that the document can be fed into
     * the lexer.
     */
    protected DocumentReader documentReader;
    /**
     * The lexer that tells us what colors different
     * words should be.
     */
    protected Lexer syntaxLexer;

    /**
     * A hash table containing the text styles.
     * Simple attribute sets are hashed by name (String)
     */
    private HashMap styles;

    private Highlighter highlighter;

//    private ArrayList tokenList = new ArrayList();

    private RedHighlightPainter painter;

    private Object highlightTag;

//    private Timer highlightTimer;

    private int defaultTabSize = 4;

    //undo helpers
    Hashtable actions;
//    protected UndoAction undoAction = new UndoAction();
//    protected RedoAction redoAction = new RedoAction();
//    protected UndoManager undo = new UndoManager();

    public HighLightPane() {
        super();
        this.document = new HighLightedDocument();
        setStyledDocument(document);
        this.documentReader = new DocumentReader(document);
        this.highlighter = this.getHighlighter();
        this.painter = new RedHighlightPainter(Color.red);


//        highlightTimer = new Timer(300,new ActionListener(){
//            /**
//             * Invoked when an action occurs.
//             */
//            public void actionPerformed(ActionEvent e) {
//                doHighlight();
//            }
//        });
//        highlightTimer.setRepeats(false);
//        addKeymapBindings();
//        document.addUndoableEditListener(new MyUndoableEditListener());
    }

    public void setHighlightStyles(HashMap styles) {
        this.styles = styles;
        setTabSize(defaultTabSize);
    }

    public void setLexer(Lexer syntaxLexer) {
        this.syntaxLexer = syntaxLexer;
        this.syntaxLexer.setReader(documentReader);
    }

    public Lexer getLexer() {
        return syntaxLexer;
    }

    public void setTabSize(int tabSize) {
        float fontSize = getFont().getSize2D();
        Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
        TabStop[] ts = new TabStop[256];
        for (int i = 0; i < 256; i++) {
            ts[i] = new TabStop(
                    tabSize * (fontSize + fontSize / tabSize) * (i + 1) / 2);
        }
        StyleConstants.setTabSet(defaultStyle, new TabSet(ts));
    }

    /**
     * retrieve the style for the given type of text.
     *
     * @param styleName the label for the type of text ("tag" for example)
     *                  or null if the styleName is not known.
     * @return the style
     */
    private SimpleAttributeSet getAttributeStyle(String styleName) {
        return ((SimpleAttributeSet)styles.get(styleName));
    }

    public void initLexer() throws IOException {
        syntaxLexer.reset(documentReader, 0, 0, 0);
        documentReader.seek(0);
    }

    public class RedHighlightPainter
            extends DefaultHighlighter.DefaultHighlightPainter {

        public RedHighlightPainter(Color c) {
            super(c);
        }

        public Shape paintLayer(Graphics g, int offs0, int offs1,
                Shape bounds, JTextComponent c, View view) {

            Rectangle b = bounds.getBounds();
            try {
                g.setColor(Color.red);
                Rectangle r1 = c.modelToView(offs0);
                Rectangle r2 = c.modelToView(offs1);
                g.drawLine((r1.x), (r1.y + r1.height) - 2, (r2.x),
                        (r2.y + r2.height) - 2);
                g.drawLine((r1.x), (r1.y + r1.height) - 1, (r2.x),
                        (r2.y + r2.height) - 1);
            } catch (BadLocationException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
            return b;
        }
    }

    public boolean isComment(int i) {
        try {
            Token t = null;
            initLexer();
            while (true) {
                t = syntaxLexer.getNextToken(true, false);

                if (t != null) {
                    if (i >= t.getCharBegin() && i <= t.getCharEnd()) {
                        return t.isComment();
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;

    }

    public void doColoring() {
        try {
            Token t = null;
//            highlightTimer.stop();
            try {
                highlighter.removeAllHighlights();
            } catch (NullPointerException e) {
                // we can get a NullPointerException if we changed the look and feel and did not restart
            }
//            tokenList.clear();
            initLexer();
            while (true) {
                t = syntaxLexer.getNextToken(true, false);
                if (t != null) {
//                    tokenList.add(t);

                    document.setCharacterAttributes(t.getCharBegin(),
                            t.getCharEnd() - t.getCharBegin(),
                            getAttributeStyle(t.getDescription()),
                            false);

                } else {

//                    highlightTimer.restart();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    private void doHighlight(){
//        try {
//            if (PopupInputHandler.isPopupShowing()){
//                return;
//            }
//
//            ListIterator iter = tokenList.listIterator();
//
//
//            highlighter.removeAllHighlights();
//            boolean stringOp = false;
//            boolean string = false;
//            boolean numOp = false;
//            boolean num = false;
//            boolean bool = false;
//            boolean expect = false;
//            JdoqlToken jt = null;
//            while (iter.hasNext()){
//                jt = (JdoqlToken)iter.next();
//                if (!jt.isComment()){
//                    if (jt.type == JdoqlToken.TYPE_PC){
//                        expect = true;
//                    } else if (jt.type == JdoqlToken.TYPE_DOT_SEPERATOR){
//                        expect = true;
//                    } else if (jt.type == JdoqlToken.TYPE_COLLECTION){
//                        expect = true;
//                    } else if (jt.type == JdoqlToken.TYPE_STRING){
//                        if (string && stringOp){
//                            stringOp = false;
//                            string = true;
//                            expect = false;
//                            bool = true;
//                        } else {
//                            string = true;
//                            expect = true;
//                        }
//                    } else if (jt.type == JdoqlToken.TYPE_BOOLEAN){
//                        expect = false;
//                        bool = true;
//                    } else if (jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_OBJECT){
//                        // we get the next token, it must be a (
//                        if (iter.hasNext()){
//                            jt = (JdoqlToken)iter.next();
//                            if (jt.type != JdoqlToken.TYPE_LEFT_SEPERATOR){
//                                highlightSyntax(jt);
//                            } else {
//                                if (iter.hasNext()){
//                                    JdoqlToken iffy = (JdoqlToken)iter.next();
//                                    if (iffy.type == JdoqlToken.TYPE_RIGHT_SEPERATOR){// there was nothing in the braket
//                                        highlightSyntax(jt);
//                                        highlightSyntax(iffy);
//                                    } else {
//                                        // go till we find the )
//                                        expect = true;
//                                        while(iter.hasNext()){
//                                            jt = (JdoqlToken)iter.next();
//                                            if (jt.type == JdoqlToken.TYPE_RIGHT_SEPERATOR){
//                                                expect = false;
//                                                bool = true;
//                                            }
//
//                                        }
//
//                                    }
//
//
//                                } else {
//                                    highlightSyntax(jt);
//                                }
//                            }
//                        } else{
//                            expect = false;
//                        }
//
//
//                    } else if (jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_STRING){
//                        // we get the next token, it must be a (
//                        if (iter.hasNext()){
//                            jt = (JdoqlToken)iter.next();
//                            if (jt.type != JdoqlToken.TYPE_LEFT_SEPERATOR){
//                                highlightSyntax(jt);
//                            } else {
//                                if (iter.hasNext()){
//                                    JdoqlToken iffy = (JdoqlToken)iter.next();
//                                    if (iffy.type == JdoqlToken.TYPE_RIGHT_SEPERATOR){// there was nothing in the braket
//                                        highlightSyntax(jt);
//                                        highlightSyntax(iffy);
//                                    } else {
//                                        // go till we find a String
//                                        iter.hasPrevious();
//                                        iter.previous();
//                                        expect = !getStringAndRightSep(iter);
//                                        if (!expect){bool = true;}
//                                    }
//
//
//                                } else {
//                                    highlightSyntax(jt);
//                                }
//                            }
//                        } else{
//                            expect = false;
//                        }
//                    } else if (jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_NONE){
//                        if (iter.hasNext()){
//                            jt = (JdoqlToken)iter.next();
//                            if (jt.type != JdoqlToken.TYPE_LEFT_SEPERATOR){
//                                highlightSyntax(jt);
//                            } else {
//                                if (iter.hasNext()){
//                                    JdoqlToken iffy = (JdoqlToken)iter.next();
//                                    if (iffy.type == JdoqlToken.TYPE_RIGHT_SEPERATOR){// there was nothing in the braket
//                                        expect = false;
//                                        bool = true;
//                                    } else {
//                                        highlightSyntax(iffy);
//                                    }
//                                } else {
//                                    highlightSyntax(jt);
//                                }
//                            }
//                        } else{
//                            expect = false;
//                        }
//
//                    } else if (jt.type == JdoqlToken.TYPE_NUMBER_OPERATOR || jt.getID() == JdoqlToken.OPERATOR_EQUAL){
//                        if (bool){
//                            highlightSyntax(jt);
//                        }
//
//                        if (num){
//                            numOp = true;
//                            expect = true;
//                        }
//
//                        if (jt.getID() == JdoqlToken.OPERATOR_EQUAL){
//                            if (string){
//                                stringOp = true;
//                                expect = true;
//                            }
//
//                        }
//                    } else if (jt.type == JdoqlToken.TYPE_STRING_NUMBER_OPERATOR){
//                        if (bool && string){
//                            expect = false;
//                        } else if (bool ){
//                            highlightSyntax(jt);
//                        }
//
//                    } else if (jt.type == JdoqlToken.TYPE_BOOLEAN_OPERATOR){
//                        bool = false;
//                        expect = true;
//                    } else if (jt.type == JdoqlToken.TYPE_NUMBER ){
//                        if (num && numOp){
//                            expect = false;
//                            num = false;
//                            numOp = false;
//                            bool = true;
//                        } else {
//                            num = true;
//                            expect = true;
//                        }
//                    }
//                }
//            }
//
//
//            if (expect)highlightSyntax(jt);
//        } catch (Exception e) {/* Hide all exceptions*/}
//    }
//
//    private boolean getStringAndRightSep(ListIterator iter)throws BadLocationException{
//        JdoqlToken jt = null;
//        while (iter.hasNext()){
//            jt = (JdoqlToken)iter.next();
//            if (jt.type == JdoqlToken.TYPE_STRING){
////                return true;
//                if (iter.hasNext()){
//                    JdoqlToken iffy = (JdoqlToken)iter.next();
//                    if (iffy.type == JdoqlToken.TYPE_RIGHT_SEPERATOR){
//                        return true;
//                    } else if (iffy.type == JdoqlToken.TYPE_STRING_NUMBER_OPERATOR) {
//                        continue;
//                    }
//                } else {
//                    //there is nothing after the string
//                    highlightSyntax(jt);
//                    return false;
//                }
//
//            } else if (jt.type == JdoqlToken.TYPE_ALL_OPERATOR ||
//                    jt.type == JdoqlToken.TYPE_ARRAY ||
//                    jt.type == JdoqlToken.TYPE_BOOLEAN ||
//                    jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_NONE ||
//                    jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_OBJECT ||
//                    jt.type == JdoqlToken.TYPE_BOOLEAN_METHOD_STRING ||
//                    jt.type == JdoqlToken.TYPE_COLLECTION ||
//                    jt.type == JdoqlToken.TYPE_NUMBER ||
//                    jt.type == JdoqlToken.TYPE_NUMBER_OPERATOR ||
//                    jt.type == JdoqlToken.TYPE_RIGHT_SEPERATOR ||
//                    jt.type == JdoqlToken.TYPE_BOOLEAN_OPERATOR ){
//                highlightSyntax(jt);
//                return false;
//
//            }
//        }
//        return false;
//    }



//    private void highlightSyntax(Token t) throws BadLocationException{
//        highlighter.addHighlight(t.getCharBegin(),t.getCharEnd(), painter);
//    }

    public void highlightError(int errorlineNum) throws BadLocationException {
        if (errorlineNum == -1) {
            return;
        }
        char[] chars = getText().toCharArray();
        int lineNum = 1;
        boolean haveStart = false;
        int start = 0;
        int end = chars.length;
        for (int i = 0; i < end; i++) {
            if (!haveStart) {
                if (lineNum == errorlineNum) {
                    if (chars[i] != '\t' && chars[i] != ' ') {
                        start = i;
                        haveStart = true;
                    }
                }
            }
            if (!haveStart) {
                if (chars[i] == '\n') {
                    lineNum++;
                }
            } else {
                if (chars[i] == '\n') {
                    end = i;
                }
            }
        }
        highlightTag = highlighter.addHighlight(start, end, painter);
    }

    public void removeHighlightError() {
        if (highlightTag != null) {
            highlighter.removeHighlight(highlightTag);
            highlightTag = null;
        }
    }

    /**
     * Just like a DefaultStyledDocument but intercepts inserts and
     * removes to color them.
     */
    private class HighLightedDocument extends DefaultStyledDocument {

        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {

            if (str.equals("\n")) {
                int tabCount = 0;
                int start = getLineStartOffset(getCaretPosition());
                int end = getLineEndOffset(getCaretPosition());
                String s = getText(start, end - start);
                if (s.indexOf("{") != -1 && s.indexOf("{") > s.indexOf("}")) {
                    tabCount = 1;
                }
                while (s.startsWith("\t")) {
                    tabCount++;
                    s = s.substring(1, s.length());
                }
                String tabs = "\n";
                for (int i = 0; i < tabCount; i++) {
                    tabs += "\t";
                }
                str = tabs;
            } else if (str.equals("}")) {
                int tabCount = 0;
                int start = getLineStartOffset(getCaretPosition());
                int end = getLineEndOffset(getCaretPosition());
                String s = getText(start, end - start);
                char[] chars = s.toCharArray();
                boolean leave = false;
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] != '\t') {
                        leave = true;
                    }
                }
                if (!leave && (start > 2) && (chars.length != 0)) {
                    int lastStart = getLineStartOffset(start - 1);
                    int lastEnd = getLineEndOffset(start - 1);
                    s = getText(lastStart, lastEnd - lastStart);
                    while (s.startsWith("\t")) {
                        tabCount++;
                        s = s.substring(1, s.length());
                    }
                    if (tabCount == chars.length) {
                        super.remove(getCaretPosition() - 1, 1);
                        offs--;
                    }
                }
            }

            super.insertString(offs, str, a);
            int lenght = str.length();
            doColoring();
            documentReader.update(offs, lenght);
        }

        public void remove(int offs, int len) throws BadLocationException {
            super.remove(offs, len);
            doColoring();
            documentReader.update(offs, -len);
        }

        public int getLineEndOffset(int n) {
            try {
                String s = getDocument().getText(0, getDocument().getLength());
                int end = s.indexOf('\n', n);
                if (end == -1) {
                    end = s.length();
                }
                return end;
            } catch (Exception e) {
            }
            return 0;
        }

        public int getLineStartOffset(int n) {
            try {
                String s = getDocument().getText(0, getDocument().getLength());
                int start;
                if (n == 0) {
                    start = 0;
                } else {
                    start = s.lastIndexOf('\n', n - 1);
                    if (start == -1) return 0;
                    start++;
                }
                return start;
            } catch (Exception e) {
            }
            return 0;
        }
    }

    public int getFontSize() {
        return getFont().getSize();
    }

    /**
     * sets word wrap on or off.
     *
     * @param wrap whether the text editor pane should wrap or not
     */
    public void setWordWrap(boolean wrap) {
        this.wrap = wrap;
    }

    /**
     * returns whether the editor wraps text.
     *
     * @return the value of the word wrap property
     */
    public boolean getWordWrap() {
        return this.wrap;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap) {
            Component parent = this.getParent();
            ComponentUI ui = this.getUI();
            boolean bool = (parent != null)
                    ? (ui.getPreferredSize(this).width < parent.getSize().width)
                    : true;

            return bool;
        } else {
            return super.getScrollableTracksViewportWidth();
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        if (wrap) {
            super.setBounds(x, y, width, height);
        } else {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y, Math.max(size.width, width),
                    Math.max(size.height, height));
        }
    }




//
//    //This one listens for edits that can be undone.
//    protected class MyUndoableEditListener
//            implements UndoableEditListener {
//        public void undoableEditHappened(UndoableEditEvent e) {
//            if (e.getEdit().getPresentationName().equals("style change")){
////                e.getEdit().die();
//            } else {
//                //Remember the edit and update the menus.
//                undo.addEdit(e.getEdit());
//                undoAction.updateUndoState();
//                redoAction.updateRedoState();
//            }
//        }
//    }
//
//
//    //Add a couple of emacs key bindings to the key map for navigation.
//    protected void addKeymapBindings() {
////        //Add a new key map to the keymap hierarchy.
//        Keymap keymap = addKeymap("UndoRedoBindings",
//                getKeymap());
//
//        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
//        keymap.addActionForKeyStroke(key, undoAction);
//
//        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK+Event.SHIFT_MASK);
//        keymap.addActionForKeyStroke(key, redoAction);
//        setKeymap(keymap);
//    }
//
//    class UndoAction extends AbstractAction {
//        public UndoAction() {
//            super("Undo");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            try {
//                undo.undo();
//            } catch (CannotUndoException ex) {
//                Toolkit.getDefaultToolkit().beep();
//            }
//            updateUndoState();
//            redoAction.updateRedoState();
//        }
//
//        protected void updateUndoState() {
//            if (undo.canUndo()) {
//                putValue(Action.NAME, undo.getUndoPresentationName());
//            } else {
//                putValue(Action.NAME, "Undo");
//            }
//        }
//    }
//
//    class RedoAction extends AbstractAction {
//        public RedoAction() {
//            super("Redo");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            try {
//
//                undo.redo();
//            } catch (CannotRedoException ex) {
//                Toolkit.getDefaultToolkit().beep();
//            }
//            updateRedoState();
//            undoAction.updateUndoState();
//        }
//
//        protected void updateRedoState() {
//            if (undo.canRedo()) {
//                putValue(Action.NAME, undo.getRedoPresentationName());
//            } else {
//                putValue(Action.NAME, "Redo");
//            }
//        }
//    }
//
//    public void startUndoRedo(){
//        undo.discardAllEdits();
//    }

    public Dimension getPreferredSize() {
        final Dimension preferredSize = super.getPreferredSize();
        preferredSize.height += 5;
        return preferredSize;
    }
}

