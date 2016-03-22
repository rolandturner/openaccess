
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
package com.versant.core.jdbc.metadata;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.jdbc.JdbcKeyGenerator;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.common.Debug;
import com.versant.core.util.CharBuf;

import java.io.Serializable;
import java.io.PrintStream;
import java.util.*;

/**
 * Extra meta data for a class stored in JDBC. Some of the fields are
 * also present in the normal ClassMetaData and are duplicated here for
 * performance reasons.
 */
public final class JdbcClass implements Serializable {

    /**
     * Do not detect concurrent updates to this class.
     */
    public static final int OPTIMISTIC_LOCKING_NONE = 1;
    /**
     * Detect concurrent updates to this class by using a version column.
     */
    public static final int OPTIMISTIC_LOCKING_VERSION = 2;
    /**
     * Detect concurrent updates to this class by using a timestamp column.
     */
    public static final int OPTIMISTIC_LOCKING_TIMESTAMP = 3;
    /**
     * Detect concurrent updates to this class by including the previous
     * values of all changed fields in the where clause.
     */
    public static final int OPTIMISTIC_LOCKING_CHANGED = 4;

    /**
     * Subclass fields stored in the table of its immediate superclass.
     */
    public static final int INHERITANCE_FLAT = 1;
    /**
     * Subclass fields stored in its own table.
     */
    public static final int INHERITANCE_VERTICAL = 2;
    /**
     * Fields stored in subclass table.
     */
    public static final int INHERITANCE_HORIZONTAL = 3;

    /**
     * The normal meta data for our class.
     */
    public ClassMetaData cmd;
    /**
     * Our SqlDriver.
     */
    public final SqlDriver sqlDriver;
    /**
     * The inheritance strategy for this class. This will be INHERITANCE_FLAT
     * if the class is not in a hierarchy or is a base class.
     *
     * @see #INHERITANCE_FLAT
     * @see #INHERITANCE_VERTICAL
     */
    public int inheritance;
    /**
     * Our table. This may be the table of the pcSuperClass if this is a
     * subclass with fields stored in the base table.
     */
    public JdbcTable table;
    /**
     * The name of our table.
     */
    public String tableName;
    /**
     * All the tables for this class including superclass tables. The entry
     * at 0 is the table for the topmost superclass. Also
     * table == allTables[allTables.length - 1].
     */
    public JdbcTable[] allTables;
    /**
     * The fields in fieldNo order. Note that if a fieldNo is transactional
     * then its entry here will be null.
     */
    public JdbcField[] fields;
    /**
     * The fields in State fieldNo order. Note that if a fieldNo is
     * transactional then its entry here will be null. This includes all
     * fields from superclasses.
     */
    public JdbcField[] stateFields;
    /**
     * mapping from column name to the jdbc fields
     */
    private transient Map colNamesToJdbcField;
    /**
     * The JDBC key generator for this class (null if no key generator is
     * required).
     */
    public JdbcKeyGenerator jdbcKeyGenerator;
    /**
     * This becomes the default value for the useJoin field for references
     * to this class.
     *
     * @see JdbcRefField#useJoin
     */
    public int useJoin;
    /**
     * Our classId for this class. This does not have to be the same as that
     * in ClassMetaData. It only has to be unique within an inheritance
     * hierarchy. This will default to the classId of the class. If the
     * classIdCol column is a number (INT etc) then this must be a Number.
     * Otherwise it must be a String.
     *
     * @see ClassMetaData#classId
     */
    public Object jdbcClassId;
    /**
     * The column used to hold the classId value for each row. This is used
     * to implement inheritance. It may be null if this class has no persistent
     * subclasses or if all of the subclasses use vertical inheritance. This
     * field is set to the same value for all the classes in a hierarchy.
     */
    public JdbcColumn classIdCol;
    /**
     * Treat rows in the database that would be instances of us as instances
     * of readAsClass instead. This is used to implement flat inheritance
     * with no descriminator where rows are instances of the leaf class.
     */
    public ClassMetaData readAsClass;
    /**
     * How optimistic locking is done for this class (one of the
     * OPTIMISTIC_LOCKING_xxx constants).
     */
    public int optimisticLocking;
    /**
     * The field used to store the row version or timestamp value for
     * this table for optimistic locking. It may be null. This may be
     * a fake field.
     */
    public JdbcSimpleField optimisticLockingField;
    /**
     * If this flag is set the the table for this class will not be created
     * by the create schema task or the workbench.
     */
    public boolean doNotCreateTable;
    /**
     * Cache for SQL required to delete a main table row for this class.
     */
    public String deleteRowSql;
    /**
     * Cache for SQL required to lock the main table row for this class.
     */
    public String lockRowSql;
    /**
     * Do not use statement batching with this class.
     */
    public boolean noBatching;

    private String lockRowColumnName; // column used for update locking

    public JdbcClass(SqlDriver sqlDriver) {
        this.sqlDriver = sqlDriver;
    }

    public String toString() {
        return cmd.qname + " (" + table.name + ")";
    }

    /**
     * Add all tables that belong to this class to the set.
     */
    public void getTables(HashSet tables) {
        if (table != null) {
            tables.add(table);
        }
        int n = 0;
        if (fields != null) {
            n = fields.length;
        }
        for (int i = 0; i < n; i++) {
            JdbcField f = fields[i];
            if (f != null) f.getTables(tables);
        }
    }

    /**
     * Build the stateFields array for this class.
     */
    public void buildStateFields() {
        if (cmd.pcSuperMetaData == null) buildStateFieldsImp();
    }

    private void buildStateFieldsImp() {
        if (cmd.pcSuperMetaData != null) {
            JdbcField[] superStateFields = ((JdbcClass)cmd.pcSuperMetaData.storeClass).stateFields;
            if (superStateFields != null) {
                int n = superStateFields.length;
                stateFields = new JdbcField[n + fields.length];
                System.arraycopy(superStateFields, 0, stateFields, 0, n);
                System.arraycopy(fields, 0, stateFields, n, fields.length);
            }
        } else {
            stateFields = fields;
        }
        if (cmd.pcSubclasses != null) {
            for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
                ((JdbcClass)cmd.pcSubclasses[i].storeClass).buildStateFieldsImp();
            }
        }
    }

    /**
     * Set the key generator for this class and recursively all its subclasses.
     */
    public void setJdbcKeyGenerator(JdbcKeyGenerator jdbcKeyGenerator) {
        this.jdbcKeyGenerator = jdbcKeyGenerator;
        cmd.useKeyGen = true;
        cmd.postInsertKeyGenerator = jdbcKeyGenerator.isPostInsertGenerator();
        ClassMetaData[] pcSubclasses = cmd.pcSubclasses;
        if (pcSubclasses == null) return;
        for (int i = 0; i < pcSubclasses.length; i++) {
            ((JdbcClass)pcSubclasses[i].storeClass).setJdbcKeyGenerator(jdbcKeyGenerator);
        }
    }

    /**
     * Copy our optimistic locking settings to all of our subclasses.
     */
    public void copyOptimisticLockingToSubs() {
        ClassMetaData[] pcSubclasses = cmd.pcSubclasses;
        if (pcSubclasses == null) return;
        for (int i = 0; i < pcSubclasses.length; i++) {
            JdbcClass sc = (JdbcClass)pcSubclasses[i].storeClass;
            sc.optimisticLocking = optimisticLocking;
            sc.cmd.changedOptimisticLocking =
                    optimisticLocking == JdbcClass.OPTIMISTIC_LOCKING_CHANGED;
            sc.optimisticLockingField = optimisticLockingField;
            if (optimisticLockingField != null) {
                sc.cmd.optimisticLockingField = optimisticLockingField.fmd;
            }
            sc.copyOptimisticLockingToSubs();
        }
    }

    /**
     * Set the classIdField for this class and recursively all its subclasses.
     */
    public void setClassIdCol(JdbcColumn classIdCol) {
        this.classIdCol = classIdCol;
        ClassMetaData[] pcSubclasses = cmd.pcSubclasses;
        if (pcSubclasses == null) return;
        for (int i = 0; i < pcSubclasses.length; i++) {
            ((JdbcClass)pcSubclasses[i].storeClass).setClassIdCol(classIdCol);
        }
    }

    /**
     * Find the class with the given JDBC classId. This might be ourselves or
     * one of our subclasses. Returns null if not found.
     */
    public ClassMetaData findClass(Object jdbcClassId) {
        if (this.jdbcClassId.equals(jdbcClassId)) return cmd;
        ClassMetaData[] subs = cmd.pcSubclasses;
        if (subs != null) {
            for (int i = subs.length - 1; i >= 0; i--) {
                ClassMetaData ans = ((JdbcClass)subs[i].storeClass).findClass(jdbcClassId);
                if (ans != null) return ans;
            }
        }
        return null;
    }

    /**
     * Is this class stored in a different table to the base class in the
     * hierarchy?
     */
    public boolean isMultiTableHierarchy() {
        return table != ((JdbcClass)cmd.pcHierarchy[0].storeClass).table;
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        out.println(is + "cmd = " + cmd);
        out.println(is + "jdbcKeyGenerator = " + jdbcKeyGenerator);
        out.println(is + "useJoin = " + JdbcRefField.toUseJoinString(useJoin));
        out.println(is + "classId = " + jdbcClassId);
        out.println(is + "classIdCol = " + classIdCol);
        out.println(is + "optimisticLocking = " +
                toOptimisticLockingString(optimisticLocking));
        out.println(
                is + "optLockFieldStateFieldNo = " + optimisticLockingField);
        out.println(is + "fields = " +
                (fields == null ? "null" : Integer.toString(fields.length)));
        out.println(is + "stateFields = " +
                (stateFields == null ? "null" : Integer.toString(
                        stateFields.length)));
    }

    public static String toOptimisticLockingString(int o) {
        switch (o) {
            case OPTIMISTIC_LOCKING_NONE:
                return "none";
            case OPTIMISTIC_LOCKING_CHANGED:
                return "changed";
            case OPTIMISTIC_LOCKING_VERSION:
                return "version";
            case OPTIMISTIC_LOCKING_TIMESTAMP:
                return "timestamp";
        }
        return "unknown(" + o + ")";
    }

    /**
     * Find a primary key field of this class or the topmost superclass in
     * the hierarchy by name or null if none.
     */
    public JdbcSimpleField findPkField(String fname) {
        FieldMetaData f = cmd.findPkField(fname);
        if (f == null) return null;
        return (JdbcSimpleField)f.storeField;
    }

    /**
     * Find the index of the primary key field f or -1 if none.
     */
    public int findPkFieldIndex(JdbcSimpleField f) {
        for (int i = cmd.pkFields.length - 1; i >= 0; i--) {
            if (cmd.pkFields[i].storeField == f) return i;
        }
        return -1;
    }

    /**
     * Get the maximum number of OIDs for this class that can be included
     * in an IN (?, .., ?) statement. This depends on the database and the
     * number of columns in the primary key. The return value is zero if
     * this class has a composite primary key.
     */
    public int getMaxOIDsForIN(SqlDriver sqlDriver) {
        if (table.pkSimpleColumnCount > 1) return 0;
        return sqlDriver.getMaxInOperands();
    }

    /**
     * Find all fields in this class that have columnName in their main table
     * columns and add them to list.
     */
    public void findFieldsForColumn(ClassMetaData cmd, String columnName,
            List list) {
        for (int i = 0; i < fields.length; i++) {
            JdbcField f = fields[i];
            if (f != null && f.findMainTableColumn(columnName) != null) {
                ClassMetaData fcmd = f.fmd.classMetaData;
                if ((cmd.isAncestorOrSelf(fcmd) || fcmd.isAncestorOrSelf(cmd))) {
                    list.add(f);
                }
            }
        }
    }

    /**
     * Find any columns in any of our subclasses (and recursively their
     * subclasses) with columnName and set shared = true for them.
     */
    public void markSubclassColumnsShared(String columnName) {
        if (cmd.pcSubclasses == null) return;
        for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
            JdbcClass jc = (JdbcClass)cmd.pcSubclasses[i].storeClass;
            jc.markColumnsShared(columnName, table);
            jc.markSubclassColumnsShared(columnName);
        }
    }

    /**
     * Mark any columns for fields in this class with name columnName
     * shared = true unless they are primary key fields.
     */
    public void markColumnsShared(String columnName, JdbcTable cTable) {
        for (int i = 0; i < fields.length; i++) {
            JdbcField f = fields[i];
            if (f == null || f.fmd.primaryKey) continue;
            if (f.mainTable != cTable) continue;
            JdbcColumn c = f.findMainTableColumn(columnName);
            if (c != null) c.setShared(true);
        }
    }

    /**
     * Get the column that should be used for update locking.
     */
    public String getLockRowColumnName() {
        if (lockRowColumnName == null) {
            lockRowColumnName = table.getLockRowColumn().name;
        }
        return lockRowColumnName;
    }

    /**
     * Set the table for this class. This will also set the tablename and
     * allTables fields. This method must only be invoked one a subclass
     * if it has been called on its superclass.
     */
    public void setTable(JdbcTable table) {
        this.table = table;
        tableName = table.name;
        ArrayList a = new ArrayList();
        JdbcTable prev = null;
        for (int i = 0; i < cmd.pcHierarchy.length; i++) {
            JdbcTable t = ((JdbcClass)cmd.pcHierarchy[i].storeClass).table;
            if (t != prev) a.add(t);
            prev = t;
        }
        allTables = new JdbcTable[a.size()];
        a.toArray(allTables);
    }

    /**
     * Get a LiteralExp for our jdbc-class-id. If subclasses is true then
     * a list of LiteralExp's are returned including all the
     */
    public LiteralExp getClassIdExp(boolean subclasses) {
        LiteralExp root = classIdCol.createClassIdLiteralExp(jdbcClassId);

        if (subclasses && cmd.pcSubclasses != null) {
            ClassMetaData[] a = cmd.pcSubclasses;
            SqlExp pos = root;
            for (int i = a.length - 1; i >= 0; i--) {
                pos = pos.setNext(((JdbcClass)a[i].storeClass).getClassIdExp(true));
                for (; pos.getNext() != null; pos = pos.getNext()) ;
            }
        }
        return root;
    }

    /**
     * Get an SqlExp that will only return instances of this class or one
     * of its subclasses from this table.
     *
     * @param se A select against our table
     */
    public SqlExp getCheckClassIdExp(SelectExp se) {
        if (classIdCol == null) return null;
        SqlExp colExp = classIdCol.toSqlExp(se);
        LiteralExp idExp = getClassIdExp(true);
        if (idExp.getNext() == null) {
            return new BinaryOpExp(colExp, BinaryOpExp.EQUAL, idExp);
        } else {
            colExp.setNext(idExp);
            return new InExp(colExp);
        }
    }

    /**
     * See if our jdbcClassId that those of all of our subclasses are ints.
     * This is used to default the type of the descriminator column to
     * INTEGER if possible.
     */
    public boolean isIntJdbcClassIdHierarchy() {
        if (jdbcClassId == null) return false;
        if (jdbcClassId instanceof Integer) return true;
        try {
            Integer.parseInt((String)jdbcClassId);
        } catch (NumberFormatException e) {
            return false;
        }
        if (cmd.pcSubclasses == null) return true;
        for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
            if (!((JdbcClass)cmd.pcSubclasses[i].storeClass).isIntJdbcClassIdHierarchy()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert our jdbcClassId to an Integer if it is not already an Integer.
     * Recursively process subclasses.
     */
    public void convertJdbcClassIdToInteger() {
        if (jdbcClassId instanceof String) {
            jdbcClassId = new Integer((String)jdbcClassId);
        }
        if (cmd.pcSubclasses != null) {
            for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
                ((JdbcClass)cmd.pcSubclasses[i].storeClass).convertJdbcClassIdToInteger();
            }
        }
    }

    public Map getColNamesToJdbcField() {
        if (colNamesToJdbcField == null) {
            colNamesToJdbcField = new HashMap(stateFields.length);
            JdbcField[] fs = stateFields;
            for (int i = 0; i < fs.length; i++) {
                JdbcField f = fs[i];
                if (f.mainTableCols != null) {
                    JdbcColumn[] cols = f.mainTableCols;
                    for (int j = 0; j < cols.length; j++) {
                        JdbcColumn col = cols[j];
                        colNamesToJdbcField.put(col.name.toUpperCase(), f);
                    }
                }
            }
        }
        return colNamesToJdbcField;
    }

    /**
     * Get SQL to lock a row in our table.
     */
    public String getLockRowSql() {
        if (lockRowSql == null) {
            CharBuf s = new CharBuf();
            s.append("UPDATE ");
            s.append(table.name);
            s.append(" SET ");
            String c = getLockRowColumnName();
            s.append(c);
            s.append('=');
            s.append(c);
            s.append(" WHERE ");
            table.appendWherePK(s);
            lockRowSql = s.toString();
        }
        return lockRowSql;
    }

}

