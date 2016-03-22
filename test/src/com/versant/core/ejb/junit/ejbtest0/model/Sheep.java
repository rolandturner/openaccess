
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
@Table(name = "SHEEP")
public class Sheep extends FarmAnimal {

    private String woolType;

    public Sheep() {
        super(false);
    }

    public Sheep(String woolType) {
        super(false);
        this.woolType = woolType;
    }

    public String getWoolType() {
        return woolType;
    }

    public void setWoolType(String woolType) {
        this.woolType = woolType;
    }

    @PrePersist
    public void sheepPrePersistCall() {
        EVENT_COLLECTOR.prePersist(this, Sheep.class);
    }

    @PostPersist
    public void sheepPostPersistCall() {
        EVENT_COLLECTOR.postPersist(this, Sheep.class);
    }



}
