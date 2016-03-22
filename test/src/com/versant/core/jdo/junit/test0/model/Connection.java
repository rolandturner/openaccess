
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
 * @keep-all
 */
public class Connection {

    private String type;  // type is 10 characters long
    private int length;  // distance between connected AtomicParts
    private AtomicPart fromRef;  // <-> AtomicPart.to
    private AtomicPart toRef;    // <-> AtomicPart.from
    private int id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void addTo(AtomicPart atomicPart) {
        atomicPart.getFrom().add(this);
        toRef = atomicPart;
    }

    public void addFrom(AtomicPart atomicPart) {
        atomicPart.getTo().add(this);
        fromRef = atomicPart;
    }

    public AtomicPart getFromRef() {
        return fromRef;
    }

    public void setFromRef(AtomicPart fromRef) {
        this.fromRef = fromRef;
    }

    public AtomicPart getToRef() {
        return toRef;
    }

    public void setToRef(AtomicPart toRef) {
        this.toRef = toRef;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

 
