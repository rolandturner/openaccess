
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
package com.versant.core.jdbc.sql.exp;

import com.versant.core.jdbc.sql.SqlDriver;

/**
 * An expression with no children.
 */
public class LeafExp extends SqlExp {

    public LeafExp() {
    }

    public SqlExp createInstance() {
        return new LeafExp();
    }

    /**
     * Create an aliases for any subtables we may have.
     */
    public int createAlias(int index) {
        return index;
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        return null;
    }

}

