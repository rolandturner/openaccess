
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
package com.versant.core.jdbc.query;

import com.versant.core.jdo.QueryDetails;
import com.versant.core.metadata.*;
import com.versant.core.jdo.query.GroupingNode;
import com.versant.core.server.CompiledQuery;
import com.versant.core.jdbc.ProjectionQueryDecoder;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.jdbc.fetch.SqlBuffer;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcRefField;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.util.CharBuf;
import com.versant.core.common.Debug;

import java.sql.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a QueryImp compiled by a JdbcDataStore and ready to run.
 * It contains the SQL and enough information to convert the ResultSet
 * into State's. There is not much logic to avoid referencing server side
 * classes as a client application may serialize a query containing one of
 * these and re-associate it with a PM later.
 */
public class JdbcCompiledQuery implements CompiledQuery {

    public static final int PARAM_IN = 0;//default
    public static final int PARAM_OUT = 1;
    public static final int PARAM_OUT_CURSOR = 2;

    private int id;
    private final QueryDetails qp;

//    private Map parCollSqlStrucMap = new HashMap();

    /**
     * The class index of the candidate class of the query.
     */
    private int classIndex;
    /**
     * The cls index's of all the filter classes.
     */
    private ClassMetaData[] filterCmds;
    /**
     * Bitmapped array of the class indexes that will cause the results
     * of this query to be evicted when their instances are modified. Each
     * class index has one bit in this array.
     */
    private int[] evictionClassBits;
    private int[] evictionClassIndexes;
    /**
     * If the results of the query is cacheble.
     */
    private boolean cacheble;
    /**
     * Must subclasses be included in the result?
     */
    private boolean includeSubclasses;
    /**
     * The root fetch group for the query. This will be from cmd. This
     * will be the default fetch group unless the user specified a
     * different group.
     */
    private int fetchGroupIndex = 0;
    /**
     * Must this query return results suitable for random access?
     */
    private boolean randomAccess;
    /**
     * The max amount of rows to return for this query.
     */
    private int maxRows;
    /**
     * This is the amount of data that will be prefetched per each round trip
     * to the server.
     */
    private int queryResultBatchSize;

    private SqlBuffer sqlBuffer;

    private boolean parColFetchEnabled;
    /**
     * This is the group by exp for the aggregate expression.
     * This may only be non-null if result was specified and it contains aggregates.
     */
    private GroupingNode groupByNode;
    protected int unique;
    private int selectColumnCount;
    private boolean copyResultsForCache;
    private boolean sqlQuery;
    private boolean storeProc;
    private boolean directSql;
    private MappingInfo mappingInfo;
    private ClassMetaData cmd;
    private int[] sqlTypes;
    /**
     * If this is a 'in', 'out' or a 'inout' type
     */
    private int[] paramDirection;
    private int outParamCount;

    private boolean oneToManyJoinAllowed;
    private ProjectionQueryDecoder projectionDecoder;
    private FetchSpec fetchSpec;

    public JdbcCompiledQuery(ClassMetaData cmd, QueryDetails queryParams) {
        this.cmd = cmd;
        if (cmd != null) {
            this.classIndex = cmd.index;
            this.includeSubclasses = queryParams.includeSubClasses() && cmd.isInHierarchy();
        } else {
            this.classIndex = -1;
        }
        if (queryParams.getLanguage() == QueryDetails.LANGUAGE_SQL) {
            sqlQuery = true;
            if (queryParams.getFilter() == null || queryParams.getFilter().trim().length() == 0) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Must supply a valid filter for a 'SqlQeury'");    
            }
            if (queryParams.getFilter().toUpperCase().startsWith("SELECT")) {
                storeProc = false;
                directSql = true;
            } else {
                storeProc = true;
                directSql = false;
            }
        }

        this.fetchGroupIndex = queryParams.getFetchGroupIndex();
        this.randomAccess = queryParams.isRandomAccess();
        this.maxRows = queryParams.getMaxResultCount();
        this.queryResultBatchSize = queryParams.getResultBatchSize();
        if (Debug.DEBUG) {
            if (queryResultBatchSize <= 0) {
                throw BindingSupportImpl.getInstance().internal(
                        "The queryDetails.resultBatchSize is not set");
            }
        }
        this.qp = queryParams;

        if (!sqlQuery) {
            this.sqlBuffer = new SqlBuffer();

				parColFetchEnabled = QueryDetails.enableParallelCollectionFetch(qp,
                    cmd.fetchGroups[qp.getFetchGroupIndex()]);
        } else {
            unique = QueryDetails.FALSE;
            //process the param types.
            String[] types = queryParams.getParamTypes();
            int count  = queryParams.getParamCount();
            sqlTypes = new int[count];
            paramDirection = new int[count];
            for (int i = 0; i < count; i++) {
                String type = types[i].toUpperCase();
                if (type.startsWith("OUT.")) {
                    outParamCount++;
                    if (type.equals("OUT.CURSOR")) {
                        //the rs
                        paramDirection[i] = PARAM_OUT_CURSOR;
                    } else {
                        //change unique to true as we will create an Object[]
                        unique = QueryDetails.TRUE;
                        //single value
                        paramDirection[i] = PARAM_OUT;
                        sqlTypes[i] = getTypeInt(type.substring(type.indexOf(".") + 1));
                    }
                } else {
                    sqlTypes[i] = getTypeInt(types[i]);
                }
            }
        }
    }

    public ClassMetaData getCmd() {
        return cmd;
    }

    public void setCmd(ClassMetaData cmd) {
        this.cmd = cmd;
    }

    public int getOutParamCount() {
        return outParamCount;
    }

    public int[] getSqlTypes() {
        return sqlTypes;
    }

    public int[] getParamDirection() {
        return paramDirection;
    }

    private int getTypeInt(String val) {
        try {
            return Types.class.getDeclaredField(val.toUpperCase()).getInt(null);
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal("Param type '" + val
                    + "' is not a valid " + Types.class.getName() + " type.");
        }
    }

    /**
     * TODO: pre-compute this value.
     */
    public String[] getParamIdentifiers() {
        SqlBuffer.Param p = sqlBuffer.getParamList();
        if (p == null) return null;
        List paramIdents = new ArrayList(5);
        while(p != null) {
            paramIdents.add(p.getIdentifier());
            p = p.next;
        }
        String[] result = new String[paramIdents.size()];
        paramIdents.toArray(result);
        return result;
    }

    public boolean isSqlQuery() {
        return sqlQuery;
    }

    public int getSelectColumnCount() {
        return selectColumnCount;
    }

    public void setSelectColumnCount(int selectColumnCount) {
        this.selectColumnCount = selectColumnCount;
    }

    /**
     * This may only be called once the resultnode and the groupBy node has been processed and set.
     */
    public void process() {
        if (qp.getUnique() == QueryDetails.TRUE) {
            unique = QueryDetails.TRUE;
        } else if (qp.getUnique() == QueryDetails.FALSE) {
            unique = QueryDetails.FALSE;
        } else {
            if (projectionDecoder != null) {
                if (projectionDecoder.containsAggregate()) {
                    if (groupByNode == null) {
                        if (!projectionDecoder.aggregateOnly()) {
                            unique = QueryDetails.FALSE;
                        } else {
                            unique = QueryDetails.TRUE;
                        }
                    } else {
                        if (projectionDecoder.aggregateOnly()) {
                            unique = QueryDetails.TRUE;
                        } else {
                            unique = QueryDetails.FALSE;
                        }
                    }
                } else {
                    unique = QueryDetails.FALSE;
                    if (groupByNode != null) {
                        throw BindingSupportImpl.getInstance().invalidOperation("The query contains a 'Group By' " +
                                "expression but no aggregate's.");
                    }
                }
            } else {
                unique = QueryDetails.FALSE;
            }
        }

        copyResultsForCache = (projectionDecoder != null
                && (projectionDecoder.containsThis() || projectionDecoder.getRefIndexArray().length > 0));

        sqlBuffer.setAggregate(
                projectionDecoder != null && projectionDecoder.containsAggregate());

        oneToManyJoinAllowed = (projectionDecoder == null)
                && parColFetchEnabled 
                && !sqlQuery
                && !qp.isRandomAccess()
                && (qp.getMaxResultCount() <= 0);
    }

    /**
     * If this is a query with a single/unique result.
     */
    public boolean isUnique() {
        if (unique == QueryDetails.NOT_SET) {
            throw BindingSupportImpl.getInstance().internal(
                    "The 'unique' value has not been processed.");
        }
        if (unique == QueryDetails.TRUE) return true;
        return false;
    }

    public boolean isProjectionQuery() {
        return (projectionDecoder != null);
    }

    public int getFirstThisIndex() {
        if (projectionDecoder == null) return -1;
        return projectionDecoder.getFirstThisIndex();
    }

    /**
     * If this is a result/projection that only contains 'this' and no other
     * fields in the projection.
     */
    public boolean isContainsThisOnly() {
        return projectionDecoder.isContainsThisOnly();
    }

    /**
     * If this is a result/projection that only contains 'this' and no other
     * fields in the projection.
     */
    public boolean isContainsThis() {
        if (projectionDecoder == null) return false;
        return projectionDecoder.containsThis();
    }

    /**
     * If the results of the query should be copied for caching.
     * This should only happen for non-default type projection queries that
     * contains references.
     * <p/>
     * If this is a projection that only specifies 'this' then this should
     * also return false.
     */
    public boolean isCopyResultsForCache() {
        return copyResultsForCache;
    }

    /**
     * If this query returns default results.
     */
    public boolean isDefaultResult() {
        if (sqlQuery) {
            return cmd != null;
        }
        if (projectionDecoder == null) return true;
        return projectionDecoder.isContainsThisOnly();
    }

    /**
     * Array containing the index pos of ref fields of the projection.
     * Return null if no ref fields in projection.
     */
    public int[] getRefIndexArray() {
        if (projectionDecoder == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "This may only be called on 'projection queries'");
        }
        return projectionDecoder.getRefIndexArray();
    }

    public int[] getResultTypeCodes() {
        return projectionDecoder == null ? null : projectionDecoder.getTypeCodes();
    }

    public void setGroupingNode(GroupingNode groupingNode) {
        groupByNode = groupingNode;
    }

    public boolean isParColFetchEnabled() {
        return parColFetchEnabled;
    }

    public SqlBuffer getSqlBuffer() {
        return sqlBuffer;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getQueryResultBatchSize() {
        return queryResultBatchSize;
    }

    public ClassMetaData[] getQueryClasses() {
        return filterCmds;
    }

    public void setFilterClsIndexs(ClassMetaData[] filterClsIndexs) {
        this.filterCmds = filterClsIndexs;
    }

    public int[] getEvictionClassBits() {
        return evictionClassBits;
    }

    public int[] getClassIndexes() {
        return evictionClassIndexes;
    }

    public QueryDetails getQueryDetails() {
        return qp;
    }

    public void setEvictionClassBits(int[] evictionClassBits) {
        this.evictionClassBits = evictionClassBits;
    }

    public void setEvictionClassIndexes(int[] evictionClassIndexes) {
        this.evictionClassIndexes = evictionClassIndexes;
    }

    public boolean isCacheble() {
        return cacheble;
    }

    public void setCacheable(boolean cacheble) {
        this.cacheble = cacheble;
    }

    public String getFirstTableOrAlias() {
        return sqlBuffer.getFirstTableOrAlias();
    }

    public void setFirstTableOrAlias(String firstTableOrAlias) {
        sqlBuffer.setFirstTableOrAlias(firstTableOrAlias);
    }

    public boolean isDistinct() {
        return sqlBuffer.isDistinct();
    }

    /**
     * Get the SQL query text.
     */
    public String getSql(SqlDriver driver, Object[] params,
            boolean forUpdate, boolean forCount, long fromIncl, long toExcl) {
        return sqlBuffer.getSql(driver, params, forUpdate, forCount, fromIncl,
                toExcl);
    }

    public int getClassIndex() {
        return classIndex;
    }

    public boolean isIncludeSubclasses() {
        return includeSubclasses;
    }

    public int getFetchGroupIndex() {
        return fetchGroupIndex;
    }

    public FetchGroup getFetchGroup() {
        return cmd.fetchGroups[fetchGroupIndex];
    }

    public boolean isRandomAccess() {
        return randomAccess;
    }

    public CharBuf getSqlbuf() {
        return sqlBuffer.getSqlbuf();
    }

    public SqlBuffer.Param getParamList() {
        return sqlBuffer.getParamList();
    }

    /**
     * Update all our Param's for the null/not null state of their parameters
     * and for 'select for update' or not. This may change the SQL query
     * string.
     */
    public void updateSql(SqlDriver driver, Object[] params, boolean forUpdate,
            boolean forCount) {
        throw new RuntimeException("Must be removed");
//        sqlStruct.updateSql(driver, params, forUpdate, forCount);
    }

    /**
     * Set all the parameters for this query on ps. This is a NOP if params
     * is null.
     */
    public void setParamsOnPS(ModelMetaData jmd, SqlDriver driver,
            PreparedStatement ps, Object[] params, String sql)
            throws SQLException {
        sqlBuffer.setParamsOnPS(jmd, driver, ps, params, sql);
    }

    public MappingInfo getMappingInfo(ResultSet rs) throws SQLException {
        if (mappingInfo == null) {
            mappingInfo = createMappingInfo(rs.getMetaData(), cmd);
        }
        return mappingInfo;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof JdbcCompiledQuery) {
            return qp.equals(((JdbcCompiledQuery)obj).qp);
        }
        return false;
    }

    public int hashCode() {
        return qp.hashCode();
    }

    public static MappingInfo createMappingInfo(ResultSetMetaData rsmd, ClassMetaData cmd) throws SQLException {
        final int count = rsmd.getColumnCount();
        MappingInfo mi = new MappingInfo();
        mi.colCount = count;
        if (cmd == null) return mi;
        mi.cmd = cmd;

        JdbcColumn[] pkCols = ((JdbcClass)cmd.storeClass).table.pk;
        JdbcColumn discr = ((JdbcClass)cmd.storeClass).classIdCol;

        /**
         * Look for discriminator
         */
        if (discr != null) {
            //get the discriminator col
            for (int i = 1; i <= count; ) {
                if (discr.name.toUpperCase().equals(rsmd.getColumnName(i).toUpperCase())) {
                    mi.discrIndex = i++;
                    break;
                }
                i++;
            }
        }

        final boolean appId = cmd.pkFields != null;
        Set fieldList = new java.util.HashSet();
        JdbcField[] jdbcFields = mi.fields = new JdbcField[count];
        int[] pkindexes = null;
        if (appId) {
            pkindexes = new int[pkCols.length];
        }

        for (int i = 1; i <= count;) {
            String colName = rsmd.getColumnName(i).toUpperCase();

            //skip the already found discriminator col
            if (i == mi.discrIndex) {
                i++;
                continue;
            }

//            if (mi.pkIndex == -1 && pkCols[0].name.toUpperCase().equals(colName)) {
//                //assume the same ordering
//                boolean ok = true;
//                for (int j = 1; j < pkCols.length; j++) {
//                    if (!pkCols[j].name.toUpperCase().equals(rsmd.getColumnName(i + j).toUpperCase())) {
//                        ok = false;
//                        break;
//                    }
//                }
//
//                if (ok) {
//                    mi.pkIndex = i;
//                    i += pkCols.length;
//                    continue;
//                }
//            }


            if (mi.dsPkIndex == -1 && !appId && pkCols[0].name.toUpperCase().equals(colName)) {
                //assume the same ordering
                mi.dsPkIndex = i;
                i++;
                continue;
            }

            JdbcField field = getSubClassField(colName, cmd, mi.discrIndex != 0);
//            if (field == null) {
//                refFieldToJdbcField.clear();
//                field = getRefField(colName, cmd, true, refFieldToJdbcField);
//                System.out.println("refField = " + refFieldToJdbcField.fromField);
//                if (field != null) {
//                    System.out.println("found ref field: " + field);
//                }
//            }

            if (field != null) {
                if (fieldList.contains(field)) {
                    //ignore
                    System.out.println("Ignoring column '"
                                    + colName + "' at index '"
                                    + i + "' because this column has already been mapped to '"
                                    + field.fmd.classMetaData.qname + "." + field.fmd.name + "'");
                    i++;
                    continue;
                } else {
                    if (field.mainTableCols.length == 1) {
                        fieldList.add(field);
                        jdbcFields[i - 1] = field;
                        i++;
                        continue;
                    } else {
                        /**
                         * This field is mapped to more that one column in the db
                         * so we must check to see that the next n cols will also resolve to this
                         * field. If not the skip the current col
                         */
                        int endIndex = i + field.mainTableCols.length;
                        boolean ok = false;
                        for (int j = i + 1; j < endIndex; j++) {
                            if (((JdbcClass)cmd.storeClass).getColNamesToJdbcField().get(colName) != field) {
                                //not the current field so skip this col and continue
                                break;
                            }
                        }

                        if (ok) {
                            /**
                             * all the cols matched for the current field so accept it. Update the index
                             * to the endindex.
                             */
                            fieldList.add(field);
                            jdbcFields[i - 1] = field;
                            i = endIndex;
                        } else {
                            /**
                             * Skip the current col
                             */
                            i++;
                        }
                        continue;
                    }
                }
            } else {
                i++;
                //not in this class
            }
        }

        if (cmd.pkFields != null) {
            //flag to check that all pk fields are found in the jdbcfields array
            boolean keyFound = false;
            for (int i = 0; i < cmd.pkFields.length; i++) {
                FieldMetaData pkField = cmd.pkFields[i];
                keyFound = false;
                for (int j = 0; j < jdbcFields.length; j++) {
                    JdbcField jdbcField = jdbcFields[j];
                    if (pkField.storeField == jdbcField) {
                        keyFound = true;
                        pkindexes[i] = j;
                        break;
                    }
                }
                //if any of the keys is not found then break.
                if (!keyFound) {
                    break;
                }
            }

            if (Debug.DEBUG) {
                if (keyFound) {
                    for (int i = 0; i < cmd.pkFields.length; i++) {
                        FieldMetaData pkField = cmd.pkFields[i];
                        if (pkField.storeField != jdbcFields[pkindexes[i]]) {
                            throw BindingSupportImpl.getInstance().internal("pk field mismatch");
                        }
                    }
                }
            }
            if (keyFound) {
                mi.pkIndexInFieldsArray = pkindexes;
            }
        }
//        if (mi.pkIndex == -1) {
//            throw BindingSupportImpl.getInstance().invalidOperation("No columns in the result set " +
//                    "could be mapped to the pk fields");
//        }
        return mi;
    }

    /**
     * Do a breadth first recursive traversal of subclasses to find a JdbcField with a column name.
     */
    private static JdbcField getSubClassField(String colName, ClassMetaData cmd, boolean checkSubClasses) {
        if (!checkSubClasses) {
            return (JdbcField)((JdbcClass)cmd.storeClass).getColNamesToJdbcField().get(colName);
        } else {
            List subsList = cmd.getHeirarchyList();
            for (int i = 0; i < subsList.size(); i++) {
                ClassMetaData icmd = (ClassMetaData) subsList.get(i);
                JdbcField field =
                        (JdbcField)((JdbcClass)icmd.storeClass).getColNamesToJdbcField().get(colName);
                if (field != null) return field;
            }
        }
        return null;
    }

    public boolean isStoredProc() {
        return storeProc;
    }

    public boolean isDirectSql() {
        return directSql;
    }

    public boolean isOneToManyJoinAllowed() {
        return oneToManyJoinAllowed;
    }

    public ProjectionQueryDecoder getProjectionDecoder() {
        return projectionDecoder;
    }

    public void setProjectionDecoder(ProjectionQueryDecoder decoder) {
        this.projectionDecoder = decoder;
    }

    /**
     * Todo get rid of this horrible hack when we refactor all the query stuff
     */
    public boolean isEJBQLHack() {
        return false;
    }

    public FetchSpec getFetchSpec() {
        return fetchSpec;
    }

    public FetchSpec setFetchSpec(FetchSpec fetchSpec) {
        this.fetchSpec = fetchSpec;
        return fetchSpec;
    }



    /**
     * Utils struct that is used when building mapping info for
     * sql queries.
     */
    public static class RefFieldMapping {
        public JdbcRefField fromField;
        public JdbcField toField;

        public void clear() {
            fromField = null;
            toField = null;
        }
    }

    /**
     * Utils struct to hold the info wrt to mapping jdbc fields to a
     * 'sql' query.
     */
    public static class MappingInfo {
        public ClassMetaData cmd;
        /**
         * The index pos of where the pkCols start
         */
        public int dsPkIndex = -1;
        /**
         * The index pos of where the discr Cols is
         */
        public int discrIndex = 0;
        /**
         * These are only fields for this hierarchy. The position in the array determine
         * the index in rs shifted by 1.
         */
        public JdbcField[] fields;
        public int colCount;
        public boolean pkFieldsFound;
        public boolean discriminatorColFound;
        public int[] pkIndexInFieldsArray;

        public void dump() {
            System.out.println("MappingInfo.dump@" + System.identityHashCode(this));
            for (int i = 0; i < fields.length; i++) {
                JdbcField jdbcField = fields[i];
                if (jdbcField != null) {
                    System.out.println("jdbcField = " + jdbcField);
                }
            }
        }

        /**
         * If there is enough info to map(id , discriminator)
         */
        public boolean isPkValid() {
            return dsPkIndex != -1 || pkIndexInFieldsArray != null;
        }
    }

    public String toString() {
        return sqlBuffer == null ? "sqlStruct is null" : sqlBuffer.getSqlbuf().toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
