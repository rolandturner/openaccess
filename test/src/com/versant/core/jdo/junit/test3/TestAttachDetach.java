
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
package com.versant.core.jdo.junit.test3;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.VersantDetachable;
import com.versant.core.jdo.VersantHelper;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.attachdetach.*;
import com.versant.core.jdo.junit.test3.model.attachdetach.externalizer.CreditCard;
import com.versant.core.jdo.junit.test3.model.attachdetach.externalizer.CardTypes;
import com.versant.core.jdo.junit.test3.model.attachdetach.demo.Country;
import com.versant.core.jdo.junit.test3.model.attachdetach.demo.Contact;
import com.versant.core.jdo.junit.test3.model.attachdetach.demo.Address;
import com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.A;
import com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.B;

import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jdo.JDOUserException;
import java.io.*;
import java.util.*;

public class TestAttachDetach extends VersantTestCase {

    public TestAttachDetach(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testAttachOfNonDetachedNonDirtyFields",
            "testCollectionWithDateEquals",
            "testSimple",
            "testCollections",
            "testSelfRefSingleTree",
            "testSelfRefCircularOne",
            "testSelfRefCircularMany",
            "testSelfRefTree",
            "testSelfRefSircularTree",
            "testSimpleAttach",
            "testSimpleAttachNew",
            "testRefAttach",
            "testRefAttachNew",
            "testRefAttachManages",
            "testRefShallowAttach",
            "testCollectionsAttach",
            "testSerializable",
            "testPolyRef",
            "testBug1113",
            "testDemo",
            "testDelete",
            "testExternalizer"
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestAttachDetach(a[i]));
        }
        return s;
    }

    public void testAttachOfNonDetachedNonDirtyFields() {
        VersantPersistenceManager pm = (VersantPersistenceManager) pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        DetAddressHolder detH = new DetAddressHolder();
        DetAddress det = new DetAddress();
        detH.getDefaultAdd().setStreet2("S2");
        detH.setAdd(det);
        det.setStreet1("CHANGED");
        pm.makePersistent(detH);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Collection detCol = new ArrayList();
        detCol.add(detH);
        detCol = pm.versantDetachCopy(detCol, null);
        try {
            ((DetAddressHolder)detCol.iterator().next()).getDefaultAdd();
            fail("Should throw noaccess");
        } catch (Exception e) {
            e.printStackTrace(System.out);  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.versantAttachCopy(detCol, true);
//        assertEquals("Only a single instance should be dirty", 1, pm.versantAllDirtyInstances().size());
        if (pm.isDirty()) {
            fail("Nothing should be dirty as there was no changes");
        }
        pm.close();
    }

    public void testCollectionWithDateEquals() {
        VersantPersistenceManager pm = (VersantPersistenceManager) pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        CollectionFields colFields = new CollectionFields();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 10; i++) {
            cal.set(Calendar.DATE, i + 1);
            ClassWithDateEquals cwd = new ClassWithDateEquals(cal.getTime());
            colFields.getDateSet().add(cwd);
        }
        pm.makePersistent(colFields);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(10, colFields.getDateSet().size());

        Collection detached = pm.versantDetachCopy(Collections.singleton(colFields), null);
        CollectionFields cf2 = (CollectionFields) detached.iterator().next();
        assertEquals(10, cf2.getDateSet().size());
        pm.close();
    }

    public void testSimple() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(1);
        a1.setBooleanField(true);
        a1.setCharField('a');
        a1.setByteField((byte)1);
        a1.setShortField((short)2);
        a1.setIntField(3);
        a1.setLongField(4);
        a1.setFloatField(5.6f);
        a1.setDoubleField(7.8);
        a1.setStringField("9");
        a1.setBooleansField(new boolean[]{true, false, true});
        a1.setCharsField(new char[]{1, 2, 3});
        a1.setBytesField(new byte[]{2, 3, 4});
        a1.setShortsField(new short[]{3, 4, 5});
        a1.setIntsField(new int[]{4, 5, 6});
        a1.setLongsField(new long[]{5, 6, 7});
        a1.setFloatsField(new float[]{1.2f, 3.4f, 5.6f});
        a1.setDoublesField(new double[]{2.3, 4.5, 6.7});
        checkSimple1(pm, a1);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        a1 = (SimpleFields)pm.getObjectById(JDOHelper.getObjectId(a1), false);
        checkSimple1(pm, a1);

        Collection defaultFG = pm.versantDetachCopy(
                Collections.singletonList(a1), "default");
        defaultFG = pm.versantDetachCopy(Collections.singletonList(a1),
                "default");
        Object o = defaultFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        SimpleFields a = (SimpleFields)o;
        a.getSimpleFieldsBaseId();
        checkSimpleDefault(a1, a);
        Collection errorFG = pm.versantDetachCopy(
                Collections.singletonList(a1), "hghghghghghghg");
        o = errorFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        a = (SimpleFields)o;
        checkSimpleDefault(a1, a);

        Collection some = pm.versantDetachCopy(Collections.singletonList(a1),
                "someFields");
        Iterator iterator = some.iterator();
        Assert.assertTrue(iterator.hasNext());
        o = iterator.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a = (SimpleFields)o;
        Assert.assertEquals((byte)1, a.getByteField());
        Assert.assertNotNull(a.getBytesField());
        Assert.assertEquals((short)2, a.getShortField());
        Assert.assertNotNull(a.getShortsField());
        Assert.assertEquals(3, a.getIntField());
        Assert.assertNotNull(a.getIntsField());
        try {
            a.isBooleanField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a.getBooleansField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a.getLongField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a.getLongsField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a.getStringField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a.getDoublesField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSimple1(VersantPersistenceManager pm,
            SimpleFields a1) throws IOException, ClassNotFoundException {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(a1), "allFields");
        checkSimple2(allFieldsFG, a1);
        allFieldsFG = serialize(allFieldsFG);
        checkSimple2(allFieldsFG, a1);
    }

    private void checkSimple2(Collection allFieldsFG, SimpleFields a1) {
        Assert.assertEquals(1, allFieldsFG.size());
        Object o = allFieldsFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        SimpleFields a2 = (SimpleFields)o;
        Assert.assertTrue(a1 != a2);
        Assert.assertEquals(a1.isBooleanField(), a2.isBooleanField());
        Assert.assertTrue(a1.isBooleanField());
        Assert.assertEquals(a1.getCharField(), a2.getCharField());
        Assert.assertEquals(a1.getByteField(), a2.getByteField());
        Assert.assertEquals(a1.getShortField(), a2.getShortField());
        Assert.assertEquals(a1.getIntField(), a2.getIntField());
        Assert.assertEquals(a1.getLongField(), a2.getLongField());
//todo        Assert.assertEquals(a1.getFloatField(), a2.getFloatField());
//todo        Assert.assertEquals(a1.getDoubleField(), a2.getDoubleField());
        Assert.assertTrue(a1.getStringField().equals(a2.getStringField()));
        Assert.assertTrue(a1.getBooleansField() != a2.getBooleansField());
        Assert.assertTrue(a1.getCharsField() != a2.getCharsField());
        Assert.assertTrue(a1.getBytesField() != a2.getBytesField());
        Assert.assertTrue(a1.getShortsField() != a2.getShortsField());
        Assert.assertTrue(a1.getIntsField() != a2.getIntsField());
        Assert.assertTrue(a1.getLongsField() != a2.getLongsField());
        Assert.assertEquals(3, a2.getIntsField().length);
        Assert.assertEquals(4, a2.getIntsField()[0]);
        Assert.assertEquals(5, a2.getIntsField()[1]);
        Assert.assertEquals(6, a2.getIntsField()[2]);
        Assert.assertTrue(a2.getBooleansField()[0]);
        Assert.assertFalse(a2.getBooleansField()[1]);
        Assert.assertTrue(a2.getBooleansField()[2]);
    }

    private void checkSimpleDefault(SimpleFields a1, SimpleFields a2) {
        Assert.assertTrue(a1 != a2);
        Assert.assertEquals(a1.isBooleanField(), a2.isBooleanField());
        Assert.assertTrue(a1.isBooleanField());
        Assert.assertEquals(a1.getCharField(), a2.getCharField());
        Assert.assertEquals(a1.getByteField(), a2.getByteField());
        Assert.assertEquals(a1.getShortField(), a2.getShortField());
        Assert.assertEquals(a1.getIntField(), a2.getIntField());
        Assert.assertEquals(a1.getLongField(), a2.getLongField());
//todo        Assert.assertEquals(a1.getFloatField(), a2.getFloatField());
//todo        Assert.assertEquals(a1.getDoubleField(), a2.getDoubleField());
        Assert.assertTrue(a1.getStringField().equals(a2.getStringField()));
        try {
            a2.getCharsField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a2.getBytesField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a2.getIntsField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        try {
            a2.getLongsField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    public void testCollections() throws IOException, ClassNotFoundException {
    	
    	if (!isManagedRelationshipSupported())
    		return;

        if ("informixse".equals(getDbName())) {
            logFilter("Many 2 many related");
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        CollectionChild c1 = new CollectionChild();
        c1.setNo(1);
        SimpleFields simpleFields = new SimpleFields(11, 1, "1",
                new int[]{1, 2});
        c1.setSimpleFields(simpleFields);
        CollectionChild c2 = new CollectionChild();
        c2.setNo(2);
        c2.setSimpleFields(simpleFields);
        CollectionFields b = new CollectionFields();
        b.getCollectionChildren()[0] = c1;
        b.getCollectionChildren()[1] = c2;
        b.getArrayList().add(c1);
        b.getArrayList().add(c2);
        b.getList().add(c1);
        b.getList().add(c2);
        b.getCollection().add(c1);
        b.getCollection().add(c2);
        b.getHashSet().add(c1);
        b.getHashSet().add(c2);
        Assert.assertEquals(2, b.getHashSet().size());
        b.getSet().add(c1);
        b.getSet().add(c2);
        Assert.assertEquals(2, b.getSet().size());
        b.getLinkedList().add(c1);
        b.getLinkedList().add(c2);
        b.getTreeSet().add(c1);
        b.getTreeSet().add(c2);
        b.getSortedSet().add(c1);
        b.getSortedSet().add(c2);
        b.getVector().add(c1);
        b.getVector().add(c2);
        b.getHashMap().put("1", c1);
        b.getHashMap().put("2", c2);
        b.getMap().put("1", c1);
        b.getMap().put("2", c2);
        b.getHashtable().put("1", c1);
        b.getHashtable().put("2", c2);
        b.getTreeMap().put("1", c1);
        b.getTreeMap().put("2", c2);
        b.getSortedMap().put("1", c1);
        b.getSortedMap().put("2", c2);
        pm.makePersistent(simpleFields);
        checkCollections1(pm, b, c1, simpleFields);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        b = (CollectionFields)pm.getObjectById(JDOHelper.getObjectId(b), false);
        checkCollections1(pm, b, c1, simpleFields);
        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkCollections1(VersantPersistenceManager pm,
            CollectionFields collectionFields, CollectionChild child,
            SimpleFields simple) throws IOException, ClassNotFoundException {
        ArrayList list = new ArrayList(3);
        list.add(collectionFields);
        list.add(child);
        list.add(simple);
        Object oid = JDOHelper.getObjectId(simple);
        checkCollections2(list, oid);
        pm.versantDetachCopy(Collections.singletonList(collectionFields),
                "allFields");
        pm.versantDetachCopy(Collections.singletonList(collectionFields),
                "allFields");
        Collection allFieldsFG = pm.versantDetachCopy(list, "allFields");
        checkCollections2(allFieldsFG, oid);
        allFieldsFG = serialize(allFieldsFG);
        checkCollections2(allFieldsFG, oid);

    }

    private void checkCollections2(Collection allFieldsFG, Object oid) {
        Assert.assertEquals(3, allFieldsFG.size());
        Iterator detIt = allFieldsFG.iterator();
        Assert.assertTrue(detIt.hasNext());
        Object o1 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        Object o2 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        Object o3 = detIt.next();
        Assert.assertTrue(o1 instanceof CollectionFields);
        Assert.assertTrue(o2 instanceof CollectionChild);
        Assert.assertTrue(o3 instanceof SimpleFields);
        CollectionFields b = (CollectionFields)o1;
        CollectionChild c = (CollectionChild)o2;
        SimpleFields d = (SimpleFields)o3;
        Object oid2 = JDOHelper.getObjectId(d);
        Assert.assertEquals(oid, oid2);
        Assert.assertNotNull(b.getCollectionChildren());
        Assert.assertNotNull(b.getArrayList());
        Assert.assertNotNull(b.getList());
        Assert.assertNotNull(b.getCollection());
        Assert.assertNotNull(b.getHashSet());
        Assert.assertNotNull(b.getSet());
        Assert.assertNotNull(b.getLinkedList());
        Assert.assertNotNull(b.getTreeSet());
        Assert.assertNotNull(b.getSortedSet());
        Assert.assertNotNull(b.getVector());
        Assert.assertNotNull(b.getHashMap());
        Assert.assertNotNull(b.getMap());
        Assert.assertNotNull(b.getHashtable());
        Assert.assertNotNull(b.getTreeMap());
        Assert.assertNotNull(b.getSortedMap());

        Assert.assertEquals(2, b.getCollectionChildren().length);
        Assert.assertEquals(2, b.getArrayList().size());
        Assert.assertEquals(2, b.getList().size());
        Assert.assertEquals(2, b.getCollection().size());
        Assert.assertEquals(2, b.getHashSet().size());
        Assert.assertEquals(2, b.getSet().size());
        Assert.assertEquals(2, b.getLinkedList().size());
        Assert.assertEquals(2, b.getSortedSet().size());
        Assert.assertEquals(2, b.getVector().size());
        Assert.assertEquals(2, b.getHashMap().size());
        Assert.assertEquals(2, b.getMap().size());
        Assert.assertEquals(2, b.getHashtable().size());
        Assert.assertEquals(2, b.getTreeMap().size());
        Assert.assertEquals(2, b.getSortedMap().size());

        o1 = b.getCollectionChildren()[0];
        o2 = b.getCollectionChildren()[1];
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        o1 = b.getArrayList().get(0);
        o2 = b.getArrayList().get(1);
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        o1 = b.getList().get(0);
        o2 = b.getList().get(1);
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        Iterator colIt = b.getCollection().iterator();
        o1 = colIt.next();
        o2 = colIt.next();
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        o1 = b.getLinkedList().get(0);
        o2 = b.getLinkedList().get(1);
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        o1 = b.getVector().get(0);
        o2 = b.getVector().get(1);
        checktestCollectionDetachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        Iterator iterator = b.getHashSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionDetachNoOrder(o1, o2, d);
        iterator = b.getSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionDetachNoOrder(o1, o2, d);
        iterator = b.getTreeSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionDetachNoOrder(o1, o2, d);
        iterator = b.getSortedSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionDetachNoOrder(o2, o1, d);
        o1 = b.getHashMap().get("1");
        o2 = b.getHashMap().get("2");
        checktestCollectionDetachNoOrder(o1, o2, d);
        o1 = b.getMap().get("1");
        o2 = b.getMap().get("2");
        checktestCollectionDetachNoOrder(o1, o2, d);
        o1 = b.getHashtable().get("1");
        o2 = b.getHashtable().get("2");
        checktestCollectionDetachNoOrder(o1, o2, d);
        o1 = b.getTreeMap().get("1");
        o2 = b.getTreeMap().get("2");
        checktestCollectionDetachNoOrder(o1, o2, d);
        o1 = b.getSortedMap().get("1");
        o2 = b.getSortedMap().get("2");
        checktestCollectionDetachNoOrder(o2, o1, d);

        CollectionChild[] collectionChildren = b.getCollectionChildren();
        int length = collectionChildren.length;
        for (int i = 0; i < length; i++) {
            o1 = collectionChildren[i];
            Assert.assertTrue(b.getArrayList().contains(o1));
            Assert.assertTrue(b.getList().contains(o1));
            Assert.assertTrue(b.getCollection().contains(o1));
            Assert.assertTrue(b.getLinkedList().contains(o1));
            Assert.assertTrue(b.getTreeSet().contains(o1));
            Assert.assertTrue(b.getSortedSet().contains(o1));
            Assert.assertTrue(b.getVector().contains(o1));
            Assert.assertTrue(b.getHashSet().contains(o1));
            Assert.assertTrue(b.getSet().contains(o1));
        }
//        Iterator colIt2 = b.getCollection().iterator();
        for (int i = 0; i < length; i++) {
            o1 = collectionChildren[i];
            Assert.assertSame(o1, b.getArrayList().get(i));
            Assert.assertSame(o1, b.getList().get(i));
//            Assert.assertSame(o1, colIt2.next());
            Assert.assertSame(o1, b.getLinkedList().get(i));
            Assert.assertSame(o1, b.getVector().get(i));
        }
        for (Iterator it = b.getHashMap().keySet().iterator(); it.hasNext();) {
            o1 = (Object)it.next();
            Object v1 = b.getHashMap().get(o1);
            Object v2 = b.getHashtable().get(o1);
            Object v3 = b.getTreeMap().get(o1);
            Object v4 = b.getSortedMap().get(o1);
            Object v5 = b.getMap().get(o1);
            Assert.assertSame(v1, v2);
            Assert.assertSame(v2, v3);
            Assert.assertSame(v3, v4);
            Assert.assertSame(v4, v5);
        }
    }

    private void checktestCollectionDetachOrder(Object o1, Object o2,
            SimpleFields o3) {
        checktestCollectionDetachNoOrder(o1, o2, o3);
        CollectionChild c1 = (CollectionChild)o1;
        CollectionChild c2 = (CollectionChild)o2;
        Assert.assertEquals(1, c1.getNo());
        Assert.assertEquals(2, c2.getNo());
    }

    private void checktestCollectionDetachNoOrder(Object o1, Object o2,
            SimpleFields o3) {
        Assert.assertNotNull(o1);
        Assert.assertNotNull(o2);
        Assert.assertTrue(o1 instanceof CollectionChild);
        Assert.assertTrue(o2 instanceof CollectionChild);
        CollectionChild c1 = (CollectionChild)o1;
        CollectionChild c2 = (CollectionChild)o2;
        Assert.assertTrue(c1 != c2);
        SimpleFields s1 = c1.getSimpleFields();
        Assert.assertNotNull(s1);
        SimpleFields s2 = c2.getSimpleFields();
        Assert.assertNotNull(s2);
        Assert.assertSame(s1, s2);
        Assert.assertSame(s1, o3);
        Assert.assertEquals(1, c1.getSimpleFields().getIntField());
        Assert.assertEquals(1, c2.getSimpleFields().getIntField());
        Assert.assertEquals("1", c1.getSimpleFields().getStringField());
        Assert.assertEquals("1", c2.getSimpleFields().getStringField());
    }

    public void testSelfRefSingleTree() {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        FetchGroupSelfRef singleTree14 = new FetchGroupSelfRef(14);
        FetchGroupSelfRef singleTree13 = new FetchGroupSelfRef(13,
                singleTree14);
        FetchGroupSelfRef singleTree12 = new FetchGroupSelfRef(12,
                singleTree13);
        FetchGroupSelfRef singleTree11 = new FetchGroupSelfRef(11,
                singleTree12);

        checkSelfRefSingleTree(pm, singleTree11);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        singleTree11 = (FetchGroupSelfRef)pm.getObjectById(
                JDOHelper.getObjectId(singleTree11), false);
        checkSelfRefSingleTree(pm, singleTree11);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSelfRefSingleTree(VersantPersistenceManager pm,
            FetchGroupSelfRef singleTree11) {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(singleTree11), "allFields");
        Assert.assertEquals(allFieldsFG.size(), 1);
        Object o = allFieldsFG.iterator().next();
        Assert.assertTrue(o instanceof FetchGroupSelfRef);
        FetchGroupSelfRef detach = (FetchGroupSelfRef)o;
        Assert.assertEquals(11, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(12, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(13, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(14, detach.getNo());
        Assert.assertNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(0, detach.getSelfRefList().size());
    }

    public void testSelfRefCircularOne() {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        FetchGroupSelfRef selfRef = new FetchGroupSelfRef(0);
        selfRef.setSelfRef(selfRef);

        checkSelfRefCircularOne(pm, selfRef);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        selfRef = (FetchGroupSelfRef)pm.getObjectById(
                JDOHelper.getObjectId(selfRef), false);
        checkSelfRefCircularOne(pm, selfRef);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSelfRefCircularOne(VersantPersistenceManager pm,
            FetchGroupSelfRef selfRef) {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(selfRef), "allFields");
        Assert.assertEquals(allFieldsFG.size(), 1);
        Object o = allFieldsFG.iterator().next();
        Assert.assertTrue(o instanceof FetchGroupSelfRef);
        FetchGroupSelfRef detach = (FetchGroupSelfRef)o;
        Assert.assertNotNull(detach);
        Assert.assertEquals(0, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        Assert.assertSame(detach, detach.getSelfRef());
    }

    public void testSelfRefCircularMany() {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        FetchGroupSelfRef circular23 = new FetchGroupSelfRef(23);
        FetchGroupSelfRef circular22 = new FetchGroupSelfRef(22, circular23);
        FetchGroupSelfRef circular21 = new FetchGroupSelfRef(21, circular22);
        circular23.setSelfRef(circular21);

        checkSelfRefCircularMany(pm, circular21);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        circular21 = (FetchGroupSelfRef)pm.getObjectById(
                JDOHelper.getObjectId(circular21), false);
        checkSelfRefCircularMany(pm, circular21);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSelfRefCircularMany(VersantPersistenceManager pm,
            FetchGroupSelfRef circular21) {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(circular21), "allFields");
        Assert.assertEquals(allFieldsFG.size(), 1);
        Iterator iterator = allFieldsFG.iterator();
        Assert.assertTrue(iterator.hasNext());
        Object o = iterator.next();
        Assert.assertTrue(o instanceof FetchGroupSelfRef);
        FetchGroupSelfRef detach = (FetchGroupSelfRef)o;
        FetchGroupSelfRef detach21 = detach;
        Assert.assertEquals(21, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(22, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(23, detach.getNo());
        Assert.assertNotNull(detach.getSelfRef());
        Assert.assertNotNull(detach.getSelfRefList());
        Assert.assertEquals(1, detach.getSelfRefList().size());
        Assert.assertSame(detach.getSelfRefList().get(0), detach.getSelfRef());
        detach = detach.getSelfRef();
        Assert.assertEquals(21, detach.getNo());
        Assert.assertSame(detach, detach21);
    }

    public void testSelfRefTree() {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        FetchGroupSelfRef tree31 = new FetchGroupSelfRef(31);
        FetchGroupSelfRef tree32 = new FetchGroupSelfRef(32);
        FetchGroupSelfRef tree33 = new FetchGroupSelfRef(33);
        FetchGroupSelfRef tree34 = new FetchGroupSelfRef(34);
        FetchGroupSelfRef tree35 = new FetchGroupSelfRef(35);
        FetchGroupSelfRef tree36 = new FetchGroupSelfRef(36);
        FetchGroupSelfRef tree37 = new FetchGroupSelfRef(37);
        FetchGroupSelfRef tree38 = new FetchGroupSelfRef(38);
        FetchGroupSelfRef tree39 = new FetchGroupSelfRef(39);
        tree31.addSelfRef(tree32);
        tree31.addSelfRef(tree33);
        tree32.addSelfRef(tree34);
        tree32.addSelfRef(tree35);
        tree33.addSelfRef(tree36);
        tree33.addSelfRef(tree37);
        tree37.addSelfRef(tree38);
        tree37.addSelfRef(tree39);

        checkSelfRefTree(pm, tree31);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        tree31 = (FetchGroupSelfRef)pm.getObjectById(
                JDOHelper.getObjectId(tree31), false);
        checkSelfRefTree(pm, tree31);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSelfRefTree(VersantPersistenceManager pm,
            FetchGroupSelfRef tree31) {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(tree31), "allFields");
        Assert.assertEquals(allFieldsFG.size(), 1);
        Iterator iterator = allFieldsFG.iterator();
        Assert.assertTrue(iterator.hasNext());
        Object o = iterator.next();
        Assert.assertTrue(o instanceof FetchGroupSelfRef);
        FetchGroupSelfRef detach31 = (FetchGroupSelfRef)o;
        Assert.assertEquals(31, detach31.getNo());
        Assert.assertNull(detach31.getSelfRef());
        Assert.assertNotNull(detach31.getSelfRefList());
        Assert.assertEquals(2, detach31.getSelfRefList().size());
        FetchGroupSelfRef detach32 = (FetchGroupSelfRef)detach31.getSelfRefList().get(
                0);
        Assert.assertEquals(32, detach32.getNo());
        Assert.assertNull(detach32.getSelfRef());
        Assert.assertNotNull(detach32.getSelfRefList());
        Assert.assertEquals(2, detach32.getSelfRefList().size());
        FetchGroupSelfRef detach33 = (FetchGroupSelfRef)detach31.getSelfRefList().get(
                1);
        Assert.assertEquals(33, detach33.getNo());
        Assert.assertNull(detach33.getSelfRef());
        Assert.assertNotNull(detach33.getSelfRefList());
        Assert.assertEquals(2, detach33.getSelfRefList().size());
        FetchGroupSelfRef detach37 = (FetchGroupSelfRef)detach33.getSelfRefList().get(
                1);
        Assert.assertEquals(37, detach37.getNo());
        Assert.assertNull(detach37.getSelfRef());
        Assert.assertNotNull(detach37.getSelfRefList());
        Assert.assertEquals(2, detach37.getSelfRefList().size());
        FetchGroupSelfRef detach39 = (FetchGroupSelfRef)detach37.getSelfRefList().get(
                1);
        Assert.assertEquals(39, detach39.getNo());
        Assert.assertNull(detach39.getSelfRef());
        Assert.assertNotNull(detach39.getSelfRefList());
        Assert.assertEquals(0, detach39.getSelfRefList().size());
    }

    public void testSelfRefSircularTree() {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        FetchGroupSelfRef circularTree41 = new FetchGroupSelfRef(41);
        FetchGroupSelfRef circularTree42 = new FetchGroupSelfRef(42);
        FetchGroupSelfRef circularTree43 = new FetchGroupSelfRef(43);
        FetchGroupSelfRef circularTree44 = new FetchGroupSelfRef(44);
        FetchGroupSelfRef circularTree45 = new FetchGroupSelfRef(45);
        FetchGroupSelfRef circularTree46 = new FetchGroupSelfRef(46);
        FetchGroupSelfRef circularTree47 = new FetchGroupSelfRef(47);
        FetchGroupSelfRef circularTree48 = new FetchGroupSelfRef(48);
        FetchGroupSelfRef circularTree49 = new FetchGroupSelfRef(49);
        circularTree41.addSelfRef(circularTree42);
        circularTree41.addSelfRef(circularTree43);
        circularTree42.addSelfRef(circularTree44);
        circularTree42.addSelfRef(circularTree45);
        circularTree43.addSelfRef(circularTree46);
        circularTree43.addSelfRef(circularTree47);
        circularTree46.addSelfRef(circularTree48);
        circularTree47.addSelfRef(circularTree49);
        circularTree48.addSelfRef(circularTree41);
        circularTree49.addSelfRef(circularTree45);

        checkSelfRefSircularTree(pm, circularTree41);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        circularTree41 = (FetchGroupSelfRef)pm.getObjectById(
                JDOHelper.getObjectId(circularTree41), false);
        checkSelfRefSircularTree(pm, circularTree41);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkSelfRefSircularTree(VersantPersistenceManager pm,
            FetchGroupSelfRef circularTree41) {
        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(circularTree41), "allFields");
        Assert.assertEquals(allFieldsFG.size(), 1);
        Iterator iterator = allFieldsFG.iterator();
        Assert.assertTrue(iterator.hasNext());
        Object o = iterator.next();
        Assert.assertTrue(o instanceof FetchGroupSelfRef);
        FetchGroupSelfRef detach41 = (FetchGroupSelfRef)o;
        Assert.assertEquals(2, detach41.getSelfRefList().size());
        FetchGroupSelfRef detach43 = (FetchGroupSelfRef)detach41.getSelfRefList().get(
                1);
        Assert.assertEquals(2, detach43.getSelfRefList().size());
        FetchGroupSelfRef detach46 = (FetchGroupSelfRef)detach43.getSelfRefList().get(
                0);
        Assert.assertEquals(1, detach46.getSelfRefList().size());
        FetchGroupSelfRef detach48 = (FetchGroupSelfRef)detach46.getSelfRefList().get(
                0);
        Assert.assertEquals(1, detach48.getSelfRefList().size());
        Assert.assertSame(detach41, detach48.getSelfRefList().get(0));
        detach41.setNo(333);
        Assert.assertEquals(333, detach41.getNo());
    }

    public void testSerializable() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(20);
        a1.setBooleanField(true);
        a1.setCharField('a');
        a1.setByteField((byte)1);
        a1.setShortField((short)2);
        a1.setIntField(3);
        a1.setLongField(4);
        a1.setFloatField(5.6f);
        a1.setDoubleField(7.8);
        a1.setStringField("9");
        a1.setCharsField(new char[]{1, 2, 3});
        a1.setBytesField(new byte[]{2, 3, 4});
        a1.setShortsField(new short[]{3, 4, 5});
        a1.setIntsField(new int[]{4, 5, 6});
        a1.setLongsField(new long[]{5, 6, 7});
        a1.setFloatsField(new float[]{1.2f, 3.4f, 5.6f});
        a1.setDoublesField(new double[]{2.3, 4.5, 6.7});
        SimpleFields a = a1;

        Collection allFieldsFG = pm.versantDetachCopy(
                Collections.singletonList(a1), "allFields");
        Assert.assertEquals(1, allFieldsFG.size());
        Object o = allFieldsFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        SimpleFields a2 = (SimpleFields)o;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream out = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream in = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteArrayOutputStream);
            out.writeObject(a2);
            byteArrayInputStream = new ByteArrayInputStream(
                    byteArrayOutputStream.toByteArray());
            in = new ObjectInputStream(byteArrayInputStream);
            a1 = (SimpleFields)in.readObject();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                in.close();
            } catch (Exception e) {
            }
        }

        Assert.assertTrue(a1 != a2);
        Assert.assertEquals(a1.isBooleanField(), a2.isBooleanField());
        Assert.assertTrue(a1.isBooleanField());
        Assert.assertEquals(a1.getCharField(), a2.getCharField());
        Assert.assertEquals(a1.getByteField(), a2.getByteField());
        Assert.assertEquals(a1.getShortField(), a2.getShortField());
        Assert.assertEquals(a1.getIntField(), a2.getIntField());
        Assert.assertEquals(a1.getLongField(), a2.getLongField());
        Assert.assertTrue(a1.getStringField().equals(a2.getStringField()));
        Assert.assertTrue(a1.getCharsField() != a2.getCharsField());
        Assert.assertTrue(a1.getBytesField() != a2.getBytesField());
        Assert.assertTrue(a1.getShortsField() != a2.getShortsField());
        Assert.assertTrue(a1.getIntsField() != a2.getIntsField());
        Assert.assertTrue(a1.getLongsField() != a2.getLongsField());

        Assert.assertFalse(((VersantDetachable)a2).versantIsDirty());
        Assert.assertEquals(3, a2.getIntField());
        a2.setIntField(1);
        Assert.assertEquals(1, a2.getIntField());
        Assert.assertTrue(((VersantDetachable)a2).versantIsDirty());

        Assert.assertFalse(((VersantDetachable)a1).versantIsDirty());
        Assert.assertEquals(3, a1.getIntField());
        a1.setIntField(1);
        Assert.assertEquals(1, a1.getIntField());
        Assert.assertTrue(((VersantDetachable)a1).versantIsDirty());

        allFieldsFG = pm.versantDetachCopy(Collections.singletonList(a),
                "default");
        Assert.assertEquals(1, allFieldsFG.size());
        o = allFieldsFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        a2 = (SimpleFields)o;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteArrayOutputStream);
            out.writeObject(a2);
            byteArrayInputStream = new ByteArrayInputStream(
                    byteArrayOutputStream.toByteArray());
            in = new ObjectInputStream(byteArrayInputStream);
            a1 = (SimpleFields)in.readObject();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                in.close();
            } catch (Exception e) {
            }
        }

        checkSimpleDefault(a, a1);

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testSimpleAttach() {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(30);
        a1.setBooleanField(true);
        a1.setCharField('a');
        a1.setByteField((byte)1);
        a1.setShortField((short)2);
        a1.setIntField(3);
        a1.setLongField(4);
        a1.setFloatField(5.6f);
        a1.setDoubleField(7.8);
        a1.setStringField("9");
        a1.setBooleansField(new boolean[]{true, false, true});
        a1.setCharsField(new char[]{1, 2, 3});
        a1.setBytesField(new byte[]{2, 3, 4});
        a1.setShortsField(new short[]{3, 4, 5});
        a1.setIntsField(new int[]{4, 5, 6});
        a1.setLongsField(new long[]{5, 6, 7});
        a1.setFloatsField(new float[]{1.2f, 3.4f, 5.6f});
        a1.setDoublesField(new double[]{2.3, 4.5, 6.7});
        pm.makePersistent(a1);
        Object objectId = JDOHelper.getObjectId(a1);
        pm.currentTransaction().commit();
//        Assert.assertTrue(TestUtils.isHollow(a1));
        pm.currentTransaction().begin();
        a1 = (SimpleFields)pm.getObjectById(objectId, false);
//        Assert.assertTrue(TestUtils.isHollow(a1));
        Collection defaultFG = pm.versantDetachCopy(
                Collections.singletonList(a1), "allFields");
        Object o = defaultFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        SimpleFields a = (SimpleFields)o;
        a1.setBooleanField(false);
        a1.setCharField('b');
        a1.setByteField((byte)10);
        a1.setShortField((short)20);
        a1.setIntField(30);
        a1.setLongField(40);
        a1.setFloatField(50.6f);
        a1.setDoubleField(70.8);
        a1.setStringField("90");
        a1.setBooleansField(new boolean[]{false, true, true});
        a1.setCharsField(new char[]{10, 20, 30});
        a1.setBytesField(new byte[]{20, 30, 40});
        a1.setShortsField(new short[]{30, 40, 50});
        a1.setIntsField(new int[]{40, 50, 60});
        a1.setLongsField(new long[]{50, 60, 70});
        a1.setFloatsField(new float[]{10.2f, 30.4f, 50.6f});
        a1.setDoublesField(new double[]{20.3, 40.5, 60.7});

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        defaultFG = pm.versantAttachCopy(Collections.singletonList(a), false);
        o = defaultFG.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        a = (SimpleFields)o;
        Assert.assertFalse(a.isBooleanField());
        Assert.assertEquals('b', a.getCharField());
        Assert.assertEquals(10, a.getByteField());
        Assert.assertEquals(20, a.getShortField());
        Assert.assertEquals(30, a.getIntField());
        Assert.assertEquals(40, a.getLongField());
//        Assert.assertEquals(50.6f, a.getFloatField(), );
        Assert.assertEquals("90", a.getStringField());
        Assert.assertEquals(3, a.getBooleansField().length);
        Assert.assertFalse(a.getBooleansField()[0]);
        Assert.assertTrue(a.getBooleansField()[1]);
        Assert.assertTrue(a.getBooleansField()[2]);
        Assert.assertEquals(3, a.getCharsField().length);
        Assert.assertEquals(10, a.getCharsField()[0]);
        Assert.assertEquals(20, a.getCharsField()[1]);

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testSimpleAttachNew() {
    	if (!isApplicationIdentitySupported())
    		return;
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(4);
        a1.setBooleanField(false);
        a1.setCharField('b');
        a1.setByteField((byte)10);
        a1.setShortField((short)20);
        a1.setIntField(30);
        a1.setLongField(40);
        a1.setFloatField(50.6f);
        a1.setDoubleField(70.8);
        a1.setStringField("90");
        a1.setBooleansField(new boolean[]{false, true, true});
        a1.setCharsField(new char[]{10, 20, 30});
        a1.setBytesField(new byte[]{20, 30, 40});
        a1.setShortsField(new short[]{30, 40, 50});
        a1.setIntsField(new int[]{40, 50, 60});
        a1.setLongsField(new long[]{50, 60, 70});
        a1.setFloatsField(new float[]{10.2f, 30.4f, 50.6f});
        a1.setDoublesField(new double[]{20.3, 40.5, 60.7});

        Collection attached = pm.versantAttachCopy(
                Collections.singletonList(a1), false);
        Object o = attached.iterator().next();
        Assert.assertTrue(o instanceof SimpleFields);
        SimpleFields a = (SimpleFields)o;
        Assert.assertFalse(a.isBooleanField());
        Assert.assertEquals('b', a.getCharField());
        Assert.assertEquals(10, a.getByteField());
        Assert.assertEquals(20, a.getShortField());
        Assert.assertEquals(30, a.getIntField());
        Assert.assertEquals(40, a.getLongField());
//        Assert.assertEquals(50.6f, a.getFloatField(), );
        Assert.assertEquals("90", a.getStringField());
        Assert.assertEquals(3, a.getBooleansField().length);
        Assert.assertFalse(a.getBooleansField()[0]);
        Assert.assertTrue(a.getBooleansField()[1]);
        Assert.assertTrue(a.getBooleansField()[2]);
        Assert.assertEquals(3, a.getCharsField().length);
        Assert.assertEquals(10, a.getCharsField()[0]);
        Assert.assertEquals(20, a.getCharsField()[1]);

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRefAttach() {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(5);
        a1.setIntField(3);
        pm.makePersistent(a1);
        CollectionChild c = new CollectionChild();
        c.setNo(5);
        c.setSimpleFields(a1);
        pm.makePersistent(c);

        ArrayList list = new ArrayList(2);
        list.add(a1);
        list.add(c);
        Collection detached = pm.versantDetachCopy(list, "allFields");
        Iterator it = detached.iterator();
        Assert.assertTrue(it.hasNext());
        Object o = it.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        a1.setIntField(200);
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        Assert.assertEquals(5, c.getNo());
        c.setNo(100);
        Assert.assertSame(c.getSimpleFields(), a1);
        Collection attached = pm.versantAttachCopy(detached, true);
        it = attached.iterator();
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        Assert.assertEquals(100, c.getNo());
        Assert.assertSame(c.getSimpleFields(), a1);
        Assert.assertEquals(200, a1.getIntField());

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRefAttachNew() {
    	if (!isApplicationIdentitySupported())
    		return;
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(6);
        a1.setIntField(3);
        CollectionChild c = new CollectionChild();
        c.setNo(5);
        c.setSimpleFields(a1);

        ArrayList list = new ArrayList(2);
        list.add(a1);
        list.add(c);
        Collection attached = pm.versantAttachCopy(list, true);
        Iterator it = attached.iterator();
        Assert.assertTrue(it.hasNext());
        Object o = it.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        Assert.assertEquals(5, c.getNo());
        Assert.assertSame(c.getSimpleFields(), a1);
        Assert.assertEquals(3, a1.getIntField());

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRefAttachManages() {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(7);
        a1.setIntField(3);
        pm.makePersistent(a1);
        CollectionChild c = new CollectionChild();
        c.setNo(5);
        c.setSimpleFields(a1);
        pm.makePersistent(c);

        ArrayList list = new ArrayList(2);
        list.add(a1);
        list.add(c);
        Collection attached = pm.versantAttachCopy(list, true);
        Iterator it = attached.iterator();
        Assert.assertTrue(it.hasNext());
        Object o = it.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        Object oid = JDOHelper.getObjectId(a1);
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        Assert.assertEquals(5, c.getNo());
        Assert.assertSame(c.getSimpleFields(), a1);
        Assert.assertEquals(3, a1.getIntField());

        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.evictAll();
        pm.currentTransaction().begin();

        o = pm.getObjectById(oid, true);
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        Assert.assertEquals(3, a1.getIntField());
        Object oid2 = JDOHelper.getObjectId(a1);
        Assert.assertEquals(oid, oid2);
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRefShallowAttach() {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleFields a1 = new SimpleFields(8);
        a1.setIntField(3);
        pm.makePersistent(a1);
        CollectionChild c = new CollectionChild();
        c.setNo(5);
        c.setSimpleFields(a1);
        pm.makePersistent(c);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        ArrayList list = new ArrayList(2);
        list.add(a1);
        list.add(c);
        Collection detach = pm.versantDetachCopy(list, "allFields");
        Iterator it = detach.iterator();
        Assert.assertTrue(it.hasNext());
        Object o = it.next();
        Assert.assertTrue(o instanceof SimpleFields);
        a1 = (SimpleFields)o;
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        Assert.assertEquals(5, c.getNo());
        Assert.assertSame(c.getSimpleFields(), a1);
        Assert.assertEquals(3, a1.getIntField());
        c.setNo(7);
        a1.setIntField(7);

        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Collection attached = pm.versantAttachCopy(
                Collections.singletonList(c), true, true);
        it = attached.iterator();
        Assert.assertTrue(it.hasNext());
        o = it.next();
        Assert.assertTrue(o instanceof CollectionChild);
        c = (CollectionChild)o;
        a1 = c.getSimpleFields();
        Assert.assertEquals(7, c.getNo());
        Assert.assertEquals(3, a1.getIntField());
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testCollectionsAttach() throws IOException,
            ClassNotFoundException, CloneNotSupportedException {
    	
    	if (!isManagedRelationshipSupported())
    		return;

        if ("informixse".equals(getDbName())) {
            logFilter("Many 2 many related");
            unsupported();
            return;
        }
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        CollectionChild c1 = new CollectionChild();
        c1.setNo(1);
        SimpleFields simpleFields = new SimpleFields(88, 8, "1",
                new int[]{1, 2});
        c1.setSimpleFields(simpleFields);
        CollectionChild c2 = new CollectionChild();
        c2.setNo(2);
        c2.setSimpleFields(simpleFields);
        CollectionFields b = new CollectionFields();
        b.getCollectionChildren()[0] = c1;
        b.getCollectionChildren()[1] = c2;
        b.getArrayList().add(c1);
        b.getArrayList().add(c2);
        b.getList().add(c1);
        b.getList().add(c2);
        b.getCollection().add(c1);
        b.getCollection().add(c2);
        b.getHashSet().add(c1);
        b.getHashSet().add(c2);
        b.getSet().add(c1);
        b.getSet().add(c2);
        b.getLinkedList().add(c1);
        b.getLinkedList().add(c2);
        b.getTreeSet().add(c1);
        b.getTreeSet().add(c2);
        b.getSortedSet().add(c1);
        b.getSortedSet().add(c2);
        b.getVector().add(c1);
        b.getVector().add(c2);
        b.getHashMap().put("1", c1);
        b.getHashMap().put("2", c2);
        b.getMap().put("1", c1);
        b.getMap().put("2", c2);
        b.getHashtable().put("1", c1);
        b.getHashtable().put("2", c2);
        b.getTreeMap().put("1", c1);
        b.getTreeMap().put("2", c2);
        b.getSortedMap().put("1", c1);
        b.getSortedMap().put("2", c2);

        ArrayList list = new ArrayList(3);
        list.add(b);
        list.add(c1);
        list.add(simpleFields);
        Collection allFieldsFG = pm.versantDetachCopy(list, "allFields");
        allFieldsFG = serialize(allFieldsFG);
        Assert.assertEquals(3, allFieldsFG.size());
        Iterator detIt = allFieldsFG.iterator();
        Assert.assertTrue(detIt.hasNext());
        Object o1 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        Object o2 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        Object o3 = detIt.next();
        Assert.assertTrue(o1 instanceof CollectionFields);
        Assert.assertTrue(o2 instanceof CollectionChild);
        Assert.assertTrue(o3 instanceof SimpleFields);
        b = (CollectionFields)o1;
        CollectionChild c = (CollectionChild)o2;
        SimpleFields d = (SimpleFields)o3;
        c1 = (CollectionChild)b.getArrayList().get(0);
        c2 = (CollectionChild)b.getArrayList().get(1);

        b.setCollectionChildren(new CollectionChild[4]);
        b.getCollectionChildren()[0] = c1;
        b.getCollectionChildren()[1] = c2;
        c1.setNo(c1.getNo());
        c2.setNo(c2.getNo());
//        c1 = (CollectionChild)c1.clone();
//        c2 = (CollectionChild)c2.clone();
        CollectionChild cTemp = new CollectionChild();
        cTemp.setNo(c1.getNo());
        cTemp.setSimpleFields(c1.getSimpleFields());
        c1 = cTemp;
        cTemp = new CollectionChild();
        cTemp.setNo(c2.getNo());
        cTemp.setSimpleFields(c2.getSimpleFields());
        c2 = cTemp;
        b.getCollectionChildren()[2] = c1;
        b.getCollectionChildren()[3] = c2;
        b.getArrayList().add(c1);
        b.getArrayList().add(c2);
        b.getList().add(c1);
        b.getList().add(c2);
        b.getCollection().add(c1);
        b.getCollection().add(c2);
        b.getHashSet().add(c1);
        b.getHashSet().add(c2);
        b.getSet().add(c1);
        b.getSet().add(c2);
        b.getLinkedList().add(c1);
        b.getLinkedList().add(c2);
        b.getTreeSet().add(c1);
        b.getTreeSet().add(c2);
        b.getSortedSet().add(c1);
        b.getSortedSet().add(c2);
        b.getVector().add(c1);
        b.getVector().add(c2);
        b.getHashMap().put("3", c1);
        b.getHashMap().put("4", c2);
        b.getMap().put("3", c1);
        b.getMap().put("4", c2);
        b.getHashtable().put("3", c1);
        b.getHashtable().put("4", c2);
        b.getTreeMap().put("3", c1);
        b.getTreeMap().put("4", c2);
        b.getSortedMap().put("3", c1);
        b.getSortedMap().put("4", c2);

        c.setNo(1);
        d.setIntField(1);
        checkCollectionsAttach(b, d, c);

        list = new ArrayList(3);
        list.add(b);
        list.add(c);
        list.add(d);
        allFieldsFG = pm.versantAttachCopy(list, true);
        Assert.assertEquals(3, allFieldsFG.size());
        detIt = allFieldsFG.iterator();
        Assert.assertTrue(detIt.hasNext());
        o1 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o2 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o3 = detIt.next();
        Assert.assertTrue(o1 instanceof CollectionFields);
        Assert.assertTrue(o2 instanceof CollectionChild);
        Assert.assertTrue(o3 instanceof SimpleFields);
        b = (CollectionFields)o1;
        c = (CollectionChild)o2;
        d = (SimpleFields)o3;
        checkCollectionsAttach(b, d, c);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        pm.evictAll();
        b = (CollectionFields)pm.getObjectById(JDOHelper.getObjectId(b), true);
        c = (CollectionChild)pm.getObjectById(JDOHelper.getObjectId(c), true);
        d = (SimpleFields)pm.getObjectById(JDOHelper.getObjectId(d), true);
        checkCollectionsAttach(b, d, c);

        list = new ArrayList(3);
        list.add(b);
        list.add(c);
        list.add(d);
        allFieldsFG = pm.versantDetachCopy(list, "allFields");
        Assert.assertEquals(3, allFieldsFG.size());
        detIt = allFieldsFG.iterator();
        Assert.assertTrue(detIt.hasNext());
        o1 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o2 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o3 = detIt.next();
        Assert.assertTrue(o1 instanceof CollectionFields);
        Assert.assertTrue(o2 instanceof CollectionChild);
        Assert.assertTrue(o3 instanceof SimpleFields);
        b = (CollectionFields)o1;
        c = (CollectionChild)o2;
        d = (SimpleFields)o3;
        VersantHelper.deletePersistent(d);

        list = new ArrayList(3);
        list.add(b);
        list.add(c);
        list.add(d);
        allFieldsFG = pm.versantAttachCopy(list, true);
        Assert.assertEquals(3, allFieldsFG.size());
        detIt = allFieldsFG.iterator();
        Assert.assertTrue(detIt.hasNext());
        o1 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o2 = detIt.next();
        Assert.assertTrue(detIt.hasNext());
        o3 = detIt.next();
        Assert.assertTrue(o1 instanceof CollectionFields);
        Assert.assertTrue(o2 instanceof CollectionChild);
        Assert.assertTrue(o3 instanceof SimpleFields);
        b = (CollectionFields)o1;
        c = (CollectionChild)o2;
        d = (SimpleFields)o3;
        Assert.assertTrue(JDOHelper.isDeleted(d));
        try {
            d.getIntField();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkCollectionsAttach(CollectionFields b, SimpleFields d,
            CollectionChild c) {
        Object o1;
        Object o2;
        Assert.assertNotNull(b.getCollectionChildren());
        Assert.assertNotNull(b.getArrayList());
        Assert.assertNotNull(b.getList());
        Assert.assertNotNull(b.getCollection());
        Assert.assertNotNull(b.getHashSet());
        Assert.assertNotNull(b.getSet());
        Assert.assertNotNull(b.getLinkedList());
        Assert.assertNotNull(b.getTreeSet());
        Assert.assertNotNull(b.getSortedSet());
        Assert.assertNotNull(b.getVector());
        Assert.assertNotNull(b.getHashMap());
        Assert.assertNotNull(b.getMap());
        Assert.assertNotNull(b.getHashtable());
        Assert.assertNotNull(b.getTreeMap());
        Assert.assertNotNull(b.getSortedMap());

        Assert.assertEquals(4, b.getCollectionChildren().length);
        Assert.assertEquals(4, b.getArrayList().size());
        Assert.assertEquals(4, b.getList().size());
        Assert.assertEquals(4, b.getCollection().size());
        Assert.assertEquals(2, b.getHashSet().size());
        Assert.assertEquals(2, b.getSet().size());
        Assert.assertEquals(4, b.getLinkedList().size());
        Assert.assertEquals(2, b.getSortedSet().size());
        Assert.assertEquals(4, b.getVector().size());
        Assert.assertEquals(4, b.getHashMap().size());
        Assert.assertEquals(4, b.getMap().size());
        Assert.assertEquals(4, b.getHashtable().size());
        Assert.assertEquals(4, b.getTreeMap().size());
        Assert.assertEquals(4, b.getSortedMap().size());

        o1 = b.getCollectionChildren()[0];
        o2 = b.getCollectionChildren()[1];
        Assert.assertSame(o1, c);
        checktestCollectionsAttachOrder(o1, o2, d);
        o1 = b.getCollectionChildren()[2];
        o2 = b.getCollectionChildren()[3];
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
        o1 = b.getArrayList().get(0);
        o2 = b.getArrayList().get(1);
        Assert.assertSame(o1, c);
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertSame(o1, c);
        o1 = b.getArrayList().get(2);
        o2 = b.getArrayList().get(3);
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
        o1 = b.getList().get(0);
        o2 = b.getList().get(1);
        Assert.assertSame(o1, c);
        checktestCollectionsAttachOrder(o1, o2, d);
        o1 = b.getList().get(2);
        o2 = b.getList().get(3);
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
//        Iterator colIt = b.getCollection().iterator();
//        o1 = colIt.next();
//        o2 = colIt.next();
//        Assert.assertSame(o1, c);
//        checktestCollectionsAttachOrder(o1, o2, d);
//        o1 = colIt.next();
//        o2 = colIt.next();
//        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
        o1 = b.getLinkedList().get(0);
        o2 = b.getLinkedList().get(1);
        Assert.assertSame(o1, c);
        checktestCollectionsAttachOrder(o1, o2, d);
        o1 = b.getLinkedList().get(2);
        o2 = b.getLinkedList().get(3);
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
        o1 = b.getVector().get(0);
        o2 = b.getVector().get(1);
        Assert.assertSame(o1, c);
        checktestCollectionsAttachOrder(o1, o2, d);
        o1 = b.getVector().get(2);
        o2 = b.getVector().get(3);
        checktestCollectionsAttachOrder(o1, o2, d);
        Assert.assertEquals(o1, c);
        Iterator iterator = b.getHashSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionsAttachNoOrder(o1, o2, d);
        iterator = b.getSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionsAttachNoOrder(o1, o2, d);
        iterator = b.getTreeSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        iterator = b.getSortedSet().iterator();
        o1 = iterator.next();
        o2 = iterator.next();
        checktestCollectionsAttachNoOrder(o2, o1, d);
        o1 = b.getHashMap().get("1");
        o2 = b.getHashMap().get("2");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getHashMap().get("3");
        o2 = b.getHashMap().get("4");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getMap().get("1");
        o2 = b.getMap().get("2");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getMap().get("3");
        o2 = b.getMap().get("4");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getHashtable().get("1");
        o2 = b.getHashtable().get("2");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getHashtable().get("3");
        o2 = b.getHashtable().get("4");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getTreeMap().get("1");
        o2 = b.getTreeMap().get("2");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getSortedMap().get("1");
        o2 = b.getSortedMap().get("2");
        checktestCollectionsAttachNoOrder(o2, o1, d);
        o1 = b.getTreeMap().get("3");
        o2 = b.getTreeMap().get("4");
        checktestCollectionsAttachNoOrder(o1, o2, d);
        o1 = b.getSortedMap().get("3");
        o2 = b.getSortedMap().get("4");
        checktestCollectionsAttachNoOrder(o2, o1, d);
        CollectionChild[] collectionChildren = b.getCollectionChildren();
        int length = collectionChildren.length;
        for (int i = 0; i < length; i++) {
            o1 = collectionChildren[i];
            Assert.assertTrue(b.getArrayList().contains(o1));
            Assert.assertTrue(b.getList().contains(o1));
            Assert.assertTrue(b.getCollection().contains(o1));
            Assert.assertTrue(b.getLinkedList().contains(o1));
            Assert.assertTrue(b.getTreeSet().contains(o1));
            Assert.assertTrue(b.getVector().contains(o1));
            Assert.assertTrue(b.getHashSet().contains(o1));
            Assert.assertTrue(b.getSet().contains(o1));
        }
//        Iterator colIt2 = b.getCollection().iterator();
        for (int i = 0; i < length; i++) {
            o1 = collectionChildren[i];
            Assert.assertSame(o1, b.getArrayList().get(i));
            Assert.assertSame(o1, b.getList().get(i));
//            Assert.assertSame(o1, colIt2.next());
            Assert.assertSame(o1, b.getLinkedList().get(i));
            Assert.assertSame(o1, b.getVector().get(i));
        }
        for (Iterator it = b.getHashMap().keySet().iterator(); it.hasNext();) {
            o1 = (Object)it.next();
            Object v1 = b.getHashMap().get(o1);
            Object v2 = b.getHashtable().get(o1);
            Object v3 = b.getTreeMap().get(o1);
            Object v4 = b.getSortedMap().get(o1);
            Object v5 = b.getMap().get(o1);
            Assert.assertSame(v1, v2);
            Assert.assertSame(v2, v3);
            Assert.assertSame(v3, v4);
            Assert.assertSame(v4, v5);
        }
    }

    private void checktestCollectionsAttachOrder(Object o1, Object o2,
            SimpleFields o3) {
        checktestCollectionsAttachNoOrder(o1, o2, o3);
        CollectionChild c1 = (CollectionChild)o1;
        CollectionChild c2 = (CollectionChild)o2;
        Assert.assertEquals(1, c1.getNo());
        Assert.assertEquals(2, c2.getNo());
    }

    private void checktestCollectionsAttachNoOrder(Object o1, Object o2,
            SimpleFields o3) {
        Assert.assertNotNull(o1);
        Assert.assertNotNull(o2);
        Assert.assertTrue(o1 instanceof CollectionChild);
        Assert.assertTrue(o2 instanceof CollectionChild);
        CollectionChild c1 = (CollectionChild)o1;
        CollectionChild c2 = (CollectionChild)o2;
        Assert.assertTrue(c1 != c2);
        SimpleFields s1 = c1.getSimpleFields();
        Assert.assertNotNull(s1);
        SimpleFields s2 = c2.getSimpleFields();
        Assert.assertNotNull(s2);
        Assert.assertSame(s1, s2);
        Assert.assertTrue(s1 == o3);
        Assert.assertSame(s1, o3);
        Assert.assertEquals(1, s1.getIntField());
        Assert.assertEquals(1, s2.getIntField());
        Assert.assertEquals(1, c1.getSimpleFields().getIntField());
        Assert.assertEquals(1, c2.getSimpleFields().getIntField());
        Assert.assertEquals("1", c1.getSimpleFields().getStringField());
        Assert.assertEquals("1", c2.getSimpleFields().getStringField());
    }

    private Collection serialize(Collection detached) throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream out = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream in = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteArrayOutputStream);
            out.writeObject(detached);
            byteArrayInputStream = new ByteArrayInputStream(
                    byteArrayOutputStream.toByteArray());
            in = new ObjectInputStream(byteArrayInputStream);
            detached = (Collection)in.readObject();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
            try {
                byteArrayInputStream.close();
            } catch (Exception e) {
            }
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return detached;
    }

    public void testPolyRef() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        PolyRefParent parentSub = new PolyRefParent();
        pm.makePersistent(parentSub);
        PolyRefParent parentSuper = new PolyRefParent();
        pm.makePersistent(parentSuper);

        SimpleFields sf = new SimpleFields(9);
        sf.setSimpleFieldsBaseId(2);
        sf.setBooleanField(true);
        sf.setCharField('a');
        sf.setByteField((byte)1);
        sf.setShortField((short)2);
        sf.setIntField(3);
        sf.setLongField(4);
        sf.setFloatField(5.6f);
        sf.setDoubleField(7.8);
        sf.setStringField("9");
        sf.setBooleansField(new boolean[]{true, false, true});
        sf.setCharsField(new char[]{1, 2, 3});
        sf.setBytesField(new byte[]{2, 3, 4});
        sf.setShortsField(new short[]{3, 4, 5});
        sf.setIntsField(new int[]{4, 5, 6});
        sf.setLongsField(new long[]{5, 6, 7});
        sf.setFloatsField(new float[]{1.2f, 3.4f, 5.6f});
        sf.setDoublesField(new double[]{2.3, 4.5, 6.7});
        parentSub.setSimpleFields(sf);

        SimpleFieldsBase sfb = new SimpleFieldsBase(1000);
        sfb.setSimpleFieldsBaseId(3);
        sfb.setBooleanField(true);
        sfb.setCharField('a');
        sfb.setByteField((byte)1);
        sfb.setShortField((short)2);
        sfb.setIntField(3);
        sfb.setLongField(4);
        sfb.setFloatField(5.6f);
        sfb.setDoubleField(7.8);
        sfb.setStringField("9");
        parentSuper.setSimpleFields(sfb);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        checkPolyRef(pm, parentSub, true);
        checkPolyRef(pm, parentSuper, false);

        Object subOid = JDOHelper.getObjectId(parentSub);
        Object superOid = JDOHelper.getObjectId(parentSuper);

        // close the PM and get a new one
        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.evictAll();
        pm.currentTransaction().begin();

        parentSub = (PolyRefParent)pm.getObjectById(subOid, true);
        parentSuper = (PolyRefParent)pm.getObjectById(superOid, true);

        checkPolyRef(pm, parentSub, true);
        checkPolyRef(pm, parentSuper, false);

        // close the PM and get a new one
        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.evictAll();
        pm.currentTransaction().begin();

        parentSub = (PolyRefParent)pm.getObjectById(subOid, false);
        parentSuper = (PolyRefParent)pm.getObjectById(superOid, false);

        checkPolyRef(pm, parentSub, true);
        checkPolyRef(pm, parentSuper, false);

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void checkPolyRef(VersantPersistenceManager pm,
            PolyRefParent refParent, boolean isSub) {
        Collection detached = pm.versantDetachCopy(
                Collections.singletonList(refParent), "polyRefFG");
        Iterator iterator = detached.iterator();
        Assert.assertTrue(iterator.hasNext());
        Object o = iterator.next();
        Assert.assertTrue(o instanceof PolyRefParent);
        PolyRefParent parent = (PolyRefParent)o;

        SimpleFieldsBase sfb = parent.getSimpleFields();
        Assert.assertTrue(sfb.isBooleanField());
        Assert.assertEquals(1, sfb.getByteField());
        Assert.assertEquals(3, sfb.getIntField());
        Assert.assertEquals("9", sfb.getStringField());
        try {
            sfb.getCharField();
            Assert.fail("field should not be loaded.");
        } catch (Exception e) {}
        if (isSub) {
            Assert.assertTrue(sfb instanceof SimpleFields);
            SimpleFields sf = (SimpleFields)sfb;
            Assert.assertEquals(3, sf.getBooleansField().length);
            Assert.assertEquals(3, sf.getBytesField().length);
            Assert.assertEquals(3, sf.getIntsField().length);
            try {
                sf.getCharsField();
                Assert.fail("field should not be loaded.");
            } catch (Exception e) {}
        }
    }

    public void testBug1113() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }
        A a = new A();
        B b = new B();
        a.addElement(b);

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(a);

        List detachList = new LinkedList();
        detachList.add(a);

        List retrievedList = (List)pm.versantDetachCopy(detachList, "A");

        A retrievedA = (A)retrievedList.get(0);

        assertEquals(a.getID(), retrievedA.getID());
        assertEquals(1, retrievedA.getNoBs());

        b = new B();
        retrievedA.addElement(b);
        List attachList = new LinkedList();
        attachList.add(retrievedA);
        attachList = (List)pm.versantAttachCopy(attachList, true);

        A newRetrievedA = (A)attachList.get(0);

        assertEquals(retrievedA.getID(), newRetrievedA.getID());
        assertEquals(2, newRetrievedA.getNoBs());

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDemo() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Country za = new Country("ZA", "South Africa");
        Country us = new Country("US", "United States");
        Country uk = new Country("UK", "United Kingdom");
        pm.makePersistent(za);
        pm.makePersistent(us);
        pm.makePersistent(uk);

        Contact david = new Contact("david", "555-1234",
                "david@versant.com",
                new Address("123 SomeStreet", "Cape Town", za), 32);
        pm.makePersistent(david);
        pm.currentTransaction().commit();
        // close the PM and get a new one
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
//        pm.evictAll();
        pm.currentTransaction().begin();

        // look up a Contact and detach it
        Query q = pm.newQuery(Contact.class, "name == p");
        q.declareParameters("String p");
        Collection ans = (Collection)q.execute("david");
        Collection detached = pm.versantDetachCopy(ans, "demo");
        q.closeAll();

        // The demo fetch group includes all of the fields of Contact
        // but not the Country field from Contact.address

        // serialize it to a byte buffer
        ByteArrayOutputStream obuf = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(obuf);
        out.writeObject(detached);
        out.close();

        // Attempt to navigate to the address field. This triggers an
        // exception as this field is not in the detached graph
        david = (Contact)detached.iterator().next();
        try {
            System.out.println("david.getAddress().getCountry() = " +
                    david.getAddress().getCountry());
        } catch (JDOUserException e) {
            System.out.println(e);
        }

        // close the PM and get a new one
        pm.currentTransaction().commit();
        pm.close();

        // read the Collection back in again
        ByteArrayInputStream ibuf = new ByteArrayInputStream(
                obuf.toByteArray());
        ObjectInputStream in = new ObjectInputStream(ibuf);
        Collection toAttach = (Collection)in.readObject();
        in.close();

        // change the first Contact
        Contact con = (Contact)toAttach.iterator().next();
        Assert.assertEquals("555-1234", con.getPhone());
        con.setPhone("555-9999");

        // start a tx and attach the graph - the phone number change
        // will be persisted
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.evictAll();
        pm.currentTransaction().begin();
        pm.versantAttachCopy(toAttach, true);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("555-9999", con.getPhone());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDelete() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int count = 20;
        int start = 347665;
        SimpleFields[] simpleFieldses = new SimpleFields[count];
        for (int i = 0; i < count; i++) {
            SimpleFields simpleFields = new SimpleFields(i + start);
            simpleFieldses[i] = simpleFields;
            pm.makePersistent(simpleFields);
        }
        Collection detached = pm.versantDetachCopy(
                Arrays.asList(simpleFieldses), null);
        Collection serialized = serialize(detached);
        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int i = 0;
        for (Iterator it = detached.iterator(); it.hasNext();) {
            Object o = (Object)it.next();
            if ((i % 2) == 0) {
                VersantHelper.deletePersistent(o);
            }
            if ((i % 3) == 0) {
                pm.versantAttachCopy(Collections.singletonList(o), true);
            }
            if (i == 10) {
                pm.currentTransaction().commit();
                pm.currentTransaction().begin();
            }
            i++;
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        i = 0;
        for (Iterator it = serialized.iterator(); it.hasNext();) {
            Object o = (Object)it.next();
            if (i == 17 || i == 18) {
                VersantHelper.deletePersistent(o);
            }
            if (i == 19) {
                VersantHelper.deletePersistent(o);
                pm.versantAttachCopy(Collections.singletonList(o), true);
            }
            i++;
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testExternalizer() throws IOException, ClassNotFoundException {
        if (!isApplicationIdentitySupported() || "informixse".equals(
                getDbName())) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        CreditCard cc = new CreditCard();
        cc.setCardType(CardTypes.Visa);
        pm.makePersistent(cc);
        Object oid = JDOHelper.getObjectId(cc);
        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        cc =(CreditCard)pm.getObjectById(oid, false);
        pm = checkExternallized(pm, cc, oid, CardTypes.DinersClub);

        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        cc =(CreditCard)pm.getObjectById(oid, false);
        cc.getCardType().getLabel();
        pm = checkExternallized(pm, cc, oid, CardTypes.MasterCard);

        pm.currentTransaction().commit();
        pm.close();
    }

    private VersantPersistenceManager checkExternallized(
            VersantPersistenceManager pm, CreditCard cc, Object oid,
            CardTypes cardType) {
        Collection detached;
        detached = pm.versantDetachCopy(Collections.singletonList(cc), "all");
        for (Iterator it = detached.iterator(); it.hasNext();) {
            CreditCard creditCard = (CreditCard)it.next();
            creditCard.setCardType(cardType);
            pm.versantAttachCopy(Collections.singletonList(creditCard), true);
        }
        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        cc =(CreditCard)pm.getObjectById(oid, false);
        Assert.assertSame("Externalizer did not attach.",cc.getCardType(), cardType);
        return pm;
    }
}
