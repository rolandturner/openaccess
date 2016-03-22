
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
package com.versant.core.jdo.query.mem;

import com.versant.core.jdo.QueryStateWrapper;

/**
 * This is the interface that the dynamic queries will implement.
 */
public abstract class BCodeQuery {
    public boolean exec(QueryStateWrapper state, Object[] params) {
        return true;
    }

    public int compare(QueryStateWrapper state1, QueryStateWrapper state2) {
        return 0;
    }
}
