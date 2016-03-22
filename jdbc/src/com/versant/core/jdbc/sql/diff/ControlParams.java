
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

/**
 * @keep-all
 * This bean is used to see how deep to check a diff.
 */
public class ControlParams {
    boolean columnsOnly = false;// not user prop
    boolean checkLength = true;
    boolean checkType = true;
    boolean checkScale = true;
    boolean checkNulls = true;
    boolean checkPK = true;
    boolean checkIndex = true;
    boolean checkConstraint = true;
    boolean checkExtraColumns = true;

    public boolean isCheckLength() {
        return checkLength;
    }

    public void setCheckLength(boolean checkLength) {
        this.checkLength = checkLength;
    }

    public boolean isCheckType() {
        return checkType;
    }

    public void setCheckType(boolean checkType) {
        this.checkType = checkType;
    }

    public boolean isCheckScale() {
        return checkScale;
    }

    public void setCheckScale(boolean checkScale) {
        this.checkScale = checkScale;
    }

    public boolean isCheckNulls() {
        return checkNulls;
    }

    public void setCheckNulls(boolean checkNulls) {
        this.checkNulls = checkNulls;
    }

    public boolean isCheckIndex() {
        return checkIndex;
    }

    public void setCheckIndex(boolean checkIndex) {
        this.checkIndex = checkIndex;
    }

    public boolean isCheckConstraint() {
        return checkConstraint;
    }

    public void setCheckConstraint(boolean checkConstraint) {
        this.checkConstraint = checkConstraint;
    }

    public boolean isCheckPK() {
        return checkPK;
    }

    public void setCheckPK(boolean checkPK) {
        this.checkPK = checkPK;
    }

    public boolean isCheckExtraColumns() {
        return checkExtraColumns;
    }

    public void setCheckExtraColumns(boolean checkExtraColumns) {
        this.checkExtraColumns = checkExtraColumns;
    }

    /**
     * this method does not start with is, because we do not want users to set it.(its only for dave)
     * @return
     */
    public boolean checkColumnsOnly() {
        return columnsOnly;
    }

    public void setColumnsOnly(boolean columnsOnly) {
        this.columnsOnly = columnsOnly;
    }
}

