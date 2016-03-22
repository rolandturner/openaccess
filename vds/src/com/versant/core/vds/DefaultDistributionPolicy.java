
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
 * Created on Nov 5, 2003
 *
 * Copyright Versant Corporation 2003
 */
package com.versant.core.vds;
import javax.jdo.PersistenceManager;

/** Provides a default policy for distributing objects across multiple databases.
 * @author ppoddar
 * @since 1.0
 */
public class DefaultDistributionPolicy implements DistributionPolicy {
/** Select the first (zero-th) datastore as where the new intstance is to be
 * persisted. 
*/
    public int selectDatastore(PersistenceManager pm, Object pc, String[] datastores) {
        return 0;
    }
    
}
