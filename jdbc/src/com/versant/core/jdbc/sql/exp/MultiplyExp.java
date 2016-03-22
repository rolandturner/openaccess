
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
import com.versant.core.jdo.query.MultiplyNode;

import java.util.Map;

/**
 * A multiplicative expression.
 */
public class MultiplyExp extends SqlExp {

    public static final int OP_TIMES = MultiplyNode.OP_TIMES;
    public static final int OP_DIVIDE = MultiplyNode.OP_DIVIDE;

    /**
     * The operators. There will be one less entry here than the childList.
     * Example: ops[0] is between childList and childList.next.
     */
    private int[] ops;

    public MultiplyExp(SqlExp children, int[] ops) {
        super(children);
        this.ops = ops;
    }

    public MultiplyExp() {
    }

    public SqlExp createInstance() {
        return new MultiplyExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        if (ops != null) {
            int n = ops.length;
            int[] cOps = ((MultiplyExp) clone).ops = new int[ops.length];
            for (int i = 0; i < n; i++) {
                cOps[i] = ops[i];
            }
        }

        return clone;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        int i = 0;
        appendSQL(childList, driver, s, null);
        SqlExp prev = childList;
        for (SqlExp e = childList.getNext(); e != null; prev = e, e = e.getNext()) {
            s.append(' ');
            switch (ops[i++]) {
                case OP_TIMES:
                    s.append('*');
                    break;
                case OP_DIVIDE:
                    s.append('/');
                    break;
            }
            s.append(' ');
            appendSQL(e, driver, s, prev);
        }
    }

    private void appendSQL(SqlExp e, SqlDriver driver, CharBuf s,
            SqlExp leftSibling) {
        boolean p = e.requiresParensInMultiply();
        if (p) s.append('(');
        e.appendSQL(driver, s, leftSibling);
        if (p) s.append(')');
    }
}
