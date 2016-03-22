
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

import com.versant.core.metadata.generator.OIDSrcGenerator;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.vds.util.Loid;

/**
 * Adds VDS specific stuff to OID.
 */
public class VdsOIDGenerator extends OIDSrcGenerator {

    public VdsOIDGenerator(ModelMetaData jmd) {
        super(jmd);
    }

    public ClassSpec generateOID(ClassMetaData cmd) {
        ClassSpec spec = super.generateOID(cmd);
        spec.addImport(Loid.class.getName());
        return spec;
    }

    protected void addGetLongPrimaryKey() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic long getLongPrimaryKey() {\n");
        buf.append("\t\treturn _0;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addSetLongPrimaryKey() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void setLongPrimaryKey(long newPK) {\n");
        buf.append("\t\t_0 = newPK;\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addfillFromPK() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void fillFromPK(Object pk) {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCompareTo() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int compareTo(Object o) {\n");
        buf.append("\t\tOID other = (OID)o;\n");
        buf.append("\t\tint diff = "+ cmd.top.index + " - other.getBaseClassMetaData().index;\n");
        buf.append("\t\tif (diff != 0)  return diff;\n");
        buf.append("\t\tif (other.isNew()) return 1;\n");
        buf.append("\t\telse {\n");
        buf.append("\t\t\t"+ className +" original = ("+ className +")other;\n");
        buf.append("\t\t\tlong x = _0 - original._0;");
        buf.append("\t\t\treturn x < 0 ? -1 : (x > 0 ? 1 : 0);\n");
        buf.append("\t\t}\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToPKString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String toPkString() {\n");
        buf.append("\t\tStringBuffer s = new StringBuffer();\n");
        buf.append("\t\ts.append(Loid.asString(_0));\n");
        buf.append("\t\treturn s.toString();\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addToSString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic String toSString() {\n");
        buf.append("\t\tStringBuffer s = new StringBuffer(\"OID:"+
                currentCMD.qname +" \");\n");
        buf.append("\t\ts.append(Loid.asString(_0));\n");
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
        buf.append("\t\tcopy._0 = _0;\n");
        buf.append("\t\treturn copy;\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsUpdate() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFieldsUpdate(State state) {\n");
        buf.append("\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addCopyKeyFieldsFromState() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFields(State state) {\n");
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
        buf.append("\t\t\treturn _0 == other._0;\n");
        buf.append("\t\t} else {\n\t\t\treturn false;\n\t\t}\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addHashCode() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic int hashCode() {\n");
        buf.append("\t\treturn (int)_0;\n\t}\n");
        spec.addMethod(buf.toString());
    }

    /**
     * Add all PK field(s).
     */
    protected void addFields() {
        super.addFields();
        spec.addField("public long _0");
    }

    protected void addCopyKeyFieldsObjects() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tpublic void copyKeyFields(Object[] data) {\n");
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
        buf.append("\t\ts.append(\"" + MDStatics.OID_STRING_SEPERATOR + "\");\n");
        buf.append("\t\ts.append(_0);\n");
        buf.append("\t\treturn s.toString();\n\t}\n");
        spec.addMethod(buf.toString());
    }

    protected void addWriteExternalImp(StringBuffer buf) {
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            super.addWriteExternalImp(buf);
        }
    }

    protected void addReadExternalImp(StringBuffer buf) {
        if (currentCMD.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            super.addReadExternalImp(buf);
        }
    }

}

