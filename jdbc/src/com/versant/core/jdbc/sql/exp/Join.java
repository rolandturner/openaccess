
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

import java.util.Map;

/**
 * A join to a table.
 * @see SelectExp
 */
public class Join {

    /**
     * The table being joined to.
     */
    public SelectExp selectExp;
    /**
     * The expression that joins the tables.
     */
    public SqlExp exp;
    /**
     * The next join in the list.
     */
    public Join next;
    /**
     * If this join was merged/replaced with another join because it is equal.
     */
    private Join mergedWith;

    public Join() {
    }

    public Join getClone(Map cloneMap) {
        Join clone = new Join();
        if (selectExp != null) clone.selectExp = (SelectExp) SqlExp.createClone(selectExp, cloneMap);
        if (exp != null) clone.exp = SqlExp.createClone(exp, cloneMap);
        if (next != null) clone.next = next.getClone(cloneMap);
        return clone;
    }

    public String toString() {
        return "Join@" + System.identityHashCode(this) + " " + selectExp;
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump(String indent) {
        System.out.println(indent + this);
        indent = indent + "  ";
        if (exp == null) {
            System.out.println(indent + " exp is NULL");
        } else {
            exp.dump(indent);
        }
        selectExp.dump(indent);
    }

    /**
     * Dump us and our list to System.out.
     */
    public void dumpList(String indent) {
        dump(indent);
        for (Join j = next; j != null; j = j.next) j.dump(indent);
    }

    /**
     * Create an aliases for any tables we may have.
     */
    public int createAlias(int index) {
        if (mergedWith != null) {
            index = mergedWith.createAlias(index);
            selectExp.alias = mergedWith.selectExp.alias;
        }

        index = selectExp.createAlias(index);
        if (exp != null) {
            return exp.createAlias(index);
        } else {
            return index;
        }
    }

    /**
     * Add an extra expression to our exp. This will be joined with any
     * existing exp using and.
     */
    public void appendJoinExp(SqlExp e) {
        if (exp == null) {
            exp = e;
        } else if (exp instanceof AndExp) {
            exp.append(e);
        } else {
            AndExp ae = new AndExp(exp);
            exp.setNext(e);
            exp = ae;
        }
    }

    /**
     * Find the deepest Join from us down. This will return this if there
     * are no joins from our selectExp.
     */
    public Join findDeepestJoin() {
        if (selectExp.joinList == null) return this;
        Join j;
        for (j = selectExp.joinList; j.next != null; j = j.next);
        return j.findDeepestJoin();
    }

    /**
     * If the join tree's are equivalent.
     */
    public static boolean isEqaul(Join j1, Join j2) {
        if (j1 == j2) return true;
        if (j1 == null) return false;
        if (j2 == null) return false;

        if (j1.selectExp != null && j2.selectExp != null) {
            if (j1.selectExp.jdbcField == j2.selectExp.jdbcField) {
                if (j1.exp instanceof JoinExp && j2.exp instanceof JoinExp) {
                    if (JoinExp.isEqual((JoinExp)j1.exp, (JoinExp)j2.exp)) {
                        if (isEqaul(j1.selectExp.joinList, j2.selectExp.joinList)) {
                            return Join.isEqaul(j1.next, j2.next);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Only check if the actual two joins are equal and ignore next joins.
     */
    public static boolean isCurrentEqaul(Join j1, Join j2) {
        if (j1 == j2) return true;
        if (j1 == null) return false;
        if (j2 == null) return false;
        
        if (j1.selectExp != null && j2.selectExp != null) {
            if (j1.selectExp.jdbcField == j2.selectExp.jdbcField) {
                if (j1.exp instanceof JoinExp && j2.exp instanceof JoinExp) {
                    return JoinExp.isEqual((JoinExp)j1.exp, (JoinExp)j2.exp);
                }
            }
        }
        return false;
    }

    /**
     * Return true if this join is only acting as a place holder for the selectList
     */
    public boolean isMerged() {
        return mergedWith != null;
    }

    public Join getMergedWith() {
        return mergedWith;
    }

    public void setMergedWith(Join mergedWith) {
        this.mergedWith = mergedWith;
    }
}
