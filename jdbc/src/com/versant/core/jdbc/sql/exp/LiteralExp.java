
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
import com.versant.core.jdbc.sql.conv.DummyPreparedStmt;
import com.versant.core.metadata.MDStatics;

import java.sql.Types;
import java.sql.SQLException;
import java.util.Map;

/**
 * A literal value (String, number etc).
 */
public class LiteralExp extends LeafExp {

    public static final int TYPE_STRING = 1;
    public static final int TYPE_OTHER = 2;
    public static final int TYPE_NULL = 3;
    public static final int TYPE_BOOLEAN = 4;

    public int type;
    public String value;

    public LiteralExp(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public LiteralExp() {
    }

    public SqlExp createInstance() {
        return new LiteralExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((LiteralExp) clone).type = type;
        ((LiteralExp) clone).value = value;

        return clone;
    }

    public LiteralExp(int value) {
        this(TYPE_OTHER, Integer.toString(value));
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(' ');
        if (type == TYPE_STRING) s.append('\'');
        s.append(value);
        if (type == TYPE_STRING) s.append('\'');
        return s.toString();
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        if (type == TYPE_BOOLEAN && leftSibling instanceof ColumnExp) {
            // The idea here is to convert the boolean literal 'true' or
            // 'false' via the converter on the field that it is being compared
            // to.
            ColumnExp cExp = (ColumnExp)leftSibling;
            if (cExp.col.converter != null) {
                DummyPreparedStmt pstmt = new DummyPreparedStmt();
                try {
                    cExp.col.converter.set(pstmt, 0, cExp.col,
                            new Boolean(value));
                } catch (SQLException e) {
                    //ignore
                }
                value = pstmt.value;
                if (pstmt.toQuote) {
                    type = LiteralExp.TYPE_STRING;
                } else {
                    type = LiteralExp.TYPE_OTHER;
                }
            }
        }
        driver.appendSqlLiteral(type, value, s);
    }

    /**
     * What is the JDBC type of this expression (0 if unknown)?
     */
    public int getJdbcType() {
        if (type == TYPE_STRING) return Types.VARCHAR;
        return 0;
    }

    /**
     * What is the java type code of this expression (0 if unknown)?
     */
    public int getJavaTypeCode() {
        if (type == TYPE_STRING) return MDStatics.STRING;
        return 0;
    }
}
