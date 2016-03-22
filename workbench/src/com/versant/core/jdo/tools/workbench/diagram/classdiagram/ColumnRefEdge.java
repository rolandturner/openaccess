
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
package com.versant.core.jdo.tools.workbench.diagram.classdiagram;

import com.jgraph.graph.DefaultEdge;
import com.versant.core.jdo.tools.workbench.model.GraphColumnRef;

/**
 * Edge for a column  (e.g. a foreign key reference).
 *
 * @keep-all
 */
public class ColumnRefEdge extends DefaultEdge implements HasComment {

    private GraphColumnRef ref;

    public ColumnRefEdge(GraphColumnRef ref) {
        super(ref);
        this.ref = ref;
    }

    public GraphColumnRef getRef() {
        return ref;
    }

    public String getComment() {
        return ref.getComment();
    }

}

 
