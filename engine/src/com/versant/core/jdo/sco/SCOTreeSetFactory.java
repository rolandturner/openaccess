
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

import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.VersantPMInternal;
import com.versant.core.common.VersantFieldMetaData;
import com.versant.core.common.Utils;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 */
public class SCOTreeSetFactory implements VersantSCOCollectionFactory,
        Serializable {

    /**
     * Create a new SCOTreeSet instance that implements the
     * VersantSCOCollection interface.
     * <p/>
     * If collectionData contains any OIDs they are first resolved into PC
     * instances.
     */
    public VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                                 VersantPMInternal pm, VersantStateManager stateManager,
                                                 VersantFieldMetaData fmd, CollectionData collectionData) {
        Object[] originalData = Utils.getObjectsById(collectionData.values,
                collectionData.valueCount, pm, (FieldMetaData) fmd,
                fmd.isElementTypePC());
        return new SCOTreeSet(owner, stateManager, fmd, originalData);
    }

    /**
     * Create a new SCOTreeSet instance that implements the VersantSCOCollection
     * interface and fill it with the data in collection.
     */
    public VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                                 VersantPMInternal pm, VersantStateManager stateManager,
                                                 VersantFieldMetaData fmd, Collection collection) {
        return new SCOTreeSet(owner, stateManager, fmd, collection.toArray());
    }
    
       
}
