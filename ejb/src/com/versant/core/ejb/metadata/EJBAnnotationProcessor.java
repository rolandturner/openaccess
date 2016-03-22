
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
package com.versant.core.ejb.metadata;

import com.versant.core.metadata.parser.*;
import com.versant.core.metadata.MetaDataPreProcessor;
import com.versant.core.metadata.MetaDataUtils;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.EntityLifecycleEvent;
import com.versant.core.ejb.EntityClassLifecycleManager;

import javax.persistence.*;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.io.Serializable;

/**
 * MetaData PreProcessor for ejb jdbc metadata. The preprocessor looks for annotations
 * and set the equivalent jdo metadata. If no annotations is present then the ejb defaults
 * will be applied.
 */
public class EJBAnnotationProcessor implements JdoExtensionKeys, MetaDataPreProcessor {
    private ClassLoader loader;
    private MetaDataUtils mdutils;
    private final Class[] EMPTY_CLASS_ARRAY;
    private static final String GENERATOR_HIGHLOW = "HIGHLOW";
    private static final String GENERATOR_AUTOINC = "AUTOINC";

    public EJBAnnotationProcessor(ClassLoader loader, MetaDataUtils mdutils) {
        this.loader = loader;
        this.mdutils = mdutils;
        EMPTY_CLASS_ARRAY = new Class[] {};
    }

    public void process(JdoClass jdoClass, ModelMetaData jmd) {
        Class cls;
        try {
            cls = loadClass(jdoClass.getQName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!cls.isAnnotationPresent(javax.persistence.Entity.class)) {
            if (cls.isAssignableFrom(javax.persistence.Embeddable.class)) {
                //mark this as embedded only
                JdoExtension ext = JdoExtension.find(EMBEDDED_ONLY, jdoClass.elements);
                if (ext == null) {
                    ext = new JdoExtension();
                    ext.key = EMBEDDED_ONLY;
                    jdoClass.addElement(ext);
                }
            }
            return;
        }
        javax.persistence.Entity ea = (Entity) cls.getAnnotation(javax.persistence.Entity.class);

        //change to appid
        jdoClass.identityType = MDStatics.IDENTITY_TYPE_APPLICATION;
        jdoClass.objectIdClasssRequired = false;

        //create a map for jdoField name to jdoField instance.
        Map jdoFieldMap = createFieldMap(jdoClass);

        Set pkFields = Collections.EMPTY_SET;
        //check for IdClass
        if (cls.isAnnotationPresent(javax.persistence.IdClass.class)) {
            javax.persistence.IdClass ano = (IdClass) cls.getAnnotation(javax.persistence.IdClass.class);
            jdoClass.objectIdClass = ano.value().getName();

            /**
             * The pk fields does not have to be anotated so we must check
             */
            Field[] pkFieldsArray = ano.value().getFields();
            pkFields = new HashSet(pkFieldsArray.length);
            for (int i = 0; i < pkFieldsArray.length; i++) {
                pkFields.add(pkFieldsArray[i].getName());
            }
        }

        //update table name
        if (cls.isAnnotationPresent(javax.persistence.Table.class)) {
            javax.persistence.Table ta = (Table) cls.getAnnotation(javax.persistence.Table.class);
            JdoExtension ext = JdoExtension.find(JDBC_TABLE_NAME, jdoClass.elements);
            if (ext == null) {
                ext = new JdoExtension();
                ext.key = JDBC_TABLE_NAME;
                ext.value = ta.name();
                jdoClass.addElement(ext);
            }
        }
        
        //do we have a EntityListener or callbacks
        List lifecycleEvents = processCallBackAndListener(cls);
        if (lifecycleEvents.size() != 0) {
            jmd.addEntityLifecycleEvent(lifecycleEvents);
        }

        //calculate pc super
        doPCSuperClass(jdoClass, cls);

        //do the inheritance mapping.
        if (cls.isAnnotationPresent(javax.persistence.Inheritance.class)) {
            javax.persistence.Inheritance ano =
                    (Inheritance) cls.getAnnotation(javax.persistence.Inheritance.class);
            //process the discriminator
            JdoExtension idClassExt = JdoExtension.find(JDBC_CLASS_ID, jdoClass.elements);
            if (idClassExt == null) {

            }

            JdoExtension inheritanceExt = JdoExtension.find(JDBC_INHERITANCE, jdoClass.elements);
            switch (ano.strategy()) {
                case SINGLE_TABLE:
                    //flat
                    if (inheritanceExt != null) {
                        inheritanceExt.value = "flat";
                    } else {
                        inheritanceExt = new JdoExtension();
                        inheritanceExt.key = JDBC_INHERITANCE;
                        inheritanceExt.value = "flat";
                        jdoClass.addElement(inheritanceExt);
                    }
                    break;
                case TABLE_PER_CLASS:
                    //vertical
                    if (inheritanceExt != null) {
                        inheritanceExt.value = "vertical";
                    } else {
                        inheritanceExt = new JdoExtension();
                        inheritanceExt.key = JDBC_INHERITANCE;
                        inheritanceExt.value = "vertical";
                        jdoClass.addElement(inheritanceExt);
                    }
                    break;
                case JOINED:
                    break;
            }
        }


        if(jdoClass.getPCSuperClassQName() == null){
            JdoExtension optLock = JdoExtension.find(JDBC_OPTIMISTIC_LOCKING, jdoClass.elements);
            if (optLock == null) {
                optLock = new JdoExtension();
                optLock.key = JDBC_OPTIMISTIC_LOCKING;
                jdoClass.addElement(optLock);
            }
            optLock.value = "none";
        }
        final AccessibleMember accessibleMember = new AccessibleMember();
        if (ea.access() == AccessType.FIELD) {
            Field[] fields = cls.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                accessibleMember.init(fields[i], null);
                processField(jdoFieldMap, jdoClass, accessibleMember, pkFields);
            }
        } else {
            Field[] fields = cls.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                String s = fields[i].getName();
                char[] ca = s.toCharArray();
                ca[0] = Character.toUpperCase(ca[0]);
                Method m = null;
                try {
                    m = cls.getDeclaredMethod("get" + String.valueOf(ca), EMPTY_CLASS_ARRAY);
                } catch (NoSuchMethodException e) {
//                    System.out.println("method 'get" + String.valueOf(ca) + "' not found");
                    try {
                        m = cls.getDeclaredMethod("is" + String.valueOf(ca), EMPTY_CLASS_ARRAY);
//                        System.out.println("method 'is" + String.valueOf(ca) + "' not found");
                    } catch (NoSuchMethodException e1) {
                    }
                }
                //only proceed if the method is found.
                if (m == null) {
//                    System.out.println("Accessor method not found for field '" + fields[i].getName() + "'");
                    continue;
                }
                accessibleMember.init(fields[i], m);
                processField(jdoFieldMap, jdoClass, accessibleMember, pkFields);
            }
        }
    }

    private List processCallBackAndListener(Class cls) {
        List eventsList = new ArrayList();
        if (cls.isAnnotationPresent(EntityListener.class)) { // process listener
            EntityListener el = (EntityListener)
                    cls.getAnnotation(EntityListener.class);
            Class elClass = el.value();
            Object listener = null;
            try {
                listener = elClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("EntityListener class '" +
                        elClass.getName() +
                        "' must have a public no args constructor.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("EntityListener class '" +
                        elClass.getName() +
                        "' must have a public no args constructor.");
            }

            Method[] methods = elClass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length != 1) continue;
                addEvents(method, eventsList, cls, listener);
            }
        }
        // process callbacks
        Method[] methods = cls.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getParameterTypes().length != 0) continue;
            addEvents(method, eventsList, cls, null);
        }

        return eventsList;
    }

    private void addEvents(Method method, List eventList,
                           Class cls, Object listener) {
        if (method.isAnnotationPresent(
                PrePersist.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.PRE_PERSIST_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PreRemove.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.PRE_REMOVE_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PreUpdate.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.PRE_UPDATE_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PostLoad.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.POST_LOAD_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PostPersist.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.POST_PERSIST_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PostRemove.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.POST_REMOVE_EVENT,
                    cls,
                    listener,
                    method));
        }
        if (method.isAnnotationPresent(
                PostUpdate.class)) {
            eventList.add(new EntityLifecycleEvent(
                    EntityClassLifecycleManager.POST_UPDATE_EVENT,
                    cls,
                    listener,
                    method));
        }
    }

    private void processEntityListener(Class cls) {
        EntityListener el = (EntityListener)
                cls.getAnnotation(EntityListener.class);
        Class elClass = el.value();
        Object listener = null;
        try {
            listener = elClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("EntityListener class '" +
                    elClass.getName() +
                    "' must have a public no args constructor.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("EntityListener class '" +
                    elClass.getName() +
                    "' must have a public no args constructor.");
        }
        ArrayList list = new ArrayList();

        Method[] methods = elClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            addEvents(method, list, cls, listener);
        }
    }

    private void processField(Map jdoFieldMap, JdoClass jdoClass,
            AccessibleMember property, Set pkFields) {
        //ignore certain fields
        if (property.isSynthetic()) return;
        if (Modifier.isStatic(property.getModifiers())) return;
        if (mdutils.isEnhancerAddedField(property.getName())) return;
        //check if there is a field for the property

        JdoField jdoField = getJdoField(jdoFieldMap, property, jdoClass);

        if (jdoField.persistenceModifier == 0
                && property.isAnnotationPresent(Transient.class)) {
            jdoField.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_NONE;
            return;
        }

        processId(property, jdoField, jdoClass, pkFields);

        if (property.isAnnotationPresent(javax.persistence.JoinColumn.class)) {
            javax.persistence.JoinColumn ca = property.getAnnotation(javax.persistence.JoinColumn.class);
            if (isValid(ca.name())) {
                jdoField.findCreate(JDBC_COLUMN, null, false).
                        findCreate(JDBC_COLUMN_NAME, ca.name(), true);
            }
        }
        if (property.isAnnotationPresent(javax.persistence.Column.class)) {
            javax.persistence.Column ca = property.getAnnotation(javax.persistence.Column.class);
            if (isValid(ca.name())) {
                jdoField.findCreate(JDBC_COLUMN, null, false).
                        findCreate(JDBC_COLUMN_NAME, ca.name(), true);
//                System.out.println("EJBAnnotationProcessor.processField(): "+ca.name());
            }
            if (ca.scale() != 0) {
                jdoField.findCreate(JDBC_COLUMN, null, false).
                        findCreate(JDBC_SCALE, "" + ca.scale(), false);
            }
            if (ca.length() != 255) {
                jdoField.findCreate(JDBC_COLUMN, null, false).
                        findCreate(JDBC_LENGTH, "" + ca.length(), false);
            }

            //check if there is already an index for the field and check if if it a single index that
            //is unique
            if (ca.unique()) {
                JdoExtension ext = JdoExtension.find(JDBC_INDEX, jdoClass.elements);
                if (!checkForUniqueIndexOnField(ext, jdoField)) {
                    //create it.
                    JdoExtension uniqueIndex = new JdoExtension();
                    uniqueIndex.key = JDBC_INDEX;
                    uniqueIndex.findCreate(FIELD_NAME, jdoField.name, false);
                    jdoClass.addElement(uniqueIndex);
                }
            }

//            if (ca.primaryKey()) jdoField.primaryKey = true;
            jdoField.findCreate(JDBC_COLUMN, null, false).findCreate(JDBC_NULLS,
                    ca.nullable() ? "true" : "false", true);
        }

        if (property.isAnnotationPresent(Basic.class)) {
            Basic basic = property.getAnnotation(Basic.class);
            jdoField.defaultFetchGroup =
                    (basic.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;


        } else if (property.isAnnotationPresent(OneToOne.class)) {
            OneToOne oto = property.getAnnotation(OneToOne.class);
            if (oto.cascade() != null) {
                setCascadeType(oto.cascade(), jdoField);
            }
            if (!oto.optional()) jdoField.nullValue = MDStatics.NULL_VALUE_EXCEPTION;
            jdoField.defaultFetchGroup = (oto.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;
        } else if (property.isAnnotationPresent(OneToMany.class)) {
            /**
             * This seems to depend if this is an uni or bidirection mapping.
             * For a bidirectional mapping then the default is a fk type relationship without a jointable.
             *
             * For a unidirectional the default is a jointable.
             *
             * We support the option of an {auto} backreference.
             */
            OneToMany otm = property.getAnnotation(OneToMany.class);
            if (otm.cascade() != null) {
                setCascadeType(otm.cascade(), jdoField);
            }
            jdoField.defaultFetchGroup = (otm.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;

            JdoCollection col = jdoField.collection;
            if (col == null) {
                col = jdoField.collection = new JdoCollection();
                jdoField.collection.parent = jdoField;
            }


            if (void.class != otm.targetEntity()) {
                col.elementType = otm.targetEntity().getName();
            }

            //must determine if this is an bidi or udi relationship
            if (isValid(otm.mappedBy())) {
                //this is an bidirection mapping
                col.findCreate(INVERSE, otm.mappedBy(), true);
            } else {
                //uni directional: use a jointable
                //if jointable is not specified then we must default to the spec name.
                //this will have to be done via a ejb3namegenerator
                JoinTable jtAno = property.getAnnotation(JoinTable.class);
                if (jtAno != null) {
                    if (jtAno.table().specified()) {
                        JdoExtension ltExt = col.findCreate(JDBC_LINK_TABLE, null, false);
                        ltExt.findCreate(JDBC_TABLE_NAME, jtAno.table().name(), true);
                    }
                }
            }

            processOrderBy(property, jdoField);
        } else if (property.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany mtm = property.getAnnotation(ManyToMany.class);
            if (mtm.cascade() != null) {
                setCascadeType(mtm.cascade(), jdoField);
            }
            jdoField.defaultFetchGroup = (mtm.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;

            JdoCollection col = jdoField.collection;
            if (col == null) {
                col = jdoField.collection = new JdoCollection();
                jdoField.collection.parent = jdoField;
            }

            if (void.class != mtm.targetEntity()) {
                col.elementType = mtm.targetEntity().getName();
            }

            //must determine if this is an bidi or udi relationship
            if (isValid(mtm.mappedBy())) {
                //this is an bidirection mapping
                col.findCreate(INVERSE, mtm.mappedBy(), true);
            } else {
                //uni directional: use a jointable
                //if jointable is not specified then we must default to the spec name.
                //this will have to be done via a ejb3namegenerator
                JoinTable jtAno = property.getAnnotation(JoinTable.class);
                if (jtAno != null) {
                    if (jtAno.table().specified()) {
                        JdoExtension ltExt = col.findCreate(JDBC_LINK_TABLE, null, false);
                        ltExt.findCreate(JDBC_TABLE_NAME, jtAno.table().name(), true);
                    }
                }
            }

            processOrderBy(property, jdoField);
        } else if (property.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne oto = property.getAnnotation(ManyToOne.class);
            if (oto.cascade() != null) {
                setCascadeType(oto.cascade(), jdoField);
            }
            jdoField.defaultFetchGroup = (oto.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;
        } else if (property.isAnnotationPresent(Serialized.class)) {
            Serialized oto = property.getAnnotation(Serialized.class);
            jdoField.defaultFetchGroup = (oto.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;
            jdoField.findCreate(EXTERNALIZER, "SERIALIZED", false);
        } else if (property.isAnnotationPresent(Embedded.class)) {
            jdoField.embedded = MDStatics.TRUE;
            jdoField.defaultFetchGroup = MDStatics.FALSE;
        } else if (property.isAnnotationPresent(Lob.class)) {
            Lob lob = property.getAnnotation(Lob.class);
            jdoField.defaultFetchGroup = (lob.fetch() == FetchType.EAGER) ? MDStatics.TRUE : MDStatics.FALSE;
        } else if (property.isAnnotationPresent(Version.class)) {
            if(jdoClass.getPCSuperClassQName() == null){
                JdoExtension optLock = JdoExtension.find(JDBC_OPTIMISTIC_LOCKING, jdoClass.elements);
                if (optLock == null) {
                    optLock = new JdoExtension();
                    optLock.key = JDBC_OPTIMISTIC_LOCKING;
                    jdoClass.addElement(optLock);
                }
                optLock.value = "version";
                optLock.findCreate(FIELD_NAME, jdoField.name, true);
            }
        } else {
            if (!mdutils.isPersistentModifiers(property.getModifiers())) {
                jdoField.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_NONE;
            } else if  (mdutils.isBasicType(property.getType())) {
                jdoField.defaultFetchGroup = MDStatics.TRUE;
            } else if (Serializable.class.isAssignableFrom(property.getType())) {
                jdoField.findCreate(EXTERNALIZER, "SERIALIZED", false);
                jdoField.defaultFetchGroup = MDStatics.TRUE;
            } else if (java.sql.Blob.class.equals(property.getType())
                    || java.sql.Clob.class.equals(property.getType())) {
                jdoField.defaultFetchGroup = MDStatics.FALSE;
            }
        }
    }

    private void processOrderBy(AccessibleMember property, JdoField jdoField) {
        OrderBy oBAno = property.getAnnotation(OrderBy.class);
        if (oBAno != null) {
            jdoField.findCreate(ORDERING, oBAno.value(), true);
        }
    }

    private boolean checkForUniqueIndexOnField(JdoExtension ext, JdoField jdoField) {
        if (ext != null && ext.nested != null && ext.nested.length == 2
                && JdoExtension.find(FIELD_NAME, jdoField.name, ext.nested) != null
                && JdoExtension.find(JDBC_UNIQUE, ext.nested) != null) {
            return true;
        }
        return false;
    }

    private void processId(AccessibleMember property, JdoField jdoField,
            JdoClass jdoClass, Set pkFields) {

        if (property.isAnnotationPresent(Id.class)) {
            jdoField.primaryKey = true;

            Id idAno = (Id) property.getAnnotation(Id.class);
            //the current keygenExt as per jdo metadata
            JdoExtension keygenExt = JdoExtension.find(JDBC_KEY_GENERATOR, jdoClass.elements);

            String generator = idAno.generator();
            if (idAno.generate() == GeneratorType.NONE) {
                if (keygenExt != null) {
                    //removing it by setting the key to -1
                    keygenExt.key = -1;
                }
            } else if (idAno.generate() == GeneratorType.SEQUENCE ||
                    idAno.generate() == GeneratorType.IDENTITY) {
                keygenExt = createKeyGen(jdoClass, keygenExt);
                keygenExt.value = GENERATOR_AUTOINC;
                if (generator != null && generator.length() > 0) {
                    System.out.println("EJBAnnotationProcessor.processId()2 -----------------------------------");
                }
            } else if (idAno.generate() == GeneratorType.AUTO) {
                keygenExt = createKeyGen(jdoClass, keygenExt);
                keygenExt.value = GENERATOR_HIGHLOW;
            } else if (idAno.generate() == GeneratorType.TABLE) {
                keygenExt = createKeyGen(jdoClass, keygenExt);
                if (generator != null && generator.length() > 0) {
                    keygenExt.value = generator;
                } else {
                    keygenExt.value= GENERATOR_HIGHLOW;
                }
            } else {
                throw new RuntimeException("Unsupported GeneratorType '"
                        + idAno.generate() + "'");
            }
        } else if (pkFields.contains(jdoField.name)) {
            jdoField.primaryKey = true;
        }
    }

    private JdoField getJdoField(Map jdoFieldMap, Member field, JdoClass jdoClass) {
        JdoField jdoField = (JdoField)jdoFieldMap.get(field.getName());
        if (jdoField == null) {
            jdoField = new JdoField();
            jdoField.name = field.getName();
            jdoField.parent = jdoClass;
            jdoClass.addElement(jdoField);
            jdoFieldMap.put(jdoField.name, jdoField);
        }
        return jdoField;
    }

    private Map createFieldMap(JdoClass jdoClass) {
        Map jdoFieldMap = new HashMap();
        if (jdoClass.elements != null) {
            JdoElement[] elements = jdoClass.elements;
            for (int i = 0; i < elements.length; i++) {
                JdoElement element = elements[i];
                if (element instanceof JdoField) {
                    jdoFieldMap.put(((JdoField)element).name, element);
                }
            }
        }
        return jdoFieldMap;
    }

    private boolean isValid(String value) {
        return !(value == null || value.length() == 0);
    }

    private void setCascadeType(CascadeType[] cct, JdoField jdoField) {
        for (int j = 0; j < cct.length; j++) {
            CascadeType cascadeType = cct[j];
            switch (cascadeType) {
                case ALL:
                    jdoField.cascadeType = MDStatics.CASCADE_ALL;
                    break;
                case MERGE:
                    jdoField.cascadeType += MDStatics.CASCADE_MERGE;
                    break;
                case PERSIST:
                    jdoField.cascadeType += MDStatics.CASCADE_PERSIST;
                    break;
                case REFRESH:
                    jdoField.cascadeType += MDStatics.CASCADE_REFRESH;
                    break;
                case REMOVE:
                    jdoField.cascadeType += MDStatics.CASCADE_REMOVE;
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("unhandled CascadeType '" + cascadeType + "'");
            }
        }
    }

    private JdoExtension createKeyGen(JdoClass jdoCls, JdoExtension keygenExt) {
        if (keygenExt == null) {
            keygenExt = new JdoExtension();
            keygenExt.key = JDBC_KEY_GENERATOR;
            jdoCls.addElement(keygenExt);
        }
        return keygenExt;
    }

    private void doPCSuperClass(JdoClass jdoClass, Class subClass) {
        Class superClass = subClass.getSuperclass();
        if (superClass == null) return;

        String superClassName = jdoClass.getPCSuperClassQName();
        if (superClassName == null && superClass.isAnnotationPresent(javax.persistence.Entity.class)) {
            jdoClass.pcSuperclass = superClass.getName();
        }
    }

    /**
     * Load class name using our loader.
     */
    private Class loadClass(String name) throws ClassNotFoundException {
        return ClassHelper.get().classForName(name, false, loader);
    }

    /**
     * Structure to provide info for a field and/or method.
     */
    private class AccessibleMember extends AccessibleObject implements Member {
        private Field field;
        private Method method;

        public void init(Field field, Method method) {
            this.field = field;
            this.method = method;
        }

        public Class getDeclaringClass() {
            return field.getDeclaringClass();
        }

        public int getModifiers() {
            return field.getModifiers();
        }

        public String getName() {
            return field.getName();
        }

        public boolean isSynthetic() {
            return field.isSynthetic();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            if (method == null) {
                return field.getAnnotation(annotationClass);
            } else {
                return method.getAnnotation(annotationClass);
            }
        }

        public Annotation[] getAnnotations() {
            if (method == null) {
                return field.getAnnotations();
            } else {
                return method.getAnnotations();
            }
        }

        public Annotation[] getDeclaredAnnotations() {
            if (method == null) {
                return field.getDeclaredAnnotations();
            } else {
                return method.getDeclaredAnnotations();
            }
        }


        public boolean isAccessible() {
            if (method == null) {
                return field.isAccessible();
            } else {
                return method.isAccessible();
            }
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            if (method == null) {
                return field.isAnnotationPresent(annotationClass);
            } else {
                return method.isAnnotationPresent(annotationClass);
            }
        }

        public void setAccessible(boolean flag) throws SecurityException {
            if (method == null) {
                field.setAccessible(flag);
            } else {
                method.setAccessible(flag);
            }
        }

        public Class getType() {
            return field.getType();
        }

    }
}
