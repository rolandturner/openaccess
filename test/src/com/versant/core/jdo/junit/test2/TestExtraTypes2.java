
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

import javax.jdo.PersistenceManager;
import java.io.File;
import java.net.URL;
import java.sql.Timestamp;

/**
 * Tests for custom types.
 */
public class TestExtraTypes2 extends VersantTestCase {

    /**
     * Test custom types.
     */
    public void testCustomTypes() throws Exception {
        String db = getDbName();
        if (!isJdbc() || db.equals("informixse") || db.equals("db2")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // make an instance
        pm.currentTransaction().begin();
        CustomTypes o = new CustomTypes();
        CustomStringType stringType = new CustomStringType("stringType");
        o.setStringType(stringType);
        CustomStringType2 stringType2 = new CustomStringType2("stringType2");
        o.setStringType2(stringType2);
        CustomStringType stringTypeClob = new CustomStringType(
                "stringTypeClob");
        o.setStringTypeClob(stringTypeClob);
        CustomBytesType bytesType = new CustomBytesType(
                "hello".getBytes("UTF8"));
        o.setBytesType(bytesType);
        CustomBytesType bytesTypeBin = new CustomBytesType(
                "helloBin".getBytes("UTF8"));
        o.setBytesTypeBin(bytesTypeBin);
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check it comes back ok
        pm.currentTransaction().begin();
        o = (CustomTypes)pm.getObjectById(oid, true);
        Assert.assertEquals(stringType, o.getStringType());
        Assert.assertEquals(stringType2, o.getStringType2());
        Assert.assertEquals(stringTypeClob, o.getStringTypeClob());
        Assert.assertEquals(bytesType, o.getBytesType());
        Assert.assertEquals(bytesTypeBin, o.getBytesTypeBin());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test extra simple types.
     */
    public void testSimpleTypes() throws Exception {
    	if (!isExtraJavaTypeSupported()) // not supported File type
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        // make an instance
        pm.currentTransaction().begin();
        ExtraSimpleTypes o = new ExtraSimpleTypes();
        File file = new File("/etc/passwd");
        o.setFile(file);
        URL url = new URL("http://www.jdogenie.com");
        o.setUrl(url);
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check it comes back ok
        pm.currentTransaction().begin();
        o = (ExtraSimpleTypes)pm.getObjectById(oid, true);
        Assert.assertEquals(file, o.getFile());
        Assert.assertEquals(url, o.getUrl());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test extra simple mutable types.
     */
    public void testSimpleMutableTypes() throws Exception {
    	if (!isExtraJavaTypeSupported()) { // java.sql.Timestemp
    		return;
   		}
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        // make an instance
        pm.currentTransaction().begin();
        ExtraSimpleMutableTypes o = new ExtraSimpleMutableTypes();
        long now = System.currentTimeMillis();
        o.setTimestamp(new Timestamp(now));
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check it comes back ok
        pm.currentTransaction().begin();
        o = (ExtraSimpleMutableTypes)pm.getObjectById(oid, true);
        Timestamp ts = o.getTimestamp();
        long diff = Math.abs(ts.getTime() - now);
        Assert.assertTrue(diff < 1000);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

}
