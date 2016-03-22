
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
import com.versant.core.jdbc.sql.conv.DummyPreparedStmt;
import com.versant.core.util.CharBuf;
import com.versant.core.jdo.query.UnaryOpNode;

import java.util.Map;
import java.sql.SQLException;

import com.versant.core.common.BindingSupportImpl;

/**
 * An unary operator (+, -, ~, !) expression.
 */
public class UnaryOpExp extends UnaryExp {

    public static final int OP_MINUS = UnaryOpNode.OP_MINUS;
    public static final int OP_PLUS = UnaryOpNode.OP_PLUS;
    public static final int OP_COMPLEMENT = UnaryOpNode.OP_TILDE;
    public static final int OP_NOT = UnaryOpNode.OP_BANG;

    private int op;

    public UnaryOpExp(SqlExp child, int op) {
        super(child);
        this.op = op;
    }

    public UnaryOpExp() {
    }

    public SqlExp createInstance() {
        return new UnaryOpExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((UnaryOpExp)clone).op = op;

        return clone;
    }

    public String toString() {
        String s = super.toString() + " ";
        switch (op) {
            case OP_PLUS:
                return s + "+";
            case OP_MINUS:
                return s + "-";
            case OP_NOT:
                return s + "not";
            case OP_COMPLEMENT:
                return s + "~";
        }
        return s + "unknown(" + op + ")";
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        if (childList instanceof UnaryOpExp && ((UnaryOpExp)childList).op == op) {
            // if our child is the same as us then skip both of us as the
            // operations will cancel each other out
            SqlExp r = childList.childList.normalize(driver, sel, convertExists);
            if (r != null) {
                return r;
            } else {
                return childList.childList;
            }
        } else {
            SqlExp r = null;
			if ( childList != null )
				r = childList.normalize(driver, sel, convertExists);
            if (r != null) childList = r;
            return null;
        }
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s      Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        // if our child is the same as us then skip both of us as the
        // operations will cancel each other out
        if (childList instanceof UnaryOpExp && ((UnaryOpExp)childList).op == op) {
            childList.childList.appendSQL(driver, s, null);
            return;
        }

        boolean brackets;
        switch (op) {
            case OP_PLUS:
                // do not need to do anything else
                childList.appendSQL(driver, s, null);
                break;
            case OP_MINUS:
                s.append('-');
                s.append(' ');
                // SAP DB does not like extra brackets around numbers
                brackets = !(childList instanceof LiteralExp);
                if (brackets) s.append('(');
                childList.appendSQL(driver, s, null);
                if (brackets) s.append(')');
                break;
            case OP_NOT:
                if (childList instanceof ColumnExp) {
                    ColumnExp cExp = (ColumnExp)childList;
                    childList.appendSQL(driver, s, null);
                    s.append(" = ");
                    if (cExp.col.converter != null) {
                        DummyPreparedStmt pstmt = new DummyPreparedStmt();
                        try {
                            cExp.col.converter.set(pstmt, 0, cExp.col, new Boolean("false"));
                        } catch (SQLException e) {
                            //ignore
                        }
                        if (pstmt.toQuote) {
                            s.append("'" + pstmt.value + "'");
                        } else {
                            s.append(pstmt.value);
                        }
                    } else {
                        s.append("false");
                    }
                } else {
                    s.append("not ");
                    s.append('(');
                    childList.appendSQL(driver, s, null);
                    s.append(')');
                }
                break;
            case OP_COMPLEMENT:
                s.append('(');
                s.append('-');
                s.append(' ');
                // SAP DB does not like extra brackets around numbers
                brackets = !(childList instanceof LiteralExp);
                if (brackets) s.append('(');
                childList.appendSQL(driver, s, null);
                if (brackets) s.append(')');
                s.append(')');
                s.append(' ');
                s.append('-');
                s.append(' ');
                s.append('1');
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown UnaryOpExp op: " + op);
        }
    }

    /**
     * Can this expression be removed and its child be converted into a join?
     *
     * @see ExistsExp
     * @see #NO
     * @see #YES
     * @see #YES_DISTINCT
     */
    public int getConvertToJoin() {
		if ( childList == null )
			return YES;
        if (op == OP_NOT) {
            int ans = childList.getConvertToJoin();
            if (ans == YES_DISTINCT) return YES_DISTINCT_NOT;
            return ans;
        } else {
            return NO;
        }
    }

}
