
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
package com.versant.core.jdo.sco;

import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.common.CollectionDiff;
import com.versant.core.common.PersistenceContext;
import com.versant.core.jdo.VersantPersistenceManagerImp;

/**
 * SCO's that are collections must implement this to have efficient database access.
 * <p/>
 * SCO collections that do not implement this will be treated like replaced
 * collections on commit or flush. All the old values will be deleted and all the
 * current values insert.
 */
public interface VersantSCOCollection extends VersantSimpleSCO {


    /**
     * Called on commit or flush to get changes to this collection.
     */
    CollectionDiff getCollectionDiff(PersistenceContext pm);

    /**
     * Put references to all the values into collectionData. If the
     * values are PC instances then the instances themselves or their
     * OIDs may be stored in collectionData.
     */
    CollectionData fillCollectionData(CollectionData collectionData);


}
