
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
 * An 'in' expression.
 */
public class InExp extends SqlExp {

    public InExp(SqlExp children) {
        super(children);
    }

    public InExp() {
    }

    public SqlExp createInstance() {
        return new InExp();
    }

    /**
     * If this expression is added to an AndExp should it be enclosed in
     * parenthesis?
     */
    public boolean requiresParensInAnd() {
        return false;
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
        s.append(" IN (");
        SqlExp e = childList.getNext();
        e.appendSQL(driver, s, null);
        for (e = e.getNext(); e != null; e = e.getNext()) {
            s.append(',');
            s.append(' ');
            e.appendSQL(driver, s, null);
        }
        s.append(')');
    }

}

