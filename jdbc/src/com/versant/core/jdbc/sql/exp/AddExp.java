
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
import com.versant.core.jdo.query.AddNode;
import com.versant.core.metadata.MDStatics;

import java.util.Map;

/**
 * A additive expression.
 */
public class AddExp extends SqlExp {

    public static final int OP_PLUS = AddNode.OP_PLUS;
    public static final int OP_MINUS = AddNode.OP_MINUS;

    /**
     * The operators. There will be one less entry here than the childList.
     * Example: ops[0] is between childList and childList.next.
     */
    private int[] ops;
    private String expAlias;

    public AddExp() {
    }

    public AddExp(SqlExp children, int[] ops) {
        super(children);
        this.ops = ops;
    }

    public SqlExp createInstance() {
        return new AddExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        AddExp cst = (AddExp) clone;
        super.getClone(cst, cloneMap);

        if (ops != null) {
            int n = ops.length;
            int[] cOps = new int[n];
            for (int i = 0; i < n; i++) {
                cOps[i] = ops[i];
            }
            cst.ops = cOps;
        }
        return cst;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        if (driver.isExtraParens()) s.append('(');
        int i = 0;
        childList.appendSQL(driver, s, null);
        boolean useConcat = (childList.getJavaTypeCode() == MDStatics.STRING);
        for (SqlExp e = childList.getNext(); e != null && !useConcat; e = e.getNext()) {
            useConcat = e.getJavaTypeCode() == MDStatics.STRING;
        }
        String concatOp = null;
        if (useConcat) {
            concatOp = driver.getSqlBinaryOp(BinaryOpExp.CONCAT);
        }
        SqlExp prev = childList;
        for (SqlExp e = childList.getNext(); e != null; prev = e, e = e.getNext()) {
            s.append(' ');
            if (useConcat) {
                s.append(concatOp);
            } else {
                switch (ops[i++]) {
                    case OP_PLUS:
                        s.append('+');
                        break;
                    case OP_MINUS:
                        s.append('-');
                        break;
                }
            }
            s.append(' ');
            e.appendSQL(driver, s, prev);
        }
        if (driver.isExtraParens()) s.append(')');
        if (expAlias != null) {
            s.append(driver.getAliasPrepend() + " " + expAlias);
        }
    }

    /**
     * If this expression is added to an MultiplyExp should it be enclosed in
     * parenthesis?
     */
    public boolean requiresParensInMultiply() {
        return true;
    }

    public void setExpAlias(String asValue) {
        if (asValue != null && asValue.length() > 0) {
            expAlias = asValue;
        } else {
            expAlias = null;
        }
    }
}

