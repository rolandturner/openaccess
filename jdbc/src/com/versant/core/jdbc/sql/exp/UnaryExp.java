
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
package com.versant.core.jdbc.sql.exp;

import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.util.CharBuf;

/**
 * An expression with one child.
 */
public class UnaryExp extends SqlExp {

    public UnaryExp(SqlExp child) {
        super(child);
    }

    public UnaryExp() {
    }

    public SqlExp createInstance() {
        return new UnaryExp();
    }

    /**
     * Create an aliases for any tables we may have.
     */
    public int createAlias(int index) {
        return childList.createAlias(index);
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        childList.appendSQL(driver, s, null);
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        SqlExp r = childList.normalize(driver, sel, convertExists);
        if (r != null) childList = r;
        return null;
    }

    /**
     * Replace any references to old with nw. This is used when redundant
     * joins are removed.
     */
    public void replaceSelectExpRef(SelectExp old, SelectExp nw) {
        childList.replaceSelectExpRef(old, nw);
    }

    /**
     * What is the JDBC type of this expression (0 if unknown)?
     */
    public int getJdbcType() {
        return childList.getJdbcType();
    }

    /**
     * What is the java type code of this expression (0 if unknown)?
     */
    public int getJavaTypeCode() {
        return childList.getJavaTypeCode();
    }
}

