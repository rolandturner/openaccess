
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
import com.versant.core.jdo.tools.workbench.model.MdField;

/**
 * Edge for a field (e.g. a collection).
 * @keep-all
 */
public class MdFieldEdge extends DefaultEdge {

    private MdField field;

    public MdFieldEdge(MdField field, Object userObject) {
        super(userObject);
        this.field = field;
    }

    public MdField getField() {
        return field;
    }

}

 
