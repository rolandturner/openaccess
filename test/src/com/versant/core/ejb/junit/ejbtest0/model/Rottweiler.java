
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

import javax.persistence.Entity;
import javax.persistence.AccessType;
import javax.persistence.Table;

/**
 * A cat eater. Extends Dog using flat mapping.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "ROTTWEILER")
public class Rottweiler extends Dog {

    private int catsEaten;

    public Rottweiler() {
    }

    public Rottweiler(String name, boolean bestFriend, int catsEaten) {
        super(name, bestFriend);
        this.catsEaten = catsEaten;
    }

    public int getCatsEaten() {
        return catsEaten;
    }

    public void setCatsEaten(int catsEaten) {
        this.catsEaten = catsEaten;
    }

    public String getNoise() {
        return "Grrrr";
    }

}

