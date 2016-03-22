
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
package com.versant.core.jdo.junit.test2.model.tom;

import java.io.Serializable;

/**
 * @keep-all
 */
public abstract class AbstractListable {

//the object identity
    private int id;

    private String parentString;


    public String getId() {
        if (this.id == 0) {
            return this.toString();
        } else {
            return String.valueOf(id);
        }
    }


    public void setParentString(String parentString) {
        this.parentString = parentString;
    }


    public String getParentString() {
        return parentString;
    }


    /**
     * ObjectID class
     */
    public static class ObjectID implements Serializable {

        public int id;

        public ObjectID() {
        }

        public ObjectID(String s) {
            id = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ObjectID))
                return false;

            final ObjectID objectId = (ObjectID) o;

            if (id != objectId.id)
                return false;

            return true;
        }

        public int hashCode() {
            return id;
        }

        public String toString() {
            return Integer.toString(id);
        }

    }

}
