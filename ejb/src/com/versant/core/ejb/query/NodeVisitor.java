
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
package com.versant.core.ejb.query;

/**
 * Callback interface for a EJBQL Node tree.
 *
 * @see Node#arrive(NodeVisitor, Object)
 */
public interface NodeVisitor {

    public Object arriveAddNode(AddNode node, Object msg);

    public Object arriveAggregateNode(AggregateNode node, Object msg);

    public Object arriveAllOrAnyNode(AllOrAnyNode node, Object msg);

    public Object arriveAndNode(AndNode node, Object msg);

    public Object arriveBetweenNode(BetweenNode node, Object msg);

    public Object arriveCollectionMemberNode(CollectionMemberNode node, Object msg);

    public Object arriveCompNode(CompNode node, Object msg);

    public Object arriveConstructorNode(ConstructorNode node, Object msg);

    public Object arriveDateFunctionNode(DateFunctionNode node, Object msg);

    public Object arriveDeleteNode(DeleteNode node, Object msg);

    public Object arriveEmptyCompNode(EmptyCompNode node, Object msg);

    public Object arriveExistsNode(ExistsNode node, Object msg);

    public Object arriveIdentificationVarNode(IdentificationVarNode node, Object msg);

    public Object arriveInNode(InNode node, Object msg);

    public Object arriveJoinNode(JoinNode node, Object msg);

    public Object arriveLikeNode(LikeNode node, Object msg);

    public Object arriveLiteralNode(LiteralNode node, Object msg);

    public Object arriveMemberCompNode(MemberCompNode node, Object msg);

    public Object arriveMultiplyNode(MultiplyNode node, Object msg);

    public Object arriveNotNode(NotNode node, Object msg);

    public Object arriveNullCompNode(NullCompNode node, Object msg);

    public Object arriveNumericFunctionNode(NumericFunctionNode node, Object msg);

    public Object arriveObjectNode(ObjectNode node, Object msg);

    public Object arriveOrNode(OrNode node, Object msg);

    public Object arriveParameterNode(ParameterNode node, Object msg);

    public Object arriveParenNode(ParenNode node, Object msg);

    public Object arrivePathNode(PathNode node, Object msg);

    public Object arriveSelectNode(SelectNode node, Object msg);

    public Object arriveSetNode(SetNode node, Object msg);

    public Object arriveStringFunctionNode(StringFunctionNode node, Object msg);

    public Object arriveUnaryMinusNode(UnaryMinusNode node, Object msg);

    public Object arriveUpdateNode(UpdateNode node, Object msg);

}
