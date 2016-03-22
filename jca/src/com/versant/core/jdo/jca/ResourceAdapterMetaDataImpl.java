
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

import javax.resource.cci.ResourceAdapterMetaData;

class ResourceAdapterMetaDataImpl implements ResourceAdapterMetaData {

    ResourceAdapterMetaDataImpl() {
    }

    public String getAdapterName() {
        return "Verant JDOGenie";
    }

    public String getAdapterShortDescription() {
        return "Versant Resource Adapter";
    }

    public String getAdapterVendorName() {
        return "Versant";
    }

    public String getAdapterVersion() {
        return "??";
    }

    public String[] getInteractionSpecsSupported() {
        return new String[0];
    }

    public String getSpecVersion() {
        return "1.0";
    }

    public boolean supportsExecuteWithInputAndOutputRecord() {
        return false;
    }

    public boolean supportsExecuteWithInputRecordOnly() {
        return false;
    }

    public boolean supportsLocalTransactionDemarcation() {
        return true;
    }
}
