
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

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.OIDObjectOutput;

import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility methods to help with Serialization.
 */
public final class SerUtils {

    public static Object readArrayField(FieldMetaData fmd,
            OIDObjectInput is)
            throws IOException, ClassNotFoundException {
        if (fmd.elementTypeMetaData != null) {
            return SerUtils.readOIDArray(is);
        } else {
            return SerUtils.readArrayField(fmd.componentTypeCode, is);
        }
    }

    public static void writeArrayField(FieldMetaData fmd, OIDObjectOutput os,
                                       Object data) throws IOException {
        if (fmd.elementTypeMetaData != null) {
                SerUtils.writeOIDArray((OID[]) data, os);
        } else {
            SerUtils.writeArrayField(fmd.componentTypeCode, os, data);
        }
    }

    public static final void writeOIDArray(OID[] oids, OIDObjectOutput out) throws IOException {
        if (oids == null) {
            out.writeInt(0);
        } else {
            int length = oids.length;
            out.writeInt(length);
            for (int i = 0; i < length; i++) {
                out.write(oids[i]);
            }
        }
    }

    public static final OID[] readOIDArray(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        int size = in.readInt();
        if (size > 0) {
            OID[] oids = new OID[size];
            for (int i = 0; i < size; i++) {
                oids[i] = in.readOID();
            }
            return oids;
        } else {
            return null;
        }
    }

    public static Object readArrayField(int type, OIDObjectInput is)
            throws IOException, ClassNotFoundException {
        switch (type) {
            case MDStatics.INTW:
                return SerUtils.readIntegerArray(is);
            case MDStatics.INT:
                return SerUtils.readIntArray(is);
            case MDStatics.SHORTW:
                return SerUtils.readShortWArray(is);
            case MDStatics.SHORT:
                return SerUtils.readShortArray(is);
            case MDStatics.CHARW:
                return SerUtils.readCharWArray(is);
            case MDStatics.CHAR:
                return SerUtils.readCharArray(is);
            case MDStatics.BOOLEANW:
                return SerUtils.readBooleanWArray(is);
            case MDStatics.BOOLEAN:
                return SerUtils.readBooleanArray(is);
            case MDStatics.BYTEW:
                return SerUtils.readByteWArray(is);
            case MDStatics.BYTE:
                return SerUtils.readByteArray(is);
            case MDStatics.DOUBLEW:
                return SerUtils.readDoubleWArray(is);
            case MDStatics.DOUBLE:
                return SerUtils.readDoubleArray(is);
            case MDStatics.FLOATW:
                return SerUtils.readFloatWArray(is);
            case MDStatics.FLOAT:
                return SerUtils.readFloatArray(is);
            case MDStatics.LONGW:
                return SerUtils.readLongWArray(is);
            case MDStatics.LONG:
                return SerUtils.readLongArray(is);
            case MDStatics.STRING:
                return SerUtils.readStringArray(is);
            case MDStatics.LOCALE:
                return SerUtils.readLocaleArray(is);
            case MDStatics.DATE:
                return SerUtils.readDateArray(is);
            case MDStatics.OID:
                return SerUtils.readOIDArray(is);
            default:
                return SerUtils.readObjectArray(is);
        }
    }

    public static void writeArrayField(int type, OIDObjectOutput os,
            Object toWrite) throws IOException {
        switch (type) {
            case MDStatics.INTW:
                SerUtils.writeIntegerArray((Integer[]) toWrite, os);
                break;
            case MDStatics.INT:
                SerUtils.writeIntArray((int[])toWrite, os);
                break;
            case MDStatics.SHORTW:
                SerUtils.writeShortWArray((Short[]) toWrite, os);
                break;
            case MDStatics.SHORT:
                SerUtils.writeShortArray((short[])toWrite, os);
                break;
            case MDStatics.CHARW:
                SerUtils.writeCharWArray((Character[]) toWrite, os);
                break;
            case MDStatics.CHAR:
                SerUtils.writeCharArray((char[])toWrite, os);
                break;
            case MDStatics.BOOLEANW:
                SerUtils.writeBooleanWArray((Boolean[]) toWrite, os);
                break;
            case MDStatics.BOOLEAN:
                SerUtils.writeBooleanArray((boolean[])toWrite, os);
                break;
            case MDStatics.BYTEW:
                SerUtils.writeByteWArray((Byte[]) toWrite, os);
                break;
            case MDStatics.BYTE:
                SerUtils.writeByteArray((byte[])toWrite, os);
                break;
            case MDStatics.DOUBLEW:
                SerUtils.writeDoubleWArray((Double[]) toWrite, os);
                break;
            case MDStatics.DOUBLE:
                SerUtils.writeDoubleArray((double[])toWrite, os);
                break;
            case MDStatics.FLOATW:
                SerUtils.writeFloatWArray((Float[]) toWrite, os);
                break;
            case MDStatics.FLOAT:
                SerUtils.writeFloatArray((float[])toWrite, os);
                break;
            case MDStatics.LONGW:
                SerUtils.writeLongWArray((Long[]) toWrite, os);
                break;
            case MDStatics.LONG:
                SerUtils.writeLongArray((long[])toWrite, os);
                break;
            case MDStatics.STRING:
                SerUtils.writeStringArray((String[])toWrite, os);
                break;
            case MDStatics.LOCALE:
                SerUtils.writeLocaleArray((Locale[])toWrite, os);
                break;
            case MDStatics.DATE:
                SerUtils.writeDateArray((Date[])toWrite, os);
                break;
            case MDStatics.OID:
                SerUtils.writeOIDArray((OID[])toWrite, os);
                break;
            default:
                writeObjectArray((Object[]) toWrite, os);
        }
    }

    public static void writeLongWArray(Long[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
        } else {
            out.writeBoolean(false);
            int n = array.length;
            out.writeInt(n);
            for (int i = 0; i < n; i++) {
                if (array[i] == null) {
                    out.writeByte(0);
                } else {
                    out.writeByte(0);
                    out.writeLong(array[i].longValue());
                }
            }
        }
    }

    public static Long[] readLongWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Long[] array = new Long[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Long(in.readLong());
        }
        return array;
    }

    public static void writeLongArray(long[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeLong(array[i]);
        }
    }

    public static long[] readLongArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final long[] array = new long[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readLong();
        }
        return array;
    }

    public static void writeBooleanWArray(Boolean[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(0);
                out.writeByte(array[i].booleanValue() == true ? 1 : 0);
            }
        }
    }

    public static Boolean[] readBooleanWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Boolean[] array = new Boolean[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Boolean(in.readByte() == 1 ? true : false);
        }
        return array;
    }

    public static void writeBooleanArray(boolean[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeBoolean(array[i]);
        }
    }

    public static boolean[] readBooleanArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final boolean[] array = new boolean[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readBoolean();
        }
        return array;
    }

    public static void writeIntegerArray(Integer[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeInt(array[i].intValue());
            }
        }
    }

    public static Integer[] readIntegerArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) {
                array[i] = new Integer(in.readInt());
            }
        }
        return array;
    }

    public static void writeIntArray(int[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeInt(array[i]);
        }
    }

    public static int[] readIntArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) {
            return null;
        }
        int n = in.readInt();
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readInt();
        }
        return array;
    }

    public static void writeByteWArray(Byte[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeByte(array[i].byteValue());
            }
        }
    }

    public static Byte[] readByteWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Byte[] array = new Byte[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Byte(in.readByte());
        }
        return array;
    }

    public static void writeByteArray(byte[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeByte(array[i]);
        }
    }

    public static byte[] readByteArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final byte[] array = new byte[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readByte();
        }
        return array;
    }

    public static void writeShortWArray(Short[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(0);
                out.writeShort(array[i].shortValue());
            }
        }
    }

    public static Short[] readShortWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Short[] array = new Short[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Short(in.readShort());
        }
        return array;
    }

    public static void writeShortArray(short[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeShort(array[i]);
        }
    }

    public static short[] readShortArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final short[] array = new short[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readShort();
        }
        return array;
    }

    public static void writeCharWArray(Character[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(0);
                out.writeChar(array[i].charValue());
            }
        }
    }

    public static Character[] readCharWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Character[] array = new Character[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Character(in.readChar());
        }
        return array;
    }

    public static void writeCharArray(char[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeChar(array[i]);
        }
    }

    public static char[] readCharArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final char[] array = new char[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readChar();
        }
        return array;
    }

    public static void writeFloatWArray(Float[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(0);
                out.writeFloat(array[i].floatValue());
            }
        }
    }

    public static Float[] readFloatWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Float[] array = new Float[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Float(in.readFloat());
        }
        return array;
    }

    public static void writeFloatArray(float[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeFloat(array[i]);
        }
    }

    public static float[] readFloatArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final float[] array = new float[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readFloat();
        }
        return array;
    }

    public static void writeDoubleWArray(Double[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(0);
                out.writeDouble(array[i].doubleValue());
            }
        }
    }

    public static Double[] readDoubleWArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Double[] array = new Double[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) array[i] = new Double(in.readDouble());
        }
        return array;
    }

    public static void writeDoubleArray(double[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeDouble(array[i]);
        }
    }

    public static double[] readDoubleArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final double[] array = new double[n];
        for (int i = 0; i < n; i++) {
            array[i] = in.readDouble();
        }
        return array;
    }

    public static void writeStringArray(String[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeUTF(array[i]);
            }
        }
    }

    public static String[] readStringArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final String[] array = new String[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) {
                array[i] = in.readUTF();
            }
        }
        return array;
    }

    public static void writeDateArray(Date[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeLong(array[i].getTime());
            }
        }
    }

    public static Date[] readDateArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Date[] array = new Date[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) {
                array[i] = new Date(in.readLong());
            }
        }
        return array;
    }

    public static void writeObjectArray(Object[] array, ObjectOutput out) throws IOException {
        out.writeObject(array);
    }

    public static Object[] readObjectArray(ObjectInput in) throws IOException,
            ClassNotFoundException {
        return (Object[])in.readObject();
    }

    public static void writeLocaleArray(Locale[] array, ObjectOutput out) throws IOException {
        if (array == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        final int n = array.length;
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                Locale l = (Locale)array[i];
                out.writeUTF(l.getLanguage());
                out.writeUTF(l.getCountry());
                out.writeUTF(l.getVariant());
            }
        }
    }

    public static Locale[] readLocaleArray(ObjectInput in) throws IOException {
        if (in.readBoolean()) return null;
        final int n = in.readInt();
        final Locale[] array = new Locale[n];
        for (int i = 0; i < n; i++) {
            if (in.readByte() == 1) {
                array[i] = new Locale(in.readUTF(), in.readUTF(), in.readUTF());
            }
        }
        return array;
    }

    public static void writeSimpleField(FieldMetaData fmd, ObjectOutput os,
            Object toWrite) throws IOException {
        writeSimpleField(fmd.typeCode, os, toWrite);
    }

    public static void writeSimpleField(int typeCode, ObjectOutput os, Object toWrite) throws IOException {
        if (toWrite == null) {
            os.writeByte(0);
            return;
        } else {
            os.writeByte(1);
            switch (typeCode) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    os.writeInt(((Integer)toWrite).intValue());
                    break;
                case MDStatics.CHARW:
                case MDStatics.CHAR:
                    os.writeChar(((Character)toWrite).charValue());
                    break;
                case MDStatics.SHORTW:
                case MDStatics.SHORT:
                    os.writeShort(((Short)toWrite).shortValue());
                    break;
                case MDStatics.STRING:
                    Utils.writeLongUTF8((String) toWrite, os);
                    break;
                case MDStatics.BOOLEANW:
                case MDStatics.BOOLEAN:
                    os.writeBoolean(((Boolean)toWrite).booleanValue());
                    break;
                case MDStatics.BYTEW:
                case MDStatics.BYTE:
                    os.writeByte(((Byte)toWrite).byteValue());
                    break;
                case MDStatics.DOUBLEW:
                case MDStatics.DOUBLE:
                    os.writeDouble(((Double)toWrite).doubleValue());
                    break;
                case MDStatics.FLOATW:
                case MDStatics.FLOAT:
                    os.writeFloat(((Float)toWrite).floatValue());
                    break;
                case MDStatics.LONGW:
                case MDStatics.LONG:
                    os.writeLong(((Long)toWrite).longValue());
                    break;
                case MDStatics.DATE:
                    os.writeLong(((Date)toWrite).getTime());
                    break;
                case MDStatics.LOCALE:
                    final Locale l = (Locale)toWrite;
                    os.writeUTF(l.getLanguage());
                    os.writeUTF(l.getCountry());
                    os.writeUTF(l.getVariant());
                    break;
                case MDStatics.BIGDECIMAL:
                    os.writeUTF(toWrite.toString());
                    break;
                case MDStatics.BIGINTEGER:
                    os.writeUTF(toWrite.toString());
                    break;
                default:
                    os.writeObject(toWrite);
                    break;
            }
        }
    }

    public static void writeObject(Object o, ObjectOutput dos) throws IOException {
        dos.writeObject(o);
    }

    public static Object readSimpleField(FieldMetaData fmd, ObjectInput is)
            throws IOException, ClassNotFoundException {
        return readSimpleField(fmd.typeCode, is);
    }

    public static Object readSimpleField(int typeCode, ObjectInput is)
            throws IOException, ClassNotFoundException {
        if (is.readByte() == 0) {
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
                    return Utils.readLongUTF8(is);
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
                case MDStatics.BIGDECIMAL:
                    return new BigDecimal(is.readUTF());
                case MDStatics.BIGINTEGER:
                    return new BigInteger(is.readUTF());
                default:
                    return is.readObject();
            }
        }
    }

    public static Object readObject(ObjectInput dis) throws IOException,
            ClassNotFoundException {
        return dis.readObject();
    }

    public static Object[] readCollectionArray(FieldMetaData fmd,
            OIDObjectInput in)
            throws IOException, ClassNotFoundException {
        if (in.readBoolean()) return null;
        if (fmd.elementTypeMetaData != null) {
            return readOIDObjectArray(in);
        } else {
            return readColObjectArray(fmd.elementTypeCode, in);
        }
    }

    public static void writeCollectionArray(FieldMetaData fmd, Object data,
            OIDObjectOutput out) throws IOException {
        if (data == null) {
            out.writeBoolean(true);
            return;
        } else {
            out.writeBoolean(false);
        }
        if (fmd.elementTypeMetaData != null) {
            writeOIDObjectArray((Object[])data, out);
        } else {
            writeColObjectArray((Object[])data, out, fmd.elementTypeCode);
        }
    }

    /**
     * This writes the Object[] of oids that is send as part of a collection read
     * from the store.
     */
    public static void writeOIDObjectArray(Object[] oids,
            OIDObjectOutput out) throws IOException {
        int n = oids.length;
        out.writeInt(n);
        for (int j = 0; j < n; j++) {
            out.write((OID)oids[j]);
        }
    }

    public static Object[] readOIDObjectArray(OIDObjectInput in) throws IOException, ClassNotFoundException {
        OID[] oids = new OID[in.readInt()];
        for (int j = 0; j < oids.length; j++) {
            oids[j] = in.readOID();
        }
        return oids;
    }

    private static void writeColObjectArray(Object[] ar, ObjectOutput out,
            int typeCode) throws IOException {
        int n = ar.length;
        out.writeInt(n);
        switch (typeCode) {
            case MDStatics.INTW:
            case MDStatics.INT:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeInt(((Integer)ar[i]).intValue());
                }
                break;
            case MDStatics.CHARW:
            case MDStatics.CHAR:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeChar(((Character)ar[i]).charValue());
                }
                break;
            case MDStatics.SHORTW:
            case MDStatics.SHORT:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeShort(((Short)ar[i]).shortValue());
                }
                break;
            case MDStatics.STRING:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeUTF((String)ar[i]);
                }
                break;
            case MDStatics.BOOLEANW:
            case MDStatics.BOOLEAN:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeBoolean(((Boolean)ar[i]).booleanValue());
                }
                break;
            case MDStatics.BYTEW:
            case MDStatics.BYTE:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeByte(((Byte)ar[i]).byteValue());
                }
                break;
            case MDStatics.DOUBLEW:
            case MDStatics.DOUBLE:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeDouble(((Double)ar[i]).doubleValue());
                }
                break;
            case MDStatics.FLOATW:
            case MDStatics.FLOAT:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeFloat(((Float)ar[i]).floatValue());
                }
                break;
            case MDStatics.LONGW:
            case MDStatics.LONG:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeLong(((Long)ar[i]).longValue());
                }
                break;
            case MDStatics.DATE:
                for (int i = 0; i < n; i++) {
                    if (ar[i] == null) {
                        out.writeByte(0);
                        continue;
                    } else {
                        out.writeByte(1);
                    }
                    out.writeLong(((Long)ar[i]).longValue());
                }
                break;
            case MDStatics.LOCALE:
                break;
            case MDStatics.BIGDECIMAL:
                break;
            case MDStatics.BIGINTEGER:
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "writeColObjectArray for " + typeCode + " is not supported");
        }
    }

    private static Object[] readColObjectArray(int typeCode,
            ObjectInput in) throws IOException {
        final int n = in.readInt();
        final Object[] ar = new Object[n];
        switch (typeCode) {
            case MDStatics.INTW:
            case MDStatics.INT:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Integer(in.readInt());
                }
                break;
            case MDStatics.CHARW:
            case MDStatics.CHAR:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Character(in.readChar());
                }
                break;
            case MDStatics.SHORTW:
            case MDStatics.SHORT:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Short(in.readShort());
                }
                break;
            case MDStatics.STRING:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = in.readUTF();
                }
                return ar;
            case MDStatics.BOOLEANW:
            case MDStatics.BOOLEAN:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Boolean(in.readBoolean());
                }
                break;
            case MDStatics.BYTEW:
            case MDStatics.BYTE:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Byte(in.readByte());
                }
                break;
            case MDStatics.DOUBLEW:
            case MDStatics.DOUBLE:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Double(in.readDouble());
                }
                break;
            case MDStatics.FLOATW:
            case MDStatics.FLOAT:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Float(in.readFloat());
                }
                break;
            case MDStatics.LONGW:
            case MDStatics.LONG:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Long(in.readLong());
                }
                break;
            case MDStatics.DATE:
                for (int i = 0; i < n; i++) {
                    if (in.readByte() == 0) continue;
                    ar[i] = new Date(in.readLong());
                }
                break;
            case MDStatics.LOCALE:
                break;
            case MDStatics.BIGDECIMAL:
                break;
            case MDStatics.BIGINTEGER:
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "readColObjectArray for " + typeCode + " is not supported");
        }
        return ar;
    }

    public static void writeMapEntries(FieldMetaData fmd, OIDObjectOutput out,
            MapEntries data) throws IOException {
        if (data == null) {
            out.writeByte(0);
            return;
        } else {
            out.writeByte(1);
        }
        if (fmd.keyTypeMetaData != null) {
            writeOIDObjectArray(data.keys, out);
        } else {
            writeColObjectArray(data.keys, out, fmd.keyTypeCode);
        }
        if (fmd.elementTypeMetaData != null) {
            writeOIDObjectArray(data.values, out);
        } else {
            writeColObjectArray(data.values, out, fmd.elementTypeCode);
        }
    }

    public static MapEntries readMapEntries(FieldMetaData fmd,
            OIDObjectInput in)
            throws IOException, ClassNotFoundException {
        if (in.readByte() == 0) {
            return null;
        }
        Object[] keys;
        Object[] values;
        if (fmd.keyTypeMetaData != null) {
            keys = readOIDObjectArray(in);
        } else {
            keys = readColObjectArray(fmd.keyTypeCode, in);
        }
        if (fmd.elementTypeMetaData != null) {
            values = readOIDObjectArray(in);
        } else {
            values = readColObjectArray(fmd.elementTypeCode, in);
        }
        return new MapEntries(keys, values);
    }

    private static final int COL_NULL = 0;
    private static final int COL_ARRAY = 1;
    private static final int COL_MAP_ENTRIES = 2;
    private static final int COL_DIFF_ORDERED = 3;
    private static final int COL_DIFF_UNORDERED = 4;
    private static final int COL_DIFF_MAP = 5;

    /**
     * Read the value of a collection or map field from the stream.
     * @see #writeCollectionOrMapField
     */
    public static Object readCollectionOrMapField(OIDObjectInput in,
            FieldMetaData fmd)
            throws IOException, ClassNotFoundException {
        CollectionDiff d;
        int code = in.readByte();
        switch (code) {
            case COL_NULL:
                return null;
            case COL_ARRAY:
                return SerUtils.readCollectionArray(fmd, in);
            case COL_MAP_ENTRIES:
                return SerUtils.readMapEntries(fmd, in);
            case COL_DIFF_UNORDERED:
                d = new UnorderedCollectionDiff(fmd);
                d.readExternal(in);
                return d;
            case COL_DIFF_ORDERED:
                d = new OrderedCollectionDiff(fmd);
                d.readExternal(in);
                return d;
            case COL_DIFF_MAP:
                d = new MapDiff(fmd);
                d.readExternal(in);
                return d;
        }
        throw BindingSupportImpl.getInstance().internal(
                "Unknown collection field code: " + code);
    }

    /**
     * Write the value of a collection or map field to the stream. This
     * can handle raw collection or map data as well as diffs.
     * @see #readCollectionOrMapField
     */
    public static void writeCollectionOrMapField(OIDObjectOutput out,
            FieldMetaData fmd, Object data) throws IOException {
        if (data instanceof CollectionDiff) {
            int code;
            if (data instanceof UnorderedCollectionDiff) {
                code = COL_DIFF_UNORDERED;
            } else if (data instanceof OrderedCollectionDiff) {
                code = COL_DIFF_ORDERED;
            } else if (data instanceof MapDiff) {
                code = COL_DIFF_MAP;
            } else {
                throw BindingSupportImpl.getInstance().internal(
                        "Unknown CollectionDiff class: " +
                        data.getClass().getName());
            }
            out.writeByte(code);
            ((CollectionDiff)data).writeExternal(out);
        } else if (data == null) {
            out.writeByte(COL_NULL);
        } else if (data instanceof MapEntries) {
            out.writeByte(COL_MAP_ENTRIES);
            SerUtils.writeMapEntries(fmd, out, (MapEntries)data);
        } else {
            out.writeByte(COL_ARRAY);
            SerUtils.writeCollectionArray(fmd, data, out);
        }
    }

}
