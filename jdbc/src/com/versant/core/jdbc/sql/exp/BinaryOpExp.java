
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
import com.versant.core.jdbc.query.SqlStruct;

import java.util.Map;

/**
 * An expression comprised of two args and an operator.
 */
public class BinaryOpExp extends BinaryExp {

    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int GT = 3;
    public static final int LT = 4;
    public static final int GE = 5;
    public static final int LE = 6;
    public static final int LIKE = 7;
    public static final int CONCAT = 8;
    public static final int PLUS = 9;
    public static final int MINUS = 10;
    public static final int TIMES = 11;
    public static final int DIVIDE = 12;

    public int op;

    /**
     * The index of the first character of our operator in the sql output
     * buffer.
     */
    private int firstCharIndex;

    public BinaryOpExp(SqlExp left, int op, SqlExp right) {
        super(left, right);
        this.op = op;
    }

    public BinaryOpExp(int op) {
        this.op = op;
    }

    public BinaryOpExp() {
    }

    public SqlExp createInstance() {
        return new BinaryOpExp();
    }

    public SqlExp getClone(SqlExp exp, Map cloneMap) {
        super.getClone(exp, cloneMap);
        BinaryOpExp cst = (BinaryOpExp) exp;

        cst.op = op;
        cst.firstCharIndex = firstCharIndex;

        return exp;
    }

    public static String toOpString(int op) {
        switch (op) {
            case EQUAL:
                return "=";
            case NOT_EQUAL:
                return "<>";
            case GT:
                return ">";
            case LT:
                return "<";
            case GE:
                return ">=";
            case LE:
                return "<=";
            case LIKE:
                return "LIKE";
            case CONCAT:
                return "||";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case TIMES:
                return "*";
            case DIVIDE:
                return "/";
        }
        return "<unknown " + op + ">";
    }

    public String toString() {
        return super.toString() + " " + toOpString(op);
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

        SqlExp right = childList.getNext();

        // convert == and != to 'is null' or 'is not null' if the right hand
        // side is a null literal
        if ((op == EQUAL || op == NOT_EQUAL) && right instanceof LiteralExp
                && ((LiteralExp) right).type == LiteralExp.TYPE_NULL) {
            if (op == EQUAL) {
                s.append(" is null");
            } else {
                s.append(" is not null");
            }
            return;
        }

        s.append(' ');
        firstCharIndex = s.size();
        s.append(driver.getSqlBinaryOp(op));
        s.append(' ');
        if (!removeConcat(driver, s, right)) {
            right.appendSQL(driver, s, childList);
        }

        // make space for 'is null' or 'is not null' if required
        if (childList.getNext() instanceof ParamExp) {
            if (op == EQUAL) {
                int n = s.size() - firstCharIndex;
                for (; n < 11; n++) s.append(' ');
            } else if (op == NOT_EQUAL) {
                int n = s.size() - firstCharIndex;
                for (; n < 11; n++) s.append(' ');
            }
        }
    }

    /**
     * If this is a LIKE and the driver does not handle LIKE well and the
     * right subtree contains a percent literal and a parameter then output
     * just the parameter with modification to add the percent before the
     * query runs.
     */
    private boolean removeConcat(SqlDriver driver, CharBuf s,
                                 SqlExp right) {
        if (op == LIKE && driver.isLikeStupid() && right instanceof BinaryOpExp
                && ((BinaryOpExp) right).isConcatParamAndPercent()) {
            ParamExp pe;
            int mod;
            if (right.childList instanceof ParamExp) {
                pe = (ParamExp) right.childList;
                mod = SqlStruct.Param.MOD_APPEND_PERCENT;
            } else {
                pe = (ParamExp) right.childList.getNext();
                mod = SqlStruct.Param.MOD_PREPEND_PERCENT;
            }
            pe.usage.mod = mod;
            pe.appendSQL(driver, s, null);
            return true;
        } else {
            return false;
        }
    }

    private boolean isConcatParamAndPercent() {
        if (op != CONCAT) return false;
        if (childList instanceof ParamExp) {
            return isLiteralPercent(childList.getNext());
        } else if (childList.getNext() instanceof ParamExp) {
            return isLiteralPercent(childList);
        }
        return false;
    }

    private static boolean isLiteralPercent(SqlExp e) {
        return e instanceof LiteralExp && ((LiteralExp) e).value.equals("%");
    }

    /**
     * Get the index of first character of the 'is null' parameter
     * replacement span for this expression.
     */
    public int getFirstCharIndex() {
        return firstCharIndex;
    }

    /**
     * Is this a negative expression for the purposes of replacing parameter
     * values with 'is null' (false) or 'is not null' (true)?
     */
    public boolean isNegative() {
        return op == NOT_EQUAL;
    }
}

