
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

@Entity(access = AccessType.FIELD)
@Table(name = "ANIMAL")
public class Animal {
    @Id(generate = GeneratorType.TABLE, generator = "HIGHLOW")
    int pkAnimal;
    private boolean isDangerous;

    public static final EventCollector EVENT_COLLECTOR = new EventCollector();

    public Animal(boolean dangerous) {
        isDangerous = dangerous;
    }

    public Animal() {
        isDangerous = true;
    }


    public boolean isDangerous() {
        return isDangerous;
    }

    public void setDangerous(boolean dangerous) {
        isDangerous = dangerous;
    }

    @PrePersist
    public void prePersistCall(){
        EVENT_COLLECTOR.prePersist(this, Animal.class);
    }
    @PostPersist
    public void postPersistCall() {
        EVENT_COLLECTOR.postPersist(this, Animal.class);
    }
    @PreRemove
    public void preRemoveCall() {
        EVENT_COLLECTOR.preRemove(this, Animal.class);
    }
    @PostRemove
    public void postRemoveCall() {
        EVENT_COLLECTOR.postRemove(this, Animal.class);
    }
    @PreUpdate
    public void preUpdateCall() {
        EVENT_COLLECTOR.preUpdate(this, Animal.class);
    }
    @PostUpdate
    public void postUpdateCall() {
        EVENT_COLLECTOR.postUpdate(this, Animal.class);
    }
    @PostLoad
    public void postLoadCall() {
        EVENT_COLLECTOR.postLoad(this, Animal.class);
    }



}
