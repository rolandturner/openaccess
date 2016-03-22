
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
package com.versant.core.jdo.query;

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.Debug;
import com.versant.core.common.OID;
//import org.apache.log4j.Category;

import javax.jdo.spi.PersistenceCapable;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 */
public class Field implements Comparable{
    public static final int TYPE_COLLECTION=1;
    public static final int TYPE_ARRAY=2;
    public static final int TYPE_STRING=3;
    public static final int TYPE_PC=4;
    public static final int TYPE_BOOLEAN=5;
    public static final int TYPE_DECIMAL=6;
    public static final int TYPE_CHAR=12;
    public static final int TYPE_MAP=13;
    public static final int TYPE_REF=14;
    public static final int TYPE_TX=15;
    public static final int TYPE_BOUND_VAR=16;
    public static final int TYPE_UNBOUND_VAR=17;
    public static final int TYPE_CAST=19;
    public static final int TYPE_OBJECT=20;
//    public static final int TYPE_OID=21;


    public int type;
    public FieldMetaData fieldMetaData;
    public ClassMetaData classMetaData;
    public PersistenceCapable pcValue;
//    public OID oidValue;
    public Object value;
    public boolean bValue;
    public BigDecimal dValue;
    public String sValue;
    public char cValue;
    public ArrayList collection;
    public String name;

    public Field(FieldMetaData fieldMetaData) {
        this.fieldMetaData = fieldMetaData;
    }

    public Field(int type) {
        this.type = type;
    }

    public Field() {
    }

    public String getType(){
        switch(type){
            case TYPE_COLLECTION:
                return "collection";
            case TYPE_OBJECT:
                return "object";
//            case TYPE_OID:
//                return "oid";
            case TYPE_ARRAY:
                return "array";
            case TYPE_STRING:
                return "string";
            case TYPE_PC:
                return "PersistenceCapable";
            case TYPE_BOOLEAN:
                return "boolean";
            case TYPE_DECIMAL:
                return "decimal";
            case TYPE_CHAR:
                return "char";
            case TYPE_MAP:
                return "map";
            case TYPE_TX:
                return "transactional";
            case TYPE_REF:
                return "reference";
            case TYPE_UNBOUND_VAR:
                return "unbound variable";
            case TYPE_BOUND_VAR:
                return "bound variable";
            case TYPE_CAST:
                return "cast";
            default:
                return null;
        }
    }

    public Object getValue(){
        switch(type){
            case TYPE_MAP:
            case TYPE_ARRAY:
            case TYPE_TX:
            case TYPE_REF:
            case TYPE_CAST:
            case TYPE_OBJECT:
                return value;
//            case TYPE_OID:
//                return oidValue;
            case TYPE_PC:
                return pcValue;
            case TYPE_BOOLEAN:
                return new Boolean(bValue);
            case TYPE_DECIMAL:
                return dValue;
            case TYPE_CHAR:
                return new Character(cValue);
            case TYPE_STRING:
                return sValue;
            case TYPE_BOUND_VAR:
            case TYPE_COLLECTION:
                return collection;
            default:
                return null;
        }
    }
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("Type = ");
        buffer.append(getType());
        buffer.append(" value = ");
        switch(type){
            case TYPE_MAP:
            case TYPE_ARRAY:
            case TYPE_TX:
            case TYPE_REF:
            case TYPE_CAST:
            case TYPE_OBJECT:
                buffer.append(value);
                break;
            case TYPE_PC:
                buffer.append(pcValue);
//            case TYPE_OID:
//                buffer.append(oidValue);
            case TYPE_BOOLEAN:
                buffer.append(bValue);
                break;
            case TYPE_DECIMAL:
                buffer.append(dValue);
                break;
            case TYPE_CHAR:
                buffer.append(cValue);
                break;
            case TYPE_STRING:
                buffer.append(sValue);
                break;
            case TYPE_BOUND_VAR:
            case TYPE_COLLECTION:
                buffer.append(collection);
            default:
                buffer.append(" --- ");
                break;
        }
        buffer.append(" FieldMetaData = ");
        buffer.append(fieldMetaData);
        return buffer.toString();
    }
    public void dump(String indent){
        if (Debug.DEBUG) {
            Debug.OUT.println(indent+ this.toString());
        }
    }

    public void clear(){
        fieldMetaData = null;
        collection = null;
        classMetaData = null;
        name = null;
        dValue = null;
        sValue = null;
        pcValue = null;
//        oidValue = null;
    }

    public int compareTo(Object o) {
        Comparable a = (Comparable)getValue();
        Comparable b = null;
        if(o instanceof Field){
            b = (Comparable)((Field)o).getValue();
        }else{
            b = (Comparable)o;
        }
        if (a == null) {
            a = "";
        }
        if (b == null) {
            b = "";
        }
        return a.compareTo(b);
    }


}
