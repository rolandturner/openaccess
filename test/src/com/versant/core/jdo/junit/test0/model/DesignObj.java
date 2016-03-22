
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
import java.util.Date;

/**
 * For testing some OO7 stuff.
 */
public abstract class DesignObj {

    private int id;
    private String type;
    protected Date buildDate = new Date(1046071155758L);
    static int key = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }

    public synchronized static int getKey() {
        return ++key;
    }

    public final static class Id implements Serializable {

        public int id;

        public Id() {
        }

        public Id(String idString) {
            id = Integer.parseInt(idString);
        }

        public int hashCode() {
            return id;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Id) {
                Id other = (Id)obj;
                if (other.id == id) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "" + id;
        }
    }

}

 
