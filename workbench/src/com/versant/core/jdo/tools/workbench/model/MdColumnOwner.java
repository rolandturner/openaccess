
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
 * Classes that contain MdColumn's implement this so the column can notify
 * the owner when it becomes empty or not.
 * @keep-all
 */
public interface MdColumnOwner {

    /**
     * The empty status of col has changed.
     */
    public void columnEmptyChanged(MdColumn col);

    /**
     * The JDBC type of col has changed.
     */
    public void columnTypeChanged(MdColumn col);

}

