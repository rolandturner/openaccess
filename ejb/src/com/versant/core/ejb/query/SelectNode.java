
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
 * A select_statement.
 */
public class SelectNode extends Node {

    private boolean distinct;
    private Node selectList;
    private Node fromList;
    private Node where;
    private Node groupBy;
    private Node having;
    private Node orderByList;

    public SelectNode(boolean distinct, Node selectList, Node fromList,
            Node where, Node groupBy, Node having, Node orderByList) {
        this.distinct = distinct;
        this.selectList = selectList;
        this.fromList = fromList;
        this.where = where;
        this.groupBy = groupBy;
        this.having = having;
        this.orderByList = orderByList;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public Node getSelectList() {
        return selectList;
    }

    public Node getFromList() {
        return fromList;
    }

    public Node getWhere() {
        return where;
    }

    public Node getGroupBy() {
        return groupBy;
    }

    public Node getHaving() {
        return having;
    }

    public Node getOrderByList() {
        return orderByList;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveSelectNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append("SELECT ");
        if (distinct) {
            s.append("DISTINCT ");
        }
        if (selectList != null) {
            selectList.appendList(s);
        }
        s.append("\nFROM ");
        if (fromList != null) {
            fromList.appendList(s, "\n     ");
        }
        if (where != null) {
            s.append("\nWHERE ");
            s.append(where);
        }
        if (groupBy != null) {
            s.append("\nGROUP BY ");
            groupBy.appendList(s);
            if (having != null) {
                s.append("\nHAVING ");
                s.append(having);
            }
        }
        if (orderByList != null) {
            s.append("\nORDER BY ");
            orderByList.appendList(s);
        }
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        resolve(fromList, rc);
        resolve(selectList, rc);
        resolve(where, rc);
        resolve(groupBy, rc);
        resolve(having, rc);
        resolve(orderByList, rc);
    }

}

