
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
 * Aggregate.
 */
public class AggregateExp extends UnaryExp {
    private String type;
    private String asValue;

    public AggregateExp(SqlExp child, String type) {
        super(child);
        this.type = type;
    }

    public AggregateExp(SqlExp child, String type, String asValue) {
        super(child);
        this.type = type;
        this.asValue = asValue;
    }

    public String getAsValue() {
        return asValue;
    }

    public void setAsValue(String asValue) {
        this.asValue = asValue;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s      Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        s.append(type);
        s.append("(");
        childList.appendSQL(driver, s, null);
        s.append(')');

        if (asValue != null) {
            s.append(" as " + asValue);
        }
    }
    

}
