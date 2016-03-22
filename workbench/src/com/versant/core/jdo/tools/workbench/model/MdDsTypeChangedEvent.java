
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
public class MdDsTypeChangedEvent extends MdEvent {

    private int oldType;
    private int newType;

    public MdDsTypeChangedEvent(Object source, MdProject project, MdDataStore dataStore,
            int oldType, int newType, int flags) {
        super(source, project, dataStore);
        this.oldType = oldType;
        this.newType = newType;
        this.flags = flags;
    }

    public MdDsTypeChangedEvent(MdDsTypeChangedEvent event, MdProject project) {
        this(event.getSource(), project, event.getDataStore(),
                event.getOldType(), event.getNewType(), event.flags);
    }

    public int getOldType() {
        return oldType;
    }

    public int getNewType() {
        return newType;
    }
}
