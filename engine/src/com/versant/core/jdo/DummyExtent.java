
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
package com.versant.core.jdo;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.FetchPlan;
import java.util.Iterator;
import java.io.Serializable;

import com.versant.core.common.BindingSupportImpl;

/**
 * @keep-all
 */
public class DummyExtent implements Extent, Serializable {
    private Class pcClass;
    private boolean hasSubs;

    public DummyExtent(Class pcClass, boolean hasSubs) {
        this.pcClass = pcClass;
        this.hasSubs = hasSubs;
    }

    public Iterator iterator() {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    public boolean hasSubclasses() {
        return hasSubs;
    }

    public Class getCandidateClass() {
        return pcClass;
    }

    public PersistenceManager getPersistenceManager() {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    public void closeAll() {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    public void close(Iterator it) {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    public FetchPlan getFetchPlan() {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }
}
