
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
package com.versant.core.jdo.junit.testHorizontal;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.teststoredproc.TestStoredProcs;

/**
 * Creates a TestSuite containing all tests in this package.
 */
public class AllTests extends VersantTestCase {

    public static Test suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(TestHorizontal.class);
        return s;
    }

}

