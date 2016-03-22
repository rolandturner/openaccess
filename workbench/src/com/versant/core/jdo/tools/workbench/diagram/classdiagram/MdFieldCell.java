
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

import com.jgraph.graph.DefaultGraphCell;
import com.versant.core.jdo.tools.workbench.model.MdField;

/**
 * @keep-all
 */
public class MdFieldCell extends DefaultGraphCell {
    MdField mdField;

    public MdFieldCell(MdField mdField) {
        this.mdField = mdField;
    }

    public MdField getMdField() {
        return mdField;
    }

    public void setMdField(MdField mdField) {
        this.mdField = mdField;
    }
}
