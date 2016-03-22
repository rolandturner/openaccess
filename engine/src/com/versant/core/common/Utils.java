
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

import com.versant.core.jdo.VersantPMInternal;
import com.versant.core.metadata.*;
import com.versant.core.util.classhelper.*;

import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility methods.
 */
public class Utils {

    public static final boolean JDK14;

    static {
        String v = System.getProperty("java.version");
        JDK14 = !v.startsWith("1.3"); // we can ignore 1.2 and older
    }

    /**
     * Safe toString method. If toString on o fails then the toString of the
     * exception is returned in angle brackets instead. This will also format
     * a byte[] nicely showing the first few bytes in hex.
     */
    public static String toString(Object o) {
        if (o == null) return "null";
        try {
            if (o instanceof byte[]) {
                byte[] a = (byte[])o;
                StringBuffer s = new StringBuffer();
                s.append("byte[]{");
                int i = 0;
                for (; i < a.length && i < 10; i++) {
                    if (i > 0) s.append(", ");
                    s.append("0x");
                    s.append(Integer.toHexString(a[i]));
                }
                if (i < a.length) s.append("...").append(a.length).append(" bytes");
                s.append('}');
                return s.toString();
            }
            return o.toString();
        } catch (Throwable e) {
            return "<toString failed: " + e + ">";
        }
    }

    /**
     * Write a UTF8 String that can be bigger than 64K. This code was cut and
     * pasted from ObjectOutputStream.
     *
     * @see #readLongUTF8(java.io.DataInput)
     */
    public static void writeLongUTF8(String s, DataOutput out)
            throws IOException {
        byte[] buf = new byte[1024];
        int len = s.length();
        int bufmax = buf.length - 3;
        out.writeInt(len);
        int pos = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c <= 0x007F && c != 0) {
                buf[pos++] = (byte)c;
            } else if (c > 0x07FF) {
                buf[pos + 2] = (byte)(0x80 | ((c >> 0) & 0x3F));
                buf[pos + 1] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[pos + 0] = (byte)(0xE0 | ((c >> 12) & 0x0F));
                pos += 3;
            } else {
                buf[pos + 1] = (byte)(0x80 | ((c >> 0) & 0x3F));
                buf[pos + 0] = (byte)(0xC0 | ((c >> 6) & 0x1F));
                pos += 2;
            }
            if (pos >= bufmax) {
                out.write(buf, 0, pos);
                pos = 0;
            }
        }
        if (pos > 0) out.write(buf, 0, pos);
    }

    /**
     * Read a UTF8 String previously written with writeLongUTF8.  This code
     * was cut and pasted from ObjectInputStream. This method will be slow
     * if in is not buffered.
     *
     * @see #writeLongUTF8(String, DataOutput)
     */
    public static String readLongUTF8(DataInput in) throws IOException {
        int len = in.readInt();
        if (len == 0) return "";
        char[] cbuf = new char[len];
        for (int i = 0; i < len; i++) {
            int b1, b2, b3;
            b1 = in.readUnsignedByte();
            switch (b1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:	  // 1 byte format: 0xxxxxxx
                    cbuf[i] = (char)b1;
                    break;

                case 12:
                case 13:  // 2 byte format: 110xxxxx 10xxxxxx
                    b2 = in.readUnsignedByte();
                    if ((b2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    cbuf[i] = (char)(((b1 & 0x1F) << 6) |
                            ((b2 & 0x3F) << 0));
                    break;

                case 14:  // 3 byte format: 1110xxxx 10xxxxxx 10xxxxxx
                    b2 = in.readUnsignedByte();
                    b3 = in.readUnsignedByte();
                    if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    cbuf[i] = (char)(((b1 & 0x0F) << 12) |
                            ((b2 & 0x3F) << 6) |
                            ((b3 & 0x3F) << 0));
                    break;

                default:  // 10xx xxxx, 1111 xxxx
                    throw new UTFDataFormatException();
            }
        }
        return new String(cbuf);
    }

    /**
     * Util method that is responsible to check if  a 'VersantObjectNotFoundException'
     * must be thrown. This is determined from the classmetadata of the oid.
     */
    public static void checkToThrowRowNotFound(OID oid, ModelMetaData jmd) {
        final ClassMetaData cmd = oid.getAvailableClassMetaData();
        if (cmd.returnNullForRowNotFound == ClassMetaData.NULL_NO_ROW_FALSE) {
            throw BindingSupportImpl.getInstance().objectNotFound(
                    "No row for " +
                    oid.getAvailableClassMetaData().storeClass + " " + oid.toSString());
        }
    }

    /**
     * @see #checkToThrowRowNotFound(com.versant.core.common.OID, com.versant.core.metadata.ModelMetaData)
     */
    public static void checkToThrowRowNotFound(OID refFrom, OID oid,
                                               ModelMetaData jmd) {
        final ClassMetaData refFromCmd = refFrom.getClassMetaData();
        final ClassMetaData cmd = oid.getAvailableClassMetaData();
        if (cmd.returnNullForRowNotFound == ClassMetaData.NULL_NO_ROW_FALSE
                || (cmd.returnNullForRowNotFound == ClassMetaData.NULL_NO_ROW_PASSON && jmd.returnNullForRowNotFound)) {
            throw BindingSupportImpl.getInstance().objectNotFound(
                    "No row for " +
                    oid.getAvailableClassMetaData().storeClass
                    + " " + oid.toSString() + " as referenced from " + refFromCmd.storeClass + " " + refFrom.toSString());
        }
    }

    /**
     * Writes the correct primitive or wrapper to the output stream
     */
    public static void writeSimple(int type, DataOutput os, Object toWrite)
            throws IOException {
        if (toWrite == null) {
            os.writeInt(0);
        } else {
            os.writeInt(1);
            switch (type) {
                case MDStatics.INTW:
                case MDStatics.INT:
                    os.writeInt(((Integer)toWrite).intValue());
                    break;
                case MDStatics.SHORTW:
                case MDStatics.SHORT:
                    os.writeShort(((Short)toWrite).shortValue());
                    break;
                case MDStatics.STRING:
                    os.writeUTF((String)toWrite);
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
                case MDStatics.BIGDECIMAL:
                    os.writeUTF(toWrite.toString());
                case MDStatics.LOCALE:
                    final Locale l = (Locale)toWrite;
                    os.writeUTF(l.getLanguage());
                    os.writeUTF(l.getCountry());
                    os.writeUTF(l.getVariant());
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("writeSimpleField for '" + MDStaticUtils.toSimpleName(
                            type) + "' is not supported");
            }
        }
    }

    public static Object[] getObjectsById(Object[] objects, int count,
                                          VersantPMInternal pm, FieldMetaData fmd, boolean isPC) {
        // Clone data into an object[] and convert OIDs to PC instances.
        // TODO should be possible to avoid the clone in future
        Object[] data;
        if (objects == null) {
            data = null;
        } else {
            data = new Object[count];
            if (isPC) {
                pm.getObjectsById(objects, count, data, fmd.stateFieldNo,
                        fmd.classMetaData.index);
            } else {
                System.arraycopy(objects, 0, data, 0, count);
            }
        }
        return data;
    }

    /**
     * Load resourceName as a Properties file using loader.
     */
    public static Properties loadProperties(String resourceName,
                                            ClassLoader loader) {
        Properties p = new Properties();
        InputStream in = null;
        try {
            try {
                in = loader.getResourceAsStream(resourceName);
                if (in == null) {
                    throw BindingSupportImpl.getInstance().runtime("Resource not found: " +
                            resourceName);
                }
                p.load(in);
            } finally {
                if (in != null) in.close();
            }
        } catch (IOException e) {
            throw BindingSupportImpl.getInstance().runtime("Error loading resource '" +
                    resourceName + "': " + e.getClass().getName() + ": " +
                    e.getMessage(), e);
        }
        return p;
    }

    /**
     * Is the database type Versant?
     */
    public static boolean isVersantDatabaseType(String dbt) {
        return "versant".equals(dbt) || "vds".equals(dbt);
    }

    /**
     * Is the URL a Versant URL?
     */
    public static boolean isVersantURL(String url) {
        return url != null && (url.startsWith("vds:")
                || url.startsWith("versant:"));
    }

    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isDataSource(String className, ClassLoader loader) {
    	if (className == null) {
    		return false;
    	}
        Class driverClass;
        try {
            driverClass = ClassHelper.get().classForName(className, false, loader);
        } catch (ClassNotFoundException cnfe) {
            try {
                driverClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        Class cls = /*CHFC*/java.sql.Driver.class/*RIGHTPAR*/;
        if (cls.isAssignableFrom(driverClass)) {
            return false;

        } else if ((javax.sql.DataSource.class).isAssignableFrom(driverClass)) {
            return true;

        } else {
            return false;
        }

    }

    /**
     * Get the url without the plain text password.
     * Assumption: URL has format mssqloracledb2://database@server;USER=XX;PASSWORD=YY;Options=ZZ;Seetings=A
     */
    public static String removePassword(String url) {
        String urlNoPassword = null;
        int pwstart = url.indexOf(";PASSWORD=");
        if (pwstart == -1)
            pwstart = url.indexOf(";password=");
        if (pwstart != -1) {
            urlNoPassword = url.substring(0, pwstart);
            int pwend = url.indexOf(';', pwstart+1);
            int end = url.length();
            if (pwend != -1 && (pwend != (end-1))) {
                urlNoPassword = urlNoPassword+url.substring(pwend, end);
            }
        }
        else
            urlNoPassword = url;
        return urlNoPassword;
    }
}
