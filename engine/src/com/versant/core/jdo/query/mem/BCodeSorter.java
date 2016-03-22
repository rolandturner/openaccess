
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
package com.versant.core.jdo.query.mem;

import com.versant.core.common.OID;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import com.versant.core.common.OID;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.VersantPersistenceManagerImp;

/**
 * @keep-all
 */
public class BCodeSorter implements Comparator {
    private List toSort;
    private PMProxy pmProxy;
    private BCodeQuery bCodeCompare;

    public void sort(List listOfOids, PMProxy pmProxy, BCodeQuery bCodeCompare) {
        this.toSort = listOfOids;
        this.pmProxy = pmProxy;
        this.bCodeCompare = bCodeCompare;
        Collections.sort(toSort, this);
    }

    public int compare(Object o1, Object o2) {
        return compare((OID) o1, (OID) o2);
    }

    /**
     * If there is a exception with the compare eg. null for one of the comp params then -1 is returned.
     * @param o1
     * @param o2
     * @return
     */
    public int compare(OID o1, OID o2) {
        int comp = 0;
        try {
            final VersantPersistenceManagerImp realPM = pmProxy.getRealPM();
            comp = bCodeCompare.compare(realPM.getInternalSM(o1).queryStateWrapper,
                    realPM.getInternalSM(o2).queryStateWrapper);
        } catch (Exception e) {
            comp = -1;
        }
        return comp;
    }
}


