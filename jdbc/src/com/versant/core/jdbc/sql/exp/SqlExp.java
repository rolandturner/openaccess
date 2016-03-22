
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
import com.versant.core.jdo.query.ParamNode;
import com.versant.core.common.Debug;

import java.util.Map;
import java.util.HashMap;

import com.versant.core.common.BindingSupportImpl;

/**
 * An expression in an SQL query.
 */
public class SqlExp {

    /** Do not convert this expression into a join. **/
    public static final int NO = 0;
    /** Convert into a join. **/
    public static final int YES = 1;
    /** Convert into a join and select distinct. **/
    public static final int YES_DISTINCT = 2;
    /** Convert into an outer join with is null check and select distinct. **/
    public static final int YES_DISTINCT_NOT = 3;

    /**
     * This makes it easy to form linked lists of expressions. This is
     * faster and uses less memory than arrays of expressions.
     */
    private SqlExp next;
    /**
     * Linked list of children formed using their next fields.
     */
    public SqlExp childList;

    private int preFirstCharIndex;
    private int lastCharIndex;

    public SqlExp() {
    }

    public SqlExp createInstance() {
        return new SqlExp();
    }

    /**
     * Use this to clone a SqlExp tree.
     * @param sqlExp
     */
    public static SqlExp createClone(SqlExp sqlExp) {
        return createClone(sqlExp, new HashMap());
    }

    /**
     * This is a util method that decides if a instance should be cloned. If the
     * instance was already cloned then the already cloned instance is returned.
     * @param inst
     * @param cloneMap
     * @return A cloned instance.
     */
    public static SqlExp createClone(SqlExp inst, Map cloneMap) {
        if (inst == null) return null;
        if (cloneMap.containsKey(inst)) {
            return (SqlExp) cloneMap.get(inst);
        } else {
            cloneMap.put(inst, inst.createInstance());
        }
        return inst.getClone((SqlExp) cloneMap.get(inst), cloneMap);
    }

    /**
     * Clone the current instance. This method is called by subclassed to create a clone of its
     * superclass.
     *
     * @param clone The instance it must set the cloned values on.
     * @param cloneMap This map is used to avoid creating multiple clones for a singel instance.
     * @return The update SqlExp
     */
    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        if (next != null) clone.next = createClone(next, cloneMap);
        if (childList != null) clone.childList = createClone(childList, cloneMap);
        clone.preFirstCharIndex = preFirstCharIndex;
        clone.lastCharIndex = lastCharIndex;
        return clone;
    }

    public SqlExp(SqlExp childList) {
        this.childList = childList;
    }

    public String toString() {
        String n = getClass().getName();
        int i = n.lastIndexOf('.');
        if (i >= 0) n = n.substring(i + 1);
        return n + "@" + Integer.toHexString(System.identityHashCode(this));
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump(String indent) {
        Debug.OUT.println(indent + this);
        if (childList != null) childList.dumpList(indent + "  ");
    }

    /**
     * Dump us and our list to System.out.
     */
    public void dumpList(String indent) {
        dump(indent);
        for (SqlExp e = next; e != null; e = e.next) e.dump(indent);
    }

    /**
     * Create an aliases for any subtables we may have.
     */
    public int createAlias(int index) {
        for (SqlExp e = childList; e != null; e = e.next) {
            index = e.createAlias(index);
        }
        return index;
    }

    /**
     * Replace any references to old with nw. This is used when redundant
     * joins are removed.
     */
    public void replaceSelectExpRef(SelectExp old, SelectExp nw) {
        for (SqlExp e = childList; e != null; e = e.next) {
            e.replaceSelectExpRef(old, nw);
        }
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on. Return expression to
     * replace us with or null if no change.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        SqlExp p = null;
        for (SqlExp e = childList; e != null; e = e.next) {
            SqlExp r = e.normalize(driver, sel, convertExists);
            if (r == null) {
                p = e;
            } else {
                if (p == null)
                    childList = r;
                else
                    p.next = r;
                r.next = e.next;
            }
        }
        return null;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling The SqlExp to the left of us or null if none
     */
    public final void appendSQL(SqlDriver driver, CharBuf s,
            SqlExp leftSibling) {
        preFirstCharIndex = s.size();
        appendSQLImp(driver, s, leftSibling);
        lastCharIndex = s.size();
    }

    /**
     * Append SQL for this node to s. This is the method that subclasses
     * should override.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling The SqlExp to the left of us or null if none
     */
    protected void appendSQLImp(SqlDriver driver, CharBuf s,
            SqlExp leftSibling) {
    }

    /**
     * If this expression is added to an AndExp should it be enclosed in
     * parenthesis?
     */
    public boolean requiresParensInAnd() {
        return false;
    }

    /**
     * If this expression is added to an MultiplyExp should it be enclosed in
     * parenthesis?
     */
    public boolean requiresParensInMultiply() {
        return false;
    }

    /**
     * Get the index of first character of the this expression in the output
     * buffer. This is used when the whole expression needs to be replaced.
     */
    public final int getPreFirstCharIndex() {
        return preFirstCharIndex;
    }

    /**
     * Get the index of first character of the 'is null' parameter
     * replacement span for this expression.
     */
    public int getFirstCharIndex() {
        throw BindingSupportImpl.getInstance().internal("getFirstCharIndex called on " +
                this);
    }

    /**
     * Get the index of the character after the last character of the
     * 'is null' parameter replacement span for this expression.
     */
    public final int getLastCharIndex() {
        return lastCharIndex;
    }

    /**
     * Is this a negative expression for the purposes of replacing parameter
     * values with 'is null' (false) or 'is not null' (true)?
     */
    public boolean isNegative() {
        return false;
    }

    /**
     * What is the JDBC type of this expression (0 if unknown)?
     */
    public int getJdbcType() {
        return 0;
    }

    /**
     * What is the java type code of this expression (0 if unknown)?
     */
    public int getJavaTypeCode() {
        return 0;
    }

    /**
     * What is the class index for this expression (-1 if unknown)?
     * @see ParamNode
     */
    public int getClassIndex() {
        return -1;
    }

    /**
     * Can this expression be removed and its child be converted into a join?
     * @see ExistsExp
     * @see #NO
     * @see #YES
     * @see #YES_DISTINCT
     * @see #YES_DISTINCT_NOT
     */
    public int getConvertToJoin() {
        return NO;
    }

    /**
     * Add a list of expressions to the end of our childList.
     */
    public void append(SqlExp extra) {
        if (childList == null) {
            childList = extra;
        } else {
            SqlExp e;
            for (e = childList; e.next != null; e = e.next) ;
            e.next = extra;
        }
    }

    /**
     * Append e to base using an AndExp. If base is already an AndExp then
     * e is simply appended. If base is null then e becomes base. Otherwise
     * base becomes a a new AndExp created from the old base and e.
     * @return New value for base 
     */
    public static SqlExp appendWithAnd(SqlExp base, SqlExp e) {
        if (e == null) return base;
        if (base == null) {
            if (e.next == null) {
                base = e;
            } else {
                base = new AndExp(e);
            }
        } else if (base instanceof AndExp) {
            base.append(e);
        } else {
            base.next = e;
            base = new AndExp(base);
        }
        return base;
    }

    /**
     * Make us an outer join or not. This is a NOP except for JoinExp and
     * AndJoinExp.
     * @see JoinExp
     * @see AndJoinExp
     */
    public void setOuter(boolean on) {
    }

    /**
     * If this expression involves a single table (other than exclude) only
     * then return the SelectExp for the table (e.g. a.col1 == 10 returns a,
     * a.col1 = b.col2 returns null, a.col1 = b.col2 with exclude = b returns
     * a). This is used to detect expressions that can be moved
     * into the ON list for the join to the table involved.
     */
    public SelectExp getSingleSelectExp(SelectExp exclude) {
        return null;
    }

    /**
     * Create an expression comparing the left and right exp with an
     * operator. Only == and != really make sense. If left and right are
     * lists then comparing matching exp's joined together in an 'and'
     * list.
     */
    public static SqlExp createBinaryOpExp(SqlExp left, int op, SqlExp right) {
        if (left.next == null && right.next == null) {
            BinaryOpExp ans = new BinaryOpExp(left, op, right);
            if (right instanceof ParamExp) {
                ParamExp p = (ParamExp)right;
                p.usage.expList = ans;
                if (left instanceof ColumnExp) p.usage.expCount = 1;
            }
            return ans;
        } else {
            SqlExp l = left.next;
            SqlExp r = right.next;
            SqlExp list = new BinaryOpExp(left, op, right);
            AndExp ans = new AndExp(list);
            ParamExp pe;
            if (right instanceof ParamExp) {
                pe = (ParamExp)right;
                pe.usage.expList = list;
            } else {
                pe = null;
            }
            boolean colExp = left instanceof ColumnExp;
            int c = 1;
            for (; l != null && r != null; c++) {
                left = l;
                right = r;
                l = left.next;
                r = right.next;
                BinaryOpExp e = new BinaryOpExp(left, op, right);
                list = list.next = e;
            }
            if (l != null || r != null) {
                throw BindingSupportImpl.getInstance().internal(
                    "left and right lists have different length");
            }
            if (pe != null && colExp) pe.usage.expCount = c;
            return ans;
        }
    }

    public SqlExp getNext() {
        return next;
    }

    public SqlExp setNext(SqlExp next) {
        if (next == this) {
//            if (next.next == null) {
//                return this;
//            }
            throw new RuntimeException("Creating an infinite loop: " + this);
        } else if (next != null && next.next == this) {
            throw new RuntimeException("Creating an infinite loop: " + this);    
        }

        this.next = next;
        return next;
    }
}
