
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
package com.versant.core.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.VersantDetachable;
import com.versant.core.jdo.VersantDetachedStateManager;

/**
 * JDO Genie specific static utility methods.
 */
public class VersantHelper {

    /**
     * Delete an instance. This method will delete both detached and managed
     * instances.
     */
    public static void deletePersistent(Object detachedPC) {
        if (detachedPC instanceof VersantDetachable) {
            VersantDetachable detachable = (VersantDetachable)detachedPC;
            VersantDetachedStateManager sm = detachable.versantGetDetachedStateManager();
            if (sm != null) {
                Object oid = detachable.versantGetOID();
                if (oid != null) {
                    sm.versantAddDeleted(oid);
                }
            } else {
                deletePC(detachedPC);
            }
        } else {
            deletePC(detachedPC);
        }
    }

    private static void deletePC(Object detachedPC) {
        if (detachedPC instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable)detachedPC;
            PersistenceManager pm = pc.jdoGetPersistenceManager();
            if (pm != null) {
                pm.deletePersistent(pc);
            } else {
                throw BindingSupportImpl.getInstance().invalidOperation("Unmanaged objects can not be " +
                        "deleted. (class='" + detachedPC.getClass() +
                        "' object='" + detachedPC + "'");
            }
        } else {
            throw BindingSupportImpl.getInstance().invalidOperation("Can not delete an object that is not " +
                    "VersantDetachable or PersistenceCapable (class='" +
                    detachedPC.getClass() + "' object='" + detachedPC + "'");
        }
    }

}
