
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

import com.versant.core.jdo.QueryDetails;

/**
 * A query compiled from a QueryDetails instance.
 */
public interface CompiledQuery {

    /**
     * Get the unique ID assigned to this CompiledQuery.
     */
    public int getId();

    /**
     * Give this query an ID. This is used by {@link CompiledQueryCache}
     * to assign unique IDs to CompiledQuery's.
     */
    public void setId(int id);

    /**
     * Return the QueryDetails this query was compiled from. This method
     * may return null if this information is not available.
     */
    public QueryDetails getQueryDetails(); 

    /**
     * Return bitmapped array of the class indexes involved in this query in
     * some way. that will cause the results
     * of this query to be evicted when their instances are modified. Each
     * class index has one bit in this array.
     */
    public int[] getEvictionClassBits();

    /**
     * Get the indexes of all of the classes involved in some way. This is
     * used to check query cache eviction and to trigger flushing.
     */
    public int[] getClassIndexes();

    /**
     * If this is a query with a single/unique result.
     */
    public boolean isUnique();

    /**
     * Is this a non default projection query.
     * This will return false for the default projection.
     */
    public boolean isProjectionQuery();

    /**
     * If this query returns default results. ie The result will be a collection
     * of managed instances.
     */
    public boolean isDefaultResult();

    /**
     * The index of the first occurance of 'this' in the projection. If not then
     * return -1.
     */
    public int getFirstThisIndex();

    /**
     * The typeCode of each column or null if this is not a projection query.
     *
     * @see com.versant.core.metadata.MDStatics
     */
    public int[] getResultTypeCodes();

    /**
     * Return the identifiers of the paramaters as used in the query. This
     * is used if the query is executed with mapped parameters.
     */
    String[] getParamIdentifiers();

}

