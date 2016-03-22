
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
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.Debug;
import com.versant.core.common.CmdBitSet;

/**
 * A variable declaration.
 */
public class VarNode extends LeafNode implements VarNodeIF {

    private String type;
    private String identifier;
    private Class cls;
    private ClassMetaData cmd;
    private Object storeExtent;
    private FieldMetaData fmd;
    private Object fieldExtent;
    public boolean bound;

    /**
     * If this variable is present in the result projection
     */
    private boolean usedInProjection;

    public VarNode() {
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitVarNode(this, results);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(' ');
        if (cls != null) s.append(cls);
        else s.append(type);
        s.append(' ');
        s.append(identifier);
        s.append(' ');
        s.append(bound ? "bound" : "unbound");
        return s.toString();
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitVarNode(this, obj);
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

    public void resolve(QueryParser comp) {
        cls = comp.resolveVarType(type);
        cmd = comp.getCMD(cls);
    }

    public Object getStoreExtent() {
        return storeExtent;
    }

    public void setStoreExtent(Object storeExtent) {
        this.storeExtent = storeExtent;
    }

    public void setFieldExtent(Object fieldExtent) {
        this.fieldExtent = fieldExtent;
    }

    public void setFmd(FieldMetaData fmd) {
        this.fmd = fmd;
    }

    /**
     * Insert a VarBindingNode for us into the tree before sibling (i.e.
     * sibling.parent == varbindingnode.parent && varbindingnode.next = sibling).
     * This is used to "bind" unbound variables to the extent of the variables
     * class.
     */
    public void insertVarBindingNode(Node sibling) {
        // 'and' our sibling with a VarBindingNode to bind us to the
        // variables extent
        if (sibling.parent.getClass() == AndNode.class) {
            AndNode and = (AndNode)sibling.parent;
            and.insertChildBefore(sibling, new VarBindingNode(this));
        } else {
            AndNode and = new AndNode();
            sibling.parent.replaceChild(sibling, and);
            and.childList = new VarBindingNode(this);
            and.childList.parent = and;
            and.childList.next = sibling;
            sibling.parent = and;
        }
        bound = true;
        if (Debug.DEBUG) {
            System.out.println("### Added VarBindingNode for " + this);
        }
    }

    /**
     * Implement this in nodes to udpate the ClassMetaData depency of the graph.
     * This is used for query eviction.
     *
     * @param bitSet
     */
    public void updateEvictionDependency(CmdBitSet bitSet) {
        if (cmd == null) return;
        bitSet.addPlus(cmd);
    }

    public void setUsedInProjection(boolean usedInProjection) {
        this.usedInProjection = usedInProjection;
    }

    public boolean isUsedInProjection() {
        return usedInProjection;
    }

    public VarNode getVarNode() {
        return this;
    }

    public Object getFieldExtent() {
        return fieldExtent;
    }

    public FieldMetaData getFmd() {
        return fmd;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveVarNode(this, msg);
    }
}
