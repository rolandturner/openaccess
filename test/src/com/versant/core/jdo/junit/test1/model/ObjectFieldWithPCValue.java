
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
/*
 * Created on Sep 20, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.jdo.junit.test1.model;

/**
 * @author ppoddar
 *
 */
public class ObjectFieldWithPCValue implements Testable {
    Object _object = new Simple(10);
    /* (non-Javadoc)
     * @see com.versant.core.jdo.junit.test1.model.Testable#update()
     */
    public void update() {
        int old = ((Simple)_object).getAge();
        ((Simple)_object).setAge(old+1);
    }
    
    public boolean equals(Object other){
        if (this==other) return true;
        if (other instanceof ObjectFieldWithPCValue==false) return false;
        ObjectFieldWithPCValue that = (ObjectFieldWithPCValue)other;
        
        if (this._object==null) return that._object==null;
        
        return this._object.equals(that._object);
    }
}
