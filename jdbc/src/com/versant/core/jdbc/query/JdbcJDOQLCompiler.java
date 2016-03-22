
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
package com.versant.core.jdbc.query;

import com.versant.core.common.Debug;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.common.CmdBitSet;
import com.versant.core.metadata.*;
import com.versant.core.jdo.query.*;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.ProjectionQueryDecoder;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This will compile a JDOQL query into a JdbcCompiledQuery.
 *
 * @see JdbcCompiledQuery
 * @see #reinit
 */
public final class JdbcJDOQLCompiler {

    private final JdbcStorageManager sm;
    private final ModelMetaData jmd;
    private final JDOQLNodeToSqlExp visitor;

    // these fields must be set to null in reinit
    private ClassMetaData cmd;
    private ParamNode[] params;
    private OrderNode[] orders;
    private UnaryNode filter;
    private ResultNode resultNode;
    private GroupingNode groupingNode;
    private QueryParser qParser;
    private SelectExp candidateSelectExp;

    public JdbcJDOQLCompiler(JdbcStorageManager sm) {
        this.sm = sm;
        this.jmd = sm.getJmd();
        visitor = new JDOQLNodeToSqlExp(this);
    }

    /**
     * Get this compiler ready to compile more queries. This is called before
     * it is returned to the pool.
     */
    public void reinit() {
        cmd = null;
        params = null;
        orders = null;
        filter = null;
        qParser = null;
        resultNode = null;
        groupingNode = null;
        candidateSelectExp = null;
    }

    public QueryParser getQParser() {
        return qParser;
    }

    public JDOQLNodeToSqlExp getVisitor() {
        return visitor;
    }

    /**
     * Compile a QueryDetails into a JdbcCompiledQuery ready to run.
     */
    public JdbcCompiledQuery compile(QueryDetails q) {

        cmd = jmd.getClassMetaData(q.getCandidateClass());
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Class " +
                    q.getCandidateClass().getName() + " not found in meta data");
        }

        return compileImp(q);
    }

    /**
     * Create a filter exp for a collection query.
     */
    public SelectExp compileParallelFetch(QueryDetails q) {
        cmd = jmd.getClassMetaData(q.getCandidateClass());
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Class " +
                    q.getCandidateClass().getName() + " not found in meta data");
        }
        return compileParallelFetchImp(q);
    }

    private JdbcCompiledQuery compileImp(final QueryDetails q) {
        final JdbcCompiledQuery cq = new JdbcCompiledQuery(cmd, q);

        qParser = new QueryParser(jmd);

        try {
            qParser.parse(q);

            resolveVarNodes(qParser);
            params = qParser.getParams();
            orders = qParser.getOrders();
            filter = qParser.getFilter();
            resultNode = qParser.getResultNode();
            groupingNode = qParser.getGroupingNode();
        } catch (Exception e) {
            if (BindingSupportImpl.getInstance().isOwnException(e)) {
                throw (RuntimeException)e;
            } else {
                throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
            }
        } catch (TokenMgrError e) {
            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
        }

        // build the SQL query tree
        candidateSelectExp = new SelectExp();
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        candidateSelectExp.table = jdbcClass.table;

        final FetchGroup fg = cmd.fetchGroups[q.getFetchGroupIndex()];

        //do inner joins to basetable
        for (FetchGroup supg = fg.superFetchGroup; supg != null;
             supg = supg.superFetchGroup) {
            JdbcClass sc = (JdbcClass)supg.classMetaData.storeClass;

            if (sc.table != candidateSelectExp.table) {
                // different table so do an inner join
                SelectExp se = candidateSelectExp.findTable(sc.table);
                if (se == null) {
                    se = new SelectExp();
                    se.table = sc.table;
                    candidateSelectExp.addJoin(candidateSelectExp.table.pk,
                            se.table.pk, se);
                }
            }
        }

        if (filter != null) {
            candidateSelectExp.whereExp = visitor.toSqlExp(filter,
                    candidateSelectExp, null, 0, null);
        }

        // include only correct subclass(es) if cmd is in a hierarchy
        if (cmd.isInHierarchy()) addSubclassFilter(candidateSelectExp, cq);

        if (Debug.DEBUG) {
            Debug.OUT.println("\n* SQL tree:");
            candidateSelectExp.dump("");
        }

        final SqlDriver driver = sm.getSqlDriver();
        final FetchSpec fSpec = new FetchSpec(candidateSelectExp, driver);
        cq.setFetchSpec(fSpec);
        fSpec.setSqlBuffer(cq.getSqlBuffer());

        fSpec.setFilterExpFactory(new FilterExpFactory() {
            public SelectExp createFilterExp() {
                JdbcJDOQLCompiler qc = new JdbcJDOQLCompiler(sm);
                return qc.compileParallelFetch(q);
            }
        });

        SqlExp orderByFromCrossJoin = null;
        if (resultNode == null) {
            candidateSelectExp.normalize(driver, null, driver.isConvertExistsToDistinctJoin());
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Normalized SQL tree:");
                candidateSelectExp.dump("");
            }
            cq.process();

            fSpec.getOptions().setUseOneToManyJoin(cq.isOneToManyJoinAllowed());
            fSpec.getOptions().setUseParallelQueries(cq.isParColFetchEnabled());


            // add the fetch group to the query and make sure the pk is also there
            addSelectFetchGroupFO(fSpec,
                    fg, cq.isIncludeSubclasses());

//            addPrimaryKey(candidateSelectExp);

            cq.setSelectColumnCount(
                    candidateSelectExp.getSelectListColumnCount());
            orderByFromCrossJoin = candidateSelectExp.orderByList;
            candidateSelectExp.orderByList = null;
        } else {
            processNode(resultNode, "Result");
            processNode(groupingNode, "Grouping");
            if (groupingNode != null && groupingNode.havingNode != null) {
                processNode(groupingNode.havingNode, "Having");
            }
            ProjectionQueryDecoder decoder =
                    new ProjectionQueryDecoder(resultNode, driver);

            cq.setProjectionDecoder(decoder);
            cq.setGroupingNode(groupingNode);
            cq.process();

            fSpec.getOptions().setUseOneToManyJoin(cq.isOneToManyJoinAllowed());
            fSpec.getOptions().setUseParallelQueries(cq.isParColFetchEnabled());

            FopProjectionController projectionFopController
                    = new FopProjectionController(fSpec,
                            FetchOpDataMainRS.INSTANCE, decoder, cmd, fg,
                            cq.isIncludeSubclasses(), visitor, resultNode);
            fSpec.addFetchOp(projectionFopController, true);
            fSpec.finish(1);



            /**
             * remove distinct if all the existExp that was removed was for the
             * variable that is in the result string.
             *
             * Check to see if the candidateSelectExp constains joins that require
             * a distinct
             */
            candidateSelectExp.normalize(driver, null, resultNode.processForVarNodes()
                     || driver.isConvertExistsToDistinctJoin());

            if (resultNode.isDistinct()) {
                candidateSelectExp.distinct = true;
            }

            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Normalized SQL tree:");
                candidateSelectExp.dump("");
            }

        }

        if (Debug.DEBUG) {
            fSpec.printPlan(System.out, "    ");
        }

        candidateSelectExp.groupByList = getGroupingExp();
        if (groupingNode != null) {
            if (groupingNode.havingNode == null) {
                candidateSelectExp.havingExp = null;
            } else {
                candidateSelectExp.havingExp = new HavingExp(visitor.toSqlExp(
                        groupingNode.havingNode, candidateSelectExp, null, 0,
                        null));
            }
        }

        reOrderJoinExp(cmd.fetchGroups[q.getFetchGroupIndex()], candidateSelectExp);

        // add an orderby if required
        if (orders != null) {
            addOrderBy(candidateSelectExp);
        }

        if (Debug.DEBUG) {
            Debug.OUT.println("\n* Finished SQL tree:");
            candidateSelectExp.dump("");
        }

        /**
         * determine if we should add a orderBy pk. This is only needed for
         * parColFetching. If this is a aggregate only query then this is not needed.
         */
        if (cq.isContainsThis()) {
            if (cq.isParColFetchEnabled() || cq.isOneToManyJoinAllowed()) {
                candidateSelectExp.appendOrderByForColumns(((JdbcClass)cmd.storeClass).table.pk);
            }
        }

        /**
         * Add the orderby as provided by the crossjoin
         */
        if (orderByFromCrossJoin != null) {
            candidateSelectExp.appendOrderByExp(orderByFromCrossJoin);
        }

        //find all equivalent joins
        SelectExp.mergeJoinList(candidateSelectExp.joinList);

        if (Debug.DEBUG) {
            Debug.OUT.println("\n* BEFORE doFinal:");
            candidateSelectExp.dump("");
        }
        doFinalSql(cq.getSqlBuffer(), candidateSelectExp, driver);
//        cq.getFetchSpec().setSqlBuffer(cq.getSqlBuffer());

        // build params from ParamNode usages
        if (params != null && params.length != 0 ) {
            compileParams(qParser, cq.getSqlBuffer());
        }

        if (Debug.DEBUG) {
            Debug.OUT.println("\nParams:");
            dumpParams(cq, cq.getParamList());
        }

        final CmdBitSet bits = qParser.getCmds();
        int[] a = q.getExtraEvictClasses();
        if (a != null) {
            for (int i = a.length - 1; i >= 0; i--) {
                bits.add(jmd.classes[a[i]]);
            }
        }
        cq.setFilterClsIndexs(bits.toArray());
        cq.setCacheable(bits.isCacheble() && !q.isRandomAccess());
        cq.setEvictionClassBits(bits.getBits());
        cq.setEvictionClassIndexes(bits.getIndexes());
        return cq;
    }

//    /**
//     * The QueryDetails will override the fetchgroup value. If both unspecified
//     * then use project default.
//     * @param qd
//     * @param fGroup
//     */
//    private static int getFetchDepth(QueryDetails qd, FetchGroup fGroup) {
//        if (qd.getFetchDepth() >= 0) {
//            return qd.getFetchDepth();
//        }
//        if (fGroup.getFetchDepth() >= 0) {
//            return fGroup.getFetchDepth();
//        }
//        return 1;
//    }

    private void addSelectFetchGroupFO(FetchSpec spec, FetchGroup group,
            boolean includeSubclasses) {
        FopGetOID oidFop = new FopGetOID(spec, FetchOpDataMainRS.INSTANCE,
                group.classMetaData, spec.getRoot());
        spec.addFetchOp(oidFop, true);

        FopGetState stateFop = new FopGetState(spec, oidFop.getOutputData(),
                group, includeSubclasses, spec.getRoot(), 0, group.classMetaData, new FetchFieldPath());
        spec.addFetchOp(stateFop, true);

        spec.finish(1);
        if (Debug.DEBUG) spec.printPlan(System.out, "    ");
    }

    /**
     * Fill in the storeExtent of all variables.
     */
    private void resolveVarNodes(QueryParser qParser) {
        VarNode[] vars = qParser.getVars();
        if (vars == null || vars.length == 0) {
            return;
        }
        for (int i = 0; i < vars.length; i++) {
            VarNode var = vars[i];
            ClassMetaData vcmd = var.getCmd();
            if (vcmd == null) {
                continue;
            }
            SelectExp se = new SelectExp();
            se.table = ((JdbcClass)vcmd.storeClass).table;
            se.var = var;
            if (vcmd.pcSuperMetaData != null) {
                // subclass so add test for correct class-id in jdo_class
                // column
                se.whereExp =
                        ((JdbcClass)vcmd.storeClass).getCheckClassIdExp(se);
            }
            var.setStoreExtent(se);
        }
    }

    public static void reOrderJoinExp(FetchGroup fg, SelectExp se) {
        Join current = se.joinList;
        FetchGroupField[] fgfs = fg.fields;
        for (int i = 0; i < fgfs.length; i++) {
            FetchGroupField fgf = fgfs[i];

            if (fgf.fmd.category != MDStatics.CATEGORY_REF) continue;
            Join join = se.findJoin((JdbcField)fgf.fmd.storeField);
            if (join != null) {
                if (join == current) {
                    current = current.next;
                } else {
                    Join beforeToMove = findJoinBefore(join,
                            se.joinList);
                    Join beforeCurrent = findJoinBefore(current,
                            se.joinList);

                    if (beforeToMove.next != join) {
                        throw new RuntimeException("before.next != join");
                    }

                    beforeToMove.next = join.next;
                    join.next = current;
                    if (beforeCurrent != null) beforeCurrent.next = join;
                    if (current == se.joinList) {
                        se.joinList = join;
                    }
                }
            }
        }
    }

    private SqlExp getGroupingExp() {
        SqlExp gpExp = null;
        if (groupingNode != null) {
            gpExp = visitor.toSqlExp(groupingNode, candidateSelectExp, null, 0,
                    null);
        }
        return gpExp;
    }

    private void processNode(Node node, String info) {
        if (node == null) return;
        if (Debug.DEBUG) {
            Debug.OUT.println("\n* " + info + ": " + node);
            Debug.OUT.println("\n* Parsed tree:");
            node.dump("");
        }

        node.normalize();
        if (Debug.DEBUG) {
            Debug.OUT.println("\n* Normalized tree:");
            node.dump("");
        }

        node.resolve(qParser, cmd, false);
        if (Debug.DEBUG) {
            Debug.OUT.println("\n* Resolved tree:");
            node.dump("");
        }

        node.normalize();
        if (Debug.DEBUG) {
            Debug.OUT.println("\n* Second normalized tree:");
            node.dump("");
        }
    }

    private SelectExp compileParallelFetchImp(QueryDetails q) {
        JdbcCompiledQuery cq = new JdbcCompiledQuery(cmd, q);
        qParser = new QueryParser(jmd);

        try {
            qParser.parse(q);
            resolveVarNodes(qParser);
            params = qParser.getParams();
            orders = qParser.getOrders();
            filter = qParser.getFilter();
        } catch (Exception e) {
        	if(BindingSupportImpl.getInstance().isOwnException(e)) {
        		throw (RuntimeException) e;
            } else {
            	throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
            }
        } catch (TokenMgrError e) {
            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
        }

        if (filter != null) {
            filter.normalize();
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Normalized tree:");
                filter.dump("");
            }
        }

        // build the SQL query tree
        candidateSelectExp = new SelectExp();
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        candidateSelectExp.table = jdbcClass.table;

        FetchGroup fg = cmd.fetchGroups[q.getFetchGroupIndex()];
        //do inner joins to basetable
        for (FetchGroup supg = fg.superFetchGroup; supg != null;
             supg = supg.superFetchGroup) {
            JdbcClass sc = (JdbcClass)supg.classMetaData.storeClass;

            if (sc.table != candidateSelectExp.table) {
                // different table so do an inner join
                SelectExp se = candidateSelectExp.findTable(sc.table);
                if (se == null) {
                    se = new SelectExp();
                    se.table = sc.table;
                    candidateSelectExp.addJoin(candidateSelectExp.table.pk,
                            se.table.pk, se);
                }
            }
        }

        if (filter != null) {
            candidateSelectExp.whereExp = visitor.toSqlExp(filter,
                    candidateSelectExp, null, 0, null);
        }

//        // add an order by if required
        if (orders != null) {
            addOrderBy(candidateSelectExp);
        }

        if (Debug.DEBUG) {
            Debug.OUT.println("\nSQL:\n" + cq.getSqlbuf());
        }
        return candidateSelectExp;
    }

    private static void doFinalSql(SqlBuffer sqlBuffer, SelectExp root,
            SqlDriver driver) {
        // get the final SQL
        int aliasCount = root.createAlias(0);
        if (aliasCount == 1) {
            root.alias = null;
            sqlBuffer.setFirstTableOrAlias(root.table.name);
        } else {
            sqlBuffer.setFirstTableOrAlias(root.alias);
        }

        root.appendSQL(driver, sqlBuffer.getSqlbuf(), null);
        sqlBuffer.setSelectListRange(root.distinct, root.selectListStartIndex,
                root.selectListFirstColEndIndex, root.selectListEndIndex);
        sqlBuffer.setOrderByRange(root.orderByStartIndex, root.orderByEndIndex);

        // work around bug with replace in CharBuffer class
        sqlBuffer.getSqlbuf().append(' ');

        if (Debug.DEBUG) {
            System.out.println("\nSQL:\n" + sqlBuffer.getSqlbuf());
        }
    }

    public static Join findJoinBefore(Join aJoin, Join root) {
        for (Join join = root; join != null; join = join.next) {
            if (join.next == aJoin) return join;
        }
        return null;
    }

    private void addSubclassFilter(SelectExp root, JdbcCompiledQuery cq) {
        // no filter needed if we want the whole hierarchy
        if (((JdbcClass)cmd.storeClass).classIdCol != null) {
            if (cq.isIncludeSubclasses() && cmd.pcSuperMetaData == null) return;
            if (cq.isIncludeSubclasses()
                    && ((JdbcClass)cmd.storeClass).inheritance == JdbcClass.INHERITANCE_VERTICAL) {
                return;
            }
            addClassIdFilter(root, cq);
        } else {
            if (!cq.isIncludeSubclasses()) {
                if (cmd.pcSubclasses == null) return;
                //must add joins to immediate sub tables and where id col is null
                for (int i = 0; i < cmd.pcSubclasses.length; i++) {
                    ClassMetaData pcSubclass = cmd.pcSubclasses[i];

                    SelectExp se = root.findTable(((JdbcClass)pcSubclass.storeClass).table);
                    if (se == null) {
                        se = new SelectExp();
                        se.outer = true;
                        se.table = ((JdbcClass)pcSubclass.storeClass).table;

                        SqlExp colExp = ((JdbcClass)pcSubclass.storeClass).table.pk[0].toSqlExp(se);

                        root.appendToWhereExp(new IsNullExp(colExp));
                        root.addJoin(root.table.pk, se.table.pk, se);
                    }
                }
            }
        }

    }

    private void addClassIdFilter(SelectExp root, JdbcCompiledQuery cq) {
        // create expression for the class ID column
        JdbcColumn cidcol = ((JdbcClass)cmd.storeClass).classIdCol;
        SelectExp se = root.findTable(cidcol.table);
        if (se == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Table for classId column not in SelectExp");
        }
        SqlExp cidexp = cidcol.toSqlExp(se);
        if (cidexp.getNext() != null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Compound classId columns not implemented");
        }

        SqlExp fe;
        if (!cq.isIncludeSubclasses() || cmd.pcSubclasses == null) {
            // classid = literal expression
            fe = new BinaryOpExp(cidexp, BinaryOpExp.EQUAL,
                    cidcol.createClassIdLiteralExp(((JdbcClass)cmd.storeClass).jdbcClassId));
        } else {
            // in expression with a literal for each class id
            cidexp.setNext(createClassIdLiteralExp(cmd));
            fe = new InExp(cidexp);
        }

        // add this to the where clause
        if (root.whereExp == null) {
            root.whereExp = fe;
        } else if (root.whereExp instanceof AndExp) {
            root.whereExp.append(fe);
        } else {
            root.whereExp.setNext(fe);
            root.whereExp = new AndExp(root.whereExp);
        }
    }

    /**
     * Create a list of LiteralExp's for the class ID's of cmd and all of
     * its subclasses.
     */
    private SqlExp createClassIdLiteralExp(ClassMetaData cmd) {
        SqlExp e = ((JdbcClass)cmd.storeClass).classIdCol.createClassIdLiteralExp(
                ((JdbcClass)cmd.storeClass).jdbcClassId);
        ClassMetaData[] a = cmd.pcSubclasses;
        if (a != null) {
            SqlExp p = e;
            for (int i = a.length - 1; i >= 0; i--) {
                SqlExp q = createClassIdLiteralExp(a[i]);
                for (; p.getNext() != null; p = p.getNext()) ;
                p = p.setNext(q);
            }
        }
        return e;
    }

    private SqlExp addOrderBy(SelectExp root) {
        int len = orders.length;
        for (int i = 0; i < len; i++) orders[i].resolve(qParser, cmd, true);
        return root.addOrderBy(orders, false, visitor);
    }

    /**
     * This nasty code has to find the bits of SQL occupied by parameter
     * expressions that could be null. They may need to be converted into
     * 'is null' or 'is not null' or removed completely (for shared columns)
     * if the corresponding parameter is null.
     */
    public static void compileParams(QueryParser qParser, SqlBuffer sqlBuffer) {
        if (qParser.getParams() == null) return;
        SqlBuffer.Param list = null;
        SqlBuffer.Param pos = null;
        ParamNode[] params = qParser.getParams();
        int np = params.length;
        for (int i = 0; i < np; i++) {
            ParamNode p = params[i];
            SqlParamUsage usage = (SqlParamUsage)p.usageList;
            if (usage == null) continue;

            for (; usage != null; usage = usage.next) {

                // create new param and add it to the list
                SqlBuffer.Param param = new SqlBuffer.Param(p.getIdentifier());
                if (pos == null) {
                    pos = list = param;
                } else {
                    pos = pos.next = param;
                }

                // fill in the param
                param.declaredParamIndex = i;
                JdbcField jdbcField = usage.jdbcField;
                if (jdbcField == null) {
                    param.classIndex = usage.classIndex;
                    param.fieldNo = -1;
                    param.javaTypeCode = usage.javaTypeCode;
                    if (param.javaTypeCode == 0) {
                        p.resolve(qParser, null, false);
                        param.javaTypeCode = MDStaticUtils.toTypeCode(
                                p.getCls());
                    }
                    param.jdbcType = usage.jdbcType;
                    param.col = usage.col;
                } else {
                    param.classIndex = jdbcField.fmd.classMetaData.index;
                    param.fieldNo = jdbcField.stateFieldNo;
                    param.col = usage.col;
                }
                param.mod = usage.mod;

                //  make a CharSpan for each usage
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
            sqlBuffer.setParamList(sortParams(list));
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
                if (p1.firstCharIndex > p2.firstCharIndex) {
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
                        case SqlStruct.CharSpan.TYPE_NULL:
                            ts = "NULL";
                            break;
                        case SqlStruct.CharSpan.TYPE_NOT_NULL:
                            ts = "NOT_NULL";
                            break;
                        case SqlStruct.CharSpan.TYPE_REMOVE:
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

    public ModelMetaData getJmd() {
        return jmd;
    }

    /**
     * Get the select for the candidate class. All other SqlExp's for the
     * query are reachable from this.
     */
    public SelectExp getCandidateSelectExp() {
        return candidateSelectExp;
    }
}
