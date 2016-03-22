
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

import com.versant.core.common.Debug;
import com.versant.core.common.CmdBitSet;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;

import com.versant.core.common.BindingSupportImpl;

/**
 * This node is created when a field that is a reference to a PC class is
 * navigated in an expression. Information required to generate a subquery
 * or join is included here. The child nodes become extra expressions for
 * the where clause.
 */
public class FieldNavNode extends UnaryNode {

    public String lexeme;
    /**
     * If we were prefixed with a class cast expression then this is the
     * type being cast to.
     */
    public String cast;
    /**
     * The field being navigated (normal ref or polyref). This is used to
     * construct a join from the column(s) for this field in the src table to
     * the primary key of the fields class.
     */
    public FieldMetaData fmd;
    /**
     * The class being referenced.
     */
    public ClassMetaData targetClass;

    public boolean resolved;
    /**
     * Does this node represent an embedded field.
     */
    public boolean embedded;

    /**
     * The variable being accessed. This is used to locate the query for
     * the variable.
     */
    public VarNode var;

    public FieldNavNode() {
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitFieldNavNode(this, results);
    }

    public String toString() {
        return super.toString() + " " +
                (cast != null ? "(" + cast + ")" : "") +
                (fmd != null ? fmd.toString() : lexeme) + " as " + asValue;
    }

    protected void normalizeImp() {
        if (embedded) {
            throw BindingSupportImpl.getInstance().internal(
                    "This node should be dissolved");
        }
        super.normalizeImp();
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (Debug.DEBUG) System.out.println("### FieldNavNode.resolve " + this);
        if (asValue != null) childList.asValue = asValue;
        ClassMetaData tcmd = null;
        if (!resolved && !(parent instanceof FieldNavNode)) {
            // this could be a variable reference
            var = comp.findVar(lexeme);
            if (var != null) {
				tcmd = resolveVariable();
				if (cast != null) {
					ClassMetaData c = comp.resolveCastType(cast)[0];
					if (tcmd == c) {
						cast = null; // redundant cast
					} else {
						tcmd = c;
					}
				}
			}
        }
        if (var == null) tcmd = resolveField(cmd, comp);
		if (childList != null) {
            childList.resolve(comp, tcmd, false);
        }
        if (embedded) {
            parent.replaceChild(this, childList);
        }
        resolved = true;
    }

    private ClassMetaData resolveField(ClassMetaData cmd, QueryParser comp) {
        ClassMetaData tcmd;

        if (parent instanceof FieldNavNode) {
            FieldNavNode pFnn = (FieldNavNode) parent;
            if (pFnn.embedded) {
                String fname = pFnn.fmd.name + "/" + lexeme;
                FieldMetaData[] fmds = pFnn.fmd.classMetaData.fields;
                for (int i = 0; i < fmds.length; i++) {
                    FieldMetaData fieldMetaData = fmds[i];
                    if (fieldMetaData.name.equals(fname)) {
                        fmd = this.fmd = fieldMetaData;
                        break;
                    }
                }
            }
        }

        if (fmd == null) {
            fmd = cmd.getFieldMetaData(lexeme);
        }
        if (fmd == null) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Field '" + lexeme + "' not found on " + cmd.qname);
        }
        embedded = fmd.embedded;

        if (fmd.category != MDStatics.CATEGORY_REF
                && fmd.category != MDStatics.CATEGORY_POLYREF) {
            throw BindingSupportImpl.getInstance().runtime("Field '" + lexeme + "' on " + cmd.qname +
                    " is not a reference to another PC class");
        }
        tcmd = fmd.typeMetaData;
        if (cast != null) {
            ClassMetaData c = comp.resolveCastType(cast)[0];
            if (tcmd == c) {
                cast = null; // redundant cast
            } else {
                tcmd = c;
            }
        } else if (tcmd == null) {  // polyref
            throw BindingSupportImpl.getInstance().runtime("Field '" + fmd.getTypeQName() + "' " +
                    " must be cast to a persistent class to be " +
                    "navigated in a query");
        }

        targetClass = tcmd;

        if (embedded) return cmd;
        return tcmd;
    }

    private ClassMetaData resolveVariable() {
        ClassMetaData vcmd = var.getCmd();
        if (vcmd == null) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Variable '" + lexeme + "' is not of a persistent class");
        }
        if (!var.bound) var.insertVarBindingNode(parent);

        return vcmd;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitFieldNavNode(this, obj);
    }

    /**
     * Return the FieldMetaData at the end of this chain.
     */
    public FieldMetaData getResultFmd() {
        if (childList instanceof FieldNavNode) {
            return ((FieldNavNode)childList).getResultFmd();
        } else if (childList instanceof FieldNode) {
            return ((FieldNode)childList).fmd;
        } else {
            throw BindingSupportImpl.getInstance().runtime("");
        }
    }

    /**
     * Implement this in nodes to udpate the ClassMetaData depency of the graph.
     * This is used for query eviction.
     *
     * @param bitSet
     */
    public void updateEvictionDependency(CmdBitSet bitSet) {
        if (targetClass != null) bitSet.addPlus(targetClass);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveFieldNavNode(this, msg);
    }

}
