
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
package com.versant.core.ejb.junit.ejbtest1;

import com.versant.core.ejb.junit.VersantEjbTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.IOException;

public class AllTests extends VersantEjbTestCase {

    public static Test suite() throws IOException {
        TestSuite s = new TestSuite();
        s.addTestSuite(TestQuery.class);
        return s;
    }
}

