
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

import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdo.query.ParamNode;

import java.util.Map;

/**
 * This tracks usage of this parameter in an SqlExp tree.
 * A parameter may be used several times in the same tree if it is
 * used more than once in the original filter expression. This will be
 * null if the param is not used.<p>
 *
 * A parameter may also have to be split into more than one SqlExp
 * if it is compared to a field with multiple columns. The first
 * expression is expList. The expcount field specifies how far down
 * the list to go as expList may be part of a larger list.
 *
 * @see com.versant.core.jdo.query.ParamNode#usageList
 */
public class ParamNodeUsage {

    /**
     * Add us to the end of the usage list for n.
     */
    public void addToParamNode(ParamNode n) {
        if (n.usageList == null) {
            n.usageList = this;
        } else {
            ParamNodeUsage p = (ParamNodeUsage)n.usageList;
            for (; p.next != null; p = p.next);
            p.next = this;
        }
    }

    /**
     * The field for this parameter or null if it is not being compared
     * to a field.
     * @see #javaTypeCode
     * @see #jdbcType
     */
    public JdbcField jdbcField;

    public JdbcColumn col;
    /**
     * The java type code of this usage of the parameter. This is not
     * set if jdbcField is not null.
     * @see #jdbcField
     */
    public int javaTypeCode;
    /**
     * The JDBC type of this usage of the parameter. This is not
     * set if jdbcField is not null.
     * @see java.sql.Types
     * @see #jdbcField
     */
    public int jdbcType;
    /**
     * The class index of the class for this parameter (-1 if unknown).
     * This is used for OID parameters compared to columns that are
     * not associated with a field e.g. in a link table.
     */
    public int classIndex;
    /**
     * The first expression for this usage of the parameter. This is
     * the parent of the first ParamExp instance for this usage.
     */
    public SqlExp expList;
    /**
     * The number of expressions in expList for this usage of the
     * parameter. If a param is used with a field split over multiple
     * columns then this will be more than 1. This will be zero if
     * the parameter never has to be converted into a 'is null' or
     * 'is not null' expression.
     */
    public int expCount;
    /**
     * How must the parameter value be modified before being set? This is
     * used for startsWith and endsWith for databases that do not allow
     * expressions on the right hand side of a LIKE (e.g. Informix).
     * @see com.versant.core.jdbc.query.SqlStruct.Param#MOD_APPEND_PERCENT
     */
    public int mod;
    /**
     * The next usage in the list.
     */
    public ParamNodeUsage next;

    public ParamNodeUsage getClone(Map cloneMap) {
        ParamNodeUsage sqlUsage = new ParamNodeUsage();
        sqlUsage.jdbcField = jdbcField;
        sqlUsage.javaTypeCode = javaTypeCode;
        sqlUsage.jdbcType = jdbcType;
        sqlUsage.classIndex = classIndex;
        sqlUsage.expCount = expCount;
        sqlUsage.mod = mod;
        sqlUsage.expList = SqlExp.createClone(expList, cloneMap);
        if (next != null) {
            sqlUsage.next = next.getClone(cloneMap);
        }
        return sqlUsage;
    }
}

