
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
 * This is a javabean wrapper for a jdbc-optimstic-locking extension for a
 * class. It extends MdField as the instance needs to appear in the fields
 * list in the gui.
 *
 * @keep-all
 */
public class MdOptLockingField extends MdSpecialField {

    private static final String DIALOG_HELP =
            "Optimistic transactions are supported by " +
            "adding a version or timestamp column to the table for the class " +
            "or by including the old values of fields in the where clause. " +
            "Three different optimistic locking modes are available " +
            "and optimistic locking may be disabled. The optimistic locking column can " +
            "be an existing timestamp or int field in your class or one may " +
            "be added automatically. Use the class properties window to configure " +
            "optimistic locking for this class.";

    public void analyze() {
        super.analyze();
        category = MDStatics.CATEGORY_OPT_LOCKING;
    }

    public String getName() {
        return JdbcMetaDataBuilder.OPT_LOCK_FIELDNAME;
    }

    public String getTypeStr() {
        String v = element.getAttributeValue("value");
        if (v == null) v = mdClass.getMdDataStore().getJdbcOptimisticLockingStr();
        if (v == null) {
            return "?";
        } else if (v.equals("version")) {
            return "int";
        } else {
            return "java.util.Date";
        }
    }

    /**
     * This is the type used to resolve the JDBC mapping for this column.
     */
    public String getMappingTypeStr() {
        String s = getTypeStr();
        if (s.equals("int")) s = "short";
        return s;
    }

    public String getShortTypeStr() {
        return getTypeStr();
    }

    public MdValue getJdbcConstraint() {
        return MdValue.NA;
    }

    protected void findFieldMetaData(ClassMetaData classMetaData) {
        JdbcClass jdbcClass = (JdbcClass)classMetaData.storeClass;
        if (jdbcClass == null) {
            fmd = null;
            jdbcField = null;
        } else {
            jdbcField = jdbcClass.optimisticLockingField;
            if (jdbcField != null) fmd = jdbcField.fmd;
        }
    }

    public String getDialogHelp() {
        if (mdClass.getMdDataStore().isVds()) {
            return "VDS opt lock";
        }
        return DIALOG_HELP;
    }

    /**
     * Get the short type of this field with added generic info if available.
     */
    public String getGenericTypeStr() {
        return "Automatically added column for optimistic locking";
    }

    public String getTreeIcon() {
        return ICON_VERSION;
    }

}

