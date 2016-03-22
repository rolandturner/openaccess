
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
package com.versant.core.ejb.junit.ejbtest0.model;

import javax.persistence.*;

/**
 * Base class.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "PET")
@EntityListener(com.versant.core.ejb.junit.ejbtest0.model.EntityLifecycleListener.class)
public class Pet extends Animal{

    private String name;

    public Pet() {
        super(false);
    }

    public Pet(String name) {
        super(false);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    public String getNoise() {
        return "grunt";
    }

}

