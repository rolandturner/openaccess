
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
 * This is the common interface for a visitor.
 * Subclasses can apply different behavior for different nodes.
 */
public interface NodeVisitor {

    Object visitLiteralNode(LiteralNode node, Object[] results);

    Object visitAddNode(AddNode node, Object[] results);

    Object visitMultiplyNode(MultiplyNode node, Object[] results);

    Object visitUnaryNode(UnaryNode node, Object[] results);

    Object visitUnaryOpNode(UnaryOpNode node, Object[] results);

    Object visitNotEqualNode(NotEqualNode node, Object[] results);

    Object visitEqualNode(EqualNode node, Object[] results);

    Object visitLikeNode(LikeNode node, Object[] results);

    Object visitCompareOpNode(CompareOpNode node, Object[] results);

    Object visitParamNode(ParamNode node, Object[] results);

    Object visitFieldNode(FieldNode node, Object[] results);

    Object visitFieldNavNode(FieldNavNode node, Object[] results);

    Object visitVarNode(VarNode node, Object[] results);

    Object visitMethodNode(MethodNode node, Object[] results);

    Object visitAndNode(AndNode node, Object[] results);

    Object visitOrNode(OrNode node, Object[] results);

    Object visitOrderNode(OrderNode node, Object[] results);

    public Object visitAggregateCountStarNode(AggregateCountStarNode node, Object[] results);

    public Object visitAggregateNode(AggregateNode node, Object[] results);

    public Object visitAsValueNode(AsValueNode node, Object[] results);

    public Object visitGroupingNode(GroupingNode node, Object[] results);

    public Object visitResultNode(ResultNode node, Object[] results);

    public Object visitVarBindingNode(VarBindingNode node, Object[] results);

    // arrive methods

    public Object arriveLiteralNode(LiteralNode node, Object msg);

    public Object arriveFieldNavNode(FieldNavNode node, Object msg);

    public Object arriveMethodNode(MethodNode node, Object msg);

    public Object arrivePrimaryExprNode(PrimaryExprNode node, Object msg);

    public Object arriveFieldNode(FieldNode node, Object msg);

    public Object arriveEqualNode(EqualNode node, Object msg);

    public Object arriveLikeNode(LikeNode node, Object msg);

    public Object arriveNotEqualNode(NotEqualNode node, Object msg);

    public Object arriveAndNode(AndNode node, Object msg);

    public Object arriveOrNode(OrNode node, Object msg);

    public Object arriveMultiplyNode(MultiplyNode node, Object msg);

    public Object arriveAddNode(AddNode node, Object msg);

    public Object arriveUnaryOpNode(UnaryOpNode node, Object msg);

    public Object arriveCompareOpNode(CompareOpNode node, Object msg);

    public Object arriveUnaryNode(UnaryNode node, Object msg);

    public Object arriveBinaryNode(BinaryNode node, Object msg);

    public Object arriveCastNode(CastNode node, Object msg);

    public Object arriveParamNode(ParamNode node, Object msg);

    public Object arriveParamNodeProxy(ParamNodeProxy node, Object msg);

    public Object arriveArgNode(ArgNode node, Object msg);

    public Object arriveArrayNode(ArrayNode node, Object msg);

    public Object arriveImportNode(ImportNode node, Object msg);

    public Object arriveLeafNode(LeafNode node, Object msg);

    public Object arriveOrderNode(OrderNode node, Object msg);

    public Object arriveVarNode(VarNode node, Object msg);

    public Object arriveVarNodeProxy(VarNodeProxy node, Object msg);

    public Object arriveReservedFieldNode(ReservedFieldNode node, Object msg);

    public Object arriveAggregateCountStarNode(AggregateCountStarNode node, Object msg);

    public Object arriveAggregateNode(AggregateNode node, Object msg);

    public Object arriveAsValueNode(AsValueNode node, Object msg);

    public Object arriveGroupingNode(GroupingNode node, Object msg);

    public Object arriveResultNode(ResultNode node, Object msg);

    public Object arriveVarBindingNode(VarBindingNode node, Object msg);

}
