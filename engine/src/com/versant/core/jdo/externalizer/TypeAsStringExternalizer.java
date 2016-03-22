
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
package com.versant.core.jdo.externalizer;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * This externalizer can convert any type to/from String. The type must have
 * a constructor that accepts a String. If it has a method 'String
 * toExternalString()' then this is used to convert it to a String. Otherwise
 * its toString() method is used.
 *
 * This class is Serializable. This is only a requirement if you are using
 * remote PMs.
 */
public class TypeAsStringExternalizer
        implements Externalizer, Externalizable {

    public static final String SHORT_NAME = "STRING";

    private Class type;
    private transient Constructor constructor;
    private transient Method toExternalString;

    public TypeAsStringExternalizer(Class type) {
        this.type = type;
        init();
    }

    public TypeAsStringExternalizer() {
    }

    private void init() {
        try {
            constructor = type.getConstructor(new Class[]{/*CHFC*/String.class/*RIGHTPAR*/});
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type +
                " does not have a " +
                "constructor that accepts a String", e);
        }
        try {
            toExternalString = type.getMethod("toExternalString", null);
            if (toExternalString.getReturnType() != /*CHFC*/String.class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().runtime(type +
                    ".toExternalString() does not " +
                    "return String");
            }
        } catch (NoSuchMethodException e) {
            // no problem - we will use toString
        }
    }

    public Object toExternalForm(Object pm, Object o) {
        if (o == null) return null;
        try {
            return toExternalString == null
                    ? o.toString()
                    : (String)toExternalString.invoke(o, null);
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().runtime("Unable to convert instance of " +
                type.getName() + " using " +
                (toExternalString == null ? "toString()" : "toExternalString()") +
                ": " + x, x);
        }
    }

    public Object fromExternalForm(Object pm, Object o) {
        if (o == null) return null;
        if (!(o instanceof String)) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expected String to create instance of " +
                type.getName() + ", got: " + Utils.toString(o));
        }
        try {
            return constructor.newInstance(new Object[]{o});
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().fatalDatastore(
                "Unable to create instance of " +
                type.getName() + " from '" + Utils.toString(o) + "': " + x, x);
        }
    }

    public Class getExternalType() {
        return /*CHFC*/String.class/*RIGHTPAR*/;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(type);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        type = (Class)in.readObject();
        init();
    }
}

