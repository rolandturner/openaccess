
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
package com.versant.core.jdo.junit;

/**
 * Starts a Open Access server for remote testing. All this does is lookup
 * the pmf using a test case. The PMF automatically exports itself.
 */
public class StartTestServer extends VersantTestCase {

    public static void main(String[] args) throws Exception {
        StartTestServer test = new StartTestServer();
        test.pmf();
        System.out.println(
            "Started remote server .. do 'ant junit-run -Dtest=testXXX -Dversant.host=127.0.0.1' from another " +
            "console (Ctrl-C to exit)\n");
        while (true) {
            Thread.sleep(60 * 1000);
        }
    }
}
