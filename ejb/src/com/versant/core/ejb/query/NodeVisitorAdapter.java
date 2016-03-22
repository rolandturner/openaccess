
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
 * Base class for classes implementing NodeVisitor with do nothing
 * implementations of all the methods. This makes it possible to add new
 * methods to the interface without breaking existing code.
 */
public class NodeVisitorAdapter implements NodeVisitor {

    /**
     * Default arrive implementation. Returns null.
     */
    protected Object defaultArrive(Node node, Object msg) {
        return null;
    }

    public Object arriveAddNode(AddNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAggregateNode(AggregateNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAllOrAnyNode(AllOrAnyNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveAndNode(AndNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveBetweenNode(BetweenNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveCollectionMemberNode(CollectionMemberNode node,
            Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveCompNode(CompNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveConstructorNode(ConstructorNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveDateFunctionNode(DateFunctionNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveDeleteNode(DeleteNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveEmptyCompNode(EmptyCompNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveExistsNode(ExistsNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveIdentificationVarNode(IdentificationVarNode node,
            Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveInNode(InNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveJoinNode(JoinNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveLikeNode(LikeNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveLiteralNode(LiteralNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveMemberCompNode(MemberCompNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveMultiplyNode(MultiplyNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveNotNode(NotNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveNullCompNode(NullCompNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveNumericFunctionNode(NumericFunctionNode node,
            Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveObjectNode(ObjectNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveOrNode(OrNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveParameterNode(ParameterNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveParenNode(ParenNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arrivePathNode(PathNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveSelectNode(SelectNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveSetNode(SetNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveStringFunctionNode(StringFunctionNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveUnaryMinusNode(UnaryMinusNode node, Object msg) {
        return defaultArrive(node, msg);
    }

    public Object arriveUpdateNode(UpdateNode node, Object msg) {
        return defaultArrive(node, msg);
    }

}

