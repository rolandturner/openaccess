
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
 * ApplicationContext that wants all data.
 */
public class DummyApplicationContext implements ApplicationContext {

    public static final DummyApplicationContext INSTANCE =
            new DummyApplicationContext();

    public boolean isStateRequired(OID oid, FetchGroup fetchGroup) {
        return true;
    }

}

