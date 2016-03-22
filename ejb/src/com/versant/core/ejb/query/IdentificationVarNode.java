
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

import com.versant.core.metadata.ClassMetaData;

/**
 * For identification_variable_declaration.
 */
public class IdentificationVarNode extends Node {

    private String abstractSchemaName;
    private String identifier;
    private JoinNode joinList;

    private NavRoot navRoot;

    public IdentificationVarNode(String schemaName, String identifier,
            JoinNode joinList) {
        this.abstractSchemaName = schemaName;
        this.identifier = identifier;
        this.joinList = joinList;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public JoinNode getJoinList() {
        return joinList;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveIdentificationVarNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(abstractSchemaName);
        if (navRoot != null) {
            s.append('%');
            s.append(navRoot.getNavClassMetaData().qname);
        }
        s.append(" AS ");
        s.append(identifier);
        for (Node e = joinList; e != null; e = e.getNext()) {
            s.append(' ');
            s.append(e);
        }
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        checkIdVarDoesNotExist(rc);
        ClassMetaData cmd = rc.getModelMetaData().getClassMetaByASN(abstractSchemaName);
        if (cmd == null) {
            throw rc.createUserException("Unknown abstract schema name: " +
                    abstractSchemaName, this);
        }
        navRoot = new NavRoot(this, cmd);
        rc.addIdVar(identifier, navRoot);
        resolve(joinList, rc);
    }

    private void checkIdVarDoesNotExist(ResolveContext rc) {
        NavBase dup = rc.getIdVar(identifier);
        if (dup != null) {
            throw rc.createUserException("Duplicate identification variable: " +
                    identifier, this);
        }
    }

    public NavRoot getNavRoot() {
        return navRoot;
    }

}

