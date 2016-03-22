
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.Debug;

/**
 * This node is created when the value of a parameter is required as part
 * of an expression.
 */
public class ParamNode extends LeafNode {

    private String type;
    private String identifier;
    private Class cls;
    private ClassMetaData cmd;
    private Object value;
    /**
     * This is the index of the parameter in the declared parameter list.
     */
    private int index;

    /**
     * The store specific usage list for this node.
     */
    public Object usageList;

    public ParamNode() {
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitParamNode(this, results);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(' ');
        if (cls != null) s.append(cls);
        else s.append(type);
        s.append(' ');
        s.append(identifier);
        s.append(" index ");
        s.append(index);
        return s.toString();
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (Debug.DEBUG) System.out.println("### ParamNode.resolve " + this);                
        if (cls == null) cls = comp.resolveParamType(type);
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitParamNode(this, obj);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }

    public ClassMetaData getCmd() {
        return cmd;
    }

    public void setCmd(ClassMetaData cmd) {
        this.cmd = cmd;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Object getUsageList() {
        return usageList;
    }

    public void clearSqlUsageList() {
        usageList = null;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveParamNode(this, msg);
    }

}
