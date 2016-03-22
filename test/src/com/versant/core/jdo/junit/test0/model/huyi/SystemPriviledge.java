
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
package com.versant.core.jdo.junit.test0.model.huyi;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 */
public class SystemPriviledge {
    private transient String name;
    private static int count = 0;
    private int index;

    public SystemPriviledge() {
        super();
    }

    public SystemPriviledge(String name) {
        this.name = name;
        index = count++;
    }

    public String getName() {
        return name;
    };
    public String toString() {
        return name;
    }

    final public static SystemPriviledge UPLOAD = new SystemPriviledge("Uploadrecord");
    final public static SystemPriviledge ANALYSIS = new
            SystemPriviledge("Analysis  record");

    final public static SystemPriviledge[] ALL_PRIVILEDGES = new SystemPriviledge[]{
        UPLOAD, ANALYSIS
    };

    private Object readResolve() throws ObjectStreamException {
        return ALL_PRIVILEDGES[index];
    }

    public boolean equals(Object obj) {
        SystemPriviledge sp = (SystemPriviledge) obj;
        return index == sp.index;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public static class PrimaryKey
            implements Serializable {
        public int index;

        public PrimaryKey() {
        }

        public PrimaryKey(String value) {
            StringTokenizer token = new StringTokenizer(value, "::");
            token.nextToken(); // className
            index = Integer.parseInt(token.nextToken()); // index
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PrimaryKey)) {
                return false;
            }
            PrimaryKey other = (PrimaryKey) object;
            return index == other.index;
        }

        public int hashCode() {
            return index;
        }

        public String toString() {
            return this.getClass().getName() + "::" + this.index;
        }
    }
}
