
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
package com.versant.core.jdo.junit.test2.model;

import java.io.Serializable;

/** 
 * For testing autoinc columns and application identity.
 * @keep-all
 */
public class AutoIncApp {

    private AutoIncApp other;

    private int autoIncAppId = 0;
    private String name;

    public AutoIncApp(String name) {
        this.name = name;
    }

    public AutoIncApp getOther() {
        return other;
    }

    public void setOther(AutoIncApp other) {
        this.other = other;
    }

    public void setAutoIncAppId(int autoIncAppId) {
        this.autoIncAppId = autoIncAppId;
    }

    public int getAutoIncAppId() {
        return autoIncAppId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return autoIncAppId + " " + name;
    }

    public static class ID implements Serializable {

        public int autoIncAppId;

        public ID() {
        }

        public ID(String s) {
            autoIncAppId = Integer.parseInt(s);
        }

        public ID(int autoIncAppId) {
            this.autoIncAppId = autoIncAppId;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (autoIncAppId != id.autoIncAppId) return false;

            return true;
        }

        public int hashCode() {
            return autoIncAppId;
        }

        public String toString() {
            return Integer.toString(autoIncAppId);
        }

    }

}
