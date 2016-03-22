
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

import com.versant.core.jdbc.sql.exp.SelectExp;

/**
 * This is a factory for creating the filter expression for a query. It is used
 * by downstream fething of parallel collection fetchop's.
 */
public interface FilterExpFactory {
    /**
     * Create a filter exp as per the original for this query.
     * @return
     */
    SelectExp createFilterExp();
}
