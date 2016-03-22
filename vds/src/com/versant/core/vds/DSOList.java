
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
package com.versant.core.vds;


import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.odbms.model.DatastoreObject;

/**
 * An auto expanding list of DataStoreObject's with direct access to the
 * underlying array. The reset method will empty the list. 
 * 
 * <B>CAUTION:</B>
 * This does not
 * null all the references in the data array or get rid of it so do not
 * resuse DSOList's outside of method scope.
 */
public class DSOList {

    private DatastoreObject[] data;
    private int size;
    private final boolean incrementTimestamp;
/** Constructs a list of given capacity. 
 * 
 * @param capacity positive number denoteing initial size
 * @param incrementTimestamp increments timestamp of the data objects
 * added to this receiver.
 */
    public DSOList(int capacity, boolean incrementTimestamp) {
//        assert capacity > 0;
    if (Debug.DEBUG) {
        Debug.assertInternal(capacity > 0,
                "capacity is not > 0 ");
    }
        data = new DatastoreObject[capacity];
        this.incrementTimestamp = incrementTimestamp;
    }
/** Adds the given data object 
 * 
 * @param o data object to be added. If null then nothing is added.
 * 
 * @exception JDOFatalInternalException
 * if LOID of the data object is NULL (zero)
 */
    public void add(DatastoreObject o) {
        if (o==null) return;
        if (o.getLOID() == 0L) {
            throw BindingSupportImpl.getInstance().internal("DSO has NULL LOID");
        }
        if (incrementTimestamp) o.setTimestamp(o.getTimestamp()+1);
        if (size == data.length) {
            DatastoreObject[] a = new DatastoreObject[size * 3 / 2 + 1];
            System.arraycopy(data, 0, a, 0, size);
            data = a;
        }
        data[size++] = o;
    }

    public int size() {
        return size;
    }

    public DatastoreObject[] getData() {
        return data;
    }

    /**
     * Empty the list. This does not null all the references in the data
     * array or get rid of it so do not resuse DSOList's outside of method
     * scope.
     */
    public void reset() {
        size = 0;
    }

    /**
     * Return a trimmed copy of our data.
     */
    public DatastoreObject[] trim() {
        DatastoreObject[] a = new DatastoreObject[size];
        System.arraycopy(data, 0, a, 0, size);
        return a;
    }

}
