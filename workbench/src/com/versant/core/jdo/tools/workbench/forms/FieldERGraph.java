
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

import com.jgraph.graph.CellView;
import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.diagram.classdiagram.*;
import com.versant.core.jdo.tools.workbench.model.*;

/**
 * Mini E/R diagram showing the mapping for a field. This supports editing
 * of the table, column and reference properties for the diagram.
 */
public class FieldERGraph extends ClassGraph {

    public FieldERGraph(ClassDiagram diagram) {
        super(diagram);
    }

    public void startEditingAtCell(Object cell) {
        try {
            WorkbenchPanel editForm = null;
            Object o = getSelectionCell();
            if (o instanceof TableCell) {
                CellView cv = getInnerSelection();
                if (cv instanceof ColumnView) {
                    GraphColumn gc = ((ColumnCell)cv.getCell()).getGraphColumn();
                    if (gc instanceof MdColumn) {
                        editColumn((MdColumn)gc);
                    }
                } else if (cv instanceof TableNameView) {
                    GraphTable t = ((TableCell)o).getGraphTable();
                    editForm = new EditMdTableForm((MdTable)t);
                }
            } else if (o instanceof ColumnRefEdge) {
                GraphColumnRef ref = ((ColumnRefEdge)o).getRef();
                if (ref instanceof MdJdbcRef) {
                    if (openEditDialog((MdJdbcRef)ref)) {
                        //fireFieldUpdated();
                    }
                }
            } else {
                super.startEditingAtCell(cell);
            }
            if (editForm != null) {
                openDialog(editForm, true, true, false);
            }
        } catch (Exception e) {
            GuiUtils.dispatchException(this, e);
        }
    }

    /**
     * Open a dialog to edit the properties of this reference. Returns true
     * if changes were made.
     */
    private boolean openEditDialog(MdJdbcRef ref) throws Exception {
        EditForeignKeyRefForm f = new EditForeignKeyRefForm(
                "Edit foreign key reference");
        if (ref.isIgnoreJoin()) {
            f.setJoin("inner");
            f.setIgnoreJoin(true);
        } else {
            f.setJoin(ref.getJdbcUseJoinStr());
        }
        f.setConstraint(ref.getJdbcConstraintStr());
        if (Utils.openDialog(null, f, true, true, true)) {
            ref.setJdbcUseJoinStr(f.getJoin());
            ref.setJdbcConstraintStr(f.getConstraint());
            return true;
        } else {
            return false;
        }
    }

    public void editColumn(MdColumn col) throws Exception {
        EditMdColumnForm f = new EditMdColumnForm("Edit column");
        f.setCol(col);
        openDialog(f, true, true, false);
        f.setCol(null);
    }

    private boolean openDialog(WorkbenchPanel form, boolean modal, boolean hasOk,
            boolean hasCancel) throws Exception {
        return Utils.openDialog(this, form, modal, hasOk,
                hasCancel);
    }

}
