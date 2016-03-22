
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
 * A sausage dog. Extends Dog using flat mapping.
 */
@Entity(access = AccessType.FIELD)
@Table(name = "WIENER_DOG")
public class WienerDog extends Dog {

    private int length;

    public WienerDog() {
    }

    public WienerDog(String name, boolean bestFriend, int length) {
        super(name, bestFriend);
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getNoise() {
        return "Yap yap";
    }

}

