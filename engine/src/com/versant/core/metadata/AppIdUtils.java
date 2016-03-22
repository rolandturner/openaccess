
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
package com.versant.core.metadata;

import com.versant.core.common.BindingSupportImpl;

import javax.jdo.identity.*;

public class AppIdUtils {


    /**
     * Utility to create a new SingleFieldIdentity when you know the type of the
     * PersistenceCapable, and also which SingleFieldIdentity, and the value of
     * the key.
     *
     * @param idType Type of SingleFieldIdentity
     * @param pcType Type of the PersistenceCapable
     * @param value  The value for the identity (the Long, or Int, or ... etc).
     */
    public static SingleFieldIdentity createSingleFieldIdentity(Class idType,
                                                                Class pcType,
                                                                Object value) {
        if (pcType == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "SingleFieldIdentity's PC type is null");
        }
        if (value == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "SingleFieldIdentity's value type is null for " +
                    pcType.getName());
        }
        try {
            if (idType == /*CHFC*/LongIdentity.class/*RIGHTPAR*/) {
                return new LongIdentity(pcType, (Long)value);
            } else if (idType == /*CHFC*/IntIdentity.class/*RIGHTPAR*/) {
                return new IntIdentity(pcType, (Integer) value);
            } else if (idType == /*CHFC*/StringIdentity.class/*RIGHTPAR*/) {
                return new StringIdentity(pcType, (String) value);
            } else if (idType == /*CHFC*/ByteIdentity.class/*RIGHTPAR*/) {
                return new ByteIdentity(pcType, (Byte) value);
            } else if (idType == /*CHFC*/ShortIdentity.class/*RIGHTPAR*/) {
                return new ShortIdentity(pcType, (Short) value);
            } else if (idType == /*CHFC*/CharIdentity.class/*RIGHTPAR*/) {
                return new CharIdentity(pcType, (Character) value);
            }
        } catch (ClassCastException e) {
                throw BindingSupportImpl.getInstance().internal(
                        "SingleFieldIdentity's value type is invalid.",e );
        }
        return null;
    }

    /**
     * Checks whether the argument is equals to one the single field identity class names
     *
     * @param className the full class name
     * @return true if the argument is equals to one the single field identity class names
     */
    public static boolean isSingleFieldIdentityClass(String className) {
        if (className == null || className.length() < 1) {
            return false;
        }

        return (className.equals(StringIdentity.class.getName()) ||
                className.equals(ByteIdentity.class.getName()) ||
                className.equals(CharIdentity.class.getName()) ||
                className.equals(IntIdentity.class.getName()) ||
                className.equals(ShortIdentity.class.getName()) ||
                className.equals(LongIdentity.class.getName()));
    }

    public static boolean isSingleFieldIdentityClass(Class cls) {
        return /*CHFC*/SingleFieldIdentity.class/*RIGHTPAR*/.isAssignableFrom(cls);
    }
}

