
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
package petstore.db;

import java.io.Serializable;

/**
 * This class represents different categories of pets in the Java
 * Pet Store Demo.  Each category can have one or more products under
 * it and each product in turn can have one or more inventory items
 * under it.  For example, the Java Pet Store Demo currently has five
 * categories: birds, cats, dogs, fish, and reptiles.
 *
 */
public class Category implements Serializable {

    private String code;
    private String name;
    private String description;

    public Category() {
        super();
    }

    public Category(String code, String name,
                    String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Category(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "{ code=" + code + "}";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
