
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
 * Special case of an 'and' where the expressions are all part of a join.
 * This does not put each expression on its own line.
 */
public class AndJoinExp extends AndExp {

    public AndJoinExp(SqlExp children) {
        super(children);
    }

    public AndJoinExp() {
    }

    public SqlExp createInstance() {
        return new AndJoinExp();
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
        SqlExp prev = childList;
        for (SqlExp e = childList.getNext(); e != null; prev = e, e = e.getNext()) {
            s.append(" and ");
            e.appendSQL(driver, s, prev);
        }
    }

    /**
     * Make us an outer join or not. This is a NOP except for JoinExp and
     * AndJoinExp.
     * @see JoinExp
     * @see AndJoinExp
     */
    public void setOuter(boolean on) {
        for (SqlExp e = childList; e != null; e = e.getNext()) {
            e.setOuter(on);
        }
    }

}

