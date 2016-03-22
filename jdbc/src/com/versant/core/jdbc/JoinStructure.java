
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
package com.versant.core.jdbc;

import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.FetchGroup;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a dataStructure that is used to remember join conditions.
 * This is a composite structure where by a child knows its parent, but not
 * the other way round.
 */
public class JoinStructure implements Comparable {
    public FetchGroupField fgField;
    public JoinStructure parent;
    public boolean refField;
    public int level = 1;

    public FetchGroupField[] fgFields;
    /**
     * If this is the root of the joinStructTree
     */
    public boolean rootJoinStructure;
    /**
     * This is a collection of all the JoinStructure end points that is a
     * collection.
     */
    public List colJoinStructs;
    public FetchGroup fetchGroup;
    private boolean finished;

    public JoinStructure(FetchGroup fg) {
        rootJoinStructure = true;
        colJoinStructs = new ArrayList();
        this.fetchGroup = fg;
    }

    public JoinStructure(JoinStructure parent, FetchGroupField field) {
        this.fgField = field;
        this.setParent(parent);
        this.refField = (field.fmd.category == MDStatics.CATEGORY_REF);
        if (!refField) {
            addColJoinStruct(this);
        }
    }

    public boolean isRefField() {
        return refField;
    }

    /**
     * Called to finalize the structure.
     */
    public void finish() {
        if (finished) return;
        finished = true;
        List structs = colJoinStructs;
        if (structs == null) return;
        for (int i = 0; i < structs.size(); i++) {
            JoinStructure js = (JoinStructure) structs.get(i);
            js.finishImp();
        }
    }

    private void finishImp() {
        fgFields = new FetchGroupField[level - 1];
        appendTo(fgFields);
    }

    private void appendTo(FetchGroupField[] fgFArray) {
        if (rootJoinStructure) return;
        fgFArray[level - 2] = fgField;
        parent.appendTo(fgFArray);
    }

    private void addColJoinStruct(JoinStructure js) {
        if (rootJoinStructure) {
            colJoinStructs.add(js);
        } else {
            parent.addColJoinStruct(js);
        }
    }

    private void setParent(JoinStructure joinStructure) {
        if (joinStructure != null) {
            parent = joinStructure;
            level = parent.level + 1;
        }
    }

    public int compareTo(Object o) {
        JoinStructure other = (JoinStructure) o;
        return level - other.level;
    }

    public String toString() {
        if (rootJoinStructure) {
            return "JoinStructure2@" + System.identityHashCode(this) + " for ROOT";
        } else {
            return "JoinStructure2@" + System.identityHashCode(this) + " for " + fgField.fmd.name + " ref " + isRefField();
        }
    }

    public void dump(String indent) {
        System.out.println(indent +  "this = " + this + " finished " + finished);
        if (colJoinStructs == null) return;
        for (int i = 0; i < colJoinStructs.size(); i++) {
            JoinStructure joinStructure = (JoinStructure) colJoinStructs.get(i);
            joinStructure.dump(indent + "  ");
        }
    }
}
