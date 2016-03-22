
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

import com.versant.core.common.BindingSupportImpl;

/**
 * This is used to prevent a field with the same name as a parameter or
 * a variable getting mangled when it is under this (i.e. this.field).
 */
public class ReservedFieldNode extends FieldNavNode {

    private int type;
    private ClassMetaData target;

    public static final int TYPE_THIS = 1;

    public ReservedFieldNode(int type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitReservedFieldNode(this, obj);
    }

    public void normalizeImp() {
        if (childList != null) childList.normalizeImp();
    }

    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (type == TYPE_THIS) {
            if (childList != null) {
                parent.replaceChild(this, childList);
                if (childList instanceof FieldNode) {
                    FieldNode fn = ((FieldNode)childList);
                    fn.resolved = true;
                    fn.useCandidateExtent = true;
                } else if (childList instanceof FieldNavNode) {
                    ((FieldNavNode)childList).resolved = true;
                }
                childList.resolve(comp, cmd, false);
            } else { // 'this' on its own reference
                target = cmd;
            }
        } else {
            throw BindingSupportImpl.getInstance().unsupported("ReservedFieldNode of type " +
                    type + " is not supported");
        }
    }

    public ClassMetaData getTarget() {
        return target;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveReservedFieldNode(this, msg);
    }
}

