
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
package com.versant.core.jdo.jca;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

public class ManagedConnectionMetaDataImpl implements ManagedConnectionMetaData, javax.resource.cci.ConnectionMetaData {
    public java.lang.String getEISProductName() throws ResourceException {
        return "Versant JDO";
    }

    public java.lang.String getEISProductVersion() throws ResourceException {
        return "1.0";
    }

    public int getMaxConnections() throws ResourceException {
        return 0; //indicating no limitation or limitation unknown, respectively
    }

    public java.lang.String getUserName() throws ResourceException {
        return "";
    }
}
