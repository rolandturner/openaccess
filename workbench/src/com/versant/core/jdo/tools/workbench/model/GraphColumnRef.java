
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
package com.versant.core.jdo.tools.workbench.model;

/**
 * A reference between two sets of columns on a graph.
 */
public interface GraphColumnRef {

    GraphTable getSrcTable();
    GraphTable getDestTable();
    GraphColumn[] getSrcColumns();
    GraphColumn[] getDestColumns();
    String getComment();
    boolean isOneToMany();

}


