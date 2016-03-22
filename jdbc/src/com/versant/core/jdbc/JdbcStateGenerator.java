
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
package com.versant.core.jdbc;

import com.versant.core.metadata.generator.StateSrcGenerator;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.jdbc.metadata.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Adds JDBC specific stuff to the standard generated State class.
 */
public class JdbcStateGenerator extends StateSrcGenerator {

    public JdbcStateGenerator() {
        super();
    }

    public ClassSpec generateState(ClassMetaData cmd) {
        ClassSpec spec = super.generateState(cmd);

        spec.addImport(JdbcState.class.getName());
        spec.addImport(JdbcOID.class.getName());
        spec.addImport(ResultSet.class.getName());
        spec.addImport(PreparedStatement.class.getName());
        spec.addImport(SQLException.class.getName());
        spec.addImport(JdbcColumn.class.getName());
        spec.addImport(JdbcUtils.class.getName());
        spec.addImport(JdbcField.class.getName());
        spec.addImport(JdbcPolyRefField.class.getName());
        spec.addImport(JdbcSimpleField.class.getName());

        spec.addInterface("JdbcState");

        addJdbcHelpers();
        addCopyPass1Fields();           //cool
        addSetOracleStyleLOBs();
        addCopyPass1Fields2();
        addSetOptimisticLockingParams();//don't know
        addSetParams();            //cool
        addSetParamsChangedAndNotNull();             //cool

        return spec;
    }

    protected void addInitStaticsBody(StringBuffer buf) {
        super.addInitStaticsBody(buf);
        // early exit from method if storeClass is null (i.e. remote PMF)
        buf.append("\t\tif (cmd.storeClass == null) return true;\n");
        FieldMetaData[] fields = cmd.stateFields;
        boolean first = true;
        for (int j = 0; j < fields.length; j++) {
            JdbcField f = (JdbcField)fields[j].storeField;
            if (f instanceof JdbcSimpleField) {
                JdbcSimpleField sf = (JdbcSimpleField)f;
                if (sf.col.converter != null) {
                    if (first) {
                        buf.append("\t\tFieldMetaData[] fields = cmd.stateFields;\n");
                        buf.append("\t\tJdbcSimpleField sf;\n");
                        first = false;
                    }
                    buf.append("\t\tsf = (JdbcSimpleField)fields[" + j + "].storeField;\n");
                    buf.append("\t\t" + StateSrcGenerator.JDBC_CONVERTER_FIELD_PREFIX + j +
                            " = (" + sf.col.converter.getClass().getName() + ")sf.col.converter;\n");
                    buf.append("\t\tjdbcCol_" + j + " = sf.col;\n");
                }
            }
        }
    }

    private void addJdbcHelpers() {
        spec.addMethod(
                "private OID getPolyRefOID(\n" +
                "        FieldMetaData fmd,\n" +
                "        ResultSet rs,\n" +
                "        int firstCol) throws SQLException {\n" +
                "    return getPolyRefOID((JdbcField)fmd.storeField, rs, firstCol);\n" +
                "}");
        spec.addMethod(
                "private OID getPolyRefOID(\n" +
                "        JdbcField f,\n" +
                "        ResultSet rs,\n" +
                "        int firstCol)\n" +
                "        throws SQLException {\n" +
                "    JdbcPolyRefField pf =\n" +
                "            (JdbcPolyRefField)f;\n" +
                "    return pf.getData(rs, firstCol);\n" +
                "}");
        spec.addMethod(
                "private int setPolyRefData(\n" +
                "        FieldMetaData fmd,\n" +
                "        OID oid,\n" +
                "        ClassMetaData cmd,\n" +
                "        PreparedStatement ps,\n" +
                "        int firstParam) throws SQLException {\n" +
                "    return setPolyRefData((JdbcField)fmd.storeField, oid, cmd, ps, firstParam);\n" +
                "}");
        spec.addMethod(
                "public static int setPolyRefData(\n" +
                "        JdbcField f,\n" +
                "        OID oid,\n" +
                "        ClassMetaData cmd,\n" +
                "        PreparedStatement ps,\n" +
                "        int firstParam) throws SQLException {\n" +
                "    JdbcPolyRefField pf =\n" +
                "            (JdbcPolyRefField)f;\n" +
                "    return pf.setData(ps, firstParam, oid);\n" +
                "}");
    }


    private void addSetOracleStyleLOBs() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void setOracleStyleLOBs(ResultSet rs, int stateFieldNos[], int numFieldNos, int firstCol) throws SQLException {\n");
        List oracleStyleList = getOracleStyleLOBFieldsMetaData();
        if (!oracleStyleList.isEmpty()) {
            buf.append("\t\tfor (int i = 0; i < numFieldNos; i++) {\n");
            buf.append("\t\t\tswitch (stateFieldNos[i]) {\n");
            for (Iterator iter = oracleStyleList.iterator();iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\t\t\tcase "+ fieldNo +":\n");
                buf.append("\t\t\t\t\tjdbcConverter_"+ fieldNo +".set(rs, firstCol++, jdbcCol_"+ fieldNo +", _"+ fieldNo +");\n");
                buf.append("\t\t\t\t\tbreak;\n\n");
            }
            buf.append("\t\t\t}\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    private void addCopyPass1Fields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyPass1Fields(ResultSet rs, FetchGroup fetchGroup, int firstCol) throws SQLException {\n");
        buf.append("\t\tint fgn[] = fetchGroup.stateFieldNos;\n");
        buf.append("\t\tfor (int i = 0; i < fgn.length; i++) {\n");
        buf.append("\t\t\tswitch (fgn[i]) {\n");
        List pass1List = getPass1FieldsMetaData();
        for (Iterator iterator = pass1List.iterator(); iterator.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iterator.next();
            int fieldNo = fmd.stateFieldNo;
            buf.append("\t\t\t\tcase "+ fieldNo +":\n");
            getCopyPass1FieldfromSQL(fmd,fieldNo, buf);
        }
        buf.append("\t\t\t\tdefault:\n");
        buf.append("\t\t\t\t\tbreak;\n\n");
        buf.append("\t\t\t}\n");
        buf.append("\t\t}\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }


    private void addCopyPass1Fields2() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyPass1Fields(ResultSet rs, JdbcField fields[]) {\n");
        List pass1List = getPass1FieldsMetaData();
        if (!pass1List.isEmpty()){
            buf.append("\t\tJdbcField field = null;\n");
            buf.append("\t\ttry {\n");
            buf.append("\t\t\tfor (int i = 0; i < fields.length; i++) {\n");
            buf.append("\t\t\t\tfield = fields[i];\n");
            buf.append("\t\t\t\tif (field != null && cmd.stateFields[field.stateFieldNo] == field.fmd) {\n");
            buf.append("\t\t\t\t\tswitch (field.stateFieldNo) {\n");
            for (Iterator iter = pass1List.iterator(); iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                int fieldNo = fmd.stateFieldNo;
                buf.append("\t\t\t\t\t\tcase "+ fieldNo +":\n");
                getCopyPass1FieldfromSQL2(fmd, fieldNo, buf);
            }
            buf.append("\t\t\t\t\t}\n");
            buf.append("\t\t\t\t}\n");
            buf.append("\t\t\t}\n");
            buf.append("\t\t} catch (SQLException e) {\n");
            buf.append("\t\t\tthrow com.versant.core.common.BindingSupportImpl.getInstance().datastore(\"Error reading field \" + field.fmd.getQName() + \" from ResultSet: \" + e, e);\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    private void getCopyPass1FieldfromSQL2(FieldMetaData fmd, int fieldNo,
            StringBuffer buf) {
        JdbcField f = (JdbcField)fmd.storeField;
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField) f).col;
            Class fieldType = c.javaType;
            boolean isPrim = c.javaType.isPrimitive();
            if (c.converter != null) {// converter
//                    _2 = (Date)jdbcConverter_2.get(rs, firstCol++, jdbcCol_2);
                if (isExternalized(fmd) || isPrimitiveArray(fmd)) {
                    buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = jdbcConverter_" + fieldNo + ".get(rs, (i + 1), jdbcCol_" + fieldNo + ");\n");
                } else if (isPrim) {
                    String wrapper = (String) primativeTypesToWrapper.get(fieldType);
                    String toValue = (String) wrapperStringToValue.get(wrapper);
                    buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = ((" + wrapper + ")jdbcConverter_" + fieldNo + ".get(rs, (i + 1), jdbcCol_" + fieldNo + "))." + toValue + "();\n");
                } else {
                    buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = (" + fieldType.getName() + ")jdbcConverter_" + fieldNo + ".get(rs, (i + 1), jdbcCol_" + fieldNo + ");\n");
                }

            } else {                        // no converter
                Class prim = (Class) wrapperTypesToPrimative.get(fieldType);
                if (prim != null) {  // its a wrapper class
//                    _0 = new Integer(rs.getInt(firstCol++));
//                    if (rs.wasNull()) {
//                        _0 = null;
//                    }
                    String rsGet = (String) typeToResultSetGetField.get(prim);
                    buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = new " + fieldType.getName() + "(rs." + rsGet + "(i + 1));\n");
                    buf.append("\t\t\t\t\t\t\tif (rs.wasNull()) {\n");
                    buf.append("\t\t\t\t\t\t\t\t_" + fieldNo + " = null;\n");
                    buf.append("\t\t\t\t\t\t\t}\n");
                } else if (fieldType.equals(java.math.BigInteger.class)) {  // special case for BigInteger
//                    BigDecimal decimal4 = rs.getBigDecimal(firstCol++);
//                    if (decimal4 != null) {
//                        _4 = decimal4.toBigInteger();
//                    } else {
//                        _4 = null;
//                    }
                    buf.append("\t\t\t\t\t\t\tjava.math.BigDecimal decimal" + fieldNo + " = rs.getBigDecimal(i + 1);\n");
                    buf.append("\t\t\t\t\t\t\tif (decimal" + fieldNo + " != null) {\n");
                    buf.append("\t\t\t\t\t\t\t\t_" + fieldNo + " = decimal" + fieldNo + ".toBigInteger();\n");
                    buf.append("\t\t\t\t\t\t\t} else {\n");
                    buf.append("\t\t\t\t\t\t\t\t_" + fieldNo + " = null;\n");
                    buf.append("\t\t\t\t\t\t\t}\n");
                } else {
                    if (isExternalized(fmd)) {
                        buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = rs.getObject(i + 1);\n");
                    } else if ((String) typeToResultSetGetField.get(fieldType) == null) {
                        buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = (" + fieldType.getName() + ")rs.getObject(i + 1);\n");
                    } else {
                        String getField = (String) typeToResultSetGetField.get(fieldType);
                        buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = rs." + getField + "(i + 1);\n");
                    }
                }

            }
        } else if (f instanceof JdbcPolyRefField) {
            buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = getPolyRefOID(cmd.stateFields[" + fieldNo + "], rs, (i + 1));\n");
        } else if (f instanceof JdbcRefField) {
            JdbcRefField rf = (JdbcRefField) f;
            String oidName = rf.targetClass.oidClassName;
            boolean isInHier = rf.targetClass.isInHierarchy();
            if (isInHier) {
                buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = new " + oidName + "(cmd.jmd.classes[" + rf.targetClass.index + "], false);\n");
            } else {
                buf.append("\t\t\t\t\t\t\t_" + fieldNo + " = new " + oidName + "();\n");
            }
            buf.append("\t\t\t\t\t\t\tif (!((JdbcOID)_" + fieldNo + ").copyKeyFields(rs, (i + 1))) {\n");
            buf.append("\t\t\t\t\t\t\t\t_" + fieldNo + " = null;\n");
            buf.append("\t\t\t\t\t\t\t}\n");
        }
        buf.append("\t\t\t\t\t\t\t" + getFilledFieldName(fieldNo) + " |= " + getFieldIndex(fieldNo) + ";\n");
        buf.append("\t\t\t\t\t\t\tbreak;\n\n");
    }

    private String getConverterSet(JdbcSimpleField field) {
        int fieldNo = field.stateFieldNo;
        JdbcColumn c = field.col;
        boolean isPrim = c.javaType.isPrimitive();
        String wrapper = null;
        if (isPrim) {
            wrapper = (String) primativeTypesToWrapper.get(c.javaType);
        }
        String con = "jdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", " +
                (isPrim ? "new " + wrapper + "(_" + fieldNo + ")" : "_" + fieldNo + "") + ");";
        return con;
    }



    private void getCopyPass1FieldfromSQL(FieldMetaData fmd, int fieldNo,
            StringBuffer buf) {
        JdbcField f = (JdbcField)fmd.storeField;
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField) f).col;
            Class fieldType = c.javaType;
            boolean isPrim = c.javaType.isPrimitive();
            if (c.converter != null) {// converter
//                    _2 = (Date)jdbcConverter_2.get(rs, firstCol++, jdbcCol_2);
                if (isExternalized(fmd) || isPrimitiveArray(fmd)) {
                    buf.append("\t\t\t\t\t_"+ fieldNo +" = jdbcConverter_"+ fieldNo +".get(rs, firstCol++, jdbcCol_"+ fieldNo +");\n");
                } else if (isPrim) {
                    String wrapper = (String) primativeTypesToWrapper.get(fieldType);
                    String toValue = (String) wrapperStringToValue.get(wrapper);
                    buf.append("\t\t\t\t\t_" + fieldNo + " = (("+ wrapper +")jdbcConverter_" + fieldNo + ".get(rs, firstCol++, jdbcCol_" + fieldNo + "))."+ toValue +"();\n");
                } else {
                    buf.append("\t\t\t\t\t_" + fieldNo + " = ("+ fieldType.getName() +")jdbcConverter_" + fieldNo + ".get(rs, firstCol++, jdbcCol_" + fieldNo + ");\n");
                }

            } else {                        // no converter
                Class prim = (Class)wrapperTypesToPrimative.get(fieldType);
                if (prim != null) {  // its a wrapper class
//                    _0 = new Integer(rs.getInt(firstCol++));
//                    if (rs.wasNull()) {
//                        _0 = null;
//                    }
                    String rsGet = (String) typeToResultSetGetField.get(prim);
                    buf.append("\t\t\t\t\t_"+ fieldNo +" = new "+ fieldType.getName() +"(rs."+ rsGet +"(firstCol++));\n");
                    buf.append("\t\t\t\t\tif (rs.wasNull()) {\n");
                    buf.append("\t\t\t\t\t\t_"+ fieldNo +" = null;\n");
                    buf.append("\t\t\t\t\t}\n");
                } else if (fieldType.equals(java.math.BigInteger.class)) {  // special case for BigInteger
//                    BigDecimal decimal4 = rs.getBigDecimal(firstCol++);
//                    if (decimal4 != null) {
//                        _4 = decimal4.toBigInteger();
//                    } else {
//                        _4 = null;
//                    }
                    buf.append("\t\t\t\t\tjava.math.BigDecimal decimal"+ fieldNo +" = rs.getBigDecimal(firstCol++);\n");
                    buf.append("\t\t\t\t\tif (decimal"+ fieldNo +" != null) {\n");
                    buf.append("\t\t\t\t\t\t_"+ fieldNo +" = decimal"+ fieldNo +".toBigInteger();\n");
                    buf.append("\t\t\t\t\t} else {\n");
                    buf.append("\t\t\t\t\t\t_"+ fieldNo +" = null;\n");
                    buf.append("\t\t\t\t\t}\n");
                } else {
                    if (isExternalized(fmd)) {
                        buf.append("\t\t\t\t\t_"+fieldNo+" = rs.getObject(firstCol++);\n");
                    } else if ((String) typeToResultSetGetField.get(fieldType) == null) {
                        buf.append("\t\t\t\t\t_" + fieldNo + " = ("+ fieldType.getName() +")rs.getObject(firstCol++);\n");
                    } else {
                        String getField = (String) typeToResultSetGetField.get(fieldType);
                        buf.append("\t\t\t\t\t_" + fieldNo + " = rs." + getField + "(firstCol++);\n");
                    }
                }

            }
        } else if (f instanceof JdbcPolyRefField) {
//            _3 = State.getPolyRefOID(cmd.stateFields[3], rs, firstCol);
//            firstCol += 2;
            buf.append("\t\t\t\t\t_"+fieldNo+" = getPolyRefOID(cmd.stateFields["+fieldNo+"], rs, firstCol);\n");
            int inc = ((JdbcPolyRefField) f).cols.length;
            if (inc == 1){
                buf.append("\t\t\t\t\tfirstCol++;\n");
            } else {
                buf.append("\t\t\t\t\tfirstCol += " + inc + ";\n");
            }


        } else if (f instanceof JdbcRefField) {
            JdbcRefField rf = (JdbcRefField) f;
            String oidName = rf.targetClass.oidClassName;
            boolean isInHier = rf.targetClass.isInHierarchy();
            if (isInHier) {
                buf.append("\t\t\t\t\t_"+fieldNo+" = new "+ oidName +"(cmd.jmd.classes["+ rf.targetClass.index +"], false);\n");
            } else {
                buf.append("\t\t\t\t\t_" + fieldNo + " = new " + oidName + "();\n");
            }
            buf.append("\t\t\t\t\tif (!((JdbcOID)_" + fieldNo + ").copyKeyFields(rs, firstCol)) {\n");
            buf.append("\t\t\t\t\t\t_" + fieldNo + " = null;\n");
            buf.append("\t\t\t\t\t}\n");
            int inc = rf.cols.length;
            if (inc == 1) {
                buf.append("\t\t\t\t\tfirstCol++;\n");
            } else {
                buf.append("\t\t\t\t\tfirstCol += " + inc + ";\n");
            }
        }
        buf.append("\t\t\t\t\t"+ getFilledFieldName(fieldNo) +" |= "+ getFieldIndex(fieldNo) +";\n");
        buf.append("\t\t\t\t\tbreak;\n\n");
    }

    private List getOracleStyleLOBFieldsMetaData() {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        list.ensureCapacity(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.storeField != null && ((JdbcField)field.storeField).isOracleStyleLOB()) {
                list.add(field);
            }
        }
        return list;
    }

    private void addSetOptimisticLockingParams() {
        StringBuffer buf = new StringBuffer();
//    public final int setOptimisticLockingParams(PreparedStatement ps, int firstParam) throws SQLException {
//        ps.setShort(firstParam++, _23);
//        ps.setInt(firstParam++, _0.intValue());
//        return firstParam;
//        jdbcConverter_5.set(ps, firstParam++, jdbcCol_5, _5);
//    }

        buf.append("\n\tpublic final int setOptimisticLockingParams(PreparedStatement ps, int firstParam) throws SQLException {\n");
        JdbcSimpleField f = ((JdbcClass)cmd.storeClass).optimisticLockingField;
        if (f != null) {
            int fieldNo = f.stateFieldNo;
            JdbcColumn c = f.col;
            Class classType = c.javaType;
            if (c.converter != null) {// converter
                buf.append("\t\t"+getConverterSet(f)+"\n");
            } else {                        // no converter
                if (wrapperTypesToPrimative.containsKey(classType)) {  // its a wrapper class
                    Class primType = (Class) wrapperTypesToPrimative.get(classType);
                    String psSet = (String)typeToPreparedStatementSetField.get(primType);
                    String toValue = (String)wrapperTypesToValue.get(classType);
                    buf.append("\t\tps."+ psSet +"(firstParam++, _"+ fieldNo +"."+ toValue +"());\n");
                } else if (classType.equals(BigInteger.class)) {  // special case for BigInteger
                    buf.append("\t\tps.setBigDecimal(firstParam++, new BigDecimal(_"+ fieldNo +"));\n");
                } else {
                    String psSet = (String)typeToPreparedStatementSetField.get(classType);
                    if (psSet != null) {
                       buf.append("\t\tps."+ psSet +"(firstParam++, _"+ fieldNo +");\n");
                    } else {
                        buf.append("\t\tps.setObject(firstParam++, _"+ fieldNo +", "+ c.jdbcType +");\n");
                    }
                }
            }

        }
        buf.append("\t\treturn firstParam;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCompareToPass1() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final int compareToPass1(com.versant.core.common.State state) {\n");
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
        // do test for CLOBS here.
        List list = getOracleStyleLOBFieldsMetaData();
        if (!list.isEmpty()) {
            // do if stuff
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                FieldMetaData metaData = (FieldMetaData) iter.next();
                int fieldNo = metaData.stateFieldNo;
//                if (ans == 0 && (filled0 & 1) != 0) {
//                    if (_0 == null && s._0 != null) {
//                        return -1;
//                    }
//                    if (_0 != null && s._0 == null) {
//                        return 1;
//                    }
//                }
                buf.append("\t\tif (ans == 0 && ("+ getFilledFieldName(fieldNo) +" & "+ getFieldIndex(fieldNo) +") != 0) {\n");
                buf.append("\t\t\tif (_"+ fieldNo +" == null && s._"+ fieldNo +" != null) {\n");
                buf.append("\t\t\t\treturn -1;\n");
                buf.append("\t\t\t}\n");
                buf.append("\t\t\tif (_"+ fieldNo +" != null && s._"+ fieldNo +" == null) {\n");
                buf.append("\t\t\t\treturn 1;\n");
                buf.append("\t\t\t}\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t\treturn ans;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetParams() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final int setParams(PreparedStatement ps, int fieldNos[], int firstFieldNo, int lastFieldNo, int firstParam, com.versant.core.server.PersistGraph pGraph, int tableNo) throws java.sql.SQLException {\n");
        buf.append("\t\tfor (; firstFieldNo < lastFieldNo; firstFieldNo++) {\n");
        buf.append("\t\t\tswitch (fieldNos[firstFieldNo]) {\n");
        List pass1List = getPass1FieldsMetaData();
        for (Iterator iter = pass1List.iterator();iter.hasNext();) {
            FieldMetaData fmd = (FieldMetaData) iter.next();
            buf.append("\t\t\t\tcase "+ fmd.stateFieldNo +":\n");
            getSetFieldsToSQL_IL_CheckNull(fmd.stateFieldNo, buf);
        }
        buf.append("\t\t\t\tdefault:\n");
        buf.append("\t\t\t\t\tbreak;\n\n");
        buf.append("\t\t\t}\n");
        buf.append("\t\t}\n");
        buf.append("\t\treturn firstParam;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetParamsChangedAndNotNull() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final int setParamsChangedAndNotNull(PreparedStatement ps, int fieldNos[], int firstFieldNo, int lastFieldNo, int firstParam, com.versant.core.server.PersistGraph pGraph, int tableNo) throws java.sql.SQLException {\n");
        List pass1List = getPass1FieldsMetaDataWithChangedLocking();
        if (!pass1List.isEmpty()){
            buf.append("\t\tfor (; firstFieldNo < lastFieldNo; firstFieldNo++) {\n");
            buf.append("\t\t\tif (!isNull(fieldNos[firstFieldNo])) {\n");
            buf.append("\t\t\t\tswitch (fieldNos[firstFieldNo]) {\n");
            for (Iterator iter = pass1List.iterator();iter.hasNext();) {
                FieldMetaData fmd = (FieldMetaData) iter.next();
                buf.append("\t\t\t\t\tcase "+fmd.stateFieldNo+":\n");
                getSetFieldsToSQL_IL(fmd.stateFieldNo, buf);
            }
            buf.append("\t\t\t\t\tdefault:\n");
            buf.append("\t\t\t\t\t\tbreak;\n\n");
            buf.append("\t\t\t\t}\n");
            buf.append("\t\t\t}\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t\treturn firstParam;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void getSetFieldsToSQL_IL_CheckNull(int fieldNo, StringBuffer buf) {
        FieldMetaData fmd = cmd.stateFields[fieldNo];
        JdbcField field = (JdbcField)fmd.storeField;
        JdbcTable fieldTable = field.mainTable;
        JdbcTable[] tables = ((JdbcClass)cmd.storeClass).allTables;
        boolean isMultiTable = false;
        int tableNo = -1;
        if (tables.length > 1) {
            isMultiTable = true;
            for (int i = 0; i < tables.length; i++) {
                if (tables[i] == fieldTable) {
                    tableNo = i;
                }
            }
        }
        if (field instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField) field).col;
            if (c.isForUpdate()) {
                Class fieldType = c.javaType;
                boolean isPrimative = c.javaType.isPrimitive();
                if (isPrimative) {
                    if (isExternalized(fmd)) {
                        isPrimative = false;
                    }
                }
                if (c.converter != null) {// converter
                    boolean isPrim = c.javaType.isPrimitive();
                    if (isMultiTable) { // we have a multi table
                        buf.append("\t\t\t\t\tif (tableNo == "+ tableNo +") {\n");
                        if (isPrim) {
                            buf.append("\t\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", new "+ primativeTypesToWrapper.get(fieldType) +"(_" + fieldNo + "));\n");
                        } else {
                            buf.append("\t\t\t\t\t\tjdbcConverter_"+ fieldNo +".set(ps, firstParam++, jdbcCol_"+ fieldNo +", _"+ fieldNo +");\n");
                        }
                        buf.append("\t\t\t\t\t}\n");
                    } else {
                        if (isPrim) {
                            buf.append("\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", new " + primativeTypesToWrapper.get(fieldType) + "(_" + fieldNo + "));\n");
                        } else {
                            buf.append("\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", _" + fieldNo + ");\n");
                        }
                    }
                } else {
                    if (isPrimative) {
                        // this is a temp var
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\tps."+ typeToPreparedStatementSetField.get(fieldType) +"(firstParam++, _"+ fieldNo +");\n");
                            buf.append("\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\tps." + typeToPreparedStatementSetField.get(fieldType) + "(firstParam++, _" + fieldNo + ");\n");
                        }
                    } else if (wrapperTypesToPrimative.containsKey(fieldType)) {  // its a wrapper class
                        String toVal = (String) wrapperTypesToValue.get(fieldType);
                        Class primType = (Class) wrapperTypesToPrimative.get(fieldType);
                        String psSet = (String) typeToPreparedStatementSetField.get(primType);
                        if (isMultiTable) { // we have a multi table
//                            if (_8 != null) {
//                                ps.setInt(firstParam++, _8.intValue());
//                            } else {
//                                ps.setNull(firstParam++, 4);
//                            }

                            buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\t\tps."+ psSet +"(firstParam++, _"+ fieldNo +"."+ toVal +"());\n");
                            buf.append("\t\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\t\tps.setNull(firstParam++, "+ c.jdbcType +");\n");
                            buf.append("\t\t\t\t\t\t}\n");
                            buf.append("\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\tps." + psSet + "(firstParam++, _" + fieldNo + "." + toVal + "());\n");
                            buf.append("\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\tps.setNull(firstParam++, " + c.jdbcType + ");\n");
                            buf.append("\t\t\t\t\t}\n");
                        }
                    } else if (fieldType.equals(java.math.BigInteger.class)) {  // special case for BigInteger
//                        if (_4 != null) {
//                            ps.setBigDecimal(firstParam++, new BigDecimal(_4));
//                        } else {
//                            ps.setNull(firstParam++, 2);
//                        }
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\t\tps.setBigDecimal(firstParam++, new java.math.BigDecimal(_"+ fieldNo +"));\n");
                            buf.append("\t\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\t\tps.setNull(firstParam++, " + c.jdbcType + ");\n");
                            buf.append("\t\t\t\t\t\t}\n");
                            buf.append("\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\tps.setBigDecimal(firstParam++, new java.math.BigDecimal(_" + fieldNo + "));\n");
                            buf.append("\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\tps.setNull(firstParam++, " + c.jdbcType + ");\n");
                            buf.append("\t\t\t\t\t}\n");
                        }
                    } else {
//                if (_3 != null) {
//                    ps.setBigDecimal(firstParam++, _3);
//                } else {
//                    ps.setNull(firstParam++, 2);
//                }
                        String psSet = (String) typeToPreparedStatementSetField.get(fieldType);
                        if (isExternalized(fmd)){
                            psSet = null;
                        }
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\t\tps."+ (psSet != null ? psSet : "setObject")
                                    +"(firstParam++, "+ ((isExternalized(fmd) && psSet != null) ? "(" + fieldType.getName() + ")" : "")
                                    +"_" + fieldNo + (psSet != null ? "" : (", " + c.jdbcType))+");\n");
                            buf.append("\t\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\t\tps.setNull(firstParam++, " + c.jdbcType + ");\n");
                            buf.append("\t\t\t\t\t\t}\n");
                            buf.append("\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\tif (_" + fieldNo + " != null) {\n");
                            buf.append("\t\t\t\t\t\tps." + (psSet != null ? psSet : "setObject")
                                    + "(firstParam++, " + ((isExternalized(fmd) && psSet != null) ? "(" + fieldType.getName() + ")" : "")
                                    + "_" + fieldNo + (psSet != null ? "" : (", " + c.jdbcType)) + ");\n");
                            buf.append("\t\t\t\t\t} else {\n");
                            buf.append("\t\t\t\t\t\tps.setNull(firstParam++, " + c.jdbcType + ");\n");
                            buf.append("\t\t\t\t\t}\n");
                        }
                    }
                }
            }
        } else if (field instanceof JdbcPolyRefField) {
//            firstParam = State.setPolyRefData(cmd.stateFields[3], (OID) _3, cmd, ps, firstParam);
            if (isMultiTable) { // we have a multi table
                buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                buf.append("\t\t\t\t\t\tfirstParam = setPolyRefData(cmd.stateFields["+ fieldNo +"], (OID) _"+ fieldNo +", cmd, ps, firstParam);\n");
                buf.append("\t\t\t\t\t}\n");
            } else {
                buf.append("\t\t\t\t\t\tfirstParam = setPolyRefData(cmd.stateFields[" + fieldNo + "], (OID) _" + fieldNo + ", cmd, ps, firstParam);\n");
            }
        } else if (field instanceof JdbcRefField) {
            if (isMultiTable) { // we have a multi table
                buf.append("\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
            } else {
                buf.append("\t\t\t\t\t{\n");
            }
            buf.append("\t\t\t\t\t\tOID oid = (OID)_" + fieldNo + ";\n");
            buf.append("\t\t\t\t\t\tif (oid == null || (oid = oid.getRealOID()) == null) {\n");
            JdbcColumn[] cols = field.mainTableCols;
            int nc = cols.length;
            for (int j = 0; j < nc; j++) {
                JdbcColumn col = cols[j];
                if (col.isForUpdate()) {
                    if (col.converter != null) {
                        String oidName = field.fmd.typeMetaData.oidClassName;
                        buf.append("\t\t\t\t\t\t\t"+ oidName+".jdbcConverter_"+j+".set(ps, firstParam++, "+ oidName+".jdbcCol_"+j+", null);\n");
                    } else {
                        buf.append("\t\t\t\t\t\t\tps.setNull(firstParam++, " + col.jdbcType + ");\n");
                    }
                }
            }
            buf.append("\t\t\t\t\t\t} else {\n");
            buf.append("\t\t\t\t\t\t\tfirstParam = ((JdbcOID)oid).setParams(ps, firstParam, ((JdbcField)cmd.stateFields["+ fieldNo +"].storeField).mainTableCols);\n");
            buf.append("\t\t\t\t\t\t}\n");
            buf.append("\t\t\t\t\t}\n");
        }
        buf.append("\t\t\t\t\tbreak;\n\n");
    }

    protected void getSetFieldsToSQL_IL(int fieldNo, StringBuffer buf) {
        FieldMetaData fmd = cmd.stateFields[fieldNo];
        JdbcField field = (JdbcField)fmd.storeField;
        JdbcTable fieldTable = field.mainTable;
        JdbcTable[] tables = ((JdbcClass)cmd.storeClass).allTables;
        boolean isMultiTable = false;
        int tableNo = -1;
        if (tables.length > 1) {
            isMultiTable = true;
            for (int i = 0; i < tables.length; i++) {
                if (tables[i] == fieldTable) {
                    tableNo = i;
                }
            }
        }
        if (field instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField) field).col;
            if (c.isForUpdate()) {
                Class fieldType = c.javaType;
                boolean isPrimative = c.javaType.isPrimitive();
                if (isPrimative){
                    if (isExternalized(fmd)){
                        isPrimative = false;
                    }
                }
                if (c.converter != null) {// converter
                    boolean isPrim = c.javaType.isPrimitive();
                    if (isMultiTable) { // we have a multi table
                        buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                        if (isPrim) {
                            buf.append("\t\t\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", new " + primativeTypesToWrapper.get(fieldType) + "(_" + fieldNo + "));\n");
                        } else {
                            buf.append("\t\t\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", _" + fieldNo + ");\n");
                        }
                        buf.append("\t\t\t\t\t\t}\n");
                    } else {
                        if (isPrim) {
                            buf.append("\t\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", new " + primativeTypesToWrapper.get(fieldType) + "(_" + fieldNo + "));\n");
                        } else {
                            buf.append("\t\t\t\t\t\tjdbcConverter_" + fieldNo + ".set(ps, firstParam++, jdbcCol_" + fieldNo + ", _" + fieldNo + ");\n");
                        }
                    }
                } else {
                    if (isPrimative) {
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\t\tps." + typeToPreparedStatementSetField.get(fieldType) + "(firstParam++, _" + fieldNo + ");\n");
                            buf.append("\t\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\t\tps." + typeToPreparedStatementSetField.get(fieldType) + "(firstParam++, _" + fieldNo + ");\n");
                        }
                    } else if (wrapperTypesToPrimative.containsKey(fieldType)) {  // its a wrapper class
                        String toVal = (String) wrapperTypesToValue.get(fieldType);
                        Class primType = (Class) wrapperTypesToPrimative.get(fieldType);
                        String psSet = (String) typeToPreparedStatementSetField.get(primType);
                        if (isMultiTable) { // we have a multi table
//                                ps.setInt(firstParam++, _8.intValue());
                            buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\t\tps." + psSet + "(firstParam++, _" + fieldNo + "." + toVal + "());\n");
                            buf.append("\t\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\t\tps." + psSet + "(firstParam++, _" + fieldNo + "." + toVal + "());\n");
                        }
                    } else if (fieldType.equals(java.math.BigInteger.class)) {  // special case for BigInteger
//                            ps.setBigDecimal(firstParam++, new BigDecimal(_4));
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\t\tps.setBigDecimal(firstParam++, new java.math.BigDecimal(_" + fieldNo + "));\n");
                            buf.append("\t\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\t\tps.setBigDecimal(firstParam++, new java.math.BigDecimal(_" + fieldNo + "));\n");
                        }
                    } else {
//                    ps.setBigDecimal(firstParam++, _3);
                        String psSet = (String) typeToPreparedStatementSetField.get(fieldType);
                        if (isExternalized(fmd)){
                            psSet = null;
                        }
                        if (isMultiTable) { // we have a multi table
                            buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                            buf.append("\t\t\t\t\t\t\tps." + (psSet != null ? psSet : "setObject")
                                    + "(firstParam++, " + ((isExternalized(fmd) && psSet != null) ? "(" + fieldType.getName() + ")" : "")
                                    + "_" + fieldNo + (psSet != null ? "" : (", " + c.jdbcType)) + ");\n");
                            buf.append("\t\t\t\t\t\t}\n");
                        } else {
                            buf.append("\t\t\t\t\t\tps." + (psSet != null ? psSet : "setObject")
                                    + "(firstParam++, " + ((isExternalized(fmd) && psSet != null) ? "(" + fieldType.getName() + ")" : "")
                                    + "_" + fieldNo + (psSet != null ? "" : (", " + c.jdbcType)) + ");\n");

                        }
                    }
                }
            }
        } else if (field instanceof JdbcPolyRefField) {
//            firstParam = State.setPolyRefData(cmd.stateFields[3], (OID) _3, cmd, ps, firstParam);
            if (isMultiTable) { // we have a multi table
                buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                buf.append("\t\t\t\t\t\t\tfirstParam = setPolyRefData(cmd.stateFields[" + fieldNo + "], (OID) _" + fieldNo + ", cmd, ps, firstParam);\n");
                buf.append("\t\t\t\t\t\t}\n");
            } else {
                buf.append("\t\t\t\t\t\t\tfirstParam = setPolyRefData(cmd.stateFields[" + fieldNo + "], (OID) _" + fieldNo + ", cmd, ps, firstParam);\n");
            }
        } else if (field instanceof JdbcRefField) {
//                firstParam = ((OID)_1).setParams(ps, firstParam, cmd.stateFields[1].storeField.mainTableCols);
            if (isMultiTable) { // we have a multi table
                buf.append("\t\t\t\t\t\tif (tableNo == " + tableNo + ") {\n");
                buf.append("\t\t\t\t\t\t\tfirstParam = ((JdbcOID)_" + fieldNo + ").setParams(ps, firstParam, ((JdbcField)cmd.stateFields[" + fieldNo + "].storeField).mainTableCols);\n");
                buf.append("\t\t\t\t\t\t}\n");
            } else {
                buf.append("\t\t\t\t\t\tfirstParam = ((JdbcOID)_" + fieldNo + ").setParams(ps, firstParam, ((JdbcField)cmd.stateFields[" + fieldNo + "].storeField).mainTableCols);\n");
            }
        }
        buf.append("\t\t\t\t\t\tbreak;\n\n");
    }

    protected void addContainsValidAppIdFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean containsValidAppIdFields() {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            FieldMetaData[] pkf = cmd.pcHierarchy[0].pkFields;
            for (int i = 0; i < pkf.length; i++) {
                FieldMetaData fmd = pkf[i];
                int pkFieldNum = fmd.stateFieldNo;
                Class fieldType = fmd.type;
                boolean isPrimative = fmd.type.isPrimitive();

                if (isPrimative) {
                    if (fieldType.equals(int.class)) {
                        Integer intt = (Integer) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == "+ intt.intValue() +") {\n");
                    } else if (fieldType.equals(long.class)) {
                        Long longg = (Long) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + longg.longValue() + ") {\n");
                    } else if (fieldType.equals(boolean.class)) {
                        Boolean bool = (Boolean) fmd.getPKDefaultValue();
                        if (bool.booleanValue()) {
                            buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + ") {\n");
                        } else {
                            buf.append("\t\tif (("+ getFilledFieldName(pkFieldNum) +" & "+ getFieldIndex(pkFieldNum) +") == 0 || !_"+ pkFieldNum +") {\n");
                        }
                    } else if (fieldType.equals(double.class)) {
                        Double doubl = (Double) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + doubl.doubleValue() + ") {\n");
                    } else if (fieldType.equals(float.class)) {
                        Float floatt = (Float) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + floatt.doubleValue() + ") {\n");
                    } else if (fieldType.equals(short.class)) {
                        Short shortt = (Short) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + shortt.shortValue() + ") {\n");
                    } else if (fieldType.equals(char.class)) {
                        Character charr = (Character) fmd.getPKDefaultValue();
                        Integer integer = new Integer(charr.charValue());
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + integer.intValue() + ") {\n");
                    } else if (fieldType.equals(byte.class)) {
                        Byte bytee = (Byte) fmd.getPKDefaultValue();
                        buf.append("\t\tif ((" + getFilledFieldName(pkFieldNum) + " & " + getFieldIndex(pkFieldNum) + ") == 0 || _" + pkFieldNum + " == " + bytee.byteValue() + ") {\n");
                    } else {
                        throw BindingSupportImpl.getInstance().invalidOperation("Unsupported type " + fieldType);
                    }
                    buf.append("\t\t\treturn false;\n");
                    buf.append("\t\t}\n");
                } else {
                    buf.append("\t\tif (("+ getFilledFieldName(pkFieldNum) +" & "+ getFieldIndex(pkFieldNum) +") == 0 || _"+ pkFieldNum +" == null || _" + pkFieldNum + ".equals(cmd.pcHierarchy[0].pkFields["+ i +"].getPKDefaultValue())) {\n");
                    buf.append("\t\t\treturn false;\n");
                    buf.append("\t\t}\n");
                }
            }
            buf.append("\t\treturn true;\n");
        } else if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            buf.append("\t\treturn false;\n");
        }
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
            JdbcField f = (JdbcField)fields[i].storeField;
            if (f instanceof JdbcSimpleField) {
                JdbcSimpleField sf = (JdbcSimpleField) f;
                if (sf.col.converter != null) {
                    spec.addField("public static " + sf.col.converter.getClass().getName()
                            +" "+JDBC_CONVERTER_FIELD_PREFIX + i);
                    spec.addField("public static " + sf.col.getClass().getName()
                            +" "+ "jdbcCol_" + i);
                }
            }
        }
    }

    protected void addCopyFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyFields(OID oid) {\n");
        ClassMetaData currentCMD = null;
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            currentCMD = getTopPCSuperClassMetaData();
            buf.append("\t\t" + currentCMD.oidClassName + " id = (" + currentCMD.oidClassName + ")oid;\n");
            FieldMetaData[] fmds = currentCMD.pkFields;
            for (int i = 0; i < fmds.length; i++) {
                FieldMetaData fmd = fmds[i];
                int stateFieldNum = fmd.stateFieldNo;
                buf.append("\t\t_"+ stateFieldNum +" = id._"+ i +";\n");
                buf.append("\t\t"+getFilledFieldName(stateFieldNum) +" |= "+ getFieldIndex(stateFieldNum) +";\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addHasSameNullFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean hasSameNullFields(State state, State mask) {\n");
        JdbcClass jc = (JdbcClass)cmd.storeClass;
        if (jc.optimisticLocking != JdbcClass.OPTIMISTIC_LOCKING_CHANGED) {
            buf.append("\t\treturn true;\n");
        } else {
            buf.append("\t\t"+className+" s = ("+ className +") state;\n");
            buf.append("\t\t"+className+" ms = ("+ className +") mask;\n");
            int num = getNumOfControlFields();
            for (int i = 0; i < num; i++) {
                if (i == 0) {
                    buf.append("\t\tint filledMask = ms.filled0;\n");
                } else {
                    buf.append("\t\tfilledMask = ms.filled" + i + ";\n");
                }
                List fields = getRealObjectFields(i);
                for (Iterator iter = fields.iterator(); iter.hasNext();) {
                    FieldMetaData fmd = (FieldMetaData) iter.next();
                    int fieldNum = getFieldNo(fmd);
                    int index = getFieldIndex(fieldNum);
                    buf.append("\t\tif ((filledMask & "+ index +") != 0 && (_"+ fieldNum +" == null) != (s._"+ fieldNum +" == null)) {return false;}\n");
                }
            }
            buf.append("\t\treturn true;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsUpdate() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyKeyFieldsUpdate(OID oid) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            buf.append("\t\t"+ cmd.oidClassName +" other = ("+ cmd.oidClassName +") oid;\n");
            FieldMetaData[] pkFields = cmd.pkFields;
            JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
            for (int i = 0; i < pkc.length; i++) {
                int stateFieldNo = pkFields[i].stateFieldNo;
                buf.append("\t\tif (containsField("+ stateFieldNo +")) {\n");
                buf.append("\t\t\tother._"+i+" = _"+ stateFieldNo +";\n");
                buf.append("\t\t}\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsFromOID() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void copyKeyFields(OID oid) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            buf.append("\t\t"+ cmd.oidClassName +" other = ("+ cmd.oidClassName +") oid;\n");
            FieldMetaData[] pkFields = cmd.pkFields;
            JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
            for (int i = 0; i < pkc.length; i++) {
                buf.append("\t\tother._"+i+" = _"+ pkFields[i].stateFieldNo +";\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCheckKeyFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean checkKeyFields(OID oid) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            buf.append("\t\t" + cmd.oidClassName + " other = (" + cmd.oidClassName + ") oid;\n");
            FieldMetaData[] pkFields = cmd.pkFields;
            JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
            for (int i = 0; i < pkc.length; i++) {
                Class classType = pkc[i].javaType;
                boolean isPrimitive = classType.isPrimitive();
                if (isPrimitive) {
                    buf.append("\t\tif (other._"+i+" != _"+ pkFields[i].stateFieldNo +") {\n");
                    buf.append("\t\t\treturn false;\n");
                    buf.append("\t\t}\n");
                } else {
                    buf.append("\t\tif (!other._"+i+".equals(_"+ pkFields[i].stateFieldNo +")) {\n");
                    buf.append("\t\t\treturn false;\n");
                    buf.append("\t\t}\n");
                }
            }
        }
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addClearApplicationIdentityFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final void clearApplicationIdentityFields() {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            int[] masks = new int[getNumOfControlFields()];
            FieldMetaData[] pkf = cmd.pcHierarchy[0].pkFields;
            for (int i = pkf.length - 1; i >= 0; i--) {
                int fieldNum = pkf[i].fieldNo;
                masks[getLocalVarIndex(0, fieldNum)] += getFieldIndex(fieldNum);
            }
            int maskLength = masks.length;
            for (int i = 0; i < maskLength; i++) {
                if (masks[i] != 0) {
                    buf.append("\t\tfilled"+i+" = filled"+i+" & "+ (masks[i] ^ 0xFFFFFFFF) +";\n");
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addContainsApplicationIdentityFields() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic final boolean containsApplicationIdentityFields() {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            FieldMetaData[] pkf = cmd.pcHierarchy[0].pkFields;
            for (int i = 0; i < pkf.length; i++) {
                int pkFieldNum = pkf[i].stateFieldNo;
                buf.append("\t\tif (("+ getFilledFieldName(pkFieldNum) +" & "+ getFieldIndex(pkFieldNum) +") == 0) {\n");
                buf.append("\t\t\treturn false;\n");
                buf.append("\t\t}\n");
            }
            buf.append("\t\treturn true;\n");
        } else {
            buf.append("\t\treturn false;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected List getPass1FieldsMetaDataWithChangedLocking() {
        ArrayList list = new ArrayList();
        FieldMetaData[] fields = cmd.stateFields;
        list.ensureCapacity(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.primaryField) {
                if (((JdbcField)field.storeField).includeForChangedLocking) {
                    list.add(field);
                }
            }
        }
        return list;
    }

}

