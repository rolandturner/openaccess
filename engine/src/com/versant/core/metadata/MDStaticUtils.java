
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

import com.versant.core.util.IntObjectHashMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * Static utility methods to convert statics to and from Strings.
 */
public class MDStaticUtils implements MDStatics {

    public static final int[] primToNumberMapping = new int[]{
        -1,
        BOOLEANW,
        BYTEW,
        SHORTW,
        INTW,
        LONGW,
        FLOATW,
        DOUBLEW,
        CHAR,
        BOOLEANW,
        BYTEW,
        SHORTW,
        INTW,
        LONGW,
        FLOATW,
        DOUBLEW,
        CHARW,
        STRING,
        BIGDECIMAL,
        BIGINTEGER,
        DATE,
        LOCALE,
    };

    protected static final Map TYPE_MAP = new HashMap();
    protected static final Map TYPE_CODE_MAP = new HashMap();
    private static final IntObjectHashMap CODE_TYPE_MAP = new IntObjectHashMap();
    private static final Map TYPE_NAME_MAP = new HashMap();

    private static void put(String name, Class t, int code) {
        TYPE_MAP.put(name, t);
        TYPE_NAME_MAP.put(t, name);
        TYPE_CODE_MAP.put(t, new Integer(code));
        CODE_TYPE_MAP.put(code, name);
    }

    private static void put(String name, String name2, Class t, int code) {
        TYPE_MAP.put(name2, t);
        put(name, t, code);
    }

    static {
        put("boolean", /*CHFC*/Boolean.TYPE/*RIGHTPAR*/, BOOLEAN);
        put("Boolean", "java.lang.Boolean", /*CHFC*/Boolean.class/*RIGHTPAR*/, BOOLEANW);
        put("byte", /*CHFC*/Byte.TYPE/*RIGHTPAR*/, BYTE);
        put("Byte", "java.lang.Byte", /*CHFC*/Byte.class/*RIGHTPAR*/, BYTEW);
        put("short", /*CHFC*/Short.TYPE/*RIGHTPAR*/, SHORT);
        put("Short", "java.lang.Short", /*CHFC*/Short.class/*RIGHTPAR*/, SHORTW);
        put("int", /*CHFC*/Integer.TYPE/*RIGHTPAR*/, INT);
        put("Integer", "java.lang.Integer", /*CHFC*/Integer.class/*RIGHTPAR*/, INTW);
        put("long", /*CHFC*/Long.TYPE/*RIGHTPAR*/, LONG);
        put("Long", "java.lang.Long", /*CHFC*/Long.class/*RIGHTPAR*/, LONGW);
        put("float", /*CHFC*/Float.TYPE/*RIGHTPAR*/, FLOAT);
        put("Float", "java.lang.Float", /*CHFC*/Float.class/*RIGHTPAR*/, FLOATW);
        put("double", /*CHFC*/Double.TYPE/*RIGHTPAR*/, DOUBLE);
        put("Double", "java.lang.Double", /*CHFC*/Double.class/*RIGHTPAR*/, DOUBLEW);
        put("char", /*CHFC*/Character.TYPE/*RIGHTPAR*/, CHAR);
        put("Character", "java.lang.Character", /*CHFC*/Character.class/*RIGHTPAR*/, CHARW);

        put("String", "java.lang.String", /*CHFC*/String.class/*RIGHTPAR*/, STRING);
        put("BigDecimal", "java.math.BigDecimal", /*CHFC*/BigDecimal.class/*RIGHTPAR*/, BIGDECIMAL);
        put("BigInteger", "java.math.BigInteger", /*CHFC*/BigInteger.class/*RIGHTPAR*/, BIGINTEGER);
        put("Date", "java.util.Date", /*CHFC*/Date.class/*RIGHTPAR*/, DATE);
        put("Locale", "java.util.Locale", /*CHFC*/Locale.class/*RIGHTPAR*/, LOCALE);

        //Collections
        put("Collection", "java.util.Collection", /*CHFC*/java.util.Collection.class/*RIGHTPAR*/,
                COLLECTION);
        put("List", "java.util.List", /*CHFC*/java.util.List.class/*RIGHTPAR*/, LIST);
        put("ArrayList", "java.util.ArrayList", /*CHFC*/java.util.ArrayList.class/*RIGHTPAR*/,
                ARRAYLIST);
        put("LinkedList", "java.util.LinkedList", /*CHFC*/java.util.LinkedList.class/*RIGHTPAR*/,
                LINKEDLIST);
        put("Vector", "java.util.Vector", /*CHFC*/java.util.Vector.class/*RIGHTPAR*/, VECTOR);

        put("Set", "java.util.Set", /*CHFC*/java.util.Set.class/*RIGHTPAR*/, SET);
        put("HashSet", "java.util.HashSet", /*CHFC*/java.util.HashSet.class/*RIGHTPAR*/, HASHSET);
        put("TreeSet", "java.util.TreeSet", /*CHFC*/java.util.TreeSet.class/*RIGHTPAR*/, TREESET);
        put("SortedSet", "java.util.SortedSet", /*CHFC*/java.util.SortedSet.class/*RIGHTPAR*/,
                SORTEDSET);



        //Maps
        put("Map", "java.util.Map", /*CHFC*/java.util.Map.class/*RIGHTPAR*/, MAP);
        put("HashMap", "java.util.HashMap", /*CHFC*/java.util.HashMap.class/*RIGHTPAR*/, HASHMAP);
        put("TreeMap", "java.util.TreeMap", /*CHFC*/java.util.TreeMap.class/*RIGHTPAR*/, TREEMAP);
        put("SortedMap", "java.util.SortedMap", /*CHFC*/java.util.SortedMap.class/*RIGHTPAR*/,
                SORTEDMAP);
        put("Hashtable", "java.util.Hashtable", /*CHFC*/java.util.Hashtable.class/*RIGHTPAR*/,
                HASHTABLE);
    }

    protected MDStaticUtils() {
    }

    /**
     * Convert the name of a simple class (int, Integer, String etc.) into
     * its Class object or null if not found.
     */
    public static final Class toSimpleClass(String name) {
        return (Class)TYPE_MAP.get(name);
    }

    /**
     * Convert a simple class (Integer.TYPE, Integer.class, String.class etc.)
     * into a type code (INT, INTW, STRING etc.) or less than 0 if not found.
     */
    public static final int toTypeCode(Class cls) {

        Integer i = (Integer)TYPE_CODE_MAP.get(cls);
        return i == null ? -1 : i.intValue();
    }

    public static String toSimpleName(Class cls) {
        return (String)TYPE_NAME_MAP.get(cls);
    }

    /**
     * Return the simple name for a type code.
     *
     * @param code
     * @return
     */
    public static String toSimpleName(int code) {
        String name = (String)CODE_TYPE_MAP.get(code);
        return name;
    }

    public static String toTriStateString(int t) {
        switch (t) {
            case NOT_SET:
                return "notset";
            case TRUE:
                return "true";
            case FALSE:
                return "false";
        }
        return "unknown(" + t + ")";
    }

    public static String toIdentityTypeString(int identityType) {
        switch (identityType) {
            case IDENTITY_TYPE_APPLICATION:
                return "application";
            case IDENTITY_TYPE_DATASTORE:
                return "datastore";
            case IDENTITY_TYPE_NONDURABLE:
                return "none";
        }
        return "unknown(" + identityType + ")";
    }

    public static String toPersistenceModifierString(int pm) {
        switch (pm) {
            case PERSISTENCE_MODIFIER_PERSISTENT:
                return "persistent";
            case PERSISTENCE_MODIFIER_TRANSACTIONAL:
                return "transactional";
            case PERSISTENCE_MODIFIER_NONE:
                return "none";
            case NOT_SET:
                return "";
        }
        return "unknown(" + pm + ")";
    }

    public static String toNullValueString(int nullValue) {
        switch (nullValue) {
            case NULL_VALUE_EXCEPTION:
                return "exception";
            case NULL_VALUE_DEFAULT:
                return "default";
            case NULL_VALUE_NONE:
                return "none";
        }
        return "unknown(" + nullValue + ")";
    }

    public static String toCategoryString(int category) {
        switch (category) {
            case CATEGORY_SIMPLE:
                return "Simple";
            case CATEGORY_REF:
                return "Ref";
            case CATEGORY_POLYREF:
                return "PolyRef";
            case CATEGORY_COLLECTION:
                return "Collection";
            case CATEGORY_ARRAY:
                return "Array";
            case CATEGORY_MAP:
                return "Map";
            case CATEGORY_TRANSACTIONAL:
                return "Transactional";
            case CATEGORY_NONE:
                return "None";
            case CATEGORY_DATASTORE_PK:
                return "Datastore Pk";
            case CATEGORY_OPT_LOCKING:
                return "Opt Locking";
            case CATEGORY_CLASS_ID:
                return "Class Id";
            case CATEGORY_EXTERNALIZED:
                return "Externalized";
        }
        return "unknown(" + category + ")";
    }

    public static String toAutoSetString(int autoSet) {
        switch (autoSet) {
            case AUTOSET_NO:
                return "NONE";
            case AUTOSET_CREATED:
                return "CREATED";
            case AUTOSET_MODIFIED:
                return "MODIFIED";
            case AUTOSET_BOTH:
                return "BOTH";
        }
        return "unknown(" + autoSet + ")";
    }

    public static String toCacheString(int cache) {
        switch (cache) {
            case CACHE_STRATEGY_NO:
                return "no";
            case CACHE_STRATEGY_YES:
                return "yes";
            case CACHE_STRATEGY_ALL:
                return "all";
        }
        return "unknown(" + cache + ")";
    }

    public static final boolean isIntegerType(int type) {
        return isSignedIntegerType(type)

                ;
    }

    public static final boolean isSignedIntegerType(int type) {
        if ((type >= BOOLEAN && type <= LONGW)
                || type == CHARW


        ) {
            return true;
        } else {
            return false;
        }
    }



}

