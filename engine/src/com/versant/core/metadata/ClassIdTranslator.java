
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

import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This maintains translation tables to translate ClassMetaData to/from
 * String and int IDs.
 */
public final class ClassIdTranslator {

    private final ModelMetaData jmd;
    private String message;

    private final boolean stringClassIds;    // are the class-id's Strings?

    private final Map cmdToIDString; // ClassMetaData -> String ID
    private final Map idToCmdString; // String ID -> ClassMetaData

    private final Map cmdToIDInt;       // ClassMetaData -> int ID
    private final IntObjectHashMap idToCmdInt;       // int ID -> ClassMetaData

    public ClassIdTranslator(ModelMetaData jmd, boolean stringClassIds,
            Map cmdToIDString, Map idToCmdString,
            Map cmdToIDInt, IntObjectHashMap idToCmdInt) {
        this.jmd = jmd;
        this.stringClassIds = stringClassIds;
        this.cmdToIDString = cmdToIDString;
        this.idToCmdString = idToCmdString;
        this.cmdToIDInt = cmdToIDInt;
        this.idToCmdInt = idToCmdInt;
    }

    /**
     * Set the message used to constuct exceptions for invalid classes and
     * so on (e.g. 'field com.acme.model.Company.data').
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Get all of the ClassMetaData's we have in alphabetical order.
     */
    public List getClassList() {
        if (cmdToIDInt != null) {
            return createArrayList(cmdToIDInt.keySet().iterator());
        }
        if (cmdToIDString != null) {
            return createArrayList(cmdToIDString.keySet().iterator());
        }
        return Collections.EMPTY_LIST;
    }

    private static ArrayList createArrayList(Iterator i) {
        ArrayList a = new ArrayList();
        for (; i.hasNext(); ) a.add(i.next());
        Collections.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                ClassMetaData a = (ClassMetaData)o1;
                ClassMetaData b = (ClassMetaData)o2;
                return a.index - b.index;
            }
        });
        return a;
    }

    /**
     * Are the class-id's Strings (i.e. not ints)?
     */
    public boolean isStringClassIds() {
        return stringClassIds;
    }

    /**
     * Convert an int class-id value read from our class-id column into
     * ClassMetaData. Throws a JDODataStoreException if the classId is
     * invalid.
     */
    public ClassMetaData getClassForIntClassId(int classId) {
        ClassMetaData ans;
        if (cmdToIDInt == null) { // use global class-id's
            ans = jmd.getClassMetaData(classId);
            if (ans == null) {
                throw createUnknownClassException(Integer.toString(classId));
            }
        } else {    // use local class-id's
            ans = (ClassMetaData)idToCmdInt.get(classId);
            if (ans == null) {
                ans = jmd.getClassMetaData(classId);
                if (ans != null) {  // not a valid class for this field
                    throw createInvalidClassException(ans, Integer.toString(classId));
                } else {    // dodgy classId
                    throw createUnknownClassException(Integer.toString(classId));
                }
            }
        }
        return ans;
    }

    /**
     * Convert a class into an int class-id for setting on our class-id column.
     * Throws a JDODataStoreException if the class is invalid.
     */
    public int getIntClassIdForClass(ClassMetaData cmd) {
        if (cmdToIDInt == null) {
            return cmd.classId;
        } else {
            int id = ((Integer)cmdToIDInt.get(cmd)).intValue();
            if (id < 0) throw createInvalidClassException(cmd);
            return id;
        }
    }

    /**
     * Convert a String class-id value read from our class-id column into
     * ClassMetaData. Throws a JDODataStoreException if the classId is
     * invalid.
     */
    public ClassMetaData getClassForStringClassId(String classId) {
        ClassMetaData ans = (ClassMetaData)idToCmdString.get(classId);
        if (ans == null) {
            throw createUnknownClassException(classId);
        }
        return ans;
    }

    /**
     * Convert a class into an String class-id for setting on our class-id
     * column. Throws a JDODataStoreException if the class is invalid.
     */
    public String getStringClassIdForClass(ClassMetaData cmd) {
        if (cmdToIDString == null) {
            return cmd.classIdString;
        } else {
            String id = (String)cmdToIDString.get(cmd);
            if (id == null) throw createInvalidClassException(cmd);
            return id;
        }
    }

    private RuntimeException createUnknownClassException(String classId) {
        return BindingSupportImpl.getInstance().datastore(
            "Unknown class-id value (" + classId + ") for " + message);
    }

    private RuntimeException createInvalidClassException(ClassMetaData cmd,
            String classId) {
        return BindingSupportImpl.getInstance().datastore(
            "Instances of class " + cmd.qname + " for ID " + classId +
            " are not allowed for " + message);
    }

    private RuntimeException createInvalidClassException(ClassMetaData cmd) {
        return BindingSupportImpl.getInstance().datastore("Instances of class " + cmd.qname +
            " are not allowed for " + message);
    }

}
