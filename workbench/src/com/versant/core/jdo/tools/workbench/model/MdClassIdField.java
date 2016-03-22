
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
 * This is a javabean wrapper for a jdbc-class-id extension for a class.
 * It extends MdField as the instance needs to appear in the fields list in
 * the gui.
 *
 * @keep-all
 */
public class MdClassIdField extends MdSpecialField {

    private static final String DIALOG_HELP =
            "Open Access supports inheritance by mapping all classes " +
            "in the hierarchy to the table for the base class. A 'int' class-id " +
            "(also known as a discriminator) column is added to the table to " +
            "identify the type of each row.";

    public void analyze() {
        super.analyze();
        category = MDStatics.CATEGORY_CLASS_ID;
    }

    public String getName() {
        return JdbcMetaDataBuilder.CLASS_ID_FIELDNAME;
    }

    public String getTypeStr() {
        return "int";
    }

    public String getShortTypeStr() {
        return "int";
    }

    public MdValue getJdbcConstraint() {
        return MdValue.NA;
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
            if (jdbcClass == null) {
                col.setJdbcColumn(null);
            } else {
                col.setJdbcColumn(jdbcClass.classIdCol);
            }
        }
    }

    public String getDialogHelp() {
        if (mdClass.getMdDataStore().isVds()) {
            return "VDS does not use classid";
        }
        return DIALOG_HELP;
    }

    public String getMiniClassDef() {
        StringBuffer s = new StringBuffer();
        s.append("<html><b><code>&nbsp;&nbsp;");
        s.append(cleanupHTML(getMiniQName()));
        s.append("</code></b>");
        s.append(" Automatically added descriminator column to " +
                "identify the type of each row");
        s.append("</body></html>");
        return s.toString();
    }

    public String getTreeIcon() {
        return ICON_CLASS_ID;
    }

}

