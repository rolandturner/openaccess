
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

public class EntityLifecycleListener {
    // we do not listen for @PreUpdate and @PostUpdate, it must be caught up in
    // the hier
    @PrePersist
    public void prePersistCall(Animal animal) {
        Animal.EVENT_COLLECTOR.prePersist(animal, EntityLifecycleListener.class);
    }

    @PostPersist
    public void postPersistCall(Animal animal) {
        Animal.EVENT_COLLECTOR.postPersist(animal, EntityLifecycleListener.class);
    }

    @PreRemove
    public void preRemoveCall(Animal animal) {
        Animal.EVENT_COLLECTOR.preRemove(animal, EntityLifecycleListener.class);
    }

    @PostRemove
    public void postRemoveCall(Animal animal) {
        Animal.EVENT_COLLECTOR.postRemove(animal, EntityLifecycleListener.class);
    }

    @PostLoad
    public void postLoadCall(Animal animal) {
        Animal.EVENT_COLLECTOR.postLoad(animal, EntityLifecycleListener.class);
    }
}

