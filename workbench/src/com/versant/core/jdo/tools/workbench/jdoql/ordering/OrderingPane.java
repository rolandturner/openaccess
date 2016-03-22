
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
package com.versant.core.jdo.tools.workbench.jdoql.ordering;

import com.versant.core.jdo.tools.workbench.jdoql.insight.InsightOrderingDataHandler;
import com.versant.core.jdo.tools.workbench.jdoql.insight.FieldInsightBody;
import com.versant.core.jdo.tools.workbench.jdoql.insight.OrderingPopupInputHandler;
import za.co.hemtech.gui.util.GuiUtils;
import za.co.hemtech.gui.model.FieldModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;



/**
 * This represents a jdoql ordering statement, with syntax highlighting and code completion
 * all you have to do is setMdClass.
 * @keep-all
 */
public class OrderingPane extends OrderingFilterPane implements ChangeListener {
    private Timer popupTimer;
    private OrderingPane orderingPane;
    private FieldOrgeringInsightListener FIELD_ORDERING_POPUP = new FieldOrgeringInsightListener();
    private OrderingPopupInputHandler inputHandler = new OrderingPopupInputHandler();
    private FieldInsightBody fieldInsightBody;
    private InsightOrderingDataHandler orderingDataHandler = new InsightOrderingDataHandler();

    boolean dirty = false;

    private FieldModel fieldModel = new FieldModel();
    public OrderingPane() throws Exception {
        super();
        orderingPane = this;
        fieldInsightBody = new FieldInsightBody();


        orderingPane.addKeyListener(new KeyChangedListener());
        orderingPane.setCaretPosition(0);

        popupTimer = new Timer(50, new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                if (inputHandler.isPopupShowing()) {
                    inputHandler.disposePopup();
                }
                if (orderingPane.hasFocus()) {
                    FIELD_ORDERING_POPUP.actionPerformed(null);
                }

            }
        });

        inputHandler.setPane(orderingPane);
        inputHandler.addDefaultKeyBindings();
        orderingPane.addKeyListener(inputHandler);
        orderingPane.addFocusListener(new FocusListener() {
            /**
             * Invoked when a component gains the keyboard focus.
             */
            public void focusGained(FocusEvent e) {
                orderingPane.doColoring();

            }

            /**
             * Invoked when a component loses the keyboard focus.
             */
            public void focusLost(FocusEvent e) {
                if (inputHandler.isPopupShowing()) {
                    inputHandler.disposePopup();
                }

                if (dirty){
                    dirty = false;
                    fieldModel.getModel().setValueAt(getText(), fieldModel.getRow(), fieldModel.getCol());
                }
            }
        });
        orderingPane.addMouseListener(new MouseListener() {
            /**
             * Invoked when a mouse button has been pressed on a component.
             */
            public void mousePressed(MouseEvent e) {
                if (inputHandler.isPopupShowing()) {
                    inputHandler.disposePopup();
                }
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        fieldInsightBody.getGrid().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inputHandler.POPUP_ENTER.enter(orderingPane);
            }
        });

        popupTimer.setRepeats(false);





    }

    private class KeyChangedListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            if ((e.isControlDown() && e.getKeyCode() == 32)) {
                popupTimer.restart();
            } else if (e.getKeyCode() == 46) {
                popupTimer.restart();
            }

            dirty = true;
        }
    }

    private class FieldOrgeringInsightListener implements ActionListener {

        public FieldOrgeringInsightListener() {
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                if (orderingDataHandler.fillBody(orderingPane, fieldInsightBody)) {
                    inputHandler.showInsightPopup(fieldInsightBody, orderingPane);
                    inputHandler.setPopupFocusWord(orderingPane);
                }
            } catch (Exception x) {
                GuiUtils.dispatchException(orderingPane, x);
            }
        }
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

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e) {
        Object o = fieldModel.getValue();

        if (o != null){
            setText(o.toString());
        } else {
            setText("");
        }
    }
}
