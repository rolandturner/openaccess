
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

import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.junit.VersantTestCase;

/**
 * Creates a TestSuite containing all tests in this package.
 */
public class AllTests extends VersantTestCase {

    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(TestManagedRelationships2.class);
        s.addTestSuite(QueryTests2.class);
        s.addTestSuite(TestApplicationPK2.class);
        s.addTestSuite(TestGeneral2.class);
        s.addTest(TestPsCache.suite());
        s.addTestSuite(TestInheritence2.class);
        s.addTestSuite(TestCollections2.class);
        s.addTestSuite(TestExtraTypes2.class);
        s.addTestSuite(TestPolyRef2.class);
        s.addTestSuite(TestEnhancer2.class);
        s.addTestSuite(TestUnmanagedRelationships2.class);
        s.addTestSuite(TestFakeOne2Many.class);
        s.addTestSuite(TestEmbedded.class);
        return s;
    }

}

