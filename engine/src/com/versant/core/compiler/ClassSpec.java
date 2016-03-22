
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
package com.versant.core.compiler;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Source code based specification for a class to be generated. This
 * essentially just keeps the interfaces implemented, fields and methods
 * in separate lists of String's so it is easy for different parts of the
 * system to add code to the same class.
 */
public class ClassSpec {

    private String pkg;
    private String name;
    private String baseClass;
    private boolean finalClass;
    private List imports = new ArrayList();
    private List interfaces = new ArrayList();
    private List fields = new ArrayList();
    private List methods = new ArrayList();

    public ClassSpec(String pkg, String name, String baseClass) {
        this.pkg = pkg;
        this.name = name;
        this.baseClass = baseClass;
        finalClass = true;
    }

    public String getPkg() {
        return pkg;
    }

    public String getName() {
        return name;
    }

    public String getQName() {
        return pkg == null ? name : pkg + "." + name;
    }

    public String getBaseClass() {
        return baseClass;
    }

    public boolean isFinalClass() {
        return finalClass;
    }

    public void setFinalClass(boolean finalClass) {
        this.finalClass = finalClass;
    }

    public void addImport(String name) {
        imports.add(name);
    }

    public void addInterface(String name) {
        interfaces.add(name);
    }

    public void addField(String name) {
        fields.add(name);
    }

    public void addMethod(String name) {
        methods.add(name);
    }

    public String toString() {
        return getQName();
    }

    /**
     * Return the complete source code for this class.
     */
    public String toSrcCode() {
        StringBuffer s = new StringBuffer();
        if (pkg != null) {
            s.append("package ");
            s.append(pkg);
            s.append(";\n\n");
        }
        int n = imports.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                s.append("import ");
                s.append((String)imports.get(i));
                s.append(";\n");
            }
            s.append('\n');
        }
        s.append("public ");
        if (finalClass) {
            s.append("final ");
        }
        s.append("class ");
        s.append(name);
        if (baseClass != null) {
            s.append(" extends ");
            s.append(baseClass);
        }
        n = interfaces.size();
        if (n > 0) {
            s.append("\n    implements ");
            s.append((String)interfaces.get(0));
            for (int i = 1; i < n; i++) {
                s.append(", ");
                s.append((String)interfaces.get(i));
            }
        }
        s.append(" {\n\n");
        n = fields.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                s.append("\t");
                s.append((String)fields.get(i));
                s.append(";\n");
            }
            s.append('\n');
        }
        n = methods.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                s.append((String)methods.get(i));
                s.append('\n');
            }
            s.append('\n');
        }
        s.append("}\n");
        return s.toString();
    }

    /**
     * Add imports for popular things in java.lang and java.math.
     */
    public void addImportsForJavaLang() {
        imports.addAll(Arrays.asList(new String[]{
            "java.lang.Integer",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Short",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Long",
            "java.lang.Boolean",
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.Object",
            "java.lang.Comparable",
            "java.lang.ClassNotFoundException",
            "java.lang.RuntimeException",
            "java.lang.NullPointerException",
            "java.lang.System",
            "java.lang.Runtime",
            "java.lang.NumberFormatException",
            "java.lang.Number",
            "java.math.BigInteger",
            "java.math.BigDecimal",
            }));
    }

}

