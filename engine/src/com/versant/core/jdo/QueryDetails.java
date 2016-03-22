
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

import com.versant.core.common.Debug;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.JdoQuery;
import com.versant.core.util.CharBuf;
import com.versant.core.util.IntArray;

import javax.jdo.Extent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import com.versant.core.common.BindingSupportImpl;

/**
 * All the information required to compile a query.
 */
public final class QueryDetails
        implements Externalizable, ParamDeclarationParser.Handler {

    private static final int DEFAULT_BATCH_SIZE = 50;

    public static final int LANGUAGE_JDOQL = 1;
    public static final int LANGUAGE_SQL = 2;
    public static final int LANGUAGE_OQL = 3;
    public static final int LANGUAGE_EJBQL = 4;

    public static final int NOT_SET = 0;
    public static final int FALSE = 1;
    public static final int TRUE = 2;

    private int language;
    private Class candidateClass;
    private boolean subClasses = true;
    private transient Collection col;
    private String filter;
    private String imports;
    private String variables;
    private String ordering;
    private String result;
    private String grouping;
    private int unique;
    private boolean ignoreCache;
    private boolean useIgnoreCacheFromPM;

    private int paramCount;
    private String[] paramTypes;
    private String[] paramNames;
    // If the query parameters were declared with the jdoGenieOptions parameter
    // then this is its index in the parameter list.
    private int optionsParamIndex = -1;

    private int fetchGroupIndex;
    private boolean randomAccess;
    private boolean countOnSize;
    private boolean bounded;

    private int resultBatchSize = -1;
    private int maxResultCount = -1;

    private int[] extraEvictClasses; // indexes of extra eviction trigger classes
    private int cacheable;  // cache results override (NOT_SET, FALSE, TRUE)

    public QueryDetails() {
    }

    /**
     * Create from a query definition in the meta data.
     */
    public QueryDetails(ClassMetaData cmd, JdoQuery q) {
        if (q.sql != null) {
            language = LANGUAGE_SQL;
            filter = q.sql;
        } else {
            language = LANGUAGE_JDOQL;
            filter = q.filter;
        }
        candidateClass = cmd.cls;
        subClasses = q.includeSubclasses != MDStatics.FALSE;
        if (useIgnoreCacheFromPM = q.ignoreCache == MDStatics.NOT_SET) {
            ignoreCache = false;
        } else {
            ignoreCache = q.ignoreCache == MDStatics.TRUE;
        }
        imports = q.imports;
        variables = q.variables;
        ordering = q.ordering;
        result = q.result;
        grouping = q.grouping;
        unique = q.unique;
        if (q.extensions != null) {
            for (int i = 0; i < q.extensions.length; i++) {
                JdoExtension e = q.extensions[i];
                switch (e.key) {
                    case JdoExtensionKeys.FETCH_GROUP:
                        FetchGroup fg = cmd.getFetchGroup(e.getString());
                        if (fg == null) {
                            throw BindingSupportImpl.getInstance().runtime("Query fetch group " +
                                    "not found on " + cmd.qname + ": " + e +
                                    "\n" + e.getContext());
                        }
                        fetchGroupIndex = fg.index;
                        break;
                    case JdoExtensionKeys.RANDOM_ACCESS:
                        randomAccess = e.getBoolean();
                        break;
                    case JdoExtensionKeys.COUNT_STAR_ON_SIZE:
                        countOnSize = e.getBoolean();
                        break;
                    case JdoExtensionKeys.MAX_ROWS:
                        maxResultCount = e.getInt();
                        break;
                    case JdoExtensionKeys.FETCH_SIZE:
                        resultBatchSize = e.getInt();
                        break;
                    case JdoExtensionKeys.BOUNDED:
                        bounded = e.getBoolean();
                        break;
                    case JdoExtensionKeys.EVICTION_CLASSES:
                        extraEvictClasses = processEvictionClassesExt(cmd, e);
                        break;
                    case JdoExtensionKeys.CACHEABLE:
                        setCacheable(e.getBoolean());
                        break;
                    case JdoExtension.QUERY_PARAM_VALUES:
                        //ignore
                        break;
                    default:
                        throw createExtNotAllowed(e);
                }
            }
        }
        try {
            declareParameters(q.parameters);
        } catch (RuntimeException e) {
        	if( BindingSupportImpl.getInstance().isOwnException(e) )
        	{
            	throw BindingSupportImpl.getInstance().runtime("Invalid parameter declaration: " +
                    e + "\n" + q.getContext(), e);
            }
            else
            {
            	throw e;	
            }
        }
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getLanguageStr() {
        switch (language) {
            case LANGUAGE_JDOQL:    return "JDOQL";
            case LANGUAGE_SQL:      return "SQL";
            case LANGUAGE_OQL:      return "OQL";
            case LANGUAGE_EJBQL:    return "EJBQL";
        }
        return "UNKNOWN(" + language + ")";
    }

    public int getUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique ? TRUE : FALSE;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    private RuntimeException createExtNotAllowed(JdoExtension e) {
        return BindingSupportImpl.getInstance().runtime("Extension not allowed here: " +
                e + "\n" + e.getContext());
    }

    private int[] processEvictionClassesExt(ClassMetaData cmd,
            JdoExtension ext) {
        boolean includeSubclasses = ext.getBoolean();
        if (ext.nested == null) return null;
        IntArray ans = new IntArray();
        for (int i = 0; i < ext.nested.length; i++) {
            JdoExtension e = ext.nested[i];
            if (e.key != JdoExtensionKeys.CLASS) throw createExtNotAllowed(e);
            String name = e.getString();
            ClassMetaData target = cmd.jmd.getClassMetaData(cmd, name);
            if (target == null) {
                throw BindingSupportImpl.getInstance().runtime("Class does not exist or " +
                        "is not persistent: " + e + "\n" + e.getContext());
            }
            if (includeSubclasses) {
                addIndexesForHierarchy(target, ans);
            } else {
                ans.add(target.index);
            }
        }
        return ans.toArray();
    }

    private void addIndexesForHierarchy(ClassMetaData root, IntArray ans) {
        ans.add(root.index);
        if (root.pcSubclasses == null) return;
        for (int i = 0; i < root.pcSubclasses.length; i++) {
            addIndexesForHierarchy(root.pcSubclasses[i], ans);
        }
    }

    public boolean includeSubClasses() {
        return subClasses;
    }

    public void setSubClasses(boolean subClasses) {
        this.subClasses = subClasses;
    }

    public QueryDetails(QueryDetails qParams) {
        this.fillFrom(qParams);
    }

    public int getResultBatchSize() {
        return resultBatchSize;
    }

    /**
     * This will return true if the maxResults is smaller of equal to the batch
     * size.
     * <p/>
     * This method is used to determine if all the results can be fetched at once.
     *
     * @return
     */
    public boolean prefetchAll() {
        return maxResultCount == resultBatchSize;
    }

    public void setResultBatchSize(int value) {
        if (value <= 0) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The batch size must be greater than zero");
        }
        this.resultBatchSize = value;
    }

    public int getMaxResultCount() {
        return maxResultCount;
    }

    /**
     * Set the max results returned for this query.
     * Setting it to zero is the same as unsetting it(no limit).
     *
     * @param value
     */
    public void setMaxResultCount(int value) {
        if (value < 0) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The query max result count must be greater or equal to zero");
        }
        this.maxResultCount = value;
    }

    /**
     * This is called just before the query is executed. It updates the
     * batch and max result counts.
     */
    public void updateCounts() {
        if (resultBatchSize == -1) {
            if (maxResultCount == -1) {
                //nothing set by user
                resultBatchSize = DEFAULT_BATCH_SIZE;
            } else {
                //user only set the maxResults therefore update the batch size to
                //be the same
                resultBatchSize = maxResultCount;
            }
        } else {
            //the user did set the batch size
            //if the maxResults is set and it is smaller than the batch
            //then update the batch size to be the same as the max.
            if (maxResultCount != -1 && maxResultCount < resultBatchSize) {
                resultBatchSize = maxResultCount;
            }
        }
    }

    /**
     * Take into acount 'parallelCollectionFetch', 'randomAccess' and 'fg.canUseParallelFetch'
     * to determine if parallel collection fetch should happen.
     */
    public static boolean enableParallelCollectionFetch(QueryDetails qp,
            FetchGroup fg) {
        return qp.bounded && !qp.randomAccess && fg.canUseParallelFetch();
    }

    public boolean isBounded() {
        return bounded;
    }

    public void setBounded(boolean value) {
        bounded = value;
    }

    public Class getCandidateClass() {
        return candidateClass;
    }

    public void setCandidateClass(Class candidateClass) {
        this.candidateClass = candidateClass;
    }

    public void setExtent(Extent extent) {
        if (Debug.DEBUG) {
            if (extent == null) {
                throw BindingSupportImpl.getInstance().internal(
                        "Setting extent to null is not supported");
            }
        }
        candidateClass = extent.getCandidateClass();
        subClasses = extent.hasSubclasses();
        col = null;
    }

    public Collection getCol() {
        return col;
    }

    public void setCol(Collection col) {
        this.col = col;
        subClasses = true;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = checkString(filter);
    }

    private boolean checkString(String lString, String oString) {
        if (lString == null) {
            return oString == null;
        }
        return lString.equals(oString);
    }

    private final String checkString(String val) {
        if (val == null || val.equals("")) {
            return null;
        }
        return val;
    }

    public String getImports() {
        return imports;
    }

    public void setImports(String imports) {
        this.imports = checkString(imports);
    }

    public void declareParameters(String params) {
        optionsParamIndex = -1;
        paramTypes = null;
        paramNames = null;
        paramCount = 0;
        ParamDeclarationParser.parse(checkString(params), this);
    }

    public int hashCode() {
        int result;
        result = (candidateClass != null ? candidateClass.getName().hashCode() : 0);
        result = 29 * result + (filter != null ? filter.hashCode() : 0);
        result = 29 * result + (ordering != null ? ordering.hashCode() : 0);
        return result;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof QueryDetails) {
            QueryDetails other = (QueryDetails)o;
            // do not include useIgnoreCacheFromPM here
            return randomAccess == other.randomAccess
                    && countOnSize == other.countOnSize
                    && bounded == other.bounded
                    && fetchGroupIndex == other.fetchGroupIndex
                    && subClasses == other.subClasses
                    && maxResultCount == other.maxResultCount
                    && resultBatchSize == other.resultBatchSize
                    && ignoreCache == other.ignoreCache
                    && unique == other.unique
                    && language == other.language
                    && cacheable == other.cacheable
                    && checkString(filter, other.filter)
                    && checkString(result, other.result)
                    && checkString(grouping, other.grouping)
                    && checkCandidate(other)
                    && checkString(ordering, other.ordering)
                    && checkExtraEvictClasses(other);
        } else {
            return false;
        }
    }

    private boolean checkExtraEvictClasses(QueryDetails other) {
        if (extraEvictClasses != null) {
            int[] a = other.extraEvictClasses;
            if (a == null) return false;
            if (a.length != extraEvictClasses.length) return false;
            for (int i = a.length - 1; i >= 0; i--) {
                if (a[i] != extraEvictClasses[i]) return false;
            }
            return true;
        } else {
            return other.extraEvictClasses == null;
        }
    }

    private boolean checkCandidate(QueryDetails other) {
        if (candidateClass != null) {
            if (candidateClass != other.candidateClass) {
                return false;
            }
        } else {
            if (other.candidateClass != null) {
                return false;
            }
        }
        return true;
    }

    public void parameterParsed(int index, String type, String name) {
        if (VersantQuery.VERSANT_OPTIONS.equals(name)
                || VersantQuery.JDO_GENIE_OPTIONS.equals(name)) {
            if (optionsParamIndex >= 0) {
                throw BindingSupportImpl.getInstance().runtime(
                        "The " + VersantQuery.VERSANT_OPTIONS +
                        " parameter may only appear once in the parameter declarations");
            }
            optionsParamIndex = index;
        } else {
            if (paramTypes == null) {
                paramTypes = new String[3];
                paramNames = new String[3];
            } else if (paramCount == paramTypes.length) {
                int len = paramCount;
                int n = len * 2;
                String[] a = new String[n];
                System.arraycopy(paramTypes, 0, a, 0, len);
                paramTypes = a;
                a = new String[n];
                System.arraycopy(paramNames, 0, a, 0, len);
                paramNames = a;
            }
            paramTypes[paramCount] = type;
            paramNames[paramCount++] = name;
        }
    }

    /**
     * Get the number of parameters excluding the jdoGenieOptions parameter
     * (if any). This returns -1 if the number of parameters is not known
     * e.g. EJBQL query or single string JDOQL query.
     */
    public int getParamCount() {
        if (language == LANGUAGE_EJBQL) {
            return -1;
        } else {
            return paramCount;
        }
    }

    /**
     * Get the total number of parameters including the jdoGenieOptions parameter
     * (if any). This returns -1 if the number of parameters is not known
     * e.g. EJBQL query or single string JDOQL query.
     */
    public int getTotalParamCount() {
        if (language == LANGUAGE_EJBQL) {
            return -1;
        } else {
            if (optionsParamIndex >= 0) return paramCount + 1;
            return paramCount;
        }
    }

    /**
     * Get the parameter types or null if there are none. The length of this
     * array may exceed getParamCount(). This array does not contain the
     * jdoGenieOptions parameter (if any).
     */
    public String[] getParamTypes() {
        return paramTypes;
    }

    /**
     * Get the parameter names or null if there are none. The length of this
     * array may exceed getParamCount(). This array does not contain the
     * jdoGenieOptions parameter (if any).
     */
    public String[] getParamNames() {
        return paramNames;
    }

    /**
     * Get a comma list of all the parameters or null if none. This is a
     * tempory fix until the code that uses this can be refactored to use
     * the already parsed types and names.
     */
    public String getParameters() {
        int n = getParamCount();
        if (n == 0) return null;
        CharBuf s = new CharBuf();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(',');
            s.append(paramTypes[i]);
            s.append(' ');
            s.append(paramNames[i]);
        }
        return s.toString();
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = checkString(variables);
    }

    public String getOrdering() {
        return ordering;
    }

    public void setOrdering(String ordering) {
        this.ordering = checkString(ordering);
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    /**
     * If true then any query created from these params should set its
     * ignoreCache flag to the current setting for the PM.
     */
    public boolean isUseIgnoreCacheFromPM() {
        return useIgnoreCacheFromPM;
    }

    public void setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }

    public int getOptionsParamIndex() {
        return optionsParamIndex;
    }

    public void setOptionsParamIndex(int optionsParamIndex) {
        this.optionsParamIndex = optionsParamIndex;
    }

    public boolean hasJdoGenieOptions() {
        return optionsParamIndex >= 0;
    }

    public int getFetchGroupIndex() {
        return fetchGroupIndex;
    }

    public void setFetchGroupIndex(int fetchGroupIndex) {
        this.fetchGroupIndex = fetchGroupIndex;
    }

    public boolean isRandomAccess() {
        return randomAccess;
    }

    public void setRandomAccess(boolean randomAccess) {
        this.randomAccess = randomAccess;
    }

    public boolean isCountOnSize() {
        return countOnSize;
    }

    public void setCountOnSize(boolean countOnSize) {
        this.countOnSize = countOnSize;
    }

    public int[] getExtraEvictClasses() {
        return extraEvictClasses;
    }

    public void setExtraEvictClasses(int[] extraEvictClasses) {
        this.extraEvictClasses = extraEvictClasses;
    }

    public void fillFrom(QueryDetails qd) {
        // do not include useIgnoreCacheFromPM here
        language = qd.language;
        candidateClass = qd.candidateClass;
        col = qd.col;
        subClasses = qd.subClasses;
        filter = qd.filter;
        result = qd.result;
        grouping = qd.grouping;
        ignoreCache = qd.ignoreCache;
        unique = qd.unique;
        imports = qd.imports;
        variables = qd.variables;
        ordering = qd.ordering;
        paramCount = qd.paramCount;
        paramTypes = qd.paramTypes;
        paramNames = qd.paramNames;
        optionsParamIndex = qd.optionsParamIndex;
        fetchGroupIndex = qd.fetchGroupIndex;
        randomAccess = qd.randomAccess;
        countOnSize = qd.countOnSize;
        maxResultCount = qd.maxResultCount;
        resultBatchSize = qd.resultBatchSize;
        extraEvictClasses = qd.extraEvictClasses;
        cacheable = qd.cacheable;
        bounded = qd.bounded;
    }



    /**
     * Clear the extent and col variables.
     */
    public void clearExtentAndCol() {
        this.col = null;
    }

    public void readExternal(ObjectInput s) throws IOException,
            ClassNotFoundException {
        language = s.read();
        candidateClass = (Class)s.readObject();
        filter = (String)s.readObject();
        result = (String)s.readObject();
        grouping = (String)s.readObject();
        unique = s.readInt();
        ignoreCache = s.readBoolean();
        useIgnoreCacheFromPM = s.readBoolean();
        imports = (String)s.readObject();
        variables = (String)s.readObject();
        ordering = (String)s.readObject();
        subClasses = s.readBoolean();

        paramCount = s.readShort();
        paramTypes = new String[paramCount];
        paramNames = new String[paramCount];
        for (int i = 0; i < paramCount; i++) {
            paramTypes[i] = s.readUTF();
            paramNames[i] = s.readUTF();
        }
        optionsParamIndex = s.readShort();

        fetchGroupIndex = s.readShort();
        randomAccess = s.readBoolean();
        countOnSize = s.readBoolean();
        maxResultCount = s.readInt();
        resultBatchSize = s.readInt();
        bounded = s.readBoolean();

        int n = s.readShort();
        if (n > 0) {
            extraEvictClasses = new int[n];
            for (int i = 0; i < n; i++) extraEvictClasses[i] = s.readShort();
        }

        cacheable = s.readByte();
    }

    public void writeExternal(ObjectOutput s) throws IOException {
        s.writeByte(language);
        s.writeObject(candidateClass);
        s.writeObject(filter);
        s.writeObject(result);
        s.writeObject(grouping);
        s.writeInt(unique);
        s.writeBoolean(ignoreCache);
        s.writeBoolean(useIgnoreCacheFromPM);
        s.writeObject(imports);
        s.writeObject(variables);
        s.writeObject(ordering);
        s.writeBoolean(subClasses);

        s.writeShort(paramCount);
        for (int i = 0; i < paramCount; i++) {
            s.writeUTF(paramTypes[i]);
            s.writeUTF(paramNames[i]);
        }
        s.writeShort(optionsParamIndex);

        s.writeShort(fetchGroupIndex);
        s.writeBoolean(randomAccess);
        s.writeBoolean(countOnSize);
        s.writeInt(maxResultCount);
        s.writeInt(resultBatchSize);
        s.writeBoolean(bounded);

        if (extraEvictClasses != null) {
            int n = extraEvictClasses.length;
            s.writeShort(n);
            for (int i = 0; i < n; i++) s.writeShort(extraEvictClasses[i]);
        } else {
            s.writeShort(0);
        }

        s.writeByte(cacheable);
    }

    public void dump() {
        StringBuffer sb = new StringBuffer(">>>>> QueryDetails: ");
        sb.append("\n   language = " + language);
        sb.append("\n   candidateClass = " + candidateClass);
        sb.append("\n   filter = " + filter);
        sb.append("\n   result = " + result);
        sb.append("\n   grouping = " + grouping);
        sb.append("\n   unique = " + unique);
        sb.append("\n   ignoreCache = " + ignoreCache);
        sb.append("\n   useIgnoreCacheFromPM = " + useIgnoreCacheFromPM);
        sb.append("\n   imports = " + imports);
        sb.append("\n   variables = " + variables);
        sb.append("\n   ordering = " + ordering);
        sb.append("\n   paramCount = " + paramCount);
        for (int i = 0; i < paramCount; i++) {
            sb.append("\n   param[");
            sb.append(i);
            sb.append("] = '");
            sb.append(paramTypes[i]);
            sb.append("' '");
            sb.append(paramNames[i]);
            sb.append('\'');
        }
        sb.append("\n   optionsParamIndex = " + optionsParamIndex);
        sb.append("\n   fetchGroupIndex = " + fetchGroupIndex);
        sb.append("\n   randomAccess = " + randomAccess);
        sb.append("\n   countOnSize = " + countOnSize);
        sb.append("\n   maxRows = " + maxResultCount);
        sb.append("\n   resultBatchSize = " + resultBatchSize);
        sb.append("\n   parallelCollectionFetch = " + bounded);
        sb.append("\n   cacheable = " + cacheable);
        System.out.println(sb.toString());
    }

    public void setCacheable(boolean on) {
        cacheable = on ? TRUE : FALSE;
    }

    /**
     * Return the cacheable tri-state (NOT_SET, FALSE, TRUE).
     */
    public int getCacheable() {
        return cacheable;
    }
}
