
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
 * Non-dangerous farm animal.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "COW")
public class Cow extends FarmAnimal {

    private int number;
    private int milkRating;

    public Cow() {
        super(true);
    }

    public Cow(int number, int milkRating) {
        super(true);
        this.number = number;
        this.milkRating = milkRating;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getMilkRating() {
        return milkRating;
    }

    public void setMilkRating(int milkRating) {
        this.milkRating = milkRating;
    }


    @PrePersist
    public void cowPrePersistCall() {
        EVENT_COLLECTOR.prePersist(this, Cow.class);
    }

    @PostPersist
    public void cowPostPersistCall() {
        EVENT_COLLECTOR.postPersist(this, Cow.class);
    }

    @PreRemove
    public void cowPreRemoveCall() {
        EVENT_COLLECTOR.preRemove(this, Cow.class);
    }

    @PostRemove
    public void cowPostRemoveCall() {
        EVENT_COLLECTOR.postRemove(this, Cow.class);
    }

    @PreUpdate
    public void cowPreUpdateCall() {
        EVENT_COLLECTOR.preUpdate(this, Cow.class);
    }

    @PostUpdate
    public void cowPostUpdateCall() {
        EVENT_COLLECTOR.postUpdate(this, Cow.class);
    }

    @PostLoad
    public void cowPostLoadCall() {
        EVENT_COLLECTOR.postLoad(this, Cow.class);
    }
}
