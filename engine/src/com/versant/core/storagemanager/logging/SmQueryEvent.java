
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
package com.versant.core.storagemanager.logging;

import com.versant.core.jdo.QueryDetails;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.server.CompiledQuery;

/**
 * An event logged for compiling and executing a query.
 */
public class SmQueryEvent extends SmStatesReturnedEvent {

    private String language;
    private String candidateClass;
    private String filter;
    private String imports;
    private String variables;
    private String ordering;
    private boolean ignoreCache;
    private String params;

    private String fetchGroup;
    private boolean randomAccess;
    private boolean countStarOnSize;
    private boolean bounded;

    private String compiledQuery;
    private int count;
    private int skipAmount;
    private int index;
    private int fetchAmount;

    public SmQueryEvent(int storageManagerId, int type, QueryDetails q,
            CompiledQuery cq, ModelMetaData jmd) {
        super(storageManagerId, type);
        if (q == null && cq != null) {
            q = cq.getQueryDetails();
        }
        if (q != null) {
            language = q.getLanguageStr();
            final Class c = q.getCandidateClass();
            candidateClass = c == null ? null : c.getName();
            filter = q.getFilter();
            imports = q.getImports();
            variables = q.getVariables();
            ordering = q.getOrdering();
            ignoreCache = q.isIgnoreCache();
            int fgi = q.getFetchGroupIndex();
            if (c != null && fgi >= 0) {
                ClassMetaData cmd = jmd.getClassMetaData(c);
                fetchGroup = cmd.fetchGroups[fgi].name;
            }
            randomAccess = q.isRandomAccess();
            countStarOnSize = q.isCountOnSize();
            bounded = q.isBounded();
        }
        if (cq != null) {
            compiledQuery = cq.toString();
        }
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        StringBuffer s = new StringBuffer();
        s.append(language);
        if (candidateClass != null) {
            s.append(' ');
            s.append(candidateClass);
        }
        if (filter != null) {
            s.append(' ');
            s.append(filter);
        }
        return s.toString();
    }

    public String getLanguage() {
        return language;
    }

    public String getCandidateClass() {
        return candidateClass;
    }

    public String getFilter() {
        return filter;
    }

    public String getImports() {
        return imports;
    }

    public String getVariables() {
        return variables;
    }

    public String getOrdering() {
        return ordering;
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    public String getParams() {
        return params;
    }

    public String getFetchGroup() {
        return fetchGroup;
    }

    public boolean getRandomAccess() {
        return randomAccess;
    }

    public boolean getCountStarOnSize() {
        return countStarOnSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setImports(String imports) {
        this.imports = imports;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setFetchGroup(String fetchGroup) {
        this.fetchGroup = fetchGroup;
    }

    public void setCandidateClass(String candidateClass) {
        this.candidateClass = candidateClass;
    }

    public boolean isBounded() {
        return bounded;
    }

    public void setBounded(boolean bounded) {
        this.bounded = bounded;
    }

    public String getCompiledQuery() {
        return compiledQuery;
    }

    public int getSkipAmount() {
        return skipAmount;
    }

    public void setSkipAmount(int skipAmount) {
        this.skipAmount = skipAmount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFetchAmount() {
        return fetchAmount;
    }

    public void setFetchAmount(int fetchAmount) {
        this.fetchAmount = fetchAmount;
    }

}
