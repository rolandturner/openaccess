
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
 * A cat. Extends Pet using vertical mapping.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "CAT")
public class Cat extends Pet {
//    @PrePersist must be caught by Pet's listener
//    @PreUpdate and @PostUpdate must be caught by Animal
    private int livesLeft;

    public Cat() {
    }

    public Cat(String name, int livesLeft) {
        super(name);
        this.livesLeft = livesLeft;
    }

    public int getLivesLeft() {
        return livesLeft;
    }

    public void setLivesLeft(int livesLeft) {
        this.livesLeft = livesLeft;
    }

    public String getNoise() {
        return "Meuow";
    }

    @PostPersist
    public void catPostPersistCall() {
        EVENT_COLLECTOR.postPersist(this, Cat.class);
    }

    @PreRemove
    public void catPreRemoveCall() {
        EVENT_COLLECTOR.preRemove(this, Cat.class);
    }

    @PostRemove
    public void catPostRemoveCall() {
        EVENT_COLLECTOR.postRemove(this, Cat.class);
    }


    @PostLoad
    public void catPostLoadCall() {
        EVENT_COLLECTOR.postLoad(this, Cat.class);
    }

}

