
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
package com.versant.core.jdo.junit.test0.model.patrikb.keygen;

import com.versant.core.jdbc.JdbcKeyGenerator;
import com.versant.core.jdbc.JdbcKeyGeneratorFactory;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcMappingResolver;
import com.versant.core.metadata.MDStatics;
import com.versant.core.common.BindingSupportImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This key generator uses a last used id table and a grab size to generate
 * primary keys. Each instance generates keys for a single class.
 */
public class TransactionIdKeyGenerator implements JdbcKeyGenerator {

    /**
     * Our args bean.
     */
    public static class Args {

        private String tableName = "jdo_keygen";
        private String keyColumnName = "table_name";
        private String valueColumnName = "last_used_id";
        private int keyColumnLength = 64;
        private int grabSize = 10;
        private int start;
        private boolean createTable = true;
        private String pkConstraint;

        public Args() {
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getKeyColumnName() {
            return keyColumnName;
        }

        public void setKeyColumnName(String keyColumnName) {
            this.keyColumnName = keyColumnName;
        }

        public String getValueColumnName() {
            return valueColumnName;
        }

        public void setValueColumnName(String valueColumnName) {
            this.valueColumnName = valueColumnName;
        }

        public int getKeyColumnLength() {
            return keyColumnLength;
        }

        public void setKeyColumnLength(int keyColumnLength) {
            this.keyColumnLength = keyColumnLength;
        }

        public int getGrabSize() {
            return grabSize;
        }

        public void setGrabSize(int grabSize) {
            this.grabSize = grabSize;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public boolean isCreateTable() {
            return createTable;
        }

        public void setCreateTable(boolean createTable) {
            this.createTable = createTable;
        }

        public String getPkConstraint() {
            return pkConstraint;
        }

        public void setPkConstraint(String pkConstraint) {
            this.pkConstraint = pkConstraint;
        }
    }

    /**
     * Our factory.
     */
    public static class Factory implements JdbcKeyGeneratorFactory {

        /**
         * Create a javabean to hold args for a createJdbcKeyGenerator call or null
         * if the key generator does not accept any arguments.
         */
        public Object createArgsBean() {
            return new Args();
        }

        /**
         * Create a JdbcKeyGenerator for class using props as parameters. The
         * instance returned may be new or may be a shared instance.
         */
        public JdbcKeyGenerator createJdbcKeyGenerator(String className,
                JdbcTable classTable, Object args) {
            TransactionIdKeyGenerator kg = new TransactionIdKeyGenerator(classTable, (Args)args);
            return kg;
        }
    }

    protected JdbcTable classTable;
    protected JdbcColumn classPk;
    protected int pkJavaTypeCode;

    protected String tableName;
    protected String keyColumnName;
    protected String valueColumnName;
    protected int keyColumnLength;
    protected int grabSize;
    protected int start;
    protected boolean createTable;
    protected String pkConstraint;

    protected String updateSql;
    protected String selectSql;
    protected int lastUsed;
    protected int grabLeft;

    public TransactionIdKeyGenerator(JdbcTable classTable, Args args) {
        if (classTable.pk.length > 1) {
            throw new IllegalArgumentException("Cannot use HIGH/LOW key generator on a table with multiple " +
                    "primary key columns");
        }
        this.classTable = classTable;
        classPk = classTable.pk[0];
        pkJavaTypeCode = classPk.javaTypeCode;
        tableName = args.getTableName();
        keyColumnName = args.getKeyColumnName();
        valueColumnName = args.getValueColumnName();
        keyColumnLength = args.getKeyColumnLength();
        grabSize = args.getGrabSize();
        start = args.getStart();
        createTable = args.isCreateTable();
        pkConstraint = args.getPkConstraint();
    }

    /**
     * Initialize this key generator. This is called when the JDO
     * implementation initializes before any keys are generated. Key
     * generators should use this to avoid popular race conditions and
     * deadlock opportunities (e.g. multiple 'select max(id) from table'
     * statements executing at the same time). If the same key generator
     * instance is used on more than one class this will be called once
     * for each class.
     *
     * @param className  The name of the class
     * @param classTable The table for the class
     * @param con        Connection to the DataSource for the class
     */
    public void init(String className, JdbcTable classTable,
            Connection con) throws SQLException {

        // generate our update and select statements
        String where = " where " + keyColumnName + " = '" + classTable.name + "'";
        updateSql = "update " + tableName + " set " + valueColumnName +
                " = " + valueColumnName + " + ?" +
                where;
        selectSql = "select " + valueColumnName + " from " + tableName +
                where;

        // make sure there is a row in our keygen table for our class
        Statement stat = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(updateSql);
            ps.setInt(1, 0);
            if (ps.executeUpdate() == 0) {
                stat = con.createStatement();
                int first = start;
                if (first == 0) {
                    String sql =
                            "select max(" + classPk.name + ") from " + classTable.name;
                    ResultSet rs = null;
                    try {
                        rs = stat.executeQuery(sql);
                        rs.next();
                        first = rs.getInt(1);
                    } finally {
                        cleanup(rs);
                    }
                }
                String sql =
                        "insert into " + tableName + " (" + keyColumnName + ", " +
                        valueColumnName + ") values ('" + classTable.name + "', " +
                        first + ")";
                stat.execute(sql);
            }
        } finally {
            cleanup(ps);
            cleanup(stat);
        }
    }

    /**
     * If the new key can only be detirmined after the new row has been
     * inserted (e.g. if using a database autoincrement column) then this
     * should return true.
     */
    public boolean isPostInsertGenerator() {
        return false;
    }

    /**
     * Does this key generator require its own connection? If it does then
     * one will be obtained to generate the key and committed after the
     * key has been generated.
     */
    public boolean isRequiresOwnConnection() {
        return grabSize > 1;
    }

    public void addKeyGenTables(HashSet set, JdbcMetaDataBuilder mdb) {
        if (!createTable) return;

        // do not create a table if there is already one with our tableName
        // do not create a table if there is already one with our tableName
        for (Iterator i = set.iterator(); i.hasNext();) {
            JdbcTable t = (JdbcTable)i.next();
            if (t.name.equals(tableName)) return;
        }

        // create the table and add it
        JdbcTable t = new JdbcTable();
        t.name = tableName;
        t.comment = getClass().getName();
        t.pkConstraintName = pkConstraint == null ? "pk_" + tableName : pkConstraint;
        JdbcMappingResolver mr = mdb.getMappingResolver();
        JdbcColumn keyCol =
                new JdbcColumn(
                        mr.resolveMapping(String.class),
                        mr);
        keyCol.name = keyColumnName;
        keyCol.length = keyColumnLength;
        keyCol.nulls = false;
        JdbcColumn valueCol =
                new JdbcColumn(
                        mr.resolveMapping(Integer.TYPE),
                        mr);

        valueCol.name = valueColumnName;
        valueCol.nulls = false;
        t.cols = new JdbcColumn[]{keyCol, valueCol};
        t.setPk(new JdbcColumn[]{keyCol});
        set.add(t);
    }

    /**
     * Generate a new primary key value for a new instance of the supplied
     * class prior to the row being inserted. The values generated will be used
     * to populate a new OID and then set on a PreparedStatement for the
     * insert. This is called if isPostInsertGenerator returns false.
     * <p/>
     * The newObjectCount parameter indicates the number of new objects that
     * will be inserted (including this one) in the same transaction using
     * this key generator. This may be used to optimize the behavior of the
     * key generator or be ignored. The highlow key generator uses this value
     * instead of its grabSize to avoid executing redundant updates and
     * selects.<p>
     *
     * @param className      The name of the class
     * @param classTable     The table for the class
     * @param newObjectCount The number of new objects being created
     * @param data           The array to store the key values in.
     * @param con            Connection to the DataSource for the class.
     * @throws SQLException on errors
     */
    public synchronized void generatePrimaryKeyPre(String className,
            JdbcTable classTable, int newObjectCount, Object[] data,
            Connection con)
            throws SQLException {
        int pk;
        if (grabSize == 1) {
            pk = lookupNewNumber(con, grabSize);
        } else {
            if (grabLeft == 0) {
                int effectiveGrabSize = newObjectCount;
                if (effectiveGrabSize < grabSize) effectiveGrabSize = grabSize;
                lastUsed = lookupNewNumber(con, effectiveGrabSize);
                grabLeft = effectiveGrabSize - 1;
            } else {
                --grabLeft;
            }
            pk = lastUsed++;
        }
        switch (pkJavaTypeCode) {
            case MDStatics.INTW:
            case MDStatics.INT:
                data[0] = new Integer((int)pk);
                break;
            case MDStatics.SHORTW:
            case MDStatics.SHORT:
                data[0] = new Short((short)pk);
                break;
            case MDStatics.BYTEW:
            case MDStatics.BYTE:
                data[0] = new Byte((byte)pk);
                break;
            case MDStatics.LONGW:
            case MDStatics.LONG:
                data[0] = new Long(pk);
                break;
            case MDStatics.STRING:
                data[0] = "TX" + pk;
//                        String.format("TX%09X", pk);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Unhandled java type code: " + pkJavaTypeCode);
        }
    }

    /**
     * Run SQL to get a new number. This does an update and a select for
     * our classes row in the keygen table.
     */
    protected int lookupNewNumber(Connection con, int effectiveGrabSize)
            throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(updateSql);
            ps.setInt(1, effectiveGrabSize);
            if (ps.executeUpdate() == 0) {
                throw BindingSupportImpl.getInstance().fatalDatastore("Row not found in keygen table:\n" +
                        updateSql);
            }
            Statement stat = null;
            ResultSet rs = null;
            try {
                stat = con.createStatement();
                rs = stat.executeQuery(selectSql);
                rs.next();
                return rs.getInt(1) - (effectiveGrabSize - 1);
            } finally {
                cleanup(rs);
                cleanup(stat);
            }
        } finally {
            cleanup(ps);
        }
    }

    private void cleanup(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    private void cleanup(Statement s) {
        try {
            if (s != null) s.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * Generate a new primary key value for a new instance of the supplied
     * class after the row has been inserted. The values generated will be used
     * to populate a new OID and then set on a PreparedStatement for the
     * insert.  This is called if isPostInsertGenerator returns true.
     *
     * @param className  The name of the class
     * @param classTable The table for the class
     * @param data       The array to store the key values in.
     * @param con        Connection to the DataSource for the class.
     * @param stat       Statement created from con. Do not close it. This will have
     *                   just been used to insert the new row.
     * @throws SQLException on errors
     */
    public void generatePrimaryKeyPost(String className,
            JdbcTable classTable, Object[] data,
            Connection con, Statement stat) throws SQLException {
        throw BindingSupportImpl.getInstance().internal("not a postInsertGenerator");
    }

    /**
     * Get extra SQL to be appended to the insert statement. This is only
     * called for post insert key generators. Return null if no extra SQL
     * is required. Key generators can use this as an alternative to running
     * a separate query to get the primary key for the just inserted row.
     */
    public String getPostInsertSQLSuffix(JdbcTable classTable) {
        throw BindingSupportImpl.getInstance().internal("not a postInsertGenerator");
    }

}
