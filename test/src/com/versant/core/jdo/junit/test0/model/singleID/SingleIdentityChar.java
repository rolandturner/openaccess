
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
 * SingleField identity using CharIdentity
 */
public class SingleIdentityChar extends TestObject{

    private char code;
    private String description;

    /**
     * Constructor
     */
    public SingleIdentityChar() {
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
    public char getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(char code) {
        this.code = code;
    }




    private static final char[] characters = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static int charNumber = 0;

    private static synchronized char getNextCharacter() {
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

    public boolean compareTo(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SingleIdentityChar))
            return false;

        SingleIdentityChar other = (SingleIdentityChar) obj;

        return this.code == other.code && this.description.equals(other.description);
    }


    public void fillRandom() {
        this.code = getNextCharacter();
        fillUpdateRandom();
    }

    public void fillUpdateRandom() {
        description = "Description " + this.getClass().toString() + " random: " + String.valueOf(r.nextDouble() * 1000);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
