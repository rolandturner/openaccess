
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
package com.versant.core.storagemanager;

import com.versant.core.common.OID;
import com.versant.core.metadata.FetchGroup;

/**
 * This is used by StorageManager methods that need to call back to the
 * application layer as part of their execution.
 */
public interface ApplicationContext {

    /**
     * Is the State for the given OID required or not? This is used by
     * StorageManager's when they prefetch data. The application level can check
     * caches (e.g. the local PM cache for JDO) and so on and decide if it
     * wants the data or not.
     */
    public boolean isStateRequired(OID oid, FetchGroup fetchGroup);

}

