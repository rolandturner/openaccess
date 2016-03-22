
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
package com.versant.core.jdo.junit.test0.model;

import java.io.Serializable;

/**
 * Base class panos.louridas@investment-bank.gr test case.
 *
 * @keep-all
 */
public class PanA {

    private String id;

    public PanA() {
    }

    public PanA(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        String n = getClass().getName();
        return n.substring(n.lastIndexOf('.') + 1) + " [id=" + id + "]";
    }

    /**
     * App identity class.
     */
    public static class ID implements Serializable {

        public String id;

        public ID() {
        }

        public ID(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public boolean equals(Object obj) {
            return id.equals(((ID)obj).id);
        }

    }

}

