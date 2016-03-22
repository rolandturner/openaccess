
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
package com.versant.core.ejb.junit.ejbtest0.model;

import javax.persistence.Column;
import javax.persistence.IdClass;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 */
@Entity(access=AccessType.FIELD)
@IdClass(ClassWithCompPK.PK.class)
public class ClassWithCompPK {
    @Id
    @Column(length=30)
    private String id1;    // id field
    @Id 
    @Column(length=30)
    private String id2;   // id field

    private String val;

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public static class PK implements Serializable {
        public String id1;
        public String id2;

        public PK() {
        }

        public PK(String id) {
            String[] idVals = id.split("-");
            id1 = idVals[0];
            id2 = idVals[1];
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;

            final PK pk = (PK) o;

            if (!id1.equals(pk.id1)) return false;
            if (!id2.equals(pk.id2)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = id1.hashCode();
            result = 29 * result + id2.hashCode();
            return result;
        }

        public String toString() {
            return id1 + "-" + id2;
        }
    }
}
