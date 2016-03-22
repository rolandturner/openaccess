
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

import com.versant.core.jdo.query.*;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.sql.exp.AggregateCountStarExp;
import com.versant.core.jdbc.sql.conv.DummyPreparedStmt;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.ClassMetaData;

import java.sql.SQLException;

/**
 * Walks a JDOQL Node trees to produce SqlExp trees.
 */
public class JDOQLNodeToSqlExp extends NodeVisitorAdapter {

    private final JdbcJDOQLCompiler comp;

    /**
     * This instance can convert Node's that do not need to access a
     * JdbcQueryCompiler to SqlExp.
     */
    public static final JDOQLNodeToSqlExp INSTANCE = new JDOQLNodeToSqlExp();

    /**
     * Information passed down from a Node to its children via arrive
     * callback mechanism.
     */
    private static class Context {

        public SelectExp root;
        public SqlExp leftSibling;
        public int method;
        public Node args;

        public Context(SelectExp root) {
            this.root = root;
        }

        public Context(SelectExp root, SqlExp leftSibling, int method, Node args) {
            this.root = root;
            this.leftSibling = leftSibling;
            this.method = method;
            this.args = args;
        }
    }

    public JDOQLNodeToSqlExp(JdbcJDOQLCompiler comp) {
        this.comp = comp;
    }

    private JDOQLNodeToSqlExp() {
        this(null);
    }

    /**
     * Convert a Node tree to an SqlExp tree.
     */
    public SqlExp toSqlExp(Node node, SelectExp root, SqlExp leftSibling,
            int method, Node args) {
        return (SqlExp)node.arrive(this, new Context(root, leftSibling,
                method, args));
    }

    protected Object defaultArrive(Node node, Object msg) {
        throw BindingSupportImpl.getInstance().internal("Not implemented: " +
                node.getClass());
    }

    public Object arriveFieldNavNode(FieldNavNode node, Object msg) {
        Context ctx = (Context)msg;
        SelectExp root = ctx.root;
        if (node.var == null) {
            JdbcField jdbcField = (JdbcField)node.fmd.storeField;
            JdbcClass targetClass = (JdbcClass)node.targetClass.storeClass;
            SelectExp se;
            Join j = root.findJoin(jdbcField);
            if (j == null) {
                SelectExp leftTableSE = root.findTable(jdbcField.mainTable);
                if (leftTableSE == null) {
                    throw BindingSupportImpl.getInstance().runtime(
                            "No join to '"
                            + jdbcField.mainTable + "' for nav field '" + jdbcField.fmd.name);
                }

                se = new SelectExp();
                se.table = targetClass.table;
                se.jdbcField = jdbcField;
                if (jdbcField instanceof JdbcPolyRefField) {
                    JdbcPolyRefField pf = (JdbcPolyRefField)jdbcField;
                    j = leftTableSE.addJoin(pf.refCols, se.table.pk, se);
                    // add a condition to check that the class-id column of
                    // the polyref matches the type used in the cast
                    j.appendJoinExp(
                            pf.createClassIdMatchExp(root, targetClass.cmd));
                } else {
                    j = leftTableSE.addJoin(jdbcField.mainTableCols,
                            se.table.pk, se);
                    if (node.cast != null && targetClass.cmd.isInHierarchy()) {
                        //join to basetable for classIdJoin
                        if (targetClass.classIdCol != null && targetClass.classIdCol.table != se.table) {
                            SelectExp bSe = root.findTable(targetClass.classIdCol.table);
                            if (bSe == null) {
                                bSe = new SelectExp();
                                bSe.outer = se.outer;
                                bSe.table = targetClass.classIdCol.table;
                                j = se.addJoin(se.table.pk, bSe.table.pk, bSe);
                            }
                        }

                        // downcast so join must include code to check the
                        // class-id column of the target table
                        j.appendJoinExp(targetClass.getCheckClassIdExp(se));
                    }
                }
            } else {
                se = j.selectExp;
            }
            if (node.childList == null) {
                return null;
            } else {
                return toSqlExp(node.childList, se, ctx.leftSibling,
                        ctx.method, ctx.args);
            }
        } else {
            return toSqlExp(node.childList, (SelectExp)node.var.getStoreExtent(),
                    ctx.leftSibling, ctx.method, ctx.args);
        }
    }

    public Object arriveLiteralNode(LiteralNode node, Object msg) {
        Context ctx = (Context)msg;
        SqlExp leftSibling = ctx.leftSibling;
        String value = node.value;
        int t;
        switch (node.type) {
            case LiteralNode.TYPE_OTHER:    t = LiteralExp.TYPE_OTHER;      break;
            case LiteralNode.TYPE_STRING:   t = LiteralExp.TYPE_STRING;     break;
            case LiteralNode.TYPE_NULL:     t = LiteralExp.TYPE_NULL;       break;
            case LiteralNode.TYPE_BOOLEAN:
                t = LiteralExp.TYPE_OTHER;

                /**
                 * The idea here is to convert the boolean literal 'true' or
                 * 'false' via the converter on the field that it is being compared to.
                 * If there is no converter on the field then we expect a 'true'
                 * or 'false' constant to work.
                 */
                if (ctx.leftSibling instanceof ColumnExp) {
                    ColumnExp cExp = (ColumnExp)leftSibling;
                    if (cExp.col.converter == null) break;
                    DummyPreparedStmt pstmt = new DummyPreparedStmt();
                    try {
                        cExp.col.converter.set(pstmt, 0, cExp.col,
                                new Boolean(value));
                    } catch (SQLException e) {
                        //ignore
                    }
                    value = pstmt.value;
                    if (pstmt.toQuote) {
                        t = LiteralExp.TYPE_STRING;
                    } else {
                        t = LiteralExp.TYPE_OTHER;
                    }
                }
                break;
            case LiteralNode.TYPE_CHAR:
            case LiteralNode.TYPE_DOUBLE:
            case LiteralNode.TYPE_LONG:     t = LiteralExp.TYPE_OTHER;      break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                    "Unknown literal type: " + node.type);
        }

        LiteralExp ans = new LiteralExp(t, value);
        if (node.type == LiteralNode.TYPE_NULL) {
            // Create a list of LiteralExp's with one entry for each expression
            // on the left hand side. This is to handle references to composite
            // pk classes.
            for (SqlExp pos = ans; (leftSibling = leftSibling.getNext()) != null; ) {
                pos = pos.setNext(new LiteralExp(t, value));
            }
        }
        return ans;
    }

    public Object arriveMethodNode(MethodNode node, Object msg) {
        Context ctx = (Context)msg;
        SelectExp root = ctx.root;
        int method = node.getMethod();
        Node childList = node.childList;
        switch (method) {
            case MethodNode.STARTS_WITH:   return toSqlLike(childList, method, root, false);
            case MethodNode.ENDS_WITH:     return toSqlLike(childList, method, root, true);
            case MethodNode.CONTAINS:
            case MethodNode.CONTAINS_KEY:  return toSqlContains(childList, method, root);
            case MethodNode.IS_EMPTY:      return toSqlIsEmpty(childList, root);
            case MethodNode.TO_LOWER_CASE: return toSqlToLowerCase(childList, method, root);
            case MethodNode.SQL:           return toSqlInline(childList, method, root);
        }
        throw BindingSupportImpl.getInstance().internal("Unknown method: " + method);
    }

    private SqlExp toSqlInline(Node childList, int method, SelectExp root) {
        SqlExp left = toSqlExp(childList, root, null, method, null);
        SqlExp right;
        if (childList.next != null) {
            right = toSqlExp(childList.next, root, left, method, null);
        } else {
            right = null;
        }
        String template;
        if (right == null) {
            template = "$1";
        } else if (right instanceof LiteralExp) {
            template = ((LiteralExp)right).value;
        } else {
            throw BindingSupportImpl.getInstance().invalidOperation("Expected literal expression: " + right);
        }
        return new InlineSqlExp(template, left);
    }

    private SqlExp toSqlLike(Node childList, int method, SelectExp root,
            boolean endsWith) {
        SqlExp left = toSqlExp(childList, root, null, method, null);
        SqlExp right = toSqlExp(childList.next, root, left, method, null);
        if (right instanceof LiteralExp
                && ((LiteralExp)right).type == LiteralExp.TYPE_STRING) {
            // prepend or append a % to the literal string
            LiteralExp le = (LiteralExp)right;
            if (endsWith) le.value = "%" + le.value;
            else le.value = le.value + "%";
        } else {
            // create a BinaryExp to prepend or append %
            LiteralExp pe = new LiteralExp(LiteralExp.TYPE_STRING, "%");
            if (endsWith) right = new BinaryOpExp(pe, BinaryOpExp.CONCAT, right);
            else right = new BinaryOpExp(right, BinaryOpExp.CONCAT, pe);
        }
        return new BinaryOpExp(left, BinaryOpExp.LIKE, right);
    }

    private SqlExp toSqlIsEmpty(Node childList, SelectExp root) {
        if (childList.next != null) {
            throw BindingSupportImpl.getInstance().runtime(
                    "isEmpty() does not accept arguments");
        }
        return toSqlExp(childList, root, null, MethodNode.IS_EMPTY, null);
    }

    private SqlExp toSqlContains(Node childList, int method, SelectExp root) {
        return toSqlExp(childList, root, null, method, childList.next);
    }

    private SqlExp toSqlToLowerCase(Node childList, int method, SelectExp root) {
        if (childList.next != null) {
            throw BindingSupportImpl.getInstance().runtime(
                    "toLowerCase() does not accept arguments");
        }
        SqlExp left = toSqlExp(childList, root, null, method, null);
        return new UnaryFunctionExp(left, UnaryFunctionExp.FUNC_TO_LOWER_CASE);
    }

    public Object arriveFieldNode(FieldNode node, Object msg) {
        Context ctx = (Context)msg;
        int method = ctx.method;
        SelectExp root = ctx.root;
        Node args = ctx.args;
        JdbcField jdbcField = (JdbcField)node.fmd.storeField;

        switch (method) {
            case MethodNode.IS_EMPTY:
                return jdbcField.toIsEmptySqlExp(comp, root);
            case MethodNode.CONTAINS:
                return jdbcField.toContainsSqlExp(comp, root, args);
            case MethodNode.CONTAINS_KEY:
                return jdbcField.toContainsKeySqlExp(comp, root, args);
            case MethodNode.CONTAINS_PARAM:
                if (jdbcField.mainTableCols.length > 1) {
                    throw BindingSupportImpl.getInstance().invalidOperation(
                            "This is only supported for single column instances");
                }
                return new CollectionParamExp(jdbcField.toColumnExp(
                        SelectExp.createJoinToSuperTable(root, jdbcField), true));
        };

        // This is to detect the scenario where a boolean field is in the jdoql
        // expression on its own. eg 'booleanField && name == param'
        if (isUnaryBoolExp(node, jdbcField)) {
            ColumnExp cExp = jdbcField.toColumnExp(root, true);

            LiteralExp lExp = new LiteralExp();
            if (cExp.col.converter != null) {
                DummyPreparedStmt pstmt = new DummyPreparedStmt();
                try {
                    cExp.col.converter.set(pstmt, 0, cExp.col, new Boolean(true));
                } catch (SQLException e) {
                    //ignore
                }

                lExp.value = pstmt.value;
                if (pstmt.toQuote) {
                    lExp.type = LiteralExp.TYPE_STRING;
                } else {
                    lExp.type = LiteralExp.TYPE_OTHER;
                }
                return new BinaryOpExp(cExp, BinaryOpExp.EQUAL, lExp);
            } else {
                lExp.type = LiteralExp.TYPE_OTHER;
                lExp.value = "true";
                return new BinaryOpExp(cExp, BinaryOpExp.EQUAL, lExp);
            }
        }

        SelectExp fSe = node.useCandidateExtent ? comp.getCandidateSelectExp() : root;
        ColumnExp columnExp = jdbcField.toColumnExp(SelectExp.createJoinToSuperTable(fSe, jdbcField), true);
        if (!isPartOfAggregate(node.parent)) columnExp.setColAlias(node.asValue);
        return columnExp;
    }

    private static boolean isUnaryBoolExp(Node node, JdbcField jdbcField) {
        if (isBooleanType(jdbcField) && !(node.parent instanceof OrderNode)) {
            if (node.parent instanceof FieldNavNode) {
                return isUnaryBoolExp(node.parent, jdbcField);
            } else if ((node.parent instanceof AndNode)
                    || (node.parent instanceof OrNode)
                    || (node.parent != null && node.parent.parent == null)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBooleanType(JdbcField jdbcField) {
        if (jdbcField == null) return false;
        return ((jdbcField.fmd.typeCode == MDStatics.BOOLEAN)
                        || (jdbcField.fmd.typeCode == MDStatics.BOOLEANW));
    }

    private static boolean isPartOfAggregate(Node n) {
        if (n == null) return false;
        if (n instanceof AggregateNode) return true;
        return isPartOfAggregate(n.parent);
    }

    public Object arriveEqualNode(EqualNode node, Object msg) {
        return arriveEqualOrNotEqual(node, msg, BinaryOpExp.EQUAL);
    }

    public Object arriveLikeNode(LikeNode node, Object msg) {
        return arriveEqualOrNotEqual(node, msg, BinaryOpExp.LIKE);
    }

    private Object arriveEqualOrNotEqual(EqualNode node, Object msg, int op) {
        Context ctx = (Context)msg;
        SelectExp root = ctx.root;

        // If a field is being compared to a null literal then only put
        // columns that are not shared in the left expression unless all of
        // the columns are shared.
        SqlExp left;
        Node childList = node.childList;
        if (isNullLiteral(childList.next) && childList instanceof FieldNode) {
            JdbcField f = (JdbcField)((FieldNode)childList).fmd.storeField;
            left = f.toColumnExpForNullLiteralCompare(root);
        } else {
            left = toSqlExp(childList, root, null, ctx.method, ctx.args);
        }

        SqlExp right = toSqlExp(childList.next, root, left, ctx.method, ctx.args);
        return SqlExp.createBinaryOpExp(left, op, right);
    }

    private static boolean isNullLiteral(Node n) {
        return n instanceof LiteralNode
            && ((LiteralNode)n).type == LiteralNode.TYPE_NULL;
    }

    public Object arriveNotEqualNode(NotEqualNode node, Object msg) {
        return arriveEqualOrNotEqual(node, msg, BinaryOpExp.NOT_EQUAL);
    }

    public Object arriveAndNode(AndNode node, Object msg) {
        Context ctx = (Context)msg;
        Node childList = node.childList;

        // Build the list of expressions to be anded together.
        SqlExp prev = null, first = null;
        for (Node cn = childList; cn != null;) {
            SqlExp e = toSqlExp(cn, ctx.root, prev, ctx.method, ctx.args);
            if (first == null) {
                first = e;
            } else if ( prev != null ) {
                prev.setNext(e);
            }
            prev = e;

            // See if this expression involves a VarNode. If so process the
            // nodes involving the variable. They become the where clause of
            // the sub select for the variable.
            VarNode vn = extractVar(e);
            if (vn != null) {
                cn = processVarNode(ctx.root, ctx.method, ctx.args, cn, vn, e);
            } else {
                cn = cn.next;
            }
        }

        // if there is now only one entry in the list then return it otherwise
        // create a new AndExp for the list
        if (first.getNext() == null) {
            return first;
        } else {
            return new AndExp(first);
        }
    }

    private static VarNode extractVar(SqlExp e) {
        if (e instanceof ExistsExp && e.childList instanceof SelectExp) {
            return ((SelectExp) e.childList).var;
        } else {
            return null;
        }
    }

    /**
     * Process the nodes involving the variable. They become the where clause of
     * of the sub select for the variable. This process stops as soon as a node
     * is encountered that does not involve the variable i.e. all the variable
     * related expressions must be together (as per spec).
     *
     * @return Next Node for the main loop to process
     */
    private Node processVarNode(SelectExp root, int method, Node args,
            Node cn, VarNode var, SqlExp vare) {

        SelectExp varSelectExp = (SelectExp)var.getStoreExtent();

        // Do not use columns from the enclosing query (root) in subquery ON
        // join conditions if the database does not allow this. If exclude is
        // null then expressions referencing tables in the subquery and
        // then enclosing query (root) will be left in the where clause.
        // This is required for DB2.
        SelectExp exclude;
        if (root.table.sqlDriver.isSubQueryJoinMayUseOuterQueryCols()) {
            exclude = root;
        } else {
            exclude = null;
        }

        // build a list of all expressions constraining the variable on first
        SqlExp prev = null, first = null;
        SqlExp leftSibling = vare;
        Node ans = cn.next;
        for (; ans != null;) {
            SqlExp e = toSqlExp(ans, root, leftSibling, method, args);
            if (e == null) {
                break;
            }

            SelectExp single = e.getSingleSelectExp(exclude);
            if (single != null) {
                Join join;
                if (single.jdbcField != null) {
                    join = varSelectExp.findJoinRec(single.jdbcField);
                } else if (vare instanceof ExistsExp) {
                    join = ((SelectExp) vare.childList).findJoin(single);
                } else {
                    join = null;
                }
                ans = ans.next;
                if (join != null) {
                    join.appendJoinExp(e);
                    continue;
                } else if (var.getCmd() == null) {
                   ((SelectExp) ((ExistsExp)vare).childList).appendToWhereExp(e);
                }
            } else {
                // recursively process nested variables
                VarNode nested = extractVar(e);
                if (nested != null) {
                    ans = processVarNode(root, method, args, ans, nested, e);
                } else {
                    ans = ans.next;
                }
            }
            if (first == null) {
                first = e;
            } else {
                prev.setNext(e);
            }
            leftSibling = e;
            prev = e;
        }

        // attach the expression list at first to the where of the variable
        if (first != null) varSelectExp.appendToWhereExp(first);

        return ans;
    }

    public Object arriveOrNode(OrNode node, Object msg) {
        Context ctx = (Context)msg;
        Node childList = node.childList;

        SqlExp e = toExtentExp(ctx.root, childList, ctx.args, false);
        if (e == null) {
            return null;
        }

        SqlExp base = e;
        for (Node c = childList.next; c != null; c = c.next) {
            e = e.setNext(toExtentExp(ctx.root, c, ctx.args, false));
        }
        mergeOrNodeRedundantExistsSelects(base);
        if (base.getNext() == null) return base;
        return new OrExp(base);
    }

    public Object arriveUnaryOpNode(UnaryOpNode node, Object msg) {
        Context ctx = (Context)msg;
        /**
         * Convert this into an 'EXISTS' expression if there is any field
         * navigation to a reference and it is a '!' operation. This done because
         * we need to negate the whole expression including the joins. If not then
         * rows that does not have the a valid reference will also be 'FALSE' because
         * a NULL value is always 'FALSE'
         */
        SqlExp ne;
        if (node.op == UnaryOpNode.OP_BANG /*&& node.childList.childList instanceof FieldNavNode*/) {
            ne = toExtentExp(ctx.root, node.childList, ctx.args, true);
        } else {
            ne = toSqlExp(node.childList, ctx.root, ctx.leftSibling, ctx.method,
                    ctx.args);
        }

        return new UnaryOpExp(ne, node.op);
    }

    private SqlExp toExtentExp(SelectExp root, Node c, Node args,
            boolean nonConvertable) {
        Join rootJoinList = root.joinList;
        root.joinList = null;
        SqlExp e = toSqlExp(c, root, null, 0, args);
        // if the child expression includes a join (e.g. field navigation)
        // then convert it into an ExistsSelectExp
        Join j = root.joinList;
        if (j != null) {
            // convert the first join into a ExistsSelectExp
            SelectExp se = j.selectExp;
            se.subSelectJoinExp = j.exp;
            se.whereExp = e;
            // make the other joins (if any) joins on this select
            j = se.joinList;
            if (j == null) {
                se.joinList = root.joinList.next;
            } else {
                for (; j.next != null; j = j.next);
                j.next = root.joinList.next;
            }
            // wrap in an exists exp
            e = new ExistsExp(se, nonConvertable);
        }
        root.joinList = rootJoinList;
        return e;
    }

    /**
     * Try to merge the list of expressions starting at base. This gets rid
     * of redundant 'exists (select ...)' sub queries.
     */
    private void mergeOrNodeRedundantExistsSelects(SqlExp base) {
        for (; base != null; ) {
            if (base instanceof ExistsExp) {
                SqlExp prev = base;
                for (SqlExp target = base.getNext(); target != null; ) {
                    if (mergeOrNodeRedundantExistsSelects(base, target)) {
                        target = prev.setNext(target.getNext());
                    } else {
                        prev = target;
                        target = target.getNext();
                    }
                }
            }
            base = base.getNext();
        }
    }

    /**
     * Merge base and e if possible or return false. The expressions can
     * be merged if:<p>
     * <ol>
     * <li>e is an ExistsExp.
     * <li>The selectExp's have the same jdbcField.
     * <li>The selectExp's do not have any other joins.
     * </ol>
     */
    private boolean mergeOrNodeRedundantExistsSelects(SqlExp base, SqlExp e) {
        if (!(e instanceof ExistsExp)) return false;
        SelectExp se = (SelectExp)base.childList;
        SelectExp ese = (SelectExp)e.childList;
        if (se.jdbcField != ese.jdbcField) return false;
        if (!isEqualJoinExp(se.subSelectJoinExp, ese.subSelectJoinExp)) return false;
        if (!Join.isEqaul(se.joinList, ese.joinList)) return false;

        SqlExp we = se.whereExp;
        if (we instanceof OrExp) { // extend the existing OrExp
            SqlExp pos;
            for (pos = we.childList; pos.getNext() != null; pos = pos.getNext());
            pos.setNext(ese.whereExp);
        } else if ( se.whereExp != null ) {    // create an OrExp
            se.whereExp.setNext(ese.whereExp);
            se.whereExp = new OrExp(se.whereExp);
        }
        // convert any references to ese into se references
        if ( ese.whereExp != null )
            ese.whereExp.replaceSelectExpRef(ese, se);
        return true;
    }

    /**
     * True if both are '==' or both of type JoinExp and JoinExp.isEqual()
     * @param se1
     * @param se2
     */
    private static boolean isEqualJoinExp(SqlExp se1, SqlExp se2) {
        if (se1 == se2) return true;
        if (se1 == null) return false;
        if (se2 == null) return false;

        if (se1 instanceof JoinExp && se2 instanceof JoinExp) {
            return JoinExp.isEqual((JoinExp) se1, (JoinExp) se2);
        }
        return false;
    }

    public Object arriveMultiplyNode(MultiplyNode node, Object msg) {
        Context ctx = (Context)msg;
        Node childList = node.childList;

        SqlExp e = processMultiplyNodeChild(ctx.root, childList, null, ctx.args);
        MultiplyExp ans = new MultiplyExp(e, node.ops);
        for (Node c = childList.next; c != null; c = c.next) {
            e = e.setNext(processMultiplyNodeChild(ctx.root, c, e, ctx.args));
        }
        return ans;
    }

    private SqlExp processMultiplyNodeChild(SelectExp root, Node c,
            SqlExp leftSibling, Node args) {
        SqlExp e = toSqlExp(c, root, leftSibling, 0, args);
        if (e.getNext() != null) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be used" +
                "with * or /");
        }
        return e;
    }

    public Object arriveAddNode(AddNode node, Object msg) {
        Context ctx = (Context)msg;
        Node childList = node.childList;
        SqlExp e = processAddNodeChild(ctx.root, childList, null, ctx.args);
        AddExp ans = new AddExp(e, node.ops);
        if (node.asValue != null) ans.setExpAlias(node.asValue);
        for (Node c = childList.next; c != null; c = c.next) {
            e = e.setNext(processAddNodeChild(ctx.root, c, e, ctx.args));
        }
        return ans;
    }

    private SqlExp processAddNodeChild(SelectExp root, Node c, SqlExp leftSibling,
            Node args) {
        SqlExp e = toSqlExp(c, root, leftSibling, 0, args);
        if (e.getNext() != null) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be used" +
                "with + or -");
        }
        return e;
    }

    public Object arriveCompareOpNode(CompareOpNode node, Object msg) {
        Context ctx = (Context)msg;
        Node childList = node.childList;
        SqlExp left = toSqlExp(childList, ctx.root, null, ctx.method, ctx.args);
        SqlExp right = toSqlExp(childList.next, ctx.root, left, ctx.method, ctx.args);
        if (left.getNext() == null && right.getNext() == null) {
            int op;
            switch (node.op) {
                case CompareOpNode.GT:    op = BinaryOpExp.GT;  break;
                case CompareOpNode.LT:    op = BinaryOpExp.LT;  break;
                case CompareOpNode.GE:    op = BinaryOpExp.GE;  break;
                case CompareOpNode.LE:    op = BinaryOpExp.LE;  break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "Unknown op: " + node.op);
            }
            return new BinaryOpExp(left, op, right);
        }
        throw BindingSupportImpl.getInstance().runtime(
            "Expressions consisting of multiple columns may not be compared " +
            "with >, <, >= or <=\n");
    }

    public Object arriveUnaryNode(UnaryNode node, Object msg) {
        Context ctx = (Context)msg;
        return toSqlExp(node.childList, ctx.root, ctx.leftSibling,
                ctx.method, ctx.args);
    }

    public Object arriveCastNode(CastNode node, Object msg) {
        Context ctx = (Context)msg;
        return toSqlExp(node.childList, ctx.root, ctx.leftSibling,
                ctx.method, ctx.args);
    }

    /**
     * Add u to the end of the usage list for n.
     */
    private void addToParamNode(SqlParamUsage u, ParamNode n) {
        if (n.usageList == null) {
            n.usageList = u;
        } else {
            SqlParamUsage p = (SqlParamUsage)n.usageList;
            for (; p.next != null; p = p.next);
            p.next = u;
        }
    }

    public Object arriveParamNode(ParamNode node, Object msg) {
        Context ctx = (Context)msg;
        SqlExp leftSibling = ctx.leftSibling;
        SelectExp root = ctx.root;
        Node args = ctx.args;

        SqlParamUsage u = new SqlParamUsage();
        addToParamNode(u, node);
        if (leftSibling instanceof ColumnExp) {
            ColumnExp left = (ColumnExp)leftSibling;
            u.jdbcField = left.jdbcField;
            u.col = left.col;
            SqlExp pos = u.expList = new ParamExp(u.jdbcType, u);
            for (;;) {
                if ((left = (ColumnExp)left.getNext()) == null) break;
                pos = pos.setNext(new ParamExp(left.getJdbcType(), u));
            }
        } else {
            if (leftSibling == null) {
                /**
                 * Expect this to be a param collection exp
                 * The args must be either a FieldNode of a FieldNavNode.
                 */
                Class colClass = /*CHFC*/java.util.Collection.class/*RIGHTPAR*/;
                if (colClass.isAssignableFrom(
                            MDStaticUtils.toSimpleClass(node.getType()))
                        && (args instanceof FieldNode
                        || args instanceof FieldNavNode)
                        || args instanceof VarNodeIF) {
                    CollectionParamExp collectionParamExp =
                        (CollectionParamExp)toSqlExp(args,
                            root, leftSibling, MethodNode.CONTAINS_PARAM, null);
                    u.expList = collectionParamExp;
                    u.jdbcField = collectionParamExp.field.jdbcField;
                    u.col = collectionParamExp.field.col;
                    u.jdbcType = collectionParamExp.field.getJdbcType();
                    u.javaTypeCode = collectionParamExp.field.getJavaTypeCode();
                    u.classIndex = collectionParamExp.field.getClassIndex();
                    return collectionParamExp;
                }
                throw BindingSupportImpl.getInstance().internal(
                    "not implemented (leftSibling == null)");
            }
            if (leftSibling.getNext() != null) {
                throw BindingSupportImpl.getInstance().internal(
                    "not implemented (leftSibling.next != null)");
            }
            u.expList = new ParamExp(leftSibling.getJdbcType(), u);
        }
        if (u.jdbcField == null) {
            u.jdbcType = leftSibling.getJdbcType();
            u.javaTypeCode = leftSibling.getJavaTypeCode();
            u.classIndex = leftSibling.getClassIndex();
        }
        return u.expList;
    }

    public Object arriveParamNodeProxy(ParamNodeProxy node, Object msg) {
        Context ctx = (Context)msg;
        return toSqlExp(node.getParamNode(), ctx.root, ctx.leftSibling,
                ctx.method, ctx.args);
    }

    public Object arriveVarNode(VarNode node, Object msg) {
        // return ColumnExp's for the primary key of our table
        if (node.getCmd() != null) {
            SelectExp jdbcSelectExp = (SelectExp)node.getStoreExtent();
            JdbcColumn[] cols = jdbcSelectExp.table.pk;
            ColumnExp ans = new ColumnExp(cols[0], jdbcSelectExp, null);
            ans.cmd = node.getCmd();
            SqlExp e = ans;
            int nc = cols.length;
            for (int i = 1; i < nc; i++) {
                e = e.setNext(new ColumnExp(cols[i], jdbcSelectExp, null));
            }

            if (((Context)msg).method == MethodNode.CONTAINS_PARAM) {
                if (cols.length > 1) {
                    throw BindingSupportImpl.getInstance().invalidOperation(
                            "This is only supported for single column instances");
                }
                return new CollectionParamExp(ans);
            }
            return ans;
        } else {
            JdbcLinkCollectionField jdbcField =
                    (JdbcLinkCollectionField)node.getFmd().storeField;
            return new ColumnExp(jdbcField.valueColumns[0],
                    (SelectExp)node.getFieldExtent(), null);
        }
    }

    public Object arriveVarNodeProxy(VarNodeProxy node, Object msg) {
        Context ctx = (Context)msg;
        return toSqlExp(node.getVarNode(), ctx.root, ctx.leftSibling,
                ctx.method, ctx.args);
    }

    public Object arriveReservedFieldNode(ReservedFieldNode node, Object msg) {
        ClassMetaData target = node.getTarget();
        SelectExp candidateSelectExp = comp.getCandidateSelectExp();
        JdbcColumn[] pk = ((JdbcClass)target.storeClass).table.pk;
        ColumnExp list = new ColumnExp(pk[0], candidateSelectExp, null);
        list.cmd = target;
        SqlExp e = list;
        int nc = pk.length;
        for (int i = 1; i < nc; i++) {
            e = e.setNext(new ColumnExp(pk[i], candidateSelectExp, null));
        }
        return list;
    }

    public Object arriveAggregateCountStarNode(AggregateCountStarNode node,
            Object msg) {
        return new AggregateCountStarExp(node.asValue);
    }

    public Object arriveAggregateNode(AggregateNode node, Object msg) {
        Context ctx = (Context)msg;
        SqlExp se = toSqlExp(node.childList, ctx.root, ctx.leftSibling,
                ctx.method, ctx.args);
        String op;
        switch (node.getType()) {
            case AggregateNode.TYPE_AVG:
                op = "AVG";
                break;
            case AggregateNode.TYPE_COUNT:
                op = "COUNT";
                break;
            case AggregateNode.TYPE_MAX:
                op = "MAX";
                break;
            case AggregateNode.TYPE_MIN:
                op = "MIN";
                break;
            case AggregateNode.TYPE_SUM:
                op = "SUM";
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Uknown AggregateNode type " + node.getType());
        };
        return new AggregateExp(se, op, node.asValue);
    }

    public Object arriveAsValueNode(AsValueNode node, Object msg) {
        return new ColumnExp(node.value);
    }

    public Object arriveGroupingNode(GroupingNode node, Object msg) {
        Context ctx = (Context)msg;
        SqlExp head = new SqlExp();
        SqlExp current = head;
        for (Node n = node.childList; n != null; n = n.next) {
            if (n instanceof ReservedFieldNode) {
                current.setNext(new GroupByThisExp());
                current = current.getNext();
            } else {
                current.setNext(toSqlExp(n, ctx.root, ctx.leftSibling,
                        ctx.method, ctx.args));
                current = current.getNext();
            }
        }
        return head.getNext();
    }

    public Object arriveResultNode(ResultNode node, Object msg) {
        Context ctx = (Context)msg;
        SqlExp head = new SqlExp();
        SqlExp current = head;
        for (Node n = node.childList; n != null; n = n.next) {
            if (n instanceof ReservedFieldNode) continue;
            current.setNext(toSqlExp(n, ctx.root, ctx.leftSibling, ctx.method,
                    ctx.args));
            current = current.getNext();
        }
        return head.getNext();
    }

    public Object arriveVarBindingNode(VarBindingNode node, Object msg) {
        SelectExp se = (SelectExp)node.getVar().getStoreExtent();
        return new ExistsExp(se, true);
    }

}

