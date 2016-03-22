
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

import com.versant.core.metadata.generator.StateSrcGenerator;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.NotImplementedException;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.vds.metadata.VdsField;
import com.versant.core.vds.metadata.VdsTemplateField;
import com.versant.core.vds.metadata.VdsExternalizedField;
import com.versant.core.vds.metadata.VdsArrayField;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.UserSchemaField;
import com.versant.odbms.DatastoreManager;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Adds VDS specific methods to State.
 */
public class VdsStateGenerator extends StateSrcGenerator {

    protected HashMap typeToVdsWrite;
    protected HashMap typeToVdsRead;

    protected static final String VDS_FIELD = "VdsField";
    protected static final String USER_SCHEMA_FIELD = "UserSchemaField";
    protected static final String VDS_UNTYPED_OID = "VdsUntypedOID";

    public VdsStateGenerator() {
        super();

        typeToVdsWrite = new HashMap();
        // joy
//        typeToVdsWrite.put(Type.INT, "writeBCD");
//        typeToVdsWrite.put(new ArrayType(Type.INT, 1), "writeBCDArray");
        typeToVdsWrite.put(boolean.class, "writeBoolean");
        typeToVdsWrite.put(boolean[].class, "writeBooleanArray");
        typeToVdsWrite.put(byte.class, "writeByte");
        typeToVdsWrite.put(byte[].class, "writeByteArray");
        typeToVdsWrite.put(char.class, "writeChar");
        typeToVdsWrite.put(char[].class, "writeCharArray");
//        typeToVdsWrite.put(Type.INT, "writeDate");
//        typeToVdsWrite.put(new ArrayType(Type.INT, 1), "writeDateArray");
        typeToVdsWrite.put(double.class, "writeDouble");
        typeToVdsWrite.put(double[].class, "writeDoubleArray");
//        typeToVdsWrite.put(Type.INT, "writeDynamicType");
        typeToVdsWrite.put(float.class, "writeFloat");
        typeToVdsWrite.put(float[].class, "writeFloatArray");
        typeToVdsWrite.put(int.class, "writeInt");
        typeToVdsWrite.put(int[].class, "writeIntArray");
//        typeToVdsWrite.put(Type.INT, "writeInterval");
//        typeToVdsWrite.put(new ArrayType(Type.INT, 1), "writeIntervalArray");
//        typeToVdsWrite.put(Type.INT, "writeListType");
//        typeToVdsWrite.put(Type.INT, "writeLOID");
//        typeToVdsWrite.put(Type.INT, "writeLOIDArray");
        typeToVdsWrite.put(long.class, "writeLong");
        typeToVdsWrite.put(long[].class, "writeLongArray");
        typeToVdsWrite.put(Object.class, "writeObject");
        typeToVdsWrite.put(short.class, "writeShort");
        typeToVdsWrite.put(short[].class, "writeShortArray");
        typeToVdsWrite.put(String.class, "writeString");
//        typeToVdsWrite.put(Type.INT, "writeTime");
//        typeToVdsWrite.put(new ArrayType(Type.INT, 1), "writeTimeArray");
        typeToVdsWrite.put(java.util.Date.class, "writeTimestamp");
        typeToVdsWrite.put(java.util.Date[].class, "writeTimestampArray");

        typeToVdsRead = new HashMap();
//        typeToVdsRead.put(Type.INT , "readBCD");
//        typeToVdsRead.put(new ArrayType(Type.INT,1) , "readBCDArray");
        typeToVdsRead.put(boolean.class, "readBoolean");
        typeToVdsRead.put(boolean[].class, "readBooleanArray");
        typeToVdsRead.put(byte.class, "readByte");
        typeToVdsRead.put(byte[].class, "readByteArray");
        typeToVdsRead.put(char.class, "readChar");
        typeToVdsRead.put(char[].class, "readCharArray");
//        typeToVdsRead.put(Type.INT , "readDate");
//        typeToVdsRead.put(new ArrayType(Type.INT, 1) , "readDateArray");
        typeToVdsRead.put(double.class, "readDouble");
        typeToVdsRead.put(double[].class, "readDoubleArray");
//        typeToVdsRead.put(Type.INT , "readDynamicType");
        typeToVdsRead.put(float.class, "readFloat");
        typeToVdsRead.put(float[].class, "readFloatArray");
        typeToVdsRead.put(int.class, "readInt");
        typeToVdsRead.put(int[].class, "readIntArray");
//        typeToVdsRead.put(Type.INT , "readInterval");
//        typeToVdsRead.put(new ArrayType(Type.INT, 1) , "readIntervalArray");
//        typeToVdsRead.put(Type.INT , "readListType");
//        typeToVdsRead.put(Type.INT , "readLOID");
//        typeToVdsRead.put(new ArrayType(Type.INT, 1) , "readLOIDArray");
        typeToVdsRead.put(long.class, "readLong");
        typeToVdsRead.put(long[].class, "readLongArray");
        typeToVdsRead.put(Object.class, "readObject");
        typeToVdsRead.put(short.class, "readShort");
        typeToVdsRead.put(short[].class, "readShortArray");
        typeToVdsRead.put(String.class, "readString");
//        typeToVdsRead.put(Type.INT , "readTime");
//        typeToVdsRead.put(new ArrayType(Type.INT, 1) , "readTimeArray");
        typeToVdsRead.put(java.util.Date.class, "readTimestamp");
        typeToVdsRead.put(java.util.Date[].class,"readTimestampArray");
    }

    /**
     * Generates a class State object from the classInfo object.
     */
    public ClassSpec generateState(ClassMetaData cmd) {
        ClassSpec spec = super.generateState(cmd);

        spec.addImport(DatastoreObject.class.getName());
        spec.addImport(DatastoreManager.class.getName());
        spec.addImport(DSOList.class.getName());
        spec.addImport(ArrayList.class.getName());
        spec.addImport(VdsField.class.getName());
        spec.addImport(UserSchemaField.class.getName());
        spec.addImport(VdsUntypedOID.class.getName());

        spec.addInterface(VdsState.class.getName());

        addWritePrimaryFieldsToDSO();
        addReadPrimaryFieldsFromDSO();
        addWriteSecondaryFieldsToDSOList();
        addDeleteSecondaryFields();

        return spec;
    }

    protected void addInitStaticsBody(StringBuffer buf) {
        super.addInitStaticsBody(buf);
        // early exit from method if storeClass is null (i.e. remote PMF)
        buf.append("\t\tif (cmd.storeClass == null) return true;\n");
        buf.append("\t\tFieldMetaData[] fields = cmd.stateFields;\n");
        buf.append("\t\tVdsField vf;\n");
        FieldMetaData[] fields = cmd.stateFields;
        for (int j = 0; j < fields.length; j++) {
            if (fields[j].primaryField) {
                buf.append("\t\tvf = (VdsField)fields[" + j + "].storeField;\n");
                buf.append("\t\t" + StateSrcGenerator.SCHEMA_FIELD_PREFIX + j +
                        " = vf.schemaField;\n");
                // is this a embedded field
                if (fields[j].category == MDStatics.CATEGORY_ARRAY ||
                        fields[j].category == MDStatics.CATEGORY_COLLECTION ||
                        fields[j].category == MDStatics.CATEGORY_MAP ||
                        fields[j].category == MDStatics.CATEGORY_EXTERNALIZED) {
                    buf.append("\t\t" + StateSrcGenerator.VDS_FIELD_PREFIX + j +
                            " = vf;\n");
                }
            }
            if (fields[j].secondaryField) {
                buf.append("\t\t" + StateSrcGenerator.VDS_FIELD_PREFIX + j +
                        " = (VdsField)fields[" + j + "].storeField;\n");
            }
        }
    }

    protected boolean isLoid(FieldMetaData fmd) {
        if (fmd.storeField != null) {
            if (fmd.fake &&
                    fmd.typeCode == MDStatics.LONG &&
                    (fmd.storeField instanceof VdsTemplateField ||
                    fmd.storeField instanceof VdsExternalizedField ||
                    fmd.storeField instanceof VdsArrayField)) {
                return true;
            }
        }
        return false;
    }

    protected void addWritePrimaryFieldsToDSO() {
        StringBuffer buf = new StringBuffer();
//    public final void writePrimaryFieldsToDSO(DatastoreObject dso, DatastoreManager dsi) {
//        dso.allocate();
//        dso.writeObject(schemaField_22, _22);
//        dso.writeLOID(87, _23);
//        dso.writeInt(0, _30);
//    }
        buf.append("\n\tpublic void writePrimaryFieldsToDSO(DatastoreObject dso, DatastoreManager dsi) {\n");
        List list = getPrimaryFields();
        if (!list.isEmpty()){
            buf.append("\t\tdso.allocate();\n");
        }
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            int fieldNum = fmd.stateFieldNo;
            Class fieldType = fmd.type;
            VdsField vdsField = (VdsField)fmd.storeField;
            switch (fmd.category) {
                case MDStatics.CATEGORY_SIMPLE:
                    if (fieldType.equals(String.class) ||
                            fieldType.equals(java.util.Locale.class) ||
                            fieldType.equals(java.math.BigDecimal.class) ||
                            fieldType.equals(java.math.BigInteger.class) ||
                            wrapperTypesToPrimative.containsKey(fieldType) ||
                            isDate(fmd) ||
                            isObject(fmd)) {
                        buf.append("\t\tdso.writeObject(schemaField_" + fieldNum + ", _" + fieldNum + ");\n");
                    } else if (isLoid(fmd)) {
                        buf.append("\t\tdso.writeLOID(schemaField_" + fieldNum + ", _" + fieldNum + ");\n");
                    } else {
                        buf.append("\t\tdso."+ typeToVdsWrite.get(fieldType) +"(schemaField_"+ fieldNum +", _" + fieldNum + ");\n");
                    }

                    break;
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_EXTERNALIZED:
                    buf.append("\t\tdso.writeObject(schemaField_" + fieldNum + ", _" + fieldNum + ");\n");
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                    buf.append("\t\tdso.writeObject(schemaField_" + fieldNum + ", vdsField_" + fieldNum + ".createEmbeddedDSO(_" + fieldNum + ", dsi));\n");
                    break;

                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    buf.append("\t\tdso.writeObject(schemaField_" + fieldNum + ", _" + fieldNum + ");\n");
                    break;
                default:
                    throw new NotImplementedException("Category " +
                            MDStaticUtils.toCategoryString(fmd.category));
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addWriteSecondaryFieldsToDSOList() {
        StringBuffer buf = new StringBuffer();

//        public final void writeSecondaryFieldsToDSOList (DatastoreManager dsi, DSOList list) {
//            if ((filled0 & 2) != 0) {
//                list.add(vdsField_1.createAndFillDSO(dsi, _1, this));
//            }
//            if ((filled0 & 4) != 0) {
//                list.add(vdsField_2.createAndFillDSO(dsi, _2, this));
//            }
//            if ((filled0 & 8) != 0) {
//                list.add(vdsField_3.createAndFillDSO(dsi, _3, this));
//            }
//        }
        buf.append("\n\tpublic void writeSecondaryFieldsToDSOList (DatastoreManager dsi, DSOList list) {\n");
        List list = getSecondaryFields();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            int fieldNum = fmd.stateFieldNo;
            buf.append("\t\tif (("+ getFilledFieldName(fieldNum) +" & "+ getFieldIndex(fieldNum) +") != 0) {\n");
            buf.append("\t\t\tlist.add(vdsField_"+ fieldNum +".createAndFillDSO(dsi, _"+ fieldNum +", this));\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addReadPrimaryFieldsFromDSO() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void readPrimaryFieldsFromDSO(DatastoreObject dso) {\n");
        boolean haveOID = false;
        List list = getPrimaryFields();
        if (!list.isEmpty()) {
            buf.append("\t\ttry {\n");
        }
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            if (fmd.category == MDStatics.CATEGORY_REF ||
                    fmd.category == MDStatics.CATEGORY_POLYREF) {
                haveOID = true;
            }
        }
        if (haveOID) {
            buf.append("\t\t\tlong loid = 0L;\n");
        }

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            int fieldNum = fmd.stateFieldNo;
            Class fieldType = fmd.type;
            VdsField vdsField = (VdsField)fmd.storeField;
            switch (fmd.category) {
                case MDStatics.CATEGORY_SIMPLE:
                    if (fieldType.equals(String.class) ||
                            wrapperTypesToPrimative.containsKey(fieldType) ||
                            isDate(fmd) ||
                            fieldType.equals(java.util.Locale.class) ||
                            fieldType.equals(java.math.BigDecimal.class) ||
                            fieldType.equals(java.math.BigInteger.class)) {
                        buf.append("\t\t\t_" + fieldNum + " = (" + fieldType.getName() + ")dso.readObject(schemaField_" + fieldNum + ");\n");
                    } else if (isObject(fmd)) {
                        buf.append("\t\t\t_" + fieldNum + " = dso.readObject(schemaField_" + fieldNum + ");\n");
                    } else if (isLoid(fmd)) {
                        buf.append("\t\t\t_" + fieldNum + " = dso.readLOID(schemaField_"+ fieldNum +");\n");
                    } else {
                        buf.append("\t\t\t_"+ fieldNum +" = dso."+ typeToVdsRead.get(fieldType) +"(schemaField_"+ fieldNum +");\n");
                    }

                    break;
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                case MDStatics.CATEGORY_EXTERNALIZED:
                    buf.append("\t\t\t_"+ fieldNum +" = vdsField_"+ fieldNum +".readEmbeddedField(dso);\n");
                    break;
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    boolean isPolyRef = (fmd.category == MDStatics.CATEGORY_POLYREF);
                    buf.append("\t\t\tloid = dso.readLOID(schemaField_"+ fieldNum +");\n");
                    buf.append("\t\t\tif (loid == 0L) {\n");
                    buf.append("\t\t\t\t_"+ fieldNum +" = null;\n");
                    buf.append("\t\t\t} else {\n");
                    boolean isInHier = false;
                    String oidName = null;
                    if (isPolyRef) {
                        oidName = VDS_UNTYPED_OID;
                        isInHier = false;
                    } else {
                        oidName = fmd.typeMetaData.oidClassName;
                        isInHier = fmd.typeMetaData.isInHierarchy();
                    }
                    if (isInHier) {
                        buf.append("\t\t\t\t_" + fieldNum + " = new " + oidName + "(cmd.jmd.classes["+ fmd.typeMetaData.index +"] ,false);\n");
                    } else {
                        buf.append("\t\t\t\t_"+ fieldNum +" = new "+ oidName +"();\n");
                    }
                    buf.append("\t\t\t\t((OID) _"+ fieldNum +").setLongPrimaryKey(loid);\n");
                    buf.append("\t\t\t}\n");
                    break;
                default:
                    throw new NotImplementedException("Category " +
                            MDStaticUtils.toCategoryString(fmd.category));
            }
        }

        boolean doMask = false;
        int num = cmd.stateFields.length;
        int[] masks = new int[getNumOfControlFields()];
        for (int i = 0; i < num; i++) {
            if (cmd.stateFields[i].primaryField) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                doMask = true;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }
        if (doMask) {
            for (int i = 0; i < masks.length; i++) {
                if (masks[i] != 0){
                    buf.append("\t\t\tfilled"+i+" |= "+ masks[i] +";\n");
                }
            }
        }

        if (!list.isEmpty()) {
            buf.append("\t\t} finally {\n");
            buf.append("\t\t\tdso.release();\n");
            buf.append("\t\t}\n");
        } else {
            buf.append("\t\tdso.release();\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addDeleteSecondaryFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void deleteSecondaryFields(ArrayList dsoList) {\n");
        int num = cmd.stateFields.length;
        int j = 0;
        for (int i = 0; i < num; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            if (!fmd.secondaryField) continue;
            j++;
        }

        if (j != 0) {
            buf.append("\t\tDatastoreObject dso = null;\n");
            j = 0;
            for (int i = 0; i < num; i++) {
                FieldMetaData fmd = cmd.stateFields[i];
                if (!fmd.secondaryField) continue;
                String fieldName = getFieldName(
                        ((VdsField)fmd.storeField).getLoidField().stateFieldNo);
                buf.append("\t\tif ("+ fieldName +" != 0L) {\n");
                buf.append("\t\t\tdso = new DatastoreObject(" + fieldName + ");\n");
                buf.append("\t\t\tdso.setIsDeleted(true);\n");
                buf.append("\t\t\tdsoList.add(dso);\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected String getOIDName(FieldMetaData fmd) {
        return fmd.typeMetaData.oidClassName;
    }

    protected void addContainsValidAppIdFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean containsValidAppIdFields() {\n");
        buf.append("\t\treturn false;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * Add all PC fields in the hier to state and
     * private boolean isDirty;
     * private boolean[] dirtyFields = new boolean[20];
     * private boolean[] filled = new boolean[20];
     */
    protected void addFields() {
        super.addFields();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].primaryField) {
                spec.addField("public static "+ USER_SCHEMA_FIELD +" "+
                        SCHEMA_FIELD_PREFIX + i);
                // is this a embedded field
                if (fields[i].category == MDStatics.CATEGORY_ARRAY ||
                        fields[i].category == MDStatics.CATEGORY_COLLECTION ||
                        fields[i].category == MDStatics.CATEGORY_MAP ||
                        fields[i].category == MDStatics.CATEGORY_EXTERNALIZED) {
                    spec.addField("public static " + VDS_FIELD + " " +
                            VDS_FIELD_PREFIX + i);
                }
            } else if (fields[i].secondaryField) {
                spec.addField("public static " + VDS_FIELD + " " +
                        VDS_FIELD_PREFIX + i);
            }
        }
    }

}

