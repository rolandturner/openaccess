
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
package com.versant.core.jdbc;

import com.versant.core.metadata.*;
import com.versant.core.jdbc.metadata.JdbcFetchGroup;
import com.versant.core.jdbc.metadata.JdbcField;

import java.util.ArrayList;

/**
 * JDBC specific tweaks to how the FetchGroup's are constructed.
 */
public class JdbcFetchGroupBuilder extends FetchGroupBuilder {

    public JdbcFetchGroupBuilder(ModelMetaData jmd) {
        super(jmd, false, false);
    }

    protected StoreFetchGroup createStoreFetchGroup() {
        return new JdbcFetchGroup();
    }

    protected FetchGroupField createFetchGroupFieldWithPrefetch(
            FieldMetaData fmd) {
        FetchGroupField fgf = super.createFetchGroupFieldWithPrefetch(fmd);
        if (fmd.category == MDStatics.CATEGORY_REF) {
            fgf.jdbcUseJoin = JdbcField.USE_JOIN_OUTER;
        }
        return fgf;
    }

    protected FetchGroup createAllColumnsFetchGroup(ClassMetaData cmd) {
        FetchGroup g = new FetchGroup(cmd, FetchGroup.ALL_COLS_NAME, createStoreFetchGroup());
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                continue;
            }
            if (fmd.isEmbeddedRef()) {
                continue;
            }
            JdbcField jdbcField = (JdbcField)fmd.storeField;
            if (jdbcField.mainTableCols == null || jdbcField.mainTableCols.length == 0) {
                continue;
            }
            FetchGroupField fgf = new FetchGroupField(fmd);
            if (fmd.category == MDStatics.CATEGORY_REF) {
                fgf.jdbcUseJoin = JdbcField.USE_JOIN_NO;
            }
            a.add(fgf);
        }
        n = a.size();
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

}

