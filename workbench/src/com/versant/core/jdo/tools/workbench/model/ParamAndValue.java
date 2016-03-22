
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
package com.versant.core.jdo.tools.workbench.model;


import com.versant.core.metadata.MDStaticUtils;

import com.versant.core.metadata.MDStatics;
import com.versant.core.jdo.query.ImportNode;
import com.versant.core.jdo.query.JDOQLParser;
import com.versant.core.jdo.query.JDOQLParserTokenManager;
import com.versant.core.jdo.query.JavaCharStream;
import com.versant.core.common.Debug;
import com.versant.core.util.CharBuf;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.StringTokenizer;
import java.util.Locale;
import java.io.StringReader;



/**
 * @keep-all
 */
public class ParamAndValue {

    private MdQuery mdQuery;
    private String declaration;
    private Object value;

    public ParamAndValue(MdQuery mdQuery) {
        this.mdQuery = mdQuery;
    }

    public void setDeclaration(String declaration) {
        if (declaration != null) {
            this.declaration = declaration.trim();
        }
        mdQuery.redoParams();

    }

    public void setPrivateDeclaration(String declaration) {
        if (declaration != null) {
            this.declaration = declaration.trim();
        }

    }

    public String getDeclaration() {
        return declaration;
    }

    public void setValue(Object value) {
        this.value = value;
        mdQuery.redoParams();
    }

    public void setPrivateValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    private Class getCls() {
        if (declaration == null || declaration.equals("")) {
            return null;
        }
        String type = null;

        try {
            type = declaration.substring(0, declaration.indexOf(" "));


			Class cls = MDStaticUtils.toSimpleClass(type);
            
            if (cls == null) {
                MdClass c = mdQuery.getMdClass().findClass(type);
                if (c == null) { // search imports
                    ImportNode[] importNodes = getParsedImports();
                    int len = importNodes.length;
                    for (int i = 0; i < len; i++) {
                        ImportNode im = importNodes[i];
                        if (im.all) {
                            c = mdQuery.getProject().findClass(im.name + type);
                            if (c != null) break;
                        }
                        if (type.equals(im.getClassName())) {
                            c = mdQuery.getProject().findClass(im.name);
                            break;
                        }
                    }
                }
                if (c != null) cls = c.getCls();
                if (cls == null) throw new ClassNotFoundException(type);
            }
            return cls;
        } catch (ClassNotFoundException e) {
            if (Debug.DEBUG) {
                e.printStackTrace(Debug.OUT);
            }
            throw new JDOUserException("Could not load class '" + type + "'");
        }
    }

    public ImportNode[] getParsedImports() {
        String imports = mdQuery.getImports();
        if (imports != null && imports.length() > 0) {
            JDOQLParser parser = new JDOQLParser(
                    new JDOQLParserTokenManager(
                            new JavaCharStream(new StringReader(imports))));
            try {
                return parser.declareImports();
            } catch (com.versant.core.jdo.query.ParseException e) {
                return new ImportNode[0];
            }
        } else {
            return new ImportNode[0];
        }
    }

    
    public Object getResolvedValue(PersistenceManager pm, String dateFormat) {
        if (declaration == null || declaration.equals("") || value == null) {
            return null;
        }
        Object val = value;
        Class cls = getCls();
        if (value instanceof String && cls != String.class) {
            String sValue = (String)value;
            int type = MDStaticUtils.toTypeCode(cls);
            switch (type) {
                case MDStatics.BOOLEAN:
                case MDStatics.BOOLEANW:
                    val = new Boolean(sValue);
                    break;
                case MDStatics.BYTE:
                case MDStatics.BYTEW:
                    val = new Byte(sValue);
                    break;
                case MDStatics.SHORT:
                case MDStatics.SHORTW:
                    val = new Short(sValue);
                    break;
                case MDStatics.CHAR:
                case MDStatics.CHARW:
                    val = new Character(sValue.toCharArray()[0]);
                    break;
                case MDStatics.INT:
                case MDStatics.INTW:
                    val = new Integer(sValue);
                    break;
                case MDStatics.LONG:
                case MDStatics.LONGW:
                    val = new Long(sValue);
                    break;
                case MDStatics.FLOAT:
                case MDStatics.FLOATW:
                    val = new Float(sValue);
                    break;
                case MDStatics.DOUBLE:
                case MDStatics.DOUBLEW:
                    val = new Double(sValue);
                    break;
                case MDStatics.DATE:
                    SimpleDateFormat formater = new SimpleDateFormat(
                            dateFormat);
                    try {
                        val = formater.parse(sValue);
                    } catch (ParseException e) {
                        throw new JDOUserException("Could not parse " +
                                sValue + " from format '" +
                                dateFormat + "'");
                    }
                    break;
                case MDStatics.BIGINTEGER:
                    val = new BigInteger(sValue);
                    break;
                case MDStatics.BIGDECIMAL:
                    val = new BigDecimal(sValue);
                    break;
                case MDStatics.LOCALE:
                    val = toLocale(sValue);
                    break;
                default:
                    val = getOIDWrapper(sValue, pm);
                    break;
            }
        }
        return val;
    }
 

    public String getStringName() {
        return declaration.substring(declaration.indexOf(" ")).trim();
    }

    public String getStringType() {
        return declaration.substring(0, declaration.indexOf(" ", 1)).trim();
    }

    public String getResolvedString(String dateFormat) {
        if (declaration == null || declaration.equals("")) {
            return null;
        }
        String name = getStringName();
        Class cls = getCls();
        String sValue = (String)value;
        int type = MDStaticUtils.toTypeCode(cls);
        switch (type) {
            case MDStatics.BOOLEAN:
            case MDStatics.BOOLEANW:

                return "Boolean " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Boolean(" + sValue.trim() + ");\n");

            case MDStatics.BYTE:
            case MDStatics.BYTEW:
                return "Byte " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Byte(" + sValue.trim() + ");\n");

            case MDStatics.SHORT:
            case MDStatics.SHORTW:
                return "Short " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Short(" + sValue.trim() + ");\n");

            case MDStatics.CHAR:
            case MDStatics.CHARW:
                return "Character " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Character('" + sValue.toCharArray()[0] + "');\n");

            case MDStatics.INT:
            case MDStatics.INTW:
                return "Integer " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Integer(" + sValue.trim() + ");\n");

            case MDStatics.LONG:
            case MDStatics.LONGW:
                return "Long " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Long(" + sValue.trim() + ");\n");

            case MDStatics.FLOAT:
            case MDStatics.FLOATW:
                return "Float " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Float(" + sValue.trim() + ");\n");

            case MDStatics.DOUBLE:
            case MDStatics.DOUBLEW:
                return "Double " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new Double(" + sValue.trim() + ");\n");

            case MDStatics.STRING:
                return "String " + name + (
                        value == null ?
                        " = null;\n" :
                        " = \"" + sValue + "\";\n");

            case MDStatics.DATE:
                return "Date " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new java.text.SimpleDateFormat(\"" +
                        dateFormat + "\").parse(\"" +
                        sValue.trim() + "\");\n");

            case MDStatics.BIGINTEGER:
                return "BigInteger " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new BigInteger(" + sValue.trim() + ");\n");

            case MDStatics.BIGDECIMAL:
                return "BigDecimal " + name + (
                        value == null ?
                        " = null;\n" :
                        " = new BigDecimal(" + sValue.trim() + ");\n");

            case MDStatics.LOCALE:
                return "Locale " + name + (
                        value == null ?
                        " = null;\n" :
                        toLocaleString(sValue.trim()));

            default:
                return (value == null ?
                        declaration + " = null;\n" :
                        getOIDString(sValue.trim()));
        }

    }

    private String getOIDString(String oid) {
        if (oid.indexOf(':') == -1) {
            Class cls = null;
            String type = getStringType();
            try {


				cls = MDStaticUtils.toSimpleClass(type);
                
                if (cls == null) {
                    MdClass c = mdQuery.getMdClass().findClass(type);
                    if (c == null) { // search imports
                        ImportNode[] importNodes = getParsedImports();
                        int len = importNodes.length;
                        for (int i = 0; i < len; i++) {
                            ImportNode im = importNodes[i];
                            if (im.all) {
                                c = mdQuery.getProject().findClass(
                                        im.name + type);
                                if (c != null) break;
                            }
                            if (type.equals(im.getClassName())) {
                                c = mdQuery.getProject().findClass(im.name);
                                break;
                            }
                        }
                    }
                    if (c != null) cls = c.getCls();
                    if (cls == null) throw new ClassNotFoundException(type);
                }
            } catch (ClassNotFoundException e) {
                throw new JDOUserException(
                        "Could not load class '" + type + "'");
            }

            String className = stripClassName(cls.getName());
            return className + " " + getStringName() + " = pm.getObjectById(\n\tpm.newObjectIdInstance(" + className + ".class,\"" + oid.trim() + "\"),\n\tfalse);\n";

        } else {
            StringTokenizer buffer = new StringTokenizer(oid, ":", false);
            Class cls = null;
            if (buffer.hasMoreTokens()) {
                String type = buffer.nextToken().trim();
                try {


					cls = MDStaticUtils.toSimpleClass(type);
                     
                    if (cls == null) {
                        MdClass c = mdQuery.getMdClass().findClass(type);
                        if (c == null) { // search imports
                            ImportNode[] importNodes = getParsedImports();
                            int len = importNodes.length;
                            for (int i = 0; i < len; i++) {
                                ImportNode im = importNodes[i];
                                if (im.all) {
                                    c = mdQuery.getProject().findClass(
                                            im.name + type);
                                    if (c != null) break;
                                }
                                if (type.equals(im.getClassName())) {
                                    c = mdQuery.getProject().findClass(im.name);
                                    break;
                                }
                            }
                        }
                        if (c != null) cls = c.getCls();
                        if (cls == null) throw new ClassNotFoundException(type);
                    }
                } catch (ClassNotFoundException e) {
                    throw new JDOUserException(
                            "Could not load class '" + type + "'");
                }

                if (buffer.hasMoreTokens()) {
                    String className = stripClassName(cls.getName());
                    return className + " " + getStringName() + " = pm.getObjectById(\n\tpm.newObjectIdInstance(" + className + ".class,\"" + buffer.nextToken().trim() + "\"),\n\tfalse);\n";

                }
            }
            return null;
        }
    }

    private String stripClassName(String className) {
        int index = className.lastIndexOf('.');
        if (index == -1) return className;
        return className.substring(index + 1, className.length());
    }

    
    private Object getOIDWrapper(String oid, PersistenceManager pm) {
        if (oid.indexOf(':') == -1) {
            Class cls = null;
            String type = getStringType();
            try {
                cls = MDStaticUtils.toSimpleClass(type);
                if (cls == null) {
                    MdClass c = mdQuery.getMdClass().findClass(type);
                    if (c == null) { // search imports
                        ImportNode[] importNodes = getParsedImports();
                        int len = importNodes.length;
                        for (int i = 0; i < len; i++) {
                            ImportNode im = importNodes[i];
                            if (im.all) {
                                c = mdQuery.getProject().findClass(
                                        im.name + type);
                                if (c != null) break;
                            }
                            if (type.equals(im.getClassName())) {
                                c = mdQuery.getProject().findClass(im.name);
                                break;
                            }
                        }
                    }
                    if (c != null) cls = c.getCls();
                    if (cls == null) throw new ClassNotFoundException(type);
                }
            } catch (ClassNotFoundException e) {
                throw new JDOUserException(
                        "Could not load class '" + type + "'");
            }

            OidWrapper w = new OidWrapper();
            w.setDisplayString(oid);
            w.setOid(pm.newObjectIdInstance(cls, oid.trim()));
            return w;
        } else {
            StringTokenizer buffer = new StringTokenizer(oid, ":", false);
            Class cls = null;
            if (buffer.hasMoreTokens()) {
                String type = buffer.nextToken().trim();
                try {
                    cls = MDStaticUtils.toSimpleClass(type);
                    if (cls == null) {
                        MdClass c = mdQuery.getMdClass().findClass(type);
                        if (c == null) { // search imports
                            ImportNode[] importNodes = getParsedImports();
                            int len = importNodes.length;
                            for (int i = 0; i < len; i++) {
                                ImportNode im = importNodes[i];
                                if (im.all) {
                                    c = mdQuery.getProject().findClass(
                                            im.name + type);
                                    if (c != null) break;
                                }
                                if (type.equals(im.getClassName())) {
                                    c = mdQuery.getProject().findClass(im.name);
                                    break;
                                }
                            }
                        }
                        if (c != null) cls = c.getCls();
                        if (cls == null) throw new ClassNotFoundException(type);
                    }
                } catch (ClassNotFoundException e) {
                    throw new JDOUserException(
                            "Could not load class '" + type + "'");
                }

                if (buffer.hasMoreTokens()) {
                    OidWrapper w = new OidWrapper();
                    w.setDisplayString(oid);
                    w.setOid(
                            pm.newObjectIdInstance(cls,
                                    buffer.nextToken().trim()));
                    return w;
                }
            }
        }
        return null;
    }
    

    private Locale toLocale(String s) {
        s = s.trim();
        if (s.length() == 0) return null;
        try {
            String lang = s.substring(0, 2);
            String country = null;
            if (s.length() > 2) {
                country = s.substring(2, 4);
            } else {
                country = "";
            }
            String variant = null;
            if (s.length() > 4) {
                variant = s.substring(4, 6);
            } else {
                variant = "";
            }
            return new Locale(lang, country, variant);
        } catch (IndexOutOfBoundsException x) {
            throw new JDOUserException("Invalid Locale value '" + s + "'");
        }
    }

    private String toLocaleString(String s) {
        s = s.trim();
        if (s.length() == 0) return " = null;\n";
        try {

            CharBuf buff = new CharBuf(" = new Locale(");

            String lang = s.substring(0, 2);
            String country = null;
            if (s.length() > 2) {
                country = s.substring(2, 4);
            } else {
                country = "";
            }
            String variant = null;
            if (s.length() > 4) {
                variant = s.substring(4, 6);
            } else {
                variant = "";
            }
            buff.append(
                    "\"" + lang + "\", \"" + country + "\", \"" + variant + "\");\n");
            return buff.toString();
        } catch (IndexOutOfBoundsException x) {
            throw new JDOUserException("Invalid Locale value '" + s + "'");
        }
    }

}
