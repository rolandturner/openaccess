
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

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.externalizer.ExtContainer;
import com.versant.core.jdo.junit.test3.model.externalizer.SerializableType;
import com.versant.core.jdo.junit.test3.model.externalizer.Address;
import com.versant.core.jdo.junit.test3.model.externalizer.Address2;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Tests for externalized field support.
 */
public class TestExternalization extends VersantTestCase {

    /**
     * Test persisting externalized fields of various types.
     */
    public void testCRUD() {
        if ("informixse".equals(getDbName())) {
            unsupported();
            return;
        }

        // make sure fields are externalized as parts of this test will work
        // anyway (e.g. the address reference)
        ClassMetaData cmd = getCmd(ExtContainer.class);
        assertTrue(
                cmd.getFieldMetaData("defExtType").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("stringExtType").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("address").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("address2").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("object").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("list").category == MDStatics.CATEGORY_EXTERNALIZED);
        assertTrue(
                cmd.getFieldMetaData("map").category == MDStatics.CATEGORY_EXTERNALIZED);

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ExtContainer con = new ExtContainer();
        con.setDefExtType(new SerializableType("con"));
        con.setStringExtType(new SerializableType("acon"));
        con.setAddress(new Address("street", "city"));
        con.setAddress2(new Address2("street2", "city2"));
        con.setObject("piglet");
        con.setList(mk(new Object[]{"a", "b", "c"}));
        con.setMap(mk(new Object[]{"a", "b"}, new Object[]{"aa", "bb"}));
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("con", con.getDefExtType().getData());
        assertEquals("acon", con.getStringExtType().getData());
        assertEquals("street/city", con.getAddress().toString());
        assertEquals("street2/city2", con.getAddress2().toString());
        assertEquals("piglet", con.getObject());
        assertEquals("[a, b, c]", con.getList().toString());
        assertEquals("[a=aa, b=bb]", con.getMapStr());
        con.setDefExtType(new SerializableType("con2"));
        con.setStringExtType(new SerializableType("acon2"));
        con.setAddress(new Address("street2", "city2"));
        con.setAddress2(new Address2("street22", "city22"));
        con.setObject("piglet2");
        con.setList(mk(new Object[]{"d", "e"}));
        con.setMap(mk(new Object[]{"d", "e"}, new Object[]{"dd", "ee"}));
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("con2", con.getDefExtType().getData());
        assertEquals("acon2", con.getStringExtType().getData());
        assertEquals("street2/city2", con.getAddress().toString());
        assertEquals("street22/city22", con.getAddress2().toString());
        assertEquals("piglet2", con.getObject());
        assertEquals("[d, e]", con.getList().toString());
        assertEquals("[d=dd, e=ee]", con.getMapStr());
        con.getDefExtType().setData("con3");
        con.getStringExtType().setData("acon3");
        con.getAddress().setCity("city3");
        con.getAddress2().setCity("city32");
        con.setObject("piglet3");
        con.getList().set(1, "f");
        con.getMap().put("e", "ff");
        JDOHelper.makeDirty(con, "defExtType");
        JDOHelper.makeDirty(con, "stringExtType");
        JDOHelper.makeDirty(con, "address");
        JDOHelper.makeDirty(con, "address2");
        JDOHelper.makeDirty(con, "object");
        JDOHelper.makeDirty(con, "list");
        JDOHelper.makeDirty(con, "map");
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("con3", con.getDefExtType().getData());
        assertEquals("acon3", con.getStringExtType().getData());
        assertEquals("street2/city3", con.getAddress().toString());
        assertEquals("street22/city32", con.getAddress2().toString());
        assertEquals("piglet3", con.getObject());
        assertEquals("[d, f]", con.getList().toString());
        assertEquals("[d=dd, e=ff]", con.getMapStr());
        pm.currentTransaction().commit();

        pm.close();
    }

    private ArrayList mk(Object[] a) {
        return new ArrayList(Arrays.asList(a));
    }

    private HashMap mk(Object[] key, Object[] value) {
        HashMap map = new HashMap();
        for (int i = 0; i < key.length; i++) map.put(key[i], value[i]);
        return map;
    }

}

