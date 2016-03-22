
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

import com.versant.core.common.VersantFieldMetaData;

import javax.jdo.spi.PersistenceCapable;
import java.util.Collection;

import com.versant.core.jdo.*;

/**
 * This Factory class is used to create SCOs for the various collection types.
 */
public interface VersantSCOCollectionFactory {

    /**
     * Create a new Collection class that implements the VersantSCOCollection
     * interface and fill it with the data in collectionData.
     */
    VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                          VersantPMInternal pm, VersantStateManager stateManager,
                                          VersantFieldMetaData fmd, CollectionData collectionData);

    /**
     * Create a new Collection class that implements the VersantSCOCollection
     * interface and fill it with the data in collection.
     */
    VersantSimpleSCO createSCOCollection(PersistenceCapable owner,
                                          VersantPMInternal pm, VersantStateManager stateManager,
                                          VersantFieldMetaData fmd, Collection collection);
                                          

	                                           
}
