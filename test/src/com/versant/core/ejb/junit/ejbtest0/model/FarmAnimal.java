
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
@Table(name = "FARM_ANIMAL")
@EntityListener(com.versant.core.ejb.junit.ejbtest0.model.EntityLifecycleListener.class)
public class FarmAnimal extends Animal{

    public FarmAnimal(boolean dangerous) {
        super(dangerous);

    }

}
