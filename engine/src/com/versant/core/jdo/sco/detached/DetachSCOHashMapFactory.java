
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
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.VersantPMInternal;
import com.versant.core.common.VersantFieldMetaData;
import com.versant.core.common.Utils;
import com.versant.core.jdo.sco.VersantSCOMapFactory;
import com.versant.core.jdo.sco.VersantSimpleSCO;
import com.versant.core.jdo.sco.MapData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Map;

public class DetachSCOHashMapFactory implements VersantSCOMapFactory,
        Serializable {

    public VersantSimpleSCO createSCOHashMap(PersistenceCapable owner,
                                              VersantPMInternal pm, VersantStateManager stateManager,
                                              VersantFieldMetaData fmd, MapData mapData) {
        mapData.keys = Utils.getObjectsById(mapData.keys, mapData.entryCount, pm,
                (FieldMetaData) fmd, fmd.isKeyTypePC());
        mapData.values = Utils.getObjectsById(mapData.values, mapData.entryCount, pm,
                (FieldMetaData) fmd, fmd.isElementTypePC());
        return new DetachSCOHashMap(owner, stateManager, fmd, mapData);
    }

    public VersantSimpleSCO createSCOHashMap(PersistenceCapable owner,
                                              VersantPMInternal pm, VersantStateManager stateManager,
                                              VersantFieldMetaData fmd, Map map) {
        return new DetachSCOHashMap(owner, stateManager, fmd, map);
    }
    
      
}
