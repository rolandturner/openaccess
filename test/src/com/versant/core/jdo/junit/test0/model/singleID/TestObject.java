
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

import javax.jdo.JDOHelper;
import java.util.*;

public abstract class TestObject implements Cloneable {
    protected static Random r = new Random(0);

    public static boolean allowNegativeByteValues = true;


    protected byte nextByte() {
        if (allowNegativeByteValues)
            return (byte) (r.nextInt(Byte.MAX_VALUE * 2) - Byte.MAX_VALUE);
        else
            return (byte) r.nextInt(Byte.MAX_VALUE + 1);
    }


    protected char nextCharacter() {
        char c = (char) ('!' + r.nextInt(93));
        return c;
    }


    protected String nextString(int length) {
        StringBuffer s = new StringBuffer();

        while (length-- > 0) s.append(nextCharacter());

        return s.toString();
    }


    protected boolean nextNull() {
        return r.nextInt(5) < 1;
    }


    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public abstract void fillRandom();

    public void fillUpdateRandom() {
        fillRandom();
    }

    public abstract boolean compareTo(Object obj);


    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        Object id = JDOHelper.getObjectId(this);

        return id == null ? super.equals(obj) : id.equals(JDOHelper.getObjectId(obj));
    }


    public int hashCode() {
        Object id = JDOHelper.getObjectId(this);

        return id == null ? super.hashCode() : id.hashCode();
    }

}

