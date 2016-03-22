
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
package com.versant.core.metadata;

/**
 * Meta data for an interface.
 * @keep-some
 */
public final class InterfaceMetaData {

    /**
     * The fully qualified name of the interface.
     */
    public final String qname;
    /**
     * The class.
     */
    public final Class cls;

    public InterfaceMetaData(Class cls) {
        this.cls = cls;
        this.qname = cls.getName();
    }

}

