
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
package com.versant.testcenter.model;

import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;

/**
 * JDO related utility methods.
 *
 */
public class JDOUtil {

    private JDOUtil() {
    }

    public static String getOID(Object po) {
        return formatOID(JDOHelper.getObjectId(po));
    }

    public static String formatOID(Object oid) {
        return oid.toString();
    }

    public static Object parseOID(PersistenceManager pm, Class cls, String oid) {
        return pm.newObjectIdInstance(cls, oid);
    }

}

