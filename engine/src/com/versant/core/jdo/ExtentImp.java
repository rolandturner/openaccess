
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

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.FetchPlan;
import java.util.Iterator;

/**
 * Extent implementation.
 */
public final class ExtentImp implements Extent {

    /**
     * The pm that this extent belongs to.
     */
    private final PMProxy pm;
    /**
     * The candidate class.
     */
    private Class candidateClass;
    /**
     * The query used for the extent.
     */
    private VersantQueryImp clientQuery;
    /**
     * The executed result from the client query.
     */
    private QueryResult resultCol;
    /**
     * Flag to indicate if subclasses is required.
     */
    private boolean subClasses = false;

    public ExtentImp(Class pcClass, boolean subclasses, PMProxy pm) {
        this.candidateClass = pcClass;
        this.subClasses = subclasses;
        this.pm = pm;
    }

    public Iterator iterator() {
        if (clientQuery == null) {
            clientQuery = new VersantQueryImp(pm);
            clientQuery.setCandidates(this);
            clientQuery.setFilter(null);
            resultCol = (QueryResult)clientQuery.execute();
        }
        Iterator iter = resultCol.iterator();
        return iter;
    }

    public boolean hasSubclasses() {
        return subClasses;
    }

    public Class getCandidateClass() {
        return candidateClass;
    }

    public PersistenceManager getPersistenceManager() {
        return pm;
    }

    public void closeAll() {
        if (clientQuery != null) clientQuery.closeAll();
    }

    public void close(Iterator it) {
        ((JDOListIterator)it).close();
    }

    public FetchPlan getFetchPlan() {
        return pm.getFetchPlan();
    }
}
