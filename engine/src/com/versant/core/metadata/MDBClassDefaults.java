
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
 * This holds class defaults used during meta data building. These are
 * provided by the DataStore for the class. They must be available before
 * the DataStore is given the meta data to modify hence the need for this
 * class.
 */
public class MDBClassDefaults {

    public boolean readOnly;
    public int cacheStrategy;
    public boolean refsInDefaultFetchGroup;

}
