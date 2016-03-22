
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
@Table(name = "WOOL_BAG")
public class WoolBag {
    @Id(generate = GeneratorType.TABLE, generator = "HIGHLOW")
    int pkWoolBag;
    String type;

    public WoolBag(String type) {
        this.type = type;
    }

    public WoolBag() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PrePersist
    public void prePersistCall() {
        Animal.EVENT_COLLECTOR.prePersist(this, WoolBag.class);
    }

    @PostPersist
    public void postPersistCall() {
        Animal.EVENT_COLLECTOR.postPersist(this, WoolBag.class);
    }

    @PreRemove
    public void preRemoveCall() {
        Animal.EVENT_COLLECTOR.preRemove(this, WoolBag.class);
    }

    @PostRemove
    public void postRemoveCall() {
        Animal.EVENT_COLLECTOR.postRemove(this, WoolBag.class);
    }

    @PreUpdate
    public void preUpdateCall() {
        Animal.EVENT_COLLECTOR.preUpdate(this, WoolBag.class);
    }

    @PostUpdate
    public void postUpdateCall() {
        Animal.EVENT_COLLECTOR.postUpdate(this, WoolBag.class);
    }

    @PostLoad
    public void postLoadCall() {
        Animal.EVENT_COLLECTOR.postLoad(this, WoolBag.class);
    }
}

