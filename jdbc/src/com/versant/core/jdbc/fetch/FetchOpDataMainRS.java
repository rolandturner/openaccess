
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
package com.versant.core.jdbc.fetch;

import java.sql.ResultSet;

/**
 * This just returns the main ResultSet from the FetchData.
 */
public class FetchOpDataMainRS extends FetchOpData {

    public static final FetchOpDataMainRS INSTANCE = new FetchOpDataMainRS();

    public ResultSet getResultSet(FetchResult fetchResult) {
        return fetchResult.getResultSet();
    }

}
