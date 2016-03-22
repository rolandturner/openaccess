
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

/**
 * For testing missing fields on insert bug.
 * @keep-all
 */
public class BigSub extends BigBase {

    private static final int FIELD_COUNT = 33;

    private String a0sub;
    private String b1sub;
    private String c2sub;
    private String d3sub;
    private String e4sub;
    private String f5sub;
    private String g6sub;
    private String h7sub;
    private String i8sub;
    private String j9sub;
    private String k10sub;
    private String l11sub;
    private String m12sub;
    private String n13sub;
    private String o14sub;
    private String p15sub;
    private String q16sub;
    private String r17sub;
    private String s18sub;
    private String t19sub;
    private String u20sub;
    private String v21sub;
    private String w22sub;
    private String x23sub;
    private String y24sub;
    private String z25sub;
    private String a26sub;
    private String b27sub;
    private String c28sub;
    private String d29sub;
    private String e30sub;
    private String f31sub;
    private String g32sub;

    public BigSub(int pk, String s) throws Exception {
        super(pk, s);
    }

    public void set(String s) throws Exception {
        super.set(s);
        set(BigSub.class, "sub", s);
    }

    public void check(String s) throws Exception {
        super.check(s);
        check(BigSub.class, "sub", s);
    }

    public void setA0sub(String a0sub) {
        this.a0sub = a0sub;
    }

    public void setB1sub(String b1sub) {
        this.b1sub = b1sub;
    }

    public void setC2sub(String c2sub) {
        this.c2sub = c2sub;
    }

    public void setD3sub(String d3sub) {
        this.d3sub = d3sub;
    }

    public void setE4sub(String e4sub) {
        this.e4sub = e4sub;
    }

    public void setF5sub(String f5sub) {
        this.f5sub = f5sub;
    }

    public void setG6sub(String g6sub) {
        this.g6sub = g6sub;
    }

    public void setH7sub(String h7sub) {
        this.h7sub = h7sub;
    }

    public void setI8sub(String i8sub) {
        this.i8sub = i8sub;
    }

    public void setJ9sub(String j9sub) {
        this.j9sub = j9sub;
    }

    public void setK10sub(String k10sub) {
        this.k10sub = k10sub;
    }

    public void setL11sub(String l11sub) {
        this.l11sub = l11sub;
    }

    public void setM12sub(String m12sub) {
        this.m12sub = m12sub;
    }

    public void setN13sub(String n13sub) {
        this.n13sub = n13sub;
    }

    public void setO14sub(String o14sub) {
        this.o14sub = o14sub;
    }

    public void setP15sub(String p15sub) {
        this.p15sub = p15sub;
    }

    public void setQ16sub(String q16sub) {
        this.q16sub = q16sub;
    }

    public void setR17sub(String r17sub) {
        this.r17sub = r17sub;
    }

    public void setS18sub(String s18sub) {
        this.s18sub = s18sub;
    }

    public void setT19sub(String t19sub) {
        this.t19sub = t19sub;
    }

    public void setU20sub(String u20sub) {
        this.u20sub = u20sub;
    }

    public void setV21sub(String v21sub) {
        this.v21sub = v21sub;
    }

    public void setW22sub(String w22sub) {
        this.w22sub = w22sub;
    }

    public void setX23sub(String x23sub) {
        this.x23sub = x23sub;
    }

    public void setY24sub(String y24sub) {
        this.y24sub = y24sub;
    }

    public void setZ25sub(String z25sub) {
        this.z25sub = z25sub;
    }

    public void setA26sub(String a26sub) {
        this.a26sub = a26sub;
    }

    public void setB27sub(String b27sub) {
        this.b27sub = b27sub;
    }

    public void setC28sub(String c28sub) {
        this.c28sub = c28sub;
    }

    public void setD29sub(String d29sub) {
        this.d29sub = d29sub;
    }

    public void setE30sub(String e30sub) {
        this.e30sub = e30sub;
    }

    public void setF31sub(String f31sub) {
        this.f31sub = f31sub;
    }

    public void setG32sub(String g32sub) {
        this.g32sub = g32sub;
    }

}
