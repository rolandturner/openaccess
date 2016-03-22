
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

import java.util.Map;

/**
 * This is an entry in an order by list.
 */
public class OrderExp extends UnaryExp {

    private boolean desc;

    public OrderExp(SqlExp child, boolean desc) {
        super(child);
        this.desc = desc;
    }

    public OrderExp() {
    }

    public SqlExp createInstance() {
        return new OrderExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((OrderExp) clone).desc = desc;

        return clone;
    }

    public String toString() {
        return super.toString() + (desc ? " desc" : "");
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        super.appendSQLImp(driver, s, leftSibling);
        if (desc) s.append(" DESC");
    }

    public boolean isDesc() {
        return desc;
    }
}

