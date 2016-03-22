
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
package com.versant.core.common;

import java.io.Serializable;

/**
 * This holds the keys and values for a map. It is stored in the State
 * when a map field is read by the store.
 */
public class MapEntries implements Serializable {

    public MapEntries(Object[] keys, Object[] values) {
        this.keys = keys;
        this.values = values;
    }

    /**
     * The keys. This array will be the same size as values.
     */
    public final Object[] keys;

    /**
     * The values. This array will be the same size as keys.
     */
    public final Object[] values;

}
