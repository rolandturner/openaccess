
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

import com.versant.core.util.CharBuf;
import com.versant.core.jdbc.sql.SqlDriver;

/**
 * An not exists sub select.
 */
public class NotExistsExp extends SelectExp {

    public NotExistsExp() {
    }

    public SqlExp createInstance() {
        return new NotExistsExp();
    }

    public String toString() {
        return super.toString();
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        s.append("not exists (");
        super.appendSQLImp(driver, s, leftSibling);
        s.append(')');
    }

    /**
     * Check that the select list is valid.
     */
    protected void finishSelectList(CharBuf s, int start) {
        int n = s.size() - start;
        if (n <= 7) {
            s.append('1');
        } else {
            s.setSize(n - 2);   // remove the extra comma and space
        }
    }
}

