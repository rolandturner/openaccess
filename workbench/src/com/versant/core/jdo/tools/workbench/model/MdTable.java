
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
import com.versant.core.jdbc.metadata.JdbcTable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A table for a class or field.
 *
 * @keep-all
 */
public class MdTable implements GraphTable {

    protected MdElement element;
    protected JdbcTable jdbcTable;
    private String comment;
    private String text;

    private ArrayList pkCols = new ArrayList();
    private ArrayList cols = new ArrayList();
    private ArrayList refs = new ArrayList();
    private ArrayList targetRefs = new ArrayList();

    /**
     * Init this table.
     */
    public void init(MdElement element, JdbcTable jdbcTable, String text,
            String comment) {
        this.element = element;
        this.jdbcTable = jdbcTable;
        this.text = text;
        this.comment = comment;
        pkCols.clear();
        cols.clear();
        refs.clear();
        targetRefs.clear();
    }

    public void addCol(GraphColumn c) {
        if (c != null) {
            if (c.isPrimaryKey()) pkCols.add(c);
            String name = c.getColumnName();
            if (name != null) {
                for (Iterator it = cols.iterator(); it.hasNext();) {
                    GraphColumn column = (GraphColumn)it.next();
                    if (name.equals(column.getColumnName())) {
                        return;
                    }
                }
                cols.add(c);
            }
        }
    }

    public void addRef(GraphColumnRef ref) {
        if (ref != null){
            refs.add(ref);
            GraphTable destTable = ref.getDestTable();
            if(destTable instanceof MdTable){
                ((MdTable)destTable).targetRefs.add(ref);
            }
        }
    }

    public String getName() {
        String n = getTableNameStr();
        if (n == null) n = getDefTableName();
        if (n == null) n = "{default}";
        return n;
    }

    public String getTableNameStr() {
        if (element == null) {
            return "<no element>";
        } else {
            return XmlUtils.getExtension(element,
                    JdoExtensionKeys.JDBC_TABLE_NAME);
        }
    }

    public void setTableNameStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_TABLE_NAME, s);
    }

    public String getDefTableName() {
        return jdbcTable == null ? null : jdbcTable.name;
    }

    public MdValue getTableName() {
        MdValue v = new MdValue(getTableNameStr());
        v.setDefText(getDefTableName());
        v.setOnlyFromPickList(false);
        if (MdClass.getDatabaseMetaData() != null) {
            v.setCaseSensitive(false);
            v.setWarningOnError(true);
            v.setOnlyFromPickList(true);
            v.setPickList(MdClass.getDatabaseMetaData().getAllTableNames());
        }
        return v;
    }

    public void setTableName(MdValue v) {
        setTableNameStr(v.getText());
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public GraphColumn[] getPkCols() {
        GraphColumn[] a = new GraphColumn[pkCols.size()];
        pkCols.toArray(a);
        return a;
    }

    public GraphColumn[] getCols() {
        GraphColumn[] a = new GraphColumn[cols.size()];
        cols.toArray(a);
        return a;
    }

    public GraphColumnRef[] getRefs() {
        GraphColumnRef[] a = new GraphColumnRef[refs.size()];
        refs.toArray(a);
        return a;
    }

    public String toString() {
        return text;
    }

    /**
     * Does this table have the same name as t? If t is null or one or both
     * of the names is null then false is returned.
     */
    public boolean isSameTable(MdTable t) {
        if (t == null) return false;
        String a = getName();
        String b = t.getName();
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public void dump() {
    }

    public String getJdbcDoNotCreateTableStr() {
        if (element == null) return null;
        return XmlUtils.getExtension(element,
                JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE);
    }

    public MdValue getJdbcDoNotCreateTable() {
        MdValue v = new MdValue(getJdbcDoNotCreateTableStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setJdbcDoNotCreateTable(MdValue v) {
        XmlUtils.setExtension(element,
                JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE, v.getText());
    }

    /**
     * Is this table flagged as 'do not create'?
     */
    public boolean isDoNotCreate() {
        return "true".equals(getJdbcDoNotCreateTableStr());
    }

    public ArrayList getSourceRefs() {
        return refs;
    }

    public ArrayList getTargetRefs() {
        return targetRefs;
    }
}
