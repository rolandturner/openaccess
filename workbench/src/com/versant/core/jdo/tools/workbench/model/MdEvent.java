
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

import java.util.EventObject;

/**
 *
 */
public class MdEvent extends EventObject {

    protected int flags;
    private boolean consumed = false;
    private MdProject project;
    private MdDataStore dataStore;

    public MdEvent(Object source, MdProject project, MdDataStore dataStore) {
        super(source);
        this.project = project;
        this.dataStore = dataStore;
    }

    public boolean hasFlagSet(int flag) {
        return (flags & flag) == flag;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    public MdProject getProject() {
        return project;
    }

    public MdDataStore getDataStore() {
        return dataStore;
    }
}
