
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

import com.versant.core.jdbc.metadata.JdbcIndex;
import com.versant.core.jdbc.metadata.JdbcConstraint;

import java.util.ArrayList;

/**
 * @keep-all
 */
public class IndexDiff {
    boolean extraIndex = false;
    boolean missingIndex = false;
    boolean extraCol = false;
    boolean missingCol = false;
    boolean uniqueness = false;
    private JdbcIndex ourIndex;
    private JdbcIndex dbIndex;

    public IndexDiff(JdbcIndex ourIndex, JdbcIndex dbIndex) {
        this.ourIndex = ourIndex;
        this.dbIndex = dbIndex;
    }

    public boolean isUniqueness() {
        return uniqueness;
    }

    public void setUniqueness(boolean uniqueness) {
        this.uniqueness = uniqueness;
    }

    public void setExtraIndex(boolean extraIndex) {
        this.extraIndex = extraIndex;
    }

    public void setMissingIndex(boolean missingIndex) {
        this.missingIndex = missingIndex;
    }

    public boolean isExtraIndex() {
        return extraIndex;
    }

    public boolean isMissingIndex() {
        return missingIndex;
    }

    public boolean isExtraCol() {
        return extraCol;
    }

    public void setExtraCol(boolean extraCol) {
        this.extraCol = extraCol;
    }

    public boolean isMissingCol() {
        return missingCol;
    }

    public void setMissingCol(boolean missingCol) {
        this.missingCol = missingCol;
    }

    public JdbcIndex getOurIndex() {
        return ourIndex;
    }

    public JdbcIndex getDbIndex() {
        return dbIndex;
    }

    public boolean hasErrors() {
        return extraIndex || missingIndex || extraCol || missingCol || uniqueness;
    }
}
