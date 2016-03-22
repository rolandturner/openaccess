
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

/**
 * SingleField identity using ShortIdentity
 */
public class SingleIdentityShortObject extends TestObject{

    private Short code;
    private String description;

    /**
     * Constructor.
     */
    public SingleIdentityShortObject() {
        super();
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return code
     */
    public Short getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(Short code) {
        this.code = code;
    }

  
    public String toString() {
        StringBuffer s = new StringBuffer(super.toString());

        s.append("  code = ").append(code);
        s.append('\n');
        s.append("  description = ").append(description);
        s.append('\n');
        return s.toString();
    }

    public void fillRandom() {
        this.code = new Short((short) r.nextInt());
        fillUpdateRandom();
    }

    public void fillUpdateRandom() {
        description = "Description " + this.getClass().toString() + " random: " + String.valueOf(r.nextDouble() * 1000);
    }

    public boolean compareTo(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SingleIdentityShortObject))
            return false;

        SingleIdentityShortObject other = (SingleIdentityShortObject) obj;

        if ((this.code == null && other.code != null) || (this.code != null && other.code == null)) {
            return false;
        }
        if (this.code == null && other.code == null) {
            return this.description.equals(other.description);
        }
        return this.code.shortValue() == other.code.shortValue() && this.description.equals(other.description);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
