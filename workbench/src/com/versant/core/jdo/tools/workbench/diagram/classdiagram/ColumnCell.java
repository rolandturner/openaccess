
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
package com.versant.core.jdo.tools.workbench.diagram.classdiagram;

import com.jgraph.graph.DefaultGraphCell;
import com.versant.core.jdo.tools.workbench.model.GraphColumn;

/**
 * Cell for a column from a GraphTable so lines can be routed to columns.
 *
 * @keep-all
 */
public class ColumnCell extends DefaultGraphCell {

    private GraphColumn column;

    public ColumnCell(GraphColumn column) {
        this.column = column;
    }

    public GraphColumn getGraphColumn() {
        return column;
    }

    public void setGraphColumn(GraphColumn column) {
        this.column = column;
    }

    public String toString() {
        return column.getComment();
    }
}
