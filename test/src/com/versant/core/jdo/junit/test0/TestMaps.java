
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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.common.Debug;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.MapModel;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

/**
 * @keep-all
 */
public class TestMaps extends VersantTestCase {

    public TestMaps(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            /*BAD "testDeleteInstanceInMap", */
            "testBasicInsert",
            "testReplaceHashMapWithNull",
            "testReplaceHashMap",
            "testBasicInsertTable",
            "testBasicDelete",
            "testBasicDeleteTable",
            "testSCOMakeDirty",
            "testSCOMakeDirtyTable",
            "testEditMapEntry",
            "testEditMapEntryTable",
            "testInsertRefKey",
            "testInsertRefKeyTable",
            "testInsertSameKey",
            "testInsertSameKeyTable",
            "testRemoveRefKey",
            "testRemoveRefKeyTable",
            "testInsertRefRef",
            "testUpdateRefRef",
            "testRemoveRefRef",
            "testRemoveRefRefViaKeySet",
            "testClearRefRefViaKeySet",
            "testClearRefRefViaValues",
            "testBasicTreeMap",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestMaps(a[i]));
        }
        return s;
    }

    public void testDeleteInstanceInMap() {
        PersistenceManager pm = pmf().getPersistenceManager();
        MapModel mapModel = new MapModel();
        pm.currentTransaction().begin();
        pm.makePersistent(mapModel);
        Person p = new Person("name");
        mapModel.getRefKeyMap().put(p, "name");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getRefKeyMap().remove(p);
        pm.deletePersistent(p);
        mapModel.getRefKeyMap().put(new Person("name1"), "name1");
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testReplaceHashMapWithNull() throws Exception {
//        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        MapModel mapModel = new MapModel();
        pm.currentTransaction().begin();
        pm.makePersistent(mapModel);
        HashMap map = new HashMap();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        mapModel.setBasicMap(map);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(map, mapModel.getBasicMap());
        mapModel.setBasicMap(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        if (mapModel.getBasicMap() != null) {
            Assert.assertTrue(mapModel.getBasicMap().size() == 0);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testReplaceHashMap() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        MapModel mapModel = new MapModel();
        pm.currentTransaction().begin();
        pm.makePersistent(mapModel);
        HashMap map = new HashMap();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        HashMap map2 = new HashMap();
        for (int i = 10; i < 30; i++) {
            map2.put("key" + i, "value" + i);
        }
        mapModel.setBasicMap(map);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(map, mapModel.getBasicMap());
        mapModel.setBasicMap(map2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(map2, mapModel.getBasicMap());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBasicInsert() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBasicInsertTable() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicTable().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBasicDelete() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(mapModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBasicDeleteTable() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicTable().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(mapModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    private void nukeMapModel() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        for (Iterator i = ((Collection)q.execute()).iterator(); i.hasNext(); ) {
            pm.deletePersistent(i.next());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSCOMakeDirty() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(mapModel));

        pm.currentTransaction().begin();
        mapModel.getBasicMap().put("key2", "val2");
        Assert.assertTrue(mapModel.getBasicMap().size() == 2);
        pm.currentTransaction().commit();

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.setIgnoreCache(true);
        pm2.currentTransaction().begin();
        Extent extent = pm2.getExtent(MapModel.class, false);
        Iterator iter = extent.iterator();
        int c = 0;
        while (iter.hasNext()) {
            MapModel mm = (MapModel)iter.next();
            Assert.assertEquals(2, mm.getBasicMap().size());
            Assert.assertEquals("val1", mm.getBasicMap().get("key1"));
            Assert.assertEquals("val2", mm.getBasicMap().get("key2"));
            c++;
        }
        Assert.assertTrue(c == 1);
        pm2.currentTransaction().commit();
        pm2.close();
        pm.close();
    }

    public void testSCOMakeDirtyTable() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicTable().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(mapModel));

        pm.currentTransaction().begin();
        mapModel.getBasicTable().put("key2", "val2");
        Assert.assertTrue(mapModel.getBasicTable().size() == 2);
        pm.currentTransaction().commit();

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.setIgnoreCache(true);
        pm2.currentTransaction().begin();
        Extent extent = pm2.getExtent(MapModel.class, false);
        Iterator iter = extent.iterator();
        int c = 0;
        while (iter.hasNext()) {
            MapModel mm = (MapModel)iter.next();
            Assert.assertTrue(mm.getBasicTable().size() == 2);
            c++;
        }
        Assert.assertTrue(c == 1);
        pm2.currentTransaction().commit();
        pm2.close();
        pm.close();
    }

    public void testEditMapEntry() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(mapModel));

        pm.currentTransaction().begin();
        mapModel.getBasicMap().put("key1", "val2");
        Assert.assertTrue(Utils.isPDirty(mapModel));
        pm.currentTransaction().commit();
        pm.close();

        checkEditMapEntry();
    }

    public void testEditMapEntryTable() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicTable().put("key1", "val1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(mapModel));

        pm.currentTransaction().begin();
        mapModel.getBasicTable().put("key1", "val2");
        Assert.assertTrue(Utils.isPDirty(mapModel));
        pm.currentTransaction().commit();
        pm.close();

        checkEditMapEntryTable();
    }

    private void checkEditMapEntry() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(MapModel.class, false);
        Iterator iterator = extent.iterator();
        while (iterator.hasNext()) {
            MapModel mapModel = (MapModel)iterator.next();
            mapModel.getBasicMap().get("key1").equals("val2");
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    private void checkEditMapEntryTable() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(MapModel.class, false);
        Iterator iterator = extent.iterator();
        while (iterator.hasNext()) {
            MapModel mapModel = (MapModel)iterator.next();
            mapModel.getBasicTable().get("key1").equals("val2");
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInsertRefKey() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyMap().put(new Person("p1"), "p1");
        pm.makePersistent(mapModel);
        pm.close();
    }

    public void testInsertRefKeyTable() throws Exception {
        nukeMapModel();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyTable().put(new Person("p1"), "p1");
        pm.makePersistent(mapModel);
        pm.close();
    }

    public void testInsertSameKey() throws Exception {
        nukeMapModel();
        Person p = new Person("p1");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyMap().put(p, "p1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        String val = (String)mapModel.getRefKeyMap().get(p);
        Assert.assertEquals("p1", val);
        mapModel.getRefKeyMap().put(p, "p2");
        pm.currentTransaction().commit();
        pm.currentTransaction().setNontransactionalRead(true);
        checkInsertSameKey(p);
        pm.close();
    }

    public void testInsertSameKeyTable() throws Exception {
        nukeMapModel();
        Person p = new Person("p1");
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setDatastoreTxLocking(
                VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyTable().put(p, "p1");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        String val = (String)mapModel.getRefKeyTable().get(p);
        Assert.assertEquals("p1", val);
        mapModel.getRefKeyTable().put(p, "p2");
        pm.currentTransaction().commit();
        pm.currentTransaction().setNontransactionalRead(true);
        checkInsertSameKeyTable(p);
        pm.close();
    }

    private void checkInsertSameKey(Person p) {
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setDatastoreTxLocking(
                VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(MapModel.class, false);
        Iterator iterator = extent.iterator();
        int c = 0;
        while (iterator.hasNext()) {
            c++;
            MapModel mapModel = (MapModel)iterator.next();
            String val = (String)mapModel.getRefKeyMap().get(p);
            Assert.assertEquals("p2", val);
        }
        Assert.assertTrue(c == 1);
        pm.currentTransaction().commit();
        pm.close();
    }

    private void checkInsertSameKeyTable(Person p) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(MapModel.class, false);
        Iterator iterator = extent.iterator();
        int c = 0;
        while (iterator.hasNext()) {
            c++;
            MapModel mapModel = (MapModel)iterator.next();
            String val = (String)mapModel.getRefKeyTable().get(p);
            Assert.assertEquals("p2", val);
        }
        Assert.assertTrue(c == 1);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRemoveRefKey() throws Exception {
        nukeMapModel();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyMap().put(p1, "p1");
        mapModel.getRefKeyMap().put(p2, "p2");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefKeyMap().size() == 2);
        mapModel.getRefKeyMap().remove(p1);
        Assert.assertTrue(mapModel.getRefKeyMap().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefKeyMap().size() == 1);
        Assert.assertEquals("p2", mapModel.getRefKeyMap().get(p2));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRemoveRefKeyTable() throws Exception {
        nukeMapModel();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefKeyTable().put(p1, "p1");
        mapModel.getRefKeyTable().put(p2, "p2");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefKeyTable().size() == 2);
        mapModel.getRefKeyTable().remove(p1);
        Assert.assertTrue(mapModel.getRefKeyTable().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefKeyTable().size() == 1);
        Assert.assertEquals("p2", mapModel.getRefKeyTable().get(p2));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInsertRefRef() throws Exception {
        nukeMapModel();
        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, mapModel.getRefRefMap().size());
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), pVal1);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), pVal2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUpdateRefRef() throws Exception {
        nukeMapModel();
        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Person pVal2Mod = new Person("pVal2Mod");
        Assert.assertTrue(mapModel.getRefRefMap().size() == 2);
        mapModel.getRefRefMap().put(pKey2, pVal2Mod);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefRefMap().size() == 2);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), pVal1);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), pVal2Mod);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRemoveRefRef() throws Exception {
        nukeMapModel();
        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getRefRefMap().remove(pKey1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getRefRefMap().size() == 1);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), null);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), pVal2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRemoveRefRefViaKeySet() throws Exception {
        nukeMapModel();

        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getRefRefMap().keySet().remove(pKey1);
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        Assert.assertTrue(mapModel.getRefRefMap().size() == 1);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), null);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), pVal2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testClearRefRefViaKeySet() throws Exception {
        nukeMapModel();

        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getRefRefMap().keySet().clear();
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        Assert.assertEquals(0, mapModel.getRefRefMap().size());
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), null);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), null);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testClearRefRefViaValues() throws Exception {
        nukeMapModel();

        Person pKey1 = new Person("pKey1");
        Person pVal1 = new Person("pVal1");
        Person pKey2 = new Person("pKey2");
        Person pVal2 = new Person("pVal2");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getRefRefMap().put(pKey1, pVal1);
        mapModel.getRefRefMap().put(pKey2, pVal2);
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getRefRefMap().values().clear();
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Debug.OUT.println("####### mapModel = " + mapModel.getRefRefMap());
        Assert.assertTrue(mapModel.getRefRefMap().size() == 0);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey1), null);
        Assert.assertEquals(mapModel.getRefRefMap().get(pKey2), null);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBasicTreeMap() throws Exception {
        nukeMapModel();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getBasicTreeMap().put("keyC", "valC");
        mapModel.getBasicTreeMap().put("keyA", "valA");
        mapModel.getBasicTreeMap().put("keyB", "valB");
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(mapModel.getBasicTreeMap().firstKey().equals("keyA"));
        Assert.assertTrue(mapModel.getBasicTreeMap().lastKey().equals("keyC"));
        pm.currentTransaction().commit();
        pm.close();
    }
}

