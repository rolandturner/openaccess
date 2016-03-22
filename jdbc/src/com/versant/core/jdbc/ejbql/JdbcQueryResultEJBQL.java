
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
package com.versant.core.jdbc.ejbql;

import com.versant.core.jdbc.JdbcQueryResult;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.query.JdbcCompiledQuery;

/**
 * Hacked version of JdbcQueryResult to suite the new FetchSpec query
 * processing. The old stuff needs to be refactored so everything uses
 * FetchSpec etc. This is a hack so we can get EJBQL into our implementation.
 */
public class JdbcQueryResultEJBQL extends JdbcQueryResult {
    
    public JdbcQueryResultEJBQL(JdbcStorageManager sm, JdbcCompiledQuery cq,
            Object[] params, boolean cachable) {
        super(sm, cq, params, cachable);
    }
}

