
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

import java.util.Map;

import com.versant.core.common.BindingSupportImpl;

/**
 * Inline SQL with parameter replacement for argument column names.
 */
public class InlineSqlExp extends SqlExp {

    private String template;

    public InlineSqlExp(String template, SqlExp children) {
        super(children);
        this.template = template;
    }

    public InlineSqlExp() {
    }

    public SqlExp createInstance() {
        return new InlineSqlExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        super.getClone(clone, cloneMap);

        ((InlineSqlExp) clone).template = template;

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
        int n = template.length();
        int state = 0;
        for (int i = 0; i < n; i++) {
            char c = template.charAt(i);
            int pn;
            switch (state) {
                case 0:
                    if (c == '$')
                        state = '$';
                    else
                        s.append(c);
                    break;
                case '$':
                    pn = 0;
                    switch (c) {
                        default:
                            s.append('$');
                        case '$':
                            s.append(c);
                            break;
                        case '1':
                            pn = 1;
                            break;
                        case '2':
                            pn = 2;
                            break;
                        case '3':
                            pn = 3;
                            break;
                        case '4':
                            pn = 4;
                            break;
                        case '5':
                            pn = 5;
                            break;
                        case '6':
                            pn = 6;
                            break;
                        case '7':
                            pn = 7;
                            break;
                        case '8':
                            pn = 8;
                            break;
                        case '9':
                            pn = 9;
                            break;
                        case '0':
                            pn = 10;
                            break;
                    }
                    if (pn > 0) get(pn).appendSQL(driver, s, null);
                    state = 0;
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("Unknown state: " + state);
            }
        }
        if (state == '$') s.append('$');
    }

    /**
     * Get the n'th entry in list with the first having index 1.
     */
    private SqlExp get(int idx) {
        SqlExp list = childList;
        for (int n = idx; list != null && --n > 0; list = list.getNext()) ;
        if (list == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("Invalid expression index: $" +
                    (idx == 10 ? "0 (10)" : Integer.toString(idx)) +
                    " in '" + template + "'");
        }
        return list;
    }

}

