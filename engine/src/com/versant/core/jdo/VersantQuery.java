
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

import javax.jdo.Query;

/**
 * <p>This interface provides additional Open Access specific query properties.
 * There are two ways to use these properties in your applications:</p>
 * <ol>
 * <li>Cast the Query returned by PersistenceManager.newQuery(...) to a
 * VersantQuery and call the setXXX methods. This is clear in code
 * but non-portable to other JDO implementations.
 * <li>Add a 'String versantOptions' parameter to the query and specify a
 * semicolon delimited String of property=value pairs when the query is
 * executed. Portability is maintained as other JDO implementations should
 * ignore this parameter.
 * </ol>
 * <p/>
 * <p>Example using casting:</p>
 * <pre>
 * VersantQuery q = (VersantQuery)pm.newQuery(Item.class);
 * q.setFetchGroup("codeOnly");
 * q.setRandomAccess(true);
 * Collection ans = (Collection)q.execute();
 * ...
 * </pre>
 * <p/>
 * <p>Example using versantOptions parameter:</p>
 * <pre>
 * Query q = pm.newQuery(Item.class);
 * q.declareParameters("String versantOptions");
 * Collection ans = (Collection)q.execute("fetchGroup=codeOnly;randomAccess=true");
 * ...
 * </pre>
 */
public interface VersantQuery extends Query {

    public static final String VERSANT_OPTIONS = "versantOptions";
    /**
     * Use {@link #VERSANT_OPTIONS} instead.
     */
    public static final String JDO_GENIE_OPTIONS = "jdoGenieOptions";

    /**
     * Select the fetch group used to execute the query. JDO Genie fetch groups
     * control exactly which fields are returned in each instance. They
     * also make it possible to fetch other referenced instances and
     * collections at the same time i.e. you can prefetch a large part
     * of your object graph with a single query.
     */
    public void setFetchGroup(String fetchGroupName);

    public String getFetchGroup();

    /**
     * Indicate that random access to query results is required or not. If this
     * is true then the collection returned by execute can be cast to a
     * List and the get(index) method can be used to get any entry in the list.
     * JDO Genie must use a scrollable JDBC ResultSet to provide this
     * functionality. This may use more database resources (cursors etc.)
     * than a normal forward only ResultSet. This option is useful for paged
     * results i.e. you only want a few results from position n onwards.
     */
    public void setRandomAccess(boolean on);

    public boolean isRandomAccess();

    /**
     * Limit the number of instances to be returned. If this property has
     * been set and {@link #setFetchSize} is not set then the batchSize
     * is set to maxRows.
     *
     * @see #setFetchSize
     * @see #getMaxRows
     */
    public void setMaxRows(int amount);

    /**
     * The maximum number of instances to return.
     *
     * @see #setMaxRows
     */
    public int getMaxRows();

    /**
     * Set the number of instances fetched per server round trip. This
     * property controls JDO Genie's own batching and is also passed
     * through to JDBC drivers that support this. If this property is
     * not set and maxRows is set then the default is maxRows.
     *
     * @see #getFetchSize
     */
    public void setFetchSize(int value);

    /**
     * The number of instances fetched from server per round trip.
     *
     * @see #setFetchSize
     */
    public int getFetchSize();

    /**
     * <p>Normally when size() is called on the Collection returned by executing
     * a non-randomAccess Query all of the results are fetched in one operation
     * to detirmine the size of the collection.  If this property is true then
     * a 'select count(*) ...' version of the query is used to count the
     * results. Subsequent calls to to size() after the first call will revert
     * to normal behaviour and resolve all of the results.</p>
     * <p/>
     * <p>Note that the actual number of results might differ to those first
     * returned by size() when this option is used. This can happen if new
     * rows that meet the filter criteria are inserted after the
     * 'select count(*)...' query has run but before the normal 'select ...'
     * to fetch the data. This may be possible even in a non-optimistic
     * transaction depending on how the database handles locking.</p>
     *
     * @see #setRandomAccess(boolean)
     * @see #isCountStarOnSize()
     */
    public void setCountStarOnSize(boolean on);

    /**
     * Has the count(*) option been set?
     *
     * @see #setCountStarOnSize(boolean)
     */
    public boolean isCountStarOnSize();

    /**
     * This property is a hint to JDO Genie that the number of instances
     * returned by the query us limited. If it is true then collections and
     * maps are fetched in bulk using parallel queries derived from the
     * orginal filter expression. If it is false then individual queries are
     * issued for each collection or map for each instance in the result.
     * The default setting is false.
     */
    public void setBounded(boolean value);

    /**
     * Has the bounded option been set?
     *
     * @see #setBounded(boolean)
     */
    public boolean isBounded();

    /**
     * Get the query plan for this query. This will include the SQL and
     * possibly also a query plan for the SQL from the database itself.
     * The params are as for executeWithArray.
     *
     * @see #executeWithArray
     */
    public VersantQueryPlan getPlan(Object[] params);

    /**
     * <p>Register classes that will cause the cached results of this query to
     * be evicted if any of its instances are modified. These replace any
     * previously set classes. JDO Genie will automatically pickup the
     * candidate class and classes involved in the filter or ordering. You
     * only need to call this method if you are using inline SQL to touch
     * tables for classes not otherwise involved in the query. Here is an
     * example:</p>
     * <p/>
     * <code>Query q = pm.newQuery(Order.class);<br>
     * String aclCheck = "$1 in (select $1 from acl where user_id = " + userId + ")";<br>
     * q.setFilter("owner.sql(\"" + aclCheck + "\")");<br>
     * ((VersantQuery)q).setEvictionClasses(new[]{Acl.class}, true);<br>
     * // make sure query result is evicted if acl table(class) changes<br>
     * ...<br></code>
     *
     * @param includeSubclasses Recursively add subclasses (if any)
     * @see #setEvictionClasses(int[])
     */
    public void setEvictionClasses(Class[] classes, boolean includeSubclasses);

    /**
     * Register the indexes of classes that will cause the cached results of
     * this query to be evicted if any of its instances are modified. This
     * performs the same function as the method accepting a Class[] but
     * is faster as the index for each class does not have to be found.
     *
     * @see #setEvictionClasses(Class[], boolean)
     * @see com.versant.core.jdo.VersantPersistenceManagerFactory#getClassIndexes(Class[], boolean)
     */
    public void setEvictionClasses(int[] classIndexes);

    /**
     * Get the registered eviction classes. This does not return classes
     * automatically picked up by JDO Genie (e.g. the candidate class). This
     * may return null if there are no registered eviction classes.
     *
     * @see #setEvictionClasses(Class[], boolean)
     * @see #setEvictionClasses(int[])
     */
    public Class[] getEvictionClasses();

    /**
     * The projection to use.
     */
    public void setResult(String result);

    /**
     * Grouping exp to use.
     * This is used in conjunction with projection queries.
     *
     * @see #setResult(java.lang.String)
     */
    public void setGrouping(String grouping);

    /**
     * Specify that there is a single result of the query.
     */
    public void setUnique(boolean unique);

    /**
     * Get the filter for the query.
     */
    public String getFilter();

    /**
     * Can the results of the query go into the level 2 cache or come from the
     * level 2 cache? If this property is set then it overrides the default
     * decision. Normally JDOQL query results are added to the level 2 cache
     * if all classes involved have a cache strategy of yes or all. SQL query
     * results are not normally cached.
     * <p/>
     * You might want to use this for a large JDOQL query if you know that
     * caching the results will not benefit the application. Or you could
     * use it to cache the results of an SQL query.
     *
     * @see #setEvictionClasses(int[])
     */
    public void setCacheable(boolean on);

    /**
     * Get the query imports.
     */
    public String getImports();

    /**
     * Get the query parameter declarations.
     */
    public String getParameters();

    /**
     * Get the query variable declarations.
     */
    public String getVariables();

    /**
     * Get the query ordering expression.
     */
    public String getOrdering();

    /**
     * Get the query grouping expression.
     */
    public String getGrouping();

    /**
     * Get the query result expression.
     */
    public String getResult();

    /**
     * Has the unique flag been set?
     */
    public boolean isUnique();

}
