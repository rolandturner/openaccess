
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

public class SingleIdentityCharObject2 extends TestObject {
    private Character code;
    private String description;

    /**
     * Constructor
     */
    public SingleIdentityCharObject2() {
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
    public Character getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(Character code) {
        this.code = code;
    }

    protected static final char[] characters = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    protected static int charNumber = 0;

    protected static synchronized char getNextCharacter() {
        char ch = characters[charNumber];
        if (charNumber == (characters.length - 1)) {
            charNumber = 0;
        } else {
            charNumber++;
        }
        return ch;
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
        this.code = new Character(getNextCharacter());
        fillUpdateRandom();
    }

    public void fillUpdateRandom() {
        description = "Description " + this.getClass().toString() + " random: " + String.valueOf(r.nextDouble() * 1000);
    }

    public boolean compareTo(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SingleIdentityCharObject2))
            return false;

        SingleIdentityCharObject2 other = (SingleIdentityCharObject2) obj;

        if ((this.code == null && other.code != null) || (this.code != null && other.code == null)) {
            return false;
        }
        if (this.code == null && other.code == null) {
            return this.description.equals(other.description);
        }
        return this.code.charValue() == other.code.charValue() && this.description.equals(other.description);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

