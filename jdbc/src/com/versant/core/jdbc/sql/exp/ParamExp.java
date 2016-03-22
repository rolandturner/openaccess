
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
import com.versant.core.jdbc.metadata.JdbcTypes;
import com.versant.core.util.CharBuf;
import com.versant.core.jdo.query.ParamNode;

import java.util.Map;

/**
 * A replaceable parameter.
 */
public class ParamExp extends LeafExp {

    public int jdbcType;
    public SqlParamUsage usage;
    protected int firstCharIndex;

    public ParamExp(int jdbcType, SqlParamUsage usage) {
        this.jdbcType = jdbcType;
        this.usage = usage;
    }

    public ParamExp() {
    }

    public SqlExp createInstance() {
        return new ParamExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);
        ParamExp cst = (ParamExp) clone;

        cst.jdbcType = jdbcType;
        if (usage != null) cst.usage = usage.getClone(cloneMap);
        cst.firstCharIndex = firstCharIndex;

        return clone;
    }

    public String toString() {
        return super.toString() + " " + JdbcTypes.toString(jdbcType);
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        firstCharIndex = s.size();
        s.append(driver.getSqlParamString(jdbcType));
    }

    /**
     * Get the index of first character of the 'is null' parameter
     * replacement span for this expression.
     */
    public int getFirstCharIndex() {
        return firstCharIndex;
    }
}
