
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


import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import za.co.hemtech.gui.Icons;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.border.*;


public class FindReplace extends JDialog implements ActionListener {
    private final static int TEXT_FIELD_SIZE = 20;
    private final static int COMPONENT_GAP = 10;
    private final static Border EMPTY_BORDER = new EmptyBorder(5, 5, 5, 5);

    //  Shared instance of this class

    private static FindReplace sharedFindReplace;
//    the replace panel
    private JPanel replaceButtonPanel;
    private JPanel questionPanel;
    private JPanel mainReplacePanel;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JButton skipButton;
    private JLabel questionLable = new JLabel(
            "Do you want to replace this occurrence?",
            Icons.getIcon("Question.png"),
            SwingConstants.CENTER);

    //  Visible components on the dialog

    private JPanel mainFindReplacepanel;
    private JLabel findLabel;
    private JLabel replaceLabel;
    private JTextField findData;
    private Document doc;
    private JTextField replaceData;
    private JCheckBox matchCase;
    private JCheckBox matchWord;
    private JRadioButton backward;
    private JRadioButton forward;
    private JRadioButton global;
    private JRadioButton selectedText;
    private JRadioButton fromCursor;
    private JRadioButton entireScope;
    private JButton findNextButton;
    private JButton closeButton;
    private JButton cancelButton;

    private JPanel findPanel;
    private JPanel replacePanel;
    private JPanel optionPanel;
    private JPanel allOptionPanel;
    private JPanel directionPanel;
    private JPanel scopePanel;
    private JPanel originPanel;
    private JPanel commandPanel;

    //  Component to search
    private JEditTextArea textComponent;

    //  Starting position of search
    private Position searchStartPosition;
    //  Ending position of search
    private Position searchEndPosition;


    //  Has the search wrapped
    private boolean searchWrap;
    private boolean found;
    private boolean init_next;
    private String message;
    private String text;
    boolean currentState;


    public FindReplace(Frame owner) {
        super(owner, true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setResizable(false);
        setName("findAndReplace");

        //  Create find panel
        doc = new PlainDocument();
        findData = new JTextField(doc,"", TEXT_FIELD_SIZE);
        findData.setMaximumSize(findData.getPreferredSize());

        findLabel = new JLabel("Text to find:");
        findLabel.setDisplayedMnemonic('T');
        findLabel.setLabelFor(findData);

        findPanel = new JPanel();
        findPanel.setBorder(EMPTY_BORDER);
        findPanel.setLayout(new BoxLayout(findPanel, BoxLayout.X_AXIS));
        findPanel.add(findLabel);
        findPanel.add(Box.createHorizontalGlue());
        findPanel.add(Box.createHorizontalStrut(COMPONENT_GAP));
        findPanel.add(findData);

        //  Create replace panel

        replaceData = new JTextField(TEXT_FIELD_SIZE);
        replaceData.setMaximumSize(findData.getPreferredSize());

        replaceLabel = new JLabel("Replace with:");
        replaceLabel.setDisplayedMnemonic('R');
        replaceLabel.setLabelFor(replaceData);

        replacePanel = new JPanel();
        replacePanel.setBorder(EMPTY_BORDER);
        replacePanel.setLayout(new BoxLayout(replacePanel, BoxLayout.X_AXIS));
        replacePanel.add(replaceLabel);
        replacePanel.add(Box.createHorizontalGlue());
        replacePanel.add(Box.createHorizontalStrut(COMPONENT_GAP));
        replacePanel.add(replaceData);

        //  Create options panel

        JPanel matchPanel = new JPanel();
        matchPanel.setLayout(new GridLayout(2, 1));
        matchCase = new JCheckBox("Case sensitive");
        matchCase.setMnemonic('C');
        matchWord = new JCheckBox("Whole words only");
        matchWord.setMnemonic('W');
        matchPanel.add(matchCase);
        matchPanel.add(matchWord);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayout(2, 1));
        forward = new JRadioButton("Forward");
        forward.setMnemonic('O');
        forward.setSelected(true);
        backward = new JRadioButton("Backward");
        backward.setMnemonic('B');
        searchPanel.add(forward);
        searchPanel.add(backward);

        ButtonGroup searchGroup = new ButtonGroup();
        searchGroup.add(forward);
        searchGroup.add(backward);

        JPanel scopesPanel = new JPanel();
        scopesPanel.setLayout(new GridLayout(2, 1));
        global = new JRadioButton("Global");
        global.setMnemonic('G');
        global.setSelected(true);
        selectedText = new JRadioButton("Selected text");
        selectedText.setMnemonic('S');
        scopesPanel.add(global);
        scopesPanel.add(selectedText);

        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(global);
        scopeGroup.add(selectedText);

        JPanel orignPanel = new JPanel();
        orignPanel.setLayout(new GridLayout(2, 1));
        fromCursor = new JRadioButton("From cursor");
        fromCursor.setMnemonic('M');
        entireScope = new JRadioButton("Entire scope");
        entireScope.setMnemonic('N');
        entireScope.setSelected(true);
        orignPanel.add(fromCursor);
        orignPanel.add(entireScope);

        ButtonGroup orignGroup = new ButtonGroup();
        orignGroup.add(fromCursor);
        orignGroup.add(entireScope);


        optionPanel = new JPanel();
        optionPanel.setLayout(new GridLayout(1, 1));
        optionPanel.setBorder(new TitledBorder("Options"));
        optionPanel.add(matchPanel);

        directionPanel = new JPanel();
        directionPanel.setLayout(new GridLayout(1, 1));
        directionPanel.setBorder(new TitledBorder("Direction"));
        directionPanel.add(searchPanel);

        scopePanel = new JPanel();
        scopePanel.setLayout(new GridLayout(1, 1));
        scopePanel.setBorder(new TitledBorder("Scope"));
        scopePanel.add(scopesPanel);

        originPanel = new JPanel();
        originPanel.setLayout(new GridLayout(1, 1));
        originPanel.setBorder(new TitledBorder("Origin"));
        originPanel.add(orignPanel);

        allOptionPanel = new JPanel();
        allOptionPanel.setLayout(new GridLayout(2, 2));
        allOptionPanel.setBorder(EMPTY_BORDER);
        allOptionPanel.add(optionPanel);
        allOptionPanel.add(directionPanel);
        allOptionPanel.add(scopePanel);
        allOptionPanel.add(originPanel);


//        optionPanel.add(searchPanel);

        //  Create command panel

        commandPanel = new JPanel();
        findNextButton = new JButton("Find", Icons.getIcon("Zoom16.gif"));
        findNextButton.setMnemonic('F');
        findNextButton.setEnabled(false);
        findNextButton.addActionListener(this);
        commandPanel.add(findNextButton);

        closeButton = createButton(commandPanel, "Cancel", ' ', true);

        //  Layout all the panels to build the dialog

        mainFindReplacepanel = new JPanel();
        mainFindReplacepanel.setLayout(new BoxLayout(mainFindReplacepanel, BoxLayout.Y_AXIS));
        mainFindReplacepanel.setBorder(EMPTY_BORDER);

        replaceButtonPanel = new JPanel();

        replaceButton = createButton(replaceButtonPanel, "Replace", 'R', true);
        skipButton = createButton(replaceButtonPanel, "Skip", 'S', true);
        replaceAllButton = createButton(replaceButtonPanel, "All", 'a', true);
        cancelButton = createButton(replaceButtonPanel, "Cancel", ' ', true);

        getRootPane().setDefaultButton(replaceButton);

        FocusAdapter resetReplaceButton = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                getRootPane().setDefaultButton(replaceButton);
            }
        };

        replaceButton.addFocusListener(resetReplaceButton);
        replaceAllButton.addFocusListener(resetReplaceButton);

        replaceButtonPanel.setLayout(new GridLayout(1, 4));
        replaceButtonPanel.setBorder(EMPTY_BORDER);

        questionPanel = new JPanel();
        questionPanel.setLayout(new GridLayout(1, 1));
        questionPanel.setBorder(EMPTY_BORDER);
        questionPanel.add(questionLable);


        mainReplacePanel = new JPanel();
        mainReplacePanel.setLayout(new GridLayout(2, 1));
        mainReplacePanel.setBorder(EMPTY_BORDER);

        mainReplacePanel.add(questionPanel);
        mainReplacePanel.add(replaceButtonPanel);

        mainFindReplacepanel.add(findPanel);
        mainFindReplacepanel.add(replacePanel);
        mainFindReplacepanel.add(allOptionPanel);
        mainFindReplacepanel.add(commandPanel);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(mainFindReplacepanel);
        panel.add(mainReplacePanel);

        mainReplacePanel.setVisible(false);

        getContentPane().add(panel);

        //  Set the default button for the dialog

        getRootPane().setDefaultButton(findNextButton);

        FocusAdapter resetDefaultButton = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                getRootPane().setDefaultButton(findNextButton);
            }
        };


        closeButton.addFocusListener(resetDefaultButton);

        //  Enable/Disable buttons on the command panel as find text is entered/deleted
        doc.addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e) {
                updateState(doc);
            }

            public void removeUpdate(DocumentEvent e) {
                updateState(doc);
            }

            public void changedUpdate(DocumentEvent e) {
                updateState(doc);
            }

        });

        //  Handle escape key

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                processClose();

            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

    }

    private void updateState(Document doc) {
        boolean state = (doc.getLength() > 0);
        if (state != currentState) {
            findNextButton.setEnabled(state);

            currentState = state;
        }
    }

    public static FindReplace getSharedInstance(Frame owner) {
        if (sharedFindReplace == null) {
            sharedFindReplace = new FindReplace(owner);
        }
        return sharedFindReplace;
    }


    public void showFind(JEditTextArea textComponent) {
        setTitle("Find");
        setTextComponent(textComponent);
        showReplace(false);
        showReplaceComponents(false);
        pack();
        Point p = this.getLocation();
        if (p.x == 0 && p.y == 0) {
            Point point = textComponent.getLocationOnScreen();
            Rectangle rec = textComponent.getVisibleRect();
            int x = ((((int) rec.getMaxX()) - this.getWidth()) + (int) point.getX());
            this.setLocation(x - 20, (int) point.getY() + 5);
        }

        findData.requestFocus();

        String selText = textComponent.getSelectedText();
        if (selText != null && !selText.trim().equals("")){
            findData.setText(selText);
            findData.selectAll();
        } else if (text != null) {
            findData.setText(text);
            findData.selectAll();
        }
        setVisible(true);


    }



    public void showReplace(JEditTextArea textComponent) {
        setTitle("Find and Replace");
        setTextComponent(textComponent);
        showReplace(false);
        showReplaceComponents(true);
        pack();
        Point p = this.getLocation();
        if (p.x == 0 && p.y == 0) {
            Point point = textComponent.getLocationOnScreen();
            Rectangle rec = textComponent.getVisibleRect();
            int x = ((((int) rec.getMaxX()) - this.getWidth()) + (int) point.getX());
            this.setLocation(x, (int) point.getY());
        }

        findData.requestFocus();
        String selText = textComponent.getSelectedText();
        if (selText != null && !selText.trim().equals("")) {
            findData.setText(selText);
            findData.selectAll();
        } else if (text != null) {
            findData.setText(text);
            findData.selectAll();
        }
        setVisible(true);
    }

    private void showReplaceComponents(boolean value) {
        replacePanel.setVisible(value);
    }

    private void showReplace(boolean value) {
        mainFindReplacepanel.setVisible(!value);
        mainReplacePanel.setVisible(value);

        if (value) {
            setTitle("Replace");
            getRootPane().setDefaultButton(replaceButton);
        } else {
            getRootPane().setDefaultButton(findNextButton);
        }

        pack();

        if (value) {
            replaceButton.requestFocus();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == findNextButton) {
            text = findData.getText();
            findData.setText("");
            if (replacePanel.isVisible()) {
                int pos = textComponent.getCaretPosition();
                resetSearchVariables();

                if (!hasFound()) {
                    processClose();
                    textComponent.setCaretPosition(pos);
                    textComponent.findNext(textComponent);
                } else {
                    processFindNext();
                    showReplace(true);
                }
            } else {
                int pos = textComponent.getCaretPosition();
                resetSearchVariables();
                processClose();
                if (!hasFound()) {
                    textComponent.setCaretPosition(pos);
                }
                textComponent.findNext(textComponent);
            }
        }

        if (o == skipButton) {
            if (!processFindNext()) {
                processClose();
            }
        }

        if (o == replaceButton) {
            if (!processReplace()) {
                processClose();
            }
        }

        if (o == replaceAllButton) {
            processReplaceAll();
            processClose();
        }

        if (o == closeButton || o == cancelButton) {
            processClose();
        }
    }

    private JButton createButton(JPanel panel, String label, char mnemonic, boolean enabled) {
        JButton button = new JButton(label);
        button.setMnemonic(mnemonic);
        button.setEnabled(enabled);
        button.addActionListener(this);
        panel.add(button);

        return button;
    }

    private boolean hasFound() {
        //  Set variables required for the search

        String needle = text;
        String haystack = textComponent.getText();
        int offset = textComponent.getSelectionStart();
        String selectedText = textComponent.getSelectedText();
//        textComponent.setSelectionEnd(offset); // deselect text

        //  Simplify search when you don't care about case

        if (!matchCase.isSelected()) {
            haystack = haystack.toLowerCase();
            needle = needle.toLowerCase();
        }

        //  When text is already selected,
        //  change offset so we don't find the same text again

        if (needle.equalsIgnoreCase(selectedText)) {
            if (searchDown()) {
                offset++;
            } else {
                offset--;
            }
        }

        //  Search for the string and show the result

        int result = searchFor(needle, haystack, offset);

        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    public boolean processFindNext() {
        if (init_next == true) {
            resetSearchVariablesToStart();
        }

        //  Set variables required for the search

        String needle = text;
        String haystack = textComponent.getText();
        int offset = textComponent.getSelectionStart();
        String selectedText = textComponent.getSelectedText();
//        textComponent.setSelectionEnd(offset); // deselect text

        //  Simplify search when you don't care about case

        if (!matchCase.isSelected()) {
            haystack = haystack.toLowerCase();
            needle = needle.toLowerCase();
        }

        //  When text is already selected,
        //  change offset so we don't find the same text again

        if (needle.equalsIgnoreCase(selectedText))
            if (searchDown())
                offset++;
            else
                offset--;

        //  Search for the string and show the result

        int result = searchFor(needle, haystack, offset);

        if (result == -1) {
            if (found) {
                message = "\"" + text + "\" not found, press F3 to search from the " +
                        (searchDown() ? "top" : "bottom");
                init_next = true;

            } else {
                if (!searchFromCursor()) {
                    message = "\"" + text + "\" not found";
                } else {
                    message = "\"" + text + "\" not found, press F3 to search from the " +
                            (searchDown() ? "top" : "bottom");
                    init_next = true;
                }
            }
            return false;
        } else {
            found = true;
            textComponent.setCaretPosition(result + needle.length());
            textComponent.setSelectionStart(result);
            textComponent.setSelectionEnd(result + needle.length());

            textComponent.requestFocus();
            return true;
        }
    }

    public String getMessage() {
        return message;
    }

    private boolean searchDown() {
        return forward.isSelected();
    }

    private boolean searchGlobal() {
        return global.isSelected();
    }

    private boolean searchFromCursor() {
        return fromCursor.isSelected();
    }


    private int searchFor(String needle, String haystack, int offset) {

        if (searchDown()) {                                // 10 < 20
            if (offset > searchEndPosition.getOffset() || offset < searchStartPosition.getOffset()) {
                return -1;
            }
        } else {
            if (offset < searchEndPosition.getOffset() || offset > searchStartPosition.getOffset()) {
                return -1;
            }
        }

        int result;
        int wrapSearchOffset;

        if (searchDown()) {
            wrapSearchOffset = 0;
            result = haystack.indexOf(needle, offset);
        } else {
            wrapSearchOffset = haystack.length();
            result = haystack.lastIndexOf(needle, offset);
        }

        //  String not found,
        //  Attempt to wrap and continue the search

        if (result == -1) {
//            return result;
            if (searchWrap) {
                return result;
            } else {
                searchWrap = true;
                return searchFor(needle, haystack, wrapSearchOffset);
            }
        }

        //  String was found,
        //  Make sure we haven't wrapped and passed the original start position

        int wrapResult;

        if (searchDown()) {
            wrapResult = result - searchStartPosition.getOffset();
        } else {
            wrapResult = searchStartPosition.getOffset() - result - 1;
        }


        if (searchWrap && (wrapResult >= 0)) {
            return -1;
        }

        //  String was found,
        //  see if it is a word

        if (matchWord.isSelected() && !isWord(haystack, result, needle.length()))
            if (searchDown())
                return searchFor(needle, haystack, result + 1);
            else
                return searchFor(needle, haystack, result - 1);

        //  The search was successfull

        return result;
    }

    private boolean isWord(String haystack, int offset, int length) {
        int leftSide = offset - 1;
        int rightSide = offset + length;

        if (isDelimiter(haystack, leftSide) && isDelimiter(haystack, rightSide))
            return true;
        else
            return false;
    }

    private boolean isDelimiter(String haystack, int offset) {
        if ((offset < 0) || (offset > haystack.length())) {
            return true;
        }
        if (Character.isLetterOrDigit(haystack.charAt(offset)) || haystack.charAt(offset) == '_') {
            return false;
        } else {
            return true;
        }
    }

    public boolean processReplace() {
        String needle = text;
        String replaceText = replaceData.getText();
        String selectedText = textComponent.getSelectedText();

        if (matchCase.isSelected() && needle.equals(selectedText)) {
            textComponent.replaceSelection(replaceText);
        }

        if (!matchCase.isSelected() && needle.equalsIgnoreCase(selectedText)) {
            textComponent.replaceSelection(replaceText);
        }

        return processFindNext();
    }

    public void processReplaceAll() {
        JViewport viewport = null;
        Point point = null;

        resetSearchVariables();

        Container c = textComponent.getParent();

        if (c instanceof JViewport) {
            viewport = (JViewport) c;
            point = viewport.getViewPosition();
        }

        while (processReplace()) ;

        if (c instanceof JViewport) {
            viewport.setViewPosition(point);
        }
    }


    private void processClose() {
        setVisible(false);
    }

    private void setTextComponent(JEditTextArea textComponent) {
        if (this.textComponent != textComponent) {
            this.textComponent = textComponent;
            this.textComponent.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        }
    }

    public JEditTextArea getTextComponent() {
        return this.textComponent;
    }

    private void resetSearchVariables() {
        try {
            init_next = false;
            found = false;
            searchWrap = false;
            if (searchGlobal()) {
                if (searchFromCursor()) {
                    searchStartPosition = textComponent.getDocument().createPosition(textComponent.getSelectionEnd());
                    if (searchDown()) {
                        searchEndPosition = textComponent.getDocument().createPosition(textComponent.getDocumentLength());
                    } else {
                        searchEndPosition = textComponent.getDocument().createPosition(0);
                    }
                } else {
                    if (searchDown()) {
                        searchStartPosition = textComponent.getDocument().createPosition(0);
                        textComponent.setCaretPosition(0);
                        searchEndPosition = textComponent.getDocument().createPosition(textComponent.getDocumentLength());
                    } else {
                        searchStartPosition = textComponent.getDocument().createPosition(textComponent.getDocumentLength());
                        textComponent.setCaretPosition(textComponent.getDocumentLength());
                        searchEndPosition = textComponent.getDocument().createPosition(0);
                    }
                }
            } else {
                if (searchDown()) {
                    searchStartPosition = textComponent.getDocument().createPosition(textComponent.getSelectionStart());
                    searchEndPosition = textComponent.getDocument().createPosition(textComponent.getSelectionEnd());
                } else {
                    searchStartPosition = textComponent.getDocument().createPosition(textComponent.getSelectionEnd());
                    searchEndPosition = textComponent.getDocument().createPosition(textComponent.getSelectionStart());
                }

            }
        } catch (BadLocationException e) {
//            System.out.println(e);
        }
    }

    private void resetSearchVariablesToStart() {
        try {
            init_next = false;
            found = false;
            searchWrap = false;
            if (searchDown()) {
                searchStartPosition = textComponent.getDocument().createPosition(0);
                textComponent.setCaretPosition(0);
                searchEndPosition = textComponent.getDocument().createPosition(textComponent.getDocumentLength());
            } else {
                searchStartPosition = textComponent.getDocument().createPosition(textComponent.getDocumentLength());
                textComponent.setCaretPosition(textComponent.getDocumentLength());
                searchEndPosition = textComponent.getDocument().createPosition(0);
            }
        } catch (BadLocationException e) {
//            System.out.println(e);
        }
    }

}


