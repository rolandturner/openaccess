
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

import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdbc.metadata.JdbcLinkCollectionField;
import com.versant.core.jdbc.metadata.JdbcMapField;
import com.versant.core.jdbc.metadata.JdbcTable;

/**
 * A link table for a field. This includes tables for the owner, key and value
 * as needed.
 *
 * @keep-all
 */
public class MdLinkTable extends MdTable 
   
{

    private JdbcTable jdbcTable;
    private boolean ordered;

    private MdClassTable ownerTable = new MdClassTable();
    private MdClassTable keyTable = new MdClassTable();
    private MdClassTable valueTable = new MdClassTable();

    private MdJdbcRef refOwner = new MdJdbcRef();
    private MdJdbcRef refKey = new MdJdbcRef();
    private MdJdbcRef refValue = new MdJdbcRef();

    private MdColumn keyCol = new MdColumn();
    private MdColumn valueCol = new MdColumn();
    private MdColumn sequenceCol = new MdColumn();

    private boolean addKeyTable;
    private boolean addValueTable;

    private static final int NONE = 0;
    private static final int REF = 1;
    private static final int COL = 2;

    private int key;        // is there a key? NONE REF COL
    private int value;      // is there a value? NONE REF COL

    public void init(MdElement element, JdbcTable jdbcTable, String text,
            String comment, MdClass owner, String keyType, String valueType,
            boolean ordered, JdbcLinkCollectionField jdbcField) {

        this.element = element;
        if (isDoNotCreate()) comment = comment + " (do not create in schema)";
        super.init(element, jdbcTable, text, comment);

        this.jdbcTable = jdbcTable;
        this.ordered = ordered;

        ownerTable.init(owner);
        ownerTable.addCol(DotDotDotColumn.INSTANCE);

        MdDataStore store = owner.getMdDataStore();
        MdPackage pkg = owner.getMdPackage();

        refOwner.init(XmlUtils.findOrCreateExtension(element,
                JdoExtensionKeys.JDBC_OWNER_REF),
                this, owner, ownerTable, store, pkg,
                jdbcField == null ? null : jdbcField.ourPkColumns);
        addRef(refOwner);
        refOwner.addColsToTable(this);

        if (ordered) {
            MdElement seqElement = XmlUtils.findOrCreateExtension(element,
                    JdoExtensionKeys.JDBC_SEQUENCE);
            sequenceCol.init(store, seqElement,
                    jdbcField == null ? null : jdbcField.sequenceColumn, this);
            addCol(sequenceCol);
        }

        addKeyTable = false;
        MdClass keyCls = null;
        if (keyType != null) {
            MdElement keyElement = XmlUtils.findOrCreateExtension(element,
                    JdoExtensionKeys.JDBC_KEY);
            JdbcMapField mapField = ((JdbcMapField)jdbcField);
            if (owner != null) {
                keyCls = owner.findClass(keyType);
            }
            if (keyCls == null) {
                if (keyCol != null && mapField != null) {
                    keyCol.init(store, keyElement,
                            mapField.keyColumns == null ? null : mapField.keyColumns[0],
                            this);
                    addCol(keyCol);
                    key = COL;
                }
            } else {
                addKeyTable = !owner.isSameTable(keyCls);
                if (addKeyTable) {
                    keyTable.init(keyCls);
                    keyTable.addCol(DotDotDotColumn.INSTANCE);
                    keyTable.setComment("Table for key class");
                }
                refKey.init(keyElement, this, keyCls,
                        addKeyTable ? keyTable : ownerTable, store, pkg,
                        mapField == null ? null : mapField.keyColumns);
                refKey.addColsToTable(this);
                addRef(refKey);
                key = REF;
            }
        } else {
            key = NONE;
        }

        addValueTable = false;
        if (valueType != null) {
            MdElement valueElement = XmlUtils.findOrCreateExtension(element,
                    JdoExtensionKeys.JDBC_VALUE);
            MdClass valueCls = owner.findClass(valueType);
            if (valueCls == null) {
                valueCol.init(store, valueElement,
                        jdbcField == null ? null :
                        jdbcField.valueColumns == null ? null : jdbcField.valueColumns[0],
                        this);
                addCol(valueCol);
                value = COL;
            } else {
                GraphTable dt;
                if (owner.isSameTable(valueCls)) {
                    dt = ownerTable;
                } else if (addKeyTable && keyCls.isSameTable(valueCls)) {
                    dt = keyTable;
                } else {
                    dt = valueTable;
                    addValueTable = true;
                }
                if (addValueTable) {
                    valueTable.init(valueCls);
                    valueTable.addCol(DotDotDotColumn.INSTANCE);
                    valueTable.setComment("Table for " + valueCls.getName());
                }
                refValue.init(valueElement, this, valueCls, dt, store, pkg,
                        jdbcField == null ? null : jdbcField.valueColumns);
                refValue.addColsToTable(this);
                addRef(refValue);
                value = REF;
            }
        } else {
            value = NONE;
        }
    }

    public MdClassTable getOwnerTable() {
        return ownerTable;
    }

    public MdClassTable getKeyTable() {
        return keyTable;
    }

    public MdClassTable getValueTable() {
        return valueTable;
    }

    /**
     * Write the names of this link table and and its columns into the meta
     * data. This is a NOP if the information is not available (i.e. the meta
     * data has errors).
     */
    public void writeMappingsToMetaData() {
        if (jdbcTable == null) return;
        String s = getTableNameStr();
        setTableNameStr(s == null ? getDefTableName() : s);
        refOwner.writeMappingsToMetaData();
        if (ordered) sequenceCol.writeNameToMetaData();
        if (key == COL) {
            keyCol.writeNameToMetaData();
        } else if (key == REF) {
            refKey.writeMappingsToMetaData();
        }
        if (value == COL) {
            valueCol.writeNameToMetaData();
        } else if (value == REF) {
            refValue.writeMappingsToMetaData();
        }
    }

    public boolean isAddKeyTable() {
        return addKeyTable;
    }

    public boolean isAddValueTable() {
        return addValueTable;
    }


}
