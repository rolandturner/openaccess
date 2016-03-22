
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcColumn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 * This is used by the JdbcMetaDataBuilder to decide which columns are shared
 * when several fields in a class hierarchy are mapped with the same column
 * names.
 *
 */
public class SharedColumnChooser implements Comparator {

    public SharedColumnChooser() {
    }

    /**
     * Select shared columns for cmd and all of its subclasses.
     */
    public void chooseSharedColumns(ClassMetaData cmd) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        ArrayList fieldList = new ArrayList();

        for (int i = 0; i < jdbcClass.fields.length; i++) {
            JdbcField f = jdbcClass.fields[i];
            if (f == null) continue;
            JdbcColumn[] mtc = f.mainTableCols;
            if (mtc == null) continue;

            // decide on shared for each main table column for this field
            for (int j = 0; j < mtc.length; j++) {
                JdbcColumn c = mtc[j];
                if (c.shared) continue; // already shared

                // if this column is part of the primary key then any non-pk
                // fields using it must have their column set shared = true
                if (c.pk) {
                    jdbcClass.markColumnsShared(c.name, jdbcClass.table);
                } else {

                    // find all the fields we have that use this column and
                    // sort them so the the only field that will have
                    // shared = false is first in the list
                    fieldList.clear();
                    jdbcClass.findFieldsForColumn(f.fmd.classMetaData, c.name,
                            fieldList);
                    int n = fieldList.size();
                    if (n > 1) {
                        Collections.sort(fieldList, this);
                        for (int k = 1; k < n; k++) {
                            JdbcField kf = ((JdbcField)fieldList.get(k));
                            kf.findMainTableColumn(c.name).setShared(true);
                        }
                    }
                }

                // mark the column shared in all subclass fields
                jdbcClass.markSubclassColumnsShared(c.name);
            }
        }

        if (((JdbcClass)cmd.storeClass).classIdCol != null) {
            jdbcClass.markColumnsShared(jdbcClass.classIdCol.name, jdbcClass.table);
        }

        //if (Debug.DEBUG) dump(jdbcClass);

        // now do all of our subclasses
        if (cmd.pcSubclasses != null) {
            for (int i = 0; i < cmd.pcSubclasses.length; i++) {
                chooseSharedColumns(cmd.pcSubclasses[i]);
            }
        }
    }

    /**
     * Order JdbcField's so the field that should have shared=false for
     * columns in common is first.
     */
    public int compare(Object o1, Object o2) {
        JdbcField a = (JdbcField)o1;
        JdbcField b = (JdbcField)o2;

        // put simple fields first
        boolean simpleA = a.fmd.category == MDStatics.CATEGORY_SIMPLE;
        boolean simpleB = b.fmd.category == MDStatics.CATEGORY_SIMPLE;
        if (simpleA && !simpleB) return -1;
        if (!simpleA && simpleB) return +1;

        // put the field with the least columns first
        int diff = a.mainTableCols.length - b.mainTableCols.length;
        if (diff != 0) return diff < 0 ? -1 : +1;

        // order by fieldNo (i.e. alpha sort on name)
        return a.fmd.fieldNo - b.fmd.fieldNo;
    }

    private void dump(JdbcClass jdbcClass) {
        System.out.println("\nSharedColumnChooser.dump " + jdbcClass);
        for (int i = 0; i < jdbcClass.fields.length; i++) {
            JdbcField f = jdbcClass.fields[i];
            System.out.println("fields[" + i + "] = " + f);
            if (f == null) continue;
            JdbcColumn[] mtc = f.mainTableCols;
            if (mtc == null) continue;
            for (int j = 0; j < mtc.length; j++) {
                JdbcColumn c = mtc[j];
                System.out.println("  mtc[" + j + "] = " + c);
            }
        }
        System.out.println("---\n");
    }

}


