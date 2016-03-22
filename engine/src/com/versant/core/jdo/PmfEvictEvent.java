
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

/**
 * An OID, collection of OIDs or class has been evicted from the PMF wide cache.
 */
public class PmfEvictEvent extends ServerLogEvent {

    public PmfEvictEvent(Object oid) {
        super(PMF_EVICT, oid.toString());
    }

    public PmfEvictEvent(Object[] oids) {
        super(PMF_EVICT, oids.length + " OIDs");
    }

    public PmfEvictEvent(Class cls, boolean includeSubclasses) {
        super(PMF_EVICT, cls.getName() + " includeSubclasses " + includeSubclasses);
    }

    public PmfEvictEvent() {
        super(PMF_EVICT, "<all>");
    }

}
