
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
package com.versant.core.jdo.junit.test2;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test2.model.embedded.*;
import com.versant.core.jdo.VersantQuery;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantDetachedFieldAccessException;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcLinkCollectionField;
import com.versant.core.jdbc.metadata.JdbcMapField;
import com.versant.core.jdbc.metadata.JdbcColumn;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.JDOUnsupportedOptionException;
import java.util.List;
import java.util.Collection;

/**
 * Tests for embedded reference fields.
 *
 * Embedded fg spec.
 * ----------------
 *
 * The fields that are by default in the default fg of the embedded will also be
 * in the default fg of the embedded instance. This holds recursivly embedded instance
 * that have embedded instances.
 *
 * If a field is explicitly marked as in the default fg in the embedded instance
 * then it will also be included in the default fg of the owner.
 *
 * If the embedded ref is marked as being in the default fg then it does not add
 * all of it fields to the default fg of the owner. This is ignored in other words.
 *
 * If the embedded ref is marked as not being in the default fg then its and all
 * it recursivle embedded instances will also not be in the dfg.
 *
 * The fg for an embedded instance is created in its own jdo metadata declaration
 * and is refered to via the next-fg. This behaviour will change for the jdo2 style
 * fetchgroups when it will be done automatically by name.
 *
 * The inclusion to the default fg for an embedded ref may either be set in the metadata
 * of the class that references it, or on a root object.
 *
 *
 * TODO:
 * * Must add code to reset all fields if an embedded ref is set to null.
 * * Must add check to see if an embedded instance was attached to another instance.
 *      This should probably not be allowed. Should discuss.
 *
 */
public class TestEmbedded extends VersantTestCase {

    /**
     * This test must check the fetching for recursivle embedded instances.
     * The fields of these recursivle embedded instances should be fetched as
     * per the defined fg. The fg must be defined on the owner class.
     */

    /**
     * Add a check for a accessing a not detached field in an recusivly embedded
     * scenario.
     */

    /**
     * If a ClassA is extended by ClassB and ClassB is marked as embedded-only then
     * no columns should be created in the table of ClassA. This currently happens if
     * the inheritance is flat.
     *
     * OA-202
     *
     * This is a jdbc only test.
     */
    public void testNoColumsCreatedInBaseClassOfEmbeddedOnly() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        ClassMetaData cmd = getCmd(com.versant.core.jdo.junit.test2.model.embedded.CoreNaming.class);
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        List colList = jdbcClass.table.getColumnList();
        if (colList.size() > 4) {
            fail("The class should only have 4 cols in the table. " +
                    "If extra fields was intentially added then" +
                    "please update the test");
        }
    }

    /**
     * Jdbc column/table names that is specified in the metadata of an embedded
     * class should not be used in the owning instance. An example of where this
     * will break is if 2 classes both embed classA and classA defines the name
     * of the linktable for a collection that it declares.
     *
     * Test that the link table name is not as is defined in the metadata of the
     * embedded class.
     */
    public void testNameOfLinkTable() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        ClassMetaData cmd = getCmd(PowerSystemResource.class);
        FieldMetaData fmd = cmd.getFieldMetaData("PSRType");
        FieldMetaData[] embeddedFmds = fmd.embeddedFmds;
        FieldMetaData arrayFmd = null;
        FieldMetaData listFmd = null;
        FieldMetaData mapFmd = null;
        for (int i = 0; i < embeddedFmds.length; i++) {
            FieldMetaData embeddedFmd = embeddedFmds[i];
            if ("PSRType/PowerSystemResource".equals(embeddedFmd.name)) {
                arrayFmd = embeddedFmd;
            } else if ("PSRType/listField".equals(embeddedFmd.name)) {
                listFmd = embeddedFmd;
            } else if ("PSRType/mapField".equals(embeddedFmd.name)) {
                mapFmd = embeddedFmd;
            }
        }
        if (arrayFmd == null) {
            fail("Could not find the embedded field 'PSRType/PowerSystemResource'");
        }
        if (listFmd == null) {
            fail("Could not find the embedded field 'PSRType/listField'");
        }
        if (mapFmd == null) {
            fail("Could not find the embedded field 'PSRType/mapField'");
        }

        JdbcLinkCollectionField jdbcField = (JdbcLinkCollectionField) arrayFmd.storeField;
        if ("notbeused".equals(jdbcField.linkTable.name.toLowerCase())) {
            fail("Jdbc column names and table names of embedded instances should" +
                    "be inherited");
        }
        JdbcMapField jdbcMapField = (JdbcMapField) mapFmd.storeField;
        if ("notbeused".equals(jdbcMapField.linkTable.name.toLowerCase())) {
            fail("Jdbc column names and table names of embedded instances should" +
                    "be inherited");
        }
        JdbcLinkCollectionField jdbcListField = (JdbcLinkCollectionField) listFmd.storeField;
        if ("notbeused".equals(jdbcListField.linkTable.name.toLowerCase())) {
            fail("Jdbc column names and table names of embedded instances should" +
                    "be inherited");
        }
    }

    /**
     * Test that using a field with a type that is marked as embedded-only is
     * embedded by default.
     */
    public void testEmbeddedOnlyRefIsEmbedded() {
        ClassMetaData cmd = getCmd(PowerSystemResource.class);
        FieldMetaData fmd = cmd.getFieldMetaData("PSRType");
        if (!fmd.isEmbeddedRef()) {
            fail("A persistent reference of a class that is " +
                    "'embedded-only' must be embedded");
        }

        FieldMetaData[] embeddedFmds = fmd.embeddedFmds;
        FieldMetaData eFmd = null;
        for (int i = 0; i < embeddedFmds.length; i++) {
            FieldMetaData embeddedFmd = embeddedFmds[i];
            if ("PSRType/psrTypeVal".equals(embeddedFmd.name)) {
                eFmd = embeddedFmd;
                break;
            }
        }
        if (eFmd == null) {
            fail("Could not find the embedded field 'PSRType/psrTypeVal'");
        }
    }

    /**
     * Test case for embedded reference that is detached as part of its owner
     * and then assigned to another detached instance. This is not allowed.
     */
    public void testDetachedEmbeddedRefSharing() {
    }

    /**
     * Test the loading of the default fg for embedded instances. The default fg
     * fields of the embedded instances should be in the default fg of the owner.
     *
     * Must also add a field that is not in the default fg by default(like a collection)
     * to the default fg of the embedded instance. These field must also be loaded.
     *
     * - Points to test.
     * That fields that are by default in the default fg is loaded in one group.
     *
     * - That fields that are not by default in the default fg, but was explicitly
     * added to the default fg is included.
     *
     * - That an embedded reference that was excluded from the default fg on its owner
     * will not have its and its embedded instance loaded as part of the default fg.
     *
     */
    public void testDetachEmbeddeDefaultFg1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.declareParameters("String param");
        List results = (List) q.execute(name);

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) results.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(1, countExecQueryEvents());

        //this should generate a query.
        p.getAddress().getAddArray();
        assertEquals(1, countExecQueryEvents());

        p.getAddress().getCountry().getCode();
        assertEquals(1, countExecQueryEvents());

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDetachEmbeddeFg1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) results.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(1, countExecQueryEvents());

        //this should generate a query.
        p.getAddress().getAddArray();
        assertEquals(1, countExecQueryEvents());

        p.getAddress().getStreetNumber();
        assertEquals(1, countExecQueryEvents());

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDetachEmbedded1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) detached.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDetachEmbedded2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) detached.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(0, countExecQueryEvents());
        try {
            p.getAddress().getAddArray();
            fail("Expected a field access vialation. This field is not supposed to be accessible");
        } catch (VersantDetachedFieldAccessException e) {
            //ignore expected error
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDetachEmbedded3() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) detached.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(0, countExecQueryEvents());

        Address[] newAdds = new Address[] {new Address("addArray-street-" + val,
                "addArray-city-" + val)};
        p.getAddress().setAddArray(newAdds);

        Address[] newAdds2 = p.getAddress().getAddArray();
        assertEquals(newAdds2.length, newAdds.length);
        assertEquals(newAdds2[0].getStreet(), newAdds[0].getStreet());
        assertEquals("addArray-city-" + val, newAdds[0].getCity());

        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test update of embedded fields values.
     */
    public void testDetachEmbedded4() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");

        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) detached.iterator().next();
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(0, countExecQueryEvents());

        Address[] newAdds = new Address[] {new Address("addArray-street-" + val, "addArray-city-" + val)};
        p.getAddress().setAddArray(newAdds);

        Address[] newAdds2 = p.getAddress().getAddArray();
        assertEquals(newAdds2.length, newAdds.length);
        assertEquals(newAdds2[0].getStreet(), newAdds[0].getStreet());
        assertEquals("addArray-city-" + val, newAdds[0].getCity());

        Collection attached = ((VersantPersistenceManager)pm).versantAttachCopy(detached, true);
        List dirtyList = ((VersantPersistenceManager)pm).versantAllDirtyInstances();
        assertEquals(2, dirtyList.size());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery) pm.newQuery(q);
        results = (List) q.execute(name);
        p = (Person) results.get(0);

        newAdds2 = p.getAddress().getAddArray();
        assertEquals(newAdds2.length, newAdds.length);
        assertEquals(newAdds2[0].getStreet(), newAdds[0].getStreet());
        assertEquals("addArray-city-" + val, newAdds[0].getCity());

        pm.close();
    }

    /**
     * Test setting a embedded field to null. The field does not have a nullIndicator.
     */
    public void testDetachEmbedded5() {
        if (true) {
            broken();
            return;
        }

        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");
        //this should the address and all it recursivly embedded fields
        p = (Person) detached.iterator().next();
        p.setAddress(null);

        Collection attached = ((VersantPersistenceManager)pm).versantAttachCopy(detached, true);
        List dirtyList = ((VersantPersistenceManager)pm).versantAllDirtyInstances();
        assertEquals(1, dirtyList.size());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery) pm.newQuery(q);
        results = (List) q.execute(name);
        p = (Person) results.get(0);

        assertNull(p.getAddress());
        pm.close();
    }

    /**
     * Test setting a currently 'null' embedded field of a detached instance
     * to a new value.
     */
    public void testDetachEmbedded6() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");
        //this should the address and all it recursivly embedded fields
        p = (Person) detached.iterator().next();
        p.setAddress(new Address("newAddress-street2-" + val, "newAddress-city2-" + val));


        Collection attached = ((VersantPersistenceManager)pm).versantAttachCopy(detached, true);
        List dirtyList = ((VersantPersistenceManager)pm).versantAllDirtyInstances();
        assertEquals(1, dirtyList.size());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery) pm.newQuery(q);
        results = (List) q.execute(name);
        p = (Person) results.get(0);

        assertEquals("newAddress-street2-" + val, p.getAddress().getStreet());
        pm.close();
    }

    /**
     * Test updating of a deep embedded field. (ie person.address.country.setName())
     */
    public void testDetachEmbedded7() {
        if (!isEmbeddedPCFieldSupported())
            return;

        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        Collection detached = ((VersantPersistenceManager)pm).versantDetachCopy(results, "pfg1");
        //this should the address and all it recursivly embedded fields
        p = (Person) detached.iterator().next();
        p.getAddress().getCountry().setName("ZA2-" + val);

        Collection attached = ((VersantPersistenceManager)pm).versantAttachCopy(detached, true);
        List dirtyList = ((VersantPersistenceManager)pm).versantAllDirtyInstances();
        assertEquals(1, dirtyList.size());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery) pm.newQuery(q);
        results = (List) q.execute(name);
        p = (Person) results.get(0);

        assertEquals("ZA2-" + val, p.getAddress().getCountry().getName());
        pm.close();
    }

    /**
     * Test that the next fg fields of the embedded references are fetched as part
     * of the original query fetch.
     */
    public void testFg1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) results.get(0);
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(1, countExecQueryEvents());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test that fields that should was not part of the fetchgroup generates a
     * query.
     */
    public void testFg2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        String name = "name-" + val;
        Person p = new Person();
        p.setName(name);
        Address a = new Address();
        p.setAddress(a);
        Country c = new Country();
        c.setName("ZA-" + val);
        a.setCountry(c);
        a.setCity("city-" + val);
        a.setStreet("street-" + val);
        Currency currency = new Currency();
        currency.setName("ZAR-" + val);
        p.setCurrency(currency);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class, "name == param");
        q.setFetchGroup("pfg1");
        q.declareParameters("String param");
        List results = (List) q.execute(name);
        countExecQueryEvents();
        System.out.println("results = " + results);
        p = (Person) results.get(0);
        assertEquals(name, p.getName());
        assertEquals("city-" + val, p.getAddress().getCity());
        assertEquals("ZA-" + val, p.getAddress().getCountry().getName());
        assertEquals("ZAR-" + val, p.getCurrency().getName());
        assertEquals(1, countExecQueryEvents());
        p.getAddress().getAddArray();
        assertEquals(1, countExecQueryEvents());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testAddressReAssign() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        Country c = new Country();
        c.setName("ZA");
        a.setCountry(c);
        a.setCity("city");
        a.setStreet("street");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        c = new Country();
        c.setName("country2");
        a.setCountry(c);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("country2", a.getCountry().getName());
        pm.close();
    }

    public void testAddressDelete() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        Country c = new Country();
        c.setName("ZA");
        a.setCountry(c);
        a.setCity("city");
        a.setStreet("street");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Currently we do not allow the sharing of embedded instances.
     */
    public void testEmbeddedShareRefs1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person();
        Person p2 = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p1.setAddress(a);
        pm.makePersistent(p1);
        p2.setAddress(a);
        try {
            pm.makePersistent(p2);
            Utils.fail("Expected JDOUnsupportedException because we do not support " +
                "sharing of embedded references");
        } catch (JDOUnsupportedOptionException e) {
            //ignore
        }
        pm.close();
    }

    public void testEmbeddedShareRefs2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person();
        Address a = new Address();
        p1.setAddress(a);
        pm.makePersistent(p1);
        Person p2 = new Person();
        p2.setAddress(a);
        try {
            pm.makePersistent(p2);
            Utils.fail("Expected JDOUnsupportedException because we do not support " +
                "sharing of embedded references");
        } catch (JDOUnsupportedOptionException e) {
            //ignore
        }
        pm.close();
    }

    public void testEmbeddedShareRefs3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        pm.makePersistent(a);
        Person p1 = new Person();
        p1.setAddress(a);
        try {
            pm.makePersistent(p1);
            Utils.fail("Expected JDOUnsupportedException because we do not support " +
                "sharing of embedded references");
        } catch (JDOUnsupportedOptionException e) {
            //ignore
        }
        pm.close();
    }

    public void testEmbedded1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        Country c = new Country();
        c.setName("South Africa");
        c.setTimezone("UCT");
        a.setCountry(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        System.out.println("p.getName() = " + p.getName());
        System.out.println("p.getAddress() = " + p.getAddress());
        System.out.println("p.getAddress() = " + p.getAddress());
        System.out.println("p.getAddress() = " + p.getAddress());
        a = p.getAddress();
        a.setCity("city2");
        c = a.getCountry();
        assertEquals("South Africa", c.getName());
        assertEquals("UCT", c.getTimezone());
        c.setName("SA");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("city2", p.getAddress().getCity());
        assertEquals("SA", p.getAddress().getCountry().getName());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person) pm.getObjectById(id, true);
        System.out.println("p = " + p);
        a = p.getAddress();
        assertEquals("city2", a.getCity());
        pm.close();
    }

    public void testEmbedded2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        pm.makePersistent(p);
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        System.out.println("p.getName() = " + p.getName());
        System.out.println("p.getAddress() = " + p.getAddress());
        System.out.println("p.getAddress() = " + p.getAddress());
        System.out.println("p.getAddress() = " + p.getAddress());
        a = p.getAddress();
        a.setCity("city2");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("city2", p.getAddress().getCity());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person) pm.getObjectById(id, true);
        System.out.println("p = " + p);
        a = p.getAddress();
        assertEquals("city2", a.getCity());
        pm.close();
    }

    public void testEmbedded3() {
        if (!isEmbeddedPCFieldSupported())
            return;

        getCmd(Person.class).dump();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        pm.makePersistent(p);
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        Country country = new Country();
        country.setName("c1");
        a.setCountry(country);
        a.setNonEmbeddedAddress(new Address("non_street", "non_city"));
        pm.currentTransaction().commit();

        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        a = p.getAddress();
        a.setCity("city2");
        assertEquals("non_street", a.getNonEmbeddedAddress().getStreet());
        assertEquals("non_city", a.getNonEmbeddedAddress().getCity());
        assertEquals("c1", a.getCountry().getName());
        Country c2 = new Country();
        c2.setName("c3");
        a.setCountry(c2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("city2", p.getAddress().getCity());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person) pm.getObjectById(id, true);
        System.out.println("p = " + p);
        a = p.getAddress();
        assertEquals("city2", a.getCity());
        pm.close();
    }

    public void testEmbedded4() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Primitives prim = new Primitives();
        p.setPrims(prim);
        pm.makePersistent(p);
        prim.setIntField(1);
        prim.setByteField((byte) 2);
        prim.setCharField('3');
        prim.setDoubleField(4);
        prim.setFloatField(5);
        prim.setLongField(6);
        prim.setShortField((short) 7);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(1, prim.getIntField());
        assertEquals(2, prim.getByteField());
        assertEquals('3', prim.getCharField());
        assertEquals(4, prim.getDoubleField(), 0);
        assertEquals(5, prim.getFloatField(), 0);
        assertEquals(6, prim.getLongField());
        assertEquals(7, prim.getShortField());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        prim.setIntField(10);
        prim.setByteField((byte) 20);
        prim.setCharField('9');
        prim.setDoubleField(40);
        prim.setFloatField(50);
        prim.setLongField(60);
        prim.setShortField((short) 70);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(10, prim.getIntField());
        assertEquals(20, prim.getByteField());
        assertEquals('9', prim.getCharField());
        assertEquals(40, prim.getDoubleField(), 0);
        assertEquals(50, prim.getFloatField(), 0);
        assertEquals(60, prim.getLongField());
        assertEquals(70, prim.getShortField());
        pm.currentTransaction().commit();

        pm.close();

    }

    public void testEmbeddedGetObjectId() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        Object ida = pm.getObjectId(a);
        System.out.println("\n\n\n\n\n\n ida = " + ida);
        System.out.println("id = " + id);
        pm.close();
    }

    public void testEmbeddedQuery1() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String param");
        q.setFilter("address.street == param");
        List result = (List) q.execute("street");
        System.out.println("result = " + result);
        pm.close();
    }

    public void testEmbeddedQuery2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        p.setAddress(a);
        p.setName("name");
        Country c = new Country();
        c.setName("South Africa");
        a.setCountry(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String param");
        q.setFilter("address.country.name == param");
        List result = (List) q.execute("South Africa");
        System.out.println("result = " + result);
        pm.close();
    }

    public void testEmbeddedQuery3() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        Address nonEAddress = new Address("nonE-Street", "nonE-City");
        a.setNonEmbeddedAddress(nonEAddress);
        p.setAddress(a);
        p.setName("name");
        Country c = new Country();
        c.setName("South Africa");
        a.setCountry(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String param");
        q.setFilter("address.nonEmbeddedAddress.city == param");
        List result = (List) q.execute("nonE-City");
        assertEquals(1, result.size());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testEmbeddedQuery4() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        a.getStringList().add("bla1");
        Address nonEAddress = new Address("nonE-Street", "nonE-City");
        a.setNonEmbeddedAddress(nonEAddress);
        p.setAddress(a);
        p.setName("name");
        Country c = new Country();
        c.setName("South Africa");
        a.setCountry(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String param");
        q.setFilter("address.stringList.contains(param)");
        List result = (List) q.execute("bla1");
        assertEquals(1, result.size());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testEmbeddedSCOField() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        a.setCity("city");
        a.setStreet("street");
        a.getStringList().add("bla1");
        p.setAddress(a);
        p.setName("name");
        Country c = new Country();
        c.setName("South Africa");
        a.setCountry(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        p = (Person) pm.getObjectById(id, true);
        assertEquals("bla1", p.getAddress().getStringList().get(0));
        assertEquals(1, p.getAddress().getStringList().size());
        pm.close();
    }

    public void testInheritence() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        BaseClass bc = new BaseClass();
        Currency c1 = new Currency();
        Country country = new Country();
        country.setName("country1");
        country.setTimezone("timezone1");
        c1.setCountry(country);
        bc.setBaseCurrency(c1);
        pm.makePersistent(bc);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(bc);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        bc = (BaseClass) pm.getObjectById(id, false);
        assertEquals("country1", bc.getBaseCurrency().getCountry().getName());
        pm.close();
    }

    public void testInheritence2() {
        if (!isEmbeddedPCFieldSupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SubClass bc = new SubClass();
        Currency c1 = new Currency();
        Country country = new Country();
        country.setName("country1");
        country.setTimezone("timezone1");
        c1.setCountry(country);
        bc.setBaseCurrency(c1);

        bc.setSubString("subString");

        Currency sc = new Currency();
        sc.setName("sc");
        bc.setSubCurrency(sc);
        pm.makePersistent(bc);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(bc);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        bc = (SubClass) pm.getObjectById(id, false);
        assertEquals("country1", bc.getBaseCurrency().getCountry().getName());
        assertEquals("sc", bc.getSubCurrency().getName());
        pm.close();
    }

    /**
     * A is a normal instance.
     * B is embedded in A.
     * C is embedded in B.
     *
     * If B and C specify the same nullIndicator column then check that only one
     * fake field is created.
     */
    public void testEmbeddedRefGraphWithSharedNullIndicatorCol() {
        if (true) {
            //to implement
            broken();
        }
    }

    /**
     * A is a normal instance.
     * B is embedded in A.
     * C is embedded in A.
     *
     * If B and C specify the same nullIndicator column then check that only one
     * fake field is created.
     */
    public void testEmbeddedRefGraphWithSharedNullIndicatorCol2() {
        if (true) {
            //to implement
            broken();
        }
    }

}
