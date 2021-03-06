
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
package com.versant.core.jdo.junit.teststoredproc.model;

import java.util.Date;

/*
 * Generated by JDO Genie 
 */
public class Metabolicregion {

    private long metaboligregion_id;
    private String abbreviation;
    private String createdBy;
    private char isdeleted;
    private String lastModifiedBy;
    private Date lastModifiedDt;
    private String name;
    private byte sequence;
    private String type;
    private Metabolicregion metabolicregion;

    public Metabolicregion() {
    }

    public long getMetaboligregion_id() {
        return metaboligregion_id;
    }

    public void setMetaboligregion_id(long metaboligregion_id) {
        this.metaboligregion_id = metaboligregion_id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public char getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(char isdeleted) {
        this.isdeleted = isdeleted;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDt() {
        return lastModifiedDt;
    }

    public void setLastModifiedDt(Date lastModifiedDt) {
        this.lastModifiedDt = lastModifiedDt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getSequence() {
        return sequence;
    }

    public void setSequence(byte sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Metabolicregion getMetabolicregion() {
        return metabolicregion;
    }

    public void setMetabolicregion(Metabolicregion metabolicregion) {
        this.metabolicregion = metabolicregion;
    }

    public Metabolicregion getParentRegion() {
        return metabolicregion;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements java.io.Serializable {

        public long metaboligregion_id;

        public ID() {
        }

        public ID(String s) {
            metaboligregion_id = Long.parseLong(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Molecule.ID)) return false;

            final Molecule.ID id = (Molecule.ID) o;

            if (this.metaboligregion_id != id.moleculeId) return false;
            return true;
        }

        public int hashCode() {
            int result = 0;
            result = 29 * result + (int) metaboligregion_id;
            return result;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(metaboligregion_id);
            return buffer.toString();
        }
    }
}
