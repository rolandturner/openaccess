
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

import com.versant.core.metadata.*;
import com.versant.core.common.*;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.PCStateMan;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.server.OIDGraph;
import com.versant.core.util.IntArray;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.spi.PersistenceCapable;
import java.util.*;
import java.math.BigDecimal;
import java.io.*;

/**
 * Generates java source for a State class for a PC class.
 */
public abstract class StateSrcGenerator {

    protected ClassMetaData cmd;
    protected String className;
    protected ArrayList imutableTypeList;
    protected ArrayList nonObjectClassTypeList;
    protected ArrayList realNonObjectClassTypeList;
    protected HashMap getFieldToClass;
    protected HashMap classToSetField;
    protected HashMap classToGetFieldAbs;
    protected HashMap classToSetFieldAbs;
    protected HashMap classToInternalSetField;
    protected HashMap classToInternalSetFieldAbs;
    protected HashMap typeToResultSetGetField;
    protected HashMap typeToPreparedStatementSetField;
    protected HashMap wrapperTypesToPrimative;
    protected HashMap primativeTypesToWrapper;
    protected HashMap wrapperTypesToValue;
    protected HashMap wrapperStringToValue;
    protected HashMap primClassToSerReadMethod;
    protected HashMap primClassToSerWriteMethod;
    protected HashMap primativeToClass;

    protected static final String CLASS_META_DATA_CLASS = ClassMetaData.class.getName();
    protected static final String FIELD_META_DATA_CLASS = FieldMetaData.class.getName();
    protected static final String OID_SUPER_CLASS = OID.class.getName();
    protected static final String STATE_SUPER_CLASS = State.class.getName();
    protected static final String INT_ARRAY = IntArray.class.getName();

    protected ClassSpec spec;

    //####################################################################################################


    protected final String FIELD_NOT_FOUND_EXCEPTION = "javax.jdo.JDOFatalInternalException";
    protected final String FILLED_FIELD_NAME = "filled";
    protected final String DIRTY_FIELD_NAME = "dirtyFields";
    protected final String RESOLVED_NAME = "resolved";
    public static final String JDBC_CONVERTER_FIELD_PREFIX = "jdbcConverter_";
    public static final String SCHEMA_FIELD_PREFIX = "schemaField_";
    public static final String VDS_FIELD_PREFIX = "vdsField_";

    protected int totalNoOfFields;

    public StateSrcGenerator() {
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
        typeToResultSetGetField.put(java.math.BigDecimal.class,"getBigDecimal");  // has a scale
//        typeToResultSetGetField.put(new ObjectType("java.sql.Date"),        "getDate");
//        typeToResultSetGetField.put(new ObjectType("java.sql.Time"),        "getTime");
//        typeToResultSetGetField.put(new ObjectType("java.sql.Timestamp"),   "getTimestamp");
//        typeToResultSetGetField.put(new ObjectType("java.sql.InputStream"), "getAsciiStream");
//        typeToResultSetGetField.put(new ObjectType("java.sql.InputStream"), "getBinaryStream");

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
        typeToPreparedStatementSetField.put(byte[].class,"setBytes");
        typeToPreparedStatementSetField.put(BigDecimal.class, "setBigDecimal");
//        typeToPreparedStatementSetField.put(new ObjectType("java.sql.Date"),        "setDate");     //
//        typeToPreparedStatementSetField.put(new ObjectType("java.sql.Time"),        "setTime");
//        typeToPreparedStatementSetField.put(new ObjectType("java.sql.Timestamp"),   "setTimestamp");
//        typeToPreparedStatementSetField.put(new ObjectType("java.sql.InputStream"), "setAsciiStream");
//        typeToPreparedStatementSetField.put(new ObjectType("java.sql.InputStream"), "setBinaryStream");



        getFieldToClass = new HashMap();
        getFieldToClass.put("getIntField", int.class);
        getFieldToClass.put("getByteField", byte.class);
        getFieldToClass.put("getCharField", char.class);
        getFieldToClass.put("getShortField", short.class);
        getFieldToClass.put("getFloatField", float.class);
        getFieldToClass.put("getDoubleField", double.class);
        getFieldToClass.put("getLongField", long.class);
        getFieldToClass.put("getLongFieldInternal", long.class);
        getFieldToClass.put("getBooleanField", boolean.class);
        getFieldToClass.put("getStringField", String.class);

        classToSetField = new HashMap();
        classToSetField.put(int.class, "setIntField");
        classToSetField.put(byte.class, "setByteField");
        classToSetField.put(long.class, "setLongField");
        classToSetField.put(char.class, "setCharField");
        classToSetField.put(short.class, "setShortField");
        classToSetField.put(float.class, "setFloatField");
        classToSetField.put(double.class, "setDoubleField");
        classToSetField.put(boolean.class, "setBooleanField");
        classToSetField.put(String.class, "setStringField");

        classToGetFieldAbs = new HashMap();
        classToGetFieldAbs.put(int.class, "getIntFieldAbs");
        classToGetFieldAbs.put(byte.class, "getByteFieldAbs");
        classToGetFieldAbs.put(char.class, "getCharFieldAbs");
        classToGetFieldAbs.put(short.class, "getShortFieldAbs");
        classToGetFieldAbs.put(float.class, "getFloatFieldAbs");
        classToGetFieldAbs.put(double.class, "getDoubleFieldAbs");
        classToGetFieldAbs.put(long.class, "getLongFieldAbs");
        classToGetFieldAbs.put(boolean.class, "getBooleanFieldAbs");
        classToGetFieldAbs.put(String.class, "getStringFieldAbs");

        classToSetFieldAbs = new HashMap();
        classToSetFieldAbs.put(int.class, "setIntFieldAbs");
        classToSetFieldAbs.put(byte.class, "setByteFieldAbs");
        classToSetFieldAbs.put(long.class, "setLongFieldAbs");
        classToSetFieldAbs.put(char.class, "setCharFieldAbs");
        classToSetFieldAbs.put(short.class, "setShortFieldAbs");
        classToSetFieldAbs.put(float.class, "setFloatFieldAbs");
        classToSetFieldAbs.put(double.class, "setDoubleFieldAbs");
        classToSetFieldAbs.put(boolean.class, "setBooleanFieldAbs");
        classToSetFieldAbs.put(String.class, "setStringFieldAbs");

        classToInternalSetField = new HashMap();
        classToInternalSetField.put(int.class, "setInternalIntField");
        classToInternalSetField.put(byte.class, "setInternalByteField");
        classToInternalSetField.put(long.class, "setInternalLongField");
        classToInternalSetField.put(char.class, "setInternalCharField");
        classToInternalSetField.put(short.class, "setInternalShortField");
        classToInternalSetField.put(float.class, "setInternalFloatField");
        classToInternalSetField.put(double.class, "setInternalDoubleField");
        classToInternalSetField.put(String.class, "setInternalStringField");
        classToInternalSetField.put(boolean.class, "setInternalBooleanField");

        classToInternalSetFieldAbs = new HashMap();
        classToInternalSetFieldAbs.put(int.class, "setInternalIntFieldAbs");
        classToInternalSetFieldAbs.put(byte.class, "setInternalByteFieldAbs");
        classToInternalSetFieldAbs.put(long.class, "setInternalLongFieldAbs");
        classToInternalSetFieldAbs.put(char.class, "setInternalCharFieldAbs");
        classToInternalSetFieldAbs.put(short.class, "setInternalShortFieldAbs");
        classToInternalSetFieldAbs.put(float.class, "setInternalFloatFieldAbs");
        classToInternalSetFieldAbs.put(double.class,
                "setInternalDoubleFieldAbs");
        classToInternalSetFieldAbs.put(String.class,
                "setInternalStringFieldAbs");
        classToInternalSetFieldAbs.put(boolean.class,
                "setInternalBooleanFieldAbs");


        wrapperTypesToPrimative = new HashMap(8);
        wrapperTypesToPrimative.put(Integer.class, Integer.TYPE);
        wrapperTypesToPrimative.put(Byte.class, Byte.TYPE);
        wrapperTypesToPrimative.put(Short.class, Short.TYPE);
        wrapperTypesToPrimative.put(Float.class, Float.TYPE);
        wrapperTypesToPrimative.put(Double.class, Double.TYPE);
        wrapperTypesToPrimative.put(Long.class, Long.TYPE);
        wrapperTypesToPrimative.put(Boolean.class, Boolean.TYPE);
        wrapperTypesToPrimative.put(Character.class, Character.TYPE);

        primativeToClass = new HashMap(8);
        primativeToClass.put("int", "java.lang.Integer.TYPE");
        primativeToClass.put("byte", "java.lang.Byte.TYPE");
        primativeToClass.put("short", "java.lang.Short.TYPE");
        primativeToClass.put("float", "java.lang.Float.TYPE");
        primativeToClass.put("double", "java.lang.Double.TYPE");
        primativeToClass.put("long", "java.lang.Long.TYPE");
        primativeToClass.put("boolean", "java.lang.Boolean.TYPE");
        primativeToClass.put("char", "java.lang.Character.TYPE");

        primativeTypesToWrapper = new HashMap(8);
        primativeTypesToWrapper.put(Integer.TYPE, "Integer");
        primativeTypesToWrapper.put(Byte.TYPE, "Byte");
        primativeTypesToWrapper.put(Character.TYPE, "Character");
        primativeTypesToWrapper.put(Short.TYPE, "Short");
        primativeTypesToWrapper.put(Float.TYPE, "Float");
        primativeTypesToWrapper.put(Double.TYPE, "Double");
        primativeTypesToWrapper.put(Long.TYPE, "Long");
        primativeTypesToWrapper.put(Boolean.TYPE, "Boolean");

        wrapperTypesToValue = new HashMap(8);
        wrapperTypesToValue.put(Integer.class, "intValue");
        wrapperTypesToValue.put(Byte.class, "byteValue");
        wrapperTypesToValue.put(Character.class, "charValue");
        wrapperTypesToValue.put(Short.class, "shortValue");
        wrapperTypesToValue.put(Float.class, "floatValue");
        wrapperTypesToValue.put(Double.class, "doubleValue");
        wrapperTypesToValue.put(Long.class, "longValue");
        wrapperTypesToValue.put(Boolean.class, "booleanValue");

        wrapperStringToValue = new HashMap(8);
        wrapperStringToValue.put("Integer", "intValue");
        wrapperStringToValue.put("Byte", "byteValue");
        wrapperStringToValue.put("Character", "charValue");
        wrapperStringToValue.put("Short", "shortValue");
        wrapperStringToValue.put("Float", "floatValue");
        wrapperStringToValue.put("Double", "doubleValue");
        wrapperStringToValue.put("Long", "longValue");
        wrapperStringToValue.put("Boolean", "booleanValue");

        nonObjectClassTypeList = new ArrayList();
        nonObjectClassTypeList.add(int.class);
        nonObjectClassTypeList.add(byte.class);
        nonObjectClassTypeList.add(long.class);
        nonObjectClassTypeList.add(char.class);
        nonObjectClassTypeList.add(short.class);
        nonObjectClassTypeList.add(float.class);
        nonObjectClassTypeList.add(double.class);
        nonObjectClassTypeList.add(boolean.class);
        nonObjectClassTypeList.add(String.class);

        realNonObjectClassTypeList = new ArrayList();
        realNonObjectClassTypeList.add(int.class);
        realNonObjectClassTypeList.add(byte.class);
        realNonObjectClassTypeList.add(long.class);
        realNonObjectClassTypeList.add(char.class);
        realNonObjectClassTypeList.add(short.class);
        realNonObjectClassTypeList.add(float.class);
        realNonObjectClassTypeList.add(double.class);
        realNonObjectClassTypeList.add(boolean.class);

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

        imutableTypeList = new ArrayList();
        imutableTypeList.add(java.util.Locale.class);
        imutableTypeList.add(java.math.BigDecimal.class);
        imutableTypeList.add(java.math.BigInteger.class);
        imutableTypeList.add(Integer.class);
        imutableTypeList.add(Byte.class);
        imutableTypeList.add(Character.class);
        imutableTypeList.add(Short.class);
        imutableTypeList.add(Float.class);
        imutableTypeList.add(Double.class);
        imutableTypeList.add(Long.class);
        imutableTypeList.add(Boolean.class);
        imutableTypeList.add(String.class);

    }

    /**
     * Generates a class State object from the classInfo object.
     */
    public ClassSpec generateState(ClassMetaData cmd) {

        this.cmd = cmd;
        this.className = cmd.stateClassName;
        this.totalNoOfFields = cmd.stateFields.length;
        spec = new ClassSpec(null, className, State.class.getName());

        spec.addImportsForJavaLang();
        spec.addImport(OID.class.getName());
        spec.addImport(State.class.getName());
        spec.addImport(ClassMetaData.class.getName());
        spec.addImport(FieldMetaData.class.getName());
        spec.addImport(ModelMetaData.class.getName());
        spec.addImport(ObjectOutput.class.getName());
        spec.addImport(ObjectInput.class.getName());
        spec.addImport(DataInputStream.class.getName());
        spec.addImport(DataOutputStream.class.getName());
        spec.addImport(IOException.class.getName());
        spec.addImport(JDOFatalInternalException.class.getName());
        spec.addImport(VersantPersistenceManagerImp.class.getName());
        spec.addImport(PersistenceCapable.class.getName());
        spec.addImport(FetchGroup.class.getName());
        spec.addImport(OIDGraph.class.getName());
        spec.addImport(StateUtil.class.getName());
        spec.addImport(PCStateMan.class.getName());
        spec.addImport(SerUtils.class.getName());
        spec.addImport(OIDObjectOutput.class.getName());
        spec.addImport(OIDObjectInput.class.getName());
        spec.addImport(Utils.class.getName());
        spec.addImport(PersistenceContext.class.getName());
        spec.addImport(VersantStateManager.class.getName());
        spec.addImport(VersantPMProxy.class.getName());

        addFields();
        addInitStatics();

        addConstructor();               //cool
        addSetClassMetaData();          //cool
        addNewInstance();               //cool

        addAllGetXXXFields();           //cool
        addAllSetInternalXXXFields();   //cool
        addAllSetXXXFields();           //cool
        addAllGetXXXFieldsAbs();        //cool
        addAllSetInternalXXXFieldsAbs();//cool
        addAllSetXXXFieldsAbs();        //cool
        addHasSameFields();             //cool
        addCopyFields();                //cool
        addClear();                     //cool
        addMakeDirty();                 //cool
        addSetFilled();
        addIsDirtyInt();                //cool
        addGetClassIndex();             //cool
        addHashCode();                  //cool
        addEqualsObject();              //cool
        addContainsField();             //cool
        addMakeClean();                 //cool
        addClearNonFilled();            //cool
        addUpdateNonFilled();           //cool
        addGetCopy();                   //cool
        addGetFieldNos();               //cool
        addContainsApplicationIdentityFields();   //cool
        addGetInternalObjectField();    //cool
        addGetInternalObjectFieldAbs(); //cool
        addIsNull();                    //cool
        addHasSameNullFields();         //cool
        addFindDirectEdges();           //cool
        addReplaceNewObjectOIDs();      //cool
        addIsDirty();                   //cool
        addIsHollow();                  //cool
        addUpdateFrom();                //cool
        addGetPass1FieldNos();          //cool
        addGetPass2FieldNos();          //cool
        addContainsPass1Fields();       //cool
        addGetPass1FieldRefFieldNosWithNewOids();
        addContainsPass2Fields();       //cool
        addIsEmpty();                   //cool
        addContainFields();             //cool
        addGetClassMetaData();          //cool
        addGetClassMetaDataJMD();       //cool
        addCompareToPass1();            //cool
        addClearTransactionNonPersistentFields();  //cool
        addToString();                  //cool
        addClearCollectionFields();     //cool
        addContainsFetchGroup();        //cool
        addReplaceSCOFields();          //cool
        addClearApplicationIdentityFields();
        addCopyKeyFieldsFromOID();
        addCheckKeyFields();
        addCopyKeyFieldsUpdate();
        addClearSCOFields();
        addGetNullFields();
        addContainsFieldAbs();
        addMakeDirtyAbs();
        addContainFieldsAbs();
        addGetResolvableObjectFieldAbs();
        addClearNonAutoSetFields();
        addUpdateAutoSetFieldsCreated();
        addUpdateAutoSetFieldsModified();
        addGetResolvableObjectField();  //cool
        addAddRefs();                   //cool
        addWriteExternal();
        addReadExternal();

        addCopyOptimisticLockingField();
        addCopyFieldsForOptimisticLocking();
        addGetDirtyState();             //cool
        addRetrieve();
        addClearDirtyFields();
        addPrepare();                   //cool
        addContainsValidAppIdFields();
        addUnmanageSCOFields();
        addVersion();
        addIsResolvedForClient();
        addGetOptimisticLockingValue();
        addOneToManyInverseFieldsForL2Evict();

        addClearFilledFlags();
        addFillForRead();
        addIsFieldNullorZero();

        return spec;
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
        buf.append("\t\tif (cmd != null) return false;\n");
        buf.append("\t\tcmd = jmd.classes[" + cmd.index + "];\n");
    }

    protected void addOneToManyInverseFieldsForL2Evict() {
//        public final void addOneToManyInverseFieldsForL2Evict(VersantPersistenceManagerImp pm) {
//            if (_4 != null) {
//                pm.evictFromL2CacheAfterCommitImp(_4);
//            }
//        }

        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void addOneToManyInverseFieldsForL2Evict(VersantPersistenceManagerImp pm) {\n");
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            if (fmd.isDetail && !fmd.managed) {
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif (_"+ fieldNo +" != null) {\n");
                buf.append("\t\t\tpm.evictFromL2CacheAfterCommitImp(_" + fieldNo + ");\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addFillForRead() {
        StringBuffer buf = new StringBuffer();
//    public final void fillForRead(State dest, VersantPersistenceManagerImp pm) {
//        HYPERDRIVE_STATE_com_versant_core_jdo_junit_test1_model_ObjectFieldWithPCValue state = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test1_model_ObjectFieldWithPCValue)dest;
//        PersistenceCapable pc = null;
//        if (_0 instanceof PersistenceCapable) {
//            pc = (PersistenceCapable)_0;
//            if (pc.jdoGetPersistenceManager() != null) {
//                state._0 = StateUtil.getPValueForRef(pc, pm);
//            }
//        } else {
//            state._0 = _0;
//        }
//        state._1 = _1;
//        state.filled0 |= 3;
//    }

        buf.append("\n\tpublic final void fillForRead(State dest, VersantPersistenceManagerImp pm) {\n");
        if (cmd.storeAllFields) {
            buf.append("\t\t"+ className +" state = ("+ className +")dest;\n");

            boolean haveOID = false;
            int num = cmd.stateFields.length;
            for (int i = 0; i < num; i++) {
                FieldMetaData fmd = cmd.stateFields[i];
                if (fmd.category == MDStatics.CATEGORY_REF ||
                        fmd.category == MDStatics.CATEGORY_POLYREF) {
                    haveOID = true;
                }
            }
            if (haveOID) {
                buf.append("\t\tPersistenceCapable pc = null;\n");
            }
            for (int i = 0; i < num; i++) {
                FieldMetaData fmd = cmd.stateFields[i];
                int fieldNum = fmd.stateFieldNo;
                if (fmd.fake) {
                    buf.append("\t\tstate._"+ fieldNum +" = _"+ fieldNum +";\n");
                } else {
                    int cat = fmd.category;
                    if (cat == MDStatics.CATEGORY_REF
                            || cat == MDStatics.CATEGORY_POLYREF) {
                        buf.append("\t\tif (_"+ fieldNum +" instanceof PersistenceCapable) {\n");
                        buf.append("\t\t\tpc = (PersistenceCapable)_" + fieldNum + ";\n");
                        buf.append("\t\t\tif (pc.jdoGetPersistenceManager() != null) {\n");
                        buf.append("\t\t\t\tstate._" + fieldNum + " = StateUtil.getPValueForRef(pc, pm);\n");
                        buf.append("\t\t\t}\n");
                        buf.append("\t\t} else {\n");
                        buf.append("\t\t\tstate._" + fieldNum + " = _" + fieldNum + ";\n");
                        buf.append("\t\t}\n");
                    }
                }
            }
            boolean doMask = false;
            int[] masks = new int[getNumOfControlFields()];
            for (int i = 0; i < num; i++) {

                if (cmd.stateFields[i].fake ||
                        cmd.stateFields[i].category == MDStatics.CATEGORY_REF ||
                        cmd.stateFields[i].category == MDStatics.CATEGORY_POLYREF) {
                    int fieldNum = cmd.stateFields[i].stateFieldNo;
                    doMask = true;
                    masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
                }
            }
            if (doMask) {
                for (int i = 0; i < masks.length; i++) {
                    if (masks[i] != 0){
                        buf.append("\t\tstate.filled"+i+" |= "+ masks[i] +";\n");
                    }
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetOptimisticLockingValue() {
        StringBuffer buf = new StringBuffer();
//        public final Object getOptimisticLockingValue() {
//            return new Short(_23);
//        }
        buf.append("\n\tpublic final Object getOptimisticLockingValue() {\n");
        if (cmd.optimisticLockingField != null) {
            FieldMetaData fmd = cmd.optimisticLockingField;
            switch (fmd.typeCode) {
                case MDStatics.DATE:
                    buf.append("\t\treturn _" + fmd.stateFieldNo + ";\n");
                    break;
                case MDStatics.LONG:
                    buf.append("\t\treturn new Long(_" + fmd.stateFieldNo + ");\n");
                    break;
                case MDStatics.INT:
                    buf.append("\t\treturn new Integer(_" + fmd.stateFieldNo + ");\n");
                    break;
                case MDStatics.SHORT:
                    buf.append("\t\treturn new Short(_"+ fmd.stateFieldNo +");\n");
                    break;
                case MDStatics.BYTE:
                    buf.append("\t\treturn new Byte(_" + fmd.stateFieldNo + ");\n");
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("Invalid typeCode " + fmd.typeCode +
                            " for version field: stateFieldNo " + fmd.stateFieldNo +
                            " " + fmd.name);
            }
        } else {
            buf.append("\t\treturn null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addVersion() {
        StringBuffer buf = new StringBuffer();
//        public final String getVersion() {
//            return "3.2.0";
//        }
        buf.append("\n\tpublic final String getVersion() {\n");
        buf.append("\t\treturn \""+ Debug.VERSION +"\";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addClearDirtyFields() {
        StringBuffer buf = new StringBuffer();
//    public final void clearDirtyFields() {
//        filled0 &= ~dirtyFields0;
//        resolved0 &= ~dirtyFields0;
//        dirtyFields0 = 0;
//
//        filled1 &= ~dirtyFields1;
//        resolved1 &= ~dirtyFields1;
//        dirtyFields1 = 0;
//
//        filled2 &= ~dirtyFields2;
//        resolved2 &= ~dirtyFields2;
//        dirtyFields2 = 0;
//    }
        buf.append("\n\tpublic final void clearDirtyFields() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            buf.append("\t\tfilled"+i+" &= ~dirtyFields"+i+";\n");
            buf.append("\t\tresolved"+i+" &= ~dirtyFields"+i+";\n");
            buf.append("\t\tdirtyFields"+i+" = 0;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addUpdateAutoSetFieldsCreated() {
        StringBuffer buf = new StringBuffer();
//    public final void updateAutoSetFieldsCreated(Date now) {
//        _6 = 1;
//        filled0 |= 64;
//    }
        buf.append("\n\tpublic final void updateAutoSetFieldsCreated(java.util.Date now) {\n");
        if (cmd.hasAutoSetFields) {
            FieldMetaData[] stateFields = cmd.stateFields;
            for (int fieldNo = stateFields.length - 1;fieldNo >= 0; fieldNo--) {
                FieldMetaData fmd = cmd.stateFields[fieldNo];
                int autoset = fmd.autoSet;
                if (autoset != MDStatics.AUTOSET_CREATED && autoset != MDStatics.AUTOSET_BOTH) {
                    continue;
                }
                switch (fmd.typeCode) {
                    case MDStatics.DATE:
                        buf.append("\t\t_"+ fieldNo +" = now;\n");
                        buf.append("\t\t"+ getFilledFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");

                        break;
                    case MDStatics.INT:
                    case MDStatics.SHORT:
                    case MDStatics.BYTE:
                        buf.append("\t\t_" + fieldNo + " = 1;\n");
                        buf.append("\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal("Invalid typeCode " + fmd.typeCode +
                                " for autoset field: stateFieldNo " + fieldNo +
                                " " + fmd.name);
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addUpdateAutoSetFieldsModified() {
        StringBuffer buf = new StringBuffer();
//    public final void updateAutoSetFieldsModified(Date now, State oldState) {
//        _6 = (short) (oldState.getShortField(6) + 1 & 32767);
//        filled0 |= 64;
//    }
        buf.append("\n\tpublic final void updateAutoSetFieldsModified(java.util.Date now, State oldState) {\n");
        if (cmd.hasAutoSetFields) {
            FieldMetaData[] stateFields = cmd.stateFields;
            for (int fieldNo = stateFields.length - 1;fieldNo >= 0; fieldNo--) {
                FieldMetaData fmd = cmd.stateFields[fieldNo];
                int autoset = fmd.autoSet;
                if (autoset != MDStatics.AUTOSET_MODIFIED && autoset != MDStatics.AUTOSET_BOTH) {
                    continue;
                }
                switch (fmd.typeCode) {
                    case MDStatics.DATE:
                        buf.append("\t\t_" + fieldNo + " = now;\n");
                        buf.append("\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                        break;
                    case MDStatics.INT:
                        buf.append("\t\t_" + fieldNo + " = (oldState.getIntField(" + fieldNo + ") + 1 & " + 0x7FFFFFFF + ");\n");
                        buf.append("\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                        break;
                    case MDStatics.SHORT:
                        buf.append("\t\t_"+ fieldNo +" = (short) (oldState.getShortField("+ fieldNo +") + 1 & "+ 0x7FFF +");\n");
                        buf.append("\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                        break;
                    case MDStatics.BYTE:
                        buf.append("\t\t_" + fieldNo + " = (byte) (oldState.getByteField(" + fieldNo + ") + 1 & " + 0x7F + ");\n");
                        buf.append("\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal("Invalid typeCode " + fmd.typeCode +
                                " for autoset field: stateFieldNo " + fieldNo +
                                " " + fmd.name);
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }



    protected void addClearNonAutoSetFields() {
        StringBuffer buf = new StringBuffer();
//    public final void clearNonAutoSetFields() {
//        filled0 = filled0 & -64;
//        resolved0 = resolved0 & -64;
//        _2 = null;
//        _3 = null;
//        _4 = null;
//        _5 = null;
//    }
        buf.append("\n\tpublic final void clearNonAutoSetFields() {\n");
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.nonAutoSetStateFieldNos.length;
        for (int i = 0; i < num; i++) {
            int fieldNum = cmd.nonAutoSetStateFieldNos[i];
            masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
        }
        int maskLenght = masks.length;
        for (int i = 0; i < maskLenght; i++) {
            if (masks[i] != 0) {
                buf.append("\t\tfilled"+i+" = filled"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
                buf.append("\t\tresolved"+i+" = resolved"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
            }
        }
        for (int i = 0; i < num; i++) {
            int fieldNum = cmd.nonAutoSetStateFieldNos[i];
            FieldMetaData data = cmd.stateFields[fieldNum];
            if (data.type.isPrimitive()) {
                continue;
            }
            buf.append("\t\t_"+ fieldNum +" = null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addMakeDirtyAbs() {
        StringBuffer buf = new StringBuffer();
//    public final void makeDirtyAbs(int absFieldNo) {
//        makeDirty(cmd.absToRel[absFieldNo]);
//    }
        buf.append("\n\tpublic final void makeDirtyAbs(int absFieldNo) {\n");
        buf.append("\t\tmakeDirty(cmd.managedFields[absFieldNo].stateFieldNo);\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsFieldAbs() {
        StringBuffer buf = new StringBuffer();
//    public final boolean containsFieldAbs(int absFieldNo) {
//        return containsField(cmd.managedFields[absFieldNo].stateFieldNo);
//    }
        buf.append("\n\tpublic final boolean containsFieldAbs(int absFieldNo) {\n");
        buf.append("\t\treturn containsField(cmd.managedFields[absFieldNo].stateFieldNo);\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetNullFields() {
        StringBuffer buf = new StringBuffer();

//    protected final int getNullFields(int index) {
//        int filled = 0;
//        int nullFields = 0;
//        switch (index) {
//            case 0: // '\0'
//                filled = filled0;
//                if ((filled & 4) != 0 && _2 == null) {
//                    nullFields |= 4;
//                }
//                if ((filled & 8) != 0 && _3 == null) {
//                    nullFields |= 8;
//                }
//                if ((filled & 16) != 0 && _4 == null) {
//                    nullFields |= 16;
//                }
//                if ((filled & 32) != 0 && _5 == null) {
//                    nullFields |= 32;
//                }
//                return nullFields;
//        }
//        return nullFields;
//    }

        buf.append("\n\tprivate final int getNullFields(int index) {\n");
        buf.append("\t\tint filled = 0;\n");
        buf.append("\t\tint nullFields = 0;\n");
        buf.append("\t\tswitch (index) {\n");
        int switchCount = getNumOfControlFields();
        for (int i = 0; i < switchCount; i++) {
            buf.append("\t\t\tcase "+i+":\n");
            buf.append("\t\t\t\tfilled = filled"+i+";\n");
            List objectList = getRealObjectFields(i);
            for (Iterator iter = objectList.iterator(); iter.hasNext();) {
                FieldMetaData data = (FieldMetaData) iter.next();
                int fieldNo = data.stateFieldNo;
                buf.append("\t\t\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0 && _"+ fieldNo +" == null) {\n");
                buf.append("\t\t\t\t\tnullFields |= "+ getFieldIndex(fieldNo) +";\n");
                buf.append("\t\t\t\t}\n");
            }
            buf.append("\t\t\t\treturn nullFields;\n");
        }
        buf.append("\t\t}\n");
        buf.append("\t\treturn nullFields;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addClearSCOFields() {
        StringBuffer buf = new StringBuffer();
//    public final void clearSCOFields() {
//        filled0 = filled0 & -907301;
//        resolved0 = resolved0 & -907301;
//        _2 = null;
//        _5 = null;
//        _11 = null;
//        _12 = null;
//        _14 = null;
//        _15 = null;
//        _16 = null;
//        _18 = null;
//        _19 = null;
//    }
        buf.append("\n\tpublic final void clearSCOFields() {\n");
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.scoFieldNos.length;
        for (int i = 0; i < num; i++) {
            int fieldNum = cmd.scoFieldNos[i];
            masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
        }
        int maskLenght = masks.length;
        for (int i = 0; i < maskLenght; i++) {
            if (masks[i] != 0) {
                buf.append("\t\tfilled"+i+" = filled"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
                buf.append("\t\tresolved"+i+" = resolved"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
            }
        }
        for (int i = 0; i < num; i++) {
            int fieldNum = cmd.scoFieldNos[i];
            buf.append("\t\t_"+ fieldNum +" = null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsUpdate() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyKeyFieldsUpdate(OID oid) {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsFromOID() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyKeyFields(OID oid) {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCheckKeyFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean checkKeyFields(OID oid) {\n");
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected void addClearApplicationIdentityFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void clearApplicationIdentityFields() {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected boolean isDate(int fieldNo) {
        FieldMetaData info = cmd.stateFields[fieldNo];
        if (info.type.isAssignableFrom(Date.class)) {
            return true;
        }
        return false;
    }

    protected boolean isDate(FieldMetaData fmd) {
        if (fmd.type.isAssignableFrom(Date.class)) {
            return true;
        }
        return false;
    }

    protected void addReplaceSCOFields() {
        StringBuffer buf = new StringBuffer();
//    public final int replaceSCOFields(PersistenceCapable owner, VersantPersistenceManagerImp sm, int absFields[]) {
//        int count = 0;
//        if (_2 != null) {
//            _2 = (Date)cmd.stateFields[2].createSCO(sm, sm.getInternalSM(owner), cmd.stateFields[2], owner, _2);
//            absFields[count++] = cmd.stateFields[2].managedFieldNo;
//        }
//        if (_5 != null) {
//            _5 = (Date)cmd.stateFields[5].createSCO(sm, sm.getInternalSM(owner), cmd.stateFields[5], owner, _5);
//            absFields[count++] = cmd.stateFields[5].managedFieldNo;
//        }
//        return count;
//    }

        buf.append("\n\tpublic final int replaceSCOFields(PersistenceCapable owner, VersantPMProxy sm, int absFields[]) {\n");
        buf.append("\t\tint count = 0;\n");
        int numSCO = cmd.scoFieldNos.length;
        if (numSCO > 0) {
            for (int i = 0; i < numSCO; i++) {
                int fieldNo = cmd.scoFieldNos[i];
                boolean isDate = isDate(fieldNo);
                buf.append("\t\tif (_" + fieldNo + " != null) {\n");
                buf.append("\t\t\t_"+ fieldNo +" = "+(isDate ? "(java.util.Date)":"")+"cmd.stateFields["+ fieldNo +"].createSCO(sm, sm.getVersantStateManager(owner), cmd.stateFields[" + fieldNo + "], owner, _" + fieldNo + ");\n");
                buf.append("\t\t\tabsFields[count++] = cmd.stateFields["+ fieldNo +"].managedFieldNo;\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn count;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addUnmanageSCOFields() {
        StringBuffer buf = new StringBuffer();
//    public final void unmanageSCOFields() {
//        if (_2 instanceof VersantSimpleSCO) {
//            ((VersantSimpleSCO) _2).makeTransient();
//        }
//        if (_5 instanceof VersantSimpleSCO) {
//            ((VersantSimpleSCO) _5).makeTransient();
//        }
//    }
        buf.append("\n\tpublic final void unmanageSCOFields() {\n");
        int numSCO = cmd.scoFieldNos.length;
        if (numSCO > 0) {
            for (int i = 0; i < numSCO; i++) {
                int fieldNo = cmd.scoFieldNos[i];
                buf.append("\t\tif (_"+ fieldNo +" instanceof com.versant.core.jdo.sco.VersantSimpleSCO) {\n");
                buf.append("\t\t\t((com.versant.core.jdo.sco.VersantSimpleSCO) _"+ fieldNo +").makeTransient();\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addContainsFetchGroup() {
        StringBuffer buf = new StringBuffer();
//    public final boolean containsFetchGroup(FetchGroup fetchGroup) {
//        for (; fetchGroup != null; fetchGroup = fetchGroup.superFetchGroup) {
//            int fgn[] = fetchGroup.stateFieldNos;
//            for (int i = fgn.length - 1; i >= 0; i--) {
//                if (!containsField(fgn[i])) {
//                    return false;
//                }
//            }
//
//        }
//
//        return true;
//    }
        buf.append("\n\tpublic final boolean containsFetchGroup(FetchGroup fetchGroup) {\n");
        buf.append("\t\tfor (; fetchGroup != null; fetchGroup = fetchGroup.superFetchGroup) {\n");
        buf.append("\t\t\tint fgn[] = fetchGroup.stateFieldNos;\n");
        buf.append("\t\t\tfor (int i = fgn.length - 1; i >= 0; i--) {\n");
        buf.append("\t\t\t\tif (!containsField(fgn[i])) {\n");
        buf.append("\t\t\t\t\treturn false;\n");
        buf.append("\t\t\t\t}\n");
        buf.append("\t\t\t}\n\n");
        buf.append("\t\t}\n\n");
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addClearCollectionFields() {
        StringBuffer buf = new StringBuffer();
//    public final void clearCollectionFields() {
//        filled0 = filled0 & -907265;
//        resolved0 = resolved0 & -907265;
//        _11 = null;
//        _12 = null;
//        _14 = null;
//    }
        buf.append("\n\tpublic final void clearCollectionFields() {\n");
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            int category = cmd.stateFields[i].category;
            if (category == MDStatics.CATEGORY_COLLECTION ||
                    category == MDStatics.CATEGORY_MAP ||
                    category == MDStatics.CATEGORY_ARRAY) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }
        int maskLenght = masks.length;
        for (int i = 0; i < maskLenght; i++) {
            if (masks[i] != 0) {
                buf.append("\t\tfilled"+i+" = filled"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
                buf.append("\t\tresolved"+i+" = resolved"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
            }
        }
        for (int i = 0; i < num; i++) {
            int category = cmd.stateFields[i].category;
            if (category == MDStatics.CATEGORY_COLLECTION ||
                    category == MDStatics.CATEGORY_MAP ||
                    category == MDStatics.CATEGORY_ARRAY) {
                FieldMetaData data = cmd.stateFields[i];
                buf.append("\t\t_"+ data.stateFieldNo +" = null;\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }




    protected void addToString() {
        StringBuffer buf = new StringBuffer();

//    public String toString() {
//        StringBuffer s = new StringBuffer("\n|---------------------------------------------------------------------------------\n| HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_ClassWithSerializableField@");
//        s.append(Integer.toHexString(System.identityHashCode(this)));
//        s.append("\n|---------------------------------------------------------------------------------");
//        s.append("\n| INDEX | FILLED | DIRTY | RESOLVE | NAME       | VALUE \n");
//        s.append("|---------------------------------------------------------------------------------");
//        s.append("\n|   0   ");
//        s.append((filled0 & 1) == 0 ? "|   0    " : "|   1    ");
//        s.append((dirtyFields0 & 1) == 0 ? "|   0   " : "|   1   ");
//        s.append((resolved0 & 1) == 0 ? "|    0    " : "|    1    ");
//        s.append("| sr         | ");
//        s.append(_0);
//        s.append("\n|   1   ");
//        s.append((filled0 & 2) == 0 ? "|   0    " : "|   1    ");
//        s.append((dirtyFields0 & 2) == 0 ? "|   0   " : "|   1   ");
//        s.append((resolved0 & 2) == 0 ? "|    0    " : "|    1    ");
//        s.append("| val        | ");
//        s.append(_1);
//        s.append("\n|   2   ");
//        s.append((filled0 & 4) == 0 ? "|   0    " : "|   1    ");
//        s.append((dirtyFields0 & 4) == 0 ? "|   0   " : "|   1   ");
//        s.append((resolved0 & 4) == 0 ? "|    0    " : "|    1    ");
//        s.append("| jdoVersion | ");
//        s.append(_2);
//        s.append("\n|---------------------------------------------------------------------------------");
//        return s.toString();
//    }

        buf.append("\n\tpublic String toString() {\n");
        buf.append("\t\tStringBuffer s = new StringBuffer(\"\\n|---------------------------------------------------------------------------------\\n| " + className + "@\");\n");
        buf.append("\t\ttry{\n");
        buf.append("\t\t\ts.append(Integer.toHexString(System.identityHashCode(this)));\n");
        buf.append("\t\t\ts.append(\"\\n|---------------------------------------------------------------------------------\");\n");

        int maxNameLenght = 0;
        for (int j = 0; j < cmd.stateFields.length; j++) {
            if (cmd.stateFields[j].name.length() > maxNameLenght) {
                maxNameLenght = cmd.stateFields[j].name.length();
            }
        }
        buf.append("\t\t\ts.append(\"\\n| INDEX | FILLED | DIRTY | RESOLVE | " + getBufName("NAME",
                maxNameLenght) + " | VALUE \\n\");\n");
        buf.append("\t\t\ts.append(\"|---------------------------------------------------------------------------------\");\n");
        int totalNum = cmd.stateFields.length;
        for (int i = 0; i < totalNum; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            int fieldNum = getFieldNo(fmd);
            int index = getFieldIndex(fieldNum);
            String fieldRealName = fmd.name;
            String filledFieldName = getFilledFieldName(fieldNum);
            String resolvedFieldName = getResolvedFieldName(fieldNum);
            String dirtyFieldName = getDirtyFieldName(fieldNum);
            String fieldName = getFieldName(fieldNum);
            /* INDEX */
            if (fieldNum < 10) {
                buf.append("\t\t\ts.append(\"\\n|   " + fieldNum + "   \");\n");
            } else if (fieldNum < 100) {
                buf.append("\t\t\ts.append(\"\\n|   " + fieldNum + "  \");\n");
            } else {
                buf.append("\t\t\ts.append(\"\\n|   " + fieldNum + " \");\n");
            }

            /* FILLED */
            buf.append("\t\t\ts.append((" + filledFieldName + " & " + index + ") == 0 ? \"|   0    \" : \"|   1    \");\n");
            /* DIRTY */
            buf.append("\t\t\ts.append((" + dirtyFieldName + " & " + index + ") == 0 ? \"|   0    \" : \"|   1    \");\n");
            /* RESOLVE */
            buf.append("\t\t\ts.append((" + resolvedFieldName + " & " + index + ") == 0 ? \"|   0    \" : \"|   1    \");\n");
            /* NAME */
            buf.append("\t\t\ts.append(\"| " + getBufName(fieldRealName, maxNameLenght) + " | \");\n");
            /* VALUE */
            if (isOID(fmd)) {
                buf.append("\t\t\ts.append(("+ fieldName +" instanceof OID) ? ((Object) (((OID)"+ fieldName +").toSString())) : "+ fieldName +");\n");
            } else {
                buf.append("\t\t\ts.append(" + fieldName + ");\n");
            }
        }
        buf.append("\t\t\ts.append(\"\\n|---------------------------------------------------------------------------------\");\n");
        buf.append("\t\t} catch(java.lang.Exception e){\n");
        buf.append("\t\t\ts = new StringBuffer(e.getMessage());\n");
        buf.append("\t\t}\n");
        buf.append("\t\treturn s.toString();\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected String getBufName(String name, int maxLenght) {
        int bufLenght = maxLenght - name.length();
        StringBuffer buf = new StringBuffer(name);
        for (int i = 0; i < bufLenght; i++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    protected void addClearTransactionNonPersistentFields() {
        StringBuffer buf = new StringBuffer();
//        public final void clearTransactionNonPersistentFields() {
//            dirtyFields0 = dirtyFields0 & -1048577;
//        }
        buf.append("\n\tpublic final void clearTransactionNonPersistentFields() {\n");
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.txFieldNos.length;
        for (int i = 0; i < num; i++) {
            int fieldNum = cmd.txFieldNos[i];
            masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
        }
        int maskLenght = masks.length;
        for (int i = 0; i < maskLenght; i++) {
            if (masks[i] != 0) {
                buf.append("\t\tdirtyFields"+i+" = dirtyFields"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addCompareToPass1() {
        StringBuffer buf = new StringBuffer();
//    public final int compareToPass1(State state) {
//        HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff s = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) state;
//        int ans = ((filled0 & -2) - (s.filled0 & -2)) + ((filled1 & -1) - (s.filled1 & -1)) + ((filled2 & 33791) - (s.filled2 & 33791));
//    }


//        buf.append("\n\tpublic final boolean isEmpty() {\n");
//        int num = getNumOfControlFields();
//        for (int i = 0; i < num; i++) {
//            if (i == 0) {
//                buf.append("\t\treturn filled0 == 0");
//            } else {
//                buf.append(" && filled" + i + " == 0");
//            }
//        }
//        buf.append(";\n");
//        buf.append("\t}\n");

        buf.append("\n\tpublic final int compareToPass1(State state) {\n");
        buf.append("\t\t" + className + " s = ("+className+") state;\n");
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            if (cmd.stateFields[i].primaryField) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }
        int maskLenght = masks.length;
        for (int i = 0; i < maskLenght; i++) {
            if (i == 0) {
                buf.append("\t\tint ans = ((filled0 & "+ masks[i] +") - (s.filled0 & " + masks[i] + "))");
            } else {
                buf.append(" + ((filled"+i+" & "+ masks[i] +") - (s.filled"+i+" & "+ masks[i] +"))");
            }

        }
        buf.append(";\n");
        buf.append("\t\treturn ans;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetClassMetaDataJMD() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final ClassMetaData getClassMetaData(ModelMetaData jmd) {\n");
        buf.append("\t\treturn cmd;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetClassMetaData() {
        StringBuffer buf = new StringBuffer();
//        public final ClassMetaData getClassMetaData() {
//            return cmd;
//        }
        buf.append("\n\tpublic final ClassMetaData getClassMetaData() {\n");
        buf.append("\t\treturn cmd;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainFields() {
        StringBuffer buf = new StringBuffer();

//        public final boolean containFields(int fieldNos[]) {
//            int numOfFields = fieldNos.length;
//            for (int i = 0; i < numOfFields; i++) {
//                if (!containsField(fieldNos[i])) {
//                    return false;
//                }
//            }
//            return true;
//        }

//        public final boolean containFields(int fieldNos[]) {
//            int numOfFields = fieldNos.length;
//            for (int i = 0; i < numOfFields; i++) {
//                if ((filled0 & 1 << fieldNos[i]) == 0) {
//                    return false;
//                }
//            }
//            return true;
//        }

        buf.append("\n\tpublic final boolean containFields(int fieldNos[]) {\n");
        buf.append("\t\tint numOfFields = fieldNos.length;\n");
        buf.append("\t\tfor (int i = 0; i < numOfFields; i++) {\n");
        int controlNum = getNumOfControlFields();
        if (controlNum == 1) {
            buf.append("\t\t\tif ((filled0 & 1 << fieldNos[i]) == 0) {\n");

        } else {  // if there is multiple control field we just call the method containsField, else it get hairy.
            buf.append("\t\t\tif (!containsField(fieldNos[i])) {\n");
        }
        buf.append("\t\t\t\treturn false;\n");
        buf.append("\t\t\t}\n");
        buf.append("\t\t}\n\n");
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }



    protected void addContainFieldsAbs() {
        StringBuffer buf = new StringBuffer();
//    public final boolean containFieldsAbs(int absFieldNos[]) {
//        int[] stateFieldNos = new int[absFieldNos.length];
//        for (int i = 0; i < absFieldNos.length; i++) {
//            stateFieldNos[i] = cmd.managedFields[absFieldNos[i]].stateFieldNo;
//        }
//        return containFields(stateFieldNos);
//    }
        buf.append("\n\tpublic final boolean containFieldsAbs(int absFieldNos[]) {\n");
        buf.append("\t\tint[] stateFieldNos = new int[absFieldNos.length];\n");
        buf.append("\t\tfor (int i = 0; i < absFieldNos.length; i++) {\n");
        buf.append("\t\t\tstateFieldNos[i] = cmd.managedFields[absFieldNos[i]].stateFieldNo;\n");
        buf.append("\t\t}\n");
        buf.append("\t\treturn containFields(stateFieldNos);\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addIsEmpty() {
        StringBuffer buf = new StringBuffer();

//        public final boolean isEmpty() {
//            return filled0 == 0 && filled1 == 0 && filled2 == 0;
//        }

        buf.append("\n\tpublic final boolean isEmpty() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\treturn filled0 == 0");
            } else {
                buf.append(" && filled" + i + " == 0");
            }
        }
        buf.append(";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsPass2Fields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean containsPass2Fields() {\n");
        boolean doMask = false;
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            if (cmd.stateFields[i].secondaryField) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                doMask = true;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }
        if (!doMask) {
            buf.append("\t\treturn false;\n");
        } else {
            for (int i = 0; i < masks.length; i++) {
                buf.append("\t\tif ((filled" + i + " & " + masks[i] + ") != 0) {\n");
                buf.append("\t\t\treturn true;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsPass1Fields() {
        StringBuffer buf = new StringBuffer();
//    public final boolean containsPass1Fields() {
//        if ((filled0 & -2) != 0) {
//            return true;
//        }
//        if ((filled1 & -1) != 0) {
//            return true;
//        }
//        return (filled2 & 33791) != 0;
//    }
        buf.append("\n\tpublic final boolean containsPass1Fields() {\n");
        boolean doMask = false;
        int[] masks = new int[getNumOfControlFields()];
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            if (cmd.stateFields[i].primaryField) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                doMask = true;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }
        if (!doMask) {
            buf.append("\t\treturn false;\n");
        } else {
            for (int i = 0; i < masks.length; i++) {
                buf.append("\t\tif ((filled"+i+" & "+ masks[i] +") != 0) {\n");
                buf.append("\t\t\treturn true;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetPass1FieldNos() {
        StringBuffer buf = new StringBuffer();
//    public final int getPass1FieldNos(int buf[]) {
//        int c = 0;
//        int filled = filled0;
//        if ((filled & 2) != 0) {
//            buf[c++] = 1;
//        }
//        if ((filled & -2147483648) != 0) {
//            buf[c++] = 31;
//        }
//        filled = filled1;
//        if ((filled & 1) != 0) {
//            buf[c++] = 32;
//        }
//        if ((filled & 32768) != 0) {
//            buf[c++] = 79;
//        }
//        return c;
//    }

        buf.append("\n\tpublic final int getPass1FieldNos(int buf[]) {\n");
        buf.append("\t\tint c = 0;\n");
        boolean first = true;         // flag
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            List fields = getPass1Fields(i);
            if (fields.isEmpty()) {
                continue;
            }
            if (first) {
                buf.append("\t\tint filled = filled"+i+";\n");
                first = false;
            } else {
                buf.append("\t\tfilled = filled"+i+";\n");
            }
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                buf.append("\t\t\tbuf[c++] = "+ fieldNo +";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn c;\n");
        buf.append("\t\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetPass1FieldRefFieldNosWithNewOids() {
        StringBuffer buf = new StringBuffer();
//    public final int getPass1FieldNos(int buf[]) {
//        int c = 0;
//        int filled = filled0;
//        if ((filled & 2) != 0) {
//            buf[c++] = 1;
//        }
//        if ((filled & -2147483648) != 0) {
//            buf[c++] = 31;
//        }
//        filled = filled1;
//        if ((filled & 1) != 0) {
//            buf[c++] = 32;
//        }
//        if ((filled & 32768) != 0) {
//            buf[c++] = 79;
//        }
//        return c;
//    }

        buf.append("\n\tpublic final int getPass1FieldRefFieldNosWithNewOids(int buf[]) {\n");
        buf.append("\t\tint c = 0;\n");
        boolean first = true;         // flag
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            List fields = getPass1FieldAndRefOrPolyRefFields(i);
            if (fields.isEmpty()) {
                continue;
            }
            if (first) {
                buf.append("\t\tint filled = filled" + i + ";\n");
                first = false;
            } else {
                buf.append("\t\tfilled = filled" + i + ";\n");
            }
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif ((filled & " + getFieldIndex(fieldNo) + ") != 0) {\n");
                buf.append("\t\t\tbuf[c++] = " + fieldNo + ";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn c;\n");
        buf.append("\t\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetPass2FieldNos() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final int getPass2FieldNos(int buf[]) {\n");
        buf.append("\t\tint c = 0;\n");
        boolean first = true;         // flag
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            List fields = getPass2Fields(i);
            if (fields.isEmpty()) {
                continue;
            }
            if (first) {
                buf.append("\t\tint filled = filled" + i + ";\n");
                first = false;
            } else {
                buf.append("\t\tfilled = filled" + i + ";\n");
            }
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif ((filled & " + getFieldIndex(fieldNo) + ") != 0) {\n");
                buf.append("\t\t\tbuf[c++] = " + fieldNo + ";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn c;\n");
        buf.append("\t\t}\n");
        spec.addMethod(buf.toString());
    }


    protected void addIsHollow() {
        StringBuffer buf = new StringBuffer();
//    public final boolean isHollow() {
//        return filled0 == 0 && filled1 == 0 && filled2 == 0;
//    }

        buf.append("\n\tpublic final boolean isHollow() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\treturn filled0 == 0");
            } else {
                buf.append(" && filled"+i+" == 0");
            }
        }
        buf.append(";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetClassMetaData() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void setClassMetaData(ClassMetaData classmetadata) {\n\t}\n");
        spec.addMethod(buf.toString());
    }



    protected void addReplaceNewObjectOIDs() {
        StringBuffer buf = new StringBuffer();
//    public final void replaceNewObjectOIDs(int fieldNos[], int fieldNosLength) {
//        for (int i = 0; i < fieldNosLength; i++) {
//            switch (fieldNos[i]) {
//                case 0: // '\0'
//                    if (_0 instanceof NewObjectOID) {
//                      if (((com.versant.core.common.NewObjectOID) _1).realOID == null) {
//                          containsUnResolvedNewOids = true;
//                      } else {
//                          _1 = ((com.versant.core.common.NewObjectOID) _1).realOID;
//                      }
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
        buf.append("\n\tpublic final boolean replaceNewObjectOIDs(int fieldNos[], int fieldNosLength) {\n");
        List oidList = getOIDsFieldsMetaData();
        if (oidList.isEmpty()) {
            buf.append("\t\treturn false;\n");
            buf.append("\t}\n");
        } else {
            buf.append("\t\tboolean containsUnResolvedNewOids = false;\n");
            buf.append("\t\tfor (int i = 0; i < fieldNosLength; i++) {\n");
            buf.append("\t\t\tswitch (fieldNos[i]) {\n");
            for (Iterator iter = oidList.iterator(); iter.hasNext();) {
                FieldMetaData field = (FieldMetaData) iter.next();
                int fieldNo = field.stateFieldNo;
                buf.append("\t\t\t\tcase "+ fieldNo +":\n");
                buf.append("\t\t\t\t\tif (_" + fieldNo + " instanceof com.versant.core.common.NewObjectOID) {\n");
                buf.append("\t\t\t\t\t\tif (((com.versant.core.common.NewObjectOID) _" + fieldNo + ").realOID == null) {\n");
                buf.append("\t\t\t\t\t\t\tcontainsUnResolvedNewOids = true;\n");
                buf.append("\t\t\t\t\t\t} else {\n");
                buf.append("\t\t\t\t\t\t\t_"+ fieldNo +" = ((com.versant.core.common.NewObjectOID) _"+ fieldNo +").realOID;\n");
                buf.append("\t\t\t\t\t\t}\n");
                buf.append("\t\t\t\t\t}\n");
                buf.append("\t\t\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\t\t\tdefault:\n");
            buf.append("\t\t\t\t\tbreak;\n\n");
            buf.append("\t\t\t}\n");
            buf.append("\t\t}\n");
            buf.append("\t\treturn containsUnResolvedNewOids;\n");
            buf.append("\t}\n");
        }
        spec.addMethod(buf.toString());
    }

    protected void addGetDirtyState() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic boolean fillToStoreState(State stateToStore, PersistenceContext pm, VersantStateManager sm) {\n");
        if (!cmd.storeAllFields) {
            buf.append("\t\t" + className + " state = (" + className + ")stateToStore;\n");
            buf.append("\t\tif (isDirty()) {\n");
            buf.append("\t\t\tboolean prepare = false;\n");
            int num = getNumOfControlFields();
            for (int i = 0; i < num; i++) {
                if (i == 0) {
                    buf.append("\t\t\tint toCheck = state.filled" + i + " = dirtyFields" + i + ";\n");
                } else {
                    buf.append("\t\t\ttoCheck = state.filled" + i + " = dirtyFields" + i + ";\n");
                }
                List fields = getFields(i);
                for (Iterator iter = fields.iterator(); iter.hasNext();) {
                    FieldMetaData fmd = (FieldMetaData) iter.next();
                    int fieldNo = fmd.stateFieldNo;
                    buf.append("\t\t\tif ((toCheck & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                    if (isOID(fmd) || isCollection(fmd) || isMap(fmd) || isPolyRef(fmd) || isPCArray(fmd) || isExternalized(fmd)) {
                        buf.append("\t\t\t\tprepare = true;\n");
                    } else if (fmd.type.equals(java.util.Date.class)) {
                        buf.append("\t\t\t\tprepare = true;\n");
                    }
                    buf.append("\t\t\t\tstate._"+ fieldNo +" = _"+ fieldNo +";\n");
                    buf.append("\t\t\t}\n");
                }
            }
            buf.append("\t\t\tif (prepare) {\n");
            buf.append("\t\t\t\tstate.prepare(pm, sm, this);\n");
            buf.append("\t\t\t}\n");
            buf.append("\t\t} else {\n");
            buf.append("\t\t\tstate = null;\n");
            buf.append("\t\t}\n");
            buf.append("\t\treturn state != null;\n");
        } else {    // vds store
            buf.append("\t\t" + className + " state = (" + className + ")stateToStore;\n");
            buf.append("\t\tif (isDirty()) {\n");
            int num = getNumOfControlFields();
            boolean toCheck_Start = true;
            for (int i = 0; i < num; i++) {
                int hash = getPrimaryHash(i);
                if (hash == 0) {
                    continue;
                }
                if (toCheck_Start){
                    buf.append("\t\t\tint toCheck = state.filled" + i + " |= filled" + i + " & " + hash + ";\n");
                    toCheck_Start = false;
                } else {
                    buf.append("\t\t\ttoCheck = state.filled" + i + " |= filled" + i + " & " + hash + ";\n");
                }
                List fields = getPrimaryFields(i);
                for (Iterator iter = fields.iterator(); iter.hasNext();) {
                    FieldMetaData fmd = (FieldMetaData) iter.next();
                    int fieldNo = fmd.stateFieldNo;
                    buf.append("\t\t\tif ((toCheck & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                    buf.append("\t\t\t\tstate._"+ fieldNo +" = _"+ fieldNo +";\n");
                    buf.append("\t\t\t}\n");
                }
            }

            for (int i = 0; i < num; i++) {
                int hash = getSecondaryHash(i);
                if (hash == 0) {
                    continue;
                }
                if (toCheck_Start) {
                    buf.append("\t\t\tint toCheck = state.filled" + i + " |= dirtyFields" + i + " & " + hash + ";\n");
                    toCheck_Start = false;
                } else {
                    buf.append("\t\t\ttoCheck = state.filled" + i + " |= dirtyFields" + i + " & " + hash + ";\n");
                }
                List fields = getSecondaryFields(i);
                for (Iterator iter = fields.iterator(); iter.hasNext();) {
                    FieldMetaData fmd = (FieldMetaData) iter.next();
                    int fieldNo = fmd.stateFieldNo;
                    buf.append("\t\t\tif ((toCheck & " + getFieldIndex(fieldNo) + ") != 0) {\n");
                    buf.append("\t\t\t\tstate._" + fieldNo + " = _" + fieldNo + ";\n");
                    buf.append("\t\t\t}\n");
                }
            }
            buf.append("\t\t\tstate.prepare(pm, sm, this);\n");
            buf.append("\t\t} else {\n");
            buf.append("\t\t\tstate = null;\n");
            buf.append("\t\t}\n");
            buf.append("\t\treturn state != null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addFindDirectEdges() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void findDirectEdges(OIDGraph graph, "+ INT_ARRAY +" edges) {\n");
        List list = getDirectRefFieldsMetaData();
        if (list.isEmpty()) {
            buf.append("\t}\n");
        } else {
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                iter.next();
            }
            while (iter.hasPrevious()) {
                FieldMetaData field = (FieldMetaData) iter.previous();
                int fieldNum = field.stateFieldNo;
                int index = getFieldIndex(fieldNum);
                String filledName = getFilledFieldName(fieldNum);

                buf.append("\t\tif (("+ filledName +" & "+index+") != 0) {\n");
                buf.append("\t\t\tfindDirectEdges(graph, cmd, "+ fieldNum+", this, edges);\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t}\n");
        }
        spec.addMethod(buf.toString());
    }

    protected void addHasSameNullFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean hasSameNullFields(State state, State mask) {\n");
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addIsNull() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean isNull(int fieldNo) {\n");
        List fields = getAllRealObjectFields();
        if (fields.isEmpty()) {
            buf.append("\t\treturn false;\n");
        } else if (fields.size() == 1) {
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                int fieldNo = getFieldNo((FieldMetaData) iter.next());
                buf.append("\t\treturn _"+ fieldNo +" == null;\n");
            }
        } else {
            buf.append("\t\tswitch (fieldNo) {\n");
            for (Iterator fieldIter = fields.iterator(); fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\t\tcase " + fieldNo + ":\n");
                buf.append("\t\t\t\treturn _" + fieldNo + " == null;\n\n");
            }
            buf.append("\t\t\tdefault:\n");
            buf.append("\t\t\t\treturn false;\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected FetchGroup findFirstRefFG(ClassMetaData cmd) {
        if (cmd.refFetchGroup != null) return cmd.refFetchGroup;
        if (cmd.refFetchGroup == null && cmd.pcSuperMetaData == null) return null;
        return findFirstRefFG(cmd.pcSuperMetaData);
    }

    protected void addAddRefs() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void addRefs(VersantPersistenceManagerImp pm, PCStateMan sm) {\n");
        FetchGroup fg = findFirstRefFG(cmd);//cmd.refFetchGroup;
        // do if (isDirty()){ only do this if refFetchGroup != null
        if (fg != null) {
            buf.append("\t\tif (isDirty()) {\n");
            while (fg != null) {
                int[] fieldNos = fg.stateFieldNos;
                int fieldNum = 0;
                FieldMetaData fmd = null;
                // the fields MUST be processed in ascending order or embedded
                // reference fields containing other embedded reference fields
                // will not work as the embedded-embedded field will be
                // processed prior to being filled
                for (int i = 0; i < fieldNos.length; i++) {
                    fieldNum = fieldNos[i];
                    fmd = cmd.stateFields[fieldNum];
                    if (fmd.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                        continue;
                    }
                    buf.append("\t\t\tif (("+ getDirtyFieldName(fieldNum) +" & "+ getFieldIndex(fieldNum) +") != 0 && _"+ fieldNum +" != null) {\n");
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_ARRAY:
                            // do StateUtil.doReachable((Object[])data[fieldNo], pm);
                            buf.append("\t\t\t\tStateUtil.doReachable((Object[])_" + fieldNum + ", pm);\n");
                            break;
                        case MDStatics.CATEGORY_COLLECTION:
                            // do StateUtil.doReachable((Collection)data[fieldNo], pm);  java.util.Collection
                            buf.append("\t\t\t\tStateUtil.doReachable((java.util.Collection)_" + fieldNum + ", pm);\n");
                            break;
                        case MDStatics.CATEGORY_MAP:
                            buf.append("\t\t\t\tStateUtil.doReachable((java.util.Map) _"+ fieldNum +", pm, cmd.stateFields["+ fieldNum +"]);\n");
                            break;
                        case MDStatics.CATEGORY_REF:
                        case MDStatics.CATEGORY_POLYREF:
                            if (fmd.embedded) {
                                buf.append(
                                        "\t\t\t\t\tStateUtil.doReachableEmbeddedReference(\n" +
                                        "\t\t\t\t\t\t(PersistenceCapable)_"+ fieldNum +", pm, sm, cmd.stateFields["+ fieldNum +"]);\n");
                            } else {
                                buf.append("\t\t\t\t\tStateUtil.doReachable((PersistenceCapable)_"+ fieldNum +", pm);\n");
                            }
                            break;
                        default:
                            fmd.dump();
                            throw BindingSupportImpl.getInstance().internal("className = " + className);
                    }
                    buf.append("\t\t\t}\n");
                }
                fg = fg.superFetchGroup;
            }
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addRetrieve() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void retrieve(VersantPersistenceManagerImp sm) {\n");
        FetchGroup fg = cmd.refFetchGroup;
        if (fg != null) {
            while (fg != null) {
                int[] fieldNos = fg.stateFieldNos;
                int fieldNum = 0;
                FieldMetaData fmd = null;
                for (int i = fieldNos.length - 1; i >= 0; i--) {
                    fieldNum = fieldNos[i];
                    fmd = cmd.stateFields[fieldNum];
                    if (fmd.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                        continue;
                    }
                    buf.append("\t\tif (_"+ fieldNum +" != null) {\n");
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_ARRAY:
                            buf.append("\t\t\tsm.retrieveAllImp((Object[])_"+ fieldNum +");\n");
                            break;
                        case MDStatics.CATEGORY_COLLECTION:
                            buf.append("\t\t\tsm.retrieveAllImp((java.util.Collection)_" + fieldNum + ");\n");
                            break;
                        case MDStatics.CATEGORY_MAP:
                            buf.append("\t\t\tStateUtil.retrieve((java.util.Map) _"+ fieldNum +", sm, cmd.stateFields["+ fieldNum +"]);\n");
                            break;
                        case MDStatics.CATEGORY_REF:
                        case MDStatics.CATEGORY_POLYREF:
                            buf.append("\t\t\tsm.retrieveImp(_" + fieldNum + ");\n");
                            break;

                        default:
                            fmd.dump();
                            throw BindingSupportImpl.getInstance().internal("className = " + className);
                    }
                    buf.append("\t\t}\n");
                }
                fg = fg.superFetchGroup;
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected void addGetResolvableObjectField() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic Object getObjectField(int stateFieldNo, PersistenceCapable owningPC, VersantPMProxy pm, OID oid) {\n");
        List fields = getObjectFieldsMetaData();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif (stateFieldNo == " + fieldNo + "){\n");
                addGetResolvableObjectFieldImp(info, buf, fieldNo);
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch (stateFieldNo) {\n");
            for (Iterator fieldIter = fields.iterator();fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\t\tcase "+ fieldNo +":\n");
                addGetResolvableObjectFieldImp(info, buf, fieldNo);
            }
            buf.append("\t\t\tdefault :\n");
            buf.append("\t\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetResolvableObjectFieldImp(FieldMetaData info, StringBuffer buf, int fieldNo) {
        if (cmd.isEmbeddedRef(fieldNo)) {
            buf.append("\t\t\t\tif (_" + fieldNo +" == null && (" + getResolvedFieldName(fieldNo) + " & " + getFieldIndex(fieldNo) + ") == 0) {\n");
            buf.append("\t\t\t\t\t_" + fieldNo +
                    " = pm.getObjectByIdForState(null, " + fieldNo + ", " + cmd.index + ", oid);\n");
            buf.append("\t\t\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
            buf.append("\t\t\t\t};\n");
            buf.append("\t\t\t\treturn _" + fieldNo + ";\n");
            return;
        }
        boolean done = false;
        if (info.scoField) {
            if (info.category == MDStatics.CATEGORY_ARRAY) {
                if (info.isElementTypePC()) {
                    buf.append("\t\t\tif (_" + fieldNo + " != null) {\n");
                    buf.append("\t\t\t\tif ((" + getResolvedFieldName(fieldNo) + " & " + getFieldIndex(fieldNo) + ") == 0) {\n");
                    buf.append("\t\t\t\t\t_"+ fieldNo +" = resolveArrayOIDs((Object[]) _"+ fieldNo +", pm, cmd.stateFields["+ fieldNo +"].elementType);\n");
                    done = true;
                } else {
                    String className = (String) primativeToClass.get(info.componentType.getName());
                    if (className == null) {
                        className = info.componentType.getName()+".class";
                    }
                    buf.append("\t\t\tif (_" + fieldNo + " != null) {\n");
                    buf.append("\t\t\t\tif (java.lang.reflect.Array.getLength(_"+ fieldNo+") == 0 && _"+ fieldNo+".getClass().getComponentType() != "+ className +") {\n");
                    buf.append("\t\t\t\t\t_" + fieldNo + " = new "+ info.componentType.getName() +"[]{};\n");
                    done = true;
                }
            } else {
                boolean isDate = info.typeCode == MDStatics.DATE;
                buf.append("\t\t\tif (_" + fieldNo + " != null) {\n");
                buf.append("\t\t\t\tif ((" + getResolvedFieldName(fieldNo) + " & " + getFieldIndex(fieldNo) + ") == 0) {\n");
                buf.append("\t\t\t\t\t_"+ fieldNo +" = "+ (isDate ? "(java.util.Date)" : "") +"cmd.stateFields["+ fieldNo +"].createSCO(pm, pm.getVersantStateManager(owningPC), cmd.stateFields["+ fieldNo +"], owningPC, _"+ fieldNo +");\n");
                done = true;
            }
        } else {
            int cat = info.category;
            if (cat == MDStatics.CATEGORY_EXTERNALIZED) {
                buf.append("\t\t\tif (_" + fieldNo + " != null) {\n");
                buf.append("\t\t\t\tif ((" + getResolvedFieldName(fieldNo) + " & " + getFieldIndex(fieldNo) + ") == 0) {\n");
                buf.append("\t\t\t\t\t_"+ fieldNo +" = cmd.stateFields["+ fieldNo +"].externalizer.fromExternalForm(pm, _"+ fieldNo +");\n");
                done = true;
            } else if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) {
                buf.append("\t\t\tif (_"+ fieldNo +" != null) {\n");
                buf.append("\t\t\t\tif (("+ getResolvedFieldName(fieldNo) +" & "+ getFieldIndex(fieldNo) +") == 0) {\n");
                buf.append("\t\t\t\t\t_"+ fieldNo +" = pm.getObjectByIdForState((OID)_"+ fieldNo +", "+ fieldNo +", "+ cmd.index +", oid);\n");
                done = true;
            }
            //ignore else
        }

        if (done) {
            buf.append("\t\t\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
            buf.append("\t\t\t\t}\n");
            buf.append("\t\t\t\treturn _" + fieldNo + ";\n");
            buf.append("\t\t\t} else {\n");
            buf.append("\t\t\t\treturn null;\n");
            buf.append("\t\t\t}\n");
        } else {
            buf.append("\t\t\t"+ getResolvedFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");
            buf.append("\t\t\treturn _"+ fieldNo +";\n");
        }
    }

    protected void addGetResolvableObjectFieldAbs() {
        StringBuffer buf = new StringBuffer();
//    public final Object getObjectFieldAbs(int absFieldNo, PersistenceCapable owningPC, VersantPersistenceManagerImp sm, OID oid) {
//        return getObjectField(cmd.managedFields[absFieldNo].stateFieldNo, owningPC, sm, oid);
//    }
        buf.append("\n\tpublic final Object getObjectFieldAbs(int absFieldNo, PersistenceCapable owningPC, VersantPMProxy sm, OID oid) {\n");
        buf.append("\t\treturn getObjectField(cmd.managedFields[absFieldNo].stateFieldNo, owningPC, sm, oid);\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected boolean isOID(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_REF) {
            return true;
        }
        return false;
    }

    protected boolean isObject(FieldMetaData fmd) {
        return isOID(fmd) || isCollection(fmd) || isMap(fmd) || isArray(fmd)
                || isPolyRef(fmd) || isExternalized(fmd);
    }

    protected boolean isExternalized(FieldMetaData fmd) {
        if (fmd.category == MDStatics.CATEGORY_EXTERNALIZED) {
            return true;
        }
        return false;
    }


    protected void addGetInternalObjectField() {
        StringBuffer buf = new StringBuffer();

        /*public final Object getInternalObjectField(int field) {
        if (field == 2) {
        return _2;
        } else {
        throw new JDOFatalInternalException("The specified Object field was not found.");
        }
        }*/

        buf.append("\n\tpublic final Object getInternalObjectField(int field) {\n");
        List fields = getObjectFieldsMetaData();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                int fieldNo = getFieldNo((FieldMetaData) singelIter.next());
                buf.append("\t\tif (field == "+ fieldNo +") {\n");
                buf.append("\t\t\treturn _"+fieldNo+";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch (field) {\n");
            for (Iterator fieldIter = fields.iterator();fieldIter.hasNext();) {
                int fieldNo = getFieldNo((FieldMetaData) fieldIter.next());
                buf.append("\t\t\tcase "+ fieldNo +":\n");
                buf.append("\t\t\t\treturn _"+ fieldNo +";\n\n");
            }
            // Do default
            buf.append("\t\t\tdefault:\n");
            buf.append("\t\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }



    protected void addGetInternalObjectFieldAbs() {
        StringBuffer buf = new StringBuffer();

//    public final Object getInternalObjectFieldAbs(int absField) {
//        switch (absField) {
//            case 0: // '\0'
//                return _0;
//            case 4: // '\004'
//                return _5;
//            default:
//                throw new JDOFatalInternalException("The specified Object field was not found.");
//        }
//    }

        buf.append("\n\tpublic final Object getInternalObjectFieldAbs(int absField) {\n");
        List fields = getObjectFieldsMetaDataAbs();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif (absField == " + getAbsFieldNo(info) + ") {\n");
                buf.append("\t\t\treturn _" + fieldNo + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch (absField) {\n");
            for (Iterator fieldIter = fields.iterator(); fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\t\tcase " + getAbsFieldNo(info) + ":\n");
                buf.append("\t\t\t\treturn _" + fieldNo + ";\n\n");
            }
            // Do default
            buf.append("\t\t\tdefault:\n");
            buf.append("\t\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsApplicationIdentityFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean containsApplicationIdentityFields() {\n");
        buf.append("\t\treturn false;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected abstract void addContainsValidAppIdFields();

    protected void addUpdateFrom() {
        StringBuffer buf = new StringBuffer();
//    public final void updateFrom(State state) {
//        HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff other = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) state;
//        int otherFilled = other.filled0;
//        filled0 |= otherFilled;
//        if ((otherFilled & 1) != 0) {
//            _0 = other._0;
//        }
//        if ((otherFilled & -2147483648) != 0) {
//            _31 = other._31;
//        }
//        otherFilled = other.filled1;
//        filled1 |= otherFilled;
//        if ((otherFilled & 1) != 0) {
//            _32 = other._32;
//        }
//    }

        buf.append("\n\tpublic final void updateFrom(State state) {\n");
        buf.append("\t\t"+className+" other = ("+ className +") state;\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint otherFilled = other.filled0;\n");
                buf.append("\t\tfilled0 |= otherFilled;\n");
            } else {
                buf.append("\t\totherFilled = other.filled"+i+";\n");
                buf.append("\t\tfilled"+i+" |= otherFilled;\n");
            }
            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif ((otherFilled & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                buf.append("\t\t\t_"+ fieldNo +" = other._"+ fieldNo +";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetCopy() {
        StringBuffer buf = new StringBuffer();

//        public final State getCopy() {
//            HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff copy = new HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff();
//            copy.filled0 = filled0;
//            copy.filled1 = filled1;
//            copy.filled2 = filled2;
//            copy.txId = super.getTxId();
//            copy._0 = _0;
//            copy._1 = _1;
//            copy._2 = _2;
//            copy._3 = _3;
//            copy._4 = _4;
//            copy._5 = _5;
//            copy._6 = _6;
//            return copy;
//        }
//        copy.txId = super.getTxId();
        buf.append("\n\tpublic final State getCopy() {\n");
        buf.append("\t\t"+ className +" copy = new "+ className +"();\n");
        int contNum = getNumOfControlFields();
        for (int i = 0; i < contNum; i++) {
            buf.append("\t\tcopy.filled"+i+" = filled"+i+";\n");
        }
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            buf.append("\t\tcopy._"+ i +" = _"+ i +";\n");
        }
        buf.append("\t\treturn copy;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addPrepare() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void prepare(PersistenceContext pm, VersantStateManager sm, State original) {\n");
        int num = cmd.stateFields.length;
        for (int i = 0; i < num; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            int fieldNum = fmd.stateFieldNo;

            boolean doIt = false;
            if (fmd.scoField) {
                if (fmd.typeCode == MDStatics.DATE) {
                    doIt = true;
                } else {
                    switch (fmd.category) {
                        case MDStatics.CATEGORY_ARRAY:
                            if (fmd.isElementTypePC()) {
                                doIt = true;
                            }
                            break;
                        case MDStatics.CATEGORY_COLLECTION:
                        case MDStatics.CATEGORY_MAP:
                            doIt = true;
                            break;
                        default:
                            throw BindingSupportImpl.getInstance().internal("No logic defined for field type "
                                    + MDStaticUtils.toSimpleName(fmd.typeCode)
                                    + " field name = " + fmd.name);
                    }
                }
            } else {
                int cat = fmd.category;
                if (cat == MDStatics.CATEGORY_EXTERNALIZED) {
                    doIt = true;
                } else if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) {
                    doIt = true;
                }
            }

            if (doIt) {  // We are not interested in any others
                boolean isDate = fmd.typeCode == MDStatics.DATE;
                if (fmd.scoField) {
                    if (isDate) {
                        buf.append("\t\tif (("+ getFilledFieldName(fieldNum) +" & "+ getFieldIndex(fieldNum) +") != 0 && _"+ fieldNum +" != null) {\n");
                        buf.append("\t\t\t_"+ fieldNum +" = StateUtil.getPValueForSCO(_"+ fieldNum +");\n");
                        buf.append("\t\t}\n");
                    } else {
                        switch (fmd.category) {
                            case MDStatics.CATEGORY_ARRAY:
                                buf.append("\t\tif (("+ getFilledFieldName(fieldNum) +" & "+ getFieldIndex(fieldNum) +") != 0 && _"+ fieldNum +" != null && !(_"+ fieldNum +" instanceof OID[])) {\n");
                                buf.append("\t\t\t_"+ fieldNum +" = resolveArrayValues((Object[]) _"+ fieldNum +", pm);\n");
                                buf.append("\t\t}\n");
                                break;
                            case MDStatics.CATEGORY_COLLECTION:
                                buf.append("\t\tif ((" + getFilledFieldName(fieldNum) + " & " + getFieldIndex(fieldNum) + ") != 0 && _" + fieldNum + " != null && !(_" + fieldNum + " instanceof OID[])) {\n");
                                buf.append("\t\t\t_"+ fieldNum +" = StateUtil.getPValueForSCO((java.util.Collection)_"+ fieldNum +", pm, sm, cmd.stateFields["+ fieldNum +"]);\n");
                                buf.append("\t\t}\n");
                                break;
                            case MDStatics.CATEGORY_MAP:
                                buf.append("\t\tif ((" + getFilledFieldName(fieldNum) + " & " + getFieldIndex(fieldNum) + ") != 0 && _" + fieldNum + " != null && !(_" + fieldNum + " instanceof "+ MapEntries.class.getName() +")) {\n");
                                buf.append("\t\t\t_" + fieldNum + " = StateUtil.getPValueForSCO((java.util.Map)_" + fieldNum + ", pm, cmd.stateFields[" + fieldNum + "]);\n");
                                buf.append("\t\t}\n");
                                break;
                            case MDStatics.CATEGORY_TRANSACTIONAL:
                                break;
                            default:
                                throw BindingSupportImpl.getInstance().internal("No logic defined for field type "
                                        + MDStaticUtils.toSimpleName(fmd.typeCode)
                                        + " field name = " + fmd.name);
                        }
                    }
                } else {
                    int cat = fmd.category;
                    if (cat == MDStatics.CATEGORY_EXTERNALIZED) {
                        buf.append("\t\tif (original.isResolvedForClient(" + fieldNum + ") && (" + getFilledFieldName(fieldNum) + " & " + getFieldIndex(fieldNum) + ") != 0 && _" + fieldNum + " != null) {\n");
                        buf.append("\t\t\t_"+ fieldNum +" = cmd.stateFields["+ fieldNum +"].externalizer.toExternalForm(pm.getPersistenceManager() , _"+ fieldNum +");\n");
                        buf.append("\t\t}\n");
                    } else if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) {
                        if (fmd.embedded) {
                            buf.append("\t\t_" + fieldNum + " = null;\n");
                        } else {
                            buf.append("\t\tif ((" + getFilledFieldName(fieldNum) + " & "+ getFieldIndex(fieldNum) +") != 0 && (_"+ fieldNum +" instanceof PersistenceCapable)) {\n");
                            buf.append("\t\t\t_"+ fieldNum +" = StateUtil.getPValueForRef((PersistenceCapable)_"+ fieldNum +", pm);\n");
                            buf.append("\t\t}\n");
                        }
                    }
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyFieldsForOptimisticLocking() {
        StringBuffer buf = new StringBuffer();

//    public final void copyFieldsForOptimisticLocking(State state, VersantPersistenceManagerImp sm) {
//        HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_Person s = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_Person)state;
//        int toCheck = ~filled0 & s.filled0;
//        filled0 |= toCheck & 8388737;
//        if ((toCheck & 1) != 0) {
//            _0 = s._0;
//        }
//        if ((toCheck & 2) != 0 && s._1 != null) {
//            if ((s.resolved0 & 2) != 0) {
//                _1 = sm.getInternalOID((PersistenceCapable)s._1);
//            } else {
//                _1 = s._1;
//            }
//            filled0 |= 2;
//        }
//        if ((toCheck & 4) != 0 && s._2 != null) {
//            if ((s.resolved0 & 4) != 0) {
//                _2 = (Date)s._2.clone();
//            } else {
//                _2 = s._2;
//            }
//            filled0 |= 4;
//        }
//        if ((toCheck & 256) != 0 && s._8 != null) {
//            _8 = s._8;
//            filled0 |= 256;
//        }
//        if ((toCheck & 8388608) != 0) {
//            _23 = s._23;
//        }
//    }
        buf.append("\n\tpublic final void copyFieldsForOptimisticLocking(State state, VersantPersistenceManagerImp sm) {\n");
        buf.append("\t\t"+className+" s = (" + className + ")state;\n");

        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint toCheck = ~filled" + i + " & s.filled" + i + ";\n");
            } else {
                buf.append("\t\ttoCheck = ~filled" + i + " & s.filled" + i + ";\n");
            }
            buf.append("\t\tfilled" + i + " |= toCheck & "+ getPrimativeHashForIndex(i) +";\n");
            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                boolean isPrimative = fmd.type.isPrimitive();
                if (isPrimative) {
                    buf.append("\t\tif ((toCheck & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                    buf.append("\t\t\t_"+ fieldNo +" = s._"+ fieldNo +";\n");
                    buf.append("\t\t}\n");
                } else {
                    buf.append("\t\tif ((toCheck & "+ getFieldIndex(fieldNo) +") != 0 && s._"+ fieldNo +" != null) {\n");
                    if (fmd.typeCode == MDStatics.DATE) {      // its a date
                        buf.append("\t\t\tif ((s."+ getResolvedFieldName(fieldNo) +" & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                        buf.append("\t\t\t\t_"+ fieldNo +" = (java.util.Date)s._"+ fieldNo +".clone();\n");
                        buf.append("\t\t\t} else {\n");
                        buf.append("\t\t\t\t_"+ fieldNo +" = s._" + fieldNo + ";\n");
                        buf.append("\t\t\t}\n");
                        buf.append("\t\t\t"+ getFilledFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");
                    } else if (fmd.category == MDStatics.CATEGORY_REF
                            || fmd.category == MDStatics.CATEGORY_POLYREF) {    // its a oid
                        buf.append("\t\t\tif ((s." + getResolvedFieldName(fieldNo) + " & " + getFieldIndex(fieldNo) + ") != 0) {\n");
                        buf.append("\t\t\t\t_" + fieldNo + " = sm.getInternalOID((PersistenceCapable)s._" + fieldNo + ");\n");
                        buf.append("\t\t\t} else {\n");
                        buf.append("\t\t\t\t_" + fieldNo + " = s._" + fieldNo + ";\n");
                        buf.append("\t\t\t}\n");
                        buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                    } else {     // its a Object
                        buf.append("\t\t\t_" + fieldNo + " = s._" + fieldNo + ";\n");
                        buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                    }
                    buf.append("\t\t}\n");
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetFieldNos() {
        StringBuffer buf = new StringBuffer();
        /*

    public final int getFieldNos(int buf[]) {
        int c = 0;
        int filled = filled0;
        if ((filled & 1) != 0) {
            buf[c++] = 0;
        }
        if ((filled & -2147483648) != 0) {
            buf[c++] = 31;
        }
        filled = filled1;
        if ((filled & 1) != 0) {
            buf[c++] = 32;
        }
        if ((filled & 2) != 0) {
            buf[c++] = 33;
        }
        return c;
    }
*/
        buf.append("\n\tpublic final int getFieldNos(int buf[]) {\n");
        buf.append("\t\tint c = 0;\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint filled = filled0;\n");
            } else {
                buf.append("\t\tfilled = filled"+i+";\n");
            }
            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                buf.append("\t\t\tbuf[c++] = "+ fieldNo +";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn c;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addUpdateNonFilled() {
        StringBuffer buf = new StringBuffer();
//        public final void updateNonFilled(State state) {
//            HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff s = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) state;
//            int toCheck = ~filled0 & s.filled0;
//            filled0 |= toCheck;
//            if ((toCheck & 1) != 0) {
//                _0 = s._0;
//            }
//
//            if ((toCheck & 1073741824) != 0) {
//                _30 = s._30;
//            }
//            if ((toCheck & -2147483648) != 0) {
//                _31 = s._31;
//            }
//            toCheck = ~filled1 & s.filled1;
//            filled1 |= toCheck;
//            if ((toCheck & 1) != 0) {
//                _32 = s._32;
//            }
//            if ((toCheck & 2) != 0) {
//                _33 = s._33;
//            }
//            if ((toCheck & 4) != 0) {
//                _34 = s._34;
//            }
//        }

        buf.append("\n\tpublic final void updateNonFilled(State state) {\n");
        buf.append("\t\t" + className + " s = (" + className + ") state;\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint toCheck = ~filled0 & s.filled0;\n");
                buf.append("\t\tfilled0 |= toCheck;\n");
            } else {
                buf.append("\t\ttoCheck = ~filled"+i+" & s.filled"+i+";\n");
                buf.append("\t\tfilled"+i+" |= toCheck;\n");
            }
            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                String fieldName = getFieldName(fieldNo);
                buf.append("\t\tif ((toCheck & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                buf.append("\t\t\t"+ fieldName +" = s."+ fieldName +";\n");
                buf.append("\t\t}\n");
            }

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyOptimisticLockingField() {
        StringBuffer buf = new StringBuffer();

//        public void copyOptimisticLockingField(State state) {
//            GenericState gState = (GenericState) state;
//            FieldMetaData optimisticLockingField = cmd.optimisticLockingField;
//            if (optimisticLockingField == null) return;
//            if (gState.filled[optimisticLockingField.stateFieldNo]) {
//                data[optimisticLockingField.stateFieldNo] = gState.data[optimisticLockingField.stateFieldNo];
//                filled[optimisticLockingField.stateFieldNo] = true;
//            }
//        }

        buf.append("\n\tpublic final void copyOptimisticLockingField(State state) {\n");
        if (cmd.optimisticLockingField == null){
            buf.append("\t\treturn;\n");
        } else {
            buf.append("\t\t" + className + " gState = (" + className + ")state;\n");
            int fieldNo = cmd.optimisticLockingField.stateFieldNo;
            String filled = getFilledFieldName(fieldNo);
            int index = getFieldIndex(fieldNo);
            buf.append("\t\tif ((gState."+ filled +" & "+ index +") != 0) {\n");
            buf.append("\t\t\t_"+ fieldNo +" = gState._"+ fieldNo +";\n");
            buf.append("\t\t\t"+ filled +" |= "+ index +";\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected void addClearNonFilled() {
        StringBuffer buf = new StringBuffer();
//        public final void clearNonFilled(State state) {
//            HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff s = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) state;
//            int filled = filled0;
//            filled0 = filled & 0 | filled & s.filled0;
//            filled = filled1;
//            filled1 = filled & 0 | filled & s.filled1;
//            filled = filled2;
//            filled2 = filled & 32768 | filled & s.filled2;
//        }

        buf.append("\n\tpublic final void clearNonFilled(State state) {\n");
        buf.append("\t\t"+ className +" s = ("+ className +") state;\n");
        int num = getNumOfControlFields();
        // setup the mask, if a field is = MDStatics.AUTOSET_NO, the index will be 0 else 1.
        int[] masks = new int[num];
        int lenght = cmd.stateFields.length;
        for (int i = 0; i < lenght; i++) {
            if (cmd.stateFields[i].autoSet != MDStatics.AUTOSET_NO) {
                int fieldNum = cmd.stateFields[i].stateFieldNo;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
        }

        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint filled = filled0;\n");
            } else {
                buf.append("\t\tfilled = filled"+i+";\n");
            }
            buf.append("\t\tfilled"+i+" = filled & "+ masks[i] +" | filled & s.filled"+i+";\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected void addMakeClean() {
        StringBuffer buf = new StringBuffer();
//        public final void makeClean() {
//            dirtyFields0 = 0;
//            dirtyFields1 = 0;
//            dirtyFields2 = 0;
//        }
        buf.append("\n\tpublic final void makeClean() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            buf.append("\t\tdirtyFields"+i+" = 0;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addIsResolvedForClient() {
        StringBuffer buf = new StringBuffer();
//    public final boolean isResolvedForClient(int stateFieldNo) {
//        if (stateFieldNo < 32) {
//            return (resolved0 & 1 << stateFieldNo) != 0;
//        }
//        if (stateFieldNo < 64) {
//            return (resolved1 & 1 << stateFieldNo ) != 0;
//        }
//        if (stateFieldNo < 96) {
//            return (resolved2 & 1 << stateFieldNo) != 0;
//        }
//        return false;
//    }
        buf.append("\n\tpublic final boolean isResolvedForClient(int stateFieldNo) {\n");
        int num = getNumOfControlFields();
        if (num == 1) {
            buf.append("\t\treturn (resolved0 & 1 << stateFieldNo) != 0;\n");
        } else {
            for (int i = 0; i < num; i++) {
                buf.append("\t\tif (stateFieldNo < "+ ((32 * i) + 32) +") {\n");
                buf.append("\t\t\treturn (resolved"+i+" & 1 << stateFieldNo) != 0;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsField() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final boolean containsField(int fieldNo) {\n");
        int num = getNumOfControlFields();
        if (num == 1) {
            buf.append("\t\treturn (filled0 & 1 << fieldNo) != 0;\n");
        } else {
            for (int i = 0; i < num; i++) {
                buf.append("\t\tif (fieldNo < "+ ((32 * i) + 32) +") {\n");
                buf.append("\t\t\treturn (filled"+ i +" & 1 << fieldNo) != 0;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addEqualsObject() {
        StringBuffer buf = new StringBuffer();

        /*public final boolean equals (Object object) {
            if (hashCode() != object.hashCode()) {
                return false;
            }
            if (object instanceof HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) {
                HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff other = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff) object;

                if (_1 != other._1) {
                    return false;
                }

                if (_78 != null) {
                    if (!_78.equals(other._78)) {
                        return false;
                    }
                } else if (other._78 != null) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }*/

        buf.append("\n\tpublic final boolean equals(Object object) {\n");
        buf.append("\t\tif (hashCode() != object.hashCode()) {\n");
        buf.append("\t\t\treturn false;\n");
        buf.append("\t\t}\n");
        buf.append("\t\tif (object instanceof "+ className +") {\n");
        buf.append("\t\t\t"+ className +" other = ("+ className +") object;\n");
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData fmd = fields[i];
            Class classType = fmd.type;
            String fieldName = getFieldName(i);
            boolean isPrimitive = classType.isPrimitive();
            if (isPrimitive) {
                buf.append("\t\t\tif ("+ fieldName +" != other."+ fieldName +") {\n");
                buf.append("\t\t\t\treturn false;\n");
                buf.append("\t\t\t}\n");
            } else { // this is a object and it can be null
                buf.append("\t\t\tif (" + fieldName + " != null) {\n");
                buf.append("\t\t\t\tif (!"+ fieldName +".equals(other."+ fieldName +")) {\n");
                buf.append("\t\t\t\t\treturn false;\n");
                buf.append("\t\t\t\t}\n");
                buf.append("\t\t\t} else if (other."+ fieldName +" != null) {\n");
                buf.append("\t\t\t\treturn false;\n");
                buf.append("\t\t\t}\n");
            }

        }
        buf.append("\t\t\treturn true;\n");
        buf.append("\t\t} else {\n");
        buf.append("\t\t\treturn false;\n");
        buf.append("\t\t}\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addHashCode() {
        StringBuffer buf = new StringBuffer();
//        public final int hashCode() {
//            return 735186935 + filled0 + filled1 + filled2;
//        }

        buf.append("\n\tpublic final int hashCode() {\n");
        buf.append("\t\treturn "+ cmd.classId);
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            buf.append(" + " + FILLED_FIELD_NAME + i);
        }
        buf.append(";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetClassIndex() {
        StringBuffer buf = new StringBuffer();
        /*public final int getClassIndex() {
            return 48;
        }*/

        buf.append("\n\tpublic final int getClassIndex() {\n");
        buf.append("\t\treturn "+ cmd.index +";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addIsDirtyInt() {
        StringBuffer buf = new StringBuffer();

       /* public final boolean isDirty(int fieldNo) {
            if (fieldNo < 32) {
                return (dirtyFields0 & 1 << fieldNo) != 0;
            }
            if (fieldNo < 64) {
                return (dirtyFields1 & 1 << fieldNo) != 0;
            }
            if (fieldNo < 96) {
                return (dirtyFields2 & 1 << fieldNo) != 0;
            } else {
                return false;
            }
        }*/

        buf.append("\n\tpublic final boolean isDirty(int fieldNo) {\n");
        int num = getNumOfControlFields();
        if (num == 1) {
            buf.append("\t\treturn (dirtyFields0 & 1 << fieldNo) != 0;\n");
        } else {
            for (int i = 0; i < num; i++) {
                buf.append("\t\tif (fieldNo < "+ ((32 * i) + 32) +") {\n");
                buf.append("\t\t\treturn (dirtyFields"+ i +" & 1 << fieldNo) != 0;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());



    }

    protected void addMakeDirty() {
        StringBuffer buf = new StringBuffer();

        /*public final void makeDirty(int fieldNo) {
            if (fieldNo < 32) {
                dirtyFields0 |= 1 << fieldNo;
                return;
            }
            if (fieldNo < 64) {
                dirtyFields1 |= 1 << fieldNo;
                return;
            }
            if (fieldNo < 96) {
                dirtyFields2 |= 1 << fieldNo;
            }
        }*/

        buf.append("\n\tpublic final void makeDirty(int fieldNo) {\n");
        int num = getNumOfControlFields();
        if (num == 1) {
            buf.append("\t\tdirtyFields0 |= 1 << fieldNo;\n");
        } else {
            for (int i = 0; i < num; i++) {
                buf.append("\t\tif (fieldNo < "+ ((32 * i) + 32) +") {\n");
                buf.append("\t\t\tdirtyFields"+i+" |= 1 << fieldNo;\n");
                buf.append("\t\t\treturn;\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetFilled() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setFilled(int fieldNo) {\n");
        int num = getNumOfControlFields();
        if (num == 1) {
            buf.append("\t\tfilled0 |= 1 << fieldNo;\n");
        } else {
            for (int i = 0; i < num; i++) {
                buf.append("\t\tif (fieldNo < " + ((32 * i) + 32) + ") {\n");
                buf.append("\t\t\tfilled" + i + " |= 1 << fieldNo;\n");
                buf.append("\t\t\treturn;\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());

    }

    protected void addClearFilledFlags() {
        StringBuffer buf = new StringBuffer();
//        public final void clearFilledFlags() {
//            filled0 = 0;
//        }
        buf.append("\n\tpublic final void clearFilledFlags() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            buf.append("\t\tfilled"+i+" = 0;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addClear() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void clear() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            buf.append("\t\t"+ FILLED_FIELD_NAME +i+" = 0;\n");
            buf.append("\t\t"+ DIRTY_FIELD_NAME +i+" = 0;\n");
            buf.append("\t\t"+ RESOLVED_NAME +i+" = 0;\n");
        }
        List objectList = getAllRealObjectFields();
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            FieldMetaData info = (FieldMetaData) iter.next();
            int fieldNo = getFieldNo(info);
            buf.append("\t\t_" + fieldNo + " = null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }



    protected void addIsDirty() {
        StringBuffer buf = new StringBuffer();
//    public final boolean isDirty() {
//        return dirtyFields0 != 0 || dirtyFields1 != 0 || dirtyFields2 != 0;
//    }
        buf.append("\n\tpublic final boolean isDirty() {\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\treturn dirtyFields0 != 0");
            } else {
                buf.append(" || dirtyFields"+i+" != 0");
            }
        }
        buf.append(";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyFields(OID oid) {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates all the public void setXXXField(int field,xxx newValue)
     */
    protected void addAllSetXXXFields() {
        Set keys = classToSetField.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            Class type = (Class) typeIter.next();
            addSetXXXField(type);
        }
        addSetObjectField();
        addSetObjectFieldUnresolved();
    }

    /**
     * This method generates all the public void setXXXFieldAbs(int absField,xxx newValue)
     */
    protected void addAllSetXXXFieldsAbs() {
        Set keys = classToSetFieldAbs.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            Class type = (Class) typeIter.next();
            addSetXXXFieldAbs(type);
        }
        addSetObjectFieldAbs();
        addSetObjectFieldUnresolvedAbs();
    }

    /**
     * This method generates all the public void setXXXField(int field,xxx newValue)
     */
    protected void addAllSetInternalXXXFields() {
        Set keys = classToInternalSetField.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            Class type = (Class) typeIter.next();
            addSetInternalXXXField(type);
        }
        addSetInternalObjectField();
    }

    /**
     * This method generates all the public void setInternalXXXFieldAbs(int absField,xxx newValue)
     */
    protected void addAllSetInternalXXXFieldsAbs() {
        Set keys = classToInternalSetFieldAbs.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            Class type = (Class) typeIter.next();
            addSetInternalXXXFieldAbs(type);
        }
        addSetInternalObjectFieldAbs();
    }

    /**
     * This method generates all the public xxx getXXXField(int field)
     */
    protected void addAllGetXXXFields() {
        Set keys = getFieldToClass.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            String type = (String) typeIter.next();
            addGetXXXField(type);
        }
    }

    /**
     * This method generates all the public xxx getXXXFieldAbs(int field)
     */
    protected void addAllGetXXXFieldsAbs() {
        Set keys = classToGetFieldAbs.keySet();
        for (Iterator typeIter = keys.iterator(); typeIter.hasNext();) {
            Class type = (Class) typeIter.next();
            addGetXXXFieldAbs(type);
        }
    }

    protected String getWriteMethod(int index) {
        int newIndex = totalNoOfFields - (index * 32);
        if (newIndex <= 8) {
            return "writeByte";
        } else if (newIndex <= 16) {
            return "writeShort";
        } else {
            return "writeInt";
        }
    }

    protected String getReadMethod(int index) {
        int newIndex = totalNoOfFields - (index * 32);
        if (newIndex <= 8) {
            return "readByte";
        } else if (newIndex <= 16) {
            return "readShort";
        } else {
            return "readInt";
        }
    }

    protected String getOIDName(FieldMetaData fmd) {
        //return ((JdbcRefField)fmd.jdbcField).targetClass.oidClassName;
        return fmd.typeMetaData.oidClassName;
    }

    protected void addWriteExternal() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void writeExternal(OIDObjectOutput os) throws java.io.IOException {\n");
        Class bigIntegerType = java.math.BigInteger.class;
        boolean isBigInteger = false;   // 2
        for (int i = 0; i < cmd.stateFields.length; i++) {
            FieldMetaData info = cmd.stateFields[i];
            if (!isExternalized(info)) {
                Class type = info.type;
                if (type.equals(bigIntegerType)) {
                    isBigInteger = true;
                }
            }
        }
        if (isBigInteger) {
            buf.append("\t\tbyte bytes[] = null;\n");
        }
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint filled = filled0;\n");
            } else {
                buf.append("\t\tfilled = filled"+i+";\n");
            }
            if (i == 0) {
                buf.append("\t\tint nullFields = getNullFields(0);\n");
            } else {
                buf.append("\t\tnullFields = getNullFields("+i+");\n");
            }
            buf.append("\t\tos."+ getWriteMethod(i) +"(filled);\n");
            buf.append("\t\tos."+ getWriteMethod(i) +"(nullFields);\n");

            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData info = (FieldMetaData) iter.next();
                int fieldNo = info.stateFieldNo;
                if (info.category != MDStatics.CATEGORY_TRANSACTIONAL) {
//                    if (Debug.DEBUG) {
//                        buf.append("\t\t\tos.writeUTF(\"begin "+ info.name +"\");\n");
//                    }
                    Class type = info.type;
                    if (isExternalized(info)) {
                        buf.append("\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0 && (nullFields & "+ getFieldIndex(fieldNo) +") == 0) {\n");
                        buf.append("\t\t\tSerUtils.writeObject(_" + fieldNo + ", os);\n");
                        buf.append("\t\t}\n");

                    } else if (info.type.isPrimitive()) {// do primative types
                        buf.append("\t\tif ((filled & " + getFieldIndex(fieldNo) + ") != 0) {\n");
                        buf.append("\t\t\tos."+ primClassToSerWriteMethod.get(type) +"(_"+ fieldNo +");\n");
                        buf.append("\t\t}\n");
                    } else { // Object type i.e. can be null
                        buf.append("\t\tif ((filled & " + getFieldIndex(fieldNo) + ") != 0 && (nullFields & " + getFieldIndex(fieldNo) + ") == 0) {\n");
                        if (isOID(info) || isPolyRef(info)) {
                            buf.append("\t\t\tos.write((OID)_"+ fieldNo +");\n");
                        } else if (isCollection(info)) {
                            buf.append("\t\t\tSerUtils.writeCollectionOrMapField(os, cmd.stateFields[" + fieldNo + "], _" + fieldNo + ");\n");
                        } else if (isMap(info)) {
                            buf.append("\t\t\tSerUtils.writeCollectionOrMapField(os, cmd.stateFields[" + fieldNo + "], _" + fieldNo + ");\n");
                        } else if (isArray(info)) {
                            if (isPCArray(info)) {
                                buf.append("\t\t\tSerUtils.writeArrayField(cmd.stateFields[" + fieldNo + "], os, _" + fieldNo + ");\n");
                            } else {
                                buf.append("\t\t\tSerUtils.writeArrayField("+ info.componentTypeCode +", os, _" + fieldNo + ");\n");
                            }
                        } else {
                            if (type.equals(String.class)) {
                                buf.append("\t\tUtils.writeLongUTF8(_" + fieldNo + ", os);\n");
                            } else if (type.equals(java.util.Locale.class)) {
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".getLanguage());\n");
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".getCountry());\n");
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".getVariant());\n");
                            } else if (type.equals(java.math.BigDecimal.class)) {
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".toString());\n");
                            } else if (type.equals(java.math.BigInteger.class)) {
                                buf.append("\t\t\tbytes = _" + fieldNo + ".toByteArray();\n");
                                buf.append("\t\t\tos.writeInt(bytes.length);\n");
                                buf.append("\t\t\tos.write(bytes);\n");
                            } else if (type.equals(java.util.Date.class)) {
                                buf.append("\t\t\tos.writeLong(_" + fieldNo + ".getTime());\n");
                            } else if (type.equals(java.net.URL.class)) {
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".toString());\n");
                            } else if (type.equals(java.io.File.class)) {
                                buf.append("\t\t\tos.writeUTF(_" + fieldNo + ".toString());\n");
                            } else if (type.equals(java.sql.Timestamp.class)) {
                                buf.append("\t\t\tos.writeLong(_" + fieldNo + ".getTime());\n");
                            } else if (wrapperTypesToPrimative.containsKey(type)) {  // wrapper
                                buf.append("\t\t\tos." +
                                        primClassToSerWriteMethod.get(wrapperTypesToPrimative.get(type)) +
                                        "(_" + fieldNo + "."+ wrapperTypesToValue.get(type) +"());\n");
                            } else {
                                buf.append("\t\t\tSerUtils.writeObject(_" + fieldNo + ", os);\n");
                            }
                        }
                        buf.append("\t\t}\n");
                    }
//                    if (Debug.DEBUG) {
//                        buf.append("\t\t\tos.writeUTF(\"end "+ info.name +"\");\n");
//                    }
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addExpect() {
        StringBuffer buf = new StringBuffer();
        buf.append(
                "    private void expect(OIDObjectInput in, String s) throws java.io.IOException {\n" +
                "        String got;\n" +
                "        try {\n" +
                "            got = in.readUTF();\n" +
                "        } catch (IOException e) {\n" +
                "            throw new RuntimeException(\"Expected '\" + s +\n" +
                "                    \"': \" + e, e);\n" +
                "        }\n" +
                "        if (!s.equals(got)) {\n" +
                "            throw new RuntimeException(\"Expected '\" + s +\n" +
                "                    \"' got '\" + got + \"'\");\n" +
                "        }\n" +
                "    }");
        spec.addMethod(buf.toString());
    }

    protected void addReadExternal() {
        //addExpect();
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void readExternal(OIDObjectInput is) throws java.lang.ClassNotFoundException, java.io.IOException {\n");
        Class bigIntegerType = java.math.BigInteger.class;
        boolean isBigInteger = false;   // 2
        for (int i = 0; i < cmd.stateFields.length; i++) {
            FieldMetaData info = cmd.stateFields[i];
            if (!isExternalized(info)) {
                Class type = info.type;
                if (type.equals(bigIntegerType)) {
                    isBigInteger = true;
                }
            }
        }
        if (isBigInteger) {
            buf.append("\t\tbyte bytes[] = null;\n");
        }

        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\tint filled = filled0 = is." + getReadMethod(i) + "();\n");
            } else {
                buf.append("\t\tfilled = filled" + i + " = is." + getReadMethod(i) + "();\n");
            }
            if (i == 0) {
                buf.append("\t\tint nullFields = is." + getReadMethod(i) + "();\n");
            } else {
                buf.append("\t\tnullFields = is." + getReadMethod(i) + "();\n");
            }
            List fields = getFields(i);
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                FieldMetaData info = (FieldMetaData) iter.next();
                int fieldNo = info.stateFieldNo;
                if (info.category != MDStatics.CATEGORY_TRANSACTIONAL) {
//                    if (Debug.DEBUG) {
//                        buf.append("\t\t\texpect(is, \"begin "+ info.name +"\");\n");
//                    }
                    Class type = info.type;
                    if (isExternalized(info)) {
                        buf.append("\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0 && (nullFields & "+ getFieldIndex(fieldNo) +") == 0) {\n");
                        buf.append("\t\t\t_"+ fieldNo +" = SerUtils.readObject(is);\n");
                        buf.append("\t\t}\n");
                    } else if (type.isPrimitive()) {// do primative types
                        buf.append("\t\tif ((filled & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                        buf.append("\t\t\t_"+ fieldNo +" = is."+ primClassToSerReadMethod.get(type) +"();\n");
                        buf.append("\t\t}\n");
                    } else { // Object type i.e. can be null
                        buf.append("\t\tif ((filled & " + getFieldIndex(fieldNo) + ") != 0 && (nullFields & " + getFieldIndex(fieldNo) + ") == 0) {\n");

                        // do stuff here
                        if (isOID(info) || isPolyRef(info)) {
                            buf.append("\t\t\t_" + fieldNo + " = is.readOID();\n");
                        } else if (isCollection(info)) {
                            buf.append("\t\t\t_" + fieldNo + " = SerUtils.readCollectionOrMapField(is, cmd.stateFields[" + fieldNo + "]);\n");
                        } else if (isMap(info)) {
                            buf.append("\t\t\t_" + fieldNo + " = SerUtils.readCollectionOrMapField(is, cmd.stateFields[" + fieldNo + "]);\n");
                        } else if (isArray(info)) {
                            if (isPCArray(info)) {
                                buf.append("\t\t\t_" + fieldNo + " = SerUtils.readArrayField(cmd.stateFields[" + fieldNo + "], is);\n");
                            } else {
                                buf.append("\t\t\t_" + fieldNo + " = SerUtils.readArrayField("+ info.componentTypeCode +", is);\n");
                            }
                        } else {
                            if (type.equals(String.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = Utils.readLongUTF8(is);\n");
                            } else if (type.equals(java.util.Locale.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.util.Locale(is.readUTF(), is.readUTF(), is.readUTF());\n");
                            } else if (type.equals(java.math.BigDecimal.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.math.BigDecimal(is.readUTF());\n");
                            } else if (type.equals(java.math.BigInteger.class)) {
                                buf.append("\t\t\tbytes = new byte[is.readInt()];\n");
                                buf.append("\t\t\tis.readFully(bytes);\n");
                                buf.append("\t\t\t_" + fieldNo + " = new java.math.BigInteger(bytes);\n");
                            } else if (type.equals(java.util.Date.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.util.Date(is.readLong());\n");
                            } else if (type.equals(java.net.URL.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.net.URL(is.readUTF());\n");
                            } else if (type.equals(java.io.File.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.io.File(is.readUTF());\n");
                            } else if (type.equals(java.sql.Timestamp.class)) {
                                buf.append("\t\t\t_" + fieldNo + " = new java.sql.Timestamp(is.readLong());\n");
                            } else if (wrapperTypesToPrimative.containsKey(type)) {  // wrapper
                                buf.append("\t\t_" + fieldNo + " = new "+ type.getName() +"(is."+ primClassToSerReadMethod.get(wrapperTypesToPrimative.get(type)) +"());\n");
                            } else {
                                buf.append("\t\t\t_" + fieldNo + " = ("+type.getName()+")SerUtils.readObject(is);\n");

                            }
                        }
                        buf.append("\t\t}\n");
                    }
//                    if (Debug.DEBUG) {
//                        buf.append("\t\t\texpect(is, \"end "+ info.name +"\");\n");
//                    }
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * Add's a default constructor that calls the State super class
     * this constructor is needed so that we can get an instance from the class with class.newInstance()
     */
    protected void addConstructor() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic ");
        buf.append(className);
        buf.append("(){\n\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method return's a new instance of the class, it is used after we called class.newInstance(),
     * the reason for this method is that class.newInstance() is +/- 15 time slower than creating the
     * class the normal way.
     * So if we do not have a instance class.newInstance() will be called, after that newInstance() will
     * be called on the instance, for fast object creation.
     */
    protected void addNewInstance() {
        StringBuffer buf = new StringBuffer();

        /*
        public final State newInstance() {
        return new HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_Assembly();
        }
        */
        buf.append("\n\tpublic final State newInstance() {\n");
        buf.append("\t\treturn new ");
        buf.append(className);
        buf.append("();\n\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * Add all PC fields in the hier to state and
     * private boolean isDirty;
     * private boolean[] dirtyFields = new boolean[20];
     * private boolean[] filled = new boolean[20];
     */
    protected void addFields() {
        spec.addField("public static "+ CLASS_META_DATA_CLASS + " cmd");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            spec.addField("public int " + FILLED_FIELD_NAME + i);
            spec.addField("public int " + DIRTY_FIELD_NAME + i);
            spec.addField("public int " + RESOLVED_NAME + i);
        }
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            if (isObject(fields[i])) {         // OID
                spec.addField("public Object " + getFieldName(i));
            } else {
                spec.addField("public " + fields[i].type.getName() + " " + getFieldName(i));
            }
        }
    }

    /**
     * @param type the type of the fields we are looking for i.e Type.INT
     * @return List list of FieldInfo objects for the specified type
     */
    protected List getFields(Class type) {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.type.equals(type)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * @param type the type of the fields we are looking for i.e Type.INT
     * @return List list of FieldInfo objects for the specified type
     */

    protected List getFieldsAbs(Class type) {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.type.equals(type)) {
                if (field.managedFieldNo != -1) {
                    list.add(field);
                }
            }
        }
        return list;
    }



    /**
     * @return List list of FieldInfo objects that are not primitive types or String's.
     */
    protected List getObjectFields() {  // excluding String
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!getFieldToClass.containsValue(field.type)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * @return List list of FieldInfo objects that are not primitive types or String's.
     */
    protected List getObjectFieldsAbs() {  // excluding String
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!getFieldToClass.containsValue(field.type)) {
                if (field.managedFieldNo != -1) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    /**
     * @return List list of FieldInfo objects that are not primitive types or String's.
     */
    protected List getObjectFieldsMetaData() {  // excluding String
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!nonObjectClassTypeList.contains(field.type)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * @return List list of FieldInfo objects that are not primitive types or String's.
     */
    protected List getObjectFieldsMetaDataAbs() {  // excluding String
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!nonObjectClassTypeList.contains(field.type)) {
                if (field.managedFieldNo != -1) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    protected List getAllRealObjectFields() {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!realNonObjectClassTypeList.contains(field.type)) {
                list.add(field);
            }
        }
        return list;
    }

    protected List getRealObjectFields(int index) {
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (!realNonObjectClassTypeList.contains(field.type)) {
                if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    protected int getPrimaryHash(int index) {
        int hash = 0;
        List list = getPrimaryFields(index);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            hash += getFieldIndex(fmd.stateFieldNo);
        }
        return hash;
    }

    protected int getSecondaryHash(int index) {
        int hash = 0;
        List list = getSecondaryFields(index);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            hash += getFieldIndex(fmd.stateFieldNo);
        }
        return hash;
    }

    protected List getPrimaryFields(int index) {
        ArrayList primFields = new ArrayList();
        List list = getFields(index);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            if (fmd.primaryField) {
                primFields.add(fmd);
            }
        }
        return primFields;
    }

    protected List getPrimaryFields() {
        ArrayList primFields = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        primFields.ensureCapacity(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.primaryField) {
                primFields.add(fmd);
            }
        }
        return primFields;
    }

    protected List getSecondaryFields() {
        ArrayList primFields = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        primFields.ensureCapacity(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.secondaryField) {
                primFields.add(fmd);
            }
        }
        return primFields;
    }

    protected List getSecondaryFields(int index) {
        ArrayList primFields = new ArrayList();
        List list = getFields(index);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            if (fmd.secondaryField) {
                primFields.add(fmd);
            }
        }
        return primFields;
    }

    protected List getFields(int index) {
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                list.add(field);
            }
        }
        return list;
    }

    protected int getPrimativeHashForIndex(int index) {
        int hash = 0;
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                if (field.type.isPrimitive()) {
                    hash += getFieldIndex(field.stateFieldNo);
                }
            }
        }
        return hash;
    }

    protected List getPass1Fields(int index) {
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                if (field.primaryField) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    protected List getPass2Fields(int index) {
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                if (field.secondaryField) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    protected List getOIDsFieldsMetaData() {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.category == MDStatics.CATEGORY_REF
                    || field.category == MDStatics.CATEGORY_POLYREF) {
                list.add(field);
            }
        }
        return list;
    }

    protected List getPass1FieldAndRefOrPolyRefFields(int index) {
        int to = ((index + 1) * 32) - 1;
        int from = (index * 32) - 1;
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.stateFieldNo > from && field.stateFieldNo <= to) {
                if (field.primaryField) {
                    if (field.category == MDStatics.CATEGORY_REF ||
                            field.category == MDStatics.CATEGORY_POLYREF) {
                        list.add(field);
                    }
                }
            }
        }
        return list;
    }

    protected LinkedList getDirectRefFieldsMetaData() {
//        int[] fieldnos =  cmd.directRefStateFieldNos;
        LinkedList list = new LinkedList();
        FieldMetaData[] fields = cmd.stateFields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.isDirectRef()) {
                list.add(field);
            }
        }

        return list;
    }

    protected List getPass1FieldsMetaData() {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        list.ensureCapacity(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.primaryField) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * This method generates public xxx getXXXField(int field)
     *
     * @param methodName the method that we are generating
     */
    protected void addGetXXXField(String methodName) {
        StringBuffer buf = new StringBuffer();
        Class type = (Class) getFieldToClass.get(methodName);
        buf.append("\n\tpublic final "+ type.getName() +" "+ methodName +"(int field){\n");
        List fields = getFields(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new "+ FIELD_NOT_FOUND_EXCEPTION +"(\"There were no " +
                    type.toString() + " fields found for field number \"+field);\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator(); singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                buf.append("\t\tif (field == " + getFieldNo(info) + ") {\n");
                buf.append("\t\t\treturn "+ getFieldName(getFieldNo(info)) +";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + type.toString() +
                        " field was not found for field number \"+field);\n\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch (field) {\n");
            for (Iterator fieldIter = fields.iterator();fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                buf.append("\t\t\tcase " + getFieldNo(info) + ":\n");
                buf.append("\t\t\t\treturn " + getFieldName(getFieldNo(info)) + ";\n\n");
            }
            buf.append("\t\t\tdefault:\n");
            buf.append("\t\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified " + type.toString() +
                    " field was not found for field number \"+field);\n\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }




    /**
     * This method generates public xxx getXXXField(int field)
     *
     * @param type the type of method we are generating i.e Type.INT
     */
    protected void addGetXXXFieldAbs(Class type) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final " + type.getName() + " " +
                (String) classToGetFieldAbs.get(type) + "(int absField){\n");
        List fields = getFieldsAbs(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION + "(\"There were no " +
                    type.toString() + " fields found for field number \"+absField);\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator(); singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                buf.append("\t\tif (absField == " + getAbsFieldNo(info) + ") {\n");
                buf.append("\t\t\treturn " + getFieldName(getFieldNo(info)) + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + type.toString() +
                        " field was not found for field number \"+absField);\n\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch (absField) {\n");
            for (Iterator fieldIter = fields.iterator(); fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                buf.append("\t\t\tcase " + getAbsFieldNo(info) + ":\n");
                buf.append("\t\t\t\treturn " + getFieldName(getFieldNo(info)) + ";\n\n");
            }
            buf.append("\t\t\tdefault:\n");
            buf.append("\t\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified " + type.toString() +
                    " field was not found for absField number \"+absField);\n\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setXXXField(int field, xxx newValue)
     *
     * @param type the type of method we are generating i.e Type.INT
     */
    protected void addSetXXXField(Class type) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void " + (String) classToSetField.get(type) +
                "(int field," + type.getName() + " newValue){\n");
        List fields = getFields(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no " + type.toString() + " fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(field == " + fieldNo + "){\n ");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + type.toString() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(field){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getFieldNo(info) + ":\n");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified " + type.toString() +
                    " field was not found for field number \"+field);\n\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setXXXField(int field, xxx newValue)
     *
     * @param type the type of method we are generating i.e Type.INT
     */
    protected void addSetXXXFieldAbs(Class type) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void " + (String) classToSetFieldAbs.get(type) +
                "(int absField," + type.getName() + " newValue){\n");
        List fields = getFieldsAbs(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no " + type.toString() + " absField found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(absField == " + getAbsFieldNo(info) + "){\n ");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + type.toString() + " absField was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(absField){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getAbsFieldNo(info) + ":\n");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified " + type.toString() +
                    " field was not found for absField number \"+absField);\n\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetObjectFieldUnresolved() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setObjectFieldUnresolved(int field,Object newValue){\n");
        List fields = getObjectFields();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no Object fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(field == " + fieldNo + "){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " &= " + getFieldIndexMask(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(field){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getFieldNo(info) + ":\n");


                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " &= " + getFieldIndexMask(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified Object field was not found for field number \"+field);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetObjectField() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void setObjectField(int field, Object newValue){\n");
        List fields = getObjectFields();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no Object fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(field == " + fieldNo + "){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");



                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(field){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getFieldNo(info) + ":\n");


                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified Object field was not found for field number \"+field);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetObjectFieldUnresolvedAbs() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setObjectFieldUnresolvedAbs(int absfield,Object newValue){\n");
        List fields = getObjectFieldsAbs();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no Object fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(absfield == " + getAbsFieldNo(info) + "){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " &= " + getFieldIndexMask(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(absfield){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getAbsFieldNo(info) + ":\n");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " &= " + getFieldIndexMask(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified Object field was not found for field number \"+absfield);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetObjectFieldAbs() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setObjectFieldAbs(int absfield, Object newValue){\n");
        List fields = getObjectFieldsAbs();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no Object fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(absfield == " + getAbsFieldNo(info) + "){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(absfield){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getAbsFieldNo(info) + ":\n");

                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getResolvedFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\t" + getDirtyFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");

                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified Object field was not found for field number \"+absfield);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addHasSameFields() {
        StringBuffer buf = new StringBuffer();

        /*
        public final boolean hasSameFields(State state) {
        HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff s = (HYPERDRIVE_STATE_com_versant_core_jdo_junit_test0_model_GuiStuff)state;
        return (filled0 ^ s.filled0) == 0 && (filled1 ^ s.filled1) == 0 && (filled2 ^ s.filled2) == 0;
        }
        */

        buf.append("\n\tpublic final boolean hasSameFields(State state){\n");
        buf.append("\t\t"+ className + " s = ("+className+")state;\n");
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                buf.append("\t\treturn (filled0 ^ s.filled0) == 0");
            } else {
                buf.append(" && (filled"+i+" ^ s.filled"+i+") == 0");
            }
        }
        buf.append(";\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setInternalXXXField(int field, xxx newValue)
     *
     * @param type the type of method we are generating i.e Type.INT
     */
    protected void addSetInternalXXXField(Class type) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void " + (String) classToInternalSetField.get(type) +
                "(int field,"+ type.getName() +" newValue){\n");
        List fields = getFields(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new "+ FIELD_NOT_FOUND_EXCEPTION+
                    "(\"There were no " + type.toString() + " fields found to set.\");\n\t}\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(field == "+ fieldNo +"){\n ");
                buf.append("\t\t\t"+ getFieldName(fieldNo) +" = newValue;\n");
                buf.append("\t\t\t"+ getFilledFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified " + type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
                buf.append("\t}\n");
            }
        } else {
            buf.append("\t\tswitch(field){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getFieldNo(info) + ":\n");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified "+ type.getName() +" field was not found.\");\n");
            buf.append("\t\t}\n");
            buf.append("\t}\n");
        }
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setInternalXXXField(int field, xxx newValue)
     *
     * @param type the type of method we are generating i.e Type.INT
     */
    protected void addSetInternalXXXFieldAbs(Class type) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void " + (String) classToInternalSetFieldAbs.get(type) +
                "(int absField," + type.getName() + " newValue){\n");
        List fields = getFieldsAbs(type);
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no " + type.toString() + " fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(absField == " + getAbsFieldNo(info) + "){\n ");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + type.toString() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(absField){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getAbsFieldNo(info) + ":\n");
                buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified " + type.toString() +
                    " field was not found for field number \"+absField);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected int getFieldNo(FieldMetaData fmd) {
        return fmd.stateFieldNo;
    }

    protected int getAbsFieldNo(FieldMetaData fmd) {
        return fmd.managedFieldNo;
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetInternalObjectField() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setInternalObjectField(int field,Object newValue){\n");
        List fields = getObjectFields();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new "+ FIELD_NOT_FOUND_EXCEPTION+
                    "(\"There were no Object fields found to set.\");\n\t}\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(field == "+ fieldNo +"){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = ("+ info.type.getName() +")newValue;\n");
                }
                buf.append("\t\t\t"+ getFilledFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n\t}\n");
            }
        } else {
            buf.append("\t\tswitch(field){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getFieldNo(info) + ":\n");


                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\treturn;\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new JDOFatalInternalException(\"The specified Object field was not found.\");\n");
            buf.append("\t\t}\n");
            buf.append("\t}\n");

        }
        spec.addMethod(buf.toString());
    }

    /**
     * This method generates public void setObjectField(int field, Object newValue)
     */
    protected void addSetInternalObjectFieldAbs() {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tpublic final void setInternalObjectFieldAbs(int absField,Object newValue){\n");
        List fields = getObjectFieldsAbs();
        if (fields.isEmpty()) {
            buf.append("\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"There were no Object fields found to set.\");\n");
        } else if (fields.size() == 1) {
            for (Iterator singelIter = fields.iterator();
                 singelIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) singelIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tif(absField == " + getAbsFieldNo(info) + "){\n ");
                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t} else {\n");
                buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                        "(\"The specified " + info.type.getName() + " field was not found.\");\n");
                buf.append("\t\t}\n");
            }
        } else {
            buf.append("\t\tswitch(absField){\n");
            for (Iterator fieldIter = fields.iterator();
                 fieldIter.hasNext();) {
                FieldMetaData info = (FieldMetaData) fieldIter.next();
                int fieldNo = getFieldNo(info);
                buf.append("\t\tcase " + getAbsFieldNo(info) + ":\n");


                if (isObject(info)) {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = newValue;\n");
                } else {
                    buf.append("\t\t\t" + getFieldName(fieldNo) + " = (" + info.type.getName() + ")newValue;\n");
                }
                buf.append("\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
                buf.append("\t\t\tbreak;\n\n");
            }
            // Do default
            buf.append("\t\tdefault:\n");
            buf.append("\t\t\tthrow new " + FIELD_NOT_FOUND_EXCEPTION +
                    "(\"The specified Object field was not found for field number \"+absField);\n\t\t}\n");

        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected boolean isCollection(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_COLLECTION) {
            return true;
        }
        return false;
    }

    protected boolean isArray(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_ARRAY) {
            return true;
        }
        return false;
    }

    protected boolean isPrimitiveArray(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_ARRAY) {
            if (info.componentType.isPrimitive() ||
                    imutableTypeList.contains(info.componentType)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isPCArray(FieldMetaData fmd) {
        if (fmd.category == MDStatics.CATEGORY_ARRAY && fmd.scoField && fmd.isElementTypePC()) {
            return true;
        }
        return false;
    }

    protected boolean isMap(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_MAP) {
            return true;
        }
        return false;
    }

    protected boolean isPolyRef(FieldMetaData info) {
        if (info.category == MDStatics.CATEGORY_POLYREF) {
            return true;
        }
        return false;
    }

    protected String getFieldName(int fieldNum) {
        return "_" + fieldNum;
    }

    protected int getNumOfControlFields() {
        return (totalNoOfFields / 32) + 1;
    }

    protected String getFilledFieldName(int fieldNum) {
        return FILLED_FIELD_NAME + (fieldNum / 32);
    }

    protected int getLocalVarIndex(int firstIndex, int fieldNo) {
        return (fieldNo / 32) + firstIndex;
    }

    protected String getDirtyFieldName(int fieldNum) {
        return DIRTY_FIELD_NAME + (fieldNum / 32);
    }

    protected String getResolvedFieldName(int fieldNum) {
        return RESOLVED_NAME + (fieldNum / 32);
    }

    protected int getFieldIndex(int fieldNum) {
        return 1 << fieldNum;
    }

    protected int getFieldIndexMask(int fieldNum) {
        return 0xffffffff ^ getFieldIndex(fieldNum);
    }

    protected ClassMetaData getTopPCSuperClassMetaData() {
        return cmd.pcHierarchy[0];
    }

    protected void addIsFieldNullorZero() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic boolean isFieldNullorZero(int stateFieldNo){\n");
        FieldMetaData[] fields = cmd.stateFields;
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData f = fields[i];
            boolean obf = isObject(f);
            if (obf || Number.class.isAssignableFrom(f.type)
                    || Boolean.class.isAssignableFrom(f.type)) {
                if (count++ == 0) {
                    buf.append("\t\tswitch (stateFieldNo) {\n");
                }
                buf.append("\t\t\tcase " + i + ":\t");
                String fn = getFieldName(i);
                if (obf) {
                    buf.append("return " + fn + " == null;\n");
                } else {
                    switch (f.typeCode) {
                        case MDStatics.BOOLEAN:
                            buf.append("return !" + fn + ";\n");
                            break;
                        case MDStatics.BOOLEANW:
                            buf.append("return " + fn + " == null || !((Boolean)" + fn + ").booleanValue();\n");
                            break;
                        case MDStatics.BYTEW:
                        case MDStatics.SHORTW:
                        case MDStatics.INTW:
                            buf.append("return " + fn + " == null || ((Number)" + fn + ").intValue() == 0;\n");
                            break;
                        case MDStatics.LONGW:
                            buf.append("return " + fn + " == null || ((Number)" + fn + ").longValue() == 0L;\n");
                            break;
                        case MDStatics.FLOATW:
                            buf.append("return " + fn + " == null || ((Number)" + fn + ").floatValue() == 0.0f;\n");
                            break;
                        case MDStatics.DOUBLEW:
                        case MDStatics.BIGDECIMAL:
                        case MDStatics.BIGINTEGER:
                            buf.append("return " + fn + " == null || ((Number)" + fn + ").doubleValue() == 0.0;\n");
                            break;
                        default:
                            buf.append("return " + getFieldName(i) + " == 0;");
                    }
                }
            }
        }
        if (count > 0) {
            buf.append("\t\t};\n");
        }
        buf.append("\t\nreturn false;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

}

