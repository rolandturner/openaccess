
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
package com.versant.core.jdo.tools.workbench.model;

import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;

/**
 * This is a javabean wrapper for a jdbc-primary-key extension for a class
 * with datastore identity. It extends MdField as the instance needs to
 * appear in the fields list in the gui.
 *
 * @keep-all
 */
public class MdDatastorePKField extends MdSpecialField {

    private static final String DIALOG_HELP =
            "The default primary key column for a datastore identity class " +
            "is a SQL INTEGER but can be changed to any int compatible " +
            "column (e.g. SMALLINT or BYTE). Primary key values are provided " +
            "by a key generator. The default key generator (HIGHLOW) uses a last " +
            "used number table to generate new numbers. User written key " +
            "generators are also supported. Use the class properties window to " +
            "change the identity type or configure key generation for this class.";

    public void analyze() {
        super.analyze();
        category = MDStatics.CATEGORY_DATASTORE_PK;
    }

    protected boolean checkEmpty() {
        return element.getContentSize() == 0;
    }

    public String getName() {
        return JdbcMetaDataBuilder.DATASTORE_PK_FIELDNAME;
    }

    public String getTypeStr() {
        return "int";
    }

    public String getShortTypeStr() {
        return "int";
    }

    public boolean isPrimaryKeyField() {
        return true;
    }

    public MdValue getPrimaryKey() {
        MdValue v = createMdValue(null);
        v.setDefText("true");
        return v;
    }

    /**
     * This is called when the meta data has been parsed. The param will be
     * null if meta data parsing failed.
     */
    public void setClassMetaData(ClassMetaData classMetaData) {
        if (col == null) return;
        if (classMetaData == null) {
            col.setJdbcColumn(null);
        } else {
            JdbcClass jdbcClass = (JdbcClass)classMetaData.storeClass;
            if (jdbcClass == null || jdbcClass.table == null) {
                col.setJdbcColumn(null);
            } else {
                col.setJdbcColumn(jdbcClass.table.pk[0]);
            }
        }
    }

    public String getDialogHelp() {
        if (mdClass.getMdDataStore().isVds()) {
            return "VDS pk";
        }
        return DIALOG_HELP;
    }

    /**
     * Get the short type of this field with added generic info if available.
     */
    public String getGenericTypeStr() {
        return "Automatically added primary key column for datastore identity";
    }

    public String getTreeIcon() {
        return ICON_PRIMARY_KEY;
    }

}

