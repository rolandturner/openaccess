
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

import javax.jdo.spi.PersistenceCapable;
import java.util.Map;

/**
 * This Factory class is used to create SCOs for the various map types.
 */
public interface VersantSCOMapFactory {

    /**
     * Create a new Map class that implements the VersantSCOCollection
     * interface and fill it with the data in mapData.
     */
    public VersantSimpleSCO createSCOHashMap(PersistenceCapable owner,
                                              VersantPMInternal pm, VersantStateManager stateManager,
                                              VersantFieldMetaData fmd, MapData mapData);

    /**
     * Create a new Map class that implements the VersantSCOCollection
     * interface and fill it with the data in map.
     */
    public VersantSimpleSCO createSCOHashMap(PersistenceCapable owner,
                                              VersantPMInternal pm, VersantStateManager stateManager,
                                              VersantFieldMetaData fmd, Map map);
                                              

                                              
}
