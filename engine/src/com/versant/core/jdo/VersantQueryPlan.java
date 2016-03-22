
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
package com.versant.core.jdo;

import java.io.Serializable;

/** 
 * Execution plan for a query.
 */
public class VersantQueryPlan implements Serializable {

    private String datastoreQuery;
    private String datastorePlan;

    public VersantQueryPlan() {
    }

    /**
     * Get the generated datastore query language (e.g. SQL) for the query.
     * Note that this may depend on the values of the query parameters
     */
    public String getDatastoreQuery() {
        return datastoreQuery;
    }

    public void setDatastoreQuery(String datastoreQuery) {
        this.datastoreQuery = datastoreQuery;
    }

    /**
     * Get the datastore execution plan for the query. Note that this may
     * depend on the values of the query parameters. If this is not supported
     * for the target database then null is returned.
     */
    public String getDatastorePlan() {
        return datastorePlan;
    }

    public void setDatastorePlan(String datastorePlan) {
        this.datastorePlan = datastorePlan;
    }

}
