
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

import junit.framework.Assert;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test2.model.*;
import com.versant.core.jdo.junit.test2.model.gandres.Contact_VO;
import com.versant.core.jdo.junit.test2.model.neil.Car;
import com.versant.core.jdo.junit.test2.model.neil.Company;
import com.versant.core.jdo.junit.test2.model.neil.Client;
import com.versant.core.jdo.junit.test2.model.neil.SubClient;
import com.versant.core.jdo.junit.test2.model.dc.JobDescription;
import com.versant.core.jdo.junit.test2.model.dc.JobPosting;
import com.versant.core.jdo.junit.test2.model.alex.*;
import com.versant.core.jdo.junit.test2.model.bair.Authorization;
import com.versant.core.jdo.junit.test2.model.bair.LaborItem;
import com.versant.core.jdo.junit.test2.model.bair.Part;
import com.versant.core.jdo.junit.test2.model.brian.SalesOrderDetailTx;
import com.versant.core.jdo.junit.test2.model.brian.SalesOrderHeaderTx;
import com.versant.core.jdo.junit.test2.model.brian.SalesOrderPaymentTx;
import com.versant.core.jdo.junit.test2.model.elaine.Option;
import com.versant.core.jdo.junit.test2.model.hierarchy.*;
import com.versant.core.jdo.junit.test2.model.mbevan1.Item;
import com.versant.core.jdo.junit.test2.model.mbevan1.TimeLine;
import com.versant.core.jdo.junit.test2.model.ojolly.KnowledgeObject;
import com.versant.core.jdo.junit.test2.model.ojolly.KnowledgeObjectAncestors;
import com.versant.core.jdo.junit.test2.model.refpk.TriRef;
import com.versant.core.jdo.junit.test2.model.rworld.Menu;
import com.versant.core.jdo.junit.test2.model.rworld.User;
import com.versant.core.jdo.junit.test2.model.rworld.UserGroup;
import com.versant.core.jdo.junit.test2.model.stevemc.Device;
import com.versant.core.jdo.junit.test2.model.tom.ListContainer;
import com.versant.core.jdo.junit.test2.model.tom.ListableElement;
import com.versant.core.jdo.*;
import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEventStore;
import com.versant.core.jdbc.conn.JDBCConnectionPool;
import com.versant.core.jdbc.logging.JdbcLogEvent;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.ServerLogEvent;

import javax.jdo.Extent;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

/**
 * Assorted JDOQL tests.
 */
public class QueryTests2 extends VersantTestCase {

    public void testUnbound44() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection childs = new HashSet();
        Child2 c = new Child2();
        childs.add(c);

        c.setCity("cityName");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        BabySitter bs = new BabySitter();
        bs.setCity("cityName");
        pm.makePersistent(bs);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Child2.class,"(this.city.startsWith(\"M\"))");
        List results = (List) q.execute();
        System.out.println("results.size() = " + results.size());
        pm.close();
    }

    public void testUnbound2() {
        if (isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection childs = new HashSet();
        Child2 c = new Child2();
        childs.add(c);

        c.setCity("cityName");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        BabySitter bs = new BabySitter();
        bs.setCity("cityName");
        pm.makePersistent(bs);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Child2.class);
        q.declareVariables("BabySitter bs");
        q.declareParameters("Collection availableBabysiters");
        q.setFilter("city == bs.city && availableBabysiters.contains(bs)");
        q.setResult("this, bs");
        List results = (List) q.execute(childs);
        System.out.println("results.size() = " + results.size());

        for (int i = 0; i < results.size(); i++) {
            Object[] objects = (Object[]) results.get(i);
            c = (Child2) objects[0];
            System.out.println("c.getCity() = " + c.getCity());
            bs = (BabySitter) objects[1];
            System.out.println("bs.getCity() = " + bs.getCity());
        }
        pm.close();
    }

    public void testUnbound4() {
        if (isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection childs = new HashSet();
        Child2 c = new Child2();
        childs.add(c);

        c.setCity("cityName");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        BabySitter bs = new BabySitter();
        bs.setCity("cityName");
        pm.makePersistent(bs);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Child2.class);
        q.declareVariables("BabySitter bs");
        q.declareParameters("Collection availableBabysiters");

        q.setFilter("availableBabysiters.contains(bs) && city == bs.city");
//        q.setFilter("availableBabysiters.contains(bs) && bs.city == param && bs.country.name == param2");
        //join BabySitterTable as b
        q.setResult("this, bs");
        List results = (List) q.execute(childs);

        System.out.println("results.size() = " + results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] objects = (Object[]) results.get(i);
            c = (Child2) objects[0];
            System.out.println("c.getCity() = " + c.getCity());
            bs = (BabySitter) objects[1];
            System.out.println("bs.getCity() = " + bs.getCity());
        }
        pm.close();
    }

    public void testUnbound3() {
        if (isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection childs = new HashSet();
        Child2 c = new Child2();
        childs.add(c);

        c.setCity("cityName");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        c = new Child2();
        c.setCity("cityName2");
        pm.makePersistent(c);
        childs.add(c);

        BabySitter bs = new BabySitter();
        bs.setCity("cityName");
        pm.makePersistent(bs);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(Child2.class);
        q.declareVariables("BabySitter bs");
        q.declareParameters("Collection availableBabysiters");
//        q.setFilter("city == bs.city && availableBabysiters.contains(bs)");
        q.setFilter("availableBabysiters.contains(bs)");

        q.setResult("this, bs");
        List results = (List) q.execute(childs);
//        List results = (List) q.execute();

        for (int i = 0; i < results.size(); i++) {
            Object[] objects = (Object[]) results.get(i);
            c = (Child2) objects[0];
            System.out.println("c.getCity() = " + c.getCity());
            bs = (BabySitter) objects[1];
            System.out.println("bs.getCity() = " + bs.getCity());
        }
        pm.close();
    }
    
    /**
     * Test for bug with jdo_version not being fetched.
     */
    public void testFetchGroupJdoVersion() throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Device d = new Device();
        d.setName("device-01");
        pm.makePersistent(d);
        pm.currentTransaction().commit();
        
        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Device.class);
        query.setFetchGroup("Device_List");
        query.setFilter("this.name == \"device-01\"");
        findExecQuerySQL();
        Collection result = (Collection)query.execute();
        Iterator iter = result.iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        String sql = findExecQuerySQL();
        assertTrue(sql.indexOf("jdo_version") >= 0);
        query.closeAll();
        pm.currentTransaction().commit();
        
        pm.close();
    }
    
    /**
     * Test a setResult query with one result.  
     */
    public void testOneResult() throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Address a = new Address("somewhere", "capetown");
        Contact c = new Contact("joe@soap", a);
        pm.makePersistent(c);
        pm.currentTransaction().commit();
        
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Contact.class);
        q.setFilter("email == 'joe@soap'");
        q.setResult("address");
        ArrayList ans = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(1, ans.size());
        assertTrue(a == ans.get(0));
        pm.currentTransaction().commit();
        
        pm.close();
    }

    public void testForumQuery() {
        if ("mysql".equals(getDbName())) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery) pm.newQuery(Contact_VO.class);
        query.declareImports("import java.util.Date;");
        query.declareVariables("Activity_VO activity; Activity_VO act2");
        query.declareParameters("Date _90days");
        query.setFilter("_owner == 4 && " +
                "_contacttype == 2 && " +
                "activityList.isEmpty() || " +
                "(" +
                    "(" +
                        "activityList.contains(activity) &&" +
                        "(activity.result != 2 || activity.result != 3) && " +
                        "(activity.dueDate < _90days || activity.dueDate == _90days) &&" +
                        "!(activityList.contains(act2) && act2.reason == 11 && act2.dueDate > _90days)" +
                    ")" +
                ")");

        Collection result = (Collection) query.execute(new Date());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testQueryCast() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Company comp = new Company();
        comp.setName("comp");
        Client client = new Client(comp);
        pm.makePersistent(client);

        SubClient subClient = new SubClient(comp);
        pm.makePersistent(subClient);

        Company comp2 = new Company();
        comp2.setName("comp2");
        Client client2 = new Client(comp2);
        pm.makePersistent(client2);

        SubClient subClient2 = new SubClient(comp2);
        pm.makePersistent(subClient2);

        Car car1 = new Car();
        car1.setSeller(client);
        pm.makePersistent(car1);

        Car car2 = new Car();
        car2.setSeller(subClient);
        pm.makePersistent(car2);

        Car car3 = new Car();
        car3.setSeller(client2);
        pm.makePersistent(car3);

        Car car4 = new Car();
        car4.setSeller(subClient2);
        pm.makePersistent(car4);

        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        Extent carExtent = pm.getExtent(Car.class, false);
        Query query = pm.newQuery(carExtent, "((Client) seller).company.name == companyName");
        query.declareParameters("String companyName");
        Collection result = (Collection) query.execute("comp");
        assertEquals(1, result.size());
        assertTrue(result.contains(car1));
        pm.close();
    }

    public void testVariableContains() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleColHolder.class);
        q.setFilter("dfgList.contains(memberA) && dfgList.contains(memberB)");
        q.declareVariables("SimpleColEntry memberA; SimpleColEntry memberB");
        List results = (List) q.execute();
        System.out.println("results.size() = " + results.size());
        pm.close();
    }

    public void testVariableContains2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleColHolder.class);
        q.declareParameters("int paramA, int paramB");
        q.setFilter("dfgList.contains(memberA) && dfgList.contains(memberB) " +
                "&& memberA.intVal == paramA && memberB.intVal == paramB");
        q.declareVariables("SimpleColEntry memberA; SimpleColEntry memberB");
        List results = (List) q.execute(new Integer(1), new Integer(2));
        System.out.println("results.size() = " + results.size());
        pm.close();
    }

    public void testVariableContains3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleColHolder res = null;
        SimpleColHolder ch = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla1"));
        pm.makePersistent(ch);

        ch = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla2"));
        pm.makePersistent(ch);

        ch = res = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla1"));
        ch.getDfgList().add(new SimpleColEntry("bla2"));
        pm.makePersistent(ch);

        ch = new SimpleColHolder();
        pm.makePersistent(ch);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleColHolder.class);
        q.declareParameters("String paramA, String paramB");
        q.setFilter("dfgList.contains(memberA) && dfgList.contains(memberB) " +
                "&& memberA.stringField == paramA && memberB.stringField == paramB");
        q.declareVariables("SimpleColEntry memberA; SimpleColEntry memberB");
        List results = (List) q.execute("bla1", "bla2");
        assertEquals(1, results.size());
        assertTrue(res == results.get(0));
        System.out.println("results.size() = " + results.size());
        pm.close();
    }

//    public void testVariableContains4() {
//        PersistenceManager pm = pmf().getPersistenceManager();
//        pm.currentTransaction().begin();
//        Query q = pm.newQuery(SimpleColHolder.class);
//        q.declareParameters("int paramA");
//        q.declareVariables("SimpleColEntry memberA");
//        q.setFilter("memberA.intVal == paramA && dfgList.contains(memberA)");
//        List results = (List) q.execute(new Integer(1));
//        System.out.println("results.size() = " + results.size());
//        pm.close();
//    }

    public void testSimpleContains() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleColHolder res = null;
        SimpleColHolder ch = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla1"));
        pm.makePersistent(ch);

        ch = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla2"));
        pm.makePersistent(ch);

        ch = res = new SimpleColHolder();
        ch.getDfgList().add(new SimpleColEntry("bla1"));
        ch.getDfgList().add(new SimpleColEntry("bla2"));
        pm.makePersistent(ch);

        ch = new SimpleColHolder();
        pm.makePersistent(ch);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleColHolder.class);
        q.declareVariables("SimpleColEntry memberA");
        q.setFilter("this.dfgList.contains(memberA)");
        List results = (List) q.execute();

        assertEquals(3, results.size());
        System.out.println("results.size() = " + results.size());
        pm.close();


    }

    public void testStringVariableContains() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel am = new ArraysModel();
        am.getStringList().add("1s");
        am.getStringList().add("2s");
        am.getStringList().add("3s");
        am.setVal("bla");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1s");
        am.getStringList().add("2s");
        am.getStringList().add("3s");
        am.setVal("blabla");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1dd");
        am.getStringList().add("2dd");
        am.getStringList().add("3dd");
        am.setVal("bla");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1dd");
        am.getStringList().add("2dd");
        am.getStringList().add("3dd");
        am.setVal("blabla");
        pm.makePersistent(am);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ArraysModel.class);
        q.declareVariables("String sv");
        q.setFilter("stringList.contains(sv) && sv.startsWith(\"%s\") && val == \"bla\"");
        List result = (List) q.execute();
        assertEquals(1, result.size());
        pm.close();
    }

    public void testStringVariableContains2() {
        if (isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel res = null;
        ArraysModel am = res = new ArraysModel();
        am.getStringList().add("1s");
        am.getStringList().add("2s");
        am.getStringList().add("3s");
        am.setVal("2s");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1s");
        am.getStringList().add("2s");
        am.getStringList().add("3s");
        am.setVal("blabla");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1dd");
        am.getStringList().add("2dd");
        am.getStringList().add("3dd");
        am.setVal("bla");
        pm.makePersistent(am);

        am = new ArraysModel();
        am.getStringList().add("1dd");
        am.getStringList().add("2dd");
        am.getStringList().add("3dd");
        am.setVal("blabla");
        pm.makePersistent(am);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ArraysModel.class);
        q.declareVariables("String sv");
        q.setFilter("stringList.contains(sv) && sv.startsWith(\"%s\") && val == sv");
        List result = (List) q.execute();
        assertEquals(1, result.size());
        assertTrue(res == result.get(0));
        pm.close();
    }

    /**
     * Test query that uses a String variable. Note that this is not yet
     * supported.
     */
    public void testStringVariableQuery() {
        if (true) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        JobDescription des = new JobDescription(100);
        des.addArea("010");
        des.addArea("020");
        des.addArea("030");
        JobPosting post = new JobPosting("post", des);
        pm.makePersistent(post);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(JobPosting.class);
        q.declareParameters("Set areas");
        q.declareVariables("String v");
        q.setFilter("position.functionalAreaKeys.contains(v) && areas.contains(v)");
        HashSet areas = new HashSet();
        areas.add("020");
        areas.add("030");
        List ans = (List)q.execute(areas);
        assertEquals(1, ans.size());
        assertTrue(post == ans.get(0));
        pm.currentTransaction().commit();
    }

    public void testCollectionCandidate() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address c1 = new Address("11", "c1");
        pm.makePersistent(c1);
        Address c2 = new Address("22", "c2");
        pm.makePersistent(c2);
        Address c3 = new Address("33", "c3");
        pm.makePersistent(c3);
        Address c4 = new Address("4", "c4");
        pm.makePersistent(c4);
        Address c5 = new Address("5", "c5");
        pm.makePersistent(c5);


        Contact p = new Contact("name", c1);
        pm.makePersistent(p);
        Contact p2 = new Contact("name2", null);
        pm.makePersistent(p2);
        Contact p3 = new Contact("name3", new Address("555", "bla"));
        pm.makePersistent(p3);
        pm.currentTransaction().commit();

        Collection countries = new HashSet();
        countries.add(c1);
        countries.add(c2);
        countries.add(c3);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Contact.class, "c.contains(this.address)");
        q.declareImports("import java.util.Collection;");
        q.declareParameters("Collection c");
        q.compile();

        List result = (List) q.execute(countries);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(p, result.get(0));

        Collection countries2 = new HashSet();
        q = pm.newQuery(Contact.class, "c.contains(this.address)");
        q.declareParameters("Collection c");
        q.compile();
        try {
            result = (List) q.execute(countries2);
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    public void testCollectionCandidate2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address c1 = new Address("11", "c1");
        pm.makePersistent(c1);
        Address c2 = new Address("22", "c2");
        pm.makePersistent(c2);
        Address c3 = new Address("33", "c3");
        pm.makePersistent(c3);
        Address c4 = new Address("4", "c4");
        pm.makePersistent(c4);
        Address c5 = new Address("5", "c5");
        pm.makePersistent(c5);


        Contact p = new Contact("name", c1);
        pm.makePersistent(p);
        Contact p2 = new Contact("name2", null);
        pm.makePersistent(p2);
        Contact p3 = new Contact("name3", new Address("555", "bla"));
        pm.makePersistent(p3);
        pm.currentTransaction().commit();

        Collection countries = new HashSet();
        countries.add(c1);
        countries.add(c2);
        countries.add(c3);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Contact.class, "c.contains(this.address)");
        q.declareImports("import java.util.Collection;");
        q.declareParameters("Collection c");
        q.compile();

        List result = (List) q.execute(countries);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(p, result.get(0));

        Collection countries2 = new HashSet();
        countries2.add(c1);
        countries2.add(c2);
        countries2.add(c3);
        countries2.add(c4);
        countries2.add(c5);
        q = pm.newQuery(Contact.class, "c.contains(this.address)");
        q.declareImports("import java.util.Collection;");
        q.declareParameters("Collection c");
        q.compile();
        result = (List) q.execute(countries2);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(p, result.get(0));

        Collection countries3 = new HashSet();
        countries3.add(c1);
        countries3.add(c2);
        q = pm.newQuery(Contact.class, "c.contains(this.address)");
        q.declareParameters("Collection c");
        q.compile();
        result = (List) q.execute(countries3);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(p, result.get(0));

        pm.close();
    }

    public void testOrderingOnPkField() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Country.class);
        q.setOrdering("code ascending");
        List results = (List)q.execute();
        results.size();
        pm.close();
    }

    public void testNonTxQueryWithRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setNontransactionalWrite(false);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(false);
        pm.setIgnoreCache(false);

        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address("street");
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setRetainValues(true);

        printAll(findAll(Address.class, pm));
        printAll(findAll(Address.class, pm));
        printAll(findAll(Address.class, pm));
        printAll(findAll(Address.class, pm));

        pm.currentTransaction().begin();
        pm.deletePersistentAll(findAll(Address.class, pm));
        pm.currentTransaction().commit();
        pm.close();
    }

    private void printAll(Collection collection) {
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Address a = (Address)iterator.next();
            System.out.println("a = " + a);
        }
    }

    private Collection findAll(Class clazz,
            PersistenceManager persistenceManager) {
        Extent extent = null;

        try {
            Collection result = new ArrayList();

            extent = persistenceManager.getExtent(clazz, false);
            Iterator extentIter = extent.iterator();

            while (extentIter.hasNext()) {
                result.add(extentIter.next());
            }

            return result;
        } catch (RuntimeException e) {
            System.out.println("Exception: " + e);
            throw e;
        } finally {
            if (extent != null) {
                extent.closeAll();
            }
        }
    }

    public void testBrian1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SalesOrderHeaderTx soht = new SalesOrderHeaderTx();

        SalesOrderPaymentTx sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt1");
        soht.getPaymentTxList().add(sopt);

        sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt2");
        soht.getPaymentTxList().add(sopt);

//        soht.getSalesOrderDetailTransactionList().add(new SalesOrderDetailTx("sodt1"));
//        soht.getSalesOrderDetailTransactionList().add(new SalesOrderDetailTx("sodt2"));
        pm.makePersistent(soht);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SalesOrderHeaderTx.class);
        q.declareVariables("TxDetail td");
        q.setFilter("salesOrderDetailTxList.contains(td)");
        List results = (List)q.execute();
        Assert.assertEquals(0, results.size());

        pm.deletePersistentAll(soht.getPaymentTxList());
        pm.deletePersistent(soht);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBrian11() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SalesOrderHeaderTx soht = new SalesOrderHeaderTx();

        SalesOrderPaymentTx sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt1");
        soht.getPaymentTxList().add(sopt);

        sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt2");
        soht.getPaymentTxList().add(sopt);

//        soht.getSalesOrderDetailTransactionList().add(new SalesOrderDetailTx("sodt1"));
//        soht.getSalesOrderDetailTransactionList().add(new SalesOrderDetailTx("sodt2"));
        pm.makePersistent(soht);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SalesOrderHeaderTx.class);
        q.declareVariables("TxDetail td");
        q.setFilter("salesOrderDetailTxList.contains(td)");
        ((VersantQuery)q).setFetchGroup("fkColFG");
        List results = (List)q.execute();
        Assert.assertEquals(0, results.size());

        pm.deletePersistentAll(soht.getPaymentTxList());
        pm.deletePersistent(soht);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBrian2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Collection col = new HashSet();
        pm.currentTransaction().begin();

        for (int i = 0; i < 3; i++) {
            SalesOrderHeaderTx soht = new SalesOrderHeaderTx();
            col.add(soht);

            SalesOrderPaymentTx sopt = new SalesOrderPaymentTx();
            sopt.setVal("sopt1");
            soht.getPaymentTxList().add(sopt);

            sopt = new SalesOrderPaymentTx();
            sopt.setVal("sopt2");
            soht.getPaymentTxList().add(sopt);

            SalesOrderDetailTx sodt = new SalesOrderDetailTx();
            sodt.setVal("sodt1");
            soht.getSalesOrderDetailTxList().add(sodt);

            sodt = new SalesOrderDetailTx();
            sodt.setVal("sodt2");
            soht.getSalesOrderDetailTxList().add(sodt);

            sodt = new SalesOrderDetailTx();
            sodt.setVal("sodt3");
            soht.getSalesOrderDetailTxList().add(sodt);
            pm.makePersistent(soht);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SalesOrderHeaderTx.class);
        q.declareVariables("TxDetail td");
        q.setFilter("salesOrderDetailTxList.contains(td)");
        List results = (List)q.execute();
        Assert.assertEquals(3, results.size());
        for (int i = 0; i < results.size(); i++) {
            SalesOrderHeaderTx soht = (SalesOrderHeaderTx)results.get(i);
            Assert.assertEquals(2, soht.getPaymentTxList().size());
            for (int j = 0; j < soht.getPaymentTxList().size(); j++) {
                //check correct type
                SalesOrderPaymentTx salesOrderPaymentTx = (SalesOrderPaymentTx)soht.getPaymentTxList().get(
                        j);
            }

            Assert.assertEquals(3, soht.getSalesOrderDetailTxList().size());
            for (int j = 0; j < soht.getSalesOrderDetailTxList().size(); j++) {
                //check correct type
                SalesOrderDetailTx salesOrderDetailTx = (SalesOrderDetailTx)soht.getSalesOrderDetailTxList().get(
                        j);
            }
        }

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            SalesOrderHeaderTx salesOrderHeaderTx = (SalesOrderHeaderTx)iterator.next();
            pm.deletePersistentAll(salesOrderHeaderTx.getPaymentTxList());
            pm.deletePersistentAll(
                    salesOrderHeaderTx.getSalesOrderDetailTxList());
            pm.deletePersistent(salesOrderHeaderTx);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBrian3() {
        if ("oracle".equals(getDbName())) {
            broken();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new HashSet();

        SalesOrderHeaderTx soht = new SalesOrderHeaderTx();
        col.add(soht);

        SalesOrderPaymentTx sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt1");
        soht.getPaymentTxList().add(sopt);

        sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt2");
        soht.getPaymentTxList().add(sopt);

//        SalesOrderDetailTx sodt = new SalesOrderDetailTx();
//        sodt.setVal("sodt1");
//        soht.getSalesOrderDetailTransactionList().add(sodt);
//
//        sodt = new SalesOrderDetailTx();
//        sodt.setVal("sodt1");
//        soht.getSalesOrderDetailTransactionList().add(sodt);
        pm.makePersistent(soht);
        pm.currentTransaction().commit();

        System.out.println(
                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(SalesOrderHeaderTx.class);
        ((VersantQuery)q).setFetchGroup("fkColFG");
        q.declareVariables("TxDetail td");
        q.setFilter("salesOrderDetailTxList.contains(td)");

        List results = (List)q.execute();
        Assert.assertEquals(0, results.size());

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            SalesOrderHeaderTx salesOrderHeaderTx = (SalesOrderHeaderTx)iterator.next();
            pm.deletePersistentAll(salesOrderHeaderTx.getPaymentTxList());
            pm.deletePersistentAll(
                    salesOrderHeaderTx.getSalesOrderDetailTxList());
            pm.deletePersistent(salesOrderHeaderTx);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBrian4() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new HashSet();

        SalesOrderHeaderTx soht = new SalesOrderHeaderTx();
        col.add(soht);

        SalesOrderPaymentTx sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt1");
        soht.getPaymentTxList().add(sopt);

        sopt = new SalesOrderPaymentTx();
        sopt.setVal("sopt2");
        soht.getPaymentTxList().add(sopt);

        SalesOrderDetailTx sodt = new SalesOrderDetailTx();
        sodt.setVal("sodt1");
        soht.getSalesOrderDetailTxList().add(sodt);

        sodt = new SalesOrderDetailTx();
        sodt.setVal("sodt1");
        soht.getSalesOrderDetailTxList().add(sodt);
        pm.makePersistent(soht);
        pm.currentTransaction().commit();

        System.out.println(
                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(SalesOrderHeaderTx.class);
        ((VersantQuery)q).setFetchGroup("fkColFG");
        q.declareVariables("TxDetail td");
        q.setFilter("paymentTxList.contains(td)");

        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        soht = (SalesOrderHeaderTx)results.get(0);
        Assert.assertEquals(2, soht.getPaymentTxList().size());
        Assert.assertEquals(2, soht.getSalesOrderDetailTxList().size());

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            SalesOrderHeaderTx salesOrderHeaderTx = (SalesOrderHeaderTx)iterator.next();
            pm.deletePersistentAll(salesOrderHeaderTx.getPaymentTxList());
            pm.deletePersistentAll(
                    salesOrderHeaderTx.getSalesOrderDetailTxList());
            pm.deletePersistent(salesOrderHeaderTx);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCountStar() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new HashSet();

        for (int i = 0; i < 10; i++) {
            Address ad = new Address("street" + i);
            pm.makePersistent(ad);
            col.add(ad);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street.startsWith(\"street\")");
        ((VersantQuery)q).setCountStarOnSize(true);
        ((VersantQuery)q).setMaxRows(5);
        Collection results = (Collection)q.execute();
        System.out.println("results.size() = " + results.size());

        results = (Collection)q.execute();
        System.out.println("results.size() = " + results.size());
        q.closeAll();

        // cleanup
        q = pm.newQuery(Address.class);
        pm.deletePersistentAll((Collection)q.execute());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testIG1() {

        String db = getDbName();
        if (!isJdbc() || db.equals("mysql") || db.equals("db2")) {
            unsupported();
            return;
        }

        Collection toDelete = new ArrayList();
        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Menu menu = null;

        User user = new User();
        user.setName("name" + val);

        UserGroup ug = new UserGroup();
        toDelete.add(ug);
        ug.getUsers().add(user);

        UserGroup ug2 = new UserGroup();
        toDelete.add(ug2);

        //hit
        com.versant.core.jdo.junit.test2.model.rworld.SecurityManager sm =
                new com.versant.core.jdo.junit.test2.model.rworld.SecurityManager();
        menu = new Menu(sm);
        toDelete.add(sm);
        toDelete.add(menu);
        sm.getAdministrators().add(user);
        pm.makePersistent(menu);

        //hit
        sm = new com.versant.core.jdo.junit.test2.model.rworld.SecurityManager();
        menu = new Menu(sm);
        toDelete.add(sm);
        toDelete.add(menu);
        sm.getAdministratorsUserGroups().add(ug);
        pm.makePersistent(menu);

        //no-hit
        sm = new com.versant.core.jdo.junit.test2.model.rworld.SecurityManager();
        menu = new Menu(sm);
        toDelete.add(sm);
        toDelete.add(menu);
        sm.getAdministratorsUserGroups().add(ug2);
        pm.makePersistent(menu);

        //no-hit
        sm = new com.versant.core.jdo.junit.test2.model.rworld.SecurityManager();
        menu = new Menu(sm);
        toDelete.add(sm);
        toDelete.add(menu);
        sm.getAdministratorsUserGroups().add(ug2);
        pm.makePersistent(menu);

        pm.makePersistent(user);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Menu.class);
        q.declareVariables("User user; User user2; UserGroup userGroup");
        q.setFilter("securityManager.administrators.contains(user) " +
                "|| (securityManager.administratorsUserGroups.contains(userGroup) " +
                "&& userGroup.users.contains(user2))");
        List result = (List)q.execute();
        Assert.assertEquals(2, result.size());

        pm.deletePersistentAll(toDelete);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQueryFilter() {
    	if (!isSetResultSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Address a = new Address("street-" + val, "city-" + val);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == p");
        ((VersantQuery)q).setResult("city");
        q.declareParameters("String p");
        Collection results = (Collection) q.execute("street-" + val);
        assertEquals(1, results.size());
        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            String cityName = (String) iterator.next();
            assertEquals("city-" + val, cityName);
        }
        pm.close();
    }

    public void testResultQueryFilter2() {
    	if (!isSetResultSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Address a = new Address("street-" + val, "city-" + val);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == p");
        ((VersantQuery)q).setResult("city");
        ((VersantQuery)q).setUnique(true);
        q.declareParameters("String p");

        String cityName = (String) q.execute("street-" + val);
        assertEquals("city-" + val, cityName);
        pm.close();
    }

    public void testResultQueryFilter3() {
    	if (!isSetResultSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Address a = new Address("street-" + val, "city-" + val);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == p && city == p");
        ((VersantQuery)q).setResult("city");
        ((VersantQuery)q).setUnique(true);
        q.declareParameters("String p");

        String cityName = (String) q.execute("street-" + val);
        assertNull(cityName);
        pm.close();
    }

    public void testResultQueryFilter4() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Address a = new Address("street-" + val, "city-" + val);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == p && city == p");
        ((VersantQuery)q).setResult("");
        ((VersantQuery)q).setUnique(true);
        q.declareParameters("String p");

        String cityName = (String) q.execute("street-" + val);
        assertNull(cityName);
        pm.close();
    }

    public void testResultQueryFilter5() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Address a = new Address("street-" + val, "city-" + val);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == p");
        ((VersantQuery)q).setResult("");
        ((VersantQuery)q).setUnique(true);
        q.declareParameters("String p");

        Address address = (Address) q.execute("street-" + val);
        assertEquals("city-" + val, address.getCity());
        pm.close();
    }

    public void testResultQueryOrderNav() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Ref1 ref1 = new Ref1("ref1Val0", new Ref2("ref2Val0", new Ref3("ref3Val0")));
        pm.makePersistent(ref1);
        ref1 = new Ref1("ref1Val1", new Ref2("ref2Val1", new Ref3("ref3Val1")));
        pm.makePersistent(ref1);
        ref1 = new Ref1("ref1Val2", null);
        pm.makePersistent(ref1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Ref1.class);
        ((VersantQuery)q).setResult("ref2.ref3.ref3Val, count(this)]");
        ((VersantQuery)q).setGrouping("ref2.ref3.ref3Val having count(this) >= n");
        ((VersantQuery)q).setOrdering("ref2.ref3.ref3Val ascending");
        q.declareParameters("Integer n");
        List result = (List) q.execute(new Integer(1));
        for (int i = 0; i < result.size(); i++) {
            Object[] rowData = (Object[]) result.get(i);
            StringBuffer sb = new StringBuffer("Row[");
            for (int j = 0; j < rowData.length; j++) {
                sb.append(rowData[j]);
                sb.append(", ");
            }
            sb.append("]");
            System.out.println("sb.toString() = " + sb.toString());
        }
        pm.close();
    }

    public void testResultQuery1112() {
        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2("bla", new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
//        query.setFilter("name == bla && collection.size() >= 10");
//        query.setResult("this, sum(amount)");
        query.setGrouping("this having sum(amount) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }


    public void testResultQuery1111() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2("bla", new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("ref2.ref3, sum(amount) as total");
        query.setOrdering("total descending");
        query.setGrouping("ref2.ref3 having sum(amount) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery11122() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2("bla", new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("ref2.ref3, sum(ref2.amount2) as total");
        query.setOrdering("total descending");
        query.setGrouping("ref2.ref3 having sum(amount) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery1121() {
        if (!getDbName().equals("mysql")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2("bla", new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("ref2.ref3 as bla, sum(amount) as totl");
        query.setOrdering("totl descending");
        query.setGrouping("bla having sum(amount) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery1122() {
        if (!getDbName().equals("mysql")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2("bla", new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("ref2.ref3.ref3Val as bla, sum(amount) as total");
        query.setOrdering("total descending");
        query.setGrouping("bla having sum(amount) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery1113() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2(10, new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("ref2.ref3, sum(ref2.amount2) as total");
        query.setOrdering("total descending");
        query.setGrouping("ref2.ref3 having sum(ref2.amount2) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery1114() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Ref1 r1 = new Ref1(10, new Ref2(10, new Ref3("bla")));
            pm.makePersistent(r1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Ref1.class);
        query.setResult("amount, ref2.ref3, sum(ref2.amount2) as total");
        query.setOrdering("total descending, amount ascending");
        query.setGrouping("amount, ref2.ref3 having sum(ref2.amount2) > 1");

        Collection col = (Collection) query.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object[] oa = (Object[]) iterator.next();
            for (int i = 0; i < oa.length; i++) {
                Object o = oa[i];
                System.out.println("o = " + o);
            }
        }

        pm.currentTransaction().commit();
        pm.close();
    }


    public void testResultQuery1() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("avg(this.age)");
        Number avg = (Number)q.execute();
        Assert.assertEquals(5, avg.intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery111() {
    	if (!isSetResultSupported())
    		return;

        if (isMySQL3()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("this, count(*)");
        ((VersantQuery)q).setGrouping("this");

        List result = (List)q.execute();
        Assert.assertEquals(10, result.size());
        for (int i = 0; i < result.size(); i++) {
            Object[] row = (Object[])result.get(i);
            Person p = (Person)row[0];
            System.out.println("row[1] = " + row[1]);
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery2() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("count(*), avg(this.age), avg(age)");
        Object[] res = (Object[])q.execute();
        Assert.assertEquals(10, ((Long)res[0]).longValue());
        Assert.assertEquals(5, ((Number)res[1]).intValue());
        Assert.assertEquals(5, ((Number)res[2]).intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery3() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "count(this), avg(this.country.val), avg(age)");
        Object[] res = (Object[])q.execute();
        Assert.assertEquals(0, ((Long)res[0]).longValue());
        Assert.assertEquals(0, ((Number)res[1]).intValue());
        Assert.assertEquals(0, ((Number)res[2]).intValue());
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery4() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("count(*), avg(country.val), avg(age)");
        Object[] res = (Object[])q.execute();
        Assert.assertEquals(0, ((Long)res[0]).longValue());
        Assert.assertEquals(0, ((Number)res[1]).intValue());
        Assert.assertEquals(0, ((Number)res[2]).intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery41() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("max(name)");
        String res = (String)q.execute();
        System.out.println("res = '" + res + "'");
        Assert.assertEquals("name9", res.trim());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery5() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("name, count(*), avg(age)");
        ((VersantQuery)q).setGrouping("name");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Long)res[1]).longValue());
            System.out.println("res[2] = " + ((Number)res[2]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Informix does not support more that 2 colums in the orderby.
     */
    public void testResultQuery51() {
        String db = getDbName();
        if (!isJdbc() || db.equals("informixse") || isMySQL3()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("name, count(*), avg(age)");
        q.setOrdering("val ascending, name ascending, age descending");
        ((VersantQuery)q).setGrouping("name");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Long)res[1]).longValue());
            System.out.println("res[2] = " + ((Number)res[2]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery6() {
    	if (!isSetResultSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("name, count(*), avg(age)");
        ((VersantQuery)q).setGrouping("name having avg(age) >= 0");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Long)res[1]).longValue());
            System.out.println("res[2] = " + ((Number)res[2]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery7() {
    	if (!isSetResultSupported())
    		return;

        if (isMySQL3()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "name, (age - val) as bla, count(*), avg(age)");
        ((VersantQuery)q).setGrouping("name, age, val having avg(age) >= 0");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Integer)res[1]).intValue());
            System.out.println("res[2] = " + ((Long)res[2]).longValue());
            System.out.println("res[3] = " + ((Number)res[3]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery71() {
        if (isMySQL3()) {
            unsupported();
            return;
        }
        if (!getDbName().equals("mysql") && !getDbName().equals("postgres")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "name, (age - val) as bla, count(*), avg(age)");
        ((VersantQuery)q).setGrouping("name, bla having avg(age) >= 0");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Integer)res[1]).intValue());
            System.out.println("res[2] = " + ((Long)res[2]).longValue());
            System.out.println("res[3] = " + ((Number)res[3]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery72() {
        if (isMySQL3()) {
            unsupported();
            return;
        }
        if (!getDbName().equals("mysql") && !getDbName().equals("postgres")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "name, (age - val) as bla, count(*) as countStar, avg(age)");
        ((VersantQuery)q).setGrouping("name, bla having avg(age) >= 0");
        ((VersantQuery)q).setOrdering("countStar ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Integer)res[1]).intValue());
            System.out.println("res[2] = " + ((Long)res[2]).longValue());
            System.out.println("res[3] = " + ((Number)res[3]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery73() {
        if (isMySQL3()) {
            unsupported();
            return;
        }
        if (!getDbName().equals("mysql") && !getDbName().equals("postgres")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        ((VersantQuery)q).setResult(
                "name, (age - val) as bla, count(*) as countStar, avg(age)");
        ((VersantQuery)q).setGrouping("name, (age - val) having avg(age) >= 0");
        ((VersantQuery)q).setOrdering("countStar ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertTrue(((String)res[0]).startsWith("name"));
            System.out.println("res[1] = " + ((Integer)res[1]).intValue());
            System.out.println("res[2] = " + ((Long)res[2]).longValue());
            System.out.println("res[3] = " + ((Number)res[3]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery8() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            p.setCountry(new Country("" + i, "name" + i));
            col.add(p.getCountry());
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("this, country, (age - val) as bla");
        q.setOrdering("name ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertEquals(3, res.length);
            Assert.assertEquals("name" + i, ((Person)res[0]).getName());
            Assert.assertEquals("" + i, ((Country)res[1]).getCode());
            Assert.assertEquals((i + 1) - (i + 12),
                    ((Integer)res[2]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery9() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            p.setCountry(new Country("" + i, "name" + i));
            col.add(p.getCountry());
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult("country, age, this, (age - val) as bla");
        q.setOrdering("name ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertEquals(4, res.length);
            Assert.assertEquals("" + i, ((Country)res[0]).getCode());
            Assert.assertEquals(1 + i, ((Integer)res[1]).intValue());
            Assert.assertEquals("name" + i, ((Person)res[2]).getName());
            Assert.assertEquals((i + 1) - (i + 12),
                    ((Integer)res[3]).intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery10() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setIntWValue(new Integer(3));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("sum(intWValue - intValue)");
        Long intVal = (Long)q.execute();
        Assert.assertEquals(10, intVal.intValue());
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery11() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setLongWValue(new Long(5));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("sum(longWValue - intValue)");
        Long nVal = (Long)q.execute();
        Assert.assertEquals(30, nVal.intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery12() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setFloatWValue(new Float(5));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("sum(floatWValue - intValue)");
        Float nVal = (Float)q.execute();
        Assert.assertEquals(30, nVal.intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery121() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setFloatWValue(new Float(5));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("avg(floatWValue - intValue)");
        Object nVal = q.execute();
        System.out.println("nVal.getClass() = " + nVal.getClass());
        System.out.println("nVal = " + nVal);
//        Assert.assertEquals(30, nVal.intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery122() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setFloatWValue(new Float(5));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("avg(floatWValue)");
        Object nVal = q.execute();
        System.out.println("nVal.getClass() = " + nVal.getClass());
        System.out.println("nVal = " + nVal);
//        Assert.assertEquals(30, nVal.intValue());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery13() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i, null);
            p.setCountry(new Country("" + i, "name" + i));
            col.add(p.getCountry());
            col.add(p);
            p.setAge(i + 1);
            p.setVal(i + 12);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "this, country, age, this, (age - val) as bla");
        q.setOrdering("name ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertEquals(5, res.length);
            Assert.assertEquals("name" + i, ((Person)res[0]).getName());
            Assert.assertEquals("" + i, ((Country)res[1]).getCode());
            Assert.assertEquals(1 + i, ((Integer)res[2]).intValue());
            Assert.assertEquals("name" + i, ((Person)res[3]).getName());
            Assert.assertEquals((i + 1) - (i + 12),
                    ((Integer)res[4]).intValue());
        }
        pm.currentTransaction().commit();

        System.out.println(
                "\n\n\n\n\n\n\n\n\n *************************************************");
        pm.currentTransaction().begin();
        q = pm.newQuery(Person.class);
        ((VersantQuery)q).setResult(
                "this, country, age, this, (age - val) as bla");
        q.setOrdering("name ascending");
        results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        countExecQueryEvents();
        for (int i = 0; i < results.size(); i++) {
            Object[] res = (Object[])results.get(i);
            Assert.assertEquals(5, res.length);
            Assert.assertEquals("name" + i, ((Person)res[0]).getName());
            Assert.assertEquals("" + i, ((Country)res[1]).getCode());
            Assert.assertEquals(1 + i, ((Integer)res[2]).intValue());
            Assert.assertEquals("name" + i, ((Person)res[3]).getName());
            Assert.assertEquals((i + 1) - (i + 12),
                    ((Integer)res[4]).intValue());
        }

        LogEvent[] events = getEvents();
        Assert.assertEquals(0, countExecQueryEvents(events));
        Assert.assertEquals(0,
                countEventsOfType(events, ServerLogEvent.GET_STATE));
//        Assert.assertEquals(0, countExecQueryEvents());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testResultQuery14() {
    	if (!isSetResultSupported())
    		return;

        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            NumberTypes p = new NumberTypes();
            p.setOrd(i);
            p.setIntValue(2);
            p.setIntWValue(new Integer(3));
            col.add(p);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NumberTypes.class);
        ((VersantQuery)q).setResult("distinct this as NumberTypes");
        q.setOrdering("ord ascending");
        List results = (List)q.execute();
        Assert.assertEquals(10, results.size());
        for (int i = 0; i < results.size(); i++) {
            NumberTypes numberTypes = (NumberTypes)results.get(i);
            Assert.assertEquals(i, numberTypes.getOrd());
            Assert.assertEquals(2, numberTypes.getIntValue());
            Assert.assertEquals(3, numberTypes.getIntWValue().intValue());
        }

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOJ1() {
    	if (!isUnboundVariableSupported())
    		return;

        Collection col = new HashSet();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        KnowledgeObject param = new KnowledgeObject("hit"); //hit
        col.add(param);
        pm.makePersistent(param);

        KnowledgeObjectAncestors kAns = new KnowledgeObjectAncestors();
        kAns.setKnowledgeObject(param);                 //no hit
        pm.makePersistent(kAns);
        col.add(kAns);

        kAns = new KnowledgeObjectAncestors();
        kAns.setKnowledgeObject(param);
        kAns.getAncestors().add(new KnowledgeObject("nohit"));
        kAns.setParentSemantic(2);                      //no hit
        pm.makePersistent(kAns);
        col.add(kAns);
        col.addAll(kAns.getAncestors());

        kAns = new KnowledgeObjectAncestors();
        kAns.setKnowledgeObject(param);
        kAns.getAncestors().add(new KnowledgeObject("hit"));
        kAns.setParentSemantic(1);                      //hit
        pm.makePersistent(kAns);
        col.add(kAns);
        col.addAll(kAns.getAncestors());

        kAns = new KnowledgeObjectAncestors();
        kAns.setKnowledgeObject(new KnowledgeObject("nohit"));
        kAns.getAncestors().add(param);
        kAns.setParentSemantic(2);                      //nohit
        pm.makePersistent(kAns);
        col.add(kAns);
        col.add(kAns.getKnowledgeObject());

        kAns = new KnowledgeObjectAncestors();
        kAns.setKnowledgeObject(new KnowledgeObject("hit"));
        kAns.getAncestors().add(param);
        kAns.setParentSemantic(1);                      //hit
        pm.makePersistent(kAns);
        col.add(kAns);
        col.add(kAns.getKnowledgeObject());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(KnowledgeObject.class);
        q.declareParameters("KnowledgeObject userParamLevel2");
        q.declareVariables("KnowledgeObjectAncestors systemVarKnowledgeObjectAncestors2; " +
                "KnowledgeObjectAncestors systemVarKnowledgeObjectAncestors4");
        q.setFilter("userParamLevel2 == this " +
                "|| (systemVarKnowledgeObjectAncestors2.knowledgeObject ==  userParamLevel2" +
                "&& systemVarKnowledgeObjectAncestors2.ancestors.contains(this)" +
                "&& systemVarKnowledgeObjectAncestors2.parentSemantic == 1)" +
                "|| (systemVarKnowledgeObjectAncestors4.knowledgeObject ==  this" +
                "&& systemVarKnowledgeObjectAncestors4.ancestors.contains(userParamLevel2)" +
                "&& systemVarKnowledgeObjectAncestors4.parentSemantic == 1 )");

        List result = (List)q.execute(param);
        Assert.assertEquals(3, result.size());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test query with unbound var and contains(this.field). Forums 944.
     * Query: var.collection.contains(this.ref).
     */
    public void testUnboundVarContainsThisNav() throws Exception {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SupplierRegister r1 = new SupplierRegister("r1");
        Supplier r1Sup1 = new Supplier("r1Sup1");
        r1Sup1.setRegister(r1);
        Contact r1Con1 = new Contact("r1Con1", null);
        r1.getSuppliers().add(r1Sup1);
        r1Sup1.addContact(r1Con1);
        pm.makePersistent(r1);
        SupplierRef sref1 = new SupplierRef("sref1", r1Sup1);
        pm.makePersistent(sref1);
        Supplier sref2Sup = new Supplier("sref2Sup");
        SupplierRef sref2 = new SupplierRef("sref2", sref2Sup);
        pm.makePersistent(sref2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SupplierRef.class);
        q.declareVariables("SupplierRegister v");
        q.setFilter("v.suppliers.contains(this.supplier)");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == sref1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(r1Con1);
        pm.deletePersistent(r1Sup1);
        pm.deletePersistent(r1);
        pm.deletePersistent(sref1);
        pm.deletePersistent(sref2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check if the ignoreCache query option is set to match the PM if and
     * only if not specifically configured on the query.
     */
    public void testIgnoreCache() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a1 = new Address("a1");
        pm.makePersistent(a1);
        pm.currentTransaction().commit();

        // make sure flush is not done if the pm default is ignoreCache=true
        pm.setIgnoreCache(true);
        pm.currentTransaction().begin();
        Address a2 = new Address("a2");
        pm.makePersistent(a2);
        Query q = pm.newQuery(Address.class);
        q.setOrdering("street ascending");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        q.closeAll();
        pm.currentTransaction().commit();

        // make sure flush done if the pm default is ignoreCache=false
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Address a3 = new Address("a3");
        pm.makePersistent(a3);
        q = pm.newQuery(Address.class);
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(3, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        Assert.assertTrue(ans.get(1) == a2);
        Assert.assertTrue(ans.get(2) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        // set ignoreCache=false on q, true on the pm and create q2 from q
        // - q2 should end up with ignoreCache=false as well i.e. flush needed
        pm.setIgnoreCache(true);
        q.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Address a4 = new Address("a4");
        pm.makePersistent(a4);
        Query q2 = pm.newQuery(q);
        ans = (List)q2.execute();
        Assert.assertEquals(4, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        Assert.assertTrue(ans.get(1) == a2);
        Assert.assertTrue(ans.get(2) == a3);
        Assert.assertTrue(ans.get(3) == a4);
        q2.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(a1);
        pm.deletePersistent(a2);
        pm.deletePersistent(a3);
        pm.deletePersistent(a4);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test basic named query. This is the query from testOJQuery2
     * placed in the meta data.
     */
    public void testSimpleNamedQuery() {
    	if (!isUnboundVariableSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SupplierRegister r1 = new SupplierRegister("r1");
        Supplier r1Sup1 = new Supplier("r1Sup1");
        r1Sup1.setRegister(r1);
        Contact r1Con1 = new Contact("r1Con1", null);
        r1.getSuppliers().add(r1Sup1);
        r1Sup1.addContact(r1Con1);
        pm.makePersistent(r1);

        Contact c2 = new Contact("bla2", null);
        pm.makePersistent(c2);
        Contact c3 = new Contact("bla3", null);
        pm.makePersistent(c3);
        Contact c4 = new Contact("bla4", null);
        pm.makePersistent(c4);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = ((VersantPersistenceManager)pm).versantNewNamedQuery(
                Contact.class, "OliverJollyQuery2");
        List ans = (List)q.execute(r1);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == r1Con1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = ((VersantPersistenceManager)pm).versantNewNamedQuery(
                Contact.class, "OliverJollyQuery2");
        q.setFilter(null);
        q.declareParameters(null);
        ans = (List)q.execute();
        Assert.assertEquals(4, ans.size());
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        pm.deletePersistent(r1Con1);
        pm.deletePersistent(r1Sup1);
        pm.deletePersistent(r1);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the results of a query are not accessable after close
     * or closeAll.
     */
    public void testAccessQueryResultsAfterClose() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Contact con1 = new Contact("con1", new Address("con1addr"));
        pm.makePersistent(con1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Contact.class);

        // check closeAll
        Collection ans = (Collection)q.execute();
        q.closeAll();
        try {
            ans.iterator();
            throw new TestFailedException("expected JDOUserException");
        } catch (JDOUserException e) {
            // good
        }

        // make sure new results still work
        ans = (Collection)q.execute();
        ans.iterator();
        q.closeAll();

        // check close
        ans = (Collection)q.execute();
        q.close(ans);
        try {
            ans.iterator();
            throw new TestFailedException("expected JDOUserException");
        } catch (JDOUserException e) {
            // good
        }

        // make sure new results still work
        ans = (Collection)q.execute();
        ans.iterator();
        q.close(ans);

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(con1);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testMBn1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        TimeLine tlParam = new TimeLine();
        tlParam.setVal(val);
        pm.makePersistent(tlParam);

        /**
         * This must be a match for last and
         */
        Item itemParam = new Item();
        itemParam.getProps().setReleaseStatus(0);
        itemParam.getTimeLines().add(tlParam);
        itemParam.getTimeLines().add(tlParam);
        itemParam.getTimeLines().add(tlParam);
        pm.makePersistent(itemParam);


        /**
         * This must be a match for first and
         */
        Item item = new Item();
        item.getProps().setReleaseStatus(1);
        item.getTimeLines().add(tlParam);
        pm.makePersistent(item);

        /**
         * This must NOT be a match
         */
        Item item2 = new Item();
        item2.getProps().setReleaseStatus(0);
        item2.getTimeLines().add(tlParam);
        pm.makePersistent(item2);

        /**
         * This must NOT be a match
         */
        Item item3 = new Item();
        item3.getProps().setReleaseStatus(1);
        pm.makePersistent(item3);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Item.class);
        q.declareParameters("TimeLine tl, Item it0");
        q.setFilter(
                "timeLines.contains(tl) & " +
                "((props.releaseStatus == 1 & (this != it0)) | (props.releaseStatus == 0 & (this == it0)))");
        List result = (List)q.execute(tlParam, itemParam);
        Assert.assertEquals(2, result.size());
        pm.close();
    }

    /**
     * Test Olivier Jolly's query with unbound var and contains(this).
     */
    public void testOJQuery2() throws Exception {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SupplierRegister r1 = new SupplierRegister("r1");
        Supplier r1Sup1 = new Supplier("r1Sup1");
        r1Sup1.setRegister(r1);
        Contact r1Con1 = new Contact("r1Con1", null);
        r1.getSuppliers().add(r1Sup1);
        r1Sup1.addContact(r1Con1);
        pm.makePersistent(r1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Contact.class);
        q.declareVariables("Supplier v");
        q.declareParameters("SupplierRegister p");
        q.setFilter("v.register == p " +
                "&& v.contacts.contains(this) " +
                "&& v.name == 'r1Sup1'");

        List ans = (List)q.execute(r1);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == r1Con1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(r1Con1);
        pm.deletePersistent(r1Sup1);
        pm.deletePersistent(r1);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test Olivier Jolly's query with extra brackets.
     */
    public void testOJQuery() throws Exception {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SupplierRegister r1 = new SupplierRegister("r1");
        Supplier r1Sup1 = new Supplier("r1Sup1");
        r1Sup1.setRegister(r1);
        Contact r1Con1 = new Contact("r1Con1", null);
        r1.getSuppliers().add(r1Sup1);
        r1Sup1.addContact(r1Con1);
        pm.makePersistent(r1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SupplierRegister.class);
        q.declareVariables("Supplier v");
        q.declareParameters("Contact p");
        q.setFilter("(this.name == 'r1') " +
                "&& (v.register == this " +
                "    && v.contacts.contains(p) " +
                "    && v.name == 'r1Sup1')");

        List ans = (List)q.execute(r1Con1);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == r1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(r1Con1);
        pm.deletePersistent(r1Sup1);
        pm.deletePersistent(r1);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testUnboundVarQueryEviction() {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();

        pm.currentTransaction().begin();
        Address a1 = new Address("a1", s + "ct");
        Address a2 = new Address("a2", s + "ct");
        Address a3 = new Address("a3", s + "jhb");
        Address a4 = new Address("a4", s + "gotham");
        Industry i1 = new Industry(s + "ct");
        Industry i2 = new Industry(s + "jhb");
        Industry i3 = new Industry(s + "pe");
        Object[] all = new Object[]{a1, a2, a3, a4, i1, i2, i3};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        Query q;
        List ans;

        // no AndNode
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("i.code == city");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(3, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        Assert.assertTrue(ans.get(1) == a2);
        Assert.assertTrue(ans.get(2) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(pm.currentTransaction().getOptimistic());
        a1.setCity("ctt");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ans = (List)q.execute();
        System.out.println("\n\n\n\n -- ans = " + ans);
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == a2);
        Assert.assertTrue(ans.get(1) == a3);

        pm.deletePersistentAll(all);
        q.closeAll();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUnboundVarQueryEviction1() {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();

        pm.currentTransaction().begin();
        Address a1 = new Address("a1", s + "ct");
        Address a2 = new Address("a2", s + "ct");
        Address a3 = new Address("a3", s + "jhb");
        Address a4 = new Address("a4", s + "gotham");
        Industry i1 = new Industry(s + "ct");
        Industry i2 = new Industry(s + "jhb");
        Industry i3 = new Industry(s + "pe");
        Object[] all = new Object[]{a1, a2, a3, a4, i1, i2, i3};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        Query q;
        List ans;

        // no AndNode
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("i.code == city");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(3, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        Assert.assertTrue(ans.get(1) == a2);
        Assert.assertTrue(ans.get(2) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(pm.currentTransaction().getOptimistic());
        i1.setCode("ctt");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ans = (List)q.execute();
        System.out.println("\n\n\n\n -- ans = " + ans);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == a3);

        pm.deletePersistentAll(all);
        q.closeAll();
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test a not expression that fails without correct brackets.
     */
    public void testUnaryNotWithBrackets() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a1 = new Address("a1", "a1c");
        Address a2 = new Address("a2", "a2c");
        Address a3 = new Address("a3", "a3c");
        Object[] all = new Object[]{a1, a2, a3};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        q.setFilter("!(street == 'a1' || city == 'a2c')");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == a3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(all);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test simple queries with unbound variables.
     */
    public void testUnboundVariableSimple() throws Exception {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a1 = new Address("a1", "ct");
        Address a2 = new Address("a2", "ct");
        Address a3 = new Address("a3", "jhb");
        Address a4 = new Address("a4", "gotham");
        Industry i1 = new Industry("ct");
        Industry i2 = new Industry("jhb");
        Industry i3 = new Industry("pe");
        Object[] all = new Object[]{a1, a2, a3, a4, i1, i2, i3};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        Query q;
        List ans;

        // no AndNode
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("i.code == city");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(3, ans.size());
        Assert.assertTrue(ans.get(0) == a1);
        Assert.assertTrue(ans.get(1) == a2);
        Assert.assertTrue(ans.get(2) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        // AndNode with var exp first
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("i.code == city && street != 'a1'");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == a2);
        Assert.assertTrue(ans.get(1) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        // AndNode with var exp last
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("street != 'a1' && i.code == city");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == a2);
        Assert.assertTrue(ans.get(1) == a3);
        q.closeAll();
        pm.currentTransaction().commit();

        // AndNode with var exp in middle
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter("street != 'a1' && i.code == city && street != 'a3'");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == a2);
        q.closeAll();
        pm.currentTransaction().commit();

        // OrNode and AndNode with var exp in middle
        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class);
        q.declareVariables("Industry i");
        q.setFilter(
                "street != 'a1' && i.code == city && street != 'a3' || street == 'a4'");
        q.setOrdering("street ascending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == a2);
        Assert.assertTrue(ans.get(1) == a4);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(all);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains queries with unbound variables.
     */
    public void testUnboundVariableContains() throws Exception {
    	if (!isUnboundVariableSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();

        Supplier sup1 = new Supplier("sup1");
        Contact con1 = new Contact("david", null);
        Contact con2 = new Contact("jaco", null);
        sup1.addContact(con1);
        sup1.addContact(con2);

        Supplier sup2 = new Supplier("sup2");
        Contact con3 = new Contact("jaco", null);
        Contact con4 = new Contact("carl", null);
        sup2.addContact(con3);
        sup2.addContact(con4);

        Supplier sup3 = new Supplier("sup3");
        Contact con5 = new Contact("dirk", null);
        sup3.addContact(con5);

        Object[] all = new Object[]{
            sup1, con1, con2, sup2, con3, con4, sup3, con5};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        Query q;
        List ans;

        // unbound var used to link two Supplier trees
        pm.currentTransaction().begin();
        q = pm.newQuery(Supplier.class);
        q.declareVariables("Supplier sup1; Contact con1; Contact con2;");
        q.setFilter("contacts.contains(con1) " +
                "&& sup1.contacts.contains(con2) " +
                "&& con1.email == con2.email " +
                "&& this != sup1");
        q.setOrdering("name ascending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == sup1);
        Assert.assertTrue(ans.get(1) == sup2);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(all);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test complex queries with unbound variables.
     */
    public void testUnboundVariableComplex() throws Exception {

        // sapdb barfs if there are lots and lots of subqueries
        if (!isJdbc() || getDbName().equals("sapdb")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();

        SupplierRegister sr1 = new SupplierRegister("sr1");
        Supplier sup1 = new Supplier("sup1");
        sr1.getSuppliers().add(sup1);
        Contact con1 = new Contact("david", null);
        Contact con2 = new Contact("jaco", null);
        sup1.addContact(con1);
        sup1.addContact(con2);

        SupplierRegister sr2 = new SupplierRegister("sr2");
        Supplier sup2 = new Supplier("sup2");
        sr2.getSuppliers().add(sup2);
        Contact con3 = new Contact("jaco", null);
        Contact con4 = new Contact("carl", null);
        sup2.addContact(con3);
        sup2.addContact(con4);

        SupplierRegister sr3 = new SupplierRegister("sr3");
        Supplier sup3 = new Supplier("sup3");
        sr3.getSuppliers().add(sup3);
        Contact con5 = new Contact("dirk", null);
        sup3.addContact(con5);

        Object[] all = new Object[]{
            sr1, sup1, con1, con2, sr2, sup2, con3,
            con4, sr3, sup3, con5};
        pm.makePersistentAll(all);
        pm.currentTransaction().commit();

        Query q;
        List ans;

        // unbound var used to link two SupplierRegister trees
        pm.currentTransaction().begin();
        q = pm.newQuery(SupplierRegister.class);
        q.declareVariables("Supplier sup1; Supplier sup2; Contact con1; Contact con2; " +
                "SupplierRegister sr1");
        q.setFilter("suppliers.contains(sup1) " +
                "&& sup1.contacts.contains(con1) " +
                "&& sr1.suppliers.contains(sup2) " +
                "&& sup2.contacts.contains(con2) " +
                "&& con1.email == con2.email " +
                "&& this != sr1");
        q.setOrdering("name ascending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == sr1);
        Assert.assertTrue(ans.get(1) == sr2);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(all);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testOrderingNavigation() throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        
        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();

        pm.currentTransaction().begin();
        Address address = new Address("street" + s, "city" + s);
        Contact contact = new Contact(s + "email1", address);
        pm.makePersistent(contact);

        contact = new Contact(s + "email2", null);
        pm.makePersistent(contact);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Contact.class, "");
        q.setOrdering("address.street ascending, email descending");
        List result = (List)q.execute();
        Assert.assertEquals(2, result.size());

        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test a query with nested variable usage.
     */
    public void testNestedVarQuery() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SupplierRegister r1 = new SupplierRegister("r1");
        Supplier r1Sup1 = new Supplier("r1Sup1");
        Contact r1Con1 = new Contact("r1Con1", null);
        r1.getSuppliers().add(r1Sup1);
        r1Sup1.addContact(r1Con1);
        pm.makePersistent(r1);
        SupplierRegister r2 = new SupplierRegister("r2");
        Supplier r2Sup1 = new Supplier("r2Sup1");
        Contact r2Con1 = new Contact(null, null);
        r2.getSuppliers().add(r2Sup1);
        r1Sup1.addContact(r2Con1);
        pm.makePersistent(r2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SupplierRegister.class);
        q.declareVariables("Supplier s; Contact c");
        q.setFilter("suppliers.contains(s) " +
                "&& s.contacts.contains(c) " +
                "&& c.email != null");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == r1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(r1Con1);
        pm.deletePersistent(r1Sup1);
        pm.deletePersistent(r1);
        pm.deletePersistent(r2Con1);
        pm.deletePersistent(r2Sup1);
        pm.deletePersistent(r2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test using countStarOnSize with a query with select distinct e.g.
     * a var query with MySQL (forums 889, end of thread).
     */
    public void testCountStarOnSizeAndDistinct() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Supplier sup1 = new Supplier("sup1");
        Contact con1 = new Contact("con", null);
        sup1.addContact(con1);
        Contact con2 = new Contact("con", null);
        sup1.addContact(con2);
        pm.makePersistent(sup1);
        pm.currentTransaction().commit();

        // run a query that will bring back different results if distinct
        // is not used
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Supplier.class);
        q.declareVariables("Contact v");
        q.setFilter("contacts.contains(v) && v.email == 'con'");
        q.setCountStarOnSize(true);
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size()); // COUNT(*)
        Assert.assertEquals(1, ans.size()); // fetch all results
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(con1);
        pm.deletePersistent(con2);
        pm.deletePersistent(sup1);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test for MySQL "Cross dependency found in OUTER JOIN" problem
     * (forums 869).
     */
    public void testQueryCrossDependencyInOuterJoin() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Supplier sup1 = new Supplier("sup1");
        Contact con1 = new Contact("con1", new Address("con1addr"));
        sup1.addContact(con1);
        pm.makePersistent(sup1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Supplier.class,
                "contacts.contains(v) " +
                "&& v.email == 'con1' " +
                "&& v.address.street == 'con1addr' " +
                "&& name == 'sup1'");
        q.declareVariables("Contact v");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(sup1 == ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(Supplier.class,
                "name == 'sup1'" +
                "&& contacts.contains(v) " +
                "&& v.email == 'con1' " +
                "&& v.address.street == 'con1addr' ");
        q.declareVariables("Contact v");
        ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(sup1 == ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        // set v.email = v.address.street for next query
        pm.currentTransaction().begin();
        con1.setEmail(con1.getAddress().getStreet());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(Supplier.class,
                "name == 'sup1'" +
                "&& contacts.contains(v) " +
                "&& v.email == v.address.street");
        q.declareVariables("Contact v");
        ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(sup1 == ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(con1);
        pm.deletePersistent(sup1);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testVarQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query query = pm.newQuery(Option.class,
                "visibility.contains(type) && type == uType ");
        query.declareVariables("String type");
        query.declareParameters("String uType");
        try {
            Collection result = (Collection)query.execute(new String("all"));
        } catch (JDOUserException e) {
        }
        pm.close();
    }

    public void testQueryExecuteWithMap() {
        if (!isJdbc() || !getSubStoreInfo().isScrollableResultSetSupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Query q = pm.newQuery(AppIdClass.class, "pk > lower && pk < upper");

        q.declareParameters(
                "Integer lower, Integer upper, String jdoGenieOptions");

        Map parameters = new HashMap();
        parameters.put("lower", new Integer(5));
        parameters.put("upper", new Integer(25));
        parameters.put("jdoGenieOptions", "randomAccess=true");
        List l = (List)q.executeWithMap(parameters);

        // These work fine:
        // System.out.println(l.size());
        // System.out.println(l.get(10));
        // System.out.println(l.get(11));
        // System.out.println(l.get(12));

        // This line throws an Exception:
        Iterator i = l.iterator();
        i.hasNext();
        pm.close();
    }

    /**
     * Test retrieveAll on a query result (forums 871).
     */
    public void testRetrieveAllOnQueryResult() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create instances to query for
        pm.currentTransaction().begin();
        Contact con1 = new Contact("con1", null);
        Customer cust1 = new Customer("cust1");
        cust1.addContact(con1);
        Customer cust2 = new Customer("cust2");
        cust2.addContact(con1);
        pm.makePersistent(cust1);
        pm.makePersistent(cust2);
        pm.currentTransaction().commit();
        Object oidCon1 = pm.getObjectId(con1);
        Object oidCust1 = pm.getObjectId(cust1);
        Object oidCust2 = pm.getObjectId(cust2);

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Customer.class, "name == p");
        q.declareParameters("String p");
        String[] a = new String[]{"cust1", "cust2"};
        for (int i = 0; i < a.length; i++) {
            List ans = (List)q.execute(a[i]);
            pm.retrieveAll(ans);
            pm.makeTransientAll(ans);
            q.close(ans);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(pm.getObjectById(oidCon1, false));
        pm.deletePersistent(pm.getObjectById(oidCust1, false));
        pm.deletePersistent(pm.getObjectById(oidCust2, false));
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test containsKey on a map with a parameter.
     */
    public void testMapContainsKeyParam() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create instances with maps to query for
        pm.currentTransaction().begin();
        ClassWithMap con1 = new ClassWithMap("con1");
        con1.getMap().put("key1", "key1");
        con1.getMap().put("key2", "key1");
        pm.makePersistent(con1);
        ClassWithMap con2 = new ClassWithMap("con2");
        con2.getMap().put("key2", "key1");
        con2.getMap().put("key3", "key1");
        pm.makePersistent(con2);
        pm.currentTransaction().commit();

        // query for an instance with Address with street1
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithMap.class);
        q.declareParameters("String p");
        q.setFilter("map.containsKey(p)");
        List ans = (List)q.execute("key1");
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(con1, ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(con1);
        pm.deletePersistent(con2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test containsKey on a map with a variable (forums 888).
     */
    public void testMapContainsKeyVar() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create instances with maps to query for
        pm.currentTransaction().begin();
        Container con1 = new Container("con1");
        con1.getMap().put(new Address("street1"), "con1-street1");
        con1.getMap().put(new Address("street2"), "con1-street2");
        pm.makePersistent(con1);
        Container con2 = new Container("con2");
        con2.getMap().put(new Address("street2"), "con1-street2");
        con2.getMap().put(new Address("street3"), "con1-street3");
        pm.makePersistent(con2);
        pm.currentTransaction().commit();

        // query for an instance with Address with street1
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Container.class);
        q.declareVariables("Address v");
        q.declareParameters("String p");
        q.setFilter("map.containsKey(v) && v.street == p");
        List ans = (List)q.execute("street1");
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(con1, ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(con1);
        pm.deletePersistent(con2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a contains(c) && c.contains(p) type query (forums 837).
     */
    public void testContainsContains() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create a tree to query
        pm.currentTransaction().begin();
        Authorization auth = new Authorization("auth");
        LaborItem labItem = new LaborItem("labItem");
        Part part = new Part("part");
        auth.getLaborItems().add(labItem);
        labItem.getParts().add(part);
        pm.makePersistent(auth);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Authorization.class);
        q.declareParameters("Part p");
        q.declareVariables("LaborItem v");
        q.setFilter("laborItems.contains(v) && v.parts.contains(p)");
        List ans = (List)q.execute(part);
        Assert.assertTrue(ans.size() == 1);
        Assert.assertTrue(ans.get(0) == auth);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(auth);
        pm.deletePersistent(labItem);
        pm.deletePersistent(part);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a contains(var) query constrained by multiple and expressions
     * joined with or's.
     */
    public void testContainsWithAndOrAndOrAnd() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Customer.class);
        q.declareParameters("Address p1, Address p2, Address p3");
        q.declareVariables("Contact v1; Contact v2; Contact v3");
        q.setFilter(
                "contacts.contains(v1) && v1.email == \"C\" && v1.address == p1 " +
                "&& contacts.contains(v2) && v2.email == \"B\" && v2.address == p2 " +
                "&& contacts.contains(v3) && v3.email == \"A\" && v3.address == p3");
        ((Collection)q.execute(null, null, null)).iterator();
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that casting a hierarchy reference down to a subclass to access
     * subclass only fields and limit the search works.
     */
    public void testDowncastThroughVariable() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Two two = new Two(100);
        Three three = new Three(100);
        Four four = new Four(100);
        ColHierarchy col1 = new ColHierarchy("col1");
        col1.getList().add(two);
        ColHierarchy col2 = new ColHierarchy("col2");
        col2.getList().add(two);
        col2.getList().add(three);
        ColHierarchy col3 = new ColHierarchy("col3");
        col3.getList().add(two);
        col3.getList().add(three);
        col3.getList().add(four);
        pm.makePersistent(col1);
        pm.makePersistent(col2);
        pm.makePersistent(col3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        // this will fail to compile if access to fields of Four is not allowed
        Query q = pm.newQuery(ColHierarchy.class);
        q.declareVariables("Four v");
        q.declareParameters("String p");
        q.setFilter("list.contains(v) && v.fieldFour0 == p");
        List ans = (List)q.execute(four.getFieldFour0());
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == col3);
        q.closeAll();

        // this will being back col1, col2 and col3 if the class-id check is
        // not done properly
        q = pm.newQuery(ColHierarchy.class);
        q.declareVariables("Four v");
        q.declareParameters("String p");
        q.setFilter("list.contains(v) && v.fieldOne0 == p");
        ans = (List)q.execute(four.getFieldOne0());
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == col3);
        q.closeAll();

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(col1);
        pm.deletePersistent(col2);
        pm.deletePersistent(col3);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that casting a hierarchy reference down to a subclass to access
     * subclass only fields works.
     */
    public void testDowncast() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Two two = new Two(100);
        Three three = new Three(100);
        Four four = new Four(100);
        RefHierarchy ref2 = new RefHierarchy("ref2", two);
        RefHierarchy ref3 = new RefHierarchy("ref3", three);
        RefHierarchy ref4 = new RefHierarchy("ref4", four);
        pm.makePersistent(ref2);
        pm.makePersistent(ref3);
        pm.makePersistent(ref4);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        // this will fail to compile if the cast does not allow access to fields
        // of Four
        Query q = pm.newQuery(RefHierarchy.class,
                "((Four)refTwo).fieldFour0 == p");
        q.declareParameters("String p");
        List ans = (List)q.execute(four.getFieldFour0());
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == ref4);
        q.closeAll();

        // this will fail and bring back ref2, ref3 and ref4 if the cast does
        // not check the jdo_class column properly (IN list)
        q = pm.newQuery(RefHierarchy.class, "((Three)refTwo).fieldOne0 == p");
        q.declareParameters("String p");
        q.setOrdering("name ascending");
        ans = (List)q.execute(two.getFieldOne0());
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == ref3);
        Assert.assertTrue(ans.get(1) == ref4);
        q.closeAll();

        // this will fail and bring back ref2, ref3 and ref4 if the cast does
        // not check the jdo_class column properly (simple = check)
        q = pm.newQuery(RefHierarchy.class, "((Four)refTwo).fieldOne0 == p");
        q.declareParameters("String p");
        ans = (List)q.execute(two.getFieldOne0());
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == ref4);
        q.closeAll();

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(ref2.getRefTwo());
        pm.deletePersistent(ref3.getRefTwo());
        pm.deletePersistent(ref4.getRefTwo());
        pm.deletePersistent(ref2);
        pm.deletePersistent(ref3);
        pm.deletePersistent(ref4);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test that ordering by a primary key field works (forums 675).
     */
    public void testOrderByPkField() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        AppIdClass o = new AppIdClass(1, "hello");
        pm.makePersistent(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(AppIdClass.class, "name == p");
        q.declareParameters("String p");
        q.setOrdering("pk ascending");
        List ans = (List)q.execute("hello");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == o);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that 'byte == 2' query works (forums thread 665).
     */
    public void testByteEqualsConstant() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassWithByte c = new ClassWithByte((byte)2);
        pm.makePersistent(c);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithByte.class, "byteCol == 2");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == c);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(c);
        pm.currentTransaction().commit();

        pm.close();
    }

//    public static void main(String[] args) {
//        TestRunner r = new TestRunner();
//        r.doRun(QueryTests2.suite());
//    }

    /**
     * Perform a stress test executing the same query with different params
     * on many threads at once. This takes a long time even on MySQL.
     */
    public void testQueryStress() throws Exception {
        if (isRemote() || isDataSource()) {
            unsupported();
            return;
        }
        if (!getDbName().equals("mysql")) {
            unsupported();
            return;
        }

        // make a bunch of Holder's to query for
        PersistenceManager pm = pmf().getPersistenceManager();
        int numHolders = 1000;
        pm.currentTransaction().begin();
        for (int i = 0; i < numHolders; i++) {
            Holder c = new Holder("" + i,
                    new Address(i + " Some Street", "CT"));
            pm.makePersistent(c);
        }
        pm.currentTransaction().commit();
        pm.close();

        // increase size of con pool
        JDBCConnectionPool pool = getJdbcConnectionPool();
        int oldMax = pool.getMaxActive();
        pool.setMaxActive(50);

        // create Threads to query for them
        int numThreads = 5;
        QueryThread[] a = new QueryThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            a[i] = new QueryThread(i, numHolders, 10);
        }
        for (int i = 0; i < numThreads; i++) a[i].start();
        for (int i = 0; i < numThreads; i++) a[i].join();
        boolean bad = false;
        for (int i = 0; i < numThreads; i++) {
            if (a[i].getError() != null) {
                a[i].getError().printStackTrace(System.out);
                bad = true;
            }
        }
        if (bad) {
            throw new TestFailedException("bad");
        }

        // cleanup
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Holder.class);
        pm.deletePersistentAll((Collection)q.execute());
        q.closeAll();
        pm.currentTransaction().commit();
        pm.close();

        pool.setMaxActive(oldMax);
    }

    private class QueryThread extends Thread {

        private int numHolders;
        private int interations;
        private Random rnd;
        private Throwable error;

        public QueryThread(int id, int numHolders, int interations) {
            super("QueryThread" + id);
            this.numHolders = numHolders;
            this.interations = interations;
            rnd = new Random(id + 1);
        }

        public void run() {
            try {
                for (int i = 0; i < interations; i++) {
                    PersistenceManager pm = pmf().getPersistenceManager();
                    int c = rnd.nextInt(10) + 1;
                    for (int k = 0; k < c; k++) {
                        pm.currentTransaction().setNontransactionalRead(true);
                        pm.currentTransaction().setOptimistic(true);
                        Query q = pm.newQuery(Holder.class, "name > p");
                        q.declareParameters("String p");
                        int p = rnd.nextInt(numHolders);
                        Collection ans = (Collection)q.execute(
                                Integer.toString(p));
                        for (Iterator j = ans.iterator(); j.hasNext();) j.next();
                        q.closeAll();
                    }
                    pm.close();
                }
            } catch (Throwable e) {
                error = e;
            }
        }

        public Throwable getError() {
            return error;
        }
    }

    /**
     * Test keeping two ResultSet's open on one connection. This is for the
     * MySQL fetch size problem.
     */
    public void testTwoOpenResultSetsOneConnection() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        int n = 50;
        for (int i = 0; i < n; i++) pm.makePersistent(new Address("a" + i));
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** run two queries against Address with fetchSize set");

        pm.currentTransaction().begin();
        VersantQuery q1 = (VersantQuery)pm.newQuery(Address.class);
        q1.setMaxRows(20);
        q1.setFetchSize(10);
        Iterator i1 = ((Collection)q1.execute()).iterator();
        for (int i = 0; i < 5; i++) i1.next();

        VersantQuery q2 = (VersantQuery)pm.newQuery(Address.class);
        q2.setMaxRows(20);
        q2.setFetchSize(10);
        Iterator i2 = ((Collection)q2.execute()).iterator();
        for (int i = 0; i < 5; i++) i2.next();

        System.out.println("\n*** closing queries");
        q1.closeAll();
        q2.closeAll();
        pm.currentTransaction().commit();

        System.out.println("\n*** cleanup");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        pm.deletePersistentAll((Collection)q.execute());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure data prefetched with an outer join ends up in the level 2
     * cache or not when using all combinations of
     * transactional or non-transactional-reads, optimistic or datastore,
     * iterate over results or load all at once.
     */
    public void testQueryOuterFetchAndLevel2Cache() throws Exception {

        // no-transaction not-optimistic iterate
        testQueryOuterFetchAndLevel2Cache(false, false, false, true);

        // no-transaction not-optimistic fetch-all
        testQueryOuterFetchAndLevel2Cache(false, false, true, true);

        // no-transaction optimistic iterate
        testQueryOuterFetchAndLevel2Cache(false, true, false, true);

        // no-transaction optimistic fetch-all
        testQueryOuterFetchAndLevel2Cache(false, true, true, true);

        // transaction optimistic iterate
        testQueryOuterFetchAndLevel2Cache(true, true, false, true);

        // transaction optimistic all
        testQueryOuterFetchAndLevel2Cache(true, true, true, true);

        // transaction datastore iterate
        testQueryOuterFetchAndLevel2Cache(true, false, false, false);

        // transaction datastore all
        testQueryOuterFetchAndLevel2Cache(true, false, true, false);
    }

    private void testQueryOuterFetchAndLevel2Cache(boolean tx, boolean opt,
            boolean all, boolean inCache) throws Exception {
        System.out.println("\n\n*** QueryTests2.testQueryOuterFetchAndLevel2Cache " +
                "tx " + tx + " opt " + opt + " all " + all +
                " inCache " + inCache);
        if (!isRemote()) {
            // give the server time to take a metric snapshot so we can get the
            // size of the level 2 cache
            Thread.sleep(2000);
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setOptimistic(opt);

        // create an instance with ref to another instance in the dfg
        pm.currentTransaction().begin();
        Holder h = new Holder("holder", new Address("street", "city"));
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        // query it back and make sure both instances or no instances go
        // into level 2 cache
        if (tx) pm.currentTransaction().begin();
        int cacheSize = getLevel2CacheSize();
        Query q = pm.newQuery(Holder.class);
        Collection ans = (Collection)q.execute();
        if (all) {
            System.out.println("ans.size() = " + ans.size());
        } else {
            for (Iterator i = ans.iterator(); i.hasNext(); i.next()) ;
        }
        q.closeAll();
        Assert.assertEquals(cacheSize + (inCache ? 2 : 0),
                getLevel2CacheSize());
        if (tx) pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h.getAddress());
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a query with p = nav.field || p = nav2.field.
     */
    public void testNavEqualParamUnderOr() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Country c = new Country("ZA", "South Africa");
        Person p = new Person("someone", c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class,
                "p == country.code || p == country.code");
        q.declareParameters("String p");
        List ans = (List)q.execute("ZA");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(c);
        pm.deletePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a query with contains invoked on two different lists (forum 545).
     */
    public void testDoubleContains() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // create some stuff to query for
        pm.currentTransaction().begin();
        ListableElement e1 = new ListableElement("pe1", "e1");
        ListableElement e2 = new ListableElement("pe2", "e2");
        ListableElement e3 = new ListableElement("pe3", "e3");
        ListContainer c1 = new ListContainer("c1");
        c1.addA(e1);
        c1.addB(e2);
        c1.addB(e3);
        ListContainer c2 = new ListContainer("c2");
        c2.addA(e1);
        c2.addB(e3);
        pm.makePersistent(c1);
        pm.makePersistent(c2);
        pm.currentTransaction().commit();

        // run the query
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ListContainer.class);
        q.declareParameters("ListableElement p");
        q.setFilter("listA.contains(p) || listB.contains(p)");
        q.setOrdering("name ascending");
        List ans = (List)q.execute(e1);
        Assert.assertEquals(2, ans.size());
        Assert.assertEquals(c1, ans.get(0));
        Assert.assertEquals(c2, ans.get(1));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(e1);
        pm.deletePersistent(e2);
        pm.deletePersistent(e3);
        pm.deletePersistent(c1);
        pm.deletePersistent(c2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test for Alex's very hard to reproduce preCharIndexBug. This bug is
     * very sensitive to the order of primary keys in the classes in the
     * meta data i.e. tables.
     */
    public void testAlexPreCharIndexBug() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // create some stuff to query for
        pm.currentTransaction().begin();
        Style style = new Style((short)1, "SKUCODE", "styleShortCode");
        StockGroup stockGroup = new StockGroup((short)1, "01",
                "stockGroupName");
        StockIndex stockIndex = new StockIndex((short)1, "EANCODE", style,
                stockGroup);
        AlexBranch alexBranch = new AlexBranch((short)1, "01",
                "alexBranchShortName");
        StockLevel stockLevel = stockIndex.createStockLevel(alexBranch);
        pm.makePersistent(stockIndex);
        pm.currentTransaction().commit();

        // see that we can get it back
        pm.currentTransaction().begin();
        Query q = pm.newQuery();
        q.setClass(StockIndex.class);
        q.setFilter("this.compno == compno && " +
                "this.style.dumpFlag == dumpFlag && " +
                "this.stockGroup == stockGroup && " +
                "stockLevelList.contains(stockLevel) &&" +
                "stockLevel.branch == branch");
        q.declareVariables("StockLevel stockLevel");
        q.declareParameters(
                "short compno, boolean dumpFlag, StockGroup stockGroup, Branch branch");
        List ans = (List)q.executeWithArray(new Object[]{
            new Short((short)1), Boolean.FALSE,
            new StockGroup.ID("1-01"),
            new AlexBranch.ID("1-01")});
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == stockIndex);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(stockLevel);
        pm.deletePersistent(alexBranch);
        pm.deletePersistent(stockIndex);
        pm.deletePersistent(stockGroup);
        pm.deletePersistent(style);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Make sure pass 2 fields are correctly fetched in a tree relationship.
     */
    public void testTreeMapFetch() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make tree to test with
        pm.currentTransaction().begin();
        ArticleCategory root = new ArticleCategory();
        root.addChild(new ArticleCategory());
        root.addChild(new ArticleCategory());
        pm.makePersistent(root);
        Object rootOID = pm.getObjectId(root);
        pm.currentTransaction().commit();

        // get new pm
        pm.close();
        pm = pmf().getPersistenceManager();

        // bring tree back and check linkProperties fields are populated
        pm.currentTransaction().begin();
        System.out.println("\n*** lookup root by ID");
        root = (ArticleCategory)pm.getObjectById(rootOID, true);
        System.out.println("\n*** root.getLnkProperties()");
        Assert.assertTrue(root.getLnkProperties() != null);
        for (Iterator i = root.getLnkChildren().iterator(); i.hasNext();) {
            ArticleCategory c = (ArticleCategory)i.next();
            System.out.println("\n*** c.getLnkProperties()");
            Assert.assertTrue(c.getLnkProperties() != null);
        }
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(root.getLnkChildren());
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test that the correct parameter type is used when a parameter is in
     * an expression that makes it hard to get the type from a field.
     */
    public void testStartIntervalQuery() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make Product instances to query for
        pm.currentTransaction().begin();
        Product good = new Product(1000, 2);    // 1020
        Product bad = new Product(1000, 1);     // 1010
        pm.makePersistent(good);
        pm.makePersistent(bad);
        pm.currentTransaction().commit();

        // make sure the correct instance is returned
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Product.class, "interval * 10 + lastDown > now");
        q.declareParameters("Long now");
        List ans = (List)q.execute(new Long(1015));
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == good);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(good);
        pm.deletePersistent(bad);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test specifying jdoGenieOptions and using Query.execWithArray.
     */
    public void testOptionsParamAndExecWithArray() throws Exception {
    	if (!isSQLSupported()) // SQL
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        // create objects to query for
        pm.currentTransaction().begin();
        Address a1 = new Address("a1", "ct");
        Address a2 = new Address("a2", "ct");
        pm.makePersistent(a1);
        pm.makePersistent(a2);
        pm.currentTransaction().commit();

        // run the query with countStarOnSize and maxRows making sure that
        // both work
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "city == p");
        q.declareParameters("String jdoGenieOptions, String p");
        q.setOrdering("street ascending");
        Object[] pa = new Object[]{"countStarOnSize=true;maxRows=1", "ct"};
        List ans = (List)q.executeWithArray(pa);
        findExecQuerySQL(); // clear events
        Assert.assertEquals(2, ans.size()); // count(*) gives all results
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") >= 0);
        Assert.assertTrue(a1 == ans.get(0));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a1);
        pm.deletePersistent(a2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test the inline SQL extension.
     */
    public void testSql() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // create objects to query for
        pm.currentTransaction().begin();
        Address good = new Address("good");
        Address bad = new Address("bad");
        TriRef trgood = new TriRef(1, 2, 3, "trgood");
        TriRef trbad = new TriRef(4, 5, 6, "trbad");
        pm.makePersistent(good);
        pm.makePersistent(bad);
        pm.makePersistent(trgood);
        pm.makePersistent(trbad);
        pm.currentTransaction().commit();

        // look for it using sql
        pm.currentTransaction().begin();
        checkInlineSql(pm, good, "sql(\"street = 'good'\")");
        pm.currentTransaction().commit();

        // look for it using sql with column name replacement
        pm.currentTransaction().begin();
        checkInlineSql(pm, good, "street.sql(\"$1 = 'good'\")");
        checkInlineSql(pm, good, "street.sql(\"'good' = $1\")");
        pm.currentTransaction().commit();

        // check column name replacement for 3 columns
        pm.currentTransaction().begin();
        checkInlineSql(pm, trgood, "sql(\"$1 = 1 and $2 = 2 and $3 = 3\")");
        pm.currentTransaction().commit();

        // make sure the extra eviction classes stuff works
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        VersantQuery q = (VersantQuery)pm.newQuery(Address.class,
                "street.sql(\"$1 = 'good'\")");

        // check subclasses option works
        q.setEvictionClasses(new Class[]{One.class, TriRef.class}, true);
        Class[] ca = q.getEvictionClasses();
        Assert.assertTrue(ca.length == 5);
        Assert.assertTrue(ca[0] == One.class);
        Assert.assertTrue(ca[1] == Two.class);
        Assert.assertTrue(ca[2] == Three.class);
        Assert.assertTrue(ca[3] == Four.class);
        Assert.assertTrue(ca[4] == TriRef.class);

        // now set the class whose instance we will modify for the test
        q.setEvictionClasses(new Class[]{TriRef.class}, false);
        ca = q.getEvictionClasses();
        Assert.assertTrue(ca.length == 1);
        Assert.assertTrue(ca[0] == TriRef.class);

        // put the results in cache making sure query runs (i.e. event logging
        // is on)
        countExecQueryEvents();
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(good == ans.get(0));
        Assert.assertEquals(1, countExecQueryEvents());
        q.closeAll();
        pm.currentTransaction().commit();

        // re-exec and make sure cache is used
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        countExecQueryEvents();
        ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(0, countExecQueryEvents());
        q.closeAll();

        // modify a TriRef to evict the results
        trgood.setRef(null);
        pm.currentTransaction().commit();

        // re-exec and make sure cache is not used
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        countExecQueryEvents();
        ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(1, countExecQueryEvents());
        q.closeAll();

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(good);
        pm.deletePersistent(bad);
        pm.deletePersistent(trgood);
        pm.deletePersistent(trbad);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void checkInlineSql(PersistenceManager pm, Object good,
            String filter) {
        Query q = pm.newQuery(good.getClass(), filter);
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(good == ans.get(0));
        q.closeAll();
    }

    /**
     * Test 'this == p' query.
     */
    public void testThisEqualsParam() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // create objects to query for
        pm.currentTransaction().begin();
        Address good = new Address("good");
        Address bad = new Address("bad");
        TriRef trgood = new TriRef(1, 2, 3, "trgood");
        TriRef trbad = new TriRef(4, 5, 6, "trbad");
        pm.makePersistent(good);
        pm.makePersistent(bad);
        pm.makePersistent(trgood);
        pm.makePersistent(trbad);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "this == p");
        checkThisParam(q, good);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(Address.class, "p == this");
        checkThisParam(q, good);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(TriRef.class, "this == p");
        checkThisParam(q, trgood);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(TriRef.class, "p == this");
        checkThisParam(q, trgood);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(good);
        pm.deletePersistent(bad);
        pm.deletePersistent(trgood);
        pm.deletePersistent(trbad);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void checkThisParam(Query q, Object good) {
        q.declareParameters("Address p");
        List ans = (List)q.execute(good);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(good == ans.get(0));
        q.closeAll();
    }

    /**
     * Test the countOnSize query option for a normal query (i.e. not
     * randomAccess).
     */
    public void testQueryCountOnSize() throws Exception {
    	if (!isSQLSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        // create n addresses
        int n = 3;
        pm.currentTransaction().begin();
        Address[] aa = new Address[n];
        for (int i = 0; i < n; i++) {
            pm.makePersistent(aa[i] = new Address("street" + i, "city" + i));
        }
        pm.currentTransaction().commit();

        System.out.println("\n*** no countOnSize i.e. normal query");
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Address.class);
        List list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size());
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") < 0);
        for (int i = 0; i < n; i++) {
            Assert.assertNotNull(list.get(i));
        }
        q.closeAll();
        pm.currentTransaction().commit();

        System.out.println("\n*** without 'order by'");
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Address.class);
        q.setCountStarOnSize(true);
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size());
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") >= 0);
        Assert.assertEquals(n, list.size());
        for (int i = 0; i < n; i++) {
            Assert.assertNotNull(list.get(i));
        }
        q.closeAll();
        pm.currentTransaction().commit();

        System.out.println("\n*** with 'order by'");
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Address.class);
        q.setOrdering("street descending");
        q.setCountStarOnSize(true);
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size());
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") >= 0);
        Assert.assertEquals(n, list.size());
        for (int i = 0; i < n; i++) {
            Assert.assertTrue(list.get(i) == aa[n - i - 1]);
        }
        q.closeAll();
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** iterator instead of size() fetch and 'order by'");
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Address.class);
        q.setOrdering("street descending");
        q.setCountStarOnSize(true);
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size());
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") >= 0);
        int c = 0;
        for (Iterator i = list.iterator(); i.hasNext();) {
            Assert.assertTrue(i.next() == aa[n - c - 1]);
            ++c;
        }
        Assert.assertEquals(n, c);
        q.closeAll();
        pm.currentTransaction().commit();

        // The rest of these tests check that COUNT(*) results are correctly
        // cached
        System.out.println("\n*** starting count cache tests");

        pmf().evictAll();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        q = (VersantQuery)pm.newQuery(Address.class);
        q.setOrdering("street ascending");
        q.setCountStarOnSize(true);
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size()); // cache size
        Assert.assertTrue(findExecQuerySQL().indexOf("COUNT(") >= 0);
        q.closeAll();

        System.out.println("\n*** check size() is cached");
        q = (VersantQuery)pm.newQuery(Address.class);
        q.setOrdering("street ascending");
        q.setCountStarOnSize(true);
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size()); // get size from cache
        Assert.assertNull(findExecQuerySQL());
        list.size(); // resolve the data - this should replace the size in cache
        q.closeAll();

        System.out.println("\n*** check results have replaced size in cache");
        list = (List)q.execute();
        findExecQuerySQL(); // clear events
        Assert.assertEquals(n, list.size()); // get size from cache
        Assert.assertNull(findExecQuerySQL());
        list.size(); // get data from cache
        Assert.assertNull(findExecQuerySQL());
        q.closeAll();

        pm.currentTransaction().commit();

        System.out.println("\n*** cleanup");
        pm.currentTransaction().begin();
        pm.deletePersistentAll(aa);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Check that instance locking works. Either 'select for update' or an
     * 'update set pk=pk where pk=?' statement must be used to lock the
     * instance. This test does not work on Interbase or Firebird.
     */
    public void testInstanceLocking() throws Exception {
        testInstanceLocking(false);
    }

    /**
     * Check that instance locking works. Either 'select for update' or an
     * 'update set pk=pk where pk=?' statement must be used to lock the
     * instance. This test does not work on Interbase or Firebird.
     */
    public void testInstanceLockingWithRefresh() throws Exception {
        testInstanceLocking(true);
    }

    private void testInstanceLocking(boolean refresh) throws Exception {
        if (true) {
            broken();
            return;
        }
        String db = getDbName();
        if (!isJdbc() || db.equals("interbase") || db.equals("firebird")
                || db.equals("hypersonic") || db.equals("mysql")) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm1 =
                (VersantPersistenceManager)pmf().getPersistenceManager();
        VersantPersistenceManager pm2 =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // create an address
        pm1.currentTransaction().begin();
        Address a = new Address("piggy", "oinkville");
        pm1.makePersistent(a);
        Object oid = pm1.getObjectId(a);
        pm1.currentTransaction().commit();

        System.out.println("\n\n\n--- BEFORE LOCK ---\n\n\n");
        // lock it
        pm1.currentTransaction().begin();
        if (refresh) {
            pm1.refresh(a);
        } else {
            pm1.getObjectById(oid, true);
        }
        System.out.println("\n\n\n--- AFTER LOCK ---\n\n\n");


        // start another thread and try to access it
        // this thread will get stuck until we release the lock
        AccessThread at = new AccessThread(oid, pm2);
        at.start();
        Thread.sleep(200); // give it time to start
        Assert.assertTrue(at.isAlive());
        Assert.assertTrue(!at.isDone());

        pm1.currentTransaction().commit(); // release the lock
        Thread.sleep(200); // give it time to run
        Assert.assertTrue(!at.isAlive());
        Assert.assertTrue(at.isDone());

        // cleanup
        pm1.currentTransaction().begin();
        pm1.deletePersistent(a);
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    private static class AccessThread extends Thread {

        private Object oid;
        private PersistenceManager pm;
        private boolean done;

        public AccessThread(Object oid, PersistenceManager pm) {
            super("AccessThread" + oid);
            this.oid = oid;
            this.pm = pm;
        }

        public void run() {
            System.out.println("*** AccessThread.run()");
            pm.currentTransaction().begin();
            pm.getObjectById(oid, true);  // will get stuck here
            pm.currentTransaction().rollback();
            done = true;
            System.out.println("*** AccessThread done = true");
        }

        public boolean isDone() {
            return done;
        }

    }

    /**
     * Test instance locking by calling refresh after retrieving an instance
     * with a query. This found a bug with refresh not going back to the
     * database for datastore transactions if the instance was already in
     * the local server cache.
     */
    public void testInstanceLockingWithRefreshAfterQuery() throws Exception {
        if (true) {
            broken();
            return;
        }
        
        String db = getDbName();
        if (!isJdbc() || db.equals("interbase") || db.equals("firebird")
                || db.equals("hypersonic") || db.equals("mysql")) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm1 =
                (VersantPersistenceManager)pmf().getPersistenceManager();
        VersantPersistenceManager pm2 =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // create an address
        pm1.currentTransaction().begin();
        Address a = new Address("piggy", "oinkville");
        pm1.makePersistent(a);
        Object oid = pm1.getObjectId(a);
        pm1.currentTransaction().commit();

        // lock it after fetching it with q query
        pm1.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm1.currentTransaction().begin();

        Query q = pm1.newQuery(Address.class, "street == \"piggy\"");
        Collection ans = (Collection)q.execute();
        Object o = ans.iterator().next();
        q.closeAll();

        pm1.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_FIRST);
        System.out.println("\n*** pm1.refresh(o)");
        System.out.println("o = " + o);
        pm1.refresh(o);
        System.out.println("***\n");

        // start another thread and try to access it
        // this thread will get stuck until we release the lock
        AccessThread at = new AccessThread(oid, pm2);
        at.start();
        Thread.sleep(200); // give it time to start
        Assert.assertTrue(at.isAlive());
        Assert.assertTrue(!at.isDone());

        pm1.currentTransaction().commit(); // release the lock
        Thread.sleep(200); // give it time to run
        Assert.assertTrue(!at.isAlive());
        Assert.assertTrue(at.isDone());

        // cleanup
        pm1.currentTransaction().begin();
        pm1.deletePersistent(a);
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    /**
     * Make sure the correct SQL is executed depending on 'select for update'
     * or not for fetching the contents of a map.
     * This test is skipped for databases that do not have 'select for update'
     * e.g. Sybase.
     */
    public void testMapFetchSelectForUpdate() {
        if (true) {
            broken();
            return;
        }
        if (!isJdbc() || getSubStoreInfo().getSelectForUpdate() == null) {
            unsupported();
            return;
        }
        String forUpdateStr = new String(getSubStoreInfo().getSelectForUpdate());

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassWithMap cm = new ClassWithMap("piggy");
        cm.setData("oink", "oinker");
        pm.makePersistent(cm);
        Object oid = pm.getObjectId(cm);
        pm.currentTransaction().commit();

        // check 'select for update' used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        cm.getMap();
        String sql = findExecQuerySQL();
        System.out.println("sql = " + sql);
        Assert.assertTrue(sql.indexOf(forUpdateStr) >= 0);
        pm.currentTransaction().commit();

        // check 'select for update' can be disabled in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        findExecQuerySQL(); // clear events
        cm.getMap();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // check 'select for update' not used in opt tx
        pmf().evict(oid); // force a db fetch
        pm.currentTransaction().setOptimistic(true);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        cm.getMap();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(cm);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the correct SQL is executed depending on 'select for update'
     * or not for fetching the contents of an inverse fk collection.
     * This test is skipped for databases that do not have 'select for update'
     * e.g. Sybase.
     */
    public void testFKCollectionFetchSelectForUpdate() {
        if (true) {
            broken();
            return;
        }
        if (!isJdbc() || getSubStoreInfo().getSelectForUpdate() == null) {
            unsupported();
            return;
        }
        String forUpdateStr = new String(getSubStoreInfo().getSelectForUpdate());

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Master m = new Master("piggy");
        m.addDetail(new Detail("oink"));
        pm.makePersistent(m);
        Object oid = pm.getObjectId(m);
        pm.currentTransaction().commit();

        // check 'select for update' used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        m.getDetails();
        String sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) >= 0);
        pm.currentTransaction().commit();

        // check 'select for update' can be disabled in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        findExecQuerySQL(); // clear events
        m.getDetails();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // check 'select for update' not used in opt tx
        pmf().evict(oid); // force a db fetch
        pm.currentTransaction().setOptimistic(true);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        m.getDetails();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(m);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the correct SQL is executed depending on 'select for update'
     * or not for fetching the contents of a collection mapped to a link table.
     * This test is skipped for databases that do not have 'select for update'
     * e.g. Sybase.
     */
    public void testLinkCollectionFetchSelectForUpdate() {
        if (true) {
            broken();
            return;
        }
        if (!isJdbc() || getSubStoreInfo().getSelectForUpdate() == null) {
            unsupported();
            return;
        }
        String forUpdateStr = new String(getSubStoreInfo().getSelectForUpdate());

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("oinker");
        cust.addContact(
                new Contact("piggy@oinker.com", new Address("somestreet")));
        pm.makePersistent(cust);
        Object oid = pm.getObjectId(cust);
        pm.currentTransaction().commit();

        // check 'select for update' used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        cust.getContacts();
        String sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) >= 0);
        pm.currentTransaction().commit();

        // check 'select for update' can be disabled in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        findExecQuerySQL(); // clear events
        cust.getContacts();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // check 'select for update' not used in opt tx
        pmf().evict(oid); // force a db fetch
        pm.currentTransaction().setOptimistic(true);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        cust.getContacts();
        sql = findExecQuerySQL();
        Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(cust.getContacts());
        pm.deletePersistent(cust);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the correct SQL is executed depending on the
     * datastoreTxLocking mode for getObjectById.
     */
    public void testGetObjectByIdLocking() {
    	if (!isSQLSupported())
    		return;
    	
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("piggy", "oinkville");
        pm.makePersistent(a);
        Object oid = pm.getObjectId(a);
        Address a2 = new Address("piggy2", "oinkville2");
        pm.makePersistent(a2);
        Object oid2 = pm.getObjectId(a2);
        pm.currentTransaction().commit();

        // check default (i.e. FIRST) works used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        pm.getObjectById(oid, true);
        Assert.assertTrue(foundLockSQL());
        pm.getObjectById(oid2, true);
        Assert.assertTrue(!foundLockSQL());
        pm.currentTransaction().commit();

        // check ALL works used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        pm.getObjectById(oid, true);
        Assert.assertTrue(foundLockSQL());
        pm.getObjectById(oid2, true);
        Assert.assertTrue(foundLockSQL());
        pm.currentTransaction().commit();

        // check FIRST works used in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_FIRST);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        pm.getObjectById(oid, true);
        Assert.assertTrue(foundLockSQL());
        pm.getObjectById(oid2, true);
        Assert.assertTrue(!foundLockSQL());
        pm.currentTransaction().commit();

        // check NONE works in datastore tx
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        findExecQuerySQL(); // clear events
        pm.getObjectById(oid, true);
        Assert.assertTrue(!foundLockSQL());
        pm.currentTransaction().commit();

        // check ALL ignored in opt tx
        pmf().evict(oid); // force a db fetch
        pm.currentTransaction().setOptimistic(true);
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
        pm.currentTransaction().begin();
        findExecQuerySQL(); // clear events
        pm.getObjectById(oid, true);
        Assert.assertTrue(!foundLockSQL());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(a2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the query text is correctly updated depending on
     * selectForUpdate or not. This test is skipped for databases that do not
     * have 'select for update' e.g. Sybase.
     */
    public void testQuerySelectForUpdate() {
        if (!isJdbc() || getSubStoreInfo().getSelectForUpdate() == null) {
            unsupported();
            return;
        }

        String forUpdateStr = new String(getSubStoreInfo().getSelectForUpdate());

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("piggy", "oinkville");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        Query q = pm.newQuery(Address.class);

        int n = 4;
        for (int i = 0; i < n; i++) {
            boolean opt = i % 2 == 0;
            pm.currentTransaction().setOptimistic(opt);
            pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_ALL);
            System.out.println("\n*** optimistic " + opt);
            pm.currentTransaction().begin();

            findExecQuerySQL(); // clear events
            ((Collection)q.execute()).iterator().hasNext();
            String sql = findExecQuerySQL();
            q.closeAll();

            if (opt) {
                Assert.assertTrue(sql == null || sql.indexOf(forUpdateStr) < 0);
            } else {
                Assert.assertTrue(sql.indexOf(forUpdateStr) >= 0);
            }

            System.out.println("\n\n\n\n\n\n*** selectForUpdate " + opt);
            pm.setDatastoreTxLocking(opt
                    ? VersantPersistenceManager.LOCKING_ALL
                    : VersantPersistenceManager.LOCKING_NONE);

            findExecQuerySQL(); // clear events
            ((Collection)q.execute()).iterator().hasNext();
            sql = findExecQuerySQL();
            System.out.println("sql = " + sql);
            q.closeAll();

            if (opt) {  // make sure data came from level 2 cache
                assertNull(sql);
            } else {    // make sure no for update was done
                Assert.assertTrue(sql.indexOf(forUpdateStr) < 0);
            }

            pm.currentTransaction().commit();
            System.out.println();
        }

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure query statements and result sets are cleaned up properly
     * after a size() call on the collection.
     */
    public void testQueryCleanupAfterSize() throws Exception {
        if (true) {
            broken();
            return;
        }
        if (!isJdbc() || isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        int[] types = new int[]{
            JdbcLogEvent.RS_CLOSE,
            getSubStoreInfo().isPreparedStatementPoolingOK()
                ? JdbcLogEvent.PSPOOL_RELEASE
                : JdbcLogEvent.STAT_CLOSE
        };

        // must log all events for this test
        LogEventStore pes = getPerfEventStore();
        String ll = pes.getLogEvents();
        pes.setLogEvents(LogEventStore.LOG_EVENTS_ALL);

        pm.currentTransaction().begin();
        int n = 100;
        for (int i = 0; i < n; i++) {
            pm.makePersistent(new Address("street" + i, "city" + i));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        Collection ans = (Collection)q.execute();

        countJdbcEvents(types); // discard events

        ArrayList a = new ArrayList(ans); // calls ans.size()

        int[] c = countJdbcEvents(types);
        Assert.assertEquals(1, c[0]);   // rs.close
        Assert.assertEquals(1, c[1]);   // ps.release or stat.close

        pm.deletePersistentAll(a);
        pm.currentTransaction().commit();

        pm.close();

        pes.setLogEvents(ll);
    }

    /**
     * For the query bug submitted by Jean Calvelli
     * jean.calvelli@daumas-autheman.com.
     */
    public void testTimeIntervalQueryNullLiteral() throws Exception {
        testTimeIntervalQueryImp(true, false);
    }

    /**
     * Check that non-tx queries work.
     */
    public void testTimeIntervalQueryNullLiteralNoTx() throws Exception {
        testTimeIntervalQueryImp(true, true);
    }

    /**
     * For the query bug submitted by Jean Calvelli
     * jean.calvelli@daumas-autheman.com.
     */
    public void testTimeIntervalQuery() throws Exception {
        testTimeIntervalQueryImp(false, false);
    }

    private void testTimeIntervalQueryImp(boolean nullok, boolean noTx)
            throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create some TimeInterval's with different mixes of null/not-null
        // start and end dates
        pm.currentTransaction().begin();
        String[] a = new String[]{
            "030510", "030710", // in
            "030511", "030521", // end date is out of range
            "030512", null, // in
            null, "030713", // in
            "030414", "030710", // start date is out of range
            null, null, // in
            null, "030521", // end date is out of range
            "030414", null, // start date is out of range
        };
        for (int i = 0; i < a.length;) {
            pm.makePersistent(new TimeInterval(i + 1, a[i++], a[i++]));
        }
        pm.currentTransaction().commit();

        // run the query
        if (noTx) {
            pm.currentTransaction().setNontransactionalRead(true);
            pm.currentTransaction().setOptimistic(true);
        } else {
            pm.currentTransaction().begin();
        }

        Query q = pm.newQuery(TimeInterval.class);
        q.declareImports("import java.util.Date;");
        q.setOrdering("age ascending");

        Date startMin = TimeInterval.parseDate("030501");
        Date startMax = TimeInterval.parseDate("030531");
        Date endMin = TimeInterval.parseDate("030630");
        ArrayList ans;

        if (nullok) {
            q.setFilter("(start == null || ( start >= startMin && start <= startMax)) " +
                    " && (end == null || end >= endMin)");
            q.declareParameters("Date startMin, Date startMax, Date endMin");
            ans = new ArrayList(
                    (Collection)q.execute(startMin, startMax, endMin));
        } else {
            q.setFilter("(start == prmNull || ( start >= startMin && start <= startMax)) " +
                    " && (end == prmNull || end >= endMin)");
            q.declareParameters(
                    "Object prmNull, Date startMin, Date startMax, Date endMin");
            ans = new ArrayList((Collection)q.executeWithArray(new Object[]{
                null, startMin, startMax, endMin,
            }));
        }
        q.closeAll();

        Assert.assertEquals(
                "[030510-030710, 030512-null, null-030713, null-null]",
                ans.toString());
        if (!noTx) pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        q = pm.newQuery(TimeInterval.class);
        pm.deletePersistentAll((Collection)q.execute());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test query caching for a query using a contains(var) expression. Each
     * class used in the query in some way is modified to make sure the
     * query is evicted as expected. This is done with the mods being made
     * in a different tx to the query.
     */
    public void testQCacheDiffTx() {
        testQCacheImp(true);
    }

    /**
     * Test query caching for a query using a contains(var) expression. Each
     * class used in the query in some way is modified to make sure the
     * query is evicted as expected. This is done with the mods being made
     * in the same tx as the query.
     */
    public void testQCacheSameTx() {
        testQCacheImp(false);
    }

    private void testQCacheImp(boolean diffTx) {
//        if (!Utils.cacheEnabled()) return;

    	if (!isSQLSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.setIgnoreCache(false);

        pm.currentTransaction().begin();
        Customer cust1, cust2, cust3;
        Contact con1, con2, con3;
        pm.makePersistent(
                con1 = new Contact("con1", new Address("street1", "ct")));
        pm.makePersistent(
                con2 = new Contact("con2", new Address("street2", "ct")));
        pm.makePersistent(
                con3 = new Contact("con3", new Address("street1", "pe")));
        pm.makePersistent(cust1 = new Customer("cust1"));
        cust1.addContact(con1);
        cust1.addContact(con2);
        pm.makePersistent(cust2 = new Customer("cust2"));
        cust2.addContact(con2);
        cust2.addContact(con3);
        pm.makePersistent(cust3 = new Customer("cust3"));
        cust3.addContact(con3);
        pm.currentTransaction().commit();

        System.out.println("\n*** Check query is cached");
        checkQCacheVariableQ(pm, "ct", "[cust1, cust2]");

        System.out.println("\n*** Modify candidate class");
        pm.currentTransaction().begin();
        cust1.setName(cust1.getName() + "a");
        if (diffTx) pm.currentTransaction().commit();
        checkQCacheVariableQ(pm, "ct", "[cust1a, cust2]");

        System.out.println("\n*** Modify variable class");
        pm.currentTransaction().begin();
        con1.setEmail("someone@somewhere.com");
        if (diffTx) pm.currentTransaction().commit();
        checkQCacheVariableQ(pm, "ct", "[cust1a, cust2]");

        System.out.println("\n*** Change parameter value");
        checkQCacheVariableQ(pm, "pe", "[cust2, cust3]");

        System.out.println("\n*** Modify class referenced by variable");
        pm.currentTransaction().begin();
        con2.getAddress().setCity("pe");
        if (diffTx) pm.currentTransaction().commit();
        checkQCacheVariableQ(pm, "pe", "[cust1a, cust2, cust3]");

        System.out.println("\n*** Modify class referenced in ordering");
        pm.currentTransaction().begin();
        cust2.getIndustry().setCode("A" + cust2.getIndustry().getCode());
        if (diffTx) pm.currentTransaction().commit();
        checkQCacheVariableQ(pm, "pe", "[cust2, cust1a, cust3]");

        System.out.println("\n*** Delete a candidate class");
        pm.currentTransaction().begin();
        pm.deletePersistent(cust3);
        if (diffTx) pm.currentTransaction().commit();
        checkQCacheVariableQ(pm, "pe", "[cust2, cust1a]");

        System.out.println("\n*** Cleanup");
        pm.currentTransaction().begin();
        pm.deletePersistent(cust1);
        pm.deletePersistent(cust2);
        pm.deletePersistent(con1);
        pm.deletePersistent(con2);
        pm.deletePersistent(con3);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Exec the test query twice. The first exec must NOT come out of the
     * cache but the second must. The expected results are checked. This
     * will start a tx if required and always commits.
     */
    private void checkQCacheVariableQ(PersistenceManager pm, String city,
            String expected) {

        // clear any legacy events
        countExecQueryEvents();

        // first exec
        boolean diffTx = !pm.currentTransaction().isActive();
        if (diffTx) pm.currentTransaction().begin();
        Assert.assertEquals(expected, execQCacheVariableQ(pm, city).toString());
        pm.currentTransaction().commit();

        // expect 1 query
        Assert.assertEquals(1, countExecQueryEvents());

        // second exec must come from cache unless this is same tx
        // (i.e. there has been a flush so cache cannot be used)
        pm.currentTransaction().begin();
        Assert.assertEquals(expected, execQCacheVariableQ(pm, city).toString());
        pm.currentTransaction().commit();

        // expect no query if diffTx
        Assert.assertEquals(diffTx ? 0 : 1, countExecQueryEvents());
    }

    private ArrayList execQCacheVariableQ(PersistenceManager pm, String city) {
        Query q = pm.newQuery(Customer.class);
        q.setFilter("contacts.contains(v) && v.address.city == p");
        q.declareParameters("String p");
        q.declareVariables("Contact v");
        q.setOrdering("industry.code ascending");
        ArrayList a = new ArrayList((Collection)q.execute(city));
        q.closeAll();
        return a;
    }

    /**
     * Make sure the query cache is used when doing non-transactional reads
     * and iterating over the results one at a time.
     */
    public void testQCacheNonTxReadIterate() throws Exception {
//        if (!Utils.cacheEnabled()) return;
        testQCacheNonTxReadImp(true);
    }

    /**
     * Make sure the query cache is used when doing non-transactional reads.
     */
    public void testQCacheNonTxRead() throws Exception {
//        if (!Utils.cacheEnabled()) return;
        testQCacheNonTxReadImp(false);
    }

    private void testQCacheNonTxReadImp(boolean iterate) throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalRead(true);

        // create some Address'es
        pm.currentTransaction().begin();
        pm.makePersistent(new Address("a", "a"));
        pm.makePersistent(new Address("b", "b"));
        pm.currentTransaction().commit();

        // get the back with query outside of tx to load query cache
        checkAddresses(pm, new String[]{"a", "b"}, iterate);

        // repeat query and make sure no SQL is run i.e. results are in cache
        countExecQueryEvents();
        checkAddresses(pm, new String[]{"a", "b"}, iterate);
        Assert.assertEquals(0, countExecQueryEvents());

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(
                checkAddresses(pm, new String[]{"a", "b"}, iterate));
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the query cache is properly evicted after an insert of a
     * new instance.
     */
    public void testQCacheForInsert() throws Exception {
//        if (!Utils.cacheEnabled()) return;

    	if (!isSQLSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        // create some Address'es
        pm.currentTransaction().begin();
        pm.makePersistent(new Address("a", "a"));
        pm.makePersistent(new Address("b", "b"));
        pm.currentTransaction().commit();

        // get the back with query to load query cache
        pm.currentTransaction().begin();
        checkAddresses(pm, new String[]{"a", "b"}, false);
        pm.currentTransaction().commit();

        // repeat query and make sure no SQL is run i.e. results are in cache
        pm.currentTransaction().begin();
        countExecQueryEvents();
        checkAddresses(pm, new String[]{"a", "b"}, false);
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();

        // get a new PM to emulate a web app doing this
        pm.close();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        // insert a new Address
        pm.currentTransaction().begin();
        pm.makePersistent(new Address("c", "c"));
        pm.currentTransaction().commit();

        // repeat query and make sure results are correct and cache was not used
        // and then nuke all Address'es
        pm.currentTransaction().begin();
        countExecQueryEvents();
        pm.deletePersistentAll(
                checkAddresses(pm, new String[]{"a", "b", "c"}, false));
        Assert.assertEquals(1, countExecQueryEvents());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Get all the Address'es using a query in street order and make sure
     * the streets match the array given.
     */
    private ArrayList checkAddresses(PersistenceManager pm, String[] streets,
            boolean iterate) {
        Query q = pm.newQuery(Address.class);
        q.setOrdering("street ascending");
        Collection ans = (Collection)q.execute();
        ArrayList a;
        if (iterate) {
            a = new ArrayList();
            for (Iterator i = ans.iterator(); i.hasNext();) a.add(i.next());
        } else {
            a = new ArrayList(ans);
        }
        q.closeAll();
        int n = streets.length;
        Assert.assertEquals(n, a.size());
        for (int i = 0; i < n; i++) {
            Assert.assertEquals(streets[i], ((Address)a.get(i)).getStreet());
        }
        return a;
    }

}
