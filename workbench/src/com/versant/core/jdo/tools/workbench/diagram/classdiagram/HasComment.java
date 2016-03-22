
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

/** 
 * If a graph cells implements this interface then the getComment method
 * is called to provide a tooltip.
 * @keep-all
 */
public interface HasComment {

    /**
     * Get a comment for the cell. This is typically displayed on a tooltip.
     */
    public String getComment();

}

