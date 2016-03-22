
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
 * A cast operation.
 */
public class CastNode extends UnaryNode {

    public int brackets;
    public String type;
    public Class cls;

    public CastNode(Node child, int brackets, Object tp) {
        super(child);
        this.brackets = brackets;
        if (tp instanceof Class) cls = (Class)tp;
        else type = (String)tp;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(" (");
        if (cls != null) s.append(cls);
        else s.append(type);
        s.append(')');
        return s.toString();
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitCastNode(this, obj);
    }

    /**
     * Convert us into a FieldNavNode with cast information.
     */
    protected void normalizeImp() {
        super.normalizeImp();

        // convert this tree:
        //   CastNode (Address) [this]
        //     FieldNavNode (PolyRefHolder)data [toRaise]
        //       FieldNode data [toReplace]
        //   FieldNode city [toMove]
        //   SomeOtherNode [toKeep] (may be missing)

        // or this one (note that toRaise is missing):
        //   CastNode (Address) [this]
        //     FieldNode data [toReplace]
        //   FieldNode city [toMove]
        //   SomeOtherNode [toKeep] (may be missing)

        // into:
        //   FieldNavNode (PolyRefHolder)data [toRaise]
        //     FieldNavNode (Address)data [castFnn]
        //       FieldNode city [toMove]
        //   SomeOtherNode [toKeep] (if not null)

        if (next instanceof FieldNode || next instanceof FieldNavNode) {

            // find FieldNode to replace with FieldNavNode including cast
            FieldNode toReplace = findFieldNode(childList);
            if (toReplace == null) return;
            Node toMove = next;
            Node toKeep = next.next;
            Node toRaise = childList;

            // create FNN including cast from toReplace's field name
            FieldNavNode castFnn = new FieldNavNode();
            castFnn.cast = type;
            castFnn.lexeme = toReplace.lexeme;

            // make toMove castFnn's only child
            castFnn.childList = toMove;
            toMove.parent = castFnn;
            toMove.next = null;

            // make castFnn toReplace.parent's only child
            toReplace.parent.childList = castFnn;
            castFnn.parent = toReplace.parent;

            // if toRaise and toReplace are the same then raise castFnn
            if (toRaise == toReplace) toRaise = castFnn;

            // make toRaise and toKeep our parents first and second children
            // removing us from the tree
            parent.childList = toRaise;
            toRaise.parent = parent;
            toRaise.next = toKeep;
            if (toKeep != null) toKeep.parent = parent;
        }
    }

    /**
     * Find the first FieldNode in tree at root following only the first
     * child of each node down the tree. Returns null if none found.
     */
    private FieldNode findFieldNode(Node root) {
        for (; root != null && !(root instanceof FieldNode); root = root.childList);
        return (FieldNode)root;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveCastNode(this, msg);
    }

}
