
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
public class MdClassChangeEvent extends MdEvent {

    private MdClass changedClass;

    public MdClassChangeEvent(Object source, MdProject project, MdDataStore dataStore,
            MdClass changedClass) {
        super(source, project, dataStore);
        this.changedClass = changedClass;
    }

    public MdClass getChangedClass() {
        return changedClass;
    }
}