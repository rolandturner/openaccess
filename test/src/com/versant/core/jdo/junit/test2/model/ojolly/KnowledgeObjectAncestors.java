
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
package com.versant.core.jdo.junit.test2.model.ojolly;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class KnowledgeObjectAncestors {
    private List ancestors = new ArrayList();
    private int parentSemantic;
    private KnowledgeObject knowledgeObject;
    private String val;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public List getAncestors() {
        return ancestors;
    }

    public void setAncestors(List ancestors) {
        this.ancestors = ancestors;
    }

    public int getParentSemantic() {
        return parentSemantic;
    }

    public void setParentSemantic(int parentSemantic) {
        this.parentSemantic = parentSemantic;
    }

    public KnowledgeObject getKnowledgeObject() {
        return knowledgeObject;
    }

    public void setKnowledgeObject(KnowledgeObject knowledgeObject) {
        this.knowledgeObject = knowledgeObject;
    }
}
