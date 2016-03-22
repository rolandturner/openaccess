
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

import java.util.Iterator;
import java.util.List;

/**
 * Interface for collections returned by JDOQL queries.
 * 
 * @see QueryResultBase
 * @see ForwardQueryResult
 * @see RandomAccessQueryResult
 * @see MemoryQueryResult
 */
public interface QueryResult extends List {

    public QueryResult getNext();

    public void setNext(QueryResult next);

    public QueryResult getPrev();

    public void setPrev(QueryResult prev);

    /**
     * Close all open iterators.
     */
    public void close();

    public void setParams(Object[] params);

    public Iterator createInternalIterNoFlush();

}
