
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
package com.versant.core.ejb;

import java.util.*;

/**
 * PMF implementation.
 */
public class PersistenceManagerFactoryImp
        extends com.versant.core.jdo.PersistenceManagerFactoryImp {

    public PersistenceManagerFactoryImp(Properties props,
            ClassLoader loader) {
        super(props, loader);
    }

    /**
     * Create a new StorageManagerFactoryBuilder.
     */
    protected StorageManagerFactoryBuilder getStorageManagerFactoryBuilder() {
        return new com.versant.core.ejb.StorageManagerFactoryBuilder();
    }
}

