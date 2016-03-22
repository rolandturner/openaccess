
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
package com.versant.core.jdo.tools.workbench.model;

/**
 * A table on a graph.
 */
public interface GraphTable {

    /**
     * Get name of the table.
     */
    public String getName();

    /**
     * Get a comment for the table. This is displayed on a tooltip on
     * graphs.
     */
    public String getComment();

    /**
     * Get primary key columns.
     */
    public GraphColumn[] getPkCols();

    /**
     * Get all columns including primary key columns.
     */
    public GraphColumn[] getCols();

    /**
     * Get references to other tables.
     */
    public GraphColumnRef[] getRefs();

    /**
     * Is this table flagged as 'do not create'?
     */
    public boolean isDoNotCreate();

}
