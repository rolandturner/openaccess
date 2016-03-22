
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
package com.versant.core.jdbc.sql.diff;

import com.versant.core.jdbc.metadata.JdbcTable;

import java.util.ArrayList;

/**
 * @keep-all
 */
public class TableDiff {
    boolean missingTable = false;
    private JdbcTable ourTable;
    private JdbcTable dbTable;
    private boolean hasRealErrors = true;

    ArrayList colDiffs = new ArrayList();
    ArrayList pkDiffs = new ArrayList();
    ArrayList indexDiffs = new ArrayList();
    ArrayList constraintDiffs = new ArrayList();

    public TableDiff(JdbcTable ourTable, JdbcTable dbTable) {
        this.ourTable = ourTable;
        this.dbTable = dbTable;
    }

    public JdbcTable getOurTable() {
        return ourTable;
    }

    public JdbcTable getDbTable() {
        return dbTable;
    }

    public boolean isMissingTable() {
        return missingTable;
    }

    public void setMissingTable(boolean missingTable) {
        this.missingTable = missingTable;
    }

    public ArrayList getColDiffs() {
        return colDiffs;
    }

    public ArrayList getPkDiffs() {
        return pkDiffs;
    }

    public ArrayList getIndexDiffs() {
        return indexDiffs;
    }

    public ArrayList getConstraintDiffs() {
        return constraintDiffs;
    }

    public boolean hasErrors() {
        if (missingTable || !colDiffs.isEmpty() || !pkDiffs.isEmpty() || !indexDiffs.isEmpty() || !constraintDiffs.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasRealErrors() {
        return hasRealErrors;
    }

    public void setHasRealErrors(boolean hasRealErrors) {
        this.hasRealErrors = hasRealErrors;
    }
}

