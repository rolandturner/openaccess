
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

import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.util.CharBuf;

import java.util.*;

/**
 * Class to keep all the database stuff in
 *
 * @keep-all
 */
public class DatabaseMetaData {
    private HashMap classToColumns = new HashMap();
    private HashMap classToTable = new HashMap();

    public DatabaseMetaData(HashMap classes, boolean toLowerCase) {
        Collection values = classes.values();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            JdbcTable table = (JdbcTable) iter.next();
            String lowerTableName = table.name.toLowerCase();
            classToTable.put(lowerTableName,new FieldDisplay(table, toLowerCase));
            ArrayList list = new ArrayList();
            for (int i = 0; i < table.cols.length; i++) {
                JdbcColumn col = table.cols[i];
                list.add(new FieldDisplay(col, toLowerCase));
            }
            classToColumns.put(lowerTableName, list);
        }
    }

    public void unmap() {
        // first we unmap everything
        Set keys = classToTable.keySet();
        FieldDisplay display = null;
        ArrayList columnList = null;
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            display = (FieldDisplay) classToTable.get(key);
            display.setMapping(null);
            columnList = (ArrayList) classToColumns.get(key);
            for (Iterator iterator = columnList.iterator(); iterator.hasNext();) {
                display = (FieldDisplay) iterator.next();
                display.setMapping(null);
            }
        }
    }

    public void remap(ArrayList newMapping){
        unmap();
        // now we remap everything
        FieldDisplay display = null;
        ArrayList columnList = null;
        int n = newMapping.size();
        for (int m = 0; m < n; m++) {
            JdbcTable ourTable = (JdbcTable) newMapping.get(m);
            String key = ourTable.name.toLowerCase();
            display = (FieldDisplay) classToTable.get(key);
            if (display != null){
                display.setMapping(getMapping(ourTable.comment));
                columnList = (ArrayList) classToColumns.get(key);
                if (columnList != null){
                    JdbcColumn[] columns = ourTable.cols;
                    for (Iterator iterator = columnList.iterator(); iterator.hasNext();) {
                        display = (FieldDisplay) iterator.next();
                        if (display != null){
                            for (int i = 0; i < columns.length; i++) {
                                JdbcColumn column = columns[i];
                                if (column.name.equalsIgnoreCase(display.getName())){
                                    display.setMapping(column.comment);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all the table names that is in the database excluding system tables
     */
    public List getAllTableNames() {
        Collection values = classToTable.values();
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            display.setLenght();
        }
        ArrayList list = new ArrayList();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            list.add(display.toString());
        }
        Collections.sort(list);
        return list;

    }

    /**
     * Get all the table names that is in the database excluding system tables
     */
    public List getAllTableNamesForDisplay() {
        Collection values = classToTable.values();
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            display.setLenght();
        }
        ArrayList list = new ArrayList(values);
        Collections.sort(list);
        return list;

    }

    /**
     * Get all the table names that is not mapped already,
     * including this table name if it exists.
     */
    public List getTableNames(String tableName) {
        Collection values = classToTable.values();
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            if (display.getMapping() == null || display.getName().equalsIgnoreCase(tableName)){
                display.setLenght();
            }

        }
        ArrayList list = new ArrayList();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            if (display.getMapping() == null || display.getName().equalsIgnoreCase(tableName)) {
                list.add(display.toString());
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Get all the columns for the tabel name.
     */
    public List getAllColumnNames(String tableName){
        if (tableName == null){
            return new ArrayList();
        }
        tableName = tableName.toLowerCase();
        if (classToColumns.get(tableName) == null){
            return new ArrayList();
        }

        ArrayList colList = (ArrayList) classToColumns.get(tableName);
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = colList.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            display.setLenght();
        }

        ArrayList list = new ArrayList();
        for (Iterator iter = colList.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            list.add(display.toString());
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Get all the columns for the tabel name.
     */
    public List getAllColumnNamesForDisplay(String tableName) {
        if (tableName == null) {
            return null;
        }
        tableName = tableName.toLowerCase();
        if (classToColumns.get(tableName) == null) {
            return null;
        }

        ArrayList colList = (ArrayList) classToColumns.get(tableName);
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = colList.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            display.setLenght();
        }

        Collections.sort(colList);
        return colList;
    }

    /**
     * Does the table name exist?
     */
    public boolean containsTable(String tableName){
        if (tableName == null) {
            return false;
        }
        tableName = tableName.toLowerCase();
        Object o = classToTable.get(tableName);
        if (o == null){
            return false;
        } else {
            return true;
        }

    }


    /**
     * Get all the columns for the tabel name.
     */
    public List getColumnNames(String tableName,String columnName) {
        if (tableName != null) {
            tableName = tableName.toLowerCase();
        }
        if (classToColumns.get(tableName) == null) {
            return new ArrayList();
        }

        ArrayList colList = (ArrayList) classToColumns.get(tableName);
        FieldDisplay.BIGGEST_LENGHT = 0;
        for (Iterator iter = colList.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            if (display.getMapping() == null || display.getName().equalsIgnoreCase(columnName)) {
                display.setLenght();
            }
        }

        ArrayList list = new ArrayList();
        for (Iterator iter = colList.iterator(); iter.hasNext();) {
            FieldDisplay display = (FieldDisplay) iter.next();
            if (display.getMapping() == null || display.getName().equalsIgnoreCase(columnName)) {
                list.add(display.toString());
            }
        }

        Collections.sort(list);
        return list;

    }



    public static String getMapping(String comment){
        if (comment != null) {
            if (comment.startsWith("java.util.")) {   // we have a list
                int dot = comment.substring(0, comment.lastIndexOf('.')).lastIndexOf(".");
                return comment.substring(dot + 1, comment.length());
            } else if (comment.lastIndexOf(".") != -1) { // we have a class
                return  comment.substring(comment.lastIndexOf('.') + 1, comment.length());
            } else { // we have a field
                return  comment;
            }
        }
        return null;
    }




    public static class FieldDisplay implements Comparable {
        public static int BIGGEST_LENGHT;
        public static final int SPACE_LENGHT = 4;
        public String name;
        public String type;
        public String mapping;

        public FieldDisplay(JdbcTable table,boolean toLowerCase) {
            if (toLowerCase){
                this.name = table.name.toLowerCase();
            } else {
                this.name = table.name;
            }
            this.mapping = DatabaseMetaData.getMapping(table.comment);
            setLenght();
        }

        public FieldDisplay(JdbcColumn c, boolean toLowerCase) {
            if (toLowerCase) {
                this.name = c.name.toLowerCase();
            } else {
                this.name = c.name;
            }
            this.mapping = c.comment;
            CharBuf s = new CharBuf();
            s.append(c.sqlType);
            if (c.length != 0 || c.scale != 0) {
                s.append('(');
                s.append(c.length);
                if (c.scale != 0) {
                    s.append(',');
                    s.append(c.scale);
                }
                s.append(')');
            }
            type = s.toString();
            setLenght();
        }

        public String getName() {
            return name;
        }

        public FieldDisplay getDisplay() {
            return this;
        }

        public String getDisplayName() {
            return name + pad() + mapping;
        }

        private int getNameLenght() {
            return name.length();
        }

        public void setName(String name) {
            this.name = name;
        }

        private int getMappingLenght() {
            return mapping.length()+3;
        }

        private int getTypeLenght() {
            return type.length();
        }


        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }

        public String toString() {
            if (mapping == null && type == null){
                return name;
            } else if (mapping != null && type == null ){
                return name + pad() +" ("+ mapping +")";
            } else if (mapping == null && type != null) {
                return name + pad() + type;
            } else {
                return name + pad() +"(" + mapping + ") "+ type;
            }
        }

        public int compareTo(Object a) {
            return this.toString().compareTo(a.toString());
        }

        private int getLenght() {
            if (mapping == null && type == null){
                return getNameLenght();
            } else if (mapping == null && type != null){
                return getNameLenght() + getTypeLenght();
            } else if (mapping != null && type == null) {
                return getNameLenght() + getMappingLenght();
            } else if (mapping != null && type != null) {
                return getNameLenght() + getMappingLenght()+ getTypeLenght();
            } else {
                return getNameLenght();
            }
        }

        public void setLenght() {
            if (BIGGEST_LENGHT < getLenght()) {
                BIGGEST_LENGHT = getLenght();
            }
        }

//        private String replace(String text, String repl, String with) {
//            if (text == null) {
//                return null;
//            }
//
//            StringBuffer buf = new StringBuffer(text.length());
//            int start = 0, end = 0;
//            while ((end = text.indexOf(repl, start)) != -1) {
//                buf.append(text.substring(start, end)).append(with);
//                start = end + repl.length();
//            }
//            buf.append(text.substring(start));
//            return buf.toString();
//        }


        private String pad() {
            int lenght = (BIGGEST_LENGHT - getLenght()) + SPACE_LENGHT;
            char [] chars = new char[lenght];
            for (int i = 0; i < chars.length; i++) {
                chars[i] = ' ';
            }
            return String.valueOf(chars);
        }


        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object obj) {
            return name.equals(((FieldDisplay) obj).name);
        }
    }


}
