
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
package com.versant.core.metadata;

import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.common.Debug;

import java.io.Serializable;
import java.io.PrintStream;

/**
 * A field in a fetch group. This holds the field and assorted store
 * specific options.
 */
public class FetchGroupField implements Serializable {

    /**
     * The field.
     */
    public FieldMetaData fmd;
    /**
     * The parsed meta data for this field (null if none i.e. automatically
     * generated default fetch group).
     */
    public JdoExtension extension;
    /**
     * If a store does read ahead (e.g. a JDBC store using a join) and this
     * field is a reference to another PC class then get the fields in this
     * fetch group of that class. This will never be null as it is set to
     * the default fetch group if not specified.
     */
    public FetchGroup nextFetchGroup;
    /**
     * If a store does read ahead (e.g. a JDBC store using a join) and this
     * field is a map and the keys reference another PC class then get the
     * fields in this fetch group of that class. This will never be null as
     * it is set to the default fetch group if not specified.
     */
    public FetchGroup nextKeyFetchGroup;

    /**
     * This flag is used to include reference fields in a fetch group
     * and to fetch only the OID of the reference and not the referenced
     * object as well.
     */
    public boolean doNotFetchObject;

    /**
     * If this is a ref or a collection then this is the useJoin option
     * to use when this group is fetched. The fields picked up by the join
     * will be those in the nextFetchGroup of the referenced class.
     * Values are from JdbcField: USE_JOIN_NO USE_JOIN_INNER USE_JOIN_OUTER.
     * @see #nextFetchGroup
     */
    public int jdbcUseJoin;
    /**
     * If this is a map and the keys are a PC class then this is the useJoin
     * option to use when this group is fetched. The fields picked up by the
     * join will be those in the nextKeyFetchGroup of the referenced class.
     * Values are from JdbcField: USE_JOIN_NO USE_JOIN_INNER USE_JOIN_OUTER.
     * @see #nextKeyFetchGroup
     */
    public int jdbcUseKeyJoin;
    /**
     * Cache for SQL required to fetch this field. This is normally only
     * used for pass 2 fields e.g. collections and so on.
     */
    public String jdbcSelectSql;
    /**
     * Cache for SQL required to fetch this field when selectForUpdate is on.
     * This is normally only used for pass 2 fields e.g. collections and so on.
     */
    public String jdbcSelectSqlForUpdate;
    /**
     * The fg fields that was added because of this embedded fg reference field
     */
    public FetchGroupField[] embeddedNextFgFields;

    public FetchGroupField(FieldMetaData field) {
        this.fmd = field;
    }

    private static String toUseJoinString(int useJoin) {
        // dont use constants from JdbcRefField to avoid engine depending
        // on jdbc
        switch (useJoin) {
            case 1:
                return "NO";
            case 3:
                return "INNER";
            case 2:
                return "OUTER";
        }
        return "unknown(" + useJoin + ")";
    }

    public String toString() {
        return String.valueOf(fmd) +
            " jdbcUseJoin " + toUseJoinString(jdbcUseJoin) +
            " nextFetchGroup " + nextFetchGroup +
            (fmd.category == MDStatics.CATEGORY_MAP
                ? " jdbcUseKeyJoin " + toUseJoinString(jdbcUseKeyJoin) +
                  " nextKeyFetchGroup " + nextKeyFetchGroup
                : "");
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
    }
}
