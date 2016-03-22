
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

import java.sql.Connection;

/**
 * TODO: Remove this class as it can be replaced by a method.
 */
public abstract class FetchResultFactory {

    /**
     * Create a FetchResult.
     */
    public abstract FetchResult createFetchResult(Connection con);
}
