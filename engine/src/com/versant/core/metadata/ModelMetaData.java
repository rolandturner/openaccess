
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

import com.versant.core.common.*;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.util.IntArray;

import javax.jdo.spi.JDOImplHelper;
import javax.jdo.identity.SingleFieldIdentity;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.VersantOid;

/**
 * This holds the meta data for all persistent classes.
 */
public final class ModelMetaData implements Serializable {

    /**
     * This is only used for refFields. If the ref Field is not found then return a null
     * instead of a VersantObjectNotFoundException. This is a projectLevel setting
     */
    public boolean returnNullForRowNotFound;
    /**
     * The persistent classes.
     */
    private static final ClassMetaData[] EMPTY_META_CLASS_ARRAY = new ClassMetaData[0];
    public ClassMetaData[] classes = EMPTY_META_CLASS_ARRAY;

    public HashMap classMap = new HashMap();   //  Class -> ClassMetaData
    /**
     * This maps objectid-class'es to their ClassMetaData. It is used to
     * find the class for an application identity class.
     */
    private final HashMap objectIdClassMap = new HashMap();
    /**
     * The max size of the fields array for any class in this meta data.
     */
    public int maxFieldsLength;
    /**
     * Extra JDBC specific meta data (null if no JdbcDataStore is in use).
     */
    public transient Object jdbcMetaData;
    /**
     * Extra VDS specific meta data (null if no VdsDataStore is in use).
     */
    public transient Object vdsModel;
    /**
     * When an instance is deleted should its state (if available) be included
     * in the  DeletePacket? If this is false then only the OID is included.
     */
    public boolean sendStateOnDelete;
    /**
     * Maps class name to resource name for the enhancer.
     */
    public HashMap classResourceMap = new HashMap();
    /**
     * List of all lifecycle event that we need to listen for
     */
    private transient List lifecycleEvents;

    private transient RuntimeException error;

    /**
     * This is true if we are running unit tests. Some checks are relaxed
     * so the same model can be used for JDBC and VDS (e.g. if this is true
     * then collections with no element-type set are allowed).
     */
    public transient boolean testing;


    private static final ThreadLocal META_DATA = new ThreadLocal();

    private final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private Map abstractSchemaNameMap;  // abstract schema name -> ClassMetaData

    private StateAndOIDFactory untypedOIDFactory = new StateAndOIDFactory() {
        public OID createOID(ClassMetaData cmd, boolean resolved) {
            throw BindingSupportImpl.getInstance().internal("");
        }
        public State createState(ClassMetaData cmd) {
            throw BindingSupportImpl.getInstance().internal("");
        }
        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            throw BindingSupportImpl.getInstance().internal("");
        }
        public OID createUntypedOID() {
            throw BindingSupportImpl.getInstance().unsupported(
                    "Untyped OIDs are not supported by the datastore");
        }
    };

    public ModelMetaData() {
    }

    /**
     * Get the JDOMetaData instance associated with the current thread. This
     * is used during deserialization.
     */
    public static ModelMetaData getThreadMetaData() {

        return (ModelMetaData)META_DATA.get();


    }

    /**
     * Associate meta data with the current thread. This is used during
     * deserialization.
     */

    public static void setThreadMetaData(ModelMetaData jmd) {
        META_DATA.set(jmd);
    }


    /**
     * Get meta data for the class by classId or null if not found.
     */
    public ClassMetaData getClassMetaData(int classId) {
        // do a binary search since classes is sorted by classId
        int low = 0;
        int high = classes.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            ClassMetaData midVal = classes[mid];
            int midValClassId = midVal.classId;
            if (midValClassId < classId) {
                low = mid + 1;
            } else if (midValClassId > classId) {
                high = mid - 1;
            } else {
                return midVal;
            }
        }
        return null;
    }

    /**
     * Get the meta data for the class or null if not found.
     */
    public ClassMetaData getClassMetaData(Class cls) {
        if (Debug.DEBUG) {
            ClassMetaData cmd = (ClassMetaData) classMap.get(cls);
            if (cmd != null && cmd.cls != cls) {
                throw BindingSupportImpl.getInstance().internal("ClassMap broken");
            }
        }
        return (ClassMetaData) classMap.get(cls);
    }

    /**
     * Get the meta data for the class or null if not found.
     *
     * @param qname Fully qualified class name
     */
    public ClassMetaData getClassMetaData(String qname) {
        for (int i = classes.length - 1; i >= 0; i--) {
            ClassMetaData cmd = classes[i];
            if (cmd.qname.equals(qname)) return cmd;
        }
        return null;
    }

    /**
     * Get the meta data for the class relative to package of base or null if
     * not found.
     */
    public ClassMetaData getClassMetaData(ClassMetaData base, String className) {
        ClassMetaData ans = getClassMetaData(className);
        if (ans == null) {
            ans = getClassMetaData(base.packageNameWithDot + className);
        }
        return ans;
    }

    /**
     * Build the abstract schema name -> ClassMetaData map. This throws
     * a JDOUserException if there are any duplicates.
     */
    public void buildAbstractSchemaNameMap() {
        abstractSchemaNameMap = new HashMap();
        for (int i = classes.length - 1; i >= 0; i--) {
            ClassMetaData cmd = classes[i];
            ClassMetaData dup = (ClassMetaData)abstractSchemaNameMap.put(
                    cmd.abstractSchemaName, cmd);
            if (dup != null) {
                // todo HACK ignore for now
//                throw BindingSupportImpl.getInstance().invalidOperation(
//                        cmd.qname + " and " + dup.qname +
//                        " have same abstract schema name '" +
//                                cmd.abstractSchemaName + "'");
            }
        }
    }

    /**
     * Lookup a class by its abstract schema name or null if not found.
     */
    public ClassMetaData getClassMetaByASN(String abstractSchemaName) {
        return (ClassMetaData)abstractSchemaNameMap.get(abstractSchemaName);
    }




    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        for (int i = 0; i < classes.length; i++) {
            classes[i].dump(out, is);
        }
    }

    /**
     * Return a list of all classes that are not PersistenceCapable i.e.
     * that have not been enhanced.
     */
    public List findNonPCClassNames() {
        ArrayList a = new ArrayList();
        int n = classes.length;
        for (int i = 0; i < n; i++) {
            if (!classes[i].isPersistenceCapable()) a.add(classes[i].qname);
        }
        return a;
    }

    /**
     * Check for classes that are not PersistenceCapable i.e. that have not
     * been enhanced and throw a JDOFatalUserException if there are any.
     */
    public void checkForNonPCClasses() {
        List nonPCList = findNonPCClassNames();
        if (!nonPCList.isEmpty()) {
            StringBuffer s = new StringBuffer();
            s.append("One or more classes in the JDO meta data have not " +
                    "been enhanced:");
            int n = nonPCList.size();
            int i;
            for (i = 0; i < n && i < 10; i++) {
                s.append('\n');
                s.append(nonPCList.get(i));
            }
            if (i < n) s.append("\n...");
            throw BindingSupportImpl.getInstance().runtime(s.toString());
        }
    }

    /**
     * Build objectIdClassMap mapping objectid-class'es to the corresponding
     * ClassMetaData.
     *
     * @see #getClassMetaDataForObjectIdClass
     */
    public void buildObjectIdClassMap() {
        int n = classes.length;
        for (int i = 0; i < n; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null && cmd.objectIdClass != null) {
                objectIdClassMap.put(cmd.objectIdClass, cmd);
            }
        }
    }

    /**
     * Get the ClassMetaData for an objectid-class or null if none.
     *
     * @see #buildObjectIdClassMap
     */
    public ClassMetaData getClassMetaDataForObjectIdClass(Class cls) {
        return (ClassMetaData)objectIdClassMap.get(cls);
    }

    /**
     * Make sure all persistent classes are registered for JDO by creating
     * an instance of each one. Errors are silently ignored.
     *
     * TODO: initialise the static fields explicetly via reflection from top to bottom.
     * This will guard against a scenarion where a class has a reference to the non-root
     * instance of a uninitilised hierarchy which causes the referenced hierarchy to
     * be wrongly initialised.
     */
    public void forceClassRegistration() {
        ClassMetaData[] cmds = classes;
        for (int i = 0; i < cmds.length; i++) {
            ClassMetaData cmd = cmds[i];
            if (cmd.pcSuperClass == null) {
                initClass(cmd);
            }
        }
    }

    /**
     * @param cmd
     */
    private void initClass(ClassMetaData cmd) {
        if (cmd.pcSuperMetaData != null) {
            throw BindingSupportImpl.getInstance().internal(
                    "This method should only be called for the base class");
        }
        initClass(cmd.cls);
        if (cmd.pcSubclasses == null) return;

        List l = new ArrayList();
        l.addAll(Arrays.asList(cmd.pcSubclasses));
        for (int i = 0; i < l.size(); i++) {
            ClassMetaData aCmd = (ClassMetaData)l.get(i);
            initClass(aCmd.cls);
            if (aCmd.pcSubclasses != null) l.addAll(Arrays.asList(aCmd.pcSubclasses));
        }
    }

    /**
     * Create a instance of the given class
     */
    private void initClass(Class cls) {
        try {
            ClassHelper.get().classForName(cls.getName(), true, cls.getClassLoader());
        } catch (Throwable e) {
            //ignore
        }
    }

    /**
     * Convert an array of classes into an array of class indexes.
     *
     * @throws javax.jdo.JDOUserException if any classes are not persistent
     */
    public int[] convertToClassIndexes(Class[] classes,
            boolean includeSubclasses) {
        if (includeSubclasses) {
            int n = classes.length;
            IntArray a = new IntArray(16);
            for (int i = 0; i < n; i++) {
                ClassMetaData cmd = getClassMetaData(classes[i]);
                if (cmd == null) {
                    throw BindingSupportImpl.getInstance().invalidOperation("Not a persistent class: " +
                            classes[i].getName());
                }
                cmd.findHierarchyIndexes(a);
            }
            return a.toArray();
        } else {
            int n = classes.length;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                ClassMetaData cmd = getClassMetaData(classes[i]);
                if (cmd == null) {
                    throw BindingSupportImpl.getInstance().invalidOperation("Not a persistent class: " +
                            classes[i].getName());
                }
                a[i] = cmd.index;
            }
            return a;
        }
    }

    /**
     * Convert an array of class indexes into an array of classes.
     *
     * @throws javax.jdo.JDOUserException if any class indexes are invalid
     */
    public Class[] convertFromClassIndexes(int[] classIndexes) {
        int n = classIndexes.length;
        Class[] ans = new Class[n];
        int max = classes.length;
        for (int i = 0; i < n; i++) {
            int ci = classIndexes[i];
            if (ci < 0 || ci >= max) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid class index: " + ci);
            }
            ans[i] = classes[ci].cls;
        }
        return ans;
    }

    /**
     * Check the consistency of the meta data. This will try and validate parts
     * of the data structure against other parts to find bugs.
     */
    public void validate() {
        for (int i = 0; i < classes.length; i++) {
            classes[i].validate();
        }
    }

    /**
     * Cleanup any data structures not needed after meta data generation.
     */
    public void cleanupAfterMetaDataGeneration() {
        for (int i = 0; i < classes.length; i++) {
            classes[i].cleanupAfterMetaDataGeneration();
        }
    }

    /**
     * Create an OID instance from a datastore identity String.
     *
     * @param resolved Mark the OID as resolved (exact class known) or not
     */
    public OID newOIDFromIDString(String value, boolean resolved) {
        try {
            char c = value.charAt(0);
            int cid = c - '0';
            int i = 1;
            for (; ;) {
                c = value.charAt(i++);
                if (c == MDStatics.OID_CHAR_SEPERATOR) break;
                cid = cid * 10 + (c - '0');
            }
            ClassMetaData cmd = getClassMetaData(cid);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid OID String (bad class ID): '" +
                        value + "'");
            }
            if (cmd.identityType != MDStatics.IDENTITY_TYPE_DATASTORE) {
                throw BindingSupportImpl.getInstance().invalidOperation("Class " + cmd.qname + " for class-id " + cid +
                        " does not use datastore identity");
            }
            resolved = (resolved || (cmd.pcSubclasses == null));
            OID oid = cmd.createOID(resolved || cmd.pcSubclasses == null);
            oid.fillFromIDString(value, i);
            return oid;
        } catch (RuntimeException e) {
        	if( BindingSupportImpl.getInstance().isOwnException(e) )
        	{
        		throw e;
        	}
        	else
        	{
            	throw BindingSupportImpl.getInstance().invalidOperation("Invalid OID String: '" +
                    value + "'", e);
            }
        }
    }

    /**
     * Create an OID instance from a datastore identity String.
     *
     * @param resolved Mark the OID as resolved (exact class known) or not
     */
    public OID newOIDFromIDObject(String value, boolean resolved) {
        try {
            char c = value.charAt(0);
            int cid = c - '0';
            int i = 1;
            for (; ;) {
                c = value.charAt(i++);
                if (c == MDStatics.OID_CHAR_SEPERATOR) break;
                cid = cid * 10 + (c - '0');
            }
            ClassMetaData cmd = getClassMetaData(cid);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid OID String (bad class ID): '" +
                        value + "'");
            }
            resolved = (resolved || (cmd.pcSubclasses == null));
            OID oid = cmd.createOID(resolved || cmd.pcSubclasses == null);

            FieldMetaData fmd = cmd.pkFields[0];
            Object realValue = null;
            try {
                switch (fmd.typeCode) {
                    case MDStatics.INTW:
                    case MDStatics.INT:
                        realValue = new Integer(value.substring(i));
                        break;
                    case MDStatics.SHORTW:
                    case MDStatics.SHORT:
                        realValue = new Short(value.substring(i));
                        break;
                    case MDStatics.STRING:
                        realValue = value.substring(i);
                        break;
                    case MDStatics.BOOLEANW:
                    case MDStatics.BOOLEAN:
                        realValue = Boolean.valueOf(value.substring(i));
                        break;
                    case MDStatics.BYTEW:
                    case MDStatics.BYTE:
                        realValue = new Byte(value.substring(i));
                        break;
                    case MDStatics.BIGDECIMAL:
                        realValue = new BigDecimal(value.substring(i));
                        break;
                    case MDStatics.BIGINTEGER:
                        realValue = new BigInteger(value.substring(i));
                        break;
                    case MDStatics.DOUBLEW:
                    case MDStatics.DOUBLE:
                        realValue = new Double(value.substring(i));
                        break;
                    case MDStatics.FLOATW:
                    case MDStatics.FLOAT:
                        realValue = new Float(value.substring(i));
                        break;
                    case MDStatics.LONGW:
                    case MDStatics.LONG:
                        realValue = new Long(value.substring(i));
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal(
                                "Unable to create id from the string '" + value +
                                "' type code " + fmd.typeCode);
                }
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "invalid OID String: '" + value + "'", e);
            }

            oid.fillFromIDObject(realValue);
            return oid;
        } catch (RuntimeException e) {
            if (BindingSupportImpl.getInstance().isOwnException(e)) {
                throw e;
            } else {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Invalid OID String: '" +  value + "'", e);
            }
        }
    }


    /**
     * Convert an internal OID to a String that can be parsed by
     * {@link #newOIDFromExternalString(java.lang.String)}. This works for
     * datastore and application identity classes.
     */
    public String toExternalString(OID oid) {
        StringBuffer b = new StringBuffer();
        ClassMetaData cmd = oid.getAvailableClassMetaData();
        if (cmd.top.objectIdClass == null) {
            if (cmd.top.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                b.append('e');
                b.append(' ');
                b.append(oid);
            } else {
                b.append('d');
                b.append(' ');
                b.append(oid);
            }
        } else {
            b.append('a');
            b.append(' ');
            b.append(cmd.classIdString);
            b.append(' ');
            b.append(oid.createObjectIdClassInstance());
        }
        String ans = b.toString();
        if (Debug.DEBUG) {
            OID oid2 = newOIDFromExternalString(ans);
            if (!oid.equals(oid2)) {
                throw BindingSupportImpl.getInstance().internal(
                        "string does not parse properly for " + oid);
            }
        }
        return ans;
    }

    /**
     * Create an internal OID from a String previously created with
     * {@link #toExternalString(com.versant.core.common.OID)}.
     */
    public OID newOIDFromExternalString(String s) {
        switch (s.charAt(0)) {
            case 'd':
                return newOIDFromIDString(s.substring(2), true);
            case 'a':
                int i = s.indexOf(' ', 2);
                int cid = Integer.parseInt(s.substring(2, i));
                ClassMetaData cmd = getClassMetaData(cid);
                if (cmd == null) {
                    throw BindingSupportImpl.getInstance().invalidOperation("Invalid string: '" + s + "', no class found for " +
                            "class-id: " + cid);
                }
                Object o;
                try {
                    o = JDOImplHelper.getInstance().newObjectIdInstance(
                            cmd.cls, s.substring(i + 1));
                } catch (Exception e) {
                	if( BindingSupportImpl.getInstance().isOwnException(e) )
                	{
                		throw (RuntimeException) e;
                	}
                	else
                	{
                    	throw BindingSupportImpl.getInstance().invalidOperation(
                            "Invalid string: '" + s + "': " + e, e);
                    }
                }
                OID oid = cmd.createOID(true);
                oid.fillFromPK(o);
                return oid;
            case 'e':
                return newOIDFromIDObject(s.substring(2), true);
            default:
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid string: '" + s + "'");
        }
    }

    public void addError(RuntimeException e, boolean quiet) {
        if (error == null) {
            error = e;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
        if (!quiet) throw e;
    }

    public boolean hasErrors() {
        if (error != null) {
            return true;
        }
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].hasErrors()) return true;
        }
        return false;
    }

    public RuntimeException getFirstError() {
        if (error != null) {
            return error;
        }
        for (int i = 0; i < classes.length; i++) {
            RuntimeException e = classes[i].getFirstError();
            if (e != null) return e;
        }
        return null;
    }

    /**
     * Convert an instance of an application identity class into an OID.
     */
    public OID convertFromAppIdToOID(Object appId) {
        OID oid = null;
        ClassMetaData cmd = null;
        if (appId instanceof SingleFieldIdentity) {
            cmd = getClassMetaData(((SingleFieldIdentity) appId).getTargetClass());
        } else {
            cmd = getClassMetaDataForObjectIdClass(/*CHFC*/appId.getClass()/*RIGHTPAR*/);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Instance is not an objectid-class for any persistent classes: " +
                        appId.getClass().getName() + ": " + toString(appId));
            }
        }
        oid = cmd.createOID(false);
        oid.fillFromPK(appId);
        return oid;
    }

    /**
     * Converts an instance of a VersantOid to an internal OID.
     */
    public OID convertJDOGenieOIDtoOID(VersantOid versantOid) {
        if (versantOid.actualOID != null) {
            return versantOid.actualOID.getAvailableOID();
        }
        ClassMetaData cmd = getClassMetaData(versantOid.classId);
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("Invalid classID in VersantOid: "
                    + versantOid);
        }
        OID oid = cmd.createOID(true);
        oid.setLongPrimaryKey(versantOid.pk);
        return versantOid.actualOID = oid;
    }



    /**
     * Converts an instance of a JDOGenieOID, application identity
     * ID instance or internal OID to an internal OID.
     */
    public OID convertToOID(Object oid)
	{
        if (oid instanceof VersantOid) {
            return convertJDOGenieOIDtoOID((VersantOid)oid);
        } else if (oid instanceof OID) {
            return (OID)oid;
        } else {
            return convertFromAppIdToOID(oid);
        }
    }

    /**
     * Converts an array of a JDOGenieOID's, application identity ID
     * instances or OIDs to an array of internal OID.
     */
    public OID[] convertToOID(Object[] oids, int n) {
        OID[] a = new OID[n];
        for (int i = 0; i < n; i++) {
            Object oid = oids[i];
            if (oid instanceof VersantOid) {
                a[i] = convertJDOGenieOIDtoOID((VersantOid)oid);
            } else if (oid instanceof OID) {
                a[i] = (OID)oid;
            } else if (oid != null) {
                a[i] = convertFromAppIdToOID(oid);
            }
        }
        return a;
    }

    /**
     * Safely do toString on o. If there is an exception then return a message
     * indicating that toString() failed including the exception.
     */
    private static String toString(Object o) {
        if (o == null) return "null";
        try {
            return o.toString();
        } catch (Exception e) {
            return o.getClass().getName() + ".toString() failed: " + e;
        }
    }

    /**
     * Get all the ClassMetaData for the hierarchy rooted at base.
     */
    public ClassMetaData[] getClassMetaDataForHierarchy(ClassMetaData base) {
        if (base.pcSubclasses == null) {
            return new ClassMetaData[]{base};
        }
        ArrayList a = new ArrayList();
        getClassMetaDataForHierarchyImp(base, a);
        ClassMetaData[] ans = new ClassMetaData[a.size()];
        a.toArray(ans);
        return ans;
    }

    private void getClassMetaDataForHierarchyImp(ClassMetaData cmd, ArrayList a) {
        a.add(cmd);
        if (cmd.pcSubclasses != null) {
            for (int i = 0; i < cmd.pcSubclasses.length; i++) {
                getClassMetaDataForHierarchyImp(cmd.pcSubclasses[i], a);
            }
        }
    }

    /**
     * Create an unresolved OID for the class for classIndex.
     */
    public OID createUnresolvedOID(int classIndex) {
        return classes[classIndex].createOID(false);
    }

   /**
     * Set the factory used to create untyped OIDs. The default factory throws
     * an unsupported option exception.
     */
    public void setUntypedOIDFactory(StateAndOIDFactory untypedOIDFactory) {
        this.untypedOIDFactory = untypedOIDFactory;
    }

    /**
     * Create a new untyped OID if the store supports this or throw an
     * unsupported option exception if not.
     */
    public OID createUntypedOID() {
        return untypedOIDFactory.createUntypedOID();
    }

    private Map candidatesForClsMap = new HashMap();

    /**
     * Return all the root candidate classes for persistent heirarchies that implement
     * or extend the supplied class.
     */
    public synchronized Class[] getQueryCandidatesFor(Class cls) {
        //if class is an interface and the interface is not a persistent interface
        //then find all the persistent classes that implement it.
        //for each of these classes we must find there root persistent object.
        Class[] clsArray = (Class[]) candidatesForClsMap.get(cls);
        if (clsArray == null) {
            ClassMetaData cmd = getClassMetaData(cls);
            if (cmd != null && !cmd.horizontal) {
                //this class is persistent and is not horizontally mapped so just return intself
                clsArray = new Class[] {cls};
            } else {
                Set result = new HashSet();
                if (cls.isInterface()) {
                    throw BindingSupportImpl.getInstance().unsupported(
                            "Query by interface is not currently supported");
                } else {
                    for (int i = 0; i < classes.length; i++) {
                        ClassMetaData aClass = classes[i];
                        if (cls.equals(aClass.cls.getSuperclass())) {
                            result.add(aClass.cls);
                        }
                    }
                }
                if (result.isEmpty()) {
                    clsArray = EMPTY_CLASS_ARRAY;
                } else {
                    clsArray = new Class[result.size()];
                    result.toArray(clsArray);
                }
            }
            candidatesForClsMap.put(cls, clsArray);
        }
        return clsArray;
    }

    /**
     * Adds event list to lifecycleEvents
     */
    public void addEntityLifecycleEvent(List eventList) {
        if (this.lifecycleEvents == null) {
            this.lifecycleEvents = new ArrayList(eventList);
        } else {
            this.lifecycleEvents.addAll(eventList);
        }
    }

    public List getEntityLifecycleEvent() {
        return this.lifecycleEvents;
    }

}
