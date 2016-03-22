
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

import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdo.QueryDetails;

/**
 * Hacked version of JdbcCompiledQuery to suite the new FetchSpec query
 * processing. The old stuff needs to be refactored so everything uses
 * FetchSpec etc. This is a hack so we can get EJBQL into our implementation.
 */
public class JdbcCompiledQueryEJBQL extends JdbcCompiledQuery {

    public JdbcCompiledQueryEJBQL(ClassMetaData cmd, QueryDetails queryParams,
            FetchSpec fetchSpec) {
        super(cmd, queryParams);
        super.setFetchSpec(fetchSpec);
        unique = QueryDetails.FALSE;
    }
}

