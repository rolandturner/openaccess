
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
package com.versant.core.server;

import com.versant.core.common.SortableBase;
import com.versant.core.common.OID;
import com.versant.core.common.OID;
import com.versant.core.common.SortableBase;

/**
 * Base class for graphs of OIDs and States.
 */
public abstract class OIDGraph extends SortableBase {

    /**
     * Find the index of OID in the graph or less than 0 if not found.
     */
    public abstract int indexOf(OID oid);

}
