
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

import com.versant.core.metadata.generator.OIDSrcGenerator;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcClass;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Adds JDBC specific stuff to OID.
 */
public class JdbcOIDGenerator extends OIDSrcGenerator {

    public JdbcOIDGenerator(ModelMetaData jmd) {
        super(jmd);
    }

    public ClassSpec generateOID(ClassMetaData cmd) {
        ClassSpec spec = super.generateOID(cmd);

        spec.addImport(JdbcOID.class.getName());
        spec.addImport(ResultSet.class.getName());
        spec.addImport(PreparedStatement.class.getName());
        spec.addImport(SQLException.class.getName());
        spec.addImport(JdbcColumn.class.getName());
        spec.addImport(JdbcUtils.class.getName());
        spec.addImport(JdbcField.class.getName());
        spec.addImport(JdbcClass.class.getName());

        spec.addInterface("JdbcOID");

        addCopyKeyFields();
        addSetParams();
        addSetParams2();
        addValidateKeyFields();
        addCopyKeyFields2();

        return spec;
    }

    protected void addInitStaticsBody(StringBuffer buf) {
        super.addInitStaticsBody(buf);
        // early exit from method if storeClass is null (i.e. remote PMF)
        if (cmd.isInHierarchy()) {
            buf.append("\t\tif (top.storeClass == null) return true;\n");
        } else {
            buf.append("\t\tif (cmd.storeClass == null) return true;\n");
        }
        JdbcColumn[] pkc = ((JdbcClass)cmd.top.storeClass).table.pk;
        boolean first = true;
        for (int j = 0; j < pkc.length; j++) {
            if (pkc[j].converter != null) {
                if (first) {
                    buf.append("\t\tClassMetaData t = jmd.classes[" + currentCMD.top.index + "];\n");
                    buf.append("\t\tJdbcColumn[] pkc = ((JdbcClass)t.storeClass).table.pk;\n");
                    first = false;
                }
                buf.append("\t\t" + JDBC_CONVERTER_FIELD_PREFIX + j +
                        " = (" + pkc[j].converter.getClass().getName() +
                        ")pkc[" + j + "].converter;\n");
                buf.append("\t\tjdbcCol_" + j + " = pkc[" + j + "];\n");
            }
        }
    }

    protected void addCreateObjectIdClassInstance() {
        /*
        public Object createObjectIdClassInstance() {
            return new javax.jdo.identity.CharIdentity(cmd.top.cls, _0);
        }
        */
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic Object createObjectIdClassInstance() {\n");
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            if (cmd.top.objectIdClass != null){
                String oidClassName = cmd.top.objectIdClass.getName().replace('$','.');
                if (cmd.top.isSingleIdentity) {
                    buf.append("\t\treturn new " + oidClassName + "(cmd.top.cls, _0);\n");
                } else {
                    buf.append("\t\t"+oidClassName+" pk = new "+ oidClassName +"();\n");
                    FieldMetaData[] fields = cmd.pkFields;
                    for (int i = 0; i < fields.length; i++) {
                        FieldMetaData field = fields[i];
                        buf.append("\t\tpk.");
                        buf.append(field.getPkFieldName());
                        buf.append(" = ");
                        buf.append(getFieldName(i));
                        buf.append(";\n");
                    }
                    buf.append("\t\treturn pk;\n");
                }
            } else {
                buf.append("\t\treturn null;\n");
            }
        } else {
            buf.append("\t\treturn null;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    private void addValidateKeyFields() {
        StringBuffer buf = new StringBuffer();
        /*
        public boolean validateKeyFields(ResultSet rs, int firstCol) throws SQLException {
            rs.getInt(firstCol++);
            return !rs.wasNull();
        }
        */
        buf.append("\n\tpublic boolean validateKeyFields(ResultSet rs, int firstCol) throws SQLException {\n");
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            Class fieldType = pkc[i].javaType;
            if (pkc[i].converter != null) { // converter
                /*
                jdbcConverter_2.get(rs, firstCol++, jdbcCol_2);
                if (rs.wasNull())return false;
                */
                buf.append("\t\t");
                buf.append("jdbcConverter_" + i);
                buf.append(".get(rs,firstCol++, jdbcCol_" + i);
                buf.append(");\n\t\t");
                buf.append("if (rs.wasNull())return false;\n");

            } else { // no converter
                boolean isWrapper = wrapperTypesToPrimitive.keySet().contains(fieldType);
                if (isWrapper) {
                    /*
                    rs.getShort(firstCol++);
                    if (rs.wasNull()) return false;
                    */

                    buf.append("\t\trs.");
                    buf.append((String) typeToResultSetGetField.get(wrapperTypesToPrimitive.get(fieldType)));
                    buf.append("(firstCol++);\n");
                } else {
                    /*
                    rs.getString(firstCol++);
                    if (rs.wasNull()) return false;
                    */
                    buf.append("\t\trs.");
                    buf.append((String) typeToResultSetGetField.get(fieldType));
                    buf.append("(firstCol++);\n");

                }
                buf.append("\t\tif (rs.wasNull())return false;\n");
            }
        }
        buf.append("\t\treturn true;\n\t}\n");
        spec.addMethod(buf.toString());
    }


    private void addCopyKeyFields() {
        StringBuffer buf = new StringBuffer();
        /*
    public boolean copyKeyFields(ResultSet rs, int firstCol) throws SQLException {
        _0 = rs.getInt(firstCol++);
        if (rs.wasNull()) {
            return false;
        }
        return true;
    }


        public boolean copyKeyFields(ResultSet rs, int firstCol) throws SQLException {


        Boolean prim_0 = (Boolean)jdbcConverter_0.get(rs, firstCol, jdbcCol_0);
        firstCol++;
        if (rs.wasNull()) {
        return false;
        }
        _0 = prim_0.booleanValue();
        Boolean prim_1 = (Boolean)jdbcConverter_1.get(rs, firstCol, jdbcCol_1);
        firstCol++;
        if (rs.wasNull()) {
        return false;
        }
        _1 = prim_1.booleanValue();
        _2 = (Locale)jdbcConverter_2.get(rs, firstCol, jdbcCol_2);
        firstCol++;
        if (rs.wasNull()) {
        return false;
        }
        _3 = rs.getString(firstCol++);
        return !rs.wasNull();
        }

        */

        buf.append("\n\tpublic boolean copyKeyFields(ResultSet rs,int firstCol) throws SQLException{\n");

        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            Class fieldType = pkc[i].javaType;
            boolean isPrim = pkc[i].javaType.isPrimitive();
            String fieldName = "_" + i;
            if (pkc[i].converter != null) { // converter
                if (isPrim) {
                    /*
                    Boolean prim_1 = (Boolean)jdbcConverter_1.get(rs, firstCol++, jdbcCol_1);
                    if (rs.wasNull()) {
                    return false;
                    }
                    _0 = prim_0.booleanValue();
                    */
                    buf.append("\t\t");
                    buf.append((String) primitiveToWrapperTypes.get(fieldType));
                    buf.append(" prim_"+i);
                    buf.append(" = (");
                    buf.append((String) primitiveToWrapperTypes.get(fieldType));
                    buf.append(")jdbcConverter_"+i);
                    buf.append(".get(rs,firstCol++, jdbcCol_"+i);
                    buf.append(");\n");
                    buf.append("\t\tif (rs.wasNull()) return false;\n");
                    buf.append("\t\t");
                    buf.append(fieldName);
                    buf.append(" = ");
                    buf.append(" prim_" + i);
                    buf.append(".");
                    buf.append((String) wrapperTypesToValue.get(fieldType));
                    buf.append("();\n");
                } else {
                    /*
                    _2 = (Locale)jdbcConverter_2.get(rs, firstCol++, jdbcCol_2);
                    if (rs.wasNull())return false;
                    */
                    buf.append("\t\t");
                    buf.append(fieldName);
                    buf.append(" = (");
                    buf.append(fieldType.getName());
                    buf.append(")jdbcConverter_" + i);
                    buf.append(".get(rs,firstCol++, jdbcCol_" + i);
                    buf.append(");\n");
                    buf.append("\t\tif (rs.wasNull()) return false;\n");
                }
            } else { // no converter
                boolean isWrapper = wrapperTypesToPrimitive.keySet().contains(fieldType);
                if (isWrapper) {
                    /*
                    _3 = new Short(rs.getShort(firstCol++));
                    if (rs.wasNull()) return false;
                    */
                    buf.append("\t\t");
                    buf.append(fieldName);
                    buf.append(" = new ");
                    buf.append(fieldType.getName());
                    buf.append("(rs.");
                    buf.append((String) typeToResultSetGetField.get(
                            wrapperTypesToPrimitive.get(fieldType)));
                    buf.append("(firstCol++));\n");
                } else {
                    /*
                    _3 = rs.getString(firstCol++);
                    if (rs.wasNull()) return false;
                    */
                    buf.append("\t\t");
                    buf.append(fieldName);
                    buf.append(" = rs.");
                    buf.append((String) typeToResultSetGetField.get(fieldType));
                    buf.append("(firstCol++);\n");

                }
                buf.append("\t\tif (rs.wasNull()) return false;\n");
            }
        }
        buf.append("\t\treturn true;\n\t}\n");
        spec.addMethod(buf.toString());
    }

    private void addSetParams() {
        StringBuffer buf = new StringBuffer();
        /*

        public int setParams(PreparedStatement ps, int firstParam) throws SQLException {
            ps.setInt(firstParam++, _0);
            jdbcConverter_1.set(ps, firstParam++, jdbcCol_1, new Boolean(_1));
            jdbcConverter_2.set(ps, firstParam++, jdbcCol_2, _2);
            if (_3 == null) {
                ps.setNull(firstParam++, 12);
            } else {
                ps.setString(firstParam++, _3);
            }
            return firstParam;
        }
        */

        buf.append("\n\tpublic int setParams(PreparedStatement ps, int firstParam) throws SQLException {\n");
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            Class fieldType = pkc[i].javaType;
            String fieldName = "_" + i;
            boolean isPrimitive = pkc[i].javaType.isPrimitive();
            if (pkc[i].converter != null) { // converter
                String converterName = JDBC_CONVERTER_FIELD_PREFIX + i;
                String colName = "jdbcCol_" + i;

                if (isPrimitive) {
                    /*
                    jdbcConverter_1.set(ps, firstParam++, jdbcCol_1, new Boolean(_1));
                    */
                    buf.append("\t\t");
                    buf.append(converterName);
                    buf.append(".set(ps, firstParam++, ");
                    buf.append(colName);
                    buf.append(", new ");
                    buf.append((String) primitiveToWrapperTypes.get(fieldType));
                    buf.append("(");
                    buf.append(fieldName);
                    buf.append("));\n");
                } else {
                    /*
                    jdbcConverter_2.set(ps, firstParam++, jdbcCol_2, _2);
                    */
                    buf.append("\t\t");
                    buf.append(converterName);
                    buf.append(".set(ps, firstParam++, ");
                    buf.append(colName);
                    buf.append(", ");
                    buf.append(fieldName);
                    buf.append(");\n");
                }
            } else if (isPrimitive) { //primitive no converter
                /*
                ps.setInt(firstParam++, _0);
                */
                buf.append("\t\t");
                buf.append("ps.");
                buf.append((String) typeToPreparedStatementSetField.get(fieldType));
                buf.append("(firstParam++, ");
                buf.append(fieldName);
                buf.append(");\n");
            } else { // Object no converter
                /*
                if (_3 == null) ps.setNull(firstParam++, 12);
                else ps.setString(firstParam++, _3);
                */
                buf.append("\t\t");
                buf.append("if (");
                buf.append(fieldName);
                buf.append(" == null) ps.setNull(firstParam++,");
                buf.append(" "+ (pkc[i]).jdbcType);
                buf.append(");\n");
                if (wrapperTypesToPrimitive.containsKey(fieldType)) {// we have a wrapper type

                    /*
                    else ps.setShort(firstParam++, _3.shortValue());
                    */

                    buf.append("\t\t");
                    buf.append("else ps.");
                    buf.append((String) typeToPreparedStatementSetField.get(
                            wrapperTypesToPrimitive.get(fieldType)));
                    buf.append("(firstParam++,");
                    buf.append(fieldName);
                    buf.append(".");
                    buf.append((String) wrapperTypesToValue.get(
                            wrapperTypesToPrimitive.get(fieldType)));
                    buf.append("());\n");
                } else {
                    /*
                    else ps.setString(firstParam++, _3);
                    */
                    buf.append("\t\t");
                    buf.append("else ps.");
                    buf.append((String) typeToPreparedStatementSetField.get(fieldType));
                    buf.append("(firstParam++,");
                    buf.append(fieldName);
                    buf.append(");\n");
                }
            }
        }
        buf.append("\t\treturn firstParam;\n\t}\n");
        spec.addMethod(buf.toString());
    }


    private void addSetParams2() {
        StringBuffer buf = new StringBuffer();
        /*
    public int setParams(PreparedStatement ps, int firstParam, JdbcColumn pkc[]) throws SQLException {
        JdbcColumn c = null;
        c = pkc[0];
        if (c.isForUpdate()) {
            if (c.converter != null) {
                c.converter.set(ps, firstParam++, c, new Boolean(_0));
            } else {
                JdbcUtils.set(ps, firstParam++, new Boolean(_0), c.javaTypeCode, c.jdbcType);
            }
        }
        c = pkc[2];
        if (c.isForUpdate()) {
            if (c.converter != null) {
                c.converter.set(ps, firstParam++, c, _2);
            } else {
                JdbcUtils.set(ps, firstParam++, _2, c.javaTypeCode, c.jdbcType);
            }
        }
        return firstParam;
    }
        */
        buf.append("\n\tpublic int setParams(PreparedStatement ps, int firstParam, JdbcColumn[] pkc) throws SQLException {\n");

        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        /*
        JdbcColumn c = null;
        */
        buf.append("\t\tJdbcColumn c = null;\n");
        for (int i = 0; i < pkc.length; i++) {
            Class fieldType = pkc[i].javaType;
            String fieldName = "_" + i;
            boolean isPrimitive = pkc[i].javaType.isPrimitive();
            /*
            c = pkc[0];
            if (c.isForUpdate()) {
                if (c.converter != null) c.converter.set(ps, firstParam++, c, new Boolean(_0));
                else JdbcUtils.set(ps, firstParam++, new Boolean(_0), c.javaTypeCode, c.jdbcType);
            }
            */
            buf.append("\t\tc = pkc["+i);
            buf.append("];\n");

            buf.append("\t\tif (c.isForUpdate()) {\n");
            if (isPrimitive) {
                /*
                if (c.converter != null) c.converter.set(ps, firstParam++, c, new Boolean(_0));
                else JdbcUtils.set(ps, firstParam++, new Boolean(_0), c.javaTypeCode, c.jdbcType);
                */

                buf.append("\t\t\tif (c.converter != null) c.converter.set(ps, firstParam++, c, new ");
                buf.append((String) primitiveToWrapperTypes.get(fieldType));
                buf.append("(");
                buf.append(fieldName);
                buf.append("));\n");

                buf.append("\t\t\telse JdbcUtils.set(ps, firstParam++, new ");
                buf.append((String) primitiveToWrapperTypes.get(fieldType));
                buf.append("(");
                buf.append(fieldName);
                buf.append("), c.javaTypeCode, c.jdbcType);\n");

            } else {
                /*
                if (c.converter != null) c.converter.set(ps, firstParam++, c, _2);
                else JdbcUtils.set(ps, firstParam++, _2, c.javaTypeCode, c.jdbcType);
                */
                buf.append("\t\t\tif (c.converter != null) c.converter.set(ps, firstParam++, c, ");
                buf.append(fieldName);
                buf.append(");\n");

                buf.append("\t\t\telse JdbcUtils.set(ps, firstParam++, ");
                buf.append(fieldName);
                buf.append(", c.javaTypeCode, c.jdbcType);\n");

            }
            buf.append("\t\t}\n");

        }
        buf.append("\t\treturn firstParam;\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFields2(){
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic boolean copyKeyFields(ResultSet rs, JdbcField[] pks, int[] pkFieldIndexs) throws SQLException {\n");
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            buf.append("\t\tfor (int j = 0; j < pkFieldIndexs.length; j++) {\n");
            JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
            buf.append("\t\t\tswitch(j){\n");
            for (int i = 0; i < pkc.length; i++) {
                Class fieldType = pkc[i].javaType;
                boolean isPrim = pkc[i].javaType.isPrimitive();
                String fieldName = "_" + i;
                buf.append("\t\t\t\tcase " + i + " :\n");
                if (pkc[i].converter != null) { // converter
                    if (isPrim) {
                        /*
                        Boolean prim_1 = (Boolean)jdbcConverter_1.get(rs, pkFieldIndexs[j] + 1, jdbcCol_1);
                        if (rs.wasNull()) {
                        return false;
                        }
                        _0 = prim_0.booleanValue();
                        */

                        buf.append("\t\t\t\t\t"+(String) primitiveToWrapperTypes.get(fieldType));
                        buf.append(" prim_" + i);
                        buf.append(" = (");
                        buf.append((String) primitiveToWrapperTypes.get(fieldType));
                        buf.append(")jdbcConverter_" + i);
                        buf.append(".get(rs, pkFieldIndexs[j] + 1, jdbcCol_" + i);
                        buf.append(");\n");
                        buf.append("\t\t\t\t\t"+fieldName);
                        buf.append(" = ");
                        buf.append(" prim_" + i);
                        buf.append(".");
                        buf.append((String) wrapperTypesToValue.get(fieldType));
                        buf.append("();\n");
                    } else {
                        /*
                        _2 = (Locale)jdbcConverter_2.get(rs, firstCol++, jdbcCol_2);
                        if (rs.wasNull())return false;
                        */
                        buf.append("\t\t\t\t\t" + fieldName);
                        buf.append(" = (");
                        buf.append(fieldType.getName());
                        buf.append(")jdbcConverter_" + i);
                        buf.append(".get(rs,pkFieldIndexs[j] + 1, jdbcCol_" + i);
                        buf.append(");\n");
                    }
                } else { // no converter
                    boolean isWrapper = wrapperTypesToPrimitive.keySet().contains(fieldType);
                    if (isWrapper) {
                        /*
                        _3 = new Short(rs.getShort(firstCol++));
                        if (rs.wasNull()) return false;
                        */
                        buf.append("\t\t\t\t\t" + fieldName);
                        buf.append(" = new ");
                        buf.append(fieldType.getName());
                        buf.append("(rs.");
                        buf.append((String) typeToResultSetGetField.get(wrapperTypesToPrimitive.get(fieldType)));
                        buf.append("(pkFieldIndexs[j] + 1));\n");
                    } else {
                        /*
                        _3 = rs.getString(firstCol++);
                        if (rs.wasNull()) return false;
                        */
                        buf.append("\t\t\t\t\t" + fieldName);
                        buf.append(" = rs.");
                        buf.append((String) typeToResultSetGetField.get(fieldType));
                        buf.append("(pkFieldIndexs[j] + 1);\n");

                    }
                }
                buf.append("\t\t\t\t\tbreak;\n");
            }
            buf.append("\t\t\t}\n");
            buf.append("\t\t\tif (rs.wasNull()) return false;\n");
            buf.append("\t\t}\n");
        }
        buf.append("\t\treturn true;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetLongPrimaryKey() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic long getLongPrimaryKey() {\n");
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            JdbcColumn c = ((JdbcClass)currentCMD.storeClass).table.pk[0];
            switch (c.javaTypeCode) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.INT:
                    buf.append("\t\treturn (long)_0;\n");
                    break;

                case MDStatics.LONG:
                    buf.append("\t\treturn _0;\n");
                    break;
            }
        } else {
            buf.append("\t\treturn (long) -1;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetLongPrimaryKey() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void setLongPrimaryKey(long newPK) {\n");
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            JdbcColumn c = ((JdbcClass)currentCMD.storeClass).table.pk[0];
            buf.append("\t\t_0 = ");
            switch (c.javaTypeCode) {
                case MDStatics.BYTE:
                    buf.append("(byte)");
                    break;
                case MDStatics.SHORT:
                    buf.append("(short)");
                    break;
                case MDStatics.INT:
                    buf.append("(int)");
                    break;
            }
            buf.append("newPK;\n");
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addfillFromPK() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void fillFromPK(Object pk) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            if (cmd.top.objectIdClass != null){
                String oidClassName = cmd.top.objectIdClass.getName().replace('$','.');
                /*
                AppIdConcrete1Key appPK = (AppIdConcrete1Key)pk;
                */
                buf.append("\t\t"+oidClassName);
                buf.append(" appPK = (");
                buf.append(oidClassName);
                buf.append(")pk;\n");

                FieldMetaData[] fields = cmd.pkFields;
                for (int i = 0; i < fields.length; i++) {
                    FieldMetaData field = fields[i];

                    buf.append("\t\t" + getFieldName(i));
                    if (cmd.top.isSingleIdentity) {
                        if (field.type.isPrimitive()) {
                            buf.append(" = appPK.getKey();\n");
                        } else {
                            buf.append(" = new ");
                            buf.append(field.type.getName());
                            buf.append("(appPK.getKey());\n");
                        }
                    } else {
                        /*
                        _0 = appPK.appIdConcKey;
                        */
                        buf.append(" = appPK.");
                        buf.append(field.getPkFieldName());
                        buf.append(";\n");
                    }
                }
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCompareTo() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int compareTo(Object o) {\n");
        buf.append("\t\tOID other = (OID)o;\n");
        buf.append("\t\tint diff = "+ cmd.top.index +" - other.getBaseClassMetaData().index;\n");
        buf.append("\t\tif (diff != 0)  return diff;\n");
        buf.append("\t\tif (other.isNew()) return 1;\n");
        buf.append("\t\telse {\n");
        buf.append("\t\t\t"+ className +" original = ("+ className +")other;\n");

        ClassMetaData currentCMD = getCurrentCMD();

        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        int count = pkc.length - 1;
        boolean isLast;
        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            String fieldName = "_" + i;
            isLast = i == count;

            switch (c.javaTypeCode) {

                case MDStatics.CHAR:
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.INT:

                    if (isLast) {
                        /*
                        return _0 - original._0;
                        */
                        buf.append("\t\t\treturn "+ fieldName +" - original."+ fieldName +";\n");
                    } else {
                        /*
                        if (_0 != original._0) return _0 - original._0;
                        */
                        buf.append("\t\t\tif (" + fieldName + " != original." + fieldName + ") return " + fieldName + " - original." + fieldName + ";\n");
                    }
                    break;
                case MDStatics.LOCALE:
                    /*
                    diff = _2.toString().compareTo(original._2.toString());
                    if (diff != 0) return diff;
                    */
                    buf.append("\t\t\tdiff = "+ fieldName +".toString().compareTo(original."+ fieldName +".toString());\n");

                    if (isLast) {
                        buf.append("\t\t\treturn diff;\n");
                    } else {
                        buf.append("\t\t\tif (diff != 0) return diff;\n");
                    }
                    break;

                case MDStatics.CHARW:
                case MDStatics.BYTEW:
                case MDStatics.SHORTW:
                case MDStatics.INTW:
                case MDStatics.LONGW:
                case MDStatics.FLOATW:
                case MDStatics.DOUBLEW:
                case MDStatics.STRING:
                case MDStatics.BIGDECIMAL:
                case MDStatics.BIGINTEGER:
                case MDStatics.DATE:

                    if (isLast) {
                        buf.append("\t\t\treturn "+ fieldName +".compareTo(original."+ fieldName +");\n");
                    } else {
                        buf.append("\t\t\tdiff = " + fieldName + ".compareTo(original." + fieldName + ");\n");
                        buf.append("\t\t\tif (diff != 0) return diff;\n");
                    }
                    break;

                case MDStatics.DOUBLE:
                case MDStatics.FLOAT:
                case MDStatics.LONG:

                    if (isLast) {
                        /*
                        return _0 - original._0;
                        */
                        buf.append("\t\t\treturn (int)(" + fieldName + " - original." + fieldName + ");\n");
                    } else {
                        /*
                        if (_0 != original._0) return _0 - original._0;
                        */
                        buf.append("\t\t\tif (" + fieldName + " != original." + fieldName + ") return (int)(" + fieldName + " - original." + fieldName + ");\n");
                    }
                    break;


                case MDStatics.BOOLEANW:

                    if (isLast) {
                        buf.append("\t\t\treturn !" + fieldName + ".booleanValue() ? -1 : 1;\n");
                    } else {
                        /*
                        if (_0 != original._0) return !_0 ? -1 : 1;
                        */
                        buf.append("\t\t\tif (!" + fieldName + ".equals(original." + fieldName + ")) return !" + fieldName + ".booleanValue() ? -1 : 1;\n");
                    }

                    break;
                case MDStatics.BOOLEAN:
                    if (isLast) {
                        buf.append("\t\t\treturn !" + fieldName + " ? -1 : 1;\n");
                    } else {
                        /*
                        if (_0 != original._0) return !_0 ? -1 : 1;
                        */
                        buf.append("\t\t\tif ("+ fieldName +" != original."+ fieldName +") return !"+ fieldName +" ? -1 : 1;\n");
                    }
                    break;


                default:
                    throw BindingSupportImpl.getInstance().runtime("Unable to create a ObjectId for a Type '"
                            + c.javaType.getName() + "'");
            }

        }
        buf.append("\t\t}\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToPKString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String toPkString() {\n");
        buf.append("\t\tStringBuffer s = new StringBuffer();\n");
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            String fieldName = "_" + i;
            if (pkc[i].javaTypeCode == MDStatics.CHAR) {
                buf.append("\t\ttry { s.append(");
                buf.append(fieldName);
                buf.append("); } catch (java.lang.Exception e) {}\n");
            } else {
                buf.append("\t\ts.append(");
                buf.append(fieldName);
                buf.append(");\n");
            }
            if (i != (pkc.length - 1)) {
                buf.append("\t\ts.append(\", \");\n");
            }
        }
        buf.append("\t\treturn s.toString();\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToSString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String toSString() {\n");
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        /*
        StringBuffer s = new StringBuffer("OID:za.AppIDAbstract1 (Table:app_i_d_abstract1");
        */
        buf.append("\t\tStringBuffer s = new StringBuffer(\"");
        buf.append("OID:" + currentCMD.qname + " (Table:" +
                ((JdbcClass)currentCMD.storeClass).tableName);
        buf.append("\");\n");

        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            String fieldName = "_" + i;

            buf.append("\t\ts.append(\"");
            buf.append(" Column:" + c.name + " = \"");
            buf.append(");\n");

            if (pkc[i].javaTypeCode == MDStatics.CHAR) {
                buf.append("\t\ttry { s.append(");
                buf.append(fieldName);
                buf.append("); } catch (java.lang.Exception e) {}\n");
            } else {
                buf.append("\t\ts.append(");
                buf.append(fieldName);
                buf.append(");\n");
            }
        }
        buf.append("\t\ts.append(\")\");\n");
        buf.append("\t\treturn s.toString();\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addGetCopy() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic OID copy() {\n");
        buf.append("\t\t"+className);
        buf.append(" copy = new ");
        buf.append(className);
        buf.append("();\n");
        if (cmd.isInHierarchy()) {
            buf.append("\t\tcopy.cmd = cmd;\n");
            buf.append("\t\tcopy.resolved = resolved;\n");
        }
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        int num = pkc.length;
        for (int i = 0; i < num; i++) {
            Class fieldType = pkc[i].javaType;
            String fieldName = getFieldName(i);
            boolean isPrimitive = pkc[i].javaType.isPrimitive();
            boolean isArray = pkc[i].javaType.isArray();
            boolean isCloneable = false;

            if (!isPrimitive) {
                Class[] interfaces = pkc[i].javaType.getInterfaces();
                for (int j = 0; j < interfaces.length; j++) {
                    Class aClass = interfaces[j];
                    if (aClass.getName().equals("Cloneable")) {
                        isCloneable = true;
                    }
                }
            }
            if (isPrimitive) {
                /*
                copy._0 = _0;
                */
                buf.append("\t\tcopy.");
                buf.append(fieldName);
                buf.append(" = ");
                buf.append(fieldName);
                buf.append(";\n");
            } else if (isArray) {
                /*
                System.arraycopy((Object)_0,0, (Object) copy._0, 0, _0.length);
                */
                buf.append("\t\tSystem.arraycopy((Object)");
                buf.append(fieldName);
                buf.append(", 0, (Object)copy.");
                buf.append(fieldName);
                buf.append(", 0, ");
                buf.append(fieldName);
                buf.append(".length);\n");
            } else if (isCloneable) {
                /*
                copy._1 = (Locale)_1.clone();
                */
                buf.append("\t\tcopy.");
                buf.append(fieldName);
                buf.append(" = (");
                buf.append(fieldType.getName());
                buf.append(")");
                buf.append(fieldName);
                buf.append(".clone();\n");
            } else {// normal object ????
                /*
                copy._0 = _0;
                */
                buf.append("\t\tcopy.");
                buf.append(fieldName);
                buf.append(" = ");
                buf.append(fieldName);
                buf.append(";\n");
            }
        }
        buf.append("\t\treturn copy;\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsUpdate() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFieldsUpdate(State state) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            ClassMetaData currentCMD = getTopPCSuperClassMetaData();
            buf.append("\t\t");
            buf.append(stateClassName);
            buf.append(" other = (");
            buf.append(stateClassName);
            buf.append(")state;\n");
            FieldMetaData[] pkFields = currentCMD.pkFields;
            JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
            for (int i = 0; i < pkc.length; i++) {
                int stateFieldNum = pkFields[i].fieldNo;
                String stateFieldName = "_" + stateFieldNum;
                String fieldName = "_" + i;
                /*
                if (other.containsField(1)) _0 = other._1;
                */
                buf.append("\t\tif (other.containsField("+ stateFieldNum);
                buf.append(")) ");
                buf.append(fieldName);
                buf.append(" = other.");
                buf.append(stateFieldName);
                buf.append(";\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsFromState() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFields(State state) {\n");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            ClassMetaData currentCMD = getTopPCSuperClassMetaData();
            buf.append("\t\t");
            buf.append(stateClassName);
            buf.append(" other = (");
            buf.append(stateClassName);
            buf.append(")state;\n");
            FieldMetaData[] pkFields = currentCMD.pkFields;
            JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
            for (int i = 0; i < pkc.length; i++) {
                String stateFieldName = "_" + pkFields[i].fieldNo;
                String fieldName = "_" + i;
                buf.append("\t\t");
                buf.append(fieldName);
                buf.append(" = other.");
                buf.append(stateFieldName);
                buf.append(";\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addEqualsObject() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic boolean equals(Object object) {\n");
        buf.append("\t\t");
        buf.append("if (object instanceof ");
        buf.append(className);
        buf.append(") {\n");
        buf.append("\t\t\t");
        buf.append(className);
        buf.append(" other = (");
        buf.append(className);
        buf.append(")object;\n");
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            String fieldName = "_" + i;
            boolean isPrimitive = pkc[i].javaType.isPrimitive();

            if (isPrimitive) {
                /*
                if (_0 != other._0) return false;
                */
                buf.append("\t\t\t");
                buf.append("if (");
                buf.append(fieldName);
                buf.append(" != other.");
                buf.append(fieldName);
                buf.append(") return false;\n");

            } else { // this is a object and it can be null
                /*
                if (_3 != null) {
                    if (!_3.equals(other._3)) return false;
                } else if (other._3 != null) return false;

                */
                buf.append("\t\t\t");
                buf.append("if (");
                buf.append(fieldName);
                buf.append(" != null){\n");
                buf.append("\t\t\t\t");
                buf.append("if (!");
                buf.append(fieldName);
                buf.append(".equals(other.");
                buf.append(fieldName);
                buf.append(")) return false;\n");
                buf.append("\t\t\t");
                buf.append("} else if (other.");
                buf.append(fieldName);
                buf.append(" != null) return false;\n");
            }
        }
        buf.append("\t\t\treturn true;\n");
        buf.append("\t\t} else {\n\t\t\treturn false;\n\t\t}\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addHashCode() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int hashCode() {\n");
        int noOfVars = 0;
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        noOfVars = pkc.length;
        for (int i = 0; i < noOfVars; i++) {
            Class fieldType = pkc[i].javaType;
            String fieldName = "_" + i;
            boolean isPrimitive = pkc[i].javaType.isPrimitive();

            /* If there is only one pk field we speed things up */
            if (noOfVars == 1) {
                if (isPrimitive) {
                    if (fieldType.equals(long.class)
                            || fieldType.equals(float.class)
                            || fieldType.equals(double.class)) {
                        /*
                        return (int)(877146213 * _0);
                        */
                        buf.append("\t\treturn (int)("+ cmd.classId);
                        buf.append(" * ");
                        buf.append(fieldName);
                        buf.append(");\n");
                    } else if (fieldType.equals(boolean.class)){
                        /*
                        return _0 ? 1 : -1;
                        */
                        buf.append("\t\treturn ");
                        buf.append(fieldName);
                        buf.append(" ? 1 : -1;\n");

                    } else {
                        /*
                        return 877146213 * _0;
                        */
                        buf.append("\t\treturn " + cmd.classId);
                        buf.append(" * ");
                        buf.append(fieldName);
                        buf.append(";\n");
                    }
                } else { // this is a object and it can be null
                    /*
                    return 877146213 * _0.hashCode(); ???????
                    */
                    buf.append("\t\treturn ");
                    buf.append(fieldName);
                    buf.append(".hashCode();\n");
                }
            } else { /** We have multiple fields */
                if (i == 0) {
                    buf.append("\t\tint hashCode = 0;\n");
                }
                if (isPrimitive) {
                    if (fieldType.equals(long.class)
                            || fieldType.equals(float.class)
                            || fieldType.equals(double.class)) {
                        /*
                        hashCode += _4;
                        */
                        buf.append("\t\thashCode += ");
                        buf.append(fieldName);
                        buf.append(";\n");
                    } else if (fieldType.equals(boolean.class)) {
                        /*
                        hashCode += _1 ? 1 : -1;
                        */
                        buf.append("\t\thashCode +=  ");
                        buf.append(fieldName);
                        buf.append(" ? 1 : -1;\n");

                    } else {
                        /*
                        hashCode += _4;
                        */
                        buf.append("\t\thashCode += ");
                        buf.append(fieldName);
                        buf.append(";\n");
                    }
                } else { // this is a object and it can be null
                    /*
                    hashCode += _4;
                    */
                    buf.append("\t\thashCode += ");
                    buf.append(fieldName);
                    buf.append(".hashCode();\n");
                }
            }
        }
        if (noOfVars > 1) {
            buf.append("\t\treturn hashCode;\n\t}\n");
        } else {
            buf.append("\t}\n");
        }
        spec.addMethod(buf.toString());
    }

    /**
     * Add all PK field(s).
     */
    protected void addFields() {
        super.addFields();
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            spec.addField("public " + pkc[i].javaType.getName() + " _" + i);
            if (pkc[i].converter != null) {
                spec.addField("public static " +
                        pkc[i].converter.getClass().getName() + " " +
                        JDBC_CONVERTER_FIELD_PREFIX + i);
                spec.addField("public static " +
                        pkc[i].getClass().getName() + " jdbcCol_" + i);
            }
        }
    }

    protected void addCopyKeyFieldsObjects() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFields(Object[] data) {\n");
        JdbcColumn[] pkc = ((JdbcClass)currentCMD.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            Class fieldType = pkc[i].javaType;
            String fieldName = "_" + i;
            boolean isPrimitive = pkc[i].javaType.isPrimitive();

            if (isPrimitive) {
                /*
                _0 = ((Integer)data[0]).intValue();
                */
                buf.append("\t\t");
                buf.append(fieldName);
                buf.append(" = ((");
                buf.append((String)primitiveToWrapperTypes.get(fieldType));
                buf.append(")data["+i);
                buf.append("]).");
                buf.append((String) primitiveTypesToValue.get(fieldType));
                buf.append("();\n");
            } else {
                /*
                _3 = (String)data[3];
                */
                buf.append("\t\t");
                buf.append(fieldName);
                buf.append(" = (");
                buf.append(fieldType.getName());
                buf.append(")data[" + i);
                buf.append("];\n");
            }
        }
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String toString() {\n");
        if (cmd.isInHierarchy()) {
            buf.append(
                    "\t\tif (!resolved) {\n" +
                    "\t\t\tthrow BindingSupportImpl.getInstance().internal(\n" +
                    "\t\t\t\t\"Called 'toString()' on unresolved oid\");\n" +
                    "\t\t}\n");
        }
        buf.append("\t\tStringBuffer s = new StringBuffer();\n");
        if (currentCMD.isInHierarchy()) {
            buf.append("\t\ts.append(cmd.classId);\n");
        } else {
            buf.append("\t\ts.append(\""+ cmd.classId);
            buf.append("\");\n");
        }
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            String fieldName = "_" + i;
            buf.append("\t\ts.append(\"" + MDStatics.OID_STRING_SEPERATOR + "\");\n");
            if (pkc[i].javaTypeCode == MDStatics.CHAR) {
                buf.append("\t\ttry { s.append(");
                buf.append(fieldName);
                buf.append("); } catch (java.lang.Exception e) {}\n");
            } else {
                buf.append("\t\ts.append(");
                buf.append(fieldName);
                buf.append(");\n");
            }
        }
        buf.append("\t\treturn s.toString();\n\t}\n");
        spec.addMethod(buf.toString());
    }

}

