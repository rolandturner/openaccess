
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
package com.versant.core.jdo.tools.workbench.jdoql.insight;

import za.co.hemtech.gui.exp.ExpTableModel;

/**
 * @keep-all
 * Exp table model with extra convenience methods.
 */
public class QExpTableModel extends ExpTableModel {

    /**
     * Create new with name.
     */
    public QExpTableModel(String name) { super(name); }

    /**
     * Moves the cursor to object o in this model.
     */
    public void setSelected(Object o) {
        if(!getList().contains(o)) {
            throw new IllegalArgumentException( "Object " + o + "does not "+
                                                "exist in model" + getName());
        }
        setCursorPosition(getList().indexOf(o), 0, null);
    }

    /**
     * Get the cursor object in this model.
     */
    public Object getSelected() {
        return getCursorObject();
    }

}


