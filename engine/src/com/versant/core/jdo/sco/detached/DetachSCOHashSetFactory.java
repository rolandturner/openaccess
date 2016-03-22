
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
package com.versant.core.jdo.sco.detached;

import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.VersantPMInternal;
import com.versant.core.common.VersantFieldMetaData;
import com.versant.core.common.Utils;
import com.versant.core.jdo.sco.CollectionData;
import com.versant.core.jdo.sco.VersantSCOCollectionFactory;
import com.versant.core.jdo.sco.VersantSimpleSCO;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Collection;

public class DetachSCOHashSetFactory implements VersantSCOCollectionFactory,
        Serializable {

    /**
     * Create a new Detached SCOHashSet instance .
     * <p/>
     * If collectionData contains any OIDs they are first resolved into PC
     * instances.
     */
    public VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                                 VersantPMInternal pm, VersantStateManager stateManager,
                                                 VersantFieldMetaData fmd, CollectionData collectionData) {
        return new DetachSCOHashSet(owner, stateManager, fmd, Utils.getObjectsById(collectionData.values,
                collectionData.valueCount, pm, (FieldMetaData) fmd,
                fmd.isElementTypePC()));
    }

    /**
     * Create a new Detached SCOHashSet instance  and fill it with the data in collection.
     */
    public VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                                 VersantPMInternal pm, VersantStateManager stateManager,
                                                 VersantFieldMetaData fmd, Collection collection) {
        return new DetachSCOHashSet(owner, stateManager, fmd, collection.toArray());
    }
    
      
}
