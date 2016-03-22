
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
import com.versant.core.jdbc.metadata.JdbcTypes;
import com.versant.core.util.CharBuf;

/**
 */
public class CollectionParamExp extends ParamExp {
    public ColumnExp field;

    public CollectionParamExp(ColumnExp field) {
        this.field = field;
    }

    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        firstCharIndex = s.size();
        field.appendSQL(driver, s, null);
        s.append(" in (");
        firstCharIndex = s.size();
        s.append(')');
    }

    public String toString() {
        String n = getClass().getName();
        int i = n.lastIndexOf('.');
        if (i >= 0) n = n.substring(i + 1);
        return n + "@" + Integer.toHexString(System.identityHashCode(this))
            + " " + JdbcTypes.toString(jdbcType);
    }
}
