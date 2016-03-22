
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
import java.util.*;
import junit.framework.Assert;
/**
 * @author ppoddar
 *
 */
public class ValueBasedEqualityChecker {
    public static boolean compareMap(Map a, Map b){
        if (a==null){
            Assert.assertNull("Second Map is not null", b);
            return (b==null);
        } else {
            if (b==null) {
                Assert.assertNotNull("Second Map is null", b);
                return false;
            } else { // a and b both non-null
                Assert.assertEquals("Size of two maps not same", a.size(), b.size());
                if (a.size()!=b.size()) return false;
                Iterator ia = a.keySet().iterator();
                while (ia.hasNext()){
                    Object aKey   = ia.next();
                    Object aValue = a.get(aKey);
                    Object bKey   = getEquivalentKey(b,aKey);
                    Assert.assertNotNull("key [" + aKey + "] is not in the second map", bKey);
                    Object bValue = b.get(bKey);
                    Assert.assertEquals("value [" + aValue + "] in first map corresponding to key " + aKey + " is value [" + bValue + "] in second map", aValue, bValue);
                }
                
            }
        }
        return true;
    }
    static Object getEquivalentKey(Map m, Object find){
        Iterator keys = m.keySet().iterator();
        while (keys.hasNext()){
            Object key = keys.next();
            if (key.equals(find)) return key;
        }
        return null;
    }
}
