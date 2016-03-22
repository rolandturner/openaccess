
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
 *
 */
public class MdChangeEvent extends MdEvent {

    public static final int FLAG_NEED_RECOMPILE = 0;
    public static final int FLAG_NEED_REBUILD = 1;
    public static final int FLAG_DO_NOT_RECOMPILE = 2;
    public static final int FLAG_CLASSES_CHANGED = 4;

    public MdChangeEvent(Object source, MdProject project, MdDataStore dataStore,
            int flags) {
        super(source, project, dataStore);
        this.flags = flags;
    }
}
