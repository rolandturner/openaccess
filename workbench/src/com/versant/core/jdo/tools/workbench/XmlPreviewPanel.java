
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
package com.versant.core.jdo.tools.workbench;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import za.co.hemtech.gui.FieldControl;
import za.co.hemtech.gui.Icons;
import za.co.hemtech.gui.model.FieldModel;
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import com.versant.core.jdo.tools.workbench.editor.XMLTokenMarker;
import com.versant.core.jdo.tools.workbench.model.MdDocument;
import com.versant.core.jdo.tools.workbench.model.MdElement;
import com.versant.core.jdo.tools.workbench.model.MdJDOMFactory;
import com.versant.core.jdo.tools.workbench.model.XmlUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.StringReader;

/**
 * Panel to display scrollable read only XML previews of JDom elements.
 * This can be hooked up to a model and column just like a FieldControl.
 * When used like a field control only one element is displated. If
 * setElements is called with two non-null elements then two elements
 * are displayed with a splitter between them.
 *
 * @see FieldControl
 */
public class XmlPreviewPanel extends JPanel implements ChangeListener {

    private TitleTextPanel textTop;
    private TitleTextPanel textBottom;
    private JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private Element top;
    private Element bottom;

    public static ProjectReloader reloader;

    protected FieldModel fieldModel = new FieldModel();

    public XmlPreviewPanel(String name, boolean editable) {
        super(new BorderLayout());
        setName(name);
        textTop = new TitleTextPanel(editable);
        textBottom = new TitleTextPanel(editable);
        add(textTop, BorderLayout.CENTER);
        fieldModel.setDefaultValue(null);
        fieldModel.addChangeListener(this);
    }

    /**
     * Set the elements displayed. If bottom is null then the splitter is
     * removed and only one element is displayed.
     */
    public void setElements(Element top, String titleTop, Element bottom,
            String titleBottom) {
        this.top = top;
        this.bottom = bottom;
        if (bottom != null) {
            if (splitter.getParent() != this) {
                splitter.setTopComponent(textTop);
                splitter.setBottomComponent(textBottom);
                removeAll();
                add(splitter, BorderLayout.CENTER);
                splitter.setDividerLocation(0.5);
            }
        } else {
            if (splitter.getParent() != null) {
                removeAll();
                add(textTop);
            }
        }
        textTop.setElement(top);
        textTop.setTitle(titleTop);
        textBottom.setElement(bottom);
        textBottom.setTitle(titleBottom);
    }

    public Element getTop() {
        return top;
    }

    public Element getBottom() {
        return bottom;
    }

    /**
     * Set our TableModel. If this is also a ColumnModel then this is set as
     * well.
     */
    public void setModel(TableModel model) {
        fieldModel.setModel(model);
    }

    public TableModel getModel() {
        return fieldModel.getModel();
    }

    /**
     * Set the name of our column. If this is null then the column from our
     * CursorModel is used.
     */
    public void setColumnName(String newColumnName) {
        fieldModel.setColumnName(newColumnName);
    }

    public String getColumnName() {
        return fieldModel.getColumnName();
    }

    public void stateChanged(ChangeEvent e) {
        Object o = fieldModel.getValue();
        if (o instanceof Element) {
            setElements((Element)o, null, null, null);
        } else {
            setElements(null, null, null, null);
        }
    }

    /**
     * A text area with a title on top.
     */
    private static class TitleTextPanel extends JPanel
            implements DocumentListener {

        private JLabel labTitle = new JLabel();
        private Element element = null;
        private JEditTextArea textArea = new JEditTextArea(){
            public void removeNotify() {
                checkChanges();
                super.removeNotify();
            }
            public void setVisible(boolean aFlag) {
                if(!aFlag) checkChanges();
                super.setVisible(aFlag);
            }
        };
        private JToolBar toolBar = new JToolBar();

        private Icon commitIcon = Icons.getIcon("Save16.gif");
        private Icon rollbackIcon = Icons.getIcon("Undo16.gif");
        private JButton commit = new JButton("Save", commitIcon);
        private JButton rollback = new JButton("Rollback", rollbackIcon);
        private XMLOutputter outputter = new XMLOutputter(XmlUtils.XML_FORMAT);
        private JPanel topPanel = new JPanel();

        private Border BORDER1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        private Border BORDER2 = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 0, 0, 0),
                BorderFactory.createLoweredBevelBorder());
        private boolean ignoreDocumentUpdate = false;
        private boolean isDirty = false;

        public TitleTextPanel(boolean editable) {
            super(new BorderLayout());
            setBorder(BORDER1);
            textArea.setTokenMarker(new XMLTokenMarker());
            textArea.setEditable(editable);
            textArea.setBorder(BORDER2);
            labTitle.setBackground(UIManager.getColor("textHighlight"));
            labTitle.setForeground(UIManager.getColor("textHighlightText"));
            labTitle.setOpaque(true);
            topPanel.setLayout(new BorderLayout());
            if (editable){
                toolBar.setFloatable(false);
                toolBar.add(commit);
                toolBar.add(rollback);
                commit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commit();
                    }
                });
                rollback.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        rollback();
                    }
                });
                commit.setEnabled(false);
                rollback.setEnabled(false);
                topPanel.add(toolBar, BorderLayout.SOUTH);
                textArea.getDocument().addDocumentListener(this);
                textArea.addComponentListener(new ComponentAdapter() {
                    public void componentHidden(ComponentEvent e) {
                        checkChanges();
                    }
                });
                textArea.addFocusListener(new FocusListener() {
                    public void focusGained(FocusEvent e) {
                    }

                    public void focusLost(FocusEvent e) {
                        Component oppositeComponent = e.getOppositeComponent();
                        if (oppositeComponent == commit || oppositeComponent == rollback) return;
                        checkChanges();
                    }
                });
            }


            topPanel.add(labTitle, BorderLayout.NORTH);


            add(topPanel, BorderLayout.NORTH);
            add(textArea, BorderLayout.CENTER);


        }

        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e) {

        }

        /**
         * Gives notification that there was an insert into the document.  The
         * range given by the DocumentEvent bounds the freshly inserted region.
         *
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e) {
            documentUpdate();
        }

        /**
         * Gives notification that a portion of the document has been
         * removed.  The range is given in terms of what the view last
         * saw (that is, before updating sticky positions).
         *
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e) {
            documentUpdate();
        }

        public boolean isDirty() {
            return isDirty;
        }

        private void documentUpdate() {
            if (ignoreDocumentUpdate) return;
            commit.setEnabled(true);
            rollback.setEnabled(true);
            isDirty = true;
        }

        public Element getElement() {
            return element;
        }

        public void setElement(Element element) {
            if (isDirty && this.element == element) return;
            checkChanges();
            this.element = element;
            ignoreDocumentUpdate = true;
            if (element != null) {
                setText(outputter.outputString(element));
            } else {
                setText("");
            }
            ignoreDocumentUpdate = false;
            commit.setEnabled(false);
            rollback.setEnabled(false);
            isDirty = false;
        }

        private void checkChanges() {
            if (isDirty) {
                isDirty = false;
                int option = JOptionPane.showConfirmDialog(textArea, "Do you want to commit the changes to the xml?", "Commit changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    commit();
                } else {
                    rollback();
                }
            }
        }

        public String getText() {
            return textArea.getText();
        }

        public void setText(String text) {
            textArea.setText(text);
        }

        public String getTitle() {
            return labTitle.getText();
        }

        public void setTitle(String title) {
            labTitle.setText(title);
        }

        /**
         * Parse the meta data and return the JDom Document.
         */
        private Element getRootElement() throws Exception {
            MdDocument doc = null;
            SAXBuilder b = new SAXBuilder(false);
            b.setFactory(MdJDOMFactory.getInstance());
            b.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) {
                    StringReader reader = new StringReader("");
                    return new InputSource(reader);
                }
            });
            StringReader reader = new StringReader(getText());
            doc = (MdDocument)b.build(reader);
            return doc.detachRootElement();
        }

        /**
         * Commit's the changes to the JDOM tree
         */
        private void commit() {
            try {
                if (element != null && reloader != null) {
                    MdElement newElement = (MdElement)getRootElement();
                    MdElement oldElement = (MdElement)element;
                    newElement.setVirtualParent(oldElement.getVirtualParent());
                    newElement.setAutoAddRemove(oldElement.isAutoAddRemove());
                    MdElement parent = (MdElement)oldElement.getParent();
                    if (parent != null) {
                        parent.removeContent(oldElement);
                        parent.addContent(newElement);
                    } else {
                        newElement.addToParent();
                    }
                    isDirty = false;
                    XmlUtils.makeDirty(newElement);
                    reloader.reloadProject();
                }
            } catch (Exception e) {
                GuiUtils.dispatchException(this, e);
            }
        }

        /**
         * Rolls back the changes
         */
        private void rollback() {
            isDirty = false;
            setElement(element);
        }
    }

}


