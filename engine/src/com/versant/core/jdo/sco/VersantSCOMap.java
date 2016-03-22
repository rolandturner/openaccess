
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

import java.util.Map;

/**
 * SCO's that are maps must implement this to have efficient database access.
 * <p/>
 * SCO maps that do not implement this will be treated like replaced maps on
 * commit or flush. All the old values will be deleted and all the current
 * values insert.
 */
public interface VersantSCOMap extends VersantSimpleSCO, Map {

    /**
     * Put references to all the keys and values into mapData. If the keys
     * and/or values are PC instances then the instances themselves or their
     * OIDs may be stored in mapData.
     */
    public MapData fillMapData(MapData mapData);

    /**
     * Called on commit or flush to get changes to this map.
     */
    public CollectionDiff getMapDiff(PersistenceContext pm);


}
