
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
package com.versant.core.storagemanager.logging;

/**
 * Transaction related event.
 */
public class SmTxEvent extends StorageManagerEvent  {

    private final boolean optimistic;

    public SmTxEvent(int storageManagerId, int type,
            boolean optimistic) {
        super(storageManagerId, type);
        this.optimistic = optimistic;
    }

    public SmTxEvent(int storageManagerId, int type) {
        this(storageManagerId, type, false);
    }

    public String getDescription() {
        return optimistic ? "optimistic" : "";
    }

}
