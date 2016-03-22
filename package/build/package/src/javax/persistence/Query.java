
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
package javax.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Interface used to control query execution.
 */
public interface Query {

    /**
     * Execute the query and return the query results as a List.
     *
     * @return List containing the query results
     */
    public List getResultList();

    /**
     * Execute a query that returns a single result.
     *
     * @return The result object
     * @throws EntityNotFoundException  if there is no result
     * @throws NonUniqueResultException if more than one result
     */
    public Object getSingleResult();

    /**
     * Execute an update or delete statement.
     *
     * @return Update or delete row count
     */
    public int executeUpdate();

    /**
     * Set the maximum number of results to retrieve.
     *
     * @param maxResult The maximum number of results to return
     * @return The same query instance
     */
    public Query setMaxResults(int maxResult);

    /**
     * Set the position of the first result to retrieve.
     *
     * @param startPosition The position of the first result, numbered from 0
     * @return The same query instance
     */
    public Query setFirstResult(int startPosition);

    /**
     * Set an implementation-specific hint.
     *
     * @param hintName A vendor-specific key to indicate the hint
     * @param value    A vendor-specific value appropriate for the specified hint key
     * @return The same query instance
     */
    public Query setHint(String hintName, Object value);

    /**
     * Bind an argument to a named parameter.
     *
     * @param name  The parameter name
     * @param value The value to bind to the parameter name
     * @return The same query instance
     */
    public Query setParameter(String name, Object value);

    /**
     * Bind an instance of java.util.Date to a named parameter.
     *
     * @param name         The parameter name
     * @param value        The Date value to bind to the parameter name
     * @param temporalType The specific part of the date to use
     * @return The same query instance
     */
    public Query setParameter(String name, Date value, TemporalType temporalType);

    /**
     * Bind an instance of java.util.Calendar to a named parameter.
     *
     * @param name         The parameter name
     * @param value        The Calendar value to bind to the parameter name
     * @param temporalType The specific part of the Calendar to use
     * @return The same query instance
     */
    public Query setParameter(String name, Calendar value, TemporalType temporalType);

    /**
     * Bind an argument to a positional parameter.
     *
     * @param position The parameter position (numbered from 0)
     * @param value    The value to bind to the parameter position
     * @return The same query instance
     */
    public Query setParameter(int position, Object value);

    /**
     * Bind an instance of java.util.Date to a positional parameter.
     *
     * @param position     The parameter position (numbered from 0)
     * @param value        The Date value to bind to the parameter position
     * @param temporalType The specific part of the date to use
     * @return The same query instance
     */
    public Query setParameter(int position, Date value, TemporalType temporalType);

    /**
     * Bind an instance of java.util.Calendar to a positional parameter.
     *
     * @param position     The parameter position (numbered from 0)
     * @param value        The Calendar value to bind to the parameter position
     * @param temporalType The specific part of the calendar to use
     * @return The same query instance
     */
    public Query setParameter(int position, Calendar value, TemporalType temporalType);

    /**
     * Set the flush mode type to be used for the query execution.
     *
     * @param flushMode
     */
    public Query setFlushMode(FlushModeType flushMode);

}
