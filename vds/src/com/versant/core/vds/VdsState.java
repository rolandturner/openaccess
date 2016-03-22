
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
package com.versant.core.vds;

import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.DatastoreManager;

import java.util.ArrayList;

/**
 * VDS specific State methods.
 */
public interface VdsState {

    /**
     * Read all primary fields from the DSO.
     */
    public void readPrimaryFieldsFromDSO(DatastoreObject dso);

    /**
     * Write all the primary fields to the DSO. All of the primary fields
     * must be filled or a JDOFatalInternalException is thrown.
     */
    public void writePrimaryFieldsToDSO(DatastoreObject dso,
            DatastoreManager dsi);

    /**
     * Create and fill DSOs for all the dirty secondary fields and add them to
     * the DSOList. Each secondary field must update its fake loidField
     * on this State if required.
     */
    public void writeSecondaryFieldsToDSOList(DatastoreManager dsi,
            DSOList list);

    /**
     * Fill the secondary field's as DatastoreObjects and mark them as deleted
     * so that the correspoding SCO can be deleted along with the owner class.
     */
    public void deleteSecondaryFields(ArrayList arraylist);

}

