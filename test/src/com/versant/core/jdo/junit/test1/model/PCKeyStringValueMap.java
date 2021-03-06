
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author ppoddar
 *
 */
public class PCKeyStringValueMap implements Testable{
    private static int COUNTER = 0;
    Map _map = new HashMap();
    
    public Map getMap(){
        return _map;
    }
    public void setMap(Map map){
        _map = map;
    }
    public void update() {
        _map.put(new Simple(40+COUNTER++),"newValue"+COUNTER);
    }
    public boolean equals(Object other){
        if (this==other) return true;
        if (other instanceof PCKeyStringValueMap==false) return false;
        PCKeyStringValueMap that = (PCKeyStringValueMap)other;
        return ValueBasedEqualityChecker.compareMap(this._map, that._map);
    }
}
