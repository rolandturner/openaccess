
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
package com.versant.core.jdo.junit.test0;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.*;
import com.versant.core.jdo.junit.TestFailedException;

import javax.jdo.PersistenceManager;
import javax.jdo.Extent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Iterator;

/**
 * Test for arrays[].
 *
 * @keep-all
 */
public class TestArrays extends VersantTestCase {

    public TestArrays(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testPrimArray",
            "testGuiStuff",
            "testDoubleArray",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestArrays(a[i]));
        }
        return s;
    }

    /**
     * Test insert, select and update for arrays of all of the primitive types.
     * The amount of data is reduced so the tests run through against SAP DB.
     * SAP DB is broken if an insert includes more than one BLOB column and
     * exceeds approx 64K.
     */
    public void testPrimArray() throws Exception {
        String dn = getSubStoreInfo().getDataStoreType();
        if (dn.equals("informixse")) return;
        boolean sapdb = false;
        if (dn.equals("sapdb") /*|| dn.equals("cache")*/) {
            sapdb = true;
        }


        // create an instance with a field of each primitive array[] type
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        PrimArray p = new PrimArray();
        int n = 1024 * 10 / (sapdb ? 10 : 1);
        byte[] ab = createByteArray(n);
        short[] as = createShortArray(n);
        int[] ai = createIntArray(n);
        long[] al = createLongArray(n);
        boolean[] abool = createBooleanArray(n);
        char[] ac = createCharArray(n);
        float[] af = createFloatArray(n);
        double[] ad = createDoubleArray(n);
        p.setByteArray(ab);
        p.setShortArray(as);
        p.setIntArray(ai);
        p.setLongArray(al);
        p.setBooleanArray(abool);
        p.setCharArray(ac);
        p.setFloatArray(af);
        p.setDoubleArray(ad);
        pm.makePersistent(p);
        Object oid = pm.getObjectId(p);
        pm.currentTransaction().commit();
        pm.close();

        // get it back and check it
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (PrimArray)pm.getObjectById(oid, true);
        check(p.getByteArray(), ab);
        check(p.getShortArray(), as);
        check(p.getIntArray(), ai);
        check(p.getLongArray(), al);
        check(p.getBooleanArray(), abool);
        check(p.getCharArray(), ac);
        check(p.getFloatArray(), af);
        check(p.getDoubleArray(), ad);

        // modify it
        n = 1024 * 8 / (sapdb ? 10 : 1);
        ab = createByteArray(n);
        as = createShortArray(n);
        ai = createIntArray(n);
        al = createLongArray(n);
        abool = createBooleanArray(n);
        ac = createCharArray(n);
        af = createFloatArray(n);
        ad = createDoubleArray(n);
        p.setByteArray(ab);
        p.setShortArray(as);
        p.setIntArray(ai);
        p.setLongArray(al);
        p.setBooleanArray(abool);
        p.setCharArray(ac);
        p.setFloatArray(af);
        p.setDoubleArray(ad);
        pm.currentTransaction().commit();
        pm.close();

        // get it back and check it
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (PrimArray)pm.getObjectById(oid, true);
        check(p.getByteArray(), ab);
        check(p.getShortArray(), as);
        check(p.getIntArray(), ai);
        check(p.getLongArray(), al);
        check(p.getBooleanArray(), abool);
        check(p.getCharArray(), ac);
        check(p.getFloatArray(), af);
        check(p.getDoubleArray(), ad);
        pm.currentTransaction().commit();
        pm.close();
    }

    private byte[] createByteArray(int n) {
        byte[] a = new byte[n];
        Random r = new Random(n);
        r.nextBytes(a);
        return a;
    }

    private short[] createShortArray(int n) {
        short[] a = new short[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = (short)r.nextInt();
        return a;
    }

    private int[] createIntArray(int n) {
        int[] a = new int[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = r.nextInt();
        return a;
    }

    private long[] createLongArray(int n) {
        long[] a = new long[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = r.nextLong();
        return a;
    }

    private boolean[] createBooleanArray(int n) {
        boolean[] a = new boolean[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = r.nextBoolean();
        return a;
    }

    private char[] createCharArray(int n) {
        char[] a = new char[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = (char)r.nextInt();
        return a;
    }

    private float[] createFloatArray(int n) {
        float[] a = new float[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = r.nextFloat();
        return a;
    }

    private double[] createDoubleArray(int n) {
        double[] a = new double[n];
        Random r = new Random(n);
        for (int i = 0; i < n; i++) a[i] = r.nextDouble();
        return a;
    }

    protected void check(byte[] got, byte[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("bytes differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(short[] got, short[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("shorts differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(int[] got, int[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("ints differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(long[] got, long[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("longs differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(boolean[] got, boolean[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("booleans differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(char[] got, char[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("chars differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(float[] got, float[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("floats differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(double[] got, double[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                throw new TestFailedException("doubles differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    protected void check(String[][] got, String[][] expected) {
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < got.length; j++) {
                String[] strings = got[j];

            }

            if (expected[i] != got[i]) {
                throw new TestFailedException("doubles differ at index " + i +
                        "(" + expected[i] + " != " + got[i] + ")");
            }
        }
    }

    public void testDoubleArray() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        GuiStuff gui = new GuiStuff();
        String[][] expected = new String[2][2];
        expected[0][0] = "00";
        expected[0][1] = "01";
        expected[1][0] = "10";
        expected[1][1] = "11";

        gui.setN2Array(expected);
        pm.makePersistent(gui);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(GuiStuff.class,true);
        for (Iterator iter = extent.iterator(); iter.hasNext();) {
            GuiStuff gui1 = (GuiStuff) iter.next();
            String[][] got = gui1.getN2Array();
            System.out.println(" got[0][0] = " + got[0][0]);
            System.out.println(" got[0][1] = " + got[0][1]);
            System.out.println(" got[1][0] = " + got[1][0]);
            System.out.println(" got[1][1] = " + got[1][1]);
            if (!expected[0][0].equals(got[0][0]))
                throw new TestFailedException("expected[0][0] = " + expected[0][0] + " && got[0][0] = "+ got[0][0]);
            if (!expected[0][1].equals(got[0][1]))
                throw new TestFailedException("expected[0][1] = " + expected[0][1] + " && got[0][1] = "+ got[0][1]);
            if (!expected[1][0].equals(got[1][0]))
                throw new TestFailedException("expected[1][0] = " + expected[1][0] + " && got[1][0] = "+ got[1][0]);
            if (!expected[1][1].equals(got[1][1]))
                throw new TestFailedException("expected[1][1] = " + expected[1][1] + " && got[1][1] = "+ got[1][1]);



        }

        pm.currentTransaction().commit();
        pm.close();

    }

    public void testGuiStuff() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        GuiStuff gui = new GuiStuff();
        pm.makePersistent(gui);
        Person3 per1 = new Person3();
        per1.setName("Carl");
        Person p1 = new Person("bla1");
        Person p2 = new Person("bla2");
        Person p3 = new Person("bla3");
        Person p4 = new Person("bla4");
        Person p5 = new Person("bla5");
        Person p6 = new Person("bla6");
        Person p7 = new Person("bla7");
        pm.makePersistent(p1);
        pm.makePersistent(p2);
        pm.makePersistent(p3);
        pm.makePersistent(p4);
        pm.makePersistent(p5);
        pm.makePersistent(p6);
        pm.makePersistent(p7);

        per1.addFriends(p1);
        per1.addFriends(p2);
        per1.addFriends(p3);
        per1.addFriends(p4);
        Person3 per2 = new Person3();
        per2.setName("Jaco");
        per2.addFriends(p5);
        per2.addFriends(p6);
        per2.addFriends(p7);
        Person3 per3 = new Person3();
        per3.setName("Dave");
        Person3 per4 = new Person3();
        per4.setName("Alex");
        Person3 per5 = new Person3();
        per5.setName("Manie");

        gui.getPCTpprim().put(per1, new Integer(1));
        gui.getPCTpprim().put(per2, new Integer(2));
        gui.getPCTpprim().put(per3, new Integer(3));
        gui.getPCTpprim().put(per4, new Integer(4));
        gui.getPCTpprim().put(per5, new Integer(5));

        gui.getPrimTpPC().put(new Integer(10), per1);
        gui.getPrimTpPC().put(new Integer(20), per2);
        gui.getPrimTpPC().put(new Integer(30), per3);
        gui.getPrimTpPC().put(new Integer(40), per4);
        gui.getPrimTpPC().put(new Integer(50), per5);

        gui.getPrimTpprim().put(new Integer(1), new Date());
        gui.getPrimTpprim().put(new Integer(2), new Date());
        gui.getPrimTpprim().put(new Integer(3), new Date());
        gui.getPrimTpprim().put(new Integer(4), new Date());
        gui.getPrimTpprim().put(new Integer(5), new Date());

        gui.getPrim().add(new Integer(1));
        gui.getPrim().add(new Integer(2));
        gui.getPrim().add(new Integer(3));
        gui.getPrim().add(new Integer(4));
        gui.getPrim().add(new Integer(5));

        gui.getPrimStrings().add("Carl");
        gui.getPrimStrings().add("Jaco");
        gui.getPrimStrings().add("Dave");
        gui.getPrimStrings().add("Alex");
        gui.getPrimStrings().add("Manie");

        Stuff stuff1 = new Stuff("1");
        Stuff stuff2 = new Stuff("2");
        Stuff stuff3 = new Stuff("3");
        Stuff stuff4 = new Stuff("4");
        Stuff stuff5 = new Stuff("5");
        Stuff stuff6 = new Stuff("6");
        Stuff stuff7 = new Stuff("7");

        gui.getStuffs().add(stuff1);
        gui.getStuffs().add(stuff2);
        gui.getStuffs().add(stuff3);
        gui.getStuffs().add(stuff4);
        gui.getStuffs().add(stuff5);
        gui.getStuffs().add(stuff6);
        gui.getStuffs().add(stuff7);
        gui.getStuffs().add(stuff7);
        gui.getStuffs().add(stuff7);

        DateFormat formatter = new SimpleDateFormat("dd MM yyyy");

        gui.setBefore(formatter.parse("28 06 1971"));
        gui.setAfter(formatter.parse("28 06 2045"));

        GuiStuff gui1 = new GuiStuff();
        pm.makePersistent(gui1);

        gui1.setBefore(formatter.parse("28 06 1971"));
        gui1.setAfter(formatter.parse("28 06 2000"));
        gui1.getStuffs().add(stuff1);
        gui1.getStuffs().add(stuff2);
        gui1.getStuffs().add(stuff3);
        gui1.getStuffs().add(stuff5);
        gui1.getStuffs().add(stuff6);
        gui1.getStuffs().add(stuff7);
        gui1.getStuffs().add(stuff7);
        gui1.getStuffs().add(stuff7);

        pm.currentTransaction().commit();
        pm.close();
    }

}

