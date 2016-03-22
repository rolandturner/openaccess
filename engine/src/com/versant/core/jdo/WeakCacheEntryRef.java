
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

import com.versant.core.jdo.PMCacheEntryOwnerRef;
import com.versant.core.common.State;
import com.versant.core.jdo.PCStateMan;
import com.versant.core.jdo.PMCacheEntry;
import com.versant.core.jdo.*;
import com.versant.core.common.Debug;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

import com.versant.core.common.BindingSupportImpl;

/**
 * Keeps a Weak reference to the pm cache entry.

 * @see com.versant.core.jdo.LocalPMCache
 */
public class WeakCacheEntryRef extends WeakReference implements PMCacheEntryOwnerRef {
    private PMCacheEntry owner;

    public WeakCacheEntryRef(Object referent, ReferenceQueue q, PMCacheEntry owner) {
        super(referent, q);
        this.owner = owner;
    }

    public PMCacheEntry getOwner() {
        return owner;
    }
}
