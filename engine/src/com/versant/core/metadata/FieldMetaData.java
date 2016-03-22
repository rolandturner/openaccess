
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

import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.sco.*;
import com.versant.core.metadata.parser.JdoArray;
import com.versant.core.metadata.parser.JdoCollection;
import com.versant.core.metadata.parser.JdoField;
import com.versant.core.metadata.parser.JdoMap;
import com.versant.core.jdo.query.OrderNode;
import com.versant.core.jdo.externalizer.Externalizer;
import com.versant.core.util.classhelper.ClassHelper;

import javax.jdo.spi.PersistenceCapable;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.versant.core.common.*;

/**
 * Meta data for a persistent or transactional field that is common to all
 * DataStore's and the enhancer.
 */
public class FieldMetaData implements Externalizable, Comparable, MDStatics,
        VersantFieldMetaData
 {
    public static String NOT_INITIALISED = "NOT_INIT";

    /**
     * The class this field belongs to.
     */
    public ClassMetaData classMetaData;
    /**
     * The parsed info for this field (null if none i.e. meta data created
     * using reflection).
     */
    public JdoField jdoField;
    /**
     * The name of the field.
     */
    public String name;
    public String origName;
    /**
     * The relative field number of this field.
     */
    public int fieldNo;
    /**
     * The state field no for this field.
     */
    public int stateFieldNo;
    /**
     * The absolute fieldNo for this field.
     */
    public int managedFieldNo = -1;
    /**
     * Is this an artificial field created to hold some store specific
     * information (e.g. row version column values for a JDBC store)?
     */
    public boolean fake;
    /**
     * Is this a field which comes from an internally embedded structured
     * type (.NET struct)? Then this field must be managed by the FCO state manager.
     * This means references to structs and fields of structs have this set.
     */
    public boolean embeddedInternally;
    /**
     * If this is a fake field then these are the real fields it is linked
     * to. This is typically used to make sure the fake field is in all
     * the same fetch groups as its associated fields. This may be null in
     * which case the fake field is added to all fetch groups.
     */
    public FieldMetaData[] fakeLinks;
    /**
     * The persistence-modifier of the field.
     *
     * @see MDStatics#PERSISTENCE_MODIFIER_PERSISTENT
     * @see MDStatics#PERSISTENCE_MODIFIER_TRANSACTIONAL
     */
    public int persistenceModifier;
    /**
     * What sort of field is this? This is a general classification to make
     * it easier to work with the field.
     *
     * @see MDStatics#CATEGORY_SIMPLE
     * @see MDStatics#CATEGORY_COLLECTION
     * @see MDStatics#CATEGORY_ARRAY
     * @see MDStatics#CATEGORY_MAP
     * @see MDStatics#CATEGORY_REF
     * @see MDStatics#CATEGORY_TRANSACTIONAL
     * @see MDStatics#CATEGORY_EXTERNALIZED
     */
    public int category;
    /**
     * Is this field transformed into a different type when going to/from
     * storage? Null if not.
     */
    public Externalizer externalizer;
    /**
     * If this field transported using a CollectionDiff subclass
     * (e.g. List, Set and Map fields) ?
     */
    public boolean collectionDiffType;

    /**
     * Is this field persisted as part of the object itself in the datastore?
     * This is set by the store for the  class (e.g. the JdbcDataStore will
     * set this for fields stored in the main table and the VdsDataStore will
     * set this for fields stored with the object itself).
     */
    public boolean primaryField;
    /**
     * Is this field persisted separately to the object itself? This is set by
     * the store for the class (e.g. the JdbcDataStore will set this for fields
     * stored using a link table and the VdsDataStore will set this for fields
     * not stored as part of the Object e.g. Collections).
     */
    public boolean secondaryField;
    /**
     * The java type of this field.
     */
    public Class type;
    /**
     * The java type code of this field if it is a simple type (int, Integer,
     * String etc.).
     *
     * @see MDStatics
     */
    public int typeCode;

    /**
     * This is just the method name on state to call to retrieve this field.
     */
    public String stateGetMethodName;
    /**
     * If this field is an array then this is the type of the entries (null
     * otherwise).
     */
    public Class componentType;
    /**
     * The type code for componentType (0 if none).
     * @see MDStatics
     * @see #componentType
     */
    public int componentTypeCode;
    /**
     * If the type or componentType is a PC class then this is its meta data.
     * This is null for Collection's.
     *
     * @see #componentType
     * @see #elementType
     * @see #elementTypeMetaData
     */
    public ClassMetaData typeMetaData;
    /**
     * The java modifiers for this field (transient etc).
     *
     * @see java.lang.reflect.Modifier
     */
    public int modifiers;
    /**
     * Is this field part of the primary key?
     */
    public boolean primaryKey;
    /**
     * Cache for the value of this field in a new objectid-class instance
     *
     * @see #getPKDefaultValue()
     */
    private Object pkDefaultValue = NOT_INITIALISED;
    /**
     * If this field is part of the primary key and this class uses application
     * identity then this is the corresponding field from the objectid-class.
     */
    private transient Field objectidClassField;
    /**
     * How are nulls handled (null-value attribute)?
     *
     * @see MDStatics#NULL_VALUE_DEFAULT
     * @see MDStatics#NULL_VALUE_EXCEPTION
     * @see MDStatics#NULL_VALUE_NONE
     */
    public int nullValue;
    /**
     * Is this field in the default-fetch-group?
     */
    public boolean defaultFetchGroup;
    /**
     * Is this field in the default-fetch-group by default (true for ints etc
     * false for references and so on)?
     */
    public boolean defaultFetchGroupDefault;
    /**
     * Should this field embedded (hint to the store)?
     */
    public boolean embedded;

    /**
     * The parsed collection element (null if none).
     */
    public JdoCollection jdoCollection;
    /**
     * The parsed array element (null if none).
     */
    public JdoArray jdoArray;
    /**
     * The parsed map element (null if none).
     */
    public JdoMap jdoMap;
    /**
     * The type stored in the collection or the value type for a map or the
     * component type for an array.
     */
    public Class elementType;
    /**
     * The type code for elementType (0 if none).
     *
     * @see MDStatics
     * @see #elementType
     */
    public int elementTypeCode;
    /**
     * The meta data for elementType if it is a PC class (null otherwise).
     */
    public ClassMetaData elementTypeMetaData;
    /**
     * Should the collection or array elements (or values for a map)
     * be embedded?
     */
    public boolean embeddedElement;
    /**
     * The key type (null if not a map).
     */
    public Class keyType;
    /**
     * The type code for keyType (0 if none).
     *
     * @see MDStatics
     * @see #keyType
     */
    public int keyTypeCode;
    /**
     * The meta data for keyType if it is a PC class (null otherwise).
     */
    public ClassMetaData keyTypeMetaData;
    /**
     * Should the keys be embedded?
     */
    public boolean embeddedKey;
    /**
     * Is this an ordered collection?
     */
    public boolean ordered;
    /**
     * The fetch group for this field. This is the fetch group used when
     * the field is requested and no fetch group is specified.
     */
    public FetchGroup fetchGroup;

    /**
     * Are the objects referenced by this field dependent? This is only valid
     * for references, collections, arrays and maps etc. Dependent objects
     * are deleted when their owner (i.e. the object with this field) is
     * deleted. For a map this refers to the values.
     *
     * @see #dependentKeys
     */
    public boolean dependentValues;
    /**
     * This is only valid for map fields. It provides the same functionality
     * for the keys of the map as the the dependentValues flag does for the
     * values.
     *
     * @see #dependentValues
     */
    public boolean dependentKeys;

    /**
     * Is this fields value set automatically on commit? This feature is used
     * to implement row version and timestamp optimistic locking but can be
     * used for other purposes.
     */
    public int autoSet;

    /**
     * Extra store specific meta data for this field.
     */
    public transient Object storeField;
    /**
     * If this is a collection, array or map and this field is true then all
     * data must be provided in the diff instance instead of just the changes
     * on commit or flush. This is used for datastores like VDS that always
     * write everything.
     */
    public boolean includeAllDataInDiff;
    /**
     * Is this field a master (one) in a master/detail (one-to-many)
     * relationship
     */
    public boolean isMaster;
    /**
     * Is this field a detail (many) in a master/detail (one-to-many)
     * relationship? This field is set both for managed and unmanaged
     * relationships.
     */
    public boolean isDetail;
    /**
     * Is this field in a many-to-many relationship?
     */
    public boolean isManyToMany;
    /**
     * Is this field read only (e.g. the inverse side of a many-to-many)?
     */
    public boolean isReadOnly;
    /**
     * If isMaster, isDetail or isManyToMany is set then this is the fieldNo of
     * the field on the other side of the relationship.
     */
    public int inverseFieldNo = -1;
    /**
     * Is isMaster or isManyToMany is set then this indicates if
     * the relationship is managed by the SCOs or not. Note that this field
     * is filled in by the JdbcField involved.
     */
    public boolean managed;
    /**
     * If isMaster, isDetail or isManyToMany is set then this is the field
     * on the other side of the relationship.
     */
    public FieldMetaData inverseFieldMetaData;
    /**
     * If the field is a sco field.
     */
    public boolean scoField;
    /**
     * Should the field be returned as null if the referenced object is not
     * found? This is useful for references with all of their columns shared
     * with the primary key.
     */
    public boolean nullIfNotFound;
    /**
     * This is filled for unordered collection fields with an ordering
     * extension. Only required on server so is transient.
     */
    public transient OrderNode[] ordering;
    /**
     * The cascade type for this field.
     * This is currently only used by entitymanager.
     * @see MDStatics.CASCADE_ALL
     * @see MDStatics.CASCADE_MERGE
     * @see MDStatics.CASCADE_PERSIST
     * @see MDStatics.CASCADE_REFRESH
     * @see MDStatics.CASCADE_REMOVE
     */
    public int cascadeType = 0;

    private transient Comparator comparator;
    private transient boolean comparatorInitDone;
    private RuntimeException error;
    private long errorTime = Long.MAX_VALUE;
    private Object scoFactory;
    public VersantSCOFactory simpleSCOFactory;
    public VersantSCOCollectionFactory collectionFactory;
    public VersantSCOMapFactory mapFactory;
    public static final String NO_FIELD_TEXT = "{auto}";
    //If this is a fmd that was created from a embedded pc instance then this
    //is the link back to the original field
    public FieldMetaData origFmd;
    public FieldMetaData[] embeddedFmds;
    /**
     * If this field acts a nullIndicator
     */
    public FieldMetaData nullIndicatorFmd;
    public FieldMetaData[] managedEmbeddedFields;
    /**
     * Is this field embedded from another class into this class.
     */
    public boolean embeddedFakeField;
    /**
     * If this is a fake field that is created from the horizontal super class.
     */
    public boolean horizontalFakeField;
    /**
     * If this is a reference field and it should be id of it should be fetched as part
     * of the defaultFg
     */
    public boolean fetchOIDInDfg;

    public FieldMetaData() {
    }

    public String toString() {
        return "Field " + name;
    }

    /**
     * Return the fully qualified name of this field.
     */
    public String getQName() {
        return classMetaData.qname + "." + name;
    }

    /**
     * Return the type and fully qualified name of this field.
     */
    public String getTypeQName() {
        return type.getName().replace(';', ' ') + " " + getQName();
    }

    /**
     * Get the meta data for the class we reference. If this field is an
     * array, collection or map of a PC class then this will be the meta data
     * for the value class. If this is a simple reference then this will
     * be the meta data of the referenced class. Otherwise null is returned.
     * Always returns null for externalized fields.
     *
     * @see #typeMetaData
     * @see #elementTypeMetaData
     */
    public ClassMetaData getRefOrValueClassMetaData() {
        if (category == MDStatics.CATEGORY_EXTERNALIZED) return null;
        if (typeMetaData != null) return typeMetaData;
        return elementTypeMetaData;
    }

    /**
     * Add all fetch groups we belong to to a.
     */
    public void findFetchGroups(ArrayList a) {
        FetchGroup[] groups = classMetaData.fetchGroups;
        int n = groups.length;
        for (int i = 0; i < n; i++) {
            FetchGroup g = groups[i];
            if (g.contains(this)) a.add(g);
        }
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        out.println(is + "persistenceModifier = " +
                MDStaticUtils.toPersistenceModifierString(persistenceModifier));
        out.println(is + "category = " +
                MDStaticUtils.toCategoryString(category));
        out.println(is + "isPass1Field = " + primaryField);
        out.println(is + "isPass2Field = " + secondaryField);
        out.println(is + "fieldNo = " + fieldNo);
        out.println(is + "stateFieldNo = " + stateFieldNo);
        out.println(is + "fake = " + fake);
        StringBuffer s = new StringBuffer();
        s.append(is).append("fakeLinks = ");
        if (fakeLinks == null) {
            s.append("null");
        } else {
            for (int i = 0; i < fakeLinks.length; i++) {
                if (i > 0) s.append(", ");
                s.append(fakeLinks[i].name);
            }
            s.append(']');
        }
        out.println(s.toString());
        out.println(is + "type = " + type);
        out.println(is + "typeCode = " + typeCode);
        out.println(is + "componentType = " + componentType);
        out.println(is + "componentTypeCode = " + componentTypeCode);
        out.println(is + "typeMetaData = " + typeMetaData);
        out.println(is + "modifiers = " + modifiers);
        out.println(is + "primaryKey = " + primaryKey);
        out.println(is + "nullValue = " +
                MDStaticUtils.toNullValueString(nullValue));
        out.println(is + "defaultFetchGroup = " + defaultFetchGroup);
        out.println(
                is + "defaultFetchGroupDefault = " + defaultFetchGroupDefault);
        out.println(is + "embedded = " + embedded);
        out.println(is + "jdoCollection = " + jdoCollection);
        out.println(is + "jdoArray = " + jdoArray);
        out.println(is + "jdoMap = " + jdoMap);
        out.println(is + "elementType = " + elementType);
        out.println(is + "elementTypeCode = " + elementTypeCode);
        out.println(is + "elementTypeMetaData = " + elementTypeMetaData);
        out.println(is + "embeddedElement = " + embeddedElement);
        out.println(is + "keyType = " + keyType);
        out.println(is + "keyTypeCode = " + keyTypeCode);
        out.println(is + "keyTypeMetaData = " + keyTypeMetaData);
        out.println(is + "embeddedKey = " + embeddedKey);
        out.println(is + "ordered = " + ordered);
        out.println(is + "fetchGroup = " + fetchGroup);
        out.println(is + "dependentValues = " + dependentValues);
        out.println(is + "dependentKeys = " + dependentKeys);
        out.println(is + "autoSet = " + MDStaticUtils.toAutoSetString(autoSet));

        out.println(is + "isMaster = " + isMaster);
        out.println(is + "isDetail = " + isDetail);
        out.println(is + "isManyToMany = " + isManyToMany);
        out.println(is + "isReadOnly = " + isReadOnly);
        out.println(is + "inverseFieldNo = " + inverseFieldNo);

        out.println(is + "storeField = " + storeField);
    }

    /**
     * Sort by name. Do not change this ordering as it is used to order fields
     * for fieldNos and so on.
     */
    public int compareTo(Object o) {
        return name.compareTo(((FieldMetaData)o).name);
    }

    public void setType(Class type) {
        this.type = type;
        if (type == null) {
            typeCode = 0;
        } else {
            typeCode = MDStaticUtils.toTypeCode(type);
        }
    }

    public static void setStateMethodName(FieldMetaData fmd) {
        switch (fmd.category) {
            case MDStatics.CATEGORY_SIMPLE:
                switch (fmd.typeCode) {
                    case MDStatics.INT:

                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_INT;
                        break;
                    case MDStatics.LONG:

                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_LONG;
                        break;
                    case MDStatics.SHORT:

                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_SHORT;
                        break;
                    case MDStatics.STRING:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_STRING;
                        break;
                    case MDStatics.BOOLEAN:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_BOOLEAN;
                        break;
                    case MDStatics.BYTE:

                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_BYTE;
                        break;
                    case MDStatics.CHAR:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_CHAR;
                        break;
                    case MDStatics.DOUBLE:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_DOUBLE;
                        break;
                    case MDStatics.FLOAT:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_FLOAT;
                        break;
                    default:
                        fmd.stateGetMethodName = MDStatics.STATE_METHOD_OBJECT;
                        break;
                }
                break;
            default:
                fmd.stateGetMethodName = MDStatics.STATE_METHOD_OBJECT;
                break;
        }
    }

    public void setComponentType(Class componentType) {
        this.componentType = componentType;
        if (componentType == null) {
            componentTypeCode = 0;
        } else {
            componentTypeCode = MDStaticUtils.toTypeCode(componentType);
        }
    }

    public void setElementType(Class elementType) {
        this.elementType = elementType;
        if (elementType == null) {
            elementTypeCode = 0;
        } else {
            elementTypeCode = MDStaticUtils.toTypeCode(elementType);
        }
    }

    public void setKeyType(Class keyType) {
        this.keyType = keyType;
        if (keyType == null) {
            keyTypeCode = 0;
        } else {
            keyTypeCode = MDStaticUtils.toTypeCode(keyType);
        }
    }

    public void setScoField(boolean scoField) {
        this.scoField = scoField;
    }

    /**
     * Is this field a reference to another PC class? Note the PolyRef's are
     * not considered direct references.
     */
    public boolean isDirectRef() {
        return category == MDStatics.CATEGORY_REF;
    }

    public void setAutoSet(int autoSet) {
        this.autoSet = autoSet;
    }

    /**
     * Is the element type a persistent class?
     */
    public boolean isElementTypePC() {
        return elementTypeMetaData != null
                || elementType == /*CHFC*/Object.class/*RIGHTPAR*/
                || (elementType != null && elementType.isInterface());
    }

    /**
     * If the key field of the map is a PersistenceCapable instance.
     *
     * @see javax.jdo.spi.PersistenceCapable
     */
    public boolean isMapKeyRef() {
        return (category == MDStatics.CATEGORY_MAP ? isKeyTypePC() : false);
    }

    /**
     * If the value field of the map is a PersistenceCapable instance.
     *
     * @see javax.jdo.spi.PersistenceCapable
     */
    public boolean isMapValueRef() {
        return (category == MDStatics.CATEGORY_MAP ? isElementTypePC() : false);
    }

    /**
     * Must this field be removed from the state after commit of a transaction
     * with retainValues true? This is used to ensure that autoSet fields
     * are reread after an update or insert.
     */
    public boolean isClearOnRetainValues() {
        return autoSet == MDStatics.AUTOSET_NO;
    }

    /**
     * Must this field be included in all fetch groups? This is true for
     * fields that are always required (e.g. row version fields for optimistic
     * locking).
     * <p/>
     * App Id fields are also always included in all fetchGroups. This is done to
     * enable existence checking for a join.(if the owners id fields and the id fieds of the
     * joined row are the same. If the joined key fields are null then the ref is null)
     */
    public boolean includeInAllFGs() {
        // TODO make only version fields and timestamp fields used for locking
        return autoSet != AUTOSET_NO;
    }

    /**
     * Does this field have default-fetch-group set to true i.e. this was
     * done explicitly in the JDO meta data ?
     */
    public boolean isDefaultFetchGroupTrue() {
        return jdoField != null && jdoField.defaultFetchGroup == MDStatics.TRUE;
    }

    public boolean isJDODefaultFetchGroup() {
        if (isEmbeddedRef()) return false;
        if (defaultFetchGroupDefault || isDefaultFetchGroupTrue()) {
            if (!defaultFetchGroup) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Get the default value of this field in the objectid-class. This is
//     * only valid for primary key fields.
//     */
//    public Object getPKDefaultValue() {
//        if (Debug.DEBUG) {
//            if (!primaryKey || classMetaData.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
//                throw BindingSupportImpl.getInstance().internal(
//                        "Not an application identity class: " + getQName());
//            }
//        }
//        if (!primaryKey) {
//            throw BindingSupportImpl.getInstance().internal("Not a primary-key field: " +
//                    getQName());
//        }
//        if (pkDefaultValue == null) {
//            try {
//                Object o = classMetaData.objectIdClass.newInstance();
//                pkDefaultValue = classMetaData.objectIdClass.getField(getPkFieldName()).get(
//                        o);
//            } catch (Exception e) {
//                throw BindingSupportImpl.getInstance().exception("Unable to get primary key field from objectid-class: " +
//                        getQName() + ": " + e, e);
//            }
//        }
//        return pkDefaultValue;
//    }

    /**
     * Get the default value of this field in the objectid-class. This is
     * only valid for primary key fields.
     */
    public Object getPKDefaultValue() {
        if (Debug.DEBUG) {
            if (!primaryKey || classMetaData.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
                throw BindingSupportImpl.getInstance().internal(
                        "Not an application identity class: " + getQName());
            }
        }
        if (!primaryKey) {
            throw BindingSupportImpl.getInstance().internal("Not a primary-key field: " +
                    getQName());
        }
        if (pkDefaultValue == NOT_INITIALISED) {
            initPkFieldDefaultValue();
        }
        return pkDefaultValue;
    }

    private void initPkFieldDefaultValue() {
        try {
            Object inst;
            if (classMetaData.objectIdClass != null) {
                inst = classMetaData.objectIdClass.newInstance();
            } else {
                inst = classMetaData.cls.newInstance();
            }

            Class cls = /*CHFC*/inst.getClass()/*RIGHTPAR*/;
            String fieldName;
            if (classMetaData.isSingleIdentity) {
                fieldName = "key";
            } else {
                fieldName = origName;
            }
            Field field = null;
            for (;cls != null; cls = cls.getSuperclass()) {
                try {
                    field = cls.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    //ignore check superclass
                } catch (SecurityException e) {
                    //this is a not fatal exception, but will influence the behaviour of
                    //persisting appid instnances in certain scenario
                    throw e;
                }
            }

            if (field != null) {
                ClassHelper.get().setAccessible(field, true);
                setPkDefaultValue(field.get(inst));
            } else {
                setPkDefaultValue(null);
            }
        } catch (InstantiationException e) {
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw BindingSupportImpl.getInstance().runtime(e.getMessage(), e);
        }
    }

    public void setPkDefaultValue(Object pkDefaultValue) {
        this.pkDefaultValue = pkDefaultValue;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
//        if (Debug.DEBUG) {
//            System.out.println("%%% FieldMetaData.writeExternal " + getQName());
//        }
        out.writeObject(classMetaData);
        out.writeObject(jdoField);
        out.writeObject(name);
        out.writeObject(origName);
        out.writeInt(fieldNo);
        out.writeInt(stateFieldNo);
        out.writeInt(managedFieldNo);
        out.writeBoolean(fake);
        out.writeObject(fakeLinks);
        out.writeInt(persistenceModifier);
        out.writeInt(category);
        out.writeBoolean(primaryField);
        out.writeBoolean(secondaryField);
        out.writeInt(typeCode);
        if (MDStaticUtils.toSimpleClass(MDStaticUtils.toSimpleName(typeCode)) == null) {
            out.writeObject(type);
        }
        out.writeObject(componentType);
        out.writeInt(componentTypeCode);
        out.writeObject(typeMetaData);
        out.writeInt(modifiers);
        out.writeInt(nullValue);
        out.writeBoolean(defaultFetchGroup);
        out.writeBoolean(defaultFetchGroupDefault);
        out.writeBoolean(embedded);
        out.writeObject(jdoCollection);
        out.writeObject(jdoArray);
        out.writeObject(elementType);
        out.writeInt(elementTypeCode);
        out.writeObject(elementTypeMetaData);
        out.writeBoolean(embeddedElement);
        out.writeObject(keyType);
        out.writeInt(keyTypeCode);
        out.writeObject(keyTypeMetaData);
        out.writeBoolean(embeddedKey);
        out.writeBoolean(ordered);
        out.writeObject(fetchGroup);
        out.writeBoolean(dependentValues);
        out.writeBoolean(dependentKeys);
        out.writeInt(autoSet);
        out.writeBoolean(scoField);
        out.writeBoolean(primaryKey);
        out.writeBoolean(collectionDiffType);
        if (stateGetMethodName == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(stateGetMethodName);
        }
        out.writeBoolean(isMaster);
        out.writeBoolean(isDetail);
        out.writeBoolean(isManyToMany);
        out.writeBoolean(managed);
        out.writeBoolean(isReadOnly);
        out.writeInt(inverseFieldNo);
        out.writeObject(simpleSCOFactory);
        out.writeObject(collectionFactory);
        out.writeObject(mapFactory);
        out.writeBoolean(includeAllDataInDiff);
        out.writeObject(inverseFieldMetaData);
        out.writeObject(externalizer);

        out.writeObject(managedEmbeddedFields);
        out.writeObject(nullIndicatorFmd);
        out.writeBoolean(embeddedFakeField);
        out.writeObject(origFmd);
        out.writeObject(embeddedFmds);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        classMetaData = (ClassMetaData)in.readObject();
        jdoField = (JdoField)in.readObject();
        name = (String)in.readObject();
        origName = (String)in.readObject();
        fieldNo = in.readInt();
        stateFieldNo = in.readInt();
        managedFieldNo = in.readInt();
        fake = in.readBoolean();
        fakeLinks = (FieldMetaData[])in.readObject();
        persistenceModifier = in.readInt();
        category = in.readInt();
        primaryField = in.readBoolean();
        secondaryField = in.readBoolean();
        typeCode = in.readInt();
        type = MDStaticUtils.toSimpleClass(
                MDStaticUtils.toSimpleName(typeCode));
        if (type == null) {
            type = (Class)in.readObject();
        }
        componentType = (Class)in.readObject();
        componentTypeCode = in.readInt();
        typeMetaData = (ClassMetaData)in.readObject();
        modifiers = in.readInt();
        nullValue = in.readInt();
        defaultFetchGroup = in.readBoolean();
        defaultFetchGroupDefault = in.readBoolean();
        embedded = in.readBoolean();
        jdoCollection = (JdoCollection)in.readObject();
        jdoArray = (JdoArray)in.readObject();
        elementType = (Class)in.readObject();
        elementTypeCode = in.readInt();
        elementTypeMetaData = (ClassMetaData)in.readObject();
        embeddedElement = in.readBoolean();
        keyType = (Class)in.readObject();
        keyTypeCode = in.readInt();
        keyTypeMetaData = (ClassMetaData)in.readObject();
        embeddedKey = in.readBoolean();
        ordered = in.readBoolean();
        fetchGroup = (FetchGroup)in.readObject();
        dependentValues = in.readBoolean();
        dependentKeys = in.readBoolean();
        autoSet = in.readInt();
        scoField = in.readBoolean();
        primaryKey = in.readBoolean();
        collectionDiffType = in.readBoolean();
        if (in.readBoolean()) {
            stateGetMethodName = in.readUTF();
        } else {
            stateGetMethodName = null;
        }
        isMaster = in.readBoolean();
        isDetail = in.readBoolean();
        isManyToMany = in.readBoolean();
        managed = in.readBoolean();
        isReadOnly = in.readBoolean();
        inverseFieldNo = in.readInt();
        simpleSCOFactory = (VersantSCOFactory)in.readObject();
        collectionFactory = (VersantSCOCollectionFactory)in.readObject();
        mapFactory = (VersantSCOMapFactory)in.readObject();
        includeAllDataInDiff = in.readBoolean();
        inverseFieldMetaData = (FieldMetaData)in.readObject();
        externalizer = (Externalizer)in.readObject();

        managedEmbeddedFields = (FieldMetaData[]) in.readObject();
        nullIndicatorFmd = (FieldMetaData) in.readObject();
        embeddedFakeField = in.readBoolean();
        origFmd = (FieldMetaData)in.readObject();
        embeddedFmds = (FieldMetaData[])in.readObject();
    }

    /**
     * Get the name for use in comments in SQL files and so on. This will
     * include the name of the class if this field is in a subclass.
     */
    public String getCommentName() {
        if (classMetaData.pcSuperMetaData == null) return name;
        if (classMetaData.jdoClass == null) return getQName();
        return classMetaData.jdoClass.name + "." + name;
    }

    /**
     * Get the comparator for this field if it makes sense i.e. this
     * is a sorted Collection or Map with Comparator. This is cached.
     */
    public Comparator getComparator() {
        if (!comparatorInitDone) {
            comparatorInitDone = true;
            Class sortedSetClass = /*CHFC*/SortedSet.class/*RIGHTPAR*/;
            Class sortedMapClass = /*CHFC*/SortedMap.class/*RIGHTPAR*/;
            boolean set = sortedSetClass.isAssignableFrom(type);
            boolean map = !set && sortedMapClass.isAssignableFrom(type);
            if (set || map) {
                Object o = classMetaData.getMetaDataInstance();
                if (o != null) {
                    Field f = getReflectField();
                    try {
                        Object v = ClassHelper.get().getFieldValue(f, o);
                        if (v != null) {
                            if (set) {
                                comparator = ((SortedSet)v).comparator();
                            } else {
                                comparator = ((SortedMap)v).comparator();
                            }
                        }
                    } catch (Exception x) {
                        throw BindingSupportImpl.getInstance().invalidOperation("Unable get Field comparator with reflection: " +
                                getQName() + ": " + x, x);
                    }
                }
            }
        }
        return comparator;
    }

    /**
     * Get a Field instance for this field using reflection. This will have
     * setAccessible(true) called on it.
     */
    public Field getReflectField() {
        try {
            Field f = classMetaData.cls.getDeclaredField(origName);
            ClassHelper.get().setAccessible(f, true);
            return f;
        } catch (Exception x) {
            throw BindingSupportImpl.getInstance().invalidOperation("Unable get Field with reflection: " +
                    getQName() + ": " + x, x);
        }
    }

    public void addError(RuntimeException e, boolean quiet) {
        if (Debug.DEBUG) e.printStackTrace(System.out);
        if (error == null) {
            errorTime = System.currentTimeMillis();
            error = e;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
        if (!quiet) throw e;
    }

    public RuntimeException getFirstError() {
        return error;
    }

    public long getFirstErrorTime() {
        return errorTime;
    }

    public boolean hasErrors() {
        return error != null;
    }



    /**
     * The name of the field.
     */
    public String getName() 
	{
        return name;
    }

    /**
     * Is this an ordered collection?
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * The type code for elementType (0 if none).
     *
     * @see MDStatics
     * @see #elementType
     */
    public int getElementTypeCode() {
        return elementTypeCode;
    }

    /**
     * The absolute fieldNo for this field.
     */
    public int getManagedFieldNo() {
        return managedFieldNo;
    }

    /**
     * The meta data for keyType if it is a PC class (null otherwise).
     */
    public boolean isKeyTypePC() {
        return keyTypeMetaData != null || keyType == /*CHFC*/Object.class/*RIGHTPAR*/
                || (keyType != null && keyType.isInterface());
    }

    /**
     * The type code for keyType (0 if none).
     *
     * @see MDStatics
     * @see #keyType
     */
    public int getKeyTypeCode() {
        return keyTypeCode;
    }

    /**
     * The type stored in the collection or the value type for a map or the
     * component type for an array.
     */
    public Class getElementType() {
        return elementType;
    }

    /**
     * The key type (null if not a map).
     */
    public Class getKeyType() {
        return keyType;
    }

    /**
     * Is isMaster or isManyToMany is set then this indicates if
     * the relationship is managed by the SCOs or not. Note that this field
     * is filled in by the JdbcField involved.
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * Is this field a master (one) in a master/detail (one-to-many)
     * relationship
     */
    public boolean isMaster() {
        return isMaster;
    }

    /**
     * Is this field in a many-to-many relationship?
     */
    public boolean isManyToMany() {
        return isManyToMany;
    }

    /**
     * If isMaster, isDetail or isManyToMany is set then this is the fieldNo of
     * the field on the other side of the relationship.
     */
    public int getInverseFieldNo() {
        return inverseFieldNo;
    }

    /**
     * If isMaster, isDetail or isManyToMany is set then this is the field
     * on the other side of the relationship.
     */
    public VersantFieldMetaData getInverseFieldMetaData() {
        return inverseFieldMetaData;
    }

    /**
     * Return an SCO instance for this fmd. If the 'data' instance is already an sco then it might be returned as is
     * and just 'reset' for re-use. So it does not necc. create an new instance.
     */
    public VersantSimpleSCO createSCO(VersantPMProxy pm,
            VersantStateManager sm, FieldMetaData fmd,
            PersistenceCapable owner, Object data) {
        //create a new SCO data
        switch (fmd.category) {
            case MDStatics.CATEGORY_SIMPLE:
                return createSimpleSCO(pm, sm, fmd, owner, data);
            case MDStatics.CATEGORY_COLLECTION:
                return createCollectionSCO(pm, sm, fmd, owner, data);
            case MDStatics.CATEGORY_MAP:
                return createMapSCO(pm, sm, fmd, owner, data);
        }
        return null;
    }

    public VersantSimpleSCO createSimpleSCO(VersantPMProxy pm,
            VersantStateManager sm, FieldMetaData fmd,
            PersistenceCapable owner, Object data) {
        if (data == null) return null;
        if (data instanceof VersantAdvancedSCO) {
            VersantAdvancedSCO sco = (VersantAdvancedSCO)data;
            if (sco.getOwner() != null && sco.getOwner() == owner) {
                //may re-use
                sco.reset();
                return sco;
            }
        }
        //create a new SCO data
        return simpleSCOFactory.createSCO(owner, pm, sm, fmd, data);
    }

    public VersantSimpleSCO createCollectionSCO(
            VersantPMProxy pm,
            VersantStateManager sm, FieldMetaData fmd,
            PersistenceCapable owner, Object data) {
        if (data == null) return null;
        if (data instanceof VersantAdvancedSCO) {
            VersantAdvancedSCO sco = (VersantAdvancedSCO)data;
            if (sco.getOwner() != null && sco.getOwner() == owner) {
                //may re-use
                sco.reset();
                return sco;
            }
        }
        //create a new SCO data
        if (data instanceof CollectionData) {
            CollectionData collectionData = (CollectionData)data;
            return collectionFactory.createSCOCollection(owner, pm, sm, fmd,
                    collectionData);
        } else if (data instanceof Object[]) {
            Object[] objects = (Object[])data;
            CollectionData collectionData = new CollectionData();
            collectionData.valueCount = objects.length;
            collectionData.values = objects;
            return collectionFactory.createSCOCollection(owner, pm, sm, fmd,
                    collectionData);
        } else if (data instanceof Collection) {
            Collection collection = (Collection)data;
            return collectionFactory.createSCOCollection(owner, pm, sm, fmd,
                    collection);

        } else {
            throw BindingSupportImpl.getInstance().internal("data is not CollectionData, Object[] or Collection: " + fmd.getQName() +
                    " data " + data);
        }
    }

    public VersantSimpleSCO createMapSCO(VersantPMProxy pm,
            VersantStateManager sm, FieldMetaData fmd,
            PersistenceCapable owner, Object data) {
        if (data == null) return null;
        if (data instanceof VersantAdvancedSCO) {
            VersantAdvancedSCO sco = (VersantAdvancedSCO)data;
            if (sco.getOwner() != null && sco.getOwner() == owner) {
                //may re-use
                sco.reset();
                return sco;
            }
        }
        //create a new SCO data
        if (data instanceof MapData) {
            MapData mapData = (MapData)data;
            return mapFactory.createSCOHashMap(owner, pm, sm, fmd, mapData);
        } else if (data instanceof MapEntries) {
            MapEntries entries = (MapEntries)data;
            MapData mapData = new MapData();
            mapData.entryCount = entries.keys.length;
            mapData.keys = entries.keys;
            mapData.values = entries.values;
            return mapFactory.createSCOHashMap(owner, pm, sm, fmd, mapData);
        } else if (data instanceof Map) {
            return mapFactory.createSCOHashMap(owner, pm, sm, fmd, (Map)data);

        } else {
            throw BindingSupportImpl.getInstance().internal("data is not MapData, MapEntries or MAp: " + fmd.getQName() +
                    " data " + data);
        }
    }

    /**
     * If this is a collection, array or map and this field is true then all
     * data must be provided in the diff instance instead of just the changes
     * on commit or flush. This is used for datastores like VDS that always
     * write everything.
     */
    public boolean isIncludeAllDataInDiff() {
        return includeAllDataInDiff;
    }

    /**
     * Is this an artificial field created to hold some store specific
     * information (e.g. row version column values for a JDBC store)?
     */
    public boolean isFake() {
        return fake;
    }

    public void setScoFactory(Object factory) {
        this.scoFactory = factory;
    }

    public boolean checkCustomFactory() {
        if (scoFactory != null) {
            switch (category) {
                case MDStatics.CATEGORY_SIMPLE:
                    simpleSCOFactory = (VersantSCOFactory)scoFactory;
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                    collectionFactory = (VersantSCOCollectionFactory)scoFactory;
                    break;
                case MDStatics.CATEGORY_MAP:
                    mapFactory = (VersantSCOMapFactory)scoFactory;
                    break;
                default:
                    throw BindingSupportImpl.getInstance().runtime("SCO factory '" + scoFactory.getClass().getName() +
                            "' set on non SCO field. class: " + classMetaData.cls.getName() +
                            " field: " + name);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the {@link Externalizer transformer} for this field.
     */
    public Externalizer getSerializer(VersantPersistenceManagerImp pm) {
        if (Debug.DEBUG) {
            if (category != MDStatics.CATEGORY_EXTERNALIZED) {
                throw BindingSupportImpl.getInstance().internal("This field '"
                        + name + "' is not a 'Serialized' field");
            }
        }
        return externalizer;
    }



    public boolean isEmbeddedRef() {
        return embedded && category == MDStatics.CATEGORY_REF;
    }

    /**
     * If this field is part of the primary key and this class uses application
     * identity then this is the corresponding field from the objectid-class.
     */
    public Field getObjectidClassField() {
        if (objectidClassField == null) {
            Class idClass = classMetaData.objectIdClass;
            Field field = getRField(idClass);
            if (field == null) {
                throw BindingSupportImpl.getInstance().runtime("Application id class '"
                        + idClass.getName()
                        + "' must have field: 'public " + type.getName() +
                        " " + name + "'");
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                throw BindingSupportImpl.getInstance().runtime("Application id class '"
                        + idClass.getName()
                        + "' field '" + name + "' is not public");
            }

            if (type != field.getType()) {
                throw BindingSupportImpl.getInstance().runtime(
                        "Application id class '"
                        + idClass.getName()
                        + "' field '" + name + "' has wrong type '" +
                        type.getName() + "' (should be " + type + ")");
            }
            objectidClassField = field;
        }
        return objectidClassField;
    }

    private Field getRField(Class idClass) {
        for (; idClass != null; idClass = idClass.getSuperclass()) {
            Field[] fields = idClass.getFields();
            Field ans = null;
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getName().equals(getPkFieldName())) {
                    if (Modifier.isPublic(field.getModifiers())) return field;
                    ans = field;
                }

            }
            if (ans != null) return ans;
        }
        return null;
    }

    public FieldMetaData findEmbeddedFmd(String name) {
        if(embeddedFmds == null){
            return null;
        }
        // do a binary search since fields is sorted by name
        int low = 0;
        int high = embeddedFmds.length - 1;
        name = this.name+"/"+name;
        while (low <= high) {
            int mid = (low + high) / 2;
            FieldMetaData midVal = embeddedFmds[mid];
            String name2 = midVal.name;
            int cmp = name2.compareTo(name);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return midVal;
            }
        }
        return null;
    }

    public String getPkFieldName() {
        if (fake) return origFmd.name;
        return name;
    }

    public void setNullIndicatorFmd(FieldMetaData fmd) {
        if (isEmbeddedRef()) {
            if (nullIndicatorFmd == null) {
                nullIndicatorFmd = fmd;
            } else {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Attempting to set more that one 'Null-Indicator' " +
                        "field for Embedded field '"
                        + classMetaData.qname + "."  + name + "'. " +
                        "Please correct the metadata.");
            }
        } else {
            throw BindingSupportImpl.getInstance().internal(
                    "Not allowed to set NullIndicator '"
                    + fmd.classMetaData.qname + "." + fmd.name
                    + "' on a non-embedded ref: '"
                    + classMetaData.qname + "."  + name + "'");
        }
    }

    public boolean isManagedField() {
        if (!this.embeddedInternally) {
            // If field is not marked as to be embedded internally,
            // we check if it is a faked one. (id/version/type)
            if (this.fake)
                return false;
        }
        return true;
    }
}
