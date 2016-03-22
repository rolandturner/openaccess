
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
package com.versant.core.common;

import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import java.io.IOException;

/**
 * This holds the differences between two unordered collections. It is stored
 * in the new State when changes to an unordered collection field are persisted.
 */
public class UnorderedCollectionDiff extends CollectionDiff {

    public UnorderedCollectionDiff() {
    }

    public UnorderedCollectionDiff(VersantFieldMetaData fmd) {
        super(fmd);
    }

    /**
     * The deleted values (null if none).
     */
    public Object[] deletedValues;

    protected Object clone() throws CloneNotSupportedException {
        UnorderedCollectionDiff cloned = null;
        try {
            cloned = (UnorderedCollectionDiff)super.clone();
            cloned.deletedValues = new Object[deletedValues.length];
            System.arraycopy(deletedValues, 0, cloned.deletedValues, 0,
                    deletedValues.length);
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return cloned;
    }

    public String toString() {
        return "<UnorderedCollectionDiff: status = " + status + ": amount added ="
                + (insertedValues != null ? insertedValues.length : -1)
                + " deleted amount = "
                + (deletedValues != null ? deletedValues.length : -1) + ">";
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        super.writeExternal(out);
        write(out, fmd.getElementTypeCode(), fmd.isElementTypePC(),
                deletedValues);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        deletedValues = read(in, fmd.getElementTypeCode(),
                fmd.isElementTypePC());
    }

    public void dump() {
        if (Debug.DEBUG) {
            Debug.OUT.println("ToBeAdded");
        }
        if (insertedValues != null) {
            for (int i = 0; i < insertedValues.length; i++) {
                if (Debug.DEBUG) {
                    Debug.OUT.println("inserted = " + insertedValues[i]);
                }
            }
        }

        if (Debug.DEBUG) {
            Debug.OUT.println("ToBeDeleted");
        }
        if (deletedValues != null) {
            for (int i = 0; i < deletedValues.length; i++) {
                if (Debug.DEBUG) {
                    Debug.OUT.println("deleted = " + deletedValues[i]);
                }
            }
        }
    }

}
