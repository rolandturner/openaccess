
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
 * This holds the differences between two ordered collections. It is stored
 * in the new State when changes to an ordered collection field are persisted.
 */
public class OrderedCollectionDiff extends CollectionDiff {

    public OrderedCollectionDiff() {
    }

    public OrderedCollectionDiff(VersantFieldMetaData fmd) {
        super(fmd);
    }

    /**
     * The indexes of all deleted values. These must be in ascending order.
     */
    public int[] deletedIndexes;

    /**
     * The indexes of all inserted values. This array will be the same size
     * as insertedValues. These must be in ascending order.
     */
    public int[] insertedIndexes;

    public String toString() {
        return "<OrderedCollectionDiff inserted = " + (insertedIndexes != null ? insertedIndexes.length : 0)
                + " deleted = " + (deletedIndexes != null ? deletedIndexes.length : 0);
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        super.writeExternal(out);
        SerUtils.writeIntArray(deletedIndexes, out);
        SerUtils.writeIntArray(insertedIndexes, out);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        deletedIndexes = SerUtils.readIntArray(in);
        insertedIndexes = SerUtils.readIntArray(in);
    }

}
