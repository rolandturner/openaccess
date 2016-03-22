
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
package com.versant.core.jdo.junit.test0.model.jochen;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class ServiceClientList {
    private ServiceClient defaultServiceClient;
    private List serviceClients;

    public ServiceClient getDefaultServiceClient() {
        return defaultServiceClient;
    }

    public void setDefaultServiceClient(ServiceClient defaultServiceClient) {
        this.defaultServiceClient = defaultServiceClient;
    }

    public List getServiceClients() {
        return serviceClients;
    }

    public void setServiceClients(List serviceClients) {
        this.serviceClients = serviceClients;
    }

    public void createInitialClientList() {
        serviceClients = new ArrayList();
    }
}
