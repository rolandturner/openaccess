
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
package com.versant.core.jdo.junit.test2.model.knowhow;

import java.util.Date;

/** 
 * @keep-all
 */
public class Resource {

    private String name;
    private Date lastUpdated = new Date();
    private ResourceData data;

    public Resource(String name, ResourceData data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public ResourceData getData() {
        return data;
    }

}
