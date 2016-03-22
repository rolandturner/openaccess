
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
package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import com.versant.core.common.Debug;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Domain value for IDs
 *
 */
public class IdentificatorDV implements DomainValue {

    /**
     * Creates float domain value
     *
     * @ensure result != null
     */
    public static IdentificatorDV identificatorDV() {
        return new IdentificatorDV(Long.toString(randomSeed1.nextLong()) +
                Long.toString(randomSeed2.nextLong()));
    }

    /**
     * Creates float domain value
     *
     * @param id A given ID
     * @ensure result != null
     */
    public static IdentificatorDV identificatorDV(String id) {
//        assert id != null : "ID != null";
//        assert id.length() > 0 : "ID is not empty";
        if (Debug.DEBUG){
            Debug.assertInternal(id != null, "ID != null");
            Debug.assertInternal(id.length() > 0, "ID is not empty"); 
        }

        return new IdentificatorDV(id);
    }

    /**
     * equals
     */
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj != null && obj instanceof IdentificatorDV) {
            IdentificatorDV thing = (IdentificatorDV)obj;

            result = thing._id.equals(this._id);
        }

        return result;
    }

    /**
     * hashCode
     */
    public int hashCode() {
        return _id.hashCode();
    }

    /**
     * toString
     */
    public String toString() {
        return _id;
    }

    //
    // private attributes
    //

    /**
     * Constructor
     */
    private IdentificatorDV(String id) {
        _id = id;
    }

    /**
     * The ID (String-Representation from VMID)
     */
    private String _id;

    private static Random randomSeed1;
    private static Random randomSeed2;

    static {
        try {
            randomSeed1 = new Random(
                    InetAddress.getLocalHost().getHostAddress().hashCode());
        } catch (UnknownHostException e) {
            randomSeed1 = new Random("127.0.0.1".hashCode());
        }
        randomSeed2 = new Random();
    }

}
