
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

/**
 * An expression with two args.
 */
public class BinaryExp extends SqlExp {

    public BinaryExp(SqlExp left, SqlExp right) {
        super(left);
        left.setNext(right);
        right.setNext(null);
    }

    public BinaryExp() {
    }

    public SqlExp createInstance() {
        return new BinaryExp();
    }

    /**
     * Create an aliases for any subtables we may have.
     */
    public int createAlias(int index) {
        return childList.getNext().createAlias(childList.createAlias(index));
    }

    /**
     * Replace any references to old with nw. This is used when redundant
     * joins are removed.
     */
    public void replaceSelectExpRef(SelectExp old, SelectExp nw) {
        childList.replaceSelectExpRef(old, nw);
        childList.getNext().replaceSelectExpRef(old, nw);
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        SqlExp r = childList.normalize(driver, sel, convertExists);
        if (r != null) {
            r.setNext(childList.getNext());
            childList = r;
        }
        r = childList.getNext().normalize(driver, sel, convertExists);
        if (r != null) {
            childList.setNext(r);
        }
        return null;
    }

    /**
     * If this expression involves a single table only then return the
     * SelectExp for the table (e.g. a.col1 == 10 returns a, a.col1 = b.col2
     * returns null). This is used to detect expressions that can be moved
     * into the ON list for the join to the table involved.
     * @param exclude
     */
    public SelectExp getSingleSelectExp(SelectExp exclude) {
        SelectExp a = childList.getSingleSelectExp(exclude);
        SelectExp b = childList.getNext().getSingleSelectExp(exclude);
        if (b == null || b == exclude) return a;
        if (a == null || a == exclude) return b;
        return a == b ? a : null;
    }

}
