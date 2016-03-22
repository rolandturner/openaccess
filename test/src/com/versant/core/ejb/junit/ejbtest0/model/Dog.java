
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
 * A dog. Extends Pet using vertical mapping.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "DOG")
public class Dog extends Pet {
    private boolean bestFriend;

    public Dog() {
    }

    public Dog(String name, boolean bestFriend) {
        super(name);
        this.bestFriend = bestFriend;
    }

    public boolean isBestFriend() {
        return bestFriend;
    }

    public void setBestFriend(boolean bestFriend) {
        this.bestFriend = bestFriend;
    }

    public String getNoise() {
        return "Woof";
    }

   

}

