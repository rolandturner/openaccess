
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
 * x BETWEEN from AND to.
 */
public class BetweenExp extends SqlExp {

    public BetweenExp(SqlExp child) {
        super(child);
    }

    public BetweenExp() {
    }

    public SqlExp createInstance() {
        return new BetweenExp();
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
        s.append(" BETWEEN ");
        childList.getNext().appendSQL(driver, s, null);
        s.append(" AND ");
        childList.getNext().getNext().appendSQL(driver, s, null);
    }

}
