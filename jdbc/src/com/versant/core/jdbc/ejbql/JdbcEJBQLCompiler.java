
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
package com.versant.core.jdbc.ejbql;

import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.jdbc.fetch.SqlBuffer;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlParamUsage;
import com.versant.core.jdbc.sql.exp.ColumnExp;
import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.ejb.query.*;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.core.common.CmdBitSet;

import java.io.StringReader;

/**
 * This will compile an EJBQL query.
 */
public class JdbcEJBQLCompiler {

    private final JdbcStorageManager sm;
    private final ModelMetaData jmd;

    public JdbcEJBQLCompiler(JdbcStorageManager sm) {
        this.sm = sm;
        this.jmd = sm.getJmd();
    }

    /**
     * Compile a QueryDetails into a JdbcCompiledQuery ready to run.
     */
    public JdbcCompiledQuery compile(QueryDetails q) {
        Node tree = parse(q);
        if (Debug.DEBUG) {
            System.out.println("\n%%% parsed:\n" + tree + "\n");
        }

        ResolveContext rc = new ResolveContext(jmd);
        tree.resolve(rc);
        if (Debug.DEBUG) {
            System.out.println("%%% resolved:\n" + tree + "\n");
            rc.dump(System.out);
            System.out.println();
        }

        EJBQLNodeToSqlExp converter = new EJBQLNodeToSqlExp(rc,
                sm.getSqlDriver());
        SqlExp sqlExp = converter.toSqlExp(tree, null);
        if (!(sqlExp instanceof SelectExp)) {
            throw BindingSupportImpl.getInstance().internal("not supported");
        }

        SelectExp root = (SelectExp)sqlExp;
        FetchSpec spec = root.fetchSpec;
        spec.finish(1);

        JdbcCompiledQueryEJBQL cq = new JdbcCompiledQueryEJBQL(
                rc.getRoot(0).getNavClassMetaData(), q, spec);
        spec.setSqlBuffer(cq.getSqlBuffer());
        spec.generateSQL();

        compileParams(rc, spec);
        if (Debug.DEBUG) {
            System.out.println("%%% root:");
            root.dump("  ");
            System.out.println("\n%%% root.fetchSpec");
            spec.printPlan(System.out, "  ");
        }



        CmdBitSet bits = new CmdBitSet(jmd);
        for (int i = rc.getRootCount() - 1; i >= 0; i--) {
            rc.getRoot(i).addInvolvedClasses(bits);
        }
        int[] a = q.getExtraEvictClasses();
        if (a != null) {
            for (int i = a.length - 1; i >= 0; i--) {
                bits.add(jmd.classes[a[i]]);
            }
        }
        cq.setFilterClsIndexs(bits.toArray());
        cq.setEvictionClassBits(bits.getBits());
        cq.setEvictionClassIndexes(bits.getIndexes());

        return cq;
    }

    private Node parse(QueryDetails q) {
        String filter = q.getFilter();
        if (filter == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "EJBQL query string (filter) is null");
        }
        StringReader r = new StringReader(filter);
        EJBQLParser p = new EJBQLParser(r);
        try {
            return p.ejbqlQuery();
        } catch (ParseException e) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    e.getMessage());
        }
    }

    /**
     * This nasty code has to find the bits of SQL occupied by parameter
     * expressions that could be null. They may need to be converted into
     * 'is null' or 'is not null' or removed completely (for shared columns)
     * if the corresponding parameter is null. This is not required by the
     * JSR 220 spec but we already have it for JDOQL so might as well have
     * it for EJBQL as well.
     *
     * The parameter code is way too complex and needs to be refactored. A
     * better solution would be to create a new 'fake parameter' for each time
     * a given input parameter is used in the query. That would simplify
     * things a lot. The real parameters to the query can be easily expanded
     * to match the list of 'fake parameters'. This would simplify things by
     * getting rid of the first layer of usage.
     */
    private void compileParams(ResolveContext rc, FetchSpec spec) {
        ResolveContext.ParamUsage[] params = rc.getParameters();
        if (params == null) {
            return;
        }
        SqlBuffer.Param list = null;
        SqlBuffer.Param pos = null;
        int np = params.length;
        for (int i = 0; i < np; i++) {
            for (ResolveContext.ParamUsage rcUsage = params[i];
                    rcUsage != null; rcUsage = rcUsage.getNext()) {
                SqlParamUsage usage = (SqlParamUsage)rcUsage.storeObject;

                // create new param and add it to the list
                SqlBuffer.Param param = new SqlBuffer.Param(
                        rcUsage.getParamNode().getName());
                if (pos == null) {
                    pos = list = param;
                } else {
                    pos = pos.next = param;
                }

                // fill in the param
                param.declaredParamIndex = rcUsage.getIndex();
                JdbcField jdbcField = usage.jdbcField;
                if (jdbcField == null) {
                    param.classIndex = usage.classIndex;
                    param.fieldNo = -1;
                    param.javaTypeCode = usage.javaTypeCode;
                    // todo param.javaTypeCode == 0: get type from value?
                    param.jdbcType = usage.jdbcType;
                    param.col = usage.col;
                } else {
                    param.classIndex = jdbcField.fmd.classMetaData.index;
                    param.fieldNo = jdbcField.stateFieldNo;
                    param.col = usage.col;
                }
                param.mod = usage.mod;

                //  make a CharSpan for each ? in the SQL
                if (usage.expCount > 0) {
                    SqlBuffer.CharSpan cspos = null;
                    SqlBuffer.CharSpan[] a
                            = new SqlBuffer.CharSpan[usage.expCount];
                    boolean multicol = usage.expCount > 1;
                    int j = 0;
                    int removeCount = 0;
                    for (SqlExp e = usage.expList; j < a.length; j++) {
                        SqlBuffer.CharSpan cs = a[j] =
                                new SqlBuffer.CharSpan();
                        if (multicol && mustBeRemovedIfNull(e)) {
                            if (++removeCount == a.length) {
                                // all expressions are to be removed so restart
                                // the loop making them all 'is null' instead
                                multicol = false;
                                e = usage.expList;
                                j = -1;
                                cspos = null;
                                continue;
                            }
                            cs.firstCharIndex = e.getPreFirstCharIndex();
                            if (e.getNext() == null) {   // last span
                                cs.lastCharIndex = e.getLastCharIndex();
                                // work back and remove trailing 'and' if any
                                for (int k = j - 1; k >= 0; k--) {
                                    if (a[k].type != SqlBuffer.CharSpan.TYPE_REMOVE) {
                                        a[k + 1].firstCharIndex -= 4; // 'and '
                                        break;
                                    }
                                }
                            } else {    // first or middle span
                                cs.lastCharIndex = e.getNext().getPreFirstCharIndex();
                            }
                            cs.type = SqlBuffer.CharSpan.TYPE_REMOVE;
                        } else {
                            cs.firstCharIndex = e.getFirstCharIndex();
                            cs.lastCharIndex = e.getLastCharIndex();
                            cs.type = e.isNegative()
                                    ? SqlBuffer.CharSpan.TYPE_NOT_NULL
                                    : SqlBuffer.CharSpan.TYPE_NULL;
                        }

                        if (cspos == null) {
                            cspos = param.charSpanList = cs;
                            param.firstCharIndex = cs.firstCharIndex;
                        } else {
                            cspos = cspos.next = cs;
                        }

                        e = e.getNext();
                    }
                } else {
                    param.firstCharIndex = usage.expList.getFirstCharIndex();
                }
            }
        }
        if (list != null) {
            spec.setParamList(sortParams(list));
        }
    }

    /**
     * Columns that are not updated (i.e. are shared) must be removed
     * completely if the matching parameter is null.
     */
    private static boolean mustBeRemovedIfNull(SqlExp e) {
        if (!e.isNegative() && e.childList instanceof ColumnExp) {
            ColumnExp ce = (ColumnExp)e.childList;
            return !ce.col.isForUpdate();
        }
        return false;
    }

    /**
     * Sort the params in the order that they appear in the query.
     */
    private static SqlBuffer.Param sortParams(SqlBuffer.Param list) {
        if (list.next == null) return list;
        // stone sort the list (bubble sort except elements sink to the bottom)
        for (; ;) {
            boolean changed = false;
            SqlBuffer.Param p0 = null;
            for (SqlBuffer.Param p1 = list; ;) {
                SqlBuffer.Param p2 = p1.next;
                if (p2 == null) break;
                if (p1.declaredParamIndex > p2.declaredParamIndex) {
                    // exchange p and p2
                    p1.next = p2.next;
                    p2.next = p1;
                    if (p0 == null) {
                        list = p2;
                    } else {
                        p0.next = p2;
                    }
                    p0 = p2;
                    changed = true;
                } else {
                    p0 = p1;
                    p1 = p2;
                }
            }
            if (!changed) return list;
        }
    }

    /*
    private void dumpParams(JdbcCompiledQuery cq, SqlBuffer.Param p) {
        for (; p != null; p = p.next) {
            if (Debug.DEBUG) {
                Debug.OUT.println("Param " + p.declaredParamIndex +
                        " firstCharIndex " + p.firstCharIndex);
            }
            for (SqlBuffer.CharSpan s = p.charSpanList; s != null; s = s.next) {
                if (Debug.DEBUG) {
                    String ts;
                    switch (s.type) {
                        case SqlBuffer.CharSpan.TYPE_NULL:
                            ts = "NULL";
                            break;
                        case SqlBuffer.CharSpan.TYPE_NOT_NULL:
                            ts = "NOT_NULL";
                            break;
                        case SqlBuffer.CharSpan.TYPE_REMOVE:
                            ts = "REMOVE";
                            break;
                        default:
                            ts = "Unknown(" + s.type + ")";
                    }
                    Debug.OUT.println("  CharSpan " + s.firstCharIndex + " to " +
                            s.lastCharIndex + " " + ts + " = '" +
                            cq.getSqlbuf().toString(s.firstCharIndex,
                                    s.lastCharIndex - s.firstCharIndex) + "'");
                }
            }
        }
    }
    */

}

