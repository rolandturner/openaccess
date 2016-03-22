
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

/**
 * @keep-all
 */
public class ColumnDiff {
    boolean extraCol = false;
    boolean missingCol = false;
    boolean typeDiff = false;
    boolean lenghtDiff = false;
    boolean nullDiff = false;
    boolean scaleDiff = false;
    private JdbcColumn ourCol;
    private JdbcColumn dbCol;

    public ColumnDiff(JdbcColumn ourCol, JdbcColumn dbCol) {
        this.ourCol = ourCol;
        this.dbCol = dbCol;
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

    public boolean isTypeDiff() {
        return typeDiff;
    }

    public void setTypeDiff(boolean typeDiff) {
        this.typeDiff = typeDiff;
    }

    public boolean isLenghtDiff() {
        return lenghtDiff;
    }

    public void setLenghtDiff(boolean lenghtDiff) {
        this.lenghtDiff = lenghtDiff;
    }

    public boolean isNullDiff() {
        return nullDiff;
    }

    public void setNullDiff(boolean nullDiff) {
        this.nullDiff = nullDiff;
    }

    public boolean isScaleDiff() {
        return scaleDiff;
    }

    public void setScaleDiff(boolean scaleDiff) {
        this.scaleDiff = scaleDiff;
    }

    public JdbcColumn getOurCol() {
        return ourCol;
    }

    public JdbcColumn getDbCol() {
        return dbCol;
    }

    public boolean hasErrors(){
        if (extraCol || missingCol || typeDiff || lenghtDiff || nullDiff || scaleDiff ){
            return true;
        } else {
            return false;
        }
    }

}
