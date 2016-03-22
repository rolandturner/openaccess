
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
import com.versant.core.jdo.query.VarNodeIF;

import java.util.Map;

/**
 * An exists (....) expression.
 */
public class ExistsExp extends UnaryExp {
    /**
     * If this exp was create for a oneToMany scenario.
     */
    private boolean distinct;
    private VarNodeIF forVariable;

    public ExistsExp(SqlExp child, boolean requiresDistinct) {
        super(child);
        this.distinct = requiresDistinct;
    }

    public ExistsExp(SqlExp child, boolean requiresDistinct, VarNodeIF var) {
        this(child, requiresDistinct);
        this.forVariable = var;
    }

    public ExistsExp() {
    }

    public SqlExp createInstance() {
        return new ExistsExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((ExistsExp) clone).distinct = distinct;

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
        s.append("EXISTS (");
        childList.appendSQL(driver, s, null);
        s.append(')');
    }

    /**
     * Can this expression be removed and its child be converted into a join?
     * @see ExistsExp
     */
    public int getConvertToJoin() {
        if (!distinct) return YES;
        if (forVariable != null && forVariable.isUsedInProjection()) return YES;
        return YES_DISTINCT;
    }

    public String toString() {
        return super.toString() + " oneToMany " + distinct;
    }

}

