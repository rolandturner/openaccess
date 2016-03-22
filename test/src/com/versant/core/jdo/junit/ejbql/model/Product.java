
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
package com.versant.core.jdo.junit.ejbql.model;

/**
 * Model for EJB 3 spec examples.
 */
public class Product implements Comparable {

    private String productType;
    private String name;

    public Product() {
    }

    public Product(String productType, String name) {
        this.productType = productType;
        this.name = name;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sort by name.
     */ 
    public int compareTo(Object o) {
        return name.compareTo(((Product)o).name);
    }

}

