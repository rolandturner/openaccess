
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

/**
 * Creates a TestSuite containing all tests in this package.
 */
public class AllTests extends VersantTestCase {

    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTest(TestRollback.suite());
        s.addTest(TestMakePersistent.suite());
        s.addTest(TestMakeTransactional.suite());
        s.addTest(TestMakeTransient.suite());
        s.addTest(TestGeneral.suite());
        if (VersantTestCase.isJDK14orNewer()) {
            s.addTestSuite(TestJavaxBeans.class);
        }
        s.addTest(TestManagedRelationships.suite());
        s.addTest(JDOCollectionTests.suite());
        s.addTest(QueryTests.suite());
        s.addTest(TestDeletes.suite());
        s.addTest(TestInheritence.suite());
        s.addTest(TestTypes.suite());
        s.addTest(TestConcurrentUsage.suite());
        s.addTest(TestApplicationPK.suite());
        s.addTest(TestSingleIdentity.suite());
        s.addTest(TestEviction.suite());
        s.addTest(TestRetrieve.suite());
        s.addTest(TestPNonTX.suite());
        s.addTest(TestRefresh.suite());
        s.addTest(TestPNew.suite());
        s.addTest(TestPClean.suite());
        s.addTest(TestMaps.suite());
        s.addTest(TestGetObjectById.suite());
        s.addTest(TestInstanceCallBacks.suite());
        s.addTest(TestLifecycleEvents.suite());
        s.addTest(TestLists.suite());
        s.addTest(TestPmfEviction.suite());
        s.addTest(TestLocalCache.suite());
        s.addTest(TestExceptionHandling.suite());
        s.addTest(TestArrays.suite());
        s.addTest(TestLevel2Cache.suite());
        s.addTest(TestPmCacheClean.suite());
        s.addTestSuite(TestL2cache.class);
        return s;
    }

}

