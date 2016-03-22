
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

import javax.jdo.InstanceCallbacks;
import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Iterator;

/**
 */
public class Reference implements InstanceCallbacks {
    private String val;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    /**
     * Called after the values are loaded from the data store into
     * this instance.
     * <p/>
     * <P>This method is not modified by the Reference Enhancer.
     * <P>Derived fields should be initialized in this method.
     * The context in which this call is made does not allow access to
     * other persistent JDO instances.
     */
    public void jdoPostLoad() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called before the values are stored from this instance to the
     * data store.
     * <p/>
     * <P>Data store fields that might have been affected by modified
     * non-persistent fields should be updated in this method.
     * <p/>
     * <P>This method is modified by the enhancer so that changes to
     * persistent fields will be reflected in the data store.
     * The context in which this call is made allows access to the
     * <code>PersistenceManager</code> and other persistent JDO instances.
     */
    public void jdoPreStore() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called before the values in the instance are cleared.
     * <p/>
     * <P>Transient fields should be cleared in this method.
     * Associations between this
     * instance and others in the runtime environment should be cleared.
     * <p/>
     * <P>This method is not modified by the enhancer.
     */
    public void jdoPreClear() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called before the instance is deleted.
     * This method is called before the state transition to persistent-deleted
     * or persistent-new-deleted. Access to field values within this call
     * are valid. Access to field values after this call are disallowed.
     * <P>This method is modified by the enhancer so that fields referenced
     * can be used in the business logic of the method.
     */
    public void jdoPreDelete() {
        Util.processDelete(this);
    }
}
