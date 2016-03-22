
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
package com.versant.core.jdo.sco;

import java.io.Serializable;

/**
 * This holds the keys and values pairs for a map.
 * Keys and values paired on index.
 */
public class MapData implements Serializable {

    /**
     * The number of entry pairs in keys and values.
     */
    public int entryCount;
    public Object[] keys;
    public Object[] values;
}
