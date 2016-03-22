
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
 * A dummy GraphColumn that just paints its name as '...' to indicate
 * columns left out of a table.
 *
 * @keep-all
 */
public class DotDotDotColumn implements GraphColumn {

    public static final DotDotDotColumn INSTANCE = new DotDotDotColumn("...");
    public static final DotDotDotColumn SPACE_INSTANCE = new DotDotDotColumn(
            " ");

    private String text;

    public DotDotDotColumn(String text) {
        this.text = text;
    }

    public String getColumnName() {
        return text;
    }

    public String getSqlDDL() {
        return "";
    }

    public GraphTable getTable() {
        return null;
    }

    public String getComment() {
        return null;
    }

    public boolean isPrimaryKey() {
        return false;
    }

}


