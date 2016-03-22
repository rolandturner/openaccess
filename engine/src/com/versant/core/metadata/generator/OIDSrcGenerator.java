
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
package com.versant.core.metadata.generator;

import com.versant.core.common.*;
import com.versant.core.util.FastParser;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.metadata.*;
import com.versant.core.metadata.*;
import com.versant.core.compiler.ClassSpec;
//import com.versant.core.jdbc.metadata.JdbcField;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

/**
 * Generates java source for an OID class for a PC class.
 */
public abstract class OIDSrcGenerator {

    protected final ModelMetaData jmd;
    protected ClassMetaData cmd;
    protected String className;
    protected ClassSpec spec;
    protected ClassMetaData currentCMD = null;

    protected final String CLASS_INDEX = "classIndex";
    protected final String RESOLVED = "resolved";

    protected HashMap typeToResultSetGetField;
    protected HashMap typeToPreparedStatementSetField;

    protected HashMap primitiveToWrapperTypes;
    protected HashMap primitiveTypesToValue;
    protected HashMap wrapperTypesToValue;
    protected HashMap wrapperTypesToPrimitive;

    protected String stateClassName;

    public static final String JDBC_CONVERTER_FIELD_PREFIX = "jdbcConverter_";

    protected HashMap primClassToSerReadMethod;
    protected HashMap primClassToSerWriteMethod;

    public OIDSrcGenerator(ModelMetaData jmd) {
        this.jmd = jmd;

        wrapperTypesToValue = new HashMap(8);
        wrapperTypesToValue.put(int.class, "intValue");
        wrapperTypesToValue.put(byte.class, "byteValue");
        wrapperTypesToValue.put(char.class, "charValue");
        wrapperTypesToValue.put(short.class, "shortValue");
        wrapperTypesToValue.put(float.class, "floatValue");
        wrapperTypesToValue.put(double.class, "doubleValue");
        wrapperTypesToValue.put(long.class, "longValue");
        wrapperTypesToValue.put(boolean.class, "booleanValue");


        primitiveToWrapperTypes = new HashMap(8);
        primitiveToWrapperTypes.put(int.class, "Integer");
        primitiveToWrapperTypes.put(byte.class, "Byte");
        primitiveToWrapperTypes.put(char.class, "Character");
        primitiveToWrapperTypes.put(short.class, "Short");
        primitiveToWrapperTypes.put(float.class, "Float");
        primitiveToWrapperTypes.put(double.class, "Double");
        primitiveToWrapperTypes.put(long.class, "Long");
        primitiveToWrapperTypes.put(boolean.class, "Boolean");



        wrapperTypesToPrimitive = new HashMap(8);
        wrapperTypesToPrimitive.put(Integer.class, int.class);
        wrapperTypesToPrimitive.put(Byte.class, byte.class);
        wrapperTypesToPrimitive.put(Character.class, char.class);
        wrapperTypesToPrimitive.put(Short.class, short.class);
        wrapperTypesToPrimitive.put(Float.class, float.class);
        wrapperTypesToPrimitive.put(Double.class, double.class);
        wrapperTypesToPrimitive.put(Long.class, long.class);
        wrapperTypesToPrimitive.put(Boolean.class, boolean.class);

        primitiveTypesToValue = new HashMap(8);
        primitiveTypesToValue.put(int.class, "intValue");
        primitiveTypesToValue.put(byte.class, "byteValue");
        primitiveTypesToValue.put(char.class, "charValue");
        primitiveTypesToValue.put(short.class, "shortValue");
        primitiveTypesToValue.put(float.class, "floatValue");
        primitiveTypesToValue.put(double.class, "doubleValue");
        primitiveTypesToValue.put(long.class, "longValue");
        primitiveTypesToValue.put(boolean.class, "booleanValue");


        typeToResultSetGetField = new HashMap();
        typeToResultSetGetField.put(int.class, "getInt");
        typeToResultSetGetField.put(byte.class, "getByte");
        typeToResultSetGetField.put(short.class, "getShort");
        typeToResultSetGetField.put(float.class, "getFloat");
        typeToResultSetGetField.put(double.class, "getDouble");
        typeToResultSetGetField.put(long.class, "getLong");
        typeToResultSetGetField.put(boolean.class, "getBoolean");
        typeToResultSetGetField.put(String.class, "getString");
        typeToResultSetGetField.put(byte[].class, "getBytes");
        typeToResultSetGetField.put(java.math.BigDecimal.class, "getBigDecimal");  // has a scale
        typeToResultSetGetField.put(java.util.Date.class, "getDate");
        typeToResultSetGetField.put(java.sql.Time.class, "getTime");
        typeToResultSetGetField.put(java.sql.Timestamp.class, "getTimestamp");
        typeToResultSetGetField.put(java.io.InputStream.class, "getAsciiStream");
        typeToResultSetGetField.put(java.io.InputStream.class, "getBinaryStream");


        typeToPreparedStatementSetField = new HashMap();
        typeToPreparedStatementSetField.put(int.class, "setInt");
        typeToPreparedStatementSetField.put(byte.class, "setByte");
        typeToPreparedStatementSetField.put(short.class, "setShort");
        typeToPreparedStatementSetField.put(float.class, "setFloat");
        typeToPreparedStatementSetField.put(double.class, "setDouble");
        typeToPreparedStatementSetField.put(long.class, "setLong");
        typeToPreparedStatementSetField.put(boolean.class, "setBoolean");
        typeToPreparedStatementSetField.put(String.class, "setString");
        typeToPreparedStatementSetField.put(Object.class, "setObject");
        typeToPreparedStatementSetField.put(byte[].class, "setBytes");
        typeToPreparedStatementSetField.put(java.math.BigDecimal.class, "setBigDecimal");
        typeToPreparedStatementSetField.put(java.util.Date.class, "setDate");
        typeToPreparedStatementSetField.put(java.sql.Time.class, "setTime");
        typeToPreparedStatementSetField.put(java.sql.Timestamp.class, "setTimestamp");
        typeToPreparedStatementSetField.put(java.io.InputStream.class, "setAsciiStream");
        typeToPreparedStatementSetField.put(java.io.InputStream.class, "setBinaryStream");

        primClassToSerReadMethod = new HashMap(8);
        primClassToSerReadMethod.put(int.class, "readInt");
        primClassToSerReadMethod.put(byte.class, "readByte");
        primClassToSerReadMethod.put(char.class, "readChar");
        primClassToSerReadMethod.put(short.class, "readShort");
        primClassToSerReadMethod.put(float.class, "readFloat");
        primClassToSerReadMethod.put(double.class, "readDouble");
        primClassToSerReadMethod.put(long.class, "readLong");
        primClassToSerReadMethod.put(boolean.class, "readBoolean");

        primClassToSerWriteMethod = new HashMap(8);
        primClassToSerWriteMethod.put(int.class, "writeInt");
        primClassToSerWriteMethod.put(byte.class, "writeByte");
        primClassToSerWriteMethod.put(char.class, "writeChar");
        primClassToSerWriteMethod.put(short.class, "writeShort");
        primClassToSerWriteMethod.put(float.class, "writeFloat");
        primClassToSerWriteMethod.put(double.class, "writeDouble");
        primClassToSerWriteMethod.put(long.class, "writeLong");
        primClassToSerWriteMethod.put(boolean.class, "writeBoolean");
    }

    public ClassSpec generateOID(ClassMetaData cmd) {
        this.cmd = cmd;
        this.className = cmd.oidClassName;
        this.stateClassName = cmd.stateClassName;
        spec = new ClassSpec(null, className, null);

        spec.addImportsForJavaLang();
        spec.addImport(OID.class.getName());
        spec.addImport(State.class.getName());
        spec.addImport(ClassMetaData.class.getName());
        spec.addImport(ModelMetaData.class.getName());
        spec.addImport(ObjectOutput.class.getName());
        spec.addImport(ObjectInput.class.getName());
        spec.addImport(DataInputStream.class.getName());
        spec.addImport(DataOutputStream.class.getName());
        spec.addImport(IOException.class.getName());
        spec.addImport(BindingSupportImpl.class.getName());
        spec.addImport(FastParser.class.getName());
        spec.addImport(SerUtils.class.getName());
        spec.addImport(OIDObjectOutput.class.getName());
        spec.addImport(OIDObjectInput.class.getName());
        spec.addImport(Externalizable.class.getName());
//        spec.addImport(JdbcField.class.getName());

        spec.addInterface("OID");
        spec.addInterface("Externalizable");

        currentCMD = cmd.top;

        addFields();
        addInitStatics();
        addDefaultConstructor();        //cool
        addConstructor();               //cool
        addCopyKeyFieldsObjects();
        addEqualsObject();
        addHashCode();
        addReadExternal();              //cool
        addWriteExternal();             //cool
        addReadExternalFast();              //cool
        addWriteExternalFast();         //cool
        addCopyKeyFieldsFromState();
        addCopyKeyFieldsUpdate();
        addIsResolved();                //cool
        addResolve();                   //cool
        addGetClassIndex();             //cool
        addGetAvailableClassMetaData(); //cool
        addGetBaseClassMetaData();      //cool
        addGetClassMetaData();          //cool
        addGetIdentityType();           //cool
        addGetCopy();                   //cool
        addfillFromPK();
        addToPKString();
        addToSString();
        addToString();
        addToStringImp();
        addFillFromIDString2();
        addGetCompareClassIndex();
        addCompareTo();
        addVersion();
        addGetLongPrimaryKey();
        addSetLongPrimaryKey();
        addCreateObjectIdClassInstance();
        addIsNewEtc();
        addGetAvailableClassId();
        addFillFromIDObject();

        return spec;
    }

    protected void addIsNewEtc() {
        spec.addMethod(
                "public boolean isNew() {\n" +
                "    return false;\n" +
                "}");
        spec.addMethod(
                "public OID getMappedOID() {\n" +
                "    return this;\n" +
                "}");
        spec.addMethod(
                "public OID getRealOID() {\n" +
                "    return this;\n" +
                "}");
        spec.addMethod(
                "public OID getAvailableOID() {\n" +
                "    return this;\n" +
                "}");
    }

    protected void addCreateObjectIdClassInstance() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic Object createObjectIdClassInstance() {\n");
        buf.append("\t\treturn null;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addGetLongPrimaryKey();

    protected abstract void addSetLongPrimaryKey();

    protected void addVersion() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String getVersion() {\n");
        buf.append("\t\treturn \""+ Debug.VERSION +"\";\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addfillFromPK();

    protected void addGetBaseClassMetaData() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic ClassMetaData getBaseClassMetaData() {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\treturn top;\n");
        } else {
            buf.append("\t\treturn cmd;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetAvailableClassMetaData() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic ClassMetaData getAvailableClassMetaData() {\n");
        buf.append("\t\treturn cmd;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetAvailableClassId() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int getAvailableClassId() {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\treturn cmd.classId;\n");
        } else {
            buf.append("\t\treturn " + cmd.classId + ";\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetClassMetaData() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic ClassMetaData getClassMetaData() {\n");
        if (cmd.isInHierarchy()) {
            buf.append(
                    "\t\tif (!resolved) {\n" +
                    "\t\t\tthrow BindingSupportImpl.getInstance().internal(\n" +
                    "\t\t\t\t\"Called 'getClassMetaData()' on unresolved oid\");\n" +
                    "\t\t}\n");
        }
        buf.append("\t\treturn cmd;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetClassIndex() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int getClassIndex() {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\treturn cmd.index;\n");
        } else {
            buf.append("\t\treturn "+ cmd.index);
            buf.append(";\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetCompareClassIndex() {
        StringBuffer buf = new StringBuffer();
        /*
        public int getCompareClassIndex() {
           return 93;
        }
        */
        buf.append("\n\tpublic int getCompareClassIndex() {\n");
        buf.append("\t\treturn "+ getIndex() +";\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addCompareTo();

    protected int getIndex() {
        if (cmd.isInHierarchy()) {
            return cmd.pcHierarchy[0].index;
        } else {
            return cmd.index;
        }
    }


    protected void addResolve() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void resolve(State state) {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\tif (!resolved) {\n");
            buf.append("\t\t\tcmd = state.getClassMetaData();\n");
            buf.append("\t\t\tresolved = true;\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addIsResolved() {
        StringBuffer buf = new StringBuffer();
        /*
        public boolean isResolved() {
        return resolved;
        }
        */
        buf.append("\n\tpublic boolean isResolved() {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\treturn resolved;\n");
        } else {
            buf.append("\t\treturn true;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addToPKString();

    protected abstract void addToSString();

    protected ClassMetaData[] getAllSubClasses(ClassMetaData topCMD) {
        ArrayList list = new ArrayList();
        ClassMetaData[] cmds = jmd.classes;
        for (int i = 0; i < cmds.length; i++) {
            ClassMetaData cmd = cmds[i];
            if (cmd.top.index == topCMD.index) {
                list.add(cmd);
            }
        }
        ClassMetaData[] allCMDs = new ClassMetaData[list.size()];
        list.toArray(allCMDs);
        return allCMDs;
    }

    protected void addFillFromIDString2() {
        StringBuffer buf = new StringBuffer();
        /*
        public void fillFromIDString(String idString, int index) {
            _0 = FastParser.parseInt(idString, index);
        }
        */
        buf.append("\n\tpublic void fillFromIDString(String idString, int index) {\n");

        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            buf.append("\t\tthrow new javax.jdo.JDOFatalInternalException(\"fillFromIDString() called on applicationId class.\");");
        } else if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            switch (cmd.datastoreIdentityTypeCode) {
                case MDStatics.INT:
                    buf.append("\t\t_0 = FastParser.parseInt(idString, index);");
                    break;
                case MDStatics.LONG:
                    buf.append("\t\t_0 = FastParser.parseLong(idString, index);");
                    break;
                default:
                    throw BindingSupportImpl.getInstance().runtime(
                            "Unhandled java type code " + cmd.datastoreIdentityTypeCode);
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToStringImp() {
        StringBuffer buf = new StringBuffer();
        /*
        public String toStringImp() {
            if (!resolved) return "null pk";
            else return toString();
        }
        */
        buf.append("\n\tpublic String toStringImp() {\n");
        if (!currentCMD.isInHierarchy()) {
           buf.append("\t\treturn toString();\n");
        } else {
            /*
            if (!resolved) return "null pk";
            else return toString();
            */
            buf.append("\t\tif (!resolved) return \"null pk\";\n");
            buf.append("\t\telse return this.toString();\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addToString();

    protected void addGetIdentityType() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int getIdentityType() {\n");
        buf.append("\t\treturn "+ cmd.identityType + ";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addGetCopy();

    protected abstract void addCopyKeyFieldsUpdate();

    protected abstract void addCopyKeyFieldsFromState();

    protected void addReadExternal() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void readExternal(ObjectInput is) throws ClassNotFoundException, IOException {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\tcmd = top.jmd.classes[is.readShort()];\n");
            buf.append("\t\tresolved = is.readBoolean();\n");
        }
        addReadExternalImp(buf);
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addWriteExternal() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void writeExternal(ObjectOutput os) throws IOException {\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\tos.writeShort(cmd.index);\n");
            buf.append("\t\tos.writeBoolean(resolved);\n");
        }
        addWriteExternalImp(buf);
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addReadExternalFast() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void readExternal(OIDObjectInput is) throws ClassNotFoundException, IOException {\n");
        addReadExternalImp(buf);
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addWriteExternalFast() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void writeExternal(OIDObjectOutput os) throws IOException {\n");
        addWriteExternalImp(buf);
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addReadExternalImp(StringBuffer buf) {
        Class bigIntegerType = java.math.BigInteger.class;
        boolean isBigInteger = false;   // 2
        FieldMetaData[] pkc = currentCMD.pkFields;
        if (pkc != null) {
            for (int i = 0; i < pkc.length; i++) {
                if (pkc[i].type.equals(bigIntegerType)) {
                    isBigInteger = true;
                }
            }
        }
        if (isBigInteger) {
            buf.append("\t\tbyte bytes[] = null;\n");
        }
        int n = pkc == null ? 1 : pkc.length;
        for (int i = 0; i < n; i++) {
            Class type = pkc == null ? currentCMD.datastoreIdentityType : pkc[i].type;
            if (type.isPrimitive()) {// do primative types
                buf.append("\t\t_" + i + " = is." + primClassToSerReadMethod.get(type) + "();\n");
            } else if (type.equals(String.class)) {
                buf.append("\t\t_" + i + " = com.versant.core.common.Utils.readLongUTF8(is);\n");
            } else if (type.equals(java.util.Locale.class)) {
                buf.append("\t\t_" + i + " = new java.util.Locale(is.readUTF(), is.readUTF(), is.readUTF());\n");
            } else if (type.equals(java.math.BigDecimal.class)) {
                buf.append("\t\t_" + i + " = new java.math.BigDecimal(is.readUTF());\n");
            } else if (type.equals(java.math.BigInteger.class)) {
                buf.append("\t\tbytes = new byte[is.readInt()];\n");
                buf.append("\t\tis.readFully(bytes);\n");
                buf.append("\t\t_" + i + " = new java.math.BigInteger(bytes);\n");
            } else if (type.equals(java.util.Date.class)) {
                buf.append("\t\t_" + i + " = new java.util.Date(is.readLong());\n");
            } else if (type.equals(java.net.URL.class)) {
                buf.append("\t\t_" + i + " = new java.net.URL(is.readUTF());\n");
            } else if (type.equals(File.class)) {
                buf.append("\t\t_" + i + " = new java.io.File(is.readUTF());\n");
            } else if (type.equals(java.sql.Timestamp.class)) {
                buf.append("\t\t_" + i + " = new java.sql.Timestamp(is.readLong());\n");
            } else if (wrapperTypesToPrimitive.containsKey(type)) {  // wrapper
                buf.append("\t\t_" + i + " = new " + type.getName() + "(is." + primClassToSerReadMethod.get(wrapperTypesToPrimitive.get(type)) + "());\n");
            } else {
                buf.append("\t\t_" + i + " = (" + type.getName() + ")SerUtils.readObject(is);\n");
            }
        }
    }

    protected void addWriteExternalImp(StringBuffer buf) {
        Class bigIntegerType = java.math.BigInteger.class;
        boolean isBigInteger = false;   // 2
        FieldMetaData[] pkc = currentCMD.pkFields;
        if (pkc != null) {
            for (int i = 0; i < pkc.length; i++) {
                if (pkc[i].type.equals(bigIntegerType)) {
                    isBigInteger = true;
                }
            }
        }
        if (isBigInteger) {
            buf.append("\t\tbyte bytes[] = null;\n");
        }
        int n = pkc == null ? 1 : pkc.length;
        for (int i = 0; i < n; i++) {
            Class type = pkc == null ? currentCMD.datastoreIdentityType : pkc[i].type;
            if (type.isPrimitive()) {// do primative types
                buf.append("\t\tos." + primClassToSerWriteMethod.get(type) + "(_" + i + ");\n");
            } else if (type.equals(String.class)) {
                buf.append("\t\tcom.versant.core.common.Utils.writeLongUTF8(_" + i + ", os);\n");
            } else if (type.equals(java.util.Locale.class)) {
                buf.append("\t\tos.writeUTF(_" + i + ".getLanguage());\n");
                buf.append("\t\tos.writeUTF(_" + i + ".getCountry());\n");
                buf.append("\t\tos.writeUTF(_" + i + ".getVariant());\n");
            } else if (type.equals(java.math.BigDecimal.class)) {
                buf.append("\t\tos.writeUTF(_" + i + ".toString());\n");
            } else if (type.equals(java.math.BigInteger.class)) {
                buf.append("\t\tbytes = _" + i + ".toByteArray();\n");
                buf.append("\t\tos.writeInt(bytes.length);\n");
                buf.append("\t\tos.write(bytes);\n");
            } else if (type.equals(java.util.Date.class)) {
                buf.append("\t\tos.writeLong(_" + i + ".getTime());\n");
            } else if (type.equals(java.net.URL.class)) {
                buf.append("\t\tos.writeUTF(_" + i + ".toString());\n");
            } else if (type.equals(File.class)) {
                buf.append("\t\tos.writeUTF(_" + i + ".toString());\n");
            } else if (type.equals(java.sql.Timestamp.class)) {
                buf.append("\t\tos.writeLong(_" + i + ".getTime());\n");
            } else if (wrapperTypesToPrimitive.containsKey(type)) {  // wrapper
                buf.append("\t\tos." +
                        primClassToSerWriteMethod.get(wrapperTypesToPrimitive.get(type)) +
                        "(_" + i + "." + wrapperTypesToValue.get(wrapperTypesToPrimitive.get(type)) + "());\n");
            } else {
                buf.append("\t\tSerUtils.writeObject(_" + i + ", os);\n");
            }
        }
    }

    protected abstract void addEqualsObject();

    protected abstract void addHashCode();

    /**
     * Add's a default constructor that calls the OID super class
     * this constructor is needed so that we can get an instance from the class with class.newInstance()
     */
    protected void addDefaultConstructor() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic ");
        buf.append(className);
        buf.append("(){}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * Add's a constructor that calls the OID super class
     * this constructor is needed only when there is a class hier involved and we keep a
     * classIndex var in the class.
     */
    protected void addConstructor() {
        StringBuffer buf = new StringBuffer();
        if (cmd.isInHierarchy()) {
            buf.append("\n\tpublic ");
            buf.append(className);
            buf.append("(ClassMetaData cmd, boolean resolved){\n");
            buf.append("\t\tthis.cmd = cmd;\n");
            buf.append("\t\tthis.resolved = resolved;\n\t}\n");
        }
        spec.addMethod(buf.toString());
    }

    protected ClassMetaData tempClassMetaData;

    protected ClassMetaData getTopPCSuperClassMetaData() {
        if (cmd.pcSuperMetaData == null) {
            return cmd;
        } else {
            setTopPCSuperClassMetaData(cmd);
        }
        return tempClassMetaData;
    }

    protected void setTopPCSuperClassMetaData(ClassMetaData currentCMD) {
        if (currentCMD.pcSuperMetaData == null) {
            tempClassMetaData = currentCMD;
            return;
        } else {
            setTopPCSuperClassMetaData(currentCMD.pcSuperMetaData);
        }
    }

    protected ClassMetaData getCurrentCMD() {
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            return getTopPCSuperClassMetaData();
        } else if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            return cmd;
        } else {
            throw BindingSupportImpl.getInstance().unsupported("");
        }
    }

    /**
     * Add all PK field(s).
     */
    protected void addFields() {
        if (cmd.isInHierarchy()) {
            spec.addField("public static ClassMetaData top");
            spec.addField("public ClassMetaData cmd");
            spec.addField("public boolean " + RESOLVED);
        } else {
            spec.addField("public static ClassMetaData cmd");
        }
    }

    /**
     * Add method to fill in values of static fields from ModelMetaData.
     * This is invoked once the class has been loaded.
     */
    protected void addInitStatics() {
        StringBuffer buf = new StringBuffer();
        buf.append("\tpublic static boolean initStatics(ModelMetaData jmd) {\n");
        addInitStaticsBody(buf);
        buf.append("\t\treturn true;\n");
        buf.append("\t}");
        spec.addMethod(buf.toString());
    }

    protected void addInitStaticsBody(StringBuffer buf) {
        if (cmd.isInHierarchy()) {
            buf.append("\t\tif (top != null) return false;\n");
            buf.append("\t\ttop = jmd.classes[" + cmd.top.index + "];\n");
        } else {
            buf.append("\t\tif (cmd != null) return false;\n");
            buf.append("\t\tcmd = jmd.classes[" + cmd.index + "];\n");
        }
    }

    protected abstract void addCopyKeyFieldsObjects();

    protected String getFieldName(int num) {
        return "_" + num;
    }

    protected void addFillFromIDObject() {
//    public OID fillFromIDObject(Object id) {
//        pk = new Object[cmd.pkFields.length];
//        if (cmd.objectIdClass == null) {
//            if (cmd.pkFields.length != 1) {
//                throw BindingSupportImpl.getInstance().invalidOperation("Classes with application can only have a single pk " +
//                        "field if a ApplicationId class is not specified");
//            }
//            pk[0] = id;
//        } else {
//            FieldMetaData[] pkFields = cmd.pkFields;
//            Object[] pkValues = pk;
//            for (int i = 0; i < pkFields.length; i++) {
//                try {
//                    pkValues[i] = pk.getClass().getField(pkFields[i].getPkFieldName()).get(pk);
//                } catch (Exception e) {
//                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
//                }
//            }
//        }
//        return this;
//    }
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic OID fillFromIDObject(Object id) {\n");
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            if (currentCMD.objectIdClass == null) {
                if (currentCMD.pkFields.length != 1) {
                    throw BindingSupportImpl.getInstance().invalidOperation("Class "+
                            currentCMD.cls.getName()
                            +" with application can only have a single pk " +
                            "field if a ApplicationId class is not specified");
                }
                Class type = currentCMD.pkFields[0].type;
                if (type.isPrimitive() && !type.isArray()) {// do primative types
                    buf.append("\t\t_0 = (("+ primitiveToWrapperTypes.get(type) +
                            ")id)."+ wrapperTypesToValue.get(type) +"();\n");
                } else {  // wrapper
                    buf.append("\t\t_0 = (" + type.getName() + ")id;\n");
                }
            } else {
                // do other stuff here
            }
        }
        buf.append("\t\treturn this;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }




}


