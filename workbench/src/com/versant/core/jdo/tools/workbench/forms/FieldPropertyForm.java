
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
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * View/edit properties for a field. This swaps forms in and out of a tabbed
 * pane depending on the category of the selected field.
 */
public class FieldPropertyForm extends WorkbenchPanel {

    private JDBCFieldPropertyForm jdbcForm;
    private VDSFieldPropertyForm vdsForm;
    private int type;

    public FieldPropertyForm() throws Exception {
        updateSetup();
        setLayout(new BorderLayout());
    }

    public void setMdField(MdField mdField) throws Exception {
        if (mdField != null) {
            int type = getType(mdField);
            if (type == MdDataStore.TYPE_JDBC) {
                JDBCFieldPropertyForm jdbcForm = getJdbcForm();
                jdbcForm.setMdField(mdField);
                if (this.type != type) {
                    removeAll();
                    jdbcForm.setTitle(null);
                    add(jdbcForm, BorderLayout.CENTER);
                }
            } else if (type == MdDataStore.TYPE_VDS) {
                VDSFieldPropertyForm vdsForm = getVdsForm();
                vdsForm.setMdField(mdField);
                if (this.type != type) {
                    removeAll();
                    vdsForm.setTitle(null);
                    add(vdsForm, BorderLayout.CENTER);
                }
            }
            this.type = type;
        } else {
            removeAll();
        }
    }

    private JDBCFieldPropertyForm getJdbcForm() throws Exception {
        if (jdbcForm == null) {
            jdbcForm = new JDBCFieldPropertyForm();
        }
        return jdbcForm;
    }

    private VDSFieldPropertyForm getVdsForm() throws Exception {
        if (vdsForm == null) {
            vdsForm = new VDSFieldPropertyForm();
        }
        return vdsForm;
    }

    private int getType(MdField mdField) {
        MdClass mdClass = mdField.getMdClass();
        MdDataStore ds = mdClass.getMdDataStore();
        int type = ds.getType();
        return type;
    }

    public void tableChanged(TableModelEvent e) {
        updateSetup();
    }

    private void updateSetup() {
        if (jdbcForm != null) {
            jdbcForm.updateSetup();
        }
        if (vdsForm != null) {
            vdsForm.updateSetup();
        }
    }

    public void updateUI() {
        super.updateUI();
        if (jdbcForm != null) {
            jdbcForm.updateUI();
        }
        if (vdsForm != null) {
            vdsForm.updateUI();
        }
    }

    /**
     * The project has changed.
     */
    public void projectChanged(MdProjectEvent ev) {
        switch (ev.getFlags()) {
            case MdProjectEvent.ID_ENGINE_STARTED:
            case MdProjectEvent.ID_ENGINE_STOPPED:
            case MdProjectEvent.ID_PARSED_DATABASE:
            case MdProjectEvent.ID_DIRTY_FLAG:
            case MdProjectEvent.ID_CLASSES_REMOVED:
//            case MdProjectEvent.ID_PARSED_META_DATA:
            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                return;
        }
        updateSetup();
    }

    /**
     * Base class for sub forms for different field types. This provides a
     * title label for the form and manages swapping components in and out
     * of a holder area depending on selected mapping options.
     */
    public static class SubForm extends WorkbenchPanel {

        private JPanel holder = new JPanel(new BorderLayout());

        private boolean inActionPerformed;

        public SubForm() throws Exception {
            holder.setName("holder");
            add(holder);
            Component c = getComponent("miniClassDef");
            if (c != null) {
                JLabel label = (JLabel)c;
                label.setBackground(UIManager.getColor("textHighlight"));
                label.setForeground(UIManager.getColor("textHighlightText"));
                label.setOpaque(true);
            }
        }

        public MdField getMdField() {
            return (MdField)((ExpTableModel)getModel()).getCursorObject();
        }

        public void fireFieldUpdated() {
            ((ExpTableModel)getModel()).fireCursorObjectUpdated();
        }

        /**
         * Get the field to display on top in the XML preview window.
         */
        public MdField getTopXmlField() {
            return getMdField();
        }

        /**
         * Get the field to display at bottom of the XML preview window or
         * null if none.
         */
        public MdField getBottomXmlField() {
            return null;
        }

        /**
         * Set the component display in our holder area or null for none.
         */
        public void setActive(Component comp) {
            if (comp == null) {
                if (holder.getComponentCount() > 0) {
                    holder.removeAll();
                    holder.invalidate();
                    repaint();
                }
            } else {
                if (holder.getComponentCount() > 0) {
                    if (holder.getComponent(0) == comp) return;
                    holder.removeAll();
                }
                holder.add(comp, BorderLayout.CENTER);
                holder.invalidate();
                repaint();
            }
        }

        /**
         * Update this form to match our field.
         */
        public void updateSetup() {
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
                if (actionPerformedImp(o)) fireFieldUpdated();
            } catch (Exception x) {
                GuiUtils.dispatchException(this, x);
            } finally {
                inActionPerformed = false;
            }
        }

        /**
         * Subclasses should override this to process action events from
         * their components and return true if changes were made to the
         * field. This will not be invoked for nested action events.
         */
        public boolean actionPerformedImp(Object source) throws Exception {
            return false;
        }

        /**
         * Get the title for the tab for this form.
         */
        public String getTabTitle() {
            return "SubForm";
        }

        /**
         * Add our tables to a diagram. If ownerRight is true then the owning
         * table is positioned on the right hand side of the diagram instead
         * of on the left.
         */
        public void addToDiagram(MdLinkTable linkTable, ClassDiagram d) {
            d.addTable(linkTable.getOwnerTable());
            d.addTable(linkTable).setUnder(true);
            boolean addKeyTable = linkTable.isAddKeyTable();
            if (addKeyTable) d.addTable(linkTable.getKeyTable());
            if (linkTable.isAddValueTable()) {
                d.addTable(linkTable.getValueTable()).setUnder(addKeyTable);
            }
        }

    }

}

