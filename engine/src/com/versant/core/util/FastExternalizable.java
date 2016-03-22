
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
package com.versant.core.util;

import java.io.IOException;

/**
 * Replacement for Externalizable that uses our custom steams to speed
 * things up. 
 */
public interface FastExternalizable {

    public void writeExternal(OIDObjectOutput out) throws IOException;

    public void readExternal(OIDObjectInput in)
            throws IOException, ClassNotFoundException;

}

