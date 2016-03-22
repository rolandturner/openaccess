
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
package com.versant.core.jdo.tools.workbench.forms;

import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.jdoql.ordering.OrderingPane;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.JdoExtension;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Select the ordering for a field.
 */
public class FieldOrderingForm extends WorkbenchPanel {

    private JRadioButton radUnordered;
    private JRadioButton radIndex;
    private JRadioButton radJDOQL;

    private OrderingPane orderingPane = new OrderingPane();

    private ExpTableModel modelField = new ExpTableModel("modelField");
    private ExpTableModel notificationModel;
    private MdField field;
    private MdField fieldMM;
    private boolean allowPreserveIndex;

    private boolean inActionPerformed;
    private boolean jdoqlOrdering;
    private boolean ignoreTableEvents;

    public FieldOrderingForm() throws Exception {
        this(false);
    }

    public FieldOrderingForm(boolean vertical) throws Exception {
        this(vertical ? "fieldOrderingFormVertical" : "fieldOrderingForm");
    }

    public FieldOrderingForm(String name) throws Exception {
        super(name);
        modelField.setConfig(getConfig());
        super.setModel(modelField);
        radUnordered = createRad("radUnordered", "Unordered",
                "Do not order collection elements in any way ");
        radIndex = createRad("radIndex", "Preserve index",
                "Preserve index of each element by adding a sequence column to the link table");
        radJDOQL = createRad("radJDOQL", "JDOQL ordering",
                "Use a JDOQL ordering expression on the element-type to order the elements");
        ButtonGroup grpFetchGroup = new ButtonGroup();
        grpFetchGroup.add(radUnordered);
        grpFetchGroup.add(radIndex);
        grpFetchGroup.add(radJDOQL);
        add(radUnordered);
        add(radIndex);
        add(radJDOQL);

        orderingPane.setModel(getModel());
        orderingPane.setColumnName("colOrderingStr");
        orderingPane.setMargin(new Insets(2, 4, 2, 4));

        JScrollPane orderingScrollPane = new JScrollPane(orderingPane,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderingScrollPane.setName("orderingPane");
        orderingScrollPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        add(orderingScrollPane);

        setTitle("Collection / Array ordering");
    }

    private JRadioButton createRad(String name, String text, String tip) {
        JRadioButton b = new JRadioButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        b.addActionListener(this);
        return b;
    }

    public void setModel(TableModel model) {
        notificationModel = (ExpTableModel)model;
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup(MdField newField, boolean allowPreserveIndex) {
        updateSetup(newField, null, allowPreserveIndex);
    }

    /**
     * Update this form to match our field. Param fieldMMinv is the inverse
     * side of a many-to-many with fieldMM being the non-inverse side. If
     * the ordering is changed to preserve index then the inverse and
     * non-inverse sides are swapped.
     */
    public void updateSetup(MdField fieldMMinv, MdField fieldMM) {
        updateSetup(fieldMMinv, fieldMM, true);
    }

    private void updateSetup(MdField newField, MdField fieldMM,
            boolean allowPreserveIndex) {
        this.fieldMM = fieldMM;

        if (newField != null){
            orderingPane.setMdClass(newField.getElementTypeMdClass());
            orderingPane.setClassType(newField.getElementTypeStr());
            String s = newField.getOrderingStr();
            orderingPane.setText(s == null ? "" : s);
        }

        radIndex.setVisible(this.allowPreserveIndex = allowPreserveIndex);

        if (field != newField) {
            jdoqlOrdering = false;
            field = newField;
            try {
                ignoreTableEvents = true;
                modelField.clear();
                modelField.add(field);
            } finally {
                ignoreTableEvents = false;
            }
        }

        String jdoql = field.getOrderingStr();
        if (jdoqlOrdering || jdoql != null) {
            radJDOQL.setSelected(true);
        } else {
            String s = field.getOrderedStr();
            if (fieldHasIndex() && allowPreserveIndex) {
                if (s == null && fieldMM == null) radIndex.setSelected(true);
                else if ("true".equals(s)) radIndex.setSelected(true);
                else radUnordered.setSelected(true);
                radIndex.setEnabled(true);
            } else {
                radUnordered.setSelected(true);
                radIndex.setEnabled(false);
            }
        }
    }

    /**
     * Is the preserve index option currently selected?
     */
    public boolean isPreserveIndex() {
        return radIndex.isSelected();
    }

    /**
     * Is this field a List or something else that has an index?
     */
    private boolean fieldHasIndex() {
        return field.fieldHasIndex();

    }

    /**
     * The form editor has been invoked or one of our controls has done
     * something.
     */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == this) {
            super.actionPerformed(e);
            return;
        }
        if (inActionPerformed) return;
        try {
            inActionPerformed = true;
            if (o == radUnordered) {
                field.setOrderingStr(null);
                if (fieldHasIndex() && allowPreserveIndex && fieldMM == null) {
                    field.setOrderedStr("false");
                } else {
                    field.setOrderedStr(null);
                }
                jdoqlOrdering = false;
            } else if (o == radIndex) {
                field.setOrderingStr(null);
                if (fieldHasIndex()) field.setOrderedStr(null);
                jdoqlOrdering = false;
                if (fieldMM != null) swapManyToManyAndInverse();
            } else if (o == radJDOQL) {
                field.setOrderedStr(null);
                jdoqlOrdering = true;
            } else {
                return;
            }
            fireFieldUpdated();
        } catch (Exception x) {
            GuiUtils.dispatchException(this, x);
        } finally {
            inActionPerformed = false;
        }
    }

    /**
     * Swap the many-to-many inverse (field) with the non-inverse (fieldMM).
     * This moves all the content of the &lt;collection&gt; element and the
     * &lt;jdbc-link-table&gt; element.
     */
    private void swapManyToManyAndInverse() {
        field.swapManyToManyAndInverse(fieldMM);
    }

    public void tableChanged(TableModelEvent e) {
        if (!ignoreTableEvents) fireFieldUpdated();
    }

    private void fireFieldUpdated() {
        notificationModel.fireCursorObjectUpdated();
    }

}
