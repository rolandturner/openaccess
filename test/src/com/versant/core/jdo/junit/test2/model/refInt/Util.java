
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
package com.versant.core.jdo.junit.test2.model.refInt;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Iterator;

/**
 */
public final class Util {

    public static void processDelete(Reference refValue) {
        PersistenceManager pm = JDOHelper.getPersistenceManager(refValue);
        if (pm == null) {
            return;
        }
        Query q = pm.newQuery(Referent.class, "ref == param");
        q.declareParameters("Reference param");
        Collection results = (Collection) q.execute(refValue);
        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            Referent ref = (Referent) iterator.next();
            ref.setRef(null);
        }
    }
}
