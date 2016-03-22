
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
package com.versant.core.jdo.junit.test2.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * For testing missing fields on insert bug.
 * @keep-all
 */
public class BigBase {

    private static final int FIELD_COUNT = 32;

    private int pk;
    private String a0;
    private String b1;
    private String c2;
    private String d3;
    private String e4;
    private String f5;
    private String g6;
    private String h7;
    private String i8;
    private String j9;
    private boolean k10;
    private boolean l11;
    private boolean m12;
    private String n13;
    private String o14;
    private String p15;
    private String q16;
    private String r17;
    private String s18;
    private String t19;
    private String u20;
    private String v21;
    private String w22;
    private String x23;
    private String y24;
    private String z25;
    private String a26;
    private String b27;
    private String c28;
    private String d29;
    private String e30;
    private String f31;
//    private String g32;

    public BigBase(int pk, String s) throws Exception {
        this.pk = pk;
        set(s);
    }

    public void set(String s) throws Exception {
        set(BigBase.class, "", s);
    }

    public void check(String s) throws Exception {
        System.out.println("BigBase.check " + a0);
        check(BigBase.class, "", s);
    }

    private String getFieldName(int i, String suffix) {
        char c = (char)('a' + (i % 26));
        return c + (i + suffix);
    }

    protected void set(Class cls, String suffix, String value) throws Exception {
        int n = getFieldCount(cls);
        for (int i = 0; i < n; i++) {
            String fname = getFieldName(i, suffix);
            String mname = "set" + Character.toUpperCase(fname.charAt(0)) + fname.substring(1);
            Method m = cls.getDeclaredMethod(mname, new Class[]{String.class});
            m.setAccessible(true);
            m.invoke(this, new Object[]{value + i + suffix});
        }
    }

    protected int getFieldCount(Class cls) throws Exception {
        Field f = cls.getDeclaredField("FIELD_COUNT");
        f.setAccessible(true);
        int n = ((Integer)f.get(null)).intValue();
        return n;
    }

    protected void check(Class cls, String suffix, String value) throws Exception {
        int n = getFieldCount(cls);
        for (int i = 0; i < n; i++) {
            String fname = getFieldName(i, suffix);
            Field f = cls.getDeclaredField(fname);
            f.setAccessible(true);
            Object o = f.get(this);
            if (o == null) {
                throw new IllegalStateException(fname + " is null");
            }
            if (o instanceof Boolean) continue;
            String e = value + i + suffix;
            if (!o.equals(e)) {
                throw new IllegalStateException(fname + ": expected '" + e +
                        "' got '" + o + "'");
            }
        }
    }

    public void setA0(String a0) {
        this.a0 = a0;
    }

    public void setB1(String b1) {
        this.b1 = b1;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public void setD3(String d3) {
        this.d3 = d3;
    }

    public void setE4(String e4) {
        this.e4 = e4;
    }

    public void setF5(String f5) {
        this.f5 = f5;
    }

    public void setG6(String g6) {
        this.g6 = g6;
    }

    public void setH7(String h7) {
        this.h7 = h7;
    }

    public void setI8(String i8) {
        this.i8 = i8;
    }

    public void setJ9(String j9) {
        this.j9 = j9;
    }

    public void setK10(String k10) {
        this.k10 = true; //k10;
    }

    public void setL11(String l11) {
        this.l11 = true; //l11;
    }

    public void setM12(String m12) {
        this.m12 = true; //m12;
    }

    public void setN13(String n13) {
        this.n13 = n13;
    }

    public void setO14(String o14) {
        this.o14 = o14;
    }

    public void setP15(String p15) {
        this.p15 = p15;
    }

    public void setQ16(String q16) {
        this.q16 = q16;
    }

    public void setR17(String r17) {
        this.r17 = r17;
    }

    public void setS18(String s18) {
        this.s18 = s18;
    }

    public void setT19(String t19) {
        this.t19 = t19;
    }

    public void setU20(String u20) {
        this.u20 = u20;
    }

    public void setV21(String v21) {
        this.v21 = v21;
    }

    public void setW22(String w22) {
        this.w22 = w22;
    }

    public void setX23(String x23) {
        this.x23 = x23;
    }

    public void setY24(String y24) {
        this.y24 = y24;
    }

    public void setZ25(String z25) {
        this.z25 = z25;
    }

    public void setA26(String a26) {
        this.a26 = a26;
    }

    public void setB27(String b27) {
        this.b27 = b27;
    }

    public void setC28(String c28) {
        this.c28 = c28;
    }

    public void setD29(String d29) {
        this.d29 = d29;
    }

    public void setE30(String e30) {
        this.e30 = e30;
    }

    public void setF31(String f31) {
        this.f31 = f31;
    }

//    public void setG32(String g32) {
//        this.g32 = g32;
//    }

    public static final class ID implements Serializable {

        public int pk;

        public ID() {
        }

        public ID(String s) {
            pk = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (pk != id.pk) return false;

            return true;
        }

        public int hashCode() {
            return pk;
        }

        public String toString() {
            return Integer.toString(pk);
        }

    }

}
