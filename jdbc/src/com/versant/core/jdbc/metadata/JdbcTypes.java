
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
package com.versant.core.jdbc.metadata;

import com.versant.core.jdbc.sql.exp.LiteralExp;

import java.util.HashMap;
import java.sql.Types;

import com.versant.core.common.BindingSupportImpl;

/**
 * Static utility methods for converting the constants in java.sql.Types
 * to and from strings.
 */
public class JdbcTypes {

    // this maps the names of the type constants to Integer values
    private static final HashMap TYPE_NAME_MAP = new HashMap();

    static {
        TYPE_NAME_MAP.put("BIT", new Integer(-7));
        TYPE_NAME_MAP.put("TINYINT", new Integer(-6));
        TYPE_NAME_MAP.put("SMALLINT", new Integer(5));
        TYPE_NAME_MAP.put("INTEGER", new Integer(4));
        TYPE_NAME_MAP.put("BIGINT", new Integer(-5));
        TYPE_NAME_MAP.put("FLOAT", new Integer(6));
        TYPE_NAME_MAP.put("REAL", new Integer(7));
        TYPE_NAME_MAP.put("DOUBLE", new Integer(8));
        TYPE_NAME_MAP.put("NUMERIC", new Integer(2));
        TYPE_NAME_MAP.put("DECIMAL", new Integer(3));
        TYPE_NAME_MAP.put("CHAR", new Integer(1));
        TYPE_NAME_MAP.put("VARCHAR", new Integer(12));
        TYPE_NAME_MAP.put("LONGVARCHAR", new Integer(-1));
        TYPE_NAME_MAP.put("DATE", new Integer(91));
        TYPE_NAME_MAP.put("TIME", new Integer(92));
        TYPE_NAME_MAP.put("TIMESTAMP", new Integer(93));
        TYPE_NAME_MAP.put("BINARY", new Integer(-2));
        TYPE_NAME_MAP.put("VARBINARY", new Integer(-3));
        TYPE_NAME_MAP.put("LONGVARBINARY", new Integer(-4));
        TYPE_NAME_MAP.put("NULL", new Integer(0));
        TYPE_NAME_MAP.put("OTHER", new Integer(1111));
        TYPE_NAME_MAP.put("JAVA_OBJECT", new Integer(2000));
        TYPE_NAME_MAP.put("DISTINCT", new Integer(2001));
        TYPE_NAME_MAP.put("STRUCT", new Integer(2002));
        TYPE_NAME_MAP.put("ARRAY", new Integer(2003));
        TYPE_NAME_MAP.put("BLOB", new Integer(2004));
        TYPE_NAME_MAP.put("CLOB", new Integer(2005));
        TYPE_NAME_MAP.put("REF", new Integer(2006));
        
    }

    private JdbcTypes() { }

    /**
     * Convert s to a java.sql.Types constant value.
     * @exception IllegalArgumentException if s is invalid
     */
    public static int parse(String s) throws IllegalArgumentException {
        Integer i = (Integer)TYPE_NAME_MAP.get(s);
        if (i == null) {
            throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: '" + s + "'");
        }
        return i.intValue();
    }

    /**
     * Convert an int constant from java.sql.Types into the String name of
     * the constant.
     */
    public static String toString(int type) throws IllegalArgumentException {
        switch (type) {
            case Types.BIT:             return "BIT";
            case Types.TINYINT:         return "TINYINT";
            case Types.SMALLINT:        return "SMALLINT";
            case Types.INTEGER:         return "INTEGER";
            case Types.BIGINT:          return "BIGINT";
            case Types.FLOAT:           return "FLOAT";
            case Types.REAL:            return "REAL";
            case Types.DOUBLE:          return "DOUBLE";
            case Types.NUMERIC:         return "NUMERIC";
            case Types.DECIMAL:         return "DECIMAL";
            case Types.CHAR:            return "CHAR";
            case Types.VARCHAR:         return "VARCHAR";
            case Types.LONGVARCHAR:     return "LONGVARCHAR";
            case Types.DATE:            return "DATE";
            case Types.TIME:            return "TIME";
            case Types.TIMESTAMP:       return "TIMESTAMP";
            case Types.BINARY:          return "BINARY";
            case Types.VARBINARY:       return "VARBINARY";
            case Types.LONGVARBINARY:   return "LONGVARBINARY";
            case Types.NULL:            return "NULL";
            case Types.OTHER:           return "OTHER";
            case Types.JAVA_OBJECT:     return "JAVA_OBJECT";
            case Types.DISTINCT:        return "DISTINCT";
            case Types.STRUCT:          return "STRUCT";
            case Types.ARRAY:           return "ARRAY";
            case Types.BLOB:            return "BLOB";
            case Types.CLOB:            return "CLOB";
            case Types.REF:             return "REF";
            
        }
        throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: " + type);
    }

    /**
     * Return an indication of the cost of updating a jdbc type. Smaller values
     * are quicker to update (e.g int, short etc) while blobs are very
     * expensive.
     */
    public static int getUpdateCost(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:             return 0;
            case Types.TINYINT:         return 1;
            case Types.SMALLINT:        return 2;
            case Types.INTEGER:         return 3;
            case Types.BIGINT:          return 4;
            case Types.FLOAT:           return 5;
            case Types.REAL:            return 6;
            case Types.DOUBLE:          return 7;
            case Types.NUMERIC:         return 8;
            case Types.DECIMAL:         return 9;
            case Types.DATE:            return 10;
            case Types.TIME:            return 11;
            case Types.TIMESTAMP:       return 12;
            case Types.VARCHAR:         return 13;
            case Types.CHAR:            return 14;
            case Types.VARBINARY:       return 16;
            case Types.BINARY:          return 17;
            case Types.LONGVARCHAR:     return 18;
            case Types.LONGVARBINARY:   return 19;
            case Types.CLOB:            return 20;
            case Types.BLOB:            return 21;
            case Types.ARRAY:           return 22;
            case Types.REF:             return 23;
            case Types.JAVA_OBJECT:     return 24;
            case Types.OTHER:           return 25;
            case Types.STRUCT:          return 26;
            case Types.DISTINCT:        return 27;
            case Types.NULL:            return 28;
            
        }
        throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: " + jdbcType);
    }

    /**
     * Get the type of SQL literal required to compare to a column of jdbcType.
     * @see LiteralExp.TYPE_OTHER
     * @see LiteralExp.TYPE_STRING
     */
    public static int getLiteralType(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return LiteralExp.TYPE_OTHER;
        }
        return LiteralExp.TYPE_STRING;
    }

}
