
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
package com.versant.core.ejb.common;

/**
 * Entry that does '==' equality testing. This entity also includeds 'nextEntry' that
 * allows it to be used as a linked list to track the insert order.
 */
public class IdentityEntry extends EntrySet.Entry {
    public IdentityEntry nextEntry;
    public Object value;

    public IdentityEntry(Object key) {
        super(key);
    }

    public boolean equals(Object o) {
        if (!(o instanceof IdentityEntry)) {
            return false;
        }
        return (getKey() == ((EntrySet.Entry)o).getKey());
    }

}
