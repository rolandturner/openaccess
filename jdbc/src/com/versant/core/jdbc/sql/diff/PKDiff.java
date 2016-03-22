
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

import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcConstraint;

import java.util.ArrayList;

/**
 * @keep-all
 */
public class PKDiff {
    boolean extraPKCol = false;
    boolean missingPKCol = false;
    boolean missingPK = false;
    private JdbcColumn ourCol;
    private JdbcColumn dbCol;
    private ArrayList dropConstraintsRefs = new ArrayList();
    private ArrayList addConstraintsRefs = new ArrayList();

    public PKDiff(JdbcColumn ourCol, JdbcColumn dbCol) {
        this.ourCol = ourCol;
        this.dbCol = dbCol;
    }

    public boolean isExtraPKCol() {
        return extraPKCol;
    }

    public void setExtraPKCol(boolean extraPK) {
        this.extraPKCol = extraPK;
    }

    public boolean isMissingPKCol() {
        return missingPKCol;
    }

    public void setMissingPKCol(boolean missingPK) {
        this.missingPKCol = missingPK;
    }

    public boolean isMissingPK() {
        return missingPK;
    }

    public void setMissingPK(boolean missingPK) {
        this.missingPK = missingPK;
    }

    public JdbcColumn getOurCol() {
        return ourCol;
    }

    public JdbcColumn getDbCol() {
        return dbCol;
    }

    public ArrayList getDropConstraintsRefs() {
        return dropConstraintsRefs;
    }

    public void setDropConstraintsRefs(JdbcConstraint constraint) {
        this.dropConstraintsRefs.add(constraint);
    }

    public ArrayList getAddConstraintsRefs() {
        return addConstraintsRefs;
    }

    public void setAddConstraintsRefs(JdbcConstraint constraint) {
        this.addConstraintsRefs.add(constraint);
    }

    public boolean hasErrors() {
        if (missingPK || extraPKCol || missingPKCol ) {
            return true;
        } else {
            return false;
        }
    }

}

