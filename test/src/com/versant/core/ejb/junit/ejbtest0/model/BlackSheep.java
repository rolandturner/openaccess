
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
import java.util.List;
import java.util.ArrayList;

@Entity(access = AccessType.FIELD)
@Table(name = "BLACK_SHEEP")
public class BlackSheep extends Sheep {
    private String woolType;

    @OneToMany(
            targetEntity = com.versant.core.ejb.junit.ejbtest0.model.WoolBag.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    private List<WoolBag> woolbags = new ArrayList<WoolBag>();

    public BlackSheep() {
    }

    public BlackSheep(String woolType) {
        this.woolType = woolType;
    }

    public String getWoolType() {
        return woolType;
    }

    public void setWoolType(String woolType) {
        this.woolType = woolType;
    }

    public List<WoolBag> getWoolbags() {
        return woolbags;
    }

    public void addWoolbags(WoolBag bag) {
        this.woolbags.add(bag);
    }

    @PostLoad
    public void blackSheepPostLoadCall() {
        EVENT_COLLECTOR.postLoad(this, BlackSheep.class);
    }
}

