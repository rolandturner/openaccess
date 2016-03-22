
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
 * This holds the values for a Collection.
 * If the collection is ordered values should by in the correct order.
 */
public class CollectionData implements Serializable {

    /**
     * The number of entries in values.
     */
    public int valueCount;

    /**
     * The values for a collection. This should be collectly ordered for
     * ordered collections.
     */
    public Object[] values;
}
