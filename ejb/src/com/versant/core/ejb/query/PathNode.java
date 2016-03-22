
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

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;

/**
 * Dot separated list of identifiers.
 */
public class PathNode extends Node {

    public static final int WHERE = 1;
    public static final int SELECT = 2;
    public static final int GROUP_BY = 3;
    public static final int ORDER_BY = 4;
    public static final int AGGREGATE = 5;
    public static final int CONSTRUCTOR = 6;
    public static final int JOIN = 7;
    public static final int COLLECTION_MEMBER = 8;

    private String[] list = new String[4];
    private int size;
    private int parentType;

    private NavBase navBase;
    private FieldMetaData fmd;

    /**
     * Create with given parent type (SELECT, GROUP_BY etc).
     */
    public PathNode(int parentType) {
        this.parentType = parentType;
    }

    /**
     * Is this node under a SELECT, GROUP_BY etc.
     */
    public int getParentType() {
        return parentType;
    }

    public void add(String identifier) {
        if (size == list.length) {
            String[] a = new String[list.length + 4];
            System.arraycopy(list, 0, a, 0, list.length);
            list = a;
        }
        list[size++] = identifier;
    }

    public String get(int i) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i + " >= " + size);
        }
        return list[i];
    }

    public int size() {
        return size;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arrivePathNode(this, msg);
    }

    public String getParentTypeString() {
        switch (parentType) {
            case WHERE:             return "WHERE";
            case SELECT:            return "SELECT";
            case GROUP_BY:          return "GROUP_BY";
            case ORDER_BY:          return "ORDER_BY";
            case AGGREGATE:         return "AGGREGATE";
            case CONSTRUCTOR:       return "CONSTRUCTOR";
            case JOIN:              return "JOIN";
            case COLLECTION_MEMBER: return "COLLECTION_MEMBER";
        }
        return "<? parentType " + parentType + " ?>";
    }

    public String toStringImp() {
        if (size == 0) {
            return "<? no identifiers in PathExpression?>";
        }
        StringBuffer s = new StringBuffer();
        s.append(list[0]);
        for (int i = 1; i < size; i++) {
            s.append('.');
            s.append(list[i]);
        }
        if (fmd != null) {
            s.append('%');
            s.append(fmd.getQName());
        } else if (navBase != null) {
            s.append('%');
            s.append(navBase.getNavClassMetaData().qname);
        }
        return s.toString();
    }

    /**
     * Get the identification variable or last field navigated to get to our
     * field. If getFmd() is null then we have no simple field and this path is
     * returning the identification variable or object field on its own.
     */ 
    public NavBase getNavBase() {
        return navBase;
    }

    /**
     * If this path ends in a field then this is it. It will be a field
     * of getNavBase().getNavClassMetaData().
     */
    public FieldMetaData getFmd() {
        return fmd;
    }

    public void resolve(ResolveContext rc) {
        if (size == 1) { // identification var on its own
            String name = get(0);
            navBase = rc.checkIdVarExists(name, this);
        } else {    // field value
            navBase = rc.resolveJoinPath(this, false, false, size - 1);
            ClassMetaData cmd = navBase.getNavClassMetaData();
            String name = get(size - 1);
            fmd = cmd.getFieldMetaData(name);
            if (fmd == null) {
                throw rc.createUserException("No such field '" + name +
                        "' on " + cmd.qname, this);
            }
        }
    }

}

