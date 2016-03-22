
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.OIDObjectOutput;

import java.io.*;

/**
 * This OID class is used for new JDO instances of any type. It is used as
 * a placeholder for the real OID for new objects. The realOID field is
 * filled once the real OID has been created so that references to this
 * OID can be fixed up.
 */
public class NewObjectOID implements OID {

    private ClassMetaData cmd;
    /**
     * This is an id that has to be unique in a client space.
     *
     * @see #init
     */
    public int idNo;
    /**
     * The real OID generated for this object. This is filled when the new
     * object is persisted. Other objects with references to this OID use
     * this field instead when they are persisted.
     */
    public OID realOID;

    public NewObjectOID(ClassMetaData cmd) {
        this.cmd = cmd;
    }

    /**
     * This is only for used for Externalizable
     *
     * @see java.io.Externalizable
     */
    public NewObjectOID() {
    }

    /**
     * Is this an OID for a new object?
     */
    public boolean isNew() {
        return true;
    }

    public boolean isResolved() {
        return true;
    }

    public void resolve(State state) {
    }

    public int getIdentityType() {
        return cmd.identityType;
    }

    /**
     * Initialise the oid.
     */
    public void init(int id) {
        idNo = id;
    }

    public int getClassIndex() {
        return cmd.index;
    }

    public ClassMetaData getCmd() {
        return cmd;
    }

    /**
     * Get the meta data for our class.
     *
     */
    public ClassMetaData getClassMetaData() {
        return cmd;
    }

    public ClassMetaData getAvailableClassMetaData() {
        return cmd;
    }

    public ClassMetaData getBaseClassMetaData() {
        return cmd.top;
    }

    public void copyKeyFields(Object[] data) {
        throw BindingSupportImpl.getInstance().internal(
                "copyKeyFields(Object[]) called on NewObjectOID");
    }

    public OID copy() {
        NewObjectOID nOID = new NewObjectOID();
        nOID.cmd = cmd;
        nOID.idNo = idNo;
        return nOID;
    }

    public void fillFromPK(Object pk) {
        //no-op
    }

    public OID fillFromIDObject(Object id) {
        //no-op
        return this;
    }

    public int hashCode() {
        return cmd.index + idNo;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof NewObjectOID) {
            NewObjectOID other = (NewObjectOID)obj;
            if (other.cmd == cmd && other.idNo == idNo) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return toStringImp();
    }

    /**
     * Get the toString of this OID even if it has not been resolved.
     */
    public String toStringImp() {
        return "NewObjectOID@" +
                Integer.toHexString(System.identityHashCode(this)) +
                " classIndex = " + cmd.index +
                " id = " + idNo +
                " realOID = " + realOID;
    }

    public String toSString() {
        return "NewObjectOID@" +
                Integer.toHexString(System.identityHashCode(this)) +
                " classIndex = " + cmd.index +
                " id = " + idNo + (realOID == null ? "" : " realOID = " + realOID.toSString());
    }

    public String toPkString() {
        return "(New)";
    }

    public OID getAvailableOID() {
        if (realOID != null) return realOID;
        return this;
    }

    public OID getRealOID() {
        return realOID;
    }

    public OID setRealOid(OID oid) {
        if (oid == null) {
            throw BindingSupportImpl.getInstance().internal("The supplied oid is NULL");
        }
        if (this.realOID == null) {
            this.realOID = oid;
        } else {
            if (!this.realOID.equals(oid)) {
                throw BindingSupportImpl.getInstance().internal("The supplied R-OID is not " +
                        "equal to the current realOID");
            }
        }
        return oid;
    }

    public void writeExternal(OIDObjectOutput os) throws IOException {
        os.writeInt(idNo);
        os.write(realOID);
    }

    public void readExternal(OIDObjectInput is) throws ClassNotFoundException,
            IOException {
        idNo = is.readInt();
        realOID = is.readOID();
    }

    public void fillFromIDString(String idString, int index) {
        throw BindingSupportImpl.getInstance().internal("fillFromIDString called");
    }

    public void fillFromIDString(String idString) {
        throw BindingSupportImpl.getInstance().internal("Should not be called");
    }

    public int compareTo(Object o) {
        OID oo = (OID)o;
        int diff = cmd.top.index - oo.getBaseClassMetaData().index;
        if (diff != 0) return diff;
        if (oo.isNew()) {
            return idNo - ((NewObjectOID)o).idNo;
        } else {
            return -1; // we are always before non-new OIDs
        }
    }

    /**
     * Return the primary key stored in this OID as an long. This will only
     * be called for datastore identity classes.
     */
    public long getLongPrimaryKey() {
        if (realOID == null) return idNo;
        return realOID.getLongPrimaryKey();
    }

    /**
     * Set the primary key stored in this OID as an long. This will only be
     * called for datastore identity classes.
     */
    public void setLongPrimaryKey(long pk) {
        realOID.setLongPrimaryKey(pk);
    }

    public Object createObjectIdClassInstance() {
        throw BindingSupportImpl.getInstance().internal("Should not be called");
     }

    public int getAvailableClassId() {
        return getAvailableClassMetaData().classId;
    }

    public NewObjectOID newInstance(ClassMetaData cmd) {
        return new NewObjectOID(cmd);
    }

}
