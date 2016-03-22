
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
/*
 * Created on Oct 31, 2003
 *
 * Copyright Versant Corporation 2003
 */
package com.versant.core.vds;
import javax.jdo.PersistenceManager;

/** Defines protocol of distributing objects across multiple datastores.

 * @author ppoddar
 * @since 1.0
 */
public interface DistributionPolicy {
/** Selects the datastore where a new instance is to be persisted.
 * @param pm The persistence manager that is managing the argument instance. 
 * @param pc The new instance to be persisted
 * @param datastores List of datastore names in the same order as specified in 
 * <code>javax.jdo.option.ConnectionURL</code>.
 * @return index on the array of datastore names where <code>pc</code> is to be persisted.
 * @exception Any exception thrown from this method results in storing the instance in the
 * default datastore. 
 */    
    int selectDatastore(PersistenceManager pm, Object pc, String[] datastores);
}
