
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
package com.versant.core.jdo.sco;

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.MDStatics;
import com.versant.core.util.classhelper.ClassHelper;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This registry map the SCO types to their factories.
 */
public class VersantSCOFactoryRegistry {

    private static final VersantSCOFactoryRegistry defaultMapping = new VersantSCOFactoryRegistry();

    private HashMap typeFactory = new HashMap(32);

    protected VersantSCOFactoryRegistry() {
        put(MDStatics.DATE, new DateSCOFactory());
        put(MDStatics.HASHSET, new SCOHashSetFactory());
        put(MDStatics.SET, new SCOHashSetFactory());
        put(MDStatics.TREESET, new SCOTreeSetFactory());
        put(MDStatics.SORTEDSET, new SCOTreeSetFactory());
        put(MDStatics.COLLECTION, new SCOListFactory());
        put(MDStatics.LIST, new SCOListFactory());
        put(MDStatics.ARRAYLIST, new SCOArrayListFactory());
        put(MDStatics.LINKEDLIST, new SCOLinkedListFactory());


        put(MDStatics.VECTOR, new SCOVectorFactory());


        put(MDStatics.MAP, new SCOHashMapFactory());
        put(MDStatics.HASHMAP, new SCOHashMapFactory());
        put(MDStatics.HASHTABLE, new SCOHashtableFactory());
        put(MDStatics.TREEMAP, new SCOTreeMapFactory());
        put(MDStatics.SORTEDMAP, new SCOTreeMapFactory());
    }

    private void put(int type, Object factory) {
        if (factory == null) return;
        String name = MDStaticUtils.toSimpleName(type);
        if (name == null) return;
        Class c = MDStaticUtils.toSimpleClass(name);
        if (c == null) return;
        typeFactory.put(c, factory);
    }

    public VersantSCOFactoryRegistry(Map mapping, ClassLoader loader) {
        this();
        if (mapping == null) return;
        for (Iterator it = mapping.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            String value = (String) mapping.get(key);
            try {
                Class javaClass = ClassHelper.get().classForName(key, true, loader);
                Class scoFactoryClass = ClassHelper.get().classForName(value, true, loader);
                Object factory = scoFactoryClass.newInstance();
                typeFactory.put(javaClass, factory);
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().runtime("Unable to add SCO factory mapping:\n" +
                        e.getMessage(), e);
            }
        }
    }

    /**
     * Get the factory for a simple SCO type.
     */
    public Object getFactory(FieldMetaData fmd) {
        if (fmd == null) return null;
        //create a new SCO data
        switch (fmd.category) {
            case MDStatics.CATEGORY_SIMPLE:
                return getJdoGenieSCOFactory(fmd);
            case MDStatics.CATEGORY_COLLECTION:
                return getJDOGenieSCOCollectionFactory(fmd);
            case MDStatics.CATEGORY_MAP:
                return getJDOGenieSCOMapFactory(fmd);
        }
        return null;
    }

    /**
     * Get the factory for a simple SCO type.
     */
    public VersantSCOFactory getJdoGenieSCOFactory(FieldMetaData fmd) {
        return (VersantSCOFactory) getFactory(fmd, MDStatics.CATEGORY_SIMPLE, /*CHFC*/VersantSCOFactory.class/*RIGHTPAR*/);
    }

    /**
     * Get the factory for a SCO collection.
     */
    public VersantSCOCollectionFactory getJDOGenieSCOCollectionFactory(FieldMetaData fmd) {
        return (VersantSCOCollectionFactory) getFactory(fmd,
                MDStatics.CATEGORY_COLLECTION, /*CHFC*/VersantSCOCollectionFactory.class/*RIGHTPAR*/);
    }

    /**
     * Get the factory for a SCO map.
     */
    public VersantSCOMapFactory getJDOGenieSCOMapFactory(FieldMetaData fmd) {
        return (VersantSCOMapFactory) getFactory(fmd,
                MDStatics.CATEGORY_MAP, /*CHFC*/VersantSCOMapFactory.class/*RIGHTPAR*/);
    }

    private Object getFactory(FieldMetaData fmd, int category, Class factoryCls) {
        if (fmd.category != category) {
            return null;
        }
		Class cls = fmd.type;
		
        Object o = typeFactory.get(cls);
		
        if (o == null) {
            throw BindingSupportImpl.getInstance().notImplemented("No SCO factory registered for type: " +
                    MDStaticUtils.toSimpleName(fmd.typeCode) + ".\nClass: " +
                    fmd.classMetaData.cls.getName() + " Field: " + fmd.name);
        }
        if (!factoryCls.isAssignableFrom(/*CHFC*/o.getClass()/*RIGHTPAR*/)) {
            throw BindingSupportImpl.getInstance().notImplemented("Incorrect SCO factory type.\nExpected:" +
                    factoryCls.getName() + "\nFound:" + o.getClass().getName() + ".\nfor Class: " +
                    fmd.classMetaData.cls.getName() + " Field: " + fmd.name);
        }
        return o;
    }

    public static String getDefaultMapping(Class javaType) {
        Object factory = defaultMapping.typeFactory.get(javaType);
        if (factory != null) {
            return factory.getClass().getName();
        }
        return null;
    }   

    public static void fillMapWithDefaults(Map map) {
        for (Iterator it = defaultMapping.typeFactory.keySet().iterator(); it.hasNext();) {
            Class javaType = (Class) it.next();
            Object factory = defaultMapping.typeFactory.get(javaType);
            map.put(javaType, factory.getClass().getName());
        }
    }

    public static List getValidSCOFactoryList(Class fieldType) {
        List factoryList = new ArrayList();
        for (Iterator it = defaultMapping.typeFactory.keySet().iterator(); it.hasNext();) {
            Class javaType = (Class) it.next();
            if (fieldType.isAssignableFrom(javaType)) {
                Object factory = defaultMapping.typeFactory.get(javaType);
                if (factory != null) {
                    String factoryName = factory.getClass().getName();
                    if (!factoryList.contains(factoryName)) {
                        factoryList.add(factoryName);
                    }
                }
            }
        }
        Collections.sort(factoryList);
        return factoryList;
    }

    public static void removeDefaults(Map map) {
        for (Iterator it = defaultMapping.typeFactory.keySet().iterator(); it.hasNext();) {
            Class javaType = (Class) it.next();
            String factoryName1 = (String) map.get(javaType);
            if (factoryName1 != null) {
                Object factory = defaultMapping.typeFactory.get(javaType);
                if (factory != null) {
                    String factoryName2 = factory.getClass().getName();
                    if (factoryName1.equals(factoryName2)) {
                        map.remove(javaType);
                    }
                }
            }
        }
    }

}
