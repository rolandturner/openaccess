
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
package com.versant.core.jdo.junit.test0.model.singleID;

public class SingleIdentitySubCharObject2 extends SingleIdentityCharObject2{
    private String description2;

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public void fillRandom() {
        setCode(new Character(getNextCharacter()));
        fillUpdateRandom();
    }

    public void fillUpdateRandom() {
        setDescription("Description " + this.getClass().toString() + " random: " + String.valueOf(r.nextDouble() * 1000));
        setDescription2("Description2 " + this.getClass().toString() + " random: " + String.valueOf(r.nextDouble() * 1000));
    }

    public boolean compareTo(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SingleIdentitySubCharObject2))
            return false;

        SingleIdentitySubCharObject2 other = (SingleIdentitySubCharObject2) obj;

        if ((getCode() == null && other.getCode() != null) ||
                (getCode() != null && other.getCode() == null)) {
            return false;
        }
        if (getCode() == null && other.getCode() == null) {
            return getDescription().equals(other.getDescription()) &&
                    getDescription2().equals(other.getDescription2());
        }
        return getCode().charValue() == other.getCode().charValue() &&
                getDescription().equals(other.getDescription()) &&
                getDescription2().equals(other.getDescription2());
    }
}

