
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
package com.versant.core.common;

import com.versant.core.jdo.sco.VersantSimpleSCO;
import com.versant.core.jdo.*;
import com.versant.core.metadata.*;
import com.versant.core.server.OIDGraph;
import com.versant.core.util.IntArray;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import javax.jdo.spi.PersistenceCapable;
import java.io.*;
import java.util.*;
import java.lang.reflect.Array;

/**
 * This is a State implementation suitable for use with any PC class. It is
 * intended for use during development when the State class generating code is
 * broken. The meta data OID factory method can just return these for all
 * PC classes. Performance is not important for this class as it will not
 * be used in a production release.
 */
public class GenericState extends State {

    protected transient ClassMetaData cmd;

    /**
     * The JDBC tests set this flag to disable the checkFilled calls on
     * getString etc.
     */
    public static boolean RUNNING_JDBC_TESTS = false;

    /**
     * This holds the data for all of the persistent fields and any extra
     * data required for a particular store. The persistent fields are
     * stored first in fieldNo order (i.e. alphabetically) followed by
     * the store defined values (if any).
     */
    protected Object[] data;
    /**
     * Each field or extra store value that has a valid entry in data has a
     * flag set here.
     */
    protected boolean[] filled;

    /**
     * Keeps track of dirty fields. This is the result of all setXXX methods. The
     * setInternalXXX method does not change the status to dirty. This is only
     * used on the client side.
     */
    protected transient boolean[] dirtyFields;
    /**
     * Keeps tracks of which fields have been resolved for client side usage. eg
     * ensure that an oid field is resolved to the actual PersistantCapable instance.
     */
    protected transient boolean[] resolvedForClient;

    /**
     * A global indication if this state has any dirty fields.
     */
    protected boolean dirty;

    /**
     * The index of the persistant class.
     */
    protected int classIndex;

    public GenericState() {
    }

    public GenericState(ClassMetaData cmd) {
        this.cmd = cmd;
        classIndex = cmd.index;
        int n = cmd.superFieldCount + cmd.fields.length;
        data = new Object[n];
        filled = new boolean[n];
    }

	// debugging helper when states are incorrectly used.
    private static String createContextStackTrace() {
        OutputStream os = new ByteArrayOutputStream(1000);
        PrintStream ps = new PrintStream(os);
        Exception e = new Exception();
        e.fillInStackTrace();
        e.printStackTrace(ps);
        return os.toString();
    }

    /**
     * Return a new State instance
     *
     * @return new State instance
     */
    public State newInstance() {
        return new GenericState(cmd);
    }

    /**
     * Return the index of our PC class in the meta data. Do not use this
     * to get the meta data for our class. Call getClassMetaData instead.
     *
     * @see ModelMetaData#classes
     * @see #getClassMetaData
     */
    public int getClassIndex() {
        return classIndex;
    }

    private final int convertAbsToState(int managedFieldNo) {
        return cmd.managedFields[managedFieldNo].stateFieldNo;
    }

    /**
     * Get the meta data for our class.
     *
     * @param jmd The meta data we belong to
     */
    public ClassMetaData getClassMetaData(ModelMetaData jmd) {
        return jmd.classes[classIndex];
    }

    public void setClassMetaData(ClassMetaData cmd) {
        this.cmd = cmd;
        int n = cmd.superFieldCount + cmd.fields.length;
        dirtyFields = new boolean[n];
        resolvedForClient = new boolean[n];
    }

    public ClassMetaData getClassMetaData() {
        return cmd;
    }

    public boolean containsField(int stateFieldNo) {
        return filled[stateFieldNo];
    }

    public boolean containsFieldAbs(int absFieldNo) {
        return containsField(convertAbsToState(absFieldNo));
    }

    public boolean containFields(int[] stateFieldNos) {
        for (int i = 0; i < stateFieldNos.length; i++) {
            if (!filled[stateFieldNos[i]]) return false;
        }
        return true;
    }

    public boolean containFieldsAbs(int[] absFieldNos) {
        int[] stateFieldNos = new int[absFieldNos.length];
        for (int i = 0; i < absFieldNos.length; i++) {
            stateFieldNos[i] = convertAbsToState(absFieldNos[i]);
        }
        return containFields(stateFieldNos);
    }

    public boolean isEmpty() {
        for (int i = 0; i < filled.length; i++) {
            if (filled[i]) return false;
        }
        return true;
    }

    public boolean containsFetchGroup(FetchGroup fetchGroup) {
        while (fetchGroup != null) {
            int[] fgn = fetchGroup.stateFieldNos;
            for (int i = fgn.length - 1; i >= 0; i--) {
                if (!containsField(fgn[i])) {
                    return false;
                }
            }
            fetchGroup = fetchGroup.superFetchGroup;
        }
        return true;
    }

    /**
     * Put the stateField numbers of all fields we have into stateFieldNoBuf. The number of field
     * numbers stored is returned.
     */
    public int getFieldNos(int[] stateFieldNoBuf) {
        int c = 0;
        for (int i = 0; i < filled.length; i++) {
            if (filled[i]) stateFieldNoBuf[c++] = i;
        }
        return c;
    }

    /**
     * Put the stateField numbers of all pass 1 fields we have into stateFieldNoBuf. The number of
     * field numbers stored is returned.
     *
     * @see FieldMetaData#secondaryField
     * @see ClassMetaData#pass2Fields
     */
    public int getPass1FieldNos(int[] stateFieldNoBuf) {
        int c = 0;
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] && cmd.stateFields[i].primaryField) stateFieldNoBuf[c++] = i;
        }
        return c;
    }

    public int getPass1FieldRefFieldNosWithNewOids(int[] stateFieldNoBuf) {
        int c = 0;
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] && cmd.stateFields[i].primaryField
                    && (cmd.stateFields[i].category == MDStatics.CATEGORY_REF
                    || cmd.stateFields[i].category == MDStatics.CATEGORY_POLYREF))
                stateFieldNoBuf[c++] = i;
        }
        return c;
    }

    /**
     * Put the stateField numbers of all pass 2 fields we have into stateFieldNoBuf. The number of
     * field numbers stored is returned.
     *
     * @see FieldMetaData#secondaryField
     * @see ClassMetaData#pass2Fields
     */
    public int getPass2FieldNos(int[] stateFieldNoBuf) {
        checkCmd();
        int c = 0;
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] && cmd.stateFields[i].secondaryField) stateFieldNoBuf[c++] = i;
        }
        return c;
    }

    /**
     * Do we contain any pass 1 fields?
     *
     * @see FieldMetaData#secondaryField
     * @see ClassMetaData#pass2Fields
     */
    public boolean containsPass1Fields() {
        checkCmd();
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] && cmd.stateFields[i].primaryField) return true;
        }
        return false;
    }

    /**
     * Do we contain any pass 2 fields?
     *
     * @see FieldMetaData#secondaryField
     * @see ClassMetaData#pass2Fields
     */
    public boolean containsPass2Fields() {
        checkCmd();
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] && cmd.stateFields[i].secondaryField) return true;
        }
        return false;
    }

    /**
     * Return 0 if state has the same field numbers as us, less than 0 we are
     * less than it or greater than 0 if we are greater than it. The definition
     * of less than and greater than is up to the state implementation but
     * must be detirministic. For fields that are stored using Oracle style
     * LOBs then the nullness of the value must also be considered in the
     * comparison i.e. states with field x null and not null respectively
     * are different.
     *
     * @param state State to compare to (will be for same class)
     */
    public int compareToPass1(State state) {
        GenericState s = (GenericState)state;
        checkCmd();
        boolean[] sf = s.filled;
        for (int i = 0; i < filled.length; i++) {
            if (!cmd.stateFields[i].primaryField) continue;
            boolean a = filled[i];
            boolean b = sf[i];
            if (a && !b) return -1;
            if (!a && b) return +1;
        }
        return 0;
    }

    /*public void clearNonPk() {
        if( cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION )
        {
            Object[] oldData = data;
            data = new Object[data.length];
            filled = new boolean[filled.length];
            for( int i=cmd.pkFieldNos.length-1; i>=0 ;i-- )
            {
                data[i] = oldData[i];
                filled[i] = true;
            }
        }
        else
        {
            data = new Object[data.length];
            filled = new boolean[filled.length];
        }

        //for the following pk fields don't need separate treatment
        makeClean();
        if (resolvedForClient != null) {
            resolvedForClient = new boolean[resolvedForClient.length];
        }
    }*/

    public void clear() {
        data = new Object[data.length];
        filled = new boolean[filled.length];
        makeClean();
        if (resolvedForClient != null) {
            resolvedForClient = new boolean[resolvedForClient.length];
        }
    }

    public void clearFilledFlags() {
        filled = new boolean[filled.length];
    }

    public void makeClean() {
        if (dirtyFields != null)
        {
            Arrays.fill(dirtyFields, false);
        }
        dirty = false;
    }

    public void clearNonFilled(State state) {
        GenericState gState = (GenericState)state;
        boolean[] localFilled = gState.filled;
        for (int i = localFilled.length - 1; i >= 0; i--) {
            if (!localFilled[i] && (cmd.stateFields[i].autoSet == MDStatics.AUTOSET_NO)) {
                data[i] = null;
                filled[i] = false;
                resolvedForClient[i] = false;
            }
        }
    }

    public void clearCollectionFields() {
        checkCmd();
        for (int i = 0; i < data.length; i++) {
            int category = cmd.stateFields[i].category;
            if (category == MDStatics.CATEGORY_COLLECTION
                    || category == MDStatics.CATEGORY_MAP
                    || category == MDStatics.CATEGORY_ARRAY) {
                data[i] = null;
                filled[i] = false;
                if (resolvedForClient != null) resolvedForClient[i] = false;
            }
        }
    }

    public void clearSCOFields() {
        checkCmd();
        for (int i = 0; i < cmd.scoFieldNos.length; i++) {
            int scoFieldNo = cmd.scoFieldNos[i];
            data[scoFieldNo] = null;
            filled[scoFieldNo] = false;
            resolvedForClient[scoFieldNo] = false;
        }
    }

    public final void clearTransactionNonPersistentFields() {
        checkCmd();
        final int n = cmd.txFieldNos.length;
        for (int i = 0; i < n; i++) {
            dirtyFields[cmd.txFieldNos[i]] = false;
        }
    }

    /**
     * Does this State contain exactly the same fields as the supplied State?
     *
     * @param state State to compare to (will be for same class)
     */
    public boolean hasSameFields(State state) {
        GenericState s = (GenericState)state;
        int n = filled.length;
        int i;
        for (i = 0; i < n && filled[i] == s.filled[i]; i++) ;
        return i == n;
    }

    /**
     * Is the supplied stateFieldNo null?
     */
    public boolean isNull(int stateFieldNo) {
        return data[stateFieldNo] == null;
    }

    /**
     * Does this State contain all of the application identity fields for
     * its class? This returns false if the class does not use application
     * identity.
     */
    public boolean containsApplicationIdentityFields() {
        checkCmd();
        if (cmd.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return false;
        FieldMetaData[] pkf = cmd.pcHierarchy[0].pkFields;
        for (int i = pkf.length - 1; i >= 0; i--) {
            if (!containsField(pkf[i].stateFieldNo)) return false;
        }
        return true;
    }

    public boolean containsValidAppIdFields() {
        checkCmd();
        if (cmd.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return false;
        FieldMetaData[] pkf = cmd.pcHierarchy[0].pkFields;
        for (int i = pkf.length - 1; i >= 0; i--) {
            if (!containsField(pkf[i].stateFieldNo)) return false;
            if (data[pkf[i].stateFieldNo] == null) return false;
            if (data[pkf[i].stateFieldNo].equals(pkf[i].getPKDefaultValue())) return false;
        }
        return true;
    }

    /**
     * Clear any application identity fields from this State. This is a NOP
     * if the class does not use application identity.
     */
    public void clearApplicationIdentityFields() {
        checkCmd();
        if (cmd.top.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return;
        FieldMetaData[] pkf = cmd.top.pkFields;
        for (int i = pkf.length - 1; i >= 0; i--) {
            data[pkf[i].stateFieldNo] = null;
            filled[pkf[i].stateFieldNo] = false;
        }
    }

    /**
     * Populate the primary key fields from the OID. This is only called
     * for PC classes that are using application identity.
     */
    public void copyFields(OID oid) {
        checkCmd();
        if (cmd.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return;
        GenericOID goid = (GenericOID)oid;
        Object[] pk = goid.getPk();
        FieldMetaData[] pkFields = cmd.pcHierarchy[0].pkFields;
        for (int i = 0; i < pk.length; i++) {
            data[pkFields[i].stateFieldNo] = pk[i];
            filled[pkFields[i].stateFieldNo] = true;
        }
    }

    /**
     * Replace any NewObjectOID's in fields in fieldNos in this state with
     * their realOID's. Entries in fieldNos that are less than 0 should be
     * skipped. Note that skipped entries will never be for fields that could
     * hold OIDs.
     */
    public boolean replaceNewObjectOIDs(int[] fieldNos, int fieldNosLength) {
        boolean containsUnResolvedNewOids = false;
        for (int i = 0; i < fieldNosLength; i++) {
            int fieldNo = fieldNos[i];
            if (fieldNo >= 0) {
                FieldMetaData fmd = cmd.stateFields[fieldNo];
                Object o;
                switch (fmd.category) {
                    case MDStatics.CATEGORY_REF:
                    case MDStatics.CATEGORY_POLYREF:
                        o = data[fieldNo];
                        if (o != null && o instanceof NewObjectOID) {
                            if (((NewObjectOID)o).realOID == null) {
                                containsUnResolvedNewOids = true;
                            } else {
                                data[fieldNo] = ((NewObjectOID)o).realOID;
                            }
                        }
                        break;
                }
            }
        }
        return containsUnResolvedNewOids;
    }

    /**
     * Populate the OID from this state. This is called for classes
     * using application identity when a new object is persisted. It will
     * not be called otherwise.
     */
    public void copyKeyFields(OID oid) {
        GenericOID gOid = (GenericOID)oid;
        Object[] pk = gOid.getPk();
        FieldMetaData[] pkFields = cmd.pkFields;
        for (int i = 0; i < pk.length; i++) {
            pk[i] = getInternalObjectField(pkFields[i].stateFieldNo);
        }
    }

    public boolean checkKeyFields(OID oid) {
        GenericOID gOid = (GenericOID)oid;
        Object[] pk = gOid.getPk();
        for (int i = 0; i < pk.length; i++) {
            if (!pk[i].equals(
                    getInternalObjectField(cmd.pkFields[i].stateFieldNo))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Populate the OID from this state. This is called for classes
     * using application identity when a primary key field of an existing
     * object is updated. It will not be called otherwise. Note that if the
     * primary key consists of multiple fields then those that have not
     * changed may not be in state.
     */
    public void copyKeyFieldsUpdate(OID oid) {
        GenericOID gOid = (GenericOID)oid;
        Object[] pk = gOid.getPk();
        FieldMetaData[] pkFields = cmd.pkFields;
        for (int i = 0; i < pk.length; i++) {
            int fieldNo = pkFields[i].stateFieldNo;
            if (containsField(fieldNo)) {
                pk[i] = getInternalObjectField(fieldNo);
            }
        }
    }

    /**
     * Add the graph indexes of all OIDs that we have direct references to
     * (e.g. foreign keys) to edges. This is called as part of the graph
     * sorting process.
     *
     * @see com.versant.core.server.PersistGraph#sort
     */
    public void findDirectEdges(OIDGraph graph,
                                IntArray edges) {
        int[] fieldNos = cmd.directRefStateFieldNos;
        int n = fieldNos.length;
        for (int i = 0; i < n; i++) {
            int fieldNo = fieldNos[i];
            if (!containsField(fieldNo)) continue;
            findDirectEdges(graph, cmd, fieldNo, this, edges);
        }
    }

    /**
     * Update all autoset fields that must be set on commit of a new JDO
     * instance.
     *
     * @see FieldMetaData#autoSet
     */
    public void updateAutoSetFieldsCreated(Date now) {
        if (!cmd.hasAutoSetFields) return;
        FieldMetaData[] stateFields = cmd.stateFields;
        for (int i = stateFields.length - 1; i >= 0; i--) {
            FieldMetaData fmd = cmd.stateFields[i];
            int autoset = fmd.autoSet;
            if (autoset != MDStatics.AUTOSET_CREATED
                    && autoset != MDStatics.AUTOSET_BOTH) {
                continue;
            }
            switch (fmd.typeCode) {
                case MDStatics.DATE:
                    setInternalObjectField(i, now);
                    break;

                case MDStatics.INT:
                    setInternalIntField(i, 1);
                    break;
                case MDStatics.SHORT:
                    setInternalShortField(i, (short)1);
                    break;
                case MDStatics.BYTE:
                    setInternalByteField(i, (byte)1);
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("Invalid typeCode " + fmd.typeCode +
                            " for autoset field: stateFieldNo " + i +
                            " " + fmd.name);
            }
        }
    }

    /**
     * Update all autoset fields that must be set on commit of modifications
     * to an existing JDO instance.
     *
     * @param oldState The pre-modification state of the instance.
     * @see FieldMetaData#autoSet
     */
    public void updateAutoSetFieldsModified(Date now, State oldState) {
        if (!cmd.hasAutoSetFields) return;
        FieldMetaData[] stateFields = cmd.stateFields;
        for (int i = stateFields.length - 1; i >= 0; i--) {
            FieldMetaData fmd = cmd.stateFields[i];
            int autoset = fmd.autoSet;
            if (autoset != MDStatics.AUTOSET_MODIFIED
                    && autoset != MDStatics.AUTOSET_BOTH) {
                continue;
            }
            // carl leave this check out of the generated State class
            if (fmd.typeCode != MDStatics.DATE && !oldState.containsField(i)) {
                throw BindingSupportImpl.getInstance().internal("oldState does not contain version field: " +
                        "stateFieldNo " + i + " " + fmd.name);
            }
            switch (fmd.typeCode) {
                case MDStatics.DATE:
                    setInternalObjectField(i, now);
                    break;

                case MDStatics.INT:
                    setInternalIntField(i,
                            (oldState.getIntField(i) + 1) & 0x7FFFFFFF);
                    break;
                case MDStatics.SHORT:
                    setInternalShortField(i, (short)
                            ((oldState.getShortField(i) + 1) & 0x7FFF));
                    break;
                case MDStatics.BYTE:
                    setInternalByteField(i, (byte)
                            ((oldState.getByteField(i) + 1) & 0x7F));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("Invalid typeCode " + fmd.typeCode +
                            " for autoset field: stateFieldNo " + i +
                            " " + fmd.name);
            }
        }
    }

    public boolean getBooleanField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Boolean)data[stateFieldNo]).booleanValue() : false;
    }

    public boolean getBooleanFieldAbs(int field) {
        return getBooleanField(convertAbsToState(field));
    }

    public char getCharField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Character)data[stateFieldNo]).charValue() : '0';
    }

    public char getCharFieldAbs(int field) {
        return getCharField(convertAbsToState(field));
    }

    public byte getByteField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Byte)data[stateFieldNo]).byteValue() : (byte)0;
    }

    public byte getByteFieldAbs(int field) {
        return getByteField(convertAbsToState(field));
    }

    public short getShortField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Short)data[stateFieldNo]).shortValue() : (short)0;
    }

    public short getShortFieldAbs(int field) {
        return getShortField(convertAbsToState(field));
    }

    public int getIntField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Integer)data[stateFieldNo]).intValue() : 0;
    }

    public int getIntFieldAbs(int field) {
        return getIntField(convertAbsToState(field));
    }

    public long getLongField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Long)data[stateFieldNo]).longValue() : 0;
    }

    public long getLongFieldInternal(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Long)data[stateFieldNo]).longValue() : 0;
    }

    public long getLongFieldAbs(int field) {
        return getLongField(convertAbsToState(field));
    }

    public float getFloatField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Float)data[stateFieldNo]).floatValue() : 0;
    }

    public float getFloatFieldAbs(int field) {
        return getFloatField(convertAbsToState(field));
    }

    public double getDoubleField(int stateFieldNo) {
        return data[stateFieldNo] != null ? ((Double)data[stateFieldNo]).doubleValue() : 0;
    }

    public double getDoubleFieldAbs(int field) {
        return getDoubleField(convertAbsToState(field));
    }

    public String getStringField(int stateFieldNo) {
        return (String)data[stateFieldNo];
    }

    public String getStringFieldAbs(int field) {
        return getStringField(convertAbsToState(field));
    }

    public Object getObjectField(int stateFieldNo, PersistenceCapable owningPC,
            VersantPMProxy pm, OID oid) {
        Object o = data[stateFieldNo];
        if (!resolvedForClient[stateFieldNo]) {
            if (o != null) {
                FieldMetaData fmd = cmd.stateFields[stateFieldNo];
                if (fmd.scoField) {
                    if (fmd.category == MDStatics.CATEGORY_ARRAY) {
                        if (fmd.elementTypeMetaData != null) {
                            o = data[stateFieldNo] = resolveArrayOIDs(
                                    (Object[])o, pm, fmd.elementType);
                        } else {
                            /**
                             * Empty arrays need to be converted to correct type.
                             */
                            
                            if (Array.getLength(o) == 0 && o.getClass().getComponentType() != fmd.componentType) {
                                data[stateFieldNo] = o = Array.newInstance(fmd.componentType, 0);
                            }
                            

                        }
                    } else {
                        data[stateFieldNo] = fmd.createSCO(pm,
                                pm.getVersantStateManager(owningPC), fmd,
                                owningPC, o);
                        o = data[stateFieldNo];
                    }
                } else {
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_EXTERNALIZED:
                            data[stateFieldNo] = o =
                                    fmd.externalizer.fromExternalForm(pm, o);
                            break;
                        case MDStatics.CATEGORY_REF:
                        case MDStatics.CATEGORY_POLYREF:
                            data[stateFieldNo] = o = pm.getObjectByIdForState(
                                    (OID)o, stateFieldNo, classIndex, oid);
                            break;
                    }
                }
                resolvedForClient[stateFieldNo] = true;
            } else if (cmd.isEmbeddedRef(stateFieldNo)) {
                data[stateFieldNo] = o = pm.getObjectByIdForState(null,
                        stateFieldNo, classIndex, oid);
                resolvedForClient[stateFieldNo] = true;
            } else if (cmd.isEmbeddedStruct(stateFieldNo)) {
                // helper method to obtain a boxed value from the pc
                // must not change the state, needs only to deliver
                // the newly boxed .NET struct from the PC
                boolean oldDirty = this.dirty;
                boolean oldDirtyField = this.dirtyFields[stateFieldNo];
                boolean oldResolvedForClient = this.resolvedForClient[stateFieldNo];
                Object oldData = this.data[stateFieldNo];
                boolean oldFilled = this.filled[stateFieldNo];
                // obtain current value as a boxed copy
                owningPC.jdoProvideField(cmd.stateFields[stateFieldNo].managedFieldNo);
                o = data[stateFieldNo];
                // reset state to state before provideField was called.
                data[stateFieldNo] = oldData;
                this.dirty = oldDirty;
                this.dirtyFields[stateFieldNo] = oldDirtyField;
                this.resolvedForClient[stateFieldNo] = oldResolvedForClient;
                this.filled[stateFieldNo] = oldFilled;
            }
        }
        if (Debug.DEBUG) {
            if (!cmd.stateFields[stateFieldNo].scoField && o != null && !cmd.stateFields[stateFieldNo].type.isAssignableFrom(
                    /*CHFC*/o.getClass()/*RIGHTPAR*/)) {
                Debug.OUT.println("########### stateFieldNo = " + stateFieldNo);
                cmd.dump();
                throw BindingSupportImpl.getInstance().internal("Type error");
            }
        }
        return o;
    }

    public boolean isFieldNullorZero(int stateFieldNo) {
        if (Debug.DEBUG) {
            if (!filled[stateFieldNo]) {
                throw BindingSupportImpl.getInstance().internal(
                        "Field " + stateFieldNo + " is not loaded");
            }
        }
        Object o = data[stateFieldNo];
        if (o == null) {
            return true;
        }
        FieldMetaData f = cmd.stateFields[stateFieldNo];
        switch (f.typeCode) {
            case MDStatics.BOOLEAN:
            case MDStatics.BOOLEANW:
                return !((Boolean)o).booleanValue();
            case MDStatics.BYTE:
            case MDStatics.BYTEW:
            case MDStatics.SHORT:
            case MDStatics.SHORTW:
            case MDStatics.INT:
            case MDStatics.INTW:
                return ((Number)o).intValue() == 0;
            case MDStatics.LONG:
            case MDStatics.LONGW:
                return ((Long)o).longValue() == 0L;
            case MDStatics.FLOAT:
            case MDStatics.FLOATW:
                return ((Float)o).floatValue() == 0.0f;
            case MDStatics.DOUBLE:
            case MDStatics.DOUBLEW:
            case MDStatics.BIGDECIMAL:
            case MDStatics.BIGINTEGER:
                return ((Number)o).doubleValue() == 0.0;
        }
        return false;
    }

    public Object getObjectFieldAbs(int absFieldNo,
            PersistenceCapable owningPC, VersantPMProxy pm,
            OID oid) {
        return getObjectField(convertAbsToState(absFieldNo), owningPC, pm, oid);
    }

    public void setBooleanField(int field, boolean newValue) {
        makeDirty(field);
        setInternalBooleanField(field, newValue);
    }

    public void setBooleanFieldAbs(int field, boolean newValue) {
        setBooleanField(convertAbsToState(field), newValue);
    }

    public void setCharField(int stateFieldNo, char newValue) {
        makeDirty(stateFieldNo);
        setInternalCharField(stateFieldNo, newValue);
    }

    public void setCharFieldAbs(int absFieldNo, char newValue) {
        setCharField(convertAbsToState(absFieldNo), newValue);
    }

    public void setByteField(int field, byte newValue) {
        makeDirty(field);
        setInternalByteField(field, newValue);
    }

    public void setByteFieldAbs(int absFieldNo, byte newValue) {
        setByteField(convertAbsToState(absFieldNo), newValue);
    }

    public void setShortField(int field, short newValue) {
        makeDirty(field);
        setInternalShortField(field, newValue);
    }

    public void setShortFieldAbs(int field, short newValue) {
        setShortField(convertAbsToState(field), newValue);
    }

    public void setIntField(int field, int newValue) {
        makeDirty(field);
        setInternalIntField(field, newValue);
    }

    public void setIntFieldAbs(int absFieldNo, int newValue) {
        setIntField(convertAbsToState(absFieldNo), newValue);
    }

    public void setLongField(int field, long newValue) {
        makeDirty(field);
        setInternalLongField(field, newValue);
    }

    public void setLongFieldAbs(int field, long newValue) {
        setLongField(convertAbsToState(field), newValue);
    }

    public void setFloatField(int field, float newValue) {
        makeDirty(field);
        setInternalFloatField(field, newValue);
    }

    public void setFloatFieldAbs(int field, float newValue) {
        setFloatField(convertAbsToState(field), newValue);
    }

    public void setDoubleField(int field, double newValue) {
        makeDirty(field);
        setInternalDoubleField(field, newValue);
    }

    public void setDoubleFieldAbs(int field, double newValue) {
        setDoubleField(convertAbsToState(field), newValue);
    }

    public void setStringField(int field, String newValue) {
        makeDirty(field);
        setInternalStringField(field, newValue);
    }

    public void setStringFieldAbs(int field, String newValue) {
        setStringField(convertAbsToState(field), newValue);
    }

    public void setObjectField(int field, Object newValue) {
        checkCmd();
        makeDirty(field);
        resolvedForClient[field] = true;
        if (Debug.DEBUG) {
            if (newValue != null && !cmd.stateFields[field].type.isAssignableFrom(
                    /*CHFC*/newValue.getClass()/*RIGHTPAR*/)) {
                Debug.OUT.println("########### field = " + field);
                Debug.OUT.println(
                        "## field type = " + cmd.stateFields[field].type.getName());
                Debug.OUT.println(
                        "## object type = " + newValue.getClass().getName());
                cmd.dump();
                throw BindingSupportImpl.getInstance().internal("Type error");
            }
        }
        setInternalObjectField(field, newValue);
    }

    public void setObjectFieldAbs(int field, Object newValue) {
        setObjectField(convertAbsToState(field), newValue);
    }

    public void setObjectFieldUnresolved(int field, Object newValue) {
        checkCmd();
        makeDirty(field);
        resolvedForClient[field] = false;
        setInternalObjectField(field, newValue);
    }

    public void setObjectFieldUnresolvedAbs(int field, Object newValue) {
        setObjectFieldUnresolved(convertAbsToState(field), newValue);
    }

//=======================setInternalXXX====================================

    public void setInternalBooleanField(int field, boolean newValue) {
        data[field] = newValue ? Boolean.TRUE : Boolean.FALSE;
        filled[field] = true;
    }

    public void setInternalBooleanFieldAbs(int field, boolean newValue) {
        setInternalBooleanField(convertAbsToState(field), newValue);
    }

    public void setInternalCharField(int field, char newValue) {
        data[field] = new Character(newValue);
        filled[field] = true;
    }

    public void setInternalCharFieldAbs(int field, char newValue) {
        setInternalCharField(convertAbsToState(field), newValue);
    }

    public void setInternalByteField(int field, byte newValue) {
        data[field] = new Byte(newValue);
        filled[field] = true;
    }

    public void setInternalByteFieldAbs(int field, byte newValue) {
        setInternalByteField(convertAbsToState(field), newValue);
    }

    public void setInternalShortField(int field, short newValue) {
        data[field] = new Short(newValue);
        filled[field] = true;
    }

    public void setInternalShortFieldAbs(int field, short newValue) {
        setInternalShortField(convertAbsToState(field), newValue);
    }

    public void setInternalIntField(int field, int newValue) {
        data[field] = new Integer(newValue);
        filled[field] = true;
    }

    public void setInternalIntFieldAbs(int field, int newValue) {
        setInternalIntField(convertAbsToState(field), newValue);
    }

    public void setInternalLongField(int field, long newValue) {
        data[field] = new Long(newValue);
        filled[field] = true;
    }

    public void setFilled(int stateFieldNo) {
        filled[stateFieldNo] = true;
    }

    public void setInternalLongFieldAbs(int field, long newValue) {
        setInternalLongField(convertAbsToState(field), newValue);
    }

    public void setInternalFloatField(int field, float newValue) {
        data[field] = new Float(newValue);
        filled[field] = true;
    }

    public void setInternalFloatFieldAbs(int field, float newValue) {
        setInternalFloatField(convertAbsToState(field), newValue);
    }

    public void setInternalDoubleField(int field, double newValue) {
        data[field] = new Double(newValue);
        filled[field] = true;
    }

    public void setInternalDoubleFieldAbs(int field, double newValue) {
        setInternalDoubleField(convertAbsToState(field), newValue);
    }

    public void setInternalStringField(int field, String newValue) {
        data[field] = newValue;
        filled[field] = true;
    }

    public void setInternalStringFieldAbs(int field, String newValue) {
        setInternalStringField(convertAbsToState(field), newValue);
    }

    public void setInternalObjectField(int field, Object newValue) {
        data[field] = newValue;
        filled[field] = true;
    }

    public void setInternalObjectFieldAbs(int field, Object newValue) {
        setInternalObjectField(convertAbsToState(field), newValue);
    }

//==========================getInternalXXX======================================

    public Object getInternalObjectField(int stateFieldNo) {
        return data[stateFieldNo];
    }

    public Object getInternalObjectFieldAbs(int field) {
        return getInternalObjectField(convertAbsToState(field));
    }

    public void updateFrom(State state) {
        GenericState gState = (GenericState)state;
        checkCompatibleState(gState);
        boolean[] fields = gState.filled;
        Object[] otherData = gState.data;
        boolean[] otherResolvedForClient = gState.resolvedForClient;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i]) {
                data[i] = otherData[i];
                filled[i] = true;
				if (otherResolvedForClient != null)
					resolvedForClient[i] = otherResolvedForClient[i];
            }
        }
    }

    /**
     * Mark all dirty fields as clean and not filled and not resolved.
     * <p/>
     * filled &= ~dirty
     * resolved &= ~dirty
     * dirty = 0
     */
    public void clearDirtyFields() {
        for (int i = 0; i < dirtyFields.length; i++) {
            if (dirtyFields[i]) {
                dirtyFields[i] = false;
                filled[i] = false;
                resolvedForClient[i] = false;
            }
        }
    }

    public void updateNonFilled(State state) {
        GenericState gState = (GenericState)state;
        checkCompatibleState(gState);
        boolean[] fields = gState.filled;
        Object[] otherData = gState.data;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] && !filled[i]) {
                data[i] = otherData[i];
                filled[i] = true;
            }
        }
    }

    private void checkCompatibleState(GenericState gState) {
        if (gState.getClassIndex() != getClassIndex()) {
            throw BindingSupportImpl.getInstance().internal(
                    "\nIncompatible states: supplied state"
                            + gState.getClassMetaData().qname
                            + ": Required " + getClassMetaData().qname);
        }
    }

    public void clearNonAutoSetFields() {
        for (int i = 0; i < cmd.nonAutoSetStateFieldNos.length; i++) {
            int f = cmd.nonAutoSetStateFieldNos[i];
            data[f] = null;
            filled[f] = false;
        }
    }

    public void retrieve(VersantPersistenceManagerImp sm) {
        FetchGroup fg = cmd.refFetchGroup;
        while (fg != null) {
            retrieveImp(fg.stateFieldNos, sm);
            fg = fg.superFetchGroup;
        }
    }

    private void retrieveImp(int[] fieldNos, VersantPersistenceManagerImp sm) {
        int fieldNo;
        FieldMetaData fmd;
        for (int i = fieldNos.length - 1; i >= 0; i--) {
            fieldNo = fieldNos[i];
            if (data[fieldNo] != null) {
                fmd = cmd.stateFields[fieldNo];
                if (fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_ARRAY:
                            sm.retrieveAllImp((Object[])data[fieldNo]);
                            break;
                        case MDStatics.CATEGORY_COLLECTION:
                            sm.retrieveAllImp((Collection)data[fieldNo]);
                            break;
                        case MDStatics.CATEGORY_MAP:
                            StateUtil.retrieve((Map)data[fieldNo], sm, fmd);
                            break;
                        case MDStatics.CATEGORY_REF:
                        case MDStatics.CATEGORY_POLYREF:
                            sm.retrieveImp(data[fieldNo]);
                            break;
                        default:
                            throw BindingSupportImpl.getInstance().internal(
                                    "Type not allowed");
                    }
                }
            }
        }
    }

    /**
     * @param pm
     * @param pcStateMan
     */
    public void addRefs(VersantPersistenceManagerImp pm, PCStateMan pcStateMan) {
        if (dirty) {
            FetchGroup fg = findFirstRefFG(cmd);
            while (fg != null) {
                addRefs(fg.stateFieldNos, pm, pcStateMan);
                fg = fg.superFetchGroup;
            }
        }
    }

    private FetchGroup findFirstRefFG(ClassMetaData cmd) {
        if (cmd.refFetchGroup != null) return cmd.refFetchGroup;
        if (cmd.refFetchGroup == null && cmd.pcSuperMetaData == null) return null;
        return findFirstRefFG(cmd.pcSuperMetaData);
    }

    /**
     * A ref may be a direct ref or an item in collection/map/array.
     */
    private final void addRefs(int[] fieldNos,
                               VersantPersistenceManagerImp pm, PCStateMan sm) {
        int fieldNo;
        FieldMetaData fmd;
        // the fields MUST be processed in ascending order or embedded
        // reference fields containing other embedded reference fields
        // will not work as the embedded-embedded field will be
        // processed prior to being filled
        for (int i = 0; i < fieldNos.length; i++) {
            fieldNo = fieldNos[i];
            if (dirtyFields[fieldNo]) {
                if (data[fieldNo] != null) {
                    fmd = cmd.stateFields[fieldNo];
                    if (fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                        switch (fmd.category) {
                            case MDStatics.CATEGORY_ARRAY:
                                StateUtil.doReachable((Object[])data[fieldNo],
                                        pm);
                                break;
                            case MDStatics.CATEGORY_COLLECTION:
                                StateUtil.doReachable(
                                        (Collection)data[fieldNo], pm);
                                break;
                            case MDStatics.CATEGORY_MAP:
                                StateUtil.doReachable((Map)data[fieldNo], pm,
                                        fmd);
                                break;
                            case MDStatics.CATEGORY_REF:
                            case MDStatics.CATEGORY_POLYREF:
                                if (data[fieldNo] instanceof PersistenceCapable) {
                                    if (fmd.embedded) {
                                        StateUtil.doReachableEmbeddedReference(
                                                (PersistenceCapable)data[fieldNo],
                                                pm, sm, fmd);

                                    } else {
                                        StateUtil.doReachable(
                                                (PersistenceCapable)data[fieldNo], pm);
                                    }
                                }
                                break;
                            default:
                                throw BindingSupportImpl.getInstance().internal(
                                        "Type not allowed");
                        }
                    }
                }
            }
        }
    }

    public void validateForCache() {
        checkCmd();
        FieldMetaData[] fmds = cmd.stateFields;
        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData fieldMetaData = fmds[i];
            Object o = data[fieldMetaData.stateFieldNo];
            if (o != null) {
                int cat = fieldMetaData.category;
                if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) {
                    if (!(o instanceof OID)) {
                        fieldMetaData.dump(Debug.OUT, " ");
                        throw BindingSupportImpl.getInstance().internal(
                                "The instance is not valid to be added to cache");
                    }
                }
            }
        }
    }

    public void makeDirty(int stateFieldNo) {
        dirty = true;
        dirtyFields[stateFieldNo] = true;
    }

    public void makeDirtyAbs(int absFieldNo) {
        makeDirty(convertAbsToState(absFieldNo));
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isDirty(int absFieldNo) {
        return dirtyFields[convertAbsToState(absFieldNo)];
    }

    /**
     * This must be called once the state is ready to be sent off to the server.
     * <p/>
     * All FirstClass instances will be resolved to an OID and second class
     * instances will be resolved to some serializable/storable format that represents the
     * state of the field.
     */
    private void prepare(PersistenceContext pm,
                         VersantStateManager sm, GenericState origState) {
        FieldMetaData fmd;
        for (int i = 0; i < data.length; i++) {
            if (filled[i] && data[i] != null) {
                fmd = cmd.stateFields[i];
                if (fmd.scoField) {
                    if (fmd.typeCode == MDStatics.DATE) {
                        data[i] = StateUtil.getPValueForSCO(
                                (java.util.Date)data[i]);

                    } else {
                        switch (fmd.category) {
                            case MDStatics.CATEGORY_ARRAY:
                                if (!(data[i] instanceof OID[]) && fmd.isElementTypePC()) {
                                    data[i] = resolveArrayValues(
                                            (Object[])data[i], pm);
                                }
                                break;
                            case MDStatics.CATEGORY_COLLECTION:
                                if (!(data[i] instanceof OID[])) {
                                    data[i] = StateUtil.getPValueForSCO(
                                            (Collection)data[i], pm, sm, fmd);
                                }
                                break;
                            case MDStatics.CATEGORY_MAP:
                                if (!(data[i] instanceof MapEntries)) {
                                    data[i] = StateUtil.getPValueForSCO(
                                        (Map)data[i], pm, fmd);
                                }
                                break;
                            default:
                                throw BindingSupportImpl.getInstance().internal("No logic defined for field type "
                                        + MDStaticUtils.toSimpleName(
                                                fmd.typeCode)
                                        + " field name = " + fmd.name);
                        }
                    }
                } else {
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_EXTERNALIZED:
                            if (origState.resolvedForClient[i]) {
                                data[i] = fmd.externalizer.toExternalForm(
                                        pm.getPersistenceManager(), data[i]);
                            }
                            break;
                        case MDStatics.CATEGORY_REF:
                        case MDStatics.CATEGORY_POLYREF:
                            if (fmd.embedded) {
                                data[i] = null;
                            } else if (data[i] instanceof PersistenceCapable) {
                                data[i] = StateUtil.getPValueForRef(
                                        (PersistenceCapable)data[i], pm);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * This is to copy the fields that is filled in on the supplied state to the
     * current state. The objective behind it is to keep a copy of the date to
     * compare against the db at the time of commit. The only field types of interest
     * is java.util.Date and Reference fields.
     *
     * @param state
     * @param sm
     */
    public void copyFieldsForOptimisticLocking(State state,
                                               VersantPersistenceManagerImp sm) {
        GenericState gState = (GenericState)state;
        boolean[] fields = gState.filled;
        Object[] otherData = gState.data;
        FieldMetaData fmd = null;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] && !filled[i]) {
                if (otherData[i] != null) {
                    fmd = cmd.stateFields[i];
                    boolean resolved = gState.resolvedForClient != null ? gState.resolvedForClient[i] : false;
                    if (fmd.typeCode == MDStatics.DATE) {
                        if (resolved) {

                            data[i] = ((Date)otherData[i]).clone();



                        } else {
                            data[i] = otherData[i];
                        }
                        filled[i] = true;
                    } else if (fmd.category == MDStatics.CATEGORY_REF
                            || fmd.category == MDStatics.CATEGORY_POLYREF) {
                        if (resolved) {
                            data[i] = sm.getInternalOID(
                                    (PersistenceCapable)otherData[i]);
                        } else {
                            data[i] = otherData[i];
                        }
                        filled[i] = true;
                    } else {
                        data[i] = otherData[i];
                        filled[i] = true;
                    }
                }
            }
        }
    }

    /**
     * This copies all the fakeFields from one the supplied state.
     *
     * @param state
     */
    public void copyOptimisticLockingField(State state) {
        GenericState gState = (GenericState)state;
        FieldMetaData optimisticLockingField = cmd.optimisticLockingField;
        if (optimisticLockingField == null) return;
        if (gState.filled[optimisticLockingField.stateFieldNo]) {
            data[optimisticLockingField.stateFieldNo] = gState.data[optimisticLockingField.stateFieldNo];
            filled[optimisticLockingField.stateFieldNo] = true;
        }
    }

    /**
     * Ensure that all fields that is of type sco is represented by an sco instance.
     * This does not mean that an instance  that is already represented as an sco
     * will be recreated.
     *
     * @return The amount field no's that was added to the int[].
     */
    public int replaceSCOFields(PersistenceCapable owner,
            VersantPMProxy sm, int[] absFields) {
        if (cmd.scoFieldNos.length == 0) return 0;
        int[] scoStateFieldNos = cmd.scoFieldNos;
        int count = 0;
        for (int i = 0; i < scoStateFieldNos.length; i++) {
            int scoStateFieldNo = scoStateFieldNos[i];
            if (data[scoStateFieldNo] != null) {
                FieldMetaData fmd = cmd.stateFields[scoStateFieldNo];
                data[scoStateFieldNo] = fmd.createSCO(sm,
                        sm.getVersantStateManager(owner),
                        fmd, owner, data[scoStateFieldNo]);
                absFields[count++] = cmd.stateFields[scoStateFieldNo].managedFieldNo;
            }
        }
        return count;
    }




    public void unmanageSCOFields() {
        final Object[] d = data;
        for (int i = 0; i < cmd.scoFieldNos.length; i++) {
            final int scoFieldNo = cmd.scoFieldNos[i];
            if ((d[scoFieldNo] != null)) {
                if ((d[scoFieldNo] instanceof VersantSimpleSCO)) {
                    ((VersantSimpleSCO)d[scoFieldNo]).makeTransient();
                }
            }
        }
    }

    public State getCopy() {
        checkCmd();
        final GenericState copy = new GenericState(cmd);
        System.arraycopy(this.filled, 0, copy.filled, 0, copy.filled.length);
        System.arraycopy(this.data, 0, copy.data, 0, copy.data.length);
        return copy;
    }

    /**
     * <p>This return a deep clone of this state instance with only fields that
     * must be sent to the server to persist changes to this instance filled
     * in. For JdbcDataStore this will include only the dirty fields. For
     * VdsDataStore this includes all fields so the whole DataStoreObject
     * can be written.</p>
     * <p/>
     * <p>All 'First Class Objects' will be resolved to an OID and
     * 'Second Class Objects' will be resolved to some serializable/storable
     * format that represents the state of the field.</p>
     *
     * @return True if some fields were written to stateToStore and false if
     *         not (i.e. we have no dirty fields)
     */
    public boolean fillToStoreState(State stateToStore,
                                    PersistenceContext pm,
                                    VersantStateManager sm) {
        GenericState state = (GenericState)stateToStore;
        if (dirty) {
            if (cmd.storeAllFields) {
                // we must include all primary fields not just the dirty ones
                for (int i = 0; i < data.length; i++) {
                    FieldMetaData fmd = cmd.stateFields[i];
                    if (fmd.persistenceModifier ==
                            MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                        continue;
                    } else if (fmd.primaryField) {
                        if (filled[i]) {
                            state.data[i] = data[i];
                            state.filled[i] = true;
                        }
                    } else if (fmd.secondaryField) {
                        if (dirtyFields[i]) {
                            state.data[i] = data[i];
                            state.filled[i] = true;
                        } else {
                            state.data[i] = null;
                            state.filled[i] = false;
                        }
                    } else {
                        throw BindingSupportImpl.getInstance().internal("not primaryField " +
                                "or secondaryField: " + fmd.getQName());
                    }
                }
                state.prepare(pm, sm, this);
            } else {
                int count = 0;
                for (int i = 0; i < data.length; i++) {
                    if (dirtyFields[i]) {
                        count++;
                        state.data[i] = data[i];
                        state.filled[i] = true;
                    } else {
                        state.data[i] = null;
                        state.filled[i] = false;
                    }
                }
                if (count == 0) {
                    dirty = false;
                    state = null;
                } else {
                    state.prepare(pm, sm, this);
                }
            }
        } else {
            state = null;
        }
        return state != null;
    }

    public void fillForRead(State dest,
                            VersantPersistenceManagerImp pm) {
        if (Debug.DEBUG) {
            // method only implemented for VDS - should not be called for JDBC
            if (!cmd.storeAllFields) {
                throw BindingSupportImpl.getInstance().internal("Only implemented for " +
                        "classes with cmd.storeAllFields true");
            }
        }
        GenericState state = (GenericState)dest;
        for (int i = 0; i < data.length; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            // filled[i] will be true for VDS so we do not need to check it
            if (fmd.fake) {
                state.data[i] = data[i];
                state.filled[i] = true;
            } else {
                int cat = fmd.category;
                if (cat == MDStatics.CATEGORY_REF
                        || cat == MDStatics.CATEGORY_POLYREF) {
                    Object o = data[i];
                    if (o instanceof PersistenceCapable) {
                        PersistenceCapable pc = (PersistenceCapable)o;
                        if (pc.jdoGetPersistenceManager() == null) {
                            o = null;
                        } else {
                            o = StateUtil.getPValueForRef(pc, pm);
                        }
                    }
                    state.data[i] = o;
                    state.filled[i] = true;
                }
            }
        }
    }

    public String toString() {
        checkCmd();
        StringBuffer s = new StringBuffer();
        s.append("GenericState@");
        s.append(Integer.toHexString(System.identityHashCode(this)));
        s.append(" ");
        String name = cmd.qname;
        int i = name.lastIndexOf('.');
        if (i >= 0) name = name.substring(i + 1);
        s.append(name);
        s.append(" {\n");
        boolean first = true;
        int n = data.length;
        for (i = 0; i < n; i++) {
            if (!filled[i]) continue;
            if (first) {
                first = false;
            } else {
                s.append(",\n  ");
            }
            s.append(cmd.stateFields[i].name);
            s.append('[');
            s.append(cmd.stateFields[i].stateFieldNo);
            s.append(']');
            s.append('=');
            s.append(toString(data[i]));
            s.append(" type = ").append(data[i] != null ? data[i].getClass().getName() : " null");
            s.append(" SysId = ").append(System.identityHashCode(data[i]));
            s.append(" jdoClsId = ").append(cmd.classId);
            if (resolvedForClient != null) {
                s.append("  res = ").append(resolvedForClient[i]);
            }
        }
        s.append('}');
        return s.toString();
    }

    private String toString(Object o) {
        if (o == null) return "null";
        if (o instanceof String) {
            String s = (String)o;
            if (s.length() > 100) return s.substring(0, 100) + " ...";
            return s;
        } else if (o instanceof OID) {
            return ((OID)o).toStringImp();
        } else {
            return o.toString();
        }
    }

    protected final void checkCmd() {
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().internal("the cmd is null");
        }
    }

    public void writeExternal(OIDObjectOutput os) throws IOException {
//        if (Debug.DEBUG) {
//            System.out.println("%%% GenericState.writeExternal " + cmd.qname);
//        }
        os.writeBoolean(dirty);
        for (int i = 0; i < filled.length; i++) {
            os.writeBoolean(filled[i]);
        }
        for (int i = 0; i < data.length; i++) {
            if (!filled[i]) {
                continue;
            }
            FieldMetaData fmd = cmd.stateFields[i];
//            if (Debug.DEBUG) {
//                Object o = data[i];
//                System.out.println("%%%   GenericState.writeExternal " + fmd +
//                        " " + (o instanceof OID ? ((OID)o).toStringImp() : String.valueOf(o)));
//                os.writeUTF("begin " + fmd.name);
//            }
            switch (fmd.category) {
                case MDStatics.CATEGORY_ARRAY:
                    SerUtils.writeArrayField(fmd, os, data[i]);
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                    SerUtils.writeCollectionOrMapField(os, fmd, data[i]);
                    break;
                case MDStatics.CATEGORY_EXTERNALIZED:
                case MDStatics.CATEGORY_SIMPLE:
                    SerUtils.writeSimpleField(fmd, os, data[i]);
                    break;
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    os.write((OID)data[i]);
                    break;
                case MDStatics.CATEGORY_TRANSACTIONAL:
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("No logic defined for field type "
                            + MDStaticUtils.toSimpleName(fmd.typeCode)
                            + " field name = " + fmd.name);
            }
//            if (Debug.DEBUG) {
//                os.writeUTF("end " + fmd.name);
//            }
        }
    }

//    private void expect(OIDObjectInput is, String expected)
//            throws IOException {
//        String s = is.readUTF();
//        if (!expected.equals(s)) {
//            throw new StreamCorruptedException("Expected: '" + expected +
//                    "' got '" + s + "'");
//        }
//    }

    public void readExternal(OIDObjectInput is) throws ClassNotFoundException,
            IOException {
//        if (Debug.DEBUG) {
//            System.out.println("%%% GenericState.readExternal " + cmd);
//        }
        // no need to read the classIndex and cmd as they are set in the
        // constructor
        dirty = is.readBoolean();
        for (int i = 0; i < filled.length; i++) {
            filled[i] = is.readBoolean();
        }
        for (int i = 0; i < data.length; i++) {
            if (!filled[i]) {
                continue;
            }
            FieldMetaData fmd = cmd.stateFields[i];
//            if (Debug.DEBUG) {
//                System.out.println("%%%   GenericState.readExternal " + fmd);
//                expect(is, "begin " + fmd.name);
//            }
            switch (fmd.category) {
                case MDStatics.CATEGORY_ARRAY:
                    data[i] = SerUtils.readArrayField(fmd, is);
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                    data[i] = SerUtils.readCollectionOrMapField(is, fmd);
                    break;
                case MDStatics.CATEGORY_SIMPLE:
                case MDStatics.CATEGORY_EXTERNALIZED:
                    data[i] = SerUtils.readSimpleField(fmd, is);
                    break;
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    data[i] = is.readOID();
                    break;
                case MDStatics.CATEGORY_TRANSACTIONAL:
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("No logic defined for field type "
                            + MDStaticUtils.toSimpleName(fmd.typeCode)
                            + " field name = " + fmd.name);
            }
//            if (Debug.DEBUG) {
//                Object o = data[i];
//                System.out.println("%%%   GenericState.readExternal " + fmd +
//                        " " + (o instanceof OID ? ((OID)o).toStringImp() : String.valueOf(o)));
//                expect(is, "end " + fmd.name);
//            }
        }
    }

    public String getVersion() {
        return Debug.VERSION;
    }

    public static Object readSimple(int type, DataInput is) throws IOException {
        if (is.readInt() == 0) {
            return null;
        } else {
            switch (type) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    return new Integer(is.readInt());
                case MDStatics.SHORTW:
                case MDStatics.SHORT:
                    return new Short(is.readShort());
                case MDStatics.STRING:
                    return is.readUTF();
                case MDStatics.BOOLEANW:
                case MDStatics.BOOLEAN:
                    return new Boolean(is.readBoolean());
                case MDStatics.BYTEW:
                case MDStatics.BYTE:
                    return new Byte(is.readByte());
                case MDStatics.DOUBLEW:
                case MDStatics.DOUBLE:
                    return new Double(is.readDouble());
                case MDStatics.FLOATW:
                case MDStatics.FLOAT:
                    return new Float(is.readFloat());
                case MDStatics.LONGW:
                case MDStatics.LONG:
                    return new Long(is.readLong());
                case MDStatics.DATE:
                    return new Date(is.readLong());
                case MDStatics.LOCALE:
                    return new Locale(is.readUTF(), is.readUTF(), is.readUTF());
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "readSimpleField for " + type + " is not supported");
            }
        }
    }

    public boolean isHollow() {
        for (int i = 0; i < filled.length; i++) {
            if (filled[i]) return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        try {
            GenericState state = (GenericState)obj;
            int n = filled.length;
            for (int i = 0; i < n; i++) {
                if (filled[i] != state.filled[i]) {
                    return false;
                }
                if (data[i] != null) {
                    if (!data[i].equals(state.data[i])) {
                        return false;
                    }
                } else {
                    if (state.data != null) {
                        return false;
                    }
                }
                return true;
            }
        } catch (ClassCastException e) {
            return false;
        }
        return false;
    }

    public boolean isResolvedForClient(int stateFieldNo) {
        if (resolvedForClient != null && resolvedForClient[stateFieldNo]) {
            return true;
        }
        return false;
    }

    /**
     * The value of the version field on the pc.
     * This will return null if there are no version fields.
     */
    public Object getOptimisticLockingValue() {
        if (cmd.optimisticLockingField != null) {
            return data[cmd.optimisticLockingField.stateFieldNo];
        }
        return null;
    }

    /**
     * Add the values of any non-null reference fields used as back or inverse
     * fields for unmanaged one-to-many collections for eviction from the L2
     * cache on commit. Note that the filled status of the field is not
     * checked. This method is called only for newly managed instances so
     * all fields will be filled.
     *
     * Example: Person has n Addresses (not managed, bidirectional in memory);
     * if creating a new Address with a reference to Person, Person must
     * be evicted, because user not necessarily has updated the other side,
     * so other side, i.e. collection in Person object, may be invalid
     */
    public void addOneToManyInverseFieldsForL2Evict(
            VersantPersistenceManagerImp pm) {
        checkCmd();
        for (int i = 0; i < data.length; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            if (fmd.isDetail && !fmd.managed) {
                Object o = data[i];
                if (o != null) {
                    pm.evictFromL2CacheAfterCommitImp(o);
                }
            }
        }
    }

}

