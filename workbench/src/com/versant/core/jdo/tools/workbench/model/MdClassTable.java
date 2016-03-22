
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.metadata.JdbcClass;

/**
 * A table for a class. This puts in the primary key of the class. Additional
 * columns and references can be added after that.
 */
public class MdClassTable extends MdTable {

    private MdClassTable superTable;
    private MdClass mdClass;

    public void init(MdClass cls) {
        init(cls, false);
    }

    public void init(MdClass cls, boolean allColumns) {
        initImp(cls, allColumns, true);
    }

    private void initImp(MdClass cls, boolean allColumns, boolean superClasses) {
        this.element = cls.getElement();
        while (!cls.isNoPCSuperclass() && !cls.isVerticalInheritance()) {
            MdClass pcSuperclassMdClass = cls.getPcSuperclassMdClass();
            if (pcSuperclassMdClass == null) {
                break;
            }else{
                cls = pcSuperclassMdClass;
            }
        }
        this.mdClass = cls;
        ClassMetaData cmd = cls.getClassMetaData();
        JdbcClass jc = cmd == null ? null : (JdbcClass)cmd.storeClass;
        StringBuffer comment = new StringBuffer();
        comment.append("Table for ");
        comment.append(cls.getName());
        if (cls.getJdbcDoNotCreateTableBool()) {
            comment.append(" (do not create in schema)");
        }
        super.init(cls.getElement(), jc == null ? null : jc.table,
                cls.getName(), comment.toString());
        cls = cls.getTableClass();
        if (allColumns) {
            cls.addColumnsToTable(this);
        } else {
            cls.addPKColumnsToTable(this);
        }
        if (superClasses) {
            MdClass superCls = cls.getPcSuperclassMdClass();
            if (superCls == null) {
                superTable = null;
            } else {
                if (superTable == null) superTable = new MdClassTable();
                superTable.initImp(superCls, allColumns, true);
                addRef(cls.getSuperRef(this, superTable));
            }
        }
    }

    /**
     * If one of this classes superclasses is in a different table then this
     * is that table (vertical inheritance) else null.
     */
    public MdClassTable getSuperTable() {
        return superTable;
    }

    public MdValue getJdbcDoNotCreateTable() {
        MdValue v = new MdValue(getJdbcDoNotCreateTableStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(mdClass.getMdDataStore().getJdbcDoNotCreateTableStr());
        return v;
    }

    /**
     * Is this table flagged as 'do not create'?
     */
    public boolean isDoNotCreate() {
        String s = getJdbcDoNotCreateTableStr();
        if (s == null && mdClass != null && mdClass.getMdDataStore() != null) {
            s = mdClass.getMdDataStore().getJdbcDoNotCreateTableStr();
        }
        return "true".equals(s);
    }

}
