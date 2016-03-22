
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

import com.versant.core.jdo.QueryDetails;
import com.versant.core.metadata.parser.JdoClass;

import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.util.IntArray;

import javax.jdo.spi.PersistenceCapable;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import com.versant.core.common.*;

/**
 * Meta data for a class that is common to all DataStore's and the enhancer.
 */
public final class ClassMetaData implements Serializable, Comparable
 {

    /**
     * These are class hierarchy level setting for specifying when a ObjectNotFoundException must be
     * thrown if this instance is not found.
     */
    public static final int NULL_NO_ROW_PASSON = 2;
    public static final int NULL_NO_ROW_TRUE = 1;
    public static final int NULL_NO_ROW_FALSE = 0;

    /**
     * This is only used for refFields. If the ref Field is not found then return a null
     * instead of a VersantObjectNotFoundException. This is a classLevel setting.
     */
    public int returnNullForRowNotFound;
    /**
     * If this is true then this class is only ever used as and embedded instance.
     * This implies that no table and keygen should be created for these (if jdbc).
     */
    public boolean embeddedOnly;
    /**
     * The meta data we belong to.
     */
    public final ModelMetaData jmd;
    /**
     * The fully qualified name of the class.
     */
    public final String qname;
    /**
     * The name of the class without package.
     */
    public final String shortName;
    /**
     * The abstract schema name of the class. Default is shortName.
     */
    public String abstractSchemaName;
    /**
     * The original parsed .jdo meta data.
     */
    public transient JdoClass jdoClass;
    /**
     * The class.
     */
    public Class cls;
    /**
     * The package name with a trailing dot if not empty.
     */
    public String packageNameWithDot;
    /**
     * The unique ID for this class. This is generated from a hash of the
     * fully qualified class name. Duplicates are resolved by incrementing
     * the classId.
     *
     * @see #setClassId(int)
     */
    public int classId;
    /**
     * The classId as a String.
     *
     * @see #setClassId(int)
     */
    public String classIdString;
    /**
     * The index of this class in the classes array.
     *
     * @see ModelMetaData#classes
     */
    public int index;
    /**
     * The objectid-class (null if none).
     */
    public Class objectIdClass;
    /**
     * Is this class a Single Identity class
     */
    public boolean isSingleIdentity;
    /**
     * The persistent superclass (null if none).
     */
    public Class pcSuperClass;
    /**
     * The meta data for the persistent superclass (null if none).
     */
    public ClassMetaData pcSuperMetaData;
    /**
     * The meta data for the persistent class hierarchy. This includes
     * all our superclasses in order as well as our selves (i.e. a class
     * with no PC superclasses will have a pcHierarchy containing just
     * itself). This excludes horizontally mapped base classes i.e. for an
     * immediate subclass of a horizontally mapped base class pcHierarchy[0]
     * is the subclass.
     */
    public ClassMetaData[] pcHierarchy;
    /**
     * The meta data for our persistent subclasses (null if none).
     */
    public ClassMetaData[] pcSubclasses;
    /**
     * The topmost class in the hierarchy (i.e. pcHierarchy[0]). This excludes
     * horizontally mapped base classes i.e. for an immediate subclass of a
     * horizontally mapped base class top is the subclass.
     */
    public ClassMetaData top;
    /**
     * This flag is set if instances of the class are not allowed. An attempt
     * to persist an instance of a class with this flag set will trigger an
     * exception.
     */
    public boolean instancesNotAllowed;
    /**
     * The type of identity.
     *
     * @see MDStatics#IDENTITY_TYPE_APPLICATION
     * @see MDStatics#IDENTITY_TYPE_DATASTORE
     * @see MDStatics#IDENTITY_TYPE_NONDURABLE
     */
    public int identityType;
    /**
     * The persistent fields declared in this class (i.e. excluding
     * superclasses) in relative fieldNo order. This includes extra fake
     * fields created to hold information required by the store (e.g. row
     * version values for a JDBC class). The fake fields are always at the
     * end of this array after all the real fields.
     */
    public FieldMetaData[] fields;
    /**
     * The persistent fields declared in this class and in superclasses in
     * State fieldNo order. This includes extra fake fields.
     *
     * @see State
     */
    public FieldMetaData[] stateFields;
    /**
     * Fields as mapped from the horizontal superclass.
     */
    public FieldMetaData[] horizontalFields;
    /**
     * The number of real fields in this class. This must be filled in by the
     * store owning this class. This excludes fake fields.
     */
    public int realFieldCount;
    /**
     * The total number of fields in all of our superclasses (i.e. the
     * total of fields.length for all our superclasses). This is useful
     * to convert relative fieldNo's to State fieldNos.
     */
    public int superFieldCount;
    /**
     * The application primary key fields in alpha (field number) order.
     * This is null if not using application identity.
     */
    public FieldMetaData[] pkFields;
    /**
     * The field nos of the pk fields.
     */
    public int[] pkFieldNos;
    /**
     * If the class is using datastore identity, then this is the java type
     * code ({@link MDStatics.INT} etc) of the identity as if it was a
     * Java field.
     */
    public int datastoreIdentityTypeCode;
    /**
     * If the class is using datastore identity, then this is the java type
     * of the identity as if it was a Java field.
     */
    public Class datastoreIdentityType;
    /**
     * This is all the fields that must be managed. eg transaction or persistent.
     */
    public int[] stateFieldNos;
    /**
     * Array of the managed fields in abs field no order.
     */
    public FieldMetaData[] managedFields;
    /**
     * This is an utility array that is filled from 0 to the amount of managed
     * fields. It is used to pass as argument to the pc.
     */
    public int[] allManagedFieldNosArray;
    /**
     * The abs fieldNos of all fields that is either is pc ref or a collection of pc ref.
     */
    public int[] absPCTypeFields;
    /**
     * The fields that are marked as transactional but not persistent.
     */
    public int[] txFieldNos;
    public int[] txfieldManagedFieldNos;
    /**
     * This holds all the nonAutoSetStateFieldNos. These fields are stateFieldNos.
     */
    public int[] nonAutoSetStateFieldNos;
    /**
     * This holds all the autoSetStateFieldNos. These fields are managedFieldNos.
     */
    public int[] autoSetManagedFieldNos;
    /**
     * The fields of the persistent fields that may contain direct (e.g.
     * foreign key) references to other PC classes. This must be filled in
     * by the dataStore owning this class. This information is used to sort
     * graphs of persistent objects for persisting in the correct order
     * (e.g. to avoid tripping database integrity constraints).
     */
    public int[] directRefStateFieldNos;
    /**
     * The reference fields that are used to complete collections
     * mapped using a foreign key in the element table. Null if none.
     */
    public int[] fkCollectionRefStateFieldNos;
    /**
     * Must orphans be deleted? An instance is considered an orphan if it
     * is on the many side of at least one one-to-many (master detail)
     * relationship and all of its back references are null (i.e. it has
     * no parents).
     */
    public boolean deleteOrphans;
    /**
     * This is true if this class or any of its superclasses has any
     * secondary fields.
     */
    public boolean hasSecondaryFields;
    /**
     * The fields that must be persisted on pass 2 in fieldNo order. This
     * is filled using the secondaryField flag on FieldMetaData. Note that
     * NOT these are relative fieldNos.
     *
     * @see FieldMetaData#secondaryField
     */
    public transient int[] pass2Fields;

    public int[] pass2AbsFieldNos;
    /**
     * Is this class read-only?
     */
    public boolean readOnly;
    /**
     * The caching strategy for this class (one of the CACHE_STRATEGY_xxx
     * constants).
     */
    public int cacheStrategy;
    /**
     * Flag to indicate that the cacheStrategy was all and all instances
     * have been read once.
     */
    public boolean cacheStrategyAllDone;
    /**
     * The name of the DataStore this class belongs to. Any DataStore may
     * have multiple names for different physical stores. This may be
     * null indicating the default.
     */
    public transient String dataStoreName;
    /**
     * The fetch groups.
     */
    public FetchGroup[] fetchGroups;
    public FetchGroup[] sortedFetchGroups;
    public transient ArrayList fgTmp;
    public transient HashMap nameGroupMap;
    /**
     * The referenced objects fetch group (null if none i.e. this class and
     * its superclasses and subclasses have no references to other PC objects).
     * This includes polyrefs and collections and is used to do reachability
     * searches.
     */
    public FetchGroup refFetchGroup;
    /**
     * The dependent objects fetch group (null if none i.e. this class and
     * its superclasses and subclasses have no references to dependent PC
     * objects). This is used to do a reachability search when deleting.
     */
    public FetchGroup depFetchGroup;
    /**
     * This fetch group contains all fields that must be filled in the
     * original state (e.g. jdoVersion etc.) when persisting changes to
     * instances. It will be null if the class has no such fields (e.g.
     * using optimistic locking 'none').
     */
    public FetchGroup reqFetchGroup;
    /**
     * The many-to-many fetch group (null if none i.e. this class and
     * its superclasses and subclasses have no many-to-many managed
     * collection fields). This is used to clear these fields when deleting.
     * It also includes all fields that must be present to persist changes
     * to an instance.
     */
    public FetchGroup managedManyToManyFetchGroup;

    public FetchGroup retrieveReferencesHollowFetchGroup;
    public FetchGroup hollowFetchGroup;
    /**
     * The total number of FetchGroups in all of our superclasses (i.e. the
     * total of fetchGroups.length for all our superclasses). This is used
     * to convert relative fetch group indexes to State fetch group indexes.
     */
    public int superFetchGroupCount;
    /**
     * This contains all the fetch groups sorted in fieldNo order. This is
     * used to find a fetch group containing a particular set of fieldNos.
     */
    public transient List allFetchGroups;
    public transient Comparator allFComparator = new AllFetchGroupComp();
    /**
     * Extra store specific meta data.
     */
    public transient Object storeClass;
    /**
     * Does this class use changed optimistic locking i.e. include the original
     * values of changed fields in the where clause for JDBC.
     */
    public boolean changedOptimisticLocking;
    /**
     * If this class uses version or timestamp optimistic locking then this
     * is the field holding the value.
     */
    public FieldMetaData optimisticLockingField;
    /**
     * Factory for State and OID instances for this class. This is set by
     * the StorageManagerFactory.
     */
    public StateAndOIDFactory stateAndOIDFactory;
    /**
     * The oid class name for hyperdrive.
     */
    public String oidClassName;
    /**
     * The state class name.
     */
    public String stateClassName;
    /**
     * The fieldNo's of the sco fields. This is stateFieldNo's
     */
    public int[] scoFieldNos;
    /**
     * Represents a struct field in .NET which is to be embedded internally only.
     */
    public boolean structType;



    /**
     * If we or any of our superclasses have any autoSet fields then this
     * is true.
     *
     * @see FieldMetaData#autoSet
     */
    public boolean hasAutoSetFields;
    /**
     * These are the absolute field numbers of the fields that are loaded into
     * an instance when it is populated with the default fetch group. This
     * may be a subset of the fields in the FetchGroup instance for the DFG
     * as extra fields (e.g. OIDs for references) may be fetched as well but
     * not loaded.
     *
     * @see com.versant.core.jdo.PCStateMan#loadDFGIntoPC
     */
    public int[] dfgAbsFieldNos;
    /**
     * The same as {@link ClassMetaData#dfgAbsFieldNos} but only the state field
     * numbers instead of abs field numbers.
     *
     * @see #dfgAbsFieldNos
     */
    public int[] dfgStateFieldNos;
    /**
     * The position of this class in the topological sort of the graph created
     * by following direct references between classes. Example: If A
     * references B, then A.index < B.index and A must be deleted before B
     * to avoid tripping constraints.
     *
     * @see #referenceGraphCycle
     */
    public int referenceGraphIndex;
    /**
     * If this class is involved in a reference cycle with other classes then
     * this flag will be set (e.g. this is true for classes A -> B -> C -> A).
     * Constraints must not be generated for any references between classes
     * with this flag set.
     *
     * @see #referenceGraphIndex
     */
    public boolean referenceGraphCycle;
    /**
     * This is true if the keys are created using a key generator.
     */
    public boolean useKeyGen;
    /**
     * Must a flush be done if getObjectId is called on a new instance of
     * this class? This is set for classes using post-insert key generators.
     * This is also used to decide if a full graph sort is required on persist.
     */
    public boolean postInsertKeyGenerator;
    /**
     * If the DataStore requires all fields of a dirty instance to store it
     * and not just the dirty fields then this flag is true
     * (e.g. VdsDataStore).
     */
    public boolean storeAllFields;
    /**
     * If the DataStore requires notification before a p-clean instance is made
     * dirty or deleted in a datastore tx then this flag is true (e.g.
     * VdsDataStore).
     */
    public boolean notifyDataStoreOnDirtyOrDelete;

    private HashMap namedQueryMap; // query name -> QueryDetails

    /**
     * This is a total of all the subClasses (direct and indirect);
     */
    public transient int totalNoOfSubClasses;

    private transient Object metaDataInstance;
    private transient RuntimeException error;
    private transient long errorTime = Long.MAX_VALUE;
    /**
     * This is a List that includes this and all the subCmds of all children and sub-children etc.
     */
    private transient List heirarchyList;
    /**
     * If this class is horizontal mapped. i.e. its fields should be in the table
     * of the subclass.
     */
    public boolean horizontal;
    /**
     * The metadata if this class is the subclass of a horizontal super class.
     */
    public ClassMetaData horizontalCMD;
    /**
     * This can be used to override the need for a objectIdClass for appid instances
     */
    private boolean objectIdClasssRequired = true;

    public ClassMetaData(JdoClass jdoClass, ModelMetaData jmd) {
        this.jdoClass = jdoClass;
        this.jmd = jmd;
        qname = jdoClass.getQName();
        int i = qname.lastIndexOf('.');
        shortName = i < 0 ? qname : qname.substring(i + 1);
        abstractSchemaName = shortName;
    }

    /**
     * Calculate and set the superFieldCount and superFetchGroup value
     * for this class and recursively all of its subclasses. This also
     * initializes various arrays of fieldNos etc.
     */
    public void calcSuperCounts() {
        if (pcSuperMetaData != null) {
            superFieldCount = pcSuperMetaData.superFieldCount +
                    pcSuperMetaData.fields.length;
            superFetchGroupCount = pcSuperMetaData.superFetchGroupCount +
                    pcSuperMetaData.fetchGroups.length;

            FieldMetaData[] superStateFields = pcSuperMetaData.stateFields;
            int n = superStateFields.length;
            stateFields = new FieldMetaData[n + fields.length];
            System.arraycopy(superStateFields, 0, stateFields, 0, n);
            System.arraycopy(fields, 0, stateFields, n, fields.length);
        } else {
            stateFields = fields;
        }

        if (pcSubclasses != null) {
            for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                pcSubclasses[i].calcSuperCounts();
            }
        }

        // this may happen if there have been previous errors
        if (stateFields == null) return;

        // see if we have any timestamp or version fields
        for (int i = stateFields.length - 1; i >= 0; i--) {
            FieldMetaData f = stateFields[i];
            if (hasAutoSetFields = f.autoSet != MDStatics.AUTOSET_NO) break;
        }

        // find all the direct references
        IntArray a = new IntArray(stateFields.length);
        for (int i = stateFields.length - 1; i >= 0; i--) {
            FieldMetaData f = stateFields[i];
            if (f.isDirectRef()) a.add(i);
        }
        directRefStateFieldNos = a.toArray();
    }

    /**
     * This is called at the end of all metadata creation. This inits all the fieldnos
     * arrays.
     */
    public void initMDFields() {
            if (pcSuperMetaData != null) {
                initStateFields();
                createStateFieldNos();

                pass2Fields = mergeFieldsNos(pcSuperMetaData.pass2Fields,
                        pass2Fields);

                pkFields = pcSuperMetaData.pkFields;
                pkFieldNos = pcSuperMetaData.pkFieldNos;
            } else {
                initStateFields();
                /**
                 * Create the fieldNo array for the state fields.
                 */
                createStateFieldNos();

                if (pkFields != null) {
                    int n = pkFields.length;
                    int[] pkFieldNos = new int[n];
                    for (int i = 0; i < n; i++) {
                        pkFieldNos[i] = pkFields[i].managedFieldNo;
                    }
                    this.pkFieldNos = pkFieldNos;
                }
            }
            hasSecondaryFields = pass2Fields != null && pass2Fields.length > 0;

            if (pcSubclasses != null) {
                for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                    pcSubclasses[i].initMDFields();
                }
            }
        }

    /**
     * This will iterate through the fmd's of this state and set the state
     * field no' s.
     */
    private void initStateFields() {
        if (fields == null) return;  // possible if previous error
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            field.stateFieldNo = (field.fieldNo + superFieldCount);
        }
    }

    /**
     * This creates various fieldNos arrays.
     */
    private void createStateFieldNos() {
        if (stateFields == null) return; // possible if previous error
        stateFieldNos = new int[stateFields.length];
        ArrayList mList = new ArrayList();
        IntArray nAutoFs = new IntArray();
        IntArray autoFs = new IntArray();

        /**
         * This is to iterate through all the fields for this PC instance
         * and set their field no as defined by the spec. This implies that if
         * a Class defines 2 fields 'a' and 'b' that 'a' must be field no '0'
         * and 'b' must be field no '1'.
         */
        if (pcSuperMetaData != null) {
            mList.addAll(Arrays.asList(pcSuperMetaData.managedFields));
        }
        // Logically this seems to be an 'else if'
        if (horizontalCMD != null) {
            mList.addAll(Arrays.asList(horizontalFields));
        }
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.isManagedField()) {
                mList.add(field);
            }
        }

        managedFields = new FieldMetaData[mList.size()];
        mList.toArray(managedFields);

        IntArray txManagedFNOs = new IntArray();
        IntArray allMFA = new IntArray(managedFields.length);
        for (int i = 0; i < managedFields.length; i++) {
            FieldMetaData mField = managedFields[i];
            mField.managedFieldNo = i;
            if (!(mField.isEmbeddedRef() && mField.typeMetaData.structType)) {
                allMFA.add(i);
            }
            if (mField.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                txManagedFNOs.add(mField.managedFieldNo);
            }
        }
        allManagedFieldNosArray = allMFA.toArray();

        for (int i = 0; i < stateFields.length; i++) {
            FieldMetaData stateField = stateFields[i];
            if (stateField.autoSet == MDStatics.AUTOSET_NO) {
                nAutoFs.add(stateField.stateFieldNo);
            } else {
                if (stateField.managedFieldNo != -1)
                    autoFs.add(stateField.managedFieldNo);
            }
            stateFieldNos[i] = stateField.stateFieldNo;
        }

        txfieldManagedFieldNos = txManagedFNOs.toArray();

        IntArray tmpPass2FieldsAbs = new IntArray();
        for (int i = 0; i < mList.size(); i++) {
            FieldMetaData fieldMetaData = (FieldMetaData)mList.get(i);
            if (fieldMetaData.secondaryField) {
                tmpPass2FieldsAbs.add(fieldMetaData.managedFieldNo);
            }
        }
        pass2AbsFieldNos = tmpPass2FieldsAbs.toArray();
        tmpPass2FieldsAbs = null;

        nonAutoSetStateFieldNos = nAutoFs.toArray();
        autoSetManagedFieldNos = autoFs.toArray();

        IntArray dfgFieldNoArray = new IntArray();
        IntArray dfgStateFieldNoArray = new IntArray();
        for (int i = 0; i < managedFields.length; i++) {
            FieldMetaData managedField = managedFields[i];
            if (managedField.isJDODefaultFetchGroup()) {
                dfgFieldNoArray.add(managedField.managedFieldNo);
                dfgStateFieldNoArray.add(managedField.stateFieldNo);
            }
        }
        dfgAbsFieldNos = dfgFieldNoArray.toArray();
        dfgStateFieldNos = dfgStateFieldNoArray.toArray();

        IntArray absPCTypeFieldArray = new IntArray();
        for (int i = 0; i < managedFields.length; i++) {
            FieldMetaData field = managedFields[i];
            if (field.elementTypeMetaData != null
                    || (field.typeMetaData != null && (! field.embedded))
                    || field.keyTypeMetaData != null) {
                absPCTypeFieldArray.add(field.managedFieldNo);
            }
        }
        absPCTypeFields = absPCTypeFieldArray.toArray();

        fillSCOFieldNos();
        fillValueTypeFieldNos();
        fillTxFields();
    }

    /**
     * Fill in the scoFieldNos array.
     */
    private void fillSCOFieldNos() {
        FieldMetaData[] fields = managedFields;
        final int numFields = fields.length;
        IntArray fieldNos = new IntArray(numFields);
        for (int i = 0; i < numFields; i++) {
            FieldMetaData fmd = fields[i];
            //TODO Fix this: the 'MDStatics.CATEGORY_ARRAY' check is due to a bug. If this is not set then array does not work.
            if (fmd.scoField  && fmd.category != MDStatics.CATEGORY_ARRAY) {
                fieldNos.add(fmd.stateFieldNo);
            }
        }
        scoFieldNos = fieldNos.toArray();
    }

    // set the fieldNo for each field
    private void fillTxFields() {
        FieldMetaData[] fields = managedFields;
        IntArray txFields = new IntArray();
        for (int i = 0; i < fields.length; i++) {
            //add all fields mark as transactional to the txFields.
            if (fields[i].persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                txFields.add(fields[i].stateFieldNo);
            }
        }
        txFieldNos = txFields.toArray();
    }

    /**
     * Fill in the valueTypeFieldNos array for cmd.
     */
    private void fillValueTypeFieldNos() {

    }

    /**
     * Merge two arrays as one. Before the local fieldNo's is merged it is
     * bumped up the the superFieldCount.
     *
     * @param s
     * @param l
     */
    private int[] mergeFieldsNos(int[] s, int[] l) {
        int[] n = null;
        if (s == null && l == null) {
        } else if (s == null) {
            n = l;
            for (int i = 0; i < l.length; i++) {
                l[i] = l[i] + superFieldCount;
            }
        } else if (l == null) {
            n = s;
        } else {
            n = new int[s.length + l.length];
            System.arraycopy(s, 0, n, 0, s.length);
            for (int i = 0; i < l.length; i++) {
                l[i] = l[i] + superFieldCount;
            }
            System.arraycopy(l, 0, n, s.length, l.length);
        }
        return n;
    }

    /**
     * Calculate the pcHierarchy for this class and recursively all of its
     * subclasses. This also copies then identityType field down to
     * subclasses.
     */
    public void calcPcHierarchy() {
        if (pcSuperMetaData != null) {
            ClassMetaData[] superPcHierarchy = pcSuperMetaData.pcHierarchy;
            int n = superPcHierarchy.length;
            pcHierarchy = new ClassMetaData[n + 1];
            System.arraycopy(superPcHierarchy, 0, pcHierarchy, 0, n);
            pcHierarchy[n] = this;
            identityType = pcSuperMetaData.identityType;
        } else {
            pcHierarchy = new ClassMetaData[]{this};
        }
        top = pcHierarchy[0];
        if (pcSubclasses != null) {
            for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                pcSubclasses[i].calcPcHierarchy();
            }
        }
    }

    /**
     * This is called on the base cmd of the hierarchy.
     * <p/>
     * The idea is that pc subs must have the same cache strat as the least derived.
     * A strat of yes and all is handled as the same in this case.
     *
     * @param strat
     */
    private void overRideCacheStrategy(int strat) {
        if (cacheStrategy != strat) {
            switch (cacheStrategy) {
                case MDStatics.CACHE_STRATEGY_NO:
                    cacheStrategy = strat;
                    break;
                case MDStatics.CACHE_STRATEGY_YES:
                    if (strat != MDStatics.CACHE_STRATEGY_ALL) {
                        cacheStrategy = strat;
                    }
                    break;
                case MDStatics.CACHE_STRATEGY_ALL:
                    if (strat != MDStatics.CACHE_STRATEGY_YES) {
                        cacheStrategy = strat;
                    }
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "Unknown caching strategy : '" + strat + "'");
            }
        }
        if (pcSubclasses != null) {
            for (int i = 0; i < pcSubclasses.length; i++) {
                pcSubclasses[i].overRideCacheStrategy(strat);
            }
        }
    }

    public void overRideCacheStrategy() {
        if (pcSuperMetaData != null) {
            throw BindingSupportImpl.getInstance().internal("This is only allowed to be " +
                    "called on the base of hierarchy.");
        }
        overRideCacheStrategy(cacheStrategy);
    }

    /**
     * Get meta data for the field fname or null if none. This will only
     * find real fields declared in this class or one of our superclasses.
     */
    public FieldMetaData getFieldMetaData(String fname) {
        // do a binary search since fields is sorted by name
        int low = 0;
        int high = realFieldCount - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            FieldMetaData midVal = fields[mid];
            int cmp = midVal.name.compareTo(fname);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return midVal;
            }
        }
        if (horizontalCMD != null) {
            low = 0;
            high = horizontalFields.length - 1;
            while (low <= high) {
                int mid = (low + high) / 2;
                FieldMetaData midVal = horizontalFields[mid];
                int cmp = midVal.name.compareTo(fname);
                if (cmp < 0) {
                    low = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    return midVal;
                }
            }
        }
        if (horizontalCMD != null) {
            low = 0;
            high = horizontalFields.length - 1;
            while (low <= high) {
                int mid = (low + high) / 2;
                FieldMetaData midVal = horizontalFields[mid];
                int cmp = midVal.origName.compareTo(fname);
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
        if (pcSuperMetaData == null) return null;
        return pcSuperMetaData.getFieldMetaData(fname);
    }

    /**
     * Get the fetch group with gname or null if none.
     */
    public FetchGroup getFetchGroup(String gname) {
        if (gname.equals(FetchGroup.DFG_NAME)) return fetchGroups[0];
        // do a binary search since groups is sorted by name
        int low = 1;
        int high = sortedFetchGroups == null ? 0 : sortedFetchGroups.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            FetchGroup midVal = sortedFetchGroups[mid];
            int cmp = midVal.name.compareTo(gname);
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

    /**
     * Add a new FetchGroup to the allFetchGroups array maintaining the
     * sort order.
     */
    private void addToAllFetchGroups(FetchGroup fg) {
        allFetchGroups.add(fg);
        Collections.sort(allFetchGroups, allFComparator);
    }

    /**
     * Get the fetch group with index fgIndex. If the group does not match
     * clsId then the super fetch group is used and so on recursively. If
     * no fetch group is found a JDOGenieFatalInternalException is thrown.
     */
    public final FetchGroup getFetchGroup(int fgIndex, int clsId) {
        FetchGroup fg = fetchGroups[fgIndex];
        for (; fg.classMetaData.classId != clsId;) {
            ClassMetaData smd = fg.classMetaData.pcSuperMetaData;
            if (smd == null) {
                ClassMetaData t = jmd.getClassMetaData(clsId);
                throw BindingSupportImpl.getInstance().internal("No FetchGroup found to match fgIndex " + fgIndex +
                        " classId " + clsId + " (" +
                        (t == null ? null : t.toString()) + ")" +
                        " on " + this + " (classId " + classId + ")");
            }
            fg = smd.fetchGroups[fgIndex];
        }
        return fg;
    }

    /**
     * Init the stateFieldNo array for all our FetchGroup's and create
     * allFetchGroups.
     */
    public void finishFetchGroups() {
        int n = fetchGroups == null ? 0 : fetchGroups.length;
        allFetchGroups = new ArrayList(n);
        for (int i = n - 1; i >= 0; i--) {
            FetchGroup g = fetchGroups[i];
            g.finish();
            addToAllFetchGroups(g);
        }
    }

    /**
     * Create the subFetchGroups array on all fetch groups.
     */
    public void finishFetchGroups2() {
        if (pcSubclasses == null) return;
        int sclen = pcSubclasses.length;
        ArrayList a = new ArrayList(sclen);
        int n = fetchGroups.length;
        for (int i = n - 1; i >= 0; i--) {
            a.clear();
            FetchGroup g = fetchGroups[i];
            for (int j = 0; j < sclen; j++) {
                ClassMetaData sc = pcSubclasses[j];
                FetchGroup sub = sc.findSubFetchGroup(g);
                if (sub != null) a.add(sub);
            }
            g.subFetchGroups = new FetchGroup[a.size()];
            a.toArray(g.subFetchGroups);
        }
    }

    private FetchGroup findSubFetchGroup(FetchGroup superGroup) {
        for (int i = 0; i < fetchGroups.length; i++) {
            FetchGroup g = fetchGroups[i];
            if (g.superFetchGroup == superGroup) return g;
        }
        return null;
    }

    /**
     * Sort by classId. Do not change this ordering.
     */
    public int compareTo(Object o) {
        ClassMetaData cmd = (ClassMetaData)o;
        if (classId < cmd.classId) return -1;
        if (classId > cmd.classId) return +1;
        return 0;
    }

    /**
     * Create a new empty OID for this class.
     *
     * @param resolved Is this a resolved OID?
     * @see OID#isResolved
     * @see OID#resolve
     */
    public OID createOID(boolean resolved) {
        return stateAndOIDFactory.createOID(this, resolved);
    }

    /**
     * Create a new empty State for this class.
     */
    public State createState() {
        return stateAndOIDFactory.createState(this);
    }

    /**
     * Create an OID for a new object of this class.
     */
    public NewObjectOID createNewObjectOID() {
        return stateAndOIDFactory.createNewObjectOID(this);
    }

    /**
     * Is cmd one of our ancestors or ourself?
     */
    public boolean isAncestorOrSelf(ClassMetaData cmd) {
        if (cmd == this) return true;
        for (ClassMetaData c = pcSuperMetaData;
             c != null; c = c.pcSuperMetaData) {
            if (c == cmd) return true;
        }
        // If ClassMetaData is not unique (but containing
        // the absolutely same description) we check more logically.
        ClassMetaData x = this;
        do {
            if (x.classId == cmd.classId && x.qname.equals(cmd.qname))
                return true;
            x = x.pcSuperMetaData;
        }
        while (x != null);
        return false;
    }

    public String toString() {
        return "Class " + qname;
    }

    /**
     * Get the name of this class without package.
     */
    public String getShortName() {
        int i = qname.lastIndexOf('.');
        if (i >= 0) {
            return qname.substring(i + 1);
        } else {
            return qname;
        }
    }

    public void dump() {
        dump(System.out, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        out.println(is + "qname = " + qname);
        out.println(is + "cls = " + cls);
        out.println(is + "classId = " + classId);
        out.println(is + "index = " + index);
        out.println(is + "objectIdClass = " + objectIdClass);
        out.println(is + "pcSuperClass = " + pcSuperClass);
        out.println(is + "pcSuperMetaData = " + pcSuperMetaData);
        out.println(is + "identityType = " +
                MDStaticUtils.toIdentityTypeString(identityType));
        out.println(is + "readOnly = " + readOnly);
        out.println(
                is + "cache = " + MDStaticUtils.toCacheString(cacheStrategy));
        out.println(is + "jdoClass = " + jdoClass);
        out.println(is + "jdbcClass = " + storeClass);
        out.println(is + "realFieldCount = " + realFieldCount);
        out.println(is + "superFieldCount = " + superFieldCount);
        out.println(is + "superFetchGroupCount = " + superFetchGroupCount);
        out.println(is + "hasAutoSetFields = " + hasAutoSetFields);
        StringBuffer s = new StringBuffer();
        s.append(is);
        s.append("directRefStateFieldNos = ");
        if (directRefStateFieldNos == null) {
            s.append("null");
        } else {
            s.append("[");
            for (int i = 0; i < directRefStateFieldNos.length; i++) {
                if (i > 0) s.append(", ");
                s.append(directRefStateFieldNos[i]);
            }
            s.append(']');
        }
        out.println(s);
        if (pcHierarchy != null) {
            for (int i = 0; i < pcHierarchy.length; i++) {
                out.println(is + "pcHierarchy[" + i + "] " + pcHierarchy[i]);
            }
        } else {
            out.println(is + "pcHierarchy is null");
        }
        if (pcSubclasses != null) {
            out.println(is + pcSubclasses.length + " persistent subclass(es)");
            for (int i = 0; i < pcSubclasses.length; i++) {
                out.println(is + "[" + i + "] " + pcSubclasses[i]);
            }
        }
        if (fields != null) {
            out.println(is + fields.length + " persistent field(s)");
            for (int i = 0; i < fields.length; i++) {
                fields[i].dump(out, is);
            }
        }
        if (stateFields != null) {
            for (int i = 0; i < stateFields.length; i++) {
                out.println(is + "stateFields[" + i + "] = " + stateFields[i]
                        + " stateFieldNo = " + stateFields[i].stateFieldNo);
            }
        } else {
            out.println(is + "stateFields is null");
        }
        if (stateFieldNos != null) {
            for (int i = 0; i < stateFieldNos.length; i++) {
                out.println(is + "stateFieldNos[" + i + "] = " +
                        stateFieldNos[i]);
            }
        } else {
            out.println(is + "stateFieldNos is null");
        }
        if (scoFieldNos != null) {
            for (int i = 0; i < scoFieldNos.length; i++) {
                out.println(is + "scoFieldNos[" + i + "] = " +
                        scoFieldNos[i]);
            }
        } else {
            out.println(is + "scoFieldNos is null");
        }
        if (pkFieldNos != null) {
            for (int i = 0; i < pkFieldNos.length; i++) {
                out.println(is + "pkFieldNos[" + i + "] = " +
                        pkFieldNos[i]);
            }
        } else {
            out.println(is + " pkFieldNos is null");
        }
        if (pass2Fields != null) {
            for (int i = 0; i < pass2Fields.length; i++) {
                out.println(is + " pass2Fields[" + i + "] = " +
                        pass2Fields[i]);
            }
        } else {
            out.println(is + " pass2Fields is null");
        }
        if (fetchGroups != null) {
            out.println(is + fetchGroups.length + " fetch group(s)");
            for (int i = 0; i < fetchGroups.length; i++) {
                out.println(is + "fetchg[" + i + "] = ");
                fetchGroups[i].dump(out, is);
            }
        }
            }

    /**
     * Is this class part of a hierarchy (i.e. Does this class have super classes
     * or sub classes that are PesistentCapable)
     *
     * @return true if this class has super classes or sub classes that
     *         are PesistentCapable
     */
    public boolean isInHierarchy() {
        return !(pcHierarchy.length == 1 && pcSubclasses == null);
    }

    /**
     * Is this class the least derived concrete class in its hierarchy? This
     * returns true for base classes and subclasses of a horizontally mapped
     * base class.
     */
    public boolean isBaseClass() {
        return top == this;
    }

    /**
     * Get the classloader that loaded our class.
     */
    public ClassLoader getClassLoader() {
        ClassLoader l = cls.getClassLoader();
        if (l == null) {
            return ClassHelper.get().getSystemClassLoader();
        } else {
            return l;
        }
    }

    public void initMDFields2() {


        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData managedField = fields[i];
            if (managedField.isEmbeddedRef() && managedField.embeddedFmds != null) {
                FieldMetaData[] embManFields
                        = managedField.managedEmbeddedFields
                        = new FieldMetaData[managedField.typeMetaData.managedFields.length];
                for (int j = 0; j < managedField.embeddedFmds.length; j++) {
                    FieldMetaData embeddedFmd = managedField.embeddedFmds[j];
                    embManFields[embeddedFmd.origFmd.managedFieldNo] = embeddedFmd;
                }
            }
        }
        if (pcSubclasses != null) {
            for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                pcSubclasses[i].initMDFields2();
            }
        }

    }

    public boolean isEmbeddedRef(int stateFieldNo) {
        return stateFields[stateFieldNo].embedded
            && stateFields[stateFieldNo].category == MDStatics.CATEGORY_REF
            && stateFields[stateFieldNo].typeMetaData.structType == false;
    }
    public boolean isEmbeddedStruct(int stateFieldNo) {
        return stateFields[stateFieldNo].embedded
            && stateFields[stateFieldNo].category == MDStatics.CATEGORY_REF
            && stateFields[stateFieldNo].typeMetaData.structType;
    }

    public void setObjectIdClasssRequired(boolean objectIdClasssRequired) {
        this.objectIdClasssRequired = objectIdClasssRequired;
    }

    public boolean isObjectIdClasssRequired() {
        if (identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return false;
        if (horizontal) return false;
        if (!objectIdClasssRequired) return false;
        return true;
    }

    /**
     * obfuscator gives problems if its private
     */
    public static final class AllFetchGroupComp implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((FetchGroup)o1).name.compareTo(((FetchGroup)o2).name);
        }

        public int compare(FetchGroup o1, FetchGroup o2) {
            return o1.name.compareTo(o2.name);
        }
    }

    /**
     * Set our referenceGraphIndex.
     */
    public void setReferenceGraphIndex(int referenceGraphIndex) {
        this.referenceGraphIndex = referenceGraphIndex;
    }

    /**
     * Set our referenceGraphCycle and recursively all of our subclasses.
     */
    public void setReferenceGraphCycle(boolean referenceGraphCycle) {
        this.referenceGraphCycle = referenceGraphCycle;
        if (pcSubclasses != null) {
            for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                pcSubclasses[i].setReferenceGraphCycle(referenceGraphCycle);
            }
        }
    }

    /**
     * Find a primary key field of this class or the topmost superclass in
     * its hierarchy by name or null if none.
     */
    public FieldMetaData findPkField(String fname) {
        if (pkFields == null) {
            if (pcSuperMetaData == null) {
                throw BindingSupportImpl.getInstance().internal(
                        "Not an application identity class: " + qname);
            }
            return pcSuperMetaData.findPkField(fname);
        }
        for (int i = pkFields.length - 1; i >= 0; i--) {
            FieldMetaData f = pkFields[i];
            if (f.name.equals(fname)) return f;
        }
        return null;
    }

    /**
     * Find all the fields declared in the class.
     */
    public FieldInfo[] getDeclaredFields() {
        Field[] fa = cls.getDeclaredFields();
        int n = fa.length;

        FieldInfo[] a = new FieldInfo[n];
        for (int i = 0; i < n; i++) {
            Field f = fa[i];

            a[i] = new FieldInfo(f.getName(), f.getModifiers(), f.getType());


        }
        return a;
    }

    /**
     * Is this class PersistenceCapable (i.e. has it been enhanced?).
     */
    public boolean isPersistenceCapable() {
        Class persistenceCapableClass = /*CHFC*/PersistenceCapable.class/*RIGHTPAR*/;
        return persistenceCapableClass.isAssignableFrom(cls);
    }

    /**
     * Info about a field collected with reflection.
     */
    public static class FieldInfo {

        private String name;
        private int modifiers;
        private Class type;

        public FieldInfo(String name, int modifiers, Class type) {
            this.name = name;
            this.modifiers = modifiers;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getModifiers() {
            return modifiers;
        }

        public Class getType() {
            return type;
        }
    }

    /**
     * Add the indexes of all the classes in the hierarchy rooted at this class
     * including this class to a.
     */
    public void findHierarchyIndexes(IntArray a) {
        a.add(index);
        if (pcSubclasses == null) return;
        for (int i = pcSubclasses.length - 1; i >= 0; i--) {
            pcSubclasses[i].findHierarchyIndexes(a);
        }
    }

    /**
     * Get an instance of our class for meta data analysis. If our class is
     * abstract then we may return an instance of one of our subclasses. If
     * we have no concrete subclasses then null is returned. The instance
     * is cached.
     */
    public Object getMetaDataInstance() {
        if (metaDataInstance == null) {
            if (cls == null) {
                throw BindingSupportImpl.getInstance().internal(
                        "cls is null: " + qname);
            }
            try {
                metaDataInstance = cls.newInstance();
            } catch (InstantiationException e) {
                if (pcSubclasses == null) return null;
                for (int i = 0; i < pcSubclasses.length; i++) {
                    metaDataInstance = pcSubclasses[i].getMetaDataInstance();
                    if (metaDataInstance != null) break;
                }
            } catch (IllegalAccessException e) {
                BindingSupportImpl.getInstance().invalidOperation("Unable to create instance of " + qname +
                        ": " + e, e);
            }
        }
        return metaDataInstance;
    }

    /**
     * Check the consistency of the meta data. This will try and validate parts
     * of the data structure against other parts to find bugs.
     */
    public void validate() {

        // make sure that the index of each field in the fields's array
        // matches the fieldNo field on FieldMetaData
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                FieldMetaData fmd = fields[i];
                if (i != fmd.fieldNo) {
                    throw createValidationError(fmd.name + ": i != fields[i].fieldNo: " +
                            i + " != " + fields[i].fieldNo);
                }
            }
        }

        // make sure that the index of each field in the stateField's array
        // matches the stateFieldNo field on FieldMetaData
        if (stateFields != null) {
            for (int i = 0; i < stateFields.length; i++) {
                FieldMetaData fmd = stateFields[i];
                if (i != fmd.stateFieldNo) {
                    throw createValidationError(fmd.name + ": i != stateFields[i].stateFieldNo: " +
                            i + " != " + stateFields[i].stateFieldNo);
                }
                if (i != stateFieldNos[i]) {
                    throw createValidationError(fmd.name + ": i != stateFieldNos[i]: " +
                            i + " != " + stateFieldNos[i]);
                }
            }
        }
    }

    private RuntimeException createValidationError(String msg) {
        return BindingSupportImpl.getInstance().internal("Validation failed: " + qname +
                ": " + msg);
    }

    /**
     * Cleanup any data structures not needed after meta data generation.
     */
    public void cleanupAfterMetaDataGeneration() {
    }

    public void setClassId(int classId) {
        this.classId = classId;
        classIdString = Integer.toString(classId);
    }

    public void addError(RuntimeException e, boolean quiet) {
        if (Debug.DEBUG) {
            e.printStackTrace(System.out);
        }
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

    public long getFirstErrorTime() {
        return errorTime;
    }

    public RuntimeException getFirstError() {
        RuntimeException e = null;
        long fieldErrorTime = Long.MAX_VALUE;
        if (fields != null) {
            int length = fields.length;
            for (int i = 0; i < length; i++) {
                FieldMetaData field = fields[i];
                if (field.hasErrors()) {
                    e = field.getFirstError();
                    fieldErrorTime = field.getFirstErrorTime();
                    break;
                }
            }
        }
        if (error == null) {
            return e;
        } else {
            if (fieldErrorTime > errorTime) {
                return error;
            } else if (fieldErrorTime < errorTime) {
                return e;
            } else {
                return error;
            }
        }
    }



    public boolean hasErrors()
	{
        if (fields != null) {
            int length = fields.length;
            for (int i = 0; i < length; i++) {
                FieldMetaData field = fields[i];
                if (field.hasErrors()) {
                    return true;
                }
            }
        }
        return error != null;
    }

    /**
     * Add a NamedQuery to this class.
     *
     * @throws IllegalArgumentException if there is already one with the
     *                                  same name
     */
    public void addNamedQuery(String queryName, QueryDetails qp) {
        if (namedQueryMap == null) namedQueryMap = new HashMap(17);
        if (namedQueryMap.containsKey(queryName)) {
            throw BindingSupportImpl.getInstance().illegalArgument("Meta data for Class " + qname +
                    " already has a query called '" + queryName + "'");
        }
        namedQueryMap.put(queryName, qp);
    }

    /**
     * Return QueryDetails for the named query or null if none. The caller
     * must not modify the returned instance.
     */
    public QueryDetails getNamedQuery(String queryName) {
        return namedQueryMap == null ? null : (QueryDetails)namedQueryMap.get(
                queryName);
    }

    /**
     * Get Map.Entry's for all of our named queries in alpha order or empty list
     * if none. The key of each Entry is the name and the value the QueryDetails
     * instance.
     */
    public List getNamedQueries() {
        if (namedQueryMap == null) return Collections.EMPTY_LIST;
        ArrayList ans = new ArrayList(namedQueryMap.entrySet());
        Collections.sort(ans, new Comparator() {
            public int compare(Object o1, Object o2) {
                String a = (String)((Map.Entry)o1).getKey();
                String b = (String)((Map.Entry)o2).getKey();
                return a.compareTo(b);
            }
        });
        return ans;
    }

    public transient int weight = 0;

    public List getHeirarchyList() {
        if (heirarchyList == null) {
            ArrayList subsList = new ArrayList();
            subsList.add(this);
            int startIndex = 0;
            int endIndex = 1;
            for (; startIndex < endIndex;) {
                for (int j = startIndex; j < endIndex; j++) {
                    ClassMetaData[] subs = ((ClassMetaData)subsList.get(j)).pcSubclasses;
                    if (subs == null) continue;
                    for (int i = 0; i < subs.length; i++) {
                        subsList.add(subs[i]);
                    }
                }
                startIndex = endIndex;
                endIndex = subsList.size();
            }
            heirarchyList = subsList;
            return subsList;
        }
        return heirarchyList;
    }


}
