
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
 * Visitor for nodes in the parse tree to generate bytecode for in memory
 * queries.
 * @see Node#visit
 */
public interface MemVisitor {

    public Field visitNode(Node node, Object obj);

    public Field visitLiteralNode(LiteralNode node, Object obj);

    public Field visitFieldNavNode(FieldNavNode node, Object obj);

    public Field visitMethodNode(MethodNode node, Object obj);

    public Field visitPrimaryExprNode(PrimaryExprNode node, Object obj);

    public Field visitFieldNode(FieldNode node, Object obj);

    public Field visitEqualNode(EqualNode node, Object obj);

    public Field visitNotEqualNode(NotEqualNode node, Object obj);

    public Field visitLikeNode(LikeNode node, Object obj);

    public Field visitAndNode(AndNode node, Object obj);

    public Field visitOrNode(OrNode node, Object obj);

    public Field visitMultiplyNode(MultiplyNode node, Object obj);

    public Field visitAddNode(AddNode node, Object obj);

    public Field visitUnaryOpNode(UnaryOpNode node, Object obj);

    public Field visitCompareOpNode(CompareOpNode node, Object obj);

    public Field visitUnaryNode(UnaryNode node, Object obj);

    public Field visitBinaryNode(BinaryNode node, Object obj);

    public Field visitMultiNode(Node node, Object obj);

    public Field visitCastNode(CastNode node, Object obj);

    public Field visitParamNode(ParamNode node, Object obj);

    public Field visitParamNodeProxy(ParamNodeProxy node, Object obj);

    public Field visitArgNode(ArgNode node, Object obj);

    public Field visitArrayNode(ArrayNode node, Object obj);

    public Field visitImportNode(ImportNode node, Object obj);

    public Field visitLeafNode(LeafNode node, Object obj);

    public Field visitOrderNode(OrderNode node, Object obj);

    public Field visitVarNode(VarNode node, Object obj);

    public Field visitVarNodeProxy(VarNodeProxy node, Object obj);

    public Field visitReservedFieldNode(ReservedFieldNode node, Object obj);
}
