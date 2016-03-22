
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
package com.versant.core.jdbc.fetch;

import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;
import com.versant.core.server.StateContainer;

import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * FetchOp to determine the type of the instance.
 */
public class FopGetType extends FetchOp {
    private final ClassMetaData cmd;
    private final FetchOpData src;
    private final Data data;
    private FetchGroup fg;


    private SelectExp joinFromExp;


    public FopGetType(FetchSpec spec, ClassMetaData cmd, FetchOpData src,
            FetchGroup fg, SelectExp joinFromExp) {
        super(spec);
        this.cmd = cmd;
        this.src = src;
        this.fg = fg;
        data = new Data(src);
        this.joinFromExp = joinFromExp;
    }

    /**
     * This gets our state from fetchData and delegates to our src for
     * the OID and ResultSet.
     */
    public class Data extends FetchOpDataProxy {

        public Data(FetchOpData src) {
            super(src);
        }

        public void setType(FetchResult fetchResult, ClassMetaData type) {
            fetchResult.setData(FopGetType.this, type);
        }

        public ClassMetaData getType(FetchResult fetchResult) {
            return (ClassMetaData) fetchResult.getData(FopGetType.this);
        }

        public String getDescription() {
            return " [" + getIndex() + "]";
        }
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer) throws SQLException {
        OID oid = src.getOID(fetchResult);
        if (oid == null) return;

        ResultSet rs = src.getResultSet(fetchResult);
        ClassMetaData localCmd = cmd;

        /**
         * This will only work if the hierarchy is mapped in same table
         */
        if (((JdbcClass)cmd.storeClass).classIdCol != null) {
            // read the classId column from rs to decide what the real class is
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            JdbcColumn classIdCol = jdbcClass.classIdCol;
            Object classId = classIdCol.get(rs, getFirstColIndex());
            if (rs.wasNull()) {
                throw BindingSupportImpl.getInstance().objectNotFound("No row for " +
                        cmd.storeClass + " " + oid.toSString() + " OR " + classIdCol.name +
                        " is null for row");
            }
            localCmd = jdbcClass.findClass(classId);
            if (localCmd == null) {
                throw BindingSupportImpl.getInstance().fatalDatastore(
                        "Row for OID " + oid.toSString() +
                        " is not in the hierarchy starting at " +
                        cmd.storeClass +
                        " (" + classIdCol.name + " for row is " + classId + ")");
            }
        } else if (cmd.pcSubclasses != null) {
            if (((JdbcClass)cmd.storeClass).readAsClass != null) {
                localCmd = ((JdbcClass)cmd.storeClass).readAsClass;
            } else {
                //must start from end of rs and work up to determine type
                localCmd = extractType(rs, fg, getFirstColIndex());
                if (localCmd == null) {
                    throw BindingSupportImpl.getInstance().internal("The instance type for " +
                            "this row could not be determined");
                }
            }
        }
        data.setType(fetchResult, localCmd);
    }

    private SqlExp addSubClassIds(final SelectExp root) {
        FetchGroup[] subFGs = fg.subFetchGroups;
        if (subFGs == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "This should only be invoked where there is subClasses");
        }

        SqlExp holder = new SqlExp();
        SqlExp p = holder;
        for (int i = 0; i < subFGs.length; i++) {
            holder = addSubClassIdImp(root, holder, subFGs[i]);
        }
        return p.getNext();
    }

    private SqlExp addSubClassIdImp(final SelectExp root, SqlExp se, FetchGroup fg) {
        final JdbcTable subClassTable = ((JdbcClass) fg.classMetaData.storeClass).table;
        //Find the selectExp for the subtable from the root.
        SelectExp subClassSelectExp = root.findTableRecursive(subClassTable);
        if (subClassSelectExp == null) {
            //must add the join the subclass table.
            subClassSelectExp = new SelectExp();
            subClassSelectExp.table = subClassTable;
            subClassSelectExp.outer = true;
            root.addJoin(root.table.pk, subClassSelectExp.table.pk, subClassSelectExp);
        }

        //only need to add the first column of the primary key as all the columns of
        //the pk must be non null for it to be a valid key.
        se.setNext(subClassTable.pk[0].toSqlExp(subClassSelectExp));

        FetchGroup[] fgs = fg.subFetchGroups;
        if (fgs == null) return se.getNext();
        SqlExp result = se.getNext();
        for (int i = 0; i < fgs.length; i++) {
            result = addSubClassIdImp(root, result, fgs[i]);
        }
        return result;
    }

    /**
     * This method reads the identity columns that was added to the resultset
     * in order to determine the correct type of the subclass. The classid column
     * of the initial class is first and should be non-null in the resultset else
     * there is no valid reference.
     *
     * A null id column means that it is not of that type.
     */
    private ClassMetaData extractType(ResultSet rs, FetchGroup fg, int index)
            throws SQLException {
        FetchGroup[] subFGs = fg.subFetchGroups;
        if (subFGs == null) {
            return fg.classMetaData;        //there is no subclasses
        }

        ClassMetaData cmd;
        for (int i = 0; i < subFGs.length; i++) {
            cmd = extractTypeImp(rs, subFGs[i], index);
            index += subFGs[i].classMetaData.totalNoOfSubClasses + 1;
            if (cmd != null) {
                return cmd;
            }
        }
        return fg.classMetaData;
    }

    private ClassMetaData extractTypeImp(ResultSet rs, FetchGroup fg,
            int index) throws SQLException {
        rs.getString(index++);
        if (!rs.wasNull()) {
            FetchGroup[] subFGs = fg.subFetchGroups;
            if (subFGs == null) {
                return fg.classMetaData;
            }

            ClassMetaData cmd;
            for (int i = 0; i < subFGs.length; i++) {
                cmd = extractTypeImp(rs, subFGs[i], index);
                index += subFGs[i].classMetaData.totalNoOfSubClasses + 1;
                if (cmd != null) {
                    return cmd;
                }
            }
            return fg.classMetaData;
        } else {
            return null;
        }
    }

    public String getDescription() {
        return "Determine the correct type for a " + cmd.qname + " instance: firstColIndex " + getFirstColIndex();
    }

    public FetchOpData getOutputData() {
        return data;
    }

    public SqlExp init(SelectExp root) {
        return doSubs();
    }

    private SqlExp doSubs() {
        JdbcClass jdbcClass = (JdbcClass) cmd.storeClass;
        if (jdbcClass.classIdCol != null) {
            JdbcColumn classIdCol = jdbcClass.classIdCol;
            if (classIdCol != null) {
                SelectExp se;
                if (classIdCol.table != joinFromExp.table) {
                        se = new SelectExp();
                        se.table = classIdCol.table;
                        se.outer = joinFromExp.outer;
                        joinFromExp.addJoin(joinFromExp.table.pk, se.table.pk,
                                se);
                } else {
                    se = joinFromExp;
                }

                // put the expression at the start of the select list
                return classIdCol.toSqlExp(se);
            }
        } else if (cmd.pcSubclasses != null
                && ((JdbcClass)cmd.storeClass).readAsClass == null) {
            return addSubClassIds(joinFromExp);
        }
        return null;
    }

    protected String toStringImp() {
        return " firstColIndex = " + getFirstColIndex();
    }
}
