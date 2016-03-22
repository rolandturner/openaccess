
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

import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MetaDataBuilder;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.metadata.*;

import java.util.ArrayList;

import com.versant.core.common.BindingSupportImpl;

/**
 * This will analyze meta data for a reference to a PC class from an
 * array of extensions. It is used for simple reference fields and for link
 * tables. Instances of this class may only be used once.
 */
public class JdbcRefMetaDataBuilder implements JdoExtensionKeys {

    private JdbcMetaDataBuilder mdb;
    private ClassMetaData targetClass;
    private JdoElement context;
    private String fieldName;

    private ArrayList colsList = new ArrayList();

    private boolean doNotCreateConstraint;
    private String constraintName;
    private int useJoin;
    private JdbcColumn[] cols;

    public JdbcRefMetaDataBuilder(ClassMetaData cmd, JdbcMetaDataBuilder mdb,
            ClassMetaData targetClass, JdoElement context,
            String fieldName, JdoExtension[] extensions, boolean quiet) {
        this.mdb = mdb;
        this.targetClass = targetClass;
        this.context = context;
        this.fieldName = fieldName;
        try {
            process(extensions);
        } catch (RuntimeException e) {
            cmd.addError(e, quiet);
        }
    }

    private void process(JdoExtension[] extensions) {
        JdbcClass target = (JdbcClass)targetClass.storeClass;
        int pklen = 1;
        if (target != null) {
            JdbcTable table = target.table;
            if (table != null) {
                JdbcColumn[] pk = table.pk;
                if (pk != null) {
                    pklen = pk.length;
                }
            }
        }

        boolean hadJdbcColumn = false;
        if (extensions != null) {
            JdbcColumn col;
            int ne = extensions.length;
            for (int i = 0; i < ne; i++) {
                JdoExtension e = extensions[i];
                switch (e.key) {

                    case JDBC_COLUMN:
                        if (hadJdbcColumn) break;
                        if (pklen > 1) {
                            throw BindingSupportImpl.getInstance().runtime("jdbc-column extensions must be nested in jdbc-ref " +
                                    "extensions as " + targetClass.qname + " has " +
                                    "composite primary key\n" + e.getContext());
                        }
                        if (target != null) {
                            colsList.add(mdb.createColumn(extensions,
                                    target.table.pk[0]));
                        } else { // not a jdbc class
                            colsList.add(mdb.createColumn(extensions,
                                    fieldName, /*CHFC*/String.class/*RIGHTPAR*/));
                        }
                        hadJdbcColumn = true;
                        break;

                    case JDBC_REF:
                        if (targetClass.top.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
                            throw BindingSupportImpl.getInstance().runtime(
                                    "The jdbc-ref extension may only be used here " +
                                    "when referencing an application identity class\n" +
                                    e.getContext());
                        }
                        String fname = e.getString();
                        JdbcSimpleField pkf = target.findPkField(fname);
                        if (pkf == null) {
                            throw BindingSupportImpl.getInstance().runtime("Primary key field '" + fname + "' not found on " +
                                    target.cmd.qname + "\n" + e.getContext());
                        }
                        col = mdb.findColumn(colsList, pkf);
                        if (col != null) {
                            throw BindingSupportImpl.getInstance().runtime(
                                    "Duplicate jdbc-ref extension\n" + e.getContext());
                        }
                        col = mdb.createColumn(e.nested,
                                findColForPkField(pkf));
                        col.refField = pkf;
                        colsList.add(col);
                        break;

                    case JDBC_USE_JOIN:
                        useJoin = e.getEnum(mdb.jdbcMDE.USE_JOIN_ENUM);
                        break;

                    case JDBC_CONSTRAINT:
                        constraintName = e.value;
                        if (e.isNoValue()) doNotCreateConstraint = true;
                        break;
                    case JDBC_INDEX:
                        //Handled
                        break;

                    default:
                        if (e.isJdbc()) {
                            MetaDataBuilder.throwUnexpectedExtension(e);
                        }
                        break;
                }
            }
        }

        // check the columns (if specified) or generate them if not
        int nc = colsList.size();
        if (target == null && nc == 0) {
            colsList.add(
                    mdb.createColumn(null, fieldName, /*CHFC*/String.class/*RIGHTPAR*/));
            nc = 1;
        }
        if (nc == 0) {
            JdbcColumn[] pks = target.table.pk;
            if (pks != null) {
                int n = pks.length;
                for (int i = 0; i < n; i++) {
                    JdbcColumn c = pks[i].copy();
                    c.refField = pks[i].refField;
                    colsList.add(c);
                }
            }
        } else {
            if (pklen != nc) {
                throw BindingSupportImpl.getInstance().runtime("Class " + targetClass.qname + " has " + pklen +
                        " primary key column(s) but only " + nc + " column(s) " +
                        "have been declared\n" +
                        context.getContext());
            }
            if (pklen > 1) { // make sure order of colsList matches ref table
                cols = new JdbcColumn[nc];
                for (int i = 0; i < nc; i++) {
                    JdbcColumn col = (JdbcColumn)colsList.get(i);
                    cols[((JdbcClass)targetClass.top.storeClass).findPkFieldIndex(
                            col.refField)] = col;
                }
            }
        }

        if (cols == null) {
            nc = colsList.size();
            cols = new JdbcColumn[nc];
            colsList.toArray(cols);
        }

        // make sure all the cols have pk=false and foreignKey=true
        for (int i = cols.length - 1; i >= 0; i--) {
            JdbcColumn c = cols[i];
            c.pk = false;
            c.foreignKey = true;
        }
    }

    /**
     * If pkf belongs to targetClass then return its column. Otherwise
     * return the corresponding column from the tableClass for targetClass
     * (i.e. the class that owns the table that targetClass is mapped to
     * for vertical inheritance).
     */
    private JdbcColumn findColForPkField(JdbcSimpleField pkf) {
        if (pkf.fmd.classMetaData == targetClass) return pkf.col;
        JdbcColumn[] pk = ((JdbcClass)targetClass.storeClass).table.pk;
        for (int i = pk.length - 1; i >= 0; i--) {
            System.out.println("pk[" + i + "] = " + pk[i]);
            if (pk[i].refField == pkf) return pk[i];
        }
        throw BindingSupportImpl.getInstance().internal("No column found in table " +
                "for " + targetClass.qname + " to match " + pkf.fmd.name);
    }

    public boolean isDoNotCreateConstraint() {
        return doNotCreateConstraint;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public int getUseJoin() {
        return useJoin;
    }

    public JdbcColumn[] getCols() {
        return cols;
    }

    public ArrayList getColsList() {
        return colsList;
    }

}
 
