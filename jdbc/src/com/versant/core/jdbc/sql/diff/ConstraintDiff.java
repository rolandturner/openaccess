
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

import com.versant.core.jdbc.metadata.JdbcConstraint;

/**
 * @keep-all
 */
public class ConstraintDiff {
    boolean extraConstraint = false;
    boolean missingConstraint = false;
    boolean extraCol = false;
    boolean missingCol = false;
    boolean recreate = false;
    private JdbcConstraint ourConstraint;
    private JdbcConstraint dbConstraint;
    private boolean drop = true;

    public ConstraintDiff(JdbcConstraint ourConstraint, JdbcConstraint dbConstraint) {
        this.ourConstraint = ourConstraint;
        this.dbConstraint = dbConstraint;
    }

    public boolean isExtraConstraint() {
        return extraConstraint;
    }

    public void setExtraConstraint(boolean extraConstraint) {
        this.extraConstraint = extraConstraint;
    }

    public boolean isMissingConstraint() {
        return missingConstraint;
    }

    public void setMissingConstraint(boolean missingConstraint) {
        this.missingConstraint = missingConstraint;
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

    public JdbcConstraint getOurConstraint() {
        return ourConstraint;
    }

    public JdbcConstraint getDbConstraint() {
        return dbConstraint;
    }

    public boolean hasErrors() {
        return extraConstraint || missingConstraint || extraCol || missingCol;
    }

    public boolean drop() {
        return drop;
    }

    public void setDrop(boolean drop){
        this.drop = drop;
    }

    public boolean isRecreate() {
        return recreate;
    }

    public void setRecreate(boolean recreate) {
        this.recreate = recreate;
    }
}
