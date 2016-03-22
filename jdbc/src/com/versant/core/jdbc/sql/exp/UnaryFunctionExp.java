
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
 * An unary operator expression.
 */
public class UnaryFunctionExp extends UnaryExp {

    public static final int FUNC_TO_LOWER_CASE = 1;

    private int func;

    public UnaryFunctionExp(SqlExp child, int func) {
        super(child);
        this.func = func;
    }

    public UnaryFunctionExp() {
    }

    public SqlExp createInstance() {
        return new UnaryFunctionExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((UnaryFunctionExp) clone).func = func;

        return clone;
    }

    public String toString() {
        String s = super.toString() + " ";
        switch (func) {
            case FUNC_TO_LOWER_CASE:
                return "toLowerCase(" + s + ")";
        }
        return s + "unknown(" + func + ")";
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        s.append(driver.getSqlUnaryFunctionName(func));
        s.append('(');
        childList.appendSQL(driver, s, null);
        s.append(')');
    }

}
