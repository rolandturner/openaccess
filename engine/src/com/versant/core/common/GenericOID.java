
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
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.util.FastParser;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.OIDObjectOutput;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.lang.reflect.Constructor;

/**
 * This is an OID implementation suitable for use with any PC class. It is
 * intended for use during development when the OID class generating code is
 * broken. The meta data OID factory method can just return these for all
 * PC classes. Performance is not important for this class as it will not
 * be used in a production release.
 */
public abstract class GenericOID implements OID, Externalizable

{

    protected ClassMetaData cmd;
    protected boolean resolved;
    protected Object[] pk;

    public GenericOID() {
    }

    public GenericOID(ClassMetaData cmd, boolean resolved) {
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().internal("The supplied cmd is null");
        }
        this.cmd = cmd;
        this.resolved = resolved;
        pk = new Object[cmd.pkFields == null ? 1 : cmd.pkFields.length];
    }

    /**
     * This is for the GenericState implementation.
     */
    public Object[] getPk() {
        return pk;
    }

    /**
     * Do we know the actual class of the object we are referencing? An
     * OID may be created from a reference to a base class. The actual
     * class of the object referenced might only be detirmined when its
     * State is fetched from the store.
     *
     * @see #resolve
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Resolve this OID from the state. This will update our class index
     * to reflect the state. It is a NOP to call this on an already
     * resolved OID.
     *
     * @see #isResolved
     */
    public void resolve(State state) {
        if (!resolved) {
            checkCompatible(state);
            cmd = state.getClassMetaData();
            resolved = true;
        }
    }

    private void checkCompatible(State state) {
        if (cmd != null && (cmd.top.classId != state.getClassMetaData().top.classId)) {
            throw BindingSupportImpl.getInstance().internal(
                    "Incompatible state of type '"
                            + state.getClassMetaData().qname
                            + "' used to resolve oid of type '"
                            + cmd.qname + "'");
        }
    }

    public int getClassIndex() {
        return cmd.index;
    }

    public ClassMetaData getClassMetaData() {
        if (!resolved) {
            throw BindingSupportImpl.getInstance().internal(
                    "Called 'getClassMetaData()' on unresolved oid");
        }
        return cmd;
    }

    public ClassMetaData getAvailableClassMetaData() {
        return cmd;
    }

    public ClassMetaData getBaseClassMetaData() {
        return cmd.top;
    }

    public int getIdentityType() {
        return cmd.top.identityType;
    }

    public void copyKeyFields(Object[] data) {
        for (int i = 0; i < pk.length; i++) pk[i] = data[i];
    }

    /**
     * Return a copy of the oid.
     */
    public OID copy() {
        GenericOID copy = newInstance();
        copy.resolved = resolved;
        copy.cmd = cmd;
        copy.pk = pk;
        return copy;
    }

    protected abstract GenericOID newInstance();

    public void fillFromPK(Object pkOid) {
        if (cmd.top.isSingleIdentity) {
            try {
                pk[0] = /*CHFC*/pkOid.getClass()/*RIGHTPAR*/.getMethod("getKey", new Class[]{})
                        .invoke(pkOid, new Class[]{});
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
            }
        } else {
            FieldMetaData[] pkFields = cmd.pkFields;
            Object[] pkValues = new Object[pkFields.length];
            for (int i = 0; i < pkFields.length; i++) {
                try {
                    pkValues[i] = pkOid.getClass().getField(pkFields[i].getPkFieldName()).get(pkOid);
                } catch (Exception e) {
                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
                }
            }
            copyKeyFields(pkValues);
        }
    }

    public OID fillFromIDObject(Object id) {
        pk = new Object[cmd.pkFields.length];
        if (cmd.objectIdClass == null) {
            if (cmd.pkFields.length != 1) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Classes with application can only have a single pk " +
                        "field if a ApplicationId class is not specified");
            }
            pk[0] = id;
        } else {
            FieldMetaData[] pkFields = cmd.pkFields;
            Object[] pkValues = pk;
            for (int i = 0; i < pkFields.length; i++) {
                try {
                    pkValues[i] = id.getClass().getField(pkFields[i].getPkFieldName()).get(id);
                } catch (Exception e) {
                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
                }
            }
        }
        return this;
    }

    /**
     * Fill this OID from its toString. The classid will have already been
     * parsed out with index indicating the first character after the
     * separator.
     */
    public void fillFromIDString(String s, int i) {
        try {
            // There will always be only one column for datastore identity.
            // Composite pk is only supported for application identity which
            // will use the code in the objectid-class.
            switch (cmd.datastoreIdentityTypeCode) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    pk[0] = new Integer(FastParser.parseInt(s, i));
                    break;
                case MDStatics.SHORTW:
                case MDStatics.SHORT:
                    pk[0] = new Short((short)FastParser.parseInt(s, i));
                    break;
                case MDStatics.STRING:
                    pk[0] = s.substring(i);
                    break;
                case MDStatics.BOOLEANW:
                case MDStatics.BOOLEAN:
                    pk[0] = new Boolean(s.substring(i));
                    break;
                case MDStatics.BYTEW:
                case MDStatics.BYTE:
                    pk[0] = new Byte((byte)FastParser.parseInt(s, i));
                    break;
                case MDStatics.BIGDECIMAL:
                    pk[0] = new BigDecimal(s.substring(i));
                    break;
                case MDStatics.BIGINTEGER:
                    pk[0] = new BigInteger(s.substring(i));
                    break;
                case MDStatics.DOUBLEW:
                case MDStatics.DOUBLE:
                    pk[0] = new Double(s.substring(i));
                    break;
                case MDStatics.FLOATW:
                case MDStatics.FLOAT:
                    pk[0] = new Float(s.substring(i));
                    break;
                case MDStatics.LONGW:
                case MDStatics.LONG:
                    pk[0] = new Long(FastParser.parseLong(s, i));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "Unable to create id from the string '" + s +
                            "' type code " + cmd.datastoreIdentityTypeCode);
            }
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().invalidOperation("invalid OID String: '" + s + "'", e);
        }
    }

    public int hashCode() {
        if (pk == null) return super.hashCode();
        int hc = pk[0].hashCode() + cmd.top.index;
        for (int i = 1; i < pk.length; i++) hc = hc * 17 + pk[i].hashCode();
        return hc;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof GenericOID) {
            GenericOID o = (GenericOID)obj;
            if (o.cmd.top != cmd.top) return false;
            for (int i = pk.length - 1; i >= 0; i--) {
                if (pk[i] == null) {
                    if (o.pk[i] != null) return false;
                } else if (!pk[i].equals(o.pk[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return obj.equals(this);
        }
    }

    public String toString() {
        if (!resolved) {
            throw BindingSupportImpl.getInstance().internal(
                    "This oid is not resolved to its actual type");
        }
        return toStringImp();
    }

    /**
     * Get the toString of this OID even if it has not been resolved.
     */
    public String toStringImp() {
        if (pk == null) return "null pk";
        StringBuffer s = new StringBuffer();
        s.append(cmd.classId);
        s.append(MDStatics.OID_STRING_SEPERATOR);
        int len = pk.length;
        s.append(pk[0]);
        for (int i = 1; i < len; i++) {
            s.append(MDStatics.OID_STRING_SEPERATOR);
            s.append(pk[i]);
        }
        return s.toString();
    }

    public abstract String toSString();

    /**
     * Encode the 'primary key' of this OID as a String. This is used for
     * pretty printing OIDs in the workbench.
     */
    public String toPkString() {
        if (pk == null) return "null pk";
        StringBuffer s = new StringBuffer();
        int len = pk.length;
        s.append(pk[0]);
        for (int i = 1; i < len; i++) {
            s.append(',');
            s.append(' ');
            s.append(pk[i]);
        }
        return s.toString();
    }

    public int compareTo(Object o) {
        OID oo = (OID)o;
        int diff = cmd.top.index - oo.getBaseClassMetaData().index;
        if (diff != 0) return diff;
        if (oo.isNew()) {
            return +1; // we are always after new OIDs
        } else {
            GenericOID oid = (GenericOID)oo;
            for (int i = 0; i < pk.length; i++) {
                Object a = pk[i];
                Object b = oid.pk[i];
                if (a instanceof Comparable) {
                    diff = ((Comparable)a).compareTo(b);
                } else {
                    // yuck but I do not know any other way to do this
                    diff = a.toString().compareTo(b.toString());
                }
                if (diff != 0) return diff;
            }
            return 0;
        }
    }

    /**
     * Return the primary key stored in this OID as an long. This will only
     * be called for datastore identity classes.
     */
    public long getLongPrimaryKey() {
    	long result = 0;
    	try {
    		result = ((Number)pk[0]).longValue();
    	}
    	catch (ClassCastException e) {
    		System.out.println("ClassCastException: "  + pk[0]);
    		result = getHashPrimaryKey();
    	}
        return result;
    }

    private long getHashPrimaryKey() {
		long result = 0;
    	try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest(pk[0].toString().getBytes());
			//.SIZE only available as of JDK 1.5
			for (int i = 0; i < /*Long.SIZE / Byte.SIZE*/ 64/8; i++) {
				result |= ((long)hash[i] & 0xff) << (/*Byte.SIZE*/ 8 * i);
			}
    	}
	   	catch (NoSuchAlgorithmException e) {
	   		e.printStackTrace();
	   	}
	   	return result;
    }


    /**
     * Set the primary key stored in this OID as an long. This will only be
     * called for datastore identity classes.
     */
    public void setLongPrimaryKey(long pki) {
        switch (cmd.datastoreIdentityTypeCode) {
            case MDStatics.INTW:
            case MDStatics.INT:
                pk[0] = new Integer((int)pki);
                break;
            case MDStatics.SHORTW:
            case MDStatics.SHORT:
                pk[0] = new Short((short)pki);
                break;
            case MDStatics.BYTEW:
            case MDStatics.BYTE:
                pk[0] = new Byte((byte)pki);
                break;
            case MDStatics.LONGW:
            case MDStatics.LONG:
                pk[0] = new Long(pki);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Unhandled java type code " + cmd.datastoreIdentityTypeCode);
        }
    }

    public Object createObjectIdClassInstance() {
        ClassMetaData cmd = getAvailableClassMetaData();
        if (cmd.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
            throw BindingSupportImpl.getInstance().internal("not an app identity class: " +
                    cmd.qname);
        }
        Object o = null;

        if (cmd.top.isSingleIdentity) {
            FieldMetaData f = cmd.pkFields[0];
            try {
                Constructor cons = cmd.top.objectIdClass.getConstructor(new Class[] {/*CHFC*/Class.class/*RIGHTPAR*/, f.type});
                o = cons.newInstance(new Object[]{cmd.cls, pk[0]});
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        } else {
            try {
                o = cmd.top.objectIdClass.newInstance();
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
            FieldMetaData[] a = cmd.pkFields;
            for (int i = 0; i < a.length; i++) {
                try {
                    a[i].getObjectidClassField().set(o, pk[i]);
                } catch (Exception e) {
                    throw BindingSupportImpl.getInstance().internal(e.toString(), e);
                }
            }
        }
        return o;
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

    public int getAvailableClassId() {
        return getAvailableClassMetaData().classId;
    }

    public void setCmd(ClassMetaData cmd) {
        this.cmd = cmd;
    }

    public void writeExternal(OIDObjectOutput os) throws IOException {
        writeExternalImp(os);
    }

    public void writeExternal(ObjectOutput os) throws IOException {
        os.writeShort(getClassIndex());
        os.writeBoolean(resolved);
        writeExternalImp(os);
    }

    private void writeExternalImp(ObjectOutput os) throws IOException {
        if (cmd.pkFields == null) {
            writeObjectByTypeCode(pk[0], cmd.datastoreIdentityTypeCode, os);
        } else {
            int n = cmd.pkFields.length;
            for (int i = 0; i < n; i++) {
                writeObjectByTypeCode(pk[i], cmd.pkFields[i].typeCode, os);
            }
        }
    }

    public void readExternal(OIDObjectInput is) throws ClassNotFoundException,
            IOException {
        readExternalImp(is);
    }

    public void readExternal(ObjectInput is) throws ClassNotFoundException,
            IOException {
        cmd = ModelMetaData.getThreadMetaData().classes[is.readShort()];
        resolved = is.readBoolean();
        readExternalImp(is);
    }

    private void readExternalImp(ObjectInput is) throws IOException {
        // cmd and resolved will have already been set by the constructor so
        // no need to read them here
        if (cmd.pkFields == null) {
            pk = new Object[]{readObjectByTypeCode(cmd.datastoreIdentityTypeCode, is)};
        } else {
            int n = cmd.pkFields.length;
            pk = new Object[n];
            for (int i = 0; i < n; i++) {
                pk[i] = readObjectByTypeCode(cmd.pkFields[i].typeCode, is);
            }
        }
    }

    private void writeObjectByTypeCode(Object value, int typeCode, ObjectOutput os)
            throws IOException {
        if (value != null) {
            //write a non-null indicator
            os.writeBoolean(false);
            switch (typeCode) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    os.writeInt(((Integer)value).intValue());
                    break;
                case MDStatics.CHARW:
                case MDStatics.CHAR:
                    os.writeChar(((Character)value).charValue());
                    break;
                case MDStatics.SHORTW:
                case MDStatics.SHORT:
                    os.writeShort(((Short)value).intValue());
                    break;
                case MDStatics.STRING:
                    os.writeUTF((String)value);
                    break;
                case MDStatics.BOOLEANW:
                case MDStatics.BOOLEAN:
                    os.writeBoolean(((Boolean)value).booleanValue());
                    break;
                case MDStatics.BYTEW:
                case MDStatics.BYTE:
                    os.writeByte(((Byte)value).byteValue());
                    break;
                case MDStatics.DOUBLEW:
                case MDStatics.DOUBLE:
                    os.writeDouble(((Double)value).doubleValue());
                    break;
                case MDStatics.FLOATW:
                case MDStatics.FLOAT:
                    os.writeFloat(((Float)value).floatValue());
                    break;
                case MDStatics.LONGW:
                case MDStatics.LONG:
                    os.writeLong(((Long)value).longValue());
                    break;
                case MDStatics.LOCALE:
                    final Locale l = (Locale)value;
                    os.writeUTF(l.getLanguage());
                    os.writeUTF(l.getCountry());
                    os.writeUTF(l.getVariant());
                    break;
                case MDStatics.BIGDECIMAL:
                    os.writeUTF(value.toString());
                    break;
                case MDStatics.BIGINTEGER:
                    os.writeUTF(value.toString());
                    break;
                case MDStatics.DATE:
                    os.writeLong(((Date)value).getTime());
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "Unable to write type code " + typeCode);
            }
        } else {
            //write a null indicator
            os.writeBoolean(true);
        }
    }

    private Object readObjectByTypeCode(int typeCode, ObjectInput is)
            throws IOException {
        if (is.readBoolean()) {
            return null;
        } else {
            switch (typeCode) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    return new Integer(is.readInt());
                case MDStatics.CHARW:
                case MDStatics.CHAR:
                    return new Character(is.readChar());
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
                case MDStatics.LOCALE:
                    return new Locale(is.readUTF(), is.readUTF(),
                            is.readUTF());
                case MDStatics.BIGDECIMAL:
                    return new BigDecimal(is.readUTF());
                case MDStatics.BIGINTEGER:
                    return new BigInteger(is.readUTF());
                case MDStatics.DATE:
                    return new Date(is.readLong());
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "Unable to read java type code " + typeCode);
            }
        }
    }

}
