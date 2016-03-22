
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

import com.versant.core.ejb.query.*;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcPolyRefField;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;

import java.util.ArrayList;

/**
 * Walks EJBQL Node trees to produce SqlExp trees.
 */
public class EJBQLNodeToSqlExp extends NodeVisitorAdapter {

    private ResolveContext rc;
    private SqlDriver sqlDriver;

    private static final Object[] EMPTY = new Object[0];

    public EJBQLNodeToSqlExp(ResolveContext rc, SqlDriver sqlDriver) {
        this.rc = rc;
        this.sqlDriver = sqlDriver;
    }

    /**
     * Throw exception if we hit any unhandled nodes.
     */
    protected Object defaultArrive(Node node, Object msg) {
        throw rc.createUserException("Not implemented: " + node.getClass(),
                node);
    }

    /**
     * Convert resolved Node tree to a SqlExp tree. Returns nul if node is null.
     */
    public SqlExp toSqlExp(Node node, Object msg) {
        if (node == null) {
            return null;
        } else {
            return (SqlExp)node.arrive(this, msg);
        }
    }

    private RuntimeException notImplemented() {
        return notImplemented("not implemented");
    }

    private RuntimeException notImplemented(String msg) {
        return BindingSupportImpl.getInstance().internal(msg);
    }

    /**
     * Invoke arrive on each entry in the list. Returns Object[0] if list is
     * null.
     */
    private Object[] invokeArriveOnList(Node list, Object msg) {
        if (list == null) {
            return EMPTY;
        }
        if (list.getNext() == null) {   // 1 node only
            return new Object[]{toSqlExp(list, msg)};
        } else {
            ArrayList a = new ArrayList();
            for (; list != null; list = list.getNext()) {
                a.add(toSqlExp(list, msg));
            }
            return a.toArray();
        }
    }

    /**
     * SELECT .. FROM .. WHERE .. GROUP BY .. HAVING .. ORDER BY expression
     * that is not a subquery.
     */
    public Object arriveSelectNode(SelectNode node, Object msg) {
        Object[] fromList = invokeArriveOnList(node.getFromList(), msg);
        SelectExp se = (SelectExp)fromList[0];
        if (fromList.length > 1) {
            // todo combine into a single SelextExp
            throw notImplemented();
        }
        se.fetchSpec = new FetchSpec(se, sqlDriver);
        invokeArriveOnList(node.getSelectList(), msg);
        se.whereExp = toSqlExp(node.getWhere(), msg);
        return se;
    }

    /**
     * Declaration of identification variable in the FROM list e.g.
     * 'FROM Order AS o, Product AS p'.
     */
    public Object arriveIdentificationVarNode(IdentificationVarNode node,
            Object msg) {
        // create SelectExp's linked with joins for whole nav tree
        NavRoot root = node.getNavRoot();
        ClassMetaData cmd = root.getNavClassMetaData();
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        SelectExp se = new SelectExp();
        se.table = jdbcClass.table;
        root.storeObject = se;
        return se;
    }

    /**
     * Path (e.g. o.customer.name, o).
     */
    public Object arrivePathNode(PathNode node, Object msg) {
        switch (node.getParentType()) {
            case PathNode.SELECT:
                return pathNodeSelect(node);
            case PathNode.WHERE:
                return pathNodeWhere(node);
            case PathNode.GROUP_BY:
            case PathNode.ORDER_BY:
            case PathNode.AGGREGATE:
            case PathNode.CONSTRUCTOR:
            case PathNode.JOIN:
            case PathNode.COLLECTION_MEMBER:
        }
        return super.arrivePathNode(node, msg);
    }

    private Object pathNodeSelect(PathNode node) {
        FieldMetaData fmd = node.getFmd();
        if (fmd == null) {  // complete object
            return arriveObjectNodeImp(node.getNavBase());
        } else {    // single field
            throw notImplemented();
        }
    }

    public Object arriveObjectNode(ObjectNode node, Object msg) {
        return arriveObjectNodeImp(node.getNavBase());
    }

    private Object arriveObjectNodeImp(NavBase navBase) {
        SelectExp se = (SelectExp)navBase.storeObject;
        FetchSpec fetchSpec = se.fetchSpec;
        ClassMetaData cmd = navBase.getNavClassMetaData();
        FopGetOID fopGetOid = new FopGetOID(fetchSpec,
                FetchOpDataMainRS.INSTANCE, cmd, se);
        FopGetState fopGetState = new FopGetState(fetchSpec,
                fopGetOid.getOutputData(), cmd.fetchGroups[0], true, se, 0, cmd, new FetchFieldPath());
        fetchSpec.addFetchOp(fopGetOid, true);
        fetchSpec.addFetchOp(fopGetState, true);
        // one of the above lines must change to false when we figure out
        // how to link the State to the OID cleanly (OID field on State??)
        return se;
    }

    /**
     * Get the SelectExp for node. This will create it and any joins required
     * if it does not already exist. This actually creates all of the joins
     * for the nodes NavRoot.
     */
    private SelectExp getNodeSelectExp(PathNode node) {
        NavBase nav = node.getNavBase();
        SelectExp se = (SelectExp)nav.storeObject;
        if (se == null) {
            createJoins(nav.getRoot());
            se = (SelectExp)nav.storeObject;
            if (Debug.DEBUG) {
                if (se == null) {
                    throw BindingSupportImpl.getInstance().internal(
                            "nav.storeObject == null: " + node);
                }
            }
        }
        return se;
    }

    /**
     * Create all the joins for a tree of navigations starting at navRoot.
     */
    private void createJoins(NavBase from) {
        SelectExp fromSE = (SelectExp)from.storeObject;
        for (NavField to = from.getChildren(); to != null; to = to.getNext()) {
            if (to.storeObject != null) {
                continue;
            }
            JdbcField jdbcField = (JdbcField)to.getFmd().storeField;
            JdbcClass targetClass = (JdbcClass)to.getNavClassMetaData().storeClass;
            SelectExp se = new SelectExp();
            se.table = targetClass.table;
            se.jdbcField = jdbcField;
            if (jdbcField instanceof JdbcPolyRefField) {
                JdbcPolyRefField pf = (JdbcPolyRefField)jdbcField;
                Join j = fromSE.addJoin(pf.refCols, se.table.pk, se);
                // add a condition to check that the class-id column of
                // the polyref matches the type used in the cast
                j.appendJoinExp(
                        pf.createClassIdMatchExp(fromSE, targetClass.cmd));
            } else {
                fromSE.addJoin(jdbcField.mainTableCols, se.table.pk, se);
            }
            se.outer = to.isOuter();
            to.storeObject = se;
            createJoins(to);
        }
    }

    private Object pathNodeWhere(PathNode node) {
        FieldMetaData fmd = node.getFmd();
        if (fmd != null) {
            JdbcField jdbcField = (JdbcField)fmd.storeField;
            SelectExp se = getNodeSelectExp(node);
            ColumnExp columnExp = jdbcField.toColumnExp(
                    SelectExp.createJoinToSuperTable(se, jdbcField), true);
            return columnExp;
        } else {
            throw notImplemented();
        }
    }

    public Object arriveCompNode(CompNode node, Object msg) {
        SqlExp left = toSqlExp(node.getLeft(), msg);
        SqlExp right = toSqlExp(node.getRight(), left);
        int op = node.getOp();
        if (op == CompNode.EQ || op == CompNode.NE) {
            if (op == CompNode.EQ) {
                op = BinaryOpExp.EQUAL;
            } else {
                op = BinaryOpExp.NOT_EQUAL;
            }
            return SqlExp.createBinaryOpExp(left, op, right);
        } else {
            if (left.getNext() == null && right.getNext() == null) {
                switch (op) {
                    case CompNode.GT:    op = BinaryOpExp.GT;  break;
                    case CompNode.LT:    op = BinaryOpExp.LT;  break;
                    case CompNode.GE:    op = BinaryOpExp.GE;  break;
                    case CompNode.LE:    op = BinaryOpExp.LE;  break;
                    default:
                        throw BindingSupportImpl.getInstance().internal(
                                "Unknown op: " + op);
                }
                return new BinaryOpExp(left, op, right);
            }
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be compared " +
                "with >, <, >= or <=\n");
        }
    }

    public Object arriveLiteralNode(LiteralNode node, Object msg) {
        String value;
        int t;
        switch (node.getType()) {
            case LiteralNode.DOUBLE:
                t = LiteralExp.TYPE_OTHER;
                value = sqlDriver.toSqlLiteral(node.getDoubleValue());
                break;
            case LiteralNode.LONG:
                t = LiteralExp.TYPE_OTHER;
                value = sqlDriver.toSqlLiteral(node.getLongValue());
                break;
            case LiteralNode.STRING:
                t = LiteralExp.TYPE_STRING;
                value = node.getStringValue();
                break;
            case LiteralNode.BOOLEAN:
                t = LiteralExp.TYPE_OTHER;
                value = sqlDriver.toSqlLiteral(node.getBooleanValue());
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                    "Unknown literal type: " + node.getType());
        }
        return new LiteralExp(t, value);
    }

    public Object arriveParameterNode(ParameterNode node, Object msg) {
        if (msg != null && !(msg instanceof SqlExp)) {
            throw BindingSupportImpl.getInstance().internal(
                    "expected SqlExp for msg");
        }
        SqlExp leftSibling = (SqlExp)msg;
        SqlParamUsage u = new SqlParamUsage();
        node.getUsage().storeObject = u;
        if (leftSibling instanceof ColumnExp) {
            ColumnExp left = (ColumnExp)leftSibling;
            u.jdbcField = left.jdbcField;
            u.col = left.col;
            // Create extra ? in the SQL if our left sibling has multiple
            // columns. Example: a reference to a composite pk class
            // compared to a parameter.
            SqlExp pos = u.expList = new ParamExp(u.jdbcType, u);
            for (;;) {
                if ((left = (ColumnExp)left.getNext()) == null) break;
                pos = pos.setNext(new ParamExp(left.getJdbcType(), u));
            }
        } else {
            if (leftSibling.getNext() != null) {
                throw BindingSupportImpl.getInstance().internal(
                    "Expression on left has more than one column and no " +
                    "field information");
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

    public Object arriveAndNode(AndNode node, Object msg) {
        // Build the list of expressions to be AND'ed together.
        SqlExp prev = null, first = null;
        for (Node cn = node.getArgsList(); cn != null; cn = cn.getNext()) {
            SqlExp e = toSqlExp(cn, prev);
            if (first == null) {
                first = e;
            } else if ( prev != null ) {
                prev.setNext(e);
            }
            prev = e;
        }

        // if there is now only one entry in the list then return it otherwise
        // create a new AndExp for the list
        if (first.getNext() == null) {
            return first;
        } else {
            return new AndExp(first);
        }
    }

    public Object arriveOrNode(OrNode node, Object msg) {
        // Build the list of expressions to be OR'ed together.
        SqlExp prev = null, first = null;
        for (Node cn = node.getArgsList(); cn != null; cn = cn.getNext()) {
            SqlExp e = toSqlExp(cn, prev);
            if (first == null) {
                first = e;
            } else if ( prev != null ) {
                prev.setNext(e);
            }
            prev = e;
        }

        // if there is now only one entry in the list then return it otherwise
        // create a new OrExp for the list
        if (first.getNext() == null) {
            return first;
        } else {
            return new OrExp(first);
        }
    }

    public Object arriveAddNode(AddNode node, Object msg) {
        SqlExp left = processAddNodeChild(node.getLeft());
        SqlExp right = processAddNodeChild(node.getRight());
        left.setNext(right);
        return new AddExp(left, new int[]{node.getOp()});
    }

    private SqlExp processAddNodeChild(Node c) {
        SqlExp e = toSqlExp(c, null);
        if (e.getNext() != null) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be used" +
                "with + or -");
        }
        return e;
    }

    public Object arriveMultiplyNode(MultiplyNode node, Object msg) {
        SqlExp left = processMultiplyNodeChild(node.getLeft());
        SqlExp right = processMultiplyNodeChild(node.getLeft());
        left.setNext(right);
        return new AddExp(left, new int[]{node.getOp()});
    }

    private SqlExp processMultiplyNodeChild(Node c) {
        SqlExp e = toSqlExp(c, null);
        if (e.getNext() != null) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be used" +
                "with * or /");
        }
        return e;
    }

    public Object arriveParenNode(ParenNode node, Object msg) {
        SqlExp e = toSqlExp(node, msg);
        if (e instanceof ParenExp) {
            return e;
        } else {
            return new ParenExp(e);
        }
    }

    public Object arriveBetweenNode(BetweenNode node, Object msg) {
        SqlExp arg = toSqlExp(node.getArg(), null);
        SqlExp from = toSqlExp(node.getFrom(), null);
        SqlExp to = toSqlExp(node.getTo(), null);
        arg.setNext(from);
        from.setNext(to);
        return new BetweenExp(arg);
    }

    public Object arriveNotNode(NotNode node, Object msg) {
        SqlExp arg = toSqlExp(node.getExp(), null);
        return new UnaryOpExp(arg, UnaryOpExp.OP_NOT);
    }

    public Object arriveLikeNode(LikeNode node, Object msg) {
        SqlExp arg = toSqlExp(node.getPath(), null);
        SqlExp pattern = toSqlExp(node.getPattern(), null);
        if (node.getEscape() != null) {
            throw notImplemented(
                    "Escape character argument to LIKE is not implemented");
        }
        return new BinaryOpExp(arg, BinaryOpExp.LIKE, pattern);
    }

    public Object arriveUnaryMinusNode(UnaryMinusNode node, Object msg) {
        SqlExp e = toSqlExp(node, msg);
        if (e.getNext() != null) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expressions consisting of multiple columns may not be used" +
                "with unary -");
        }
        return new UnaryOpExp(e, UnaryOpExp.OP_MINUS);
    }

}

