
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
import com.versant.core.jdo.tools.workbench.model.GraphTable;

/**
 * Name at the top of a JdbcTable. This is a separate class to JdbcTableCell
 * so lines can be routed to the name part of the table.
 *
 * @keep-all
 */
public class TableNameCell extends DefaultGraphCell implements HasComment {

    private GraphTable table;

    public TableNameCell(GraphTable table) {
        super(table);
        this.table = table;
    }

    public String getComment() {
        return table.getComment();
    }

}
