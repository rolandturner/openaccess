
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

import com.versant.core.common.BindingSupportImpl;

/**
 * An 'and' expression.
 */
public class AndExp extends SqlExp {

    public AndExp() {
    }

    public AndExp(SqlExp children) {
        super(children);
    }

    public SqlExp createInstance() {
        return new AndExp();
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s      Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        appendSQL(childList, driver, s, null);
        SqlExp prev = childList;
        for (SqlExp e = childList.getNext(); e != null; prev = e, e = e.getNext()) {
            s.append(" AND ");
            appendSQL(e, driver, s, prev);
        }
    }

    private void appendSQL(SqlExp e, SqlDriver driver, CharBuf s,
            SqlExp leftSibling) {
        boolean p = e.requiresParensInAnd();
        if (p) s.append('(');
        e.appendSQL(driver, s, leftSibling);
        if (p) s.append(')');
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on. Return expression to
     * replace us with or null if no change.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        super.normalize(driver, sel, convertExists);

        if (!driver.isConvertExistsToDistinctJoin()) return null;

        SqlExp p = null;
        for (SqlExp e = childList; e != null;) {
            int cj = e.getConvertToJoin();
            if (cj >= SqlExp.YES) {
                // convert to join with distinct
                boolean not = cj == SqlExp.YES_DISTINCT_NOT;
                SelectExp sub;
                if (not) {
                    sub = (SelectExp)(e.childList.childList);
                } else {
                    sub = (SelectExp)(e.childList);
                }
                Join j = new Join();
                j.selectExp = sub;
                j.exp = sub.subSelectJoinExp;
                sub.subSelectJoinExp = null;
                sel.addJoinMerge(j);
                if (!sel.distinct) {
                    sel.distinct = cj >= SqlExp.YES_DISTINCT;
                }
                if (not) {
                    sub.setOuterRec();
                    if (sub.whereExp != null) {
                        throw BindingSupportImpl.getInstance().fatalDatastore("Query too complex for " + driver.getName());
                    }
                    // replace us in childlist with exp to check row not matched
                    SqlExp ne = sub.getOuterJoinNotMatchedExp();
                    if (p == null) {
                        p = childList = ne;
                    } else {
                        p = p.setNext(ne);
                    }
                    e = ne.setNext(e.getNext());
                } else {
                    SqlExp ne = sub.whereExp;
                    if (ne == null) {
                        ne = sub.getOuterJoinMatchedExp();
                    } else {
                        sub.whereExp = null;
                    }
                    // replace us in childlist with ne
                    if (p == null) {
                        p = childList = ne;
                    } else {
                        p = p.setNext(ne);
                    }
                    e = ne.setNext(e.getNext());
                }
            } else {
                p = e;
                e = e.getNext();
            }
        }

        // see if we can get rid of the AndExp
        if (childList == null) return null; // should return dummy exp
        if (childList.getNext() == null) return childList;
        return null;
    }

}
