
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

import com.versant.core.common.Debug;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.parser.JdoExtension;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.versant.core.common.BindingSupportImpl;

/**
 * A group of fields from a class that are retrieved together. This holds
 * the field and assorted store specific options.
 */
public final class FetchGroup implements Serializable, Comparable {

    public static final FetchGroupField[] EMPTY_FETCHGROUP_FIELDS = new FetchGroupField[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * This is the name reserved for the default fetch group.
     */
    public static final String DFG_NAME = "default";
    /**
     * This is the name reserved for the default fetch group without fake fields.
     */
    public static final String DFG_NAME_NO_FAKES = "defaultNoFakes";
    /**
     * This is the fetchGroup to load all data.
     */
    public static final String RETRIEVE_NAME = "jdoGenieRetrieveFG";
    /**
     * This is the fetchGroup to load all columns in the class table.
     */
    public static final String ALL_COLS_NAME = "_jdoall";
    /**
     * This is the name reserved for the fetch group containing all the
     * reference and collection fields for reachability searching.
     */
    public static final String REF_NAME = "_jdoref";
    /**
     * This is the name reserved for the fetch group containing all the
     * reference fields used to complete one-to-many relationships.
     */
    public static final String DETAIL_NAME = "_jdodetail";
    /**
     * This is the name reserved for the fetch group containing all the
     * direct references and dependent fields. All instances in the delete
     * graph must contain at least this group. This will always be
     * a superset of the DEP_NAME group.
     *
     * @see #DEP_NAME
     */
    public static final String DEL_NAME = "_jdodel";
    /**
     * This is the name reserved for the fetch group containing all the
     * depedent reference and collection fields for delete reachability
     * searching.
     */
    public static final String DEP_NAME = "_jdodep";
    /**
     * This is the name reserved for the fetch group containing all the
     * fields that must be filled in the original state (e.g. jdoVersion etc.)
     * when persisting changes to instances.
     */
    public static final String REQ_NAME = "_jdoreq";
    /**
     * This is the name reserved for the fetch group containing all the
     * many-to-many fields that must be cleared when deleting an instance.
     *
     * @see ClassMetaData#managedManyToManyFetchGroup
     */
    public static final String MANY_TO_MANY_NAME = "_manytomany";
    
    /**
     *  like the retrieve fetchgroup, but references objects are hollow
     *  (with the retrieve fg, the dfg is used for referenced objects)
     */
    public static final String RETRIEVE_REFERENCES_HOLLOW_NAME = "_retrieveReferencesHollow";
    /**
     *  used as nextFetchGroup for the retrieveReferenceHollow fetch group
     */    
    public static final String HOLLOW_NAME = "_hollow";

    /**
     * The name of this group.
     */
    public String name;
    /**
     * The class this group belongs to.
     */
    public ClassMetaData classMetaData;
    /**
     * Our index in our classes fetchGroups array. This is -1 for dynamically
     * created fetch groups as they are not in the fetchGroups array.
     *
     * @see ClassMetaData#fetchGroups
     */
    public int index = -1;
    /**
     * The parsed meta data for this group (null if none i.e. automatically
     * generated fetch group e.g. the default fetch group).
     */
    public JdoExtension extension;
    /**
     * The fields in this group in fieldNo order.
     */
    public FetchGroupField[] fields;
    /**
     * The state field no's of the fetchGroup.
     */
    public int[] stateFieldNos;
    /**
     * The corresponding fetch group from our superclass or null if none.
     */
    public FetchGroup superFetchGroup;
    /**
     * The sub fetch groups from our subclasses or null if none.
     */
    public FetchGroup[] subFetchGroups;
    /**
     * Send any available State data for the instance being fetched along
     * with the fetch call. This is used for fields that have some data
     * stored with the instance itself and some data stored elsewhere e.g.
     * collections on VDS.
     */
    public boolean sendFieldsOnFetch;
    /**
     * Does this fetch group or any other fetch group in the hierarchy
     * contain any fields with primaryField true?
     */
    public boolean hasPrimaryFields;
    /**
     * Extra store specific info attached to this fetch group.
     */
    public transient StoreFetchGroup storeFetchGroup;

    private boolean canUseParallelFetch;
    private boolean canUseParallelFetchDone;

    /**
     * The total number of main table columns in the fetch group.
     */
    public int jdbcTotalCols;

    /**
     * This is a fgf for a jdbc collection field that must be cross joined.
     * Maps is not supported.
     */
    public FetchGroupField crossJoinedCollectionField;

    public FetchGroup(ClassMetaData classMetaData, String name,
            StoreFetchGroup sfg) {
        this.classMetaData = classMetaData;
        this.name = name;
        this.storeFetchGroup = sfg;
        if (sfg != null) {
            sfg.setFetchGroup(this);
        }
    }

    /**
     * Sort by name except for the default fetch group which is always first.
     * Do not change this ordering.
     */
    public int compareTo(Object o) {
        if (name == DFG_NAME) return -1;
        return name.compareTo(((FetchGroup)o).name);
    }

    /**
     * Add a field to this group. This is used to add fake fields created by
     * stores to hold extra information (e.g. row version column values for
     * the JDBC store).
     */
    public void add(FieldMetaData fmd) {
        int n = fields.length;
        FetchGroupField[] a = new FetchGroupField[n + 1];
        System.arraycopy(fields, 0, a, 0, n);
        a[n] = new FetchGroupField(fmd);
        fields = a;
        if (storeFetchGroup != null) {
            storeFetchGroup.fieldAdded(fmd);
        }
    }

    /**
     * Is the field part of this group?
     */
    public boolean contains(FieldMetaData fmd) {
        for (int i = fields.length - 1; i >= 0; i--) {
            FetchGroupField f = fields[i];
            if (f.fmd == fmd) return true;
        }
        return false;
    }

    public String toString() {
        return "FetchGroup@" + System.identityHashCode(this) + ": " + name;
    }

    /**
     * Finish initialization of this fetch group.
     */
    public void finish() {
        if (fields != null) {
            // init the stateFieldNos array
            int nf = fields.length;
            stateFieldNos = new int[nf];
            for (int i = nf - 1; i >= 0; i--) {
                stateFieldNos[i] = fields[i].fmd.stateFieldNo;
            }
        } else {
            fields = EMPTY_FETCHGROUP_FIELDS;
            stateFieldNos = EMPTY_INT_ARRAY;
        }

        // find the super fetch group (if any)
        if (name != null) {
            ClassMetaData pcmd = classMetaData.pcSuperMetaData;
            if (pcmd != null) {
                superFetchGroup = pcmd.getFetchGroup(name);
            }
        }

        if (storeFetchGroup != null) {
            storeFetchGroup.finish();
        }
    }

    /**
     * Get the state fetch group index of this group.
     *
     * @see State
     */
    public int getStateIndex() {
        return classMetaData.superFetchGroupCount + index;
    }

    public boolean isRefFG() {
        return name.equals("_jdoref");
    }

    /**
     * Make sure this fetchGroup is for the available class of OID or one
     * of its superclasses. Returns the most derived usable group i.e. if
     * this method is called with a group for class Base and the available
     * class from the OID is a subclass of base then the corresponding
     * sub group will be returned.
     */
    public FetchGroup resolve(OID oid, ModelMetaData jmd) {
        // make sure the fetch group is for the available meta data of oid
        ClassMetaData acmd = oid.getAvailableClassMetaData();
        ClassMetaData gcmd = classMetaData;
        if (gcmd == acmd) return this;
        for (ClassMetaData cmd = acmd; cmd != gcmd;) {
            cmd = cmd.pcSuperMetaData;
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().internal("Fetch group " + this + " (" + classMetaData +
                        ") does not match OID " + oid + " (" +
                        acmd + ")");
            }
        }
        return acmd.getFetchGroup(name);
    }

    /**
     * Returns the most derived usable group i.e. if this method is called
     * with a group for class Base and availableCmd is a subclass of base
     * then the corresponding sub group will be returned.
     */
    public FetchGroup resolve(ClassMetaData availableCmd) {
        if (availableCmd == classMetaData) return this;
        return availableCmd.getFetchGroup(name);
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + "FetchGroup " + this);
        String is = indent + "  ";
        out.println(is + "classMetaData = " + classMetaData);
        out.println(is + "index = " + index);
        out.println(is + "getStateIndex() = " + getStateIndex());
        out.println(is + "superFetchGroup = " + superFetchGroup);
        if (subFetchGroups != null) {
            for (int i = 0; i < subFetchGroups.length; i++) {
                FetchGroup sg = subFetchGroups[i];
                out.println(is + "subFetchGroups[" + i + "] = " +
                        sg.classMetaData.qname + " " + sg);
            }
        } else {
            out.println(is + "subFetchGroups is null");
        }
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                out.println(is + "fields[" + i + "] " + fields[i]);
            }
        }
        if (stateFieldNos != null) {
            for (int i = 0; i < stateFieldNos.length; i++) {
                out.println(
                        is + "stateField[" + i + "] no = " + stateFieldNos[i]);
            }
        }
    }

    /**
     * Can this fetch group make use of parallel fetching of collections
     * and maps? This will recursively check fetch groups we reference
     * and so on.
     */
    public boolean canUseParallelFetch() {
        if (canUseParallelFetchDone) return canUseParallelFetch;
        return canUseParallelFetchImp(new HashSet());
    }

    private boolean canUseParallelFetchImp(Set fgs) {
        if (fgs.contains(this)) return canUseParallelFetch;

        fgs.add(this);
        for (int i = fields.length - 1; i >= 0; i--) {
            FetchGroupField fgf = fields[i];
            int cat = fgf.fmd.category;
            if (cat == MDStatics.CATEGORY_COLLECTION
                    || cat == MDStatics.CATEGORY_MAP) {
                canUseParallelFetch = true;
                break;
            } else if (cat == MDStatics.CATEGORY_REF
                    && fgf.nextFetchGroup.canUseParallelFetchImp(fgs)) {
                canUseParallelFetch = true;
                break;
            }
        }
        //give the superclass fg a change to calculate
        if (superFetchGroup != null) {
            superFetchGroup.canUseParallelFetchImp(fgs);
            //if we are false then take superfetch groups property
            if (!canUseParallelFetch) canUseParallelFetch = superFetchGroup.canUseParallelFetch;
        }

        canUseParallelFetchDone = true;
        return canUseParallelFetch;
    }

    /**
     * Does this fetch group or any of its sub fetch groups contain any
     * fields with secondaryField true?
     */
    public boolean hasSecondaryFields() {
        if (fields != null) {
            for (int i = fields.length - 1; i >= 0; i--) {
                if (fields[i].fmd.secondaryField) return true;
            }
        }
        if (subFetchGroups != null) {
            for (int i = subFetchGroups.length - 1; i >= 0; i--) {
                if (subFetchGroups[i].hasSecondaryFields()) return true;
            }
        }
        return false;
    }

    /**
     * Does this fetch group or any of its sub fetch groups contain any
     * fields with primaryField true? This will search the hierarchy i.e.
     * it does not check the hasPrimaryFields flag. If nonFake is true then
     * only fields with fake == true are not considered.
     */
    public boolean hasPrimaryFields(boolean nonFake) {
        if (fields == null) return false;
        for (int i = fields.length - 1; i >= 0; i--) {
            final FieldMetaData fmd = fields[i].fmd;
            if (fmd.primaryField && (!nonFake || !fmd.fake)) {
                return true;
            }
        }
        if (subFetchGroups != null) {
            for (int i = subFetchGroups.length - 1; i >= 0; i--) {
                if (subFetchGroups[i].hasPrimaryFields(false)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the sendFieldsOnFetch flag for us and all of our sub
     * fetch groups recursively.
     */
    public void setSendFieldsOnFetch(boolean on) {
        this.sendFieldsOnFetch = on;
        if (subFetchGroups != null) {
            for (int i = subFetchGroups.length - 1; i >= 0; i--) {
                subFetchGroups[i].setSendFieldsOnFetch(on);
            }
        }
    }

    /**
     * Set the hasPrimaryFields flag on us and all of our sub fetch groups
     * recursively.
     */
    public void setHasPrimaryFields(boolean on) {
        this.hasPrimaryFields = on;
        if (subFetchGroups != null) {
            for (int i = subFetchGroups.length - 1; i >= 0; i--) {
                subFetchGroups[i].setHasPrimaryFields(on);
            }
        }
    }
}

