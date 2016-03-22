
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
package com.versant.core.jdo.query;

/**
 * Base class for classes implementing NodeVisitor with do nothing
 * implementations of all the methods. This makes it possible to add new
 * methods to the interface without breaking existing code.
 */
public class NodeVisitorAdapter implements NodeVisitor {

    /**
     * Default visit implementation. Returns null.
     */
    protected Object defaultVisit(Node node, Object[] results) {
        return null;
    }

    /**
     * Default arrive implementation. Returns null.
     */
    protected Object defaultArrive(Node node, Object msg) {
        return null;
    }

    public Object visitLiteralNode(LiteralNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitAddNode(AddNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitMultiplyNode(MultiplyNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitUnaryNode(UnaryNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitUnaryOpNode(UnaryOpNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitNotEqualNode(NotEqualNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitEqualNode(EqualNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitLikeNode(LikeNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitCompareOpNode(CompareOpNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitParamNode(ParamNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitFieldNode(FieldNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitFieldNavNode(FieldNavNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitVarNode(VarNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitMethodNode(MethodNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitAndNode(AndNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitOrNode(OrNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitAggregateCountStarNode(AggregateCountStarNode node,
            Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitAggregateNode(AggregateNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitAsValueNode(AsValueNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitGroupingNode(GroupingNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitResultNode(ResultNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitVarBindingNode(VarBindingNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object visitOrderNode(OrderNode node, Object[] results) {
        return defaultVisit(node, results);
    }

    public Object arriveLiteralNode(LiteralNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveFieldNavNode(FieldNavNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveMethodNode(MethodNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arrivePrimaryExprNode(PrimaryExprNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveFieldNode(FieldNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveEqualNode(EqualNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveLikeNode(LikeNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveNotEqualNode(NotEqualNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAndNode(AndNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveOrNode(OrNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveMultiplyNode(MultiplyNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAddNode(AddNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveUnaryOpNode(UnaryOpNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveCompareOpNode(CompareOpNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveUnaryNode(UnaryNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveBinaryNode(BinaryNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveCastNode(CastNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveParamNode(ParamNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveParamNodeProxy(ParamNodeProxy node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveArgNode(ArgNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveArrayNode(ArrayNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveImportNode(ImportNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveLeafNode(LeafNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveOrderNode(OrderNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveVarNode(VarNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveVarNodeProxy(VarNodeProxy node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveReservedFieldNode(ReservedFieldNode node,
            Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAggregateCountStarNode(AggregateCountStarNode node,
            Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAggregateNode(AggregateNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAsValueNode(AsValueNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveGroupingNode(GroupingNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveResultNode(ResultNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveVarBindingNode(VarBindingNode node, Object msg) {
        return defaultArrive(node, msg);
    }

}

