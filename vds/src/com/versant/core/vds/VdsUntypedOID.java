
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
package com.versant.core.vds;

import com.versant.core.common.*;
import com.versant.core.util.FastParser;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.vds.util.Loid;

import java.io.*;

/**
 * OID implementation that does not know the class of the referenced instance.
 * The hashcode() and equals() methods work the same as those in VdsGenericOID
 * so typed and untyped OIDs can be interchanged as keys in maps. The
 * hyperdrive OID for VDS must behave in the same way.
 */
public class VdsUntypedOID implements OID, Externalizable {

    private long loid;

    public VdsUntypedOID() {
    }

    public VdsUntypedOID(long loid) {
        this.loid = loid;
    }

    /**
     * Just use the least signficant bits of the loid. The other OID classes
     * must use the same hashing function.
     */
    public int hashCode() {
        return (int)loid;
    }

    public OID fillFromIDObject(Object id) {
        return null;
    }

    /**
     * We can be compared to any other OID class instance. The other OID
     * classes must delegate to the object being compared to if it is
     * not an instance of themselves to make this work:
     *
     * <pre>
     * if (o instanceof Self) {
     *     ...
     * } else {
     *     return o.equals(this);
     * }
     * </pre>
     *
     */
    public boolean equals(Object o) {
        return loid == ((OID)o).getLongPrimaryKey();
    }

    public boolean isResolved() {
        return false;
    }

    private RuntimeException badCall() {
        return BindingSupportImpl.getInstance().internal(
                "Method should not be invoked: LOID " + this);
    }

    public void resolve(State state) {
        // nothing to do
    }

    public int getClassIndex() {
        return -1;
    }

    public ClassMetaData getClassMetaData() {
        throw badCall();
    }

    public ClassMetaData getAvailableClassMetaData() {
        return null;
    }

    public ClassMetaData getBaseClassMetaData() {
        return null;
    }

    public int getIdentityType() {
        return MDStatics.IDENTITY_TYPE_DATASTORE;
    }

    public void copyKeyFields(Object[] data) {
        loid = ((Number)data[0]).longValue();
    }

    public OID copy() {
        throw badCall();
    }

    public void fillFromPK(Object pk) {
        throw badCall();
    }

    public void fillFromIDString(String idString, int index) {
        loid = FastParser.parseLong(idString, index);
    }

    public String toPkString() {
        return Loid.asString(loid);
    }

    public String toSString() {
        return Loid.asString(loid);
    }

    public String toStringImp() {
        return Loid.asString(loid);
    }

    public String toString() {
        return toStringImp();
    }

    public int compareTo(Object o) {
        throw badCall();
    }

    public long getLongPrimaryKey() {
        return loid;
    }

    public void setLongPrimaryKey(long pk) {
        loid = pk;
    }

    public Object createObjectIdClassInstance() {
        throw badCall();
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        loid = in.readLong();
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        loid = in.readLong();
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        out.writeLong(loid);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(loid);
    }

    public int getAvailableClassId() {
        return -1;
    }

    public boolean isNew() {
        return false;
    }

    public OID getMappedOID() {
        return this;
    }

    public OID getRealOID() {
        return this;
    }

    public OID getAvailableOID() {
        return this;
    }

    /**
     * Factory to create instances of us.
     */
    public static class Factory implements StateAndOIDFactory {

        public OID createUntypedOID() {
            return new VdsUntypedOID();
        }

        private RuntimeException badCall() {
            return BindingSupportImpl.getInstance().internal("");
        }

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            throw badCall();
        }

        public State createState(ClassMetaData cmd) {
            throw badCall();
        }

        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            throw badCall();
        }
    }
}

