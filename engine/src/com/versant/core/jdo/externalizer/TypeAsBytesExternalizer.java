
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
import java.io.*;

/**
 * This externalizer can convert any type to/from byte[]. The type must have
 * a constructor that accepts a byte[] and a method called toBytes that
 * will provide a byte[].
 *
 * This class is Serializable. This is only a requirement if you are using
 * remote PMs.
 */
public class TypeAsBytesExternalizer implements Externalizer, Externalizable {

    public static final String SHORT_NAME = "BYTES";    

    private Class type;
    private Constructor constructor;
    private Method toBytes;

    public TypeAsBytesExternalizer(Class type) {
        this.type = type;
        init();
    }

    public TypeAsBytesExternalizer() {
    }

    private void init() {
        try {
            constructor = type.getConstructor(new Class[]{/*CHFC*/byte[].class/*RIGHTPAR*/});
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type + " does not have a " +
                "constructor that accepts a byte[]", e);
        }
        try {
            toBytes = type.getMethod("toBytes", null);
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type + " does not have a " +
                "public toBytes() method", e);
        }
        if (toBytes.getReturnType() != /*CHFC*/byte[].class/*RIGHTPAR*/) {
            throw BindingSupportImpl.getInstance().runtime(type + ".toBytes() does not " +
                "return byte[]");
        }
    }

    public Object toExternalForm(Object pm, Object o) {
        if (o == null) return null;
        try {
            return toBytes.invoke(o, null);
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().fatalDatastore("Unable to convert instance of " +
                type.getName() + " '" + Utils.toString(o) + "' to byte[]: " + x, x);
        }
    }

    public Object fromExternalForm(Object pm, Object o) {
        if (o == null) return null;
        if (!(o instanceof byte[])) {
            throw BindingSupportImpl.getInstance().runtime(
                "Expected byte[] to create instance of " +
                type.getName() + ", got: " + Utils.toString(o));
        }
        try {
            return constructor.newInstance(new Object[]{o});
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().runtime(
                "Unable to create instance of " +
                type.getName() + " from " + Utils.toString(o) + ": " + x, x);
        }
    }

    public Class getExternalType() {
        return /*CHFC*/byte[].class/*RIGHTPAR*/;
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

