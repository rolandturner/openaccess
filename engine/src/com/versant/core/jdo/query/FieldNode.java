
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

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.common.Debug;

import com.versant.core.common.BindingSupportImpl;

/**
 * This node is created when the value of a field is required as part
 * of an expression.
 */
public class FieldNode extends LeafNode {

    public String lexeme;
    /**
     * The field whose value is required.
     */
    public FieldMetaData fmd;
    /**
     * If true then the field is on the candidate instance being tested
     * (e.g. this.field).
     */
    public boolean useCandidateExtent;

    public boolean resolved;

    public FieldNode() {
    }

    public FieldNode(Node parent, String lexeme) {
        this.parent = parent;
        this.lexeme = lexeme;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitFieldNode(this, results);
    }

    public String toString() {
        return super.toString() + " " + (fmd != null ? fmd.toString() : lexeme) + " asValue " + asValue;
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (Debug.DEBUG) System.out.println("### FieldNode.resolve " + this);
        final boolean fnn = parent instanceof FieldNavNode;
        if (!ordering) {
            if (!resolved && !fnn) {
                VarNode v = comp.findVar(lexeme);
                Node rep;
                if (v != null) {
                    // if we are the first child of a MethodNode (i.e. the
                    // instance the method is being invoked on) and have not
                    // been bound then we are an unbound variable
                    if (!v.bound && ((parent instanceof BinaryNode)
                            || (parent instanceof MethodNode && parent.childList == this))) {
                        v.insertVarBindingNode(parent);
                    } else if(!v.bound && (parent instanceof MethodNode
                            && parent.childList instanceof ParamNode)) {
                        v.insertVarBindingNode(parent);
                    } else {
                        v.bound = true;
                        if (Debug.DEBUG) {
                            System.out.println("### bound " + this);
                        }
                    }
                    if (v.parent != null) rep = new VarNodeProxy(v);
                    else rep = v;
                } else {
                    rep = v;
                }
                if (rep == null) {
                    ParamNode p = comp.findParam(lexeme);
                    if (p != null && p.parent != null) rep = new ParamNodeProxy(p);
                    else rep = p;
                }
                if (rep == null && lexeme.equals("this")) {
                    rep = new ReservedFieldNode(ReservedFieldNode.TYPE_THIS, "this");
                    rep.resolve(comp, cmd, false);
                }
                if (rep != null) {
                    parent.replaceChild(this, rep);
                    return;
                }
            }
        }

        if (fnn && ((FieldNavNode)parent).embedded) {
            fmd = FieldNode.findEmbeddedFmd((FieldNavNode) parent, cmd, lexeme);
        } else {
            fmd = cmd.getFieldMetaData(lexeme);
        }
        if (fmd == null && "NULL".equals(lexeme.toUpperCase())) {
            parent.replaceChild(this, new LiteralNode(parent,
                    LiteralNode.TYPE_NULL, lexeme));
            return;
        } else if (fmd == null) {
            //assume that this is an aliased column name
            parent.replaceChild(this, new AsValueNode(lexeme));
            return;
        }
        if (fmd == null) {
            String msg;
            if (fnn) {
                msg = "Field '" + lexeme + "' not found on " +
                    cmd.qname;
            } else {
                msg = "Identifier '" + lexeme + "' is not a parameter, " +
                    "variable or field of " + cmd.qname;
            }
            throw BindingSupportImpl.getInstance().runtime(msg);
        }

        /**
         * Update the cmd for multi-Table inheritance
         */
        if (fmd.classMetaData != cmd) {
            cmd = fmd.classMetaData;
        }
    }

    /**
     * Find a fieldmetadata of the embedded class.
     * @param fNN
     * @param cmd
     * @param fieldName
     */
    private static FieldMetaData findEmbeddedFmd(FieldNavNode fNN, ClassMetaData cmd, String fieldName) {
        FieldMetaData lFmd = null;
        if (!fNN.embedded) return null;
        /**
         * The lexeme might refer to a horizontal inhereted field or a normal field
         */
        FieldMetaData origFmd = fNN.fmd.typeMetaData.getFieldMetaData(fieldName);
        FieldMetaData[] fmds = cmd.fields;
        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData fieldMetaData = fmds[i];
            if (fieldMetaData.name.equals(fNN.fmd.name + "/" + origFmd.name)) {
                lFmd = fieldMetaData;
                break;
            }
        }
        if (lFmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("Field '" + fieldName + "' in not a persistent embedded field");
        }
        if (lFmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
            throw BindingSupportImpl.getInstance().invalidOperation("Field '" + fieldName + "' in not a persistent embedded field");
        }
        return lFmd;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitFieldNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveFieldNode(this, msg);
    }

}

