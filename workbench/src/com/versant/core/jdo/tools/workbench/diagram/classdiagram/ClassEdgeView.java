
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

import com.jgraph.graph.EdgeView;
import com.jgraph.graph.CellMapper;
import com.jgraph.JGraph;

import java.util.List;
import java.awt.*;

/**
 * @keep-all
 */
public class ClassEdgeView extends EdgeView{

    static {
        renderer = new ClassEdgeRenderer();
    }

    public ClassEdgeView(Object cell, JGraph graph, CellMapper mapper) {
        super(cell, graph, mapper);
    }
}

