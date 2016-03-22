
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
 * This holds the differences between two maps. It is stored in the new State
 * when changes to a map field are persisted.
 */
public class MapDiff extends CollectionDiff {

    public MapDiff() {
    }

    public MapDiff(VersantFieldMetaData fmd) {
        super(fmd);
    }

    /**
     * The deleted keys.
     */
    public Object[] deletedKeys;

    /**
     * The inserted keys. This array will be the same size as insertedValues.
     * The value for each key is the element of insertedValues at the same
     * index.
     */
    public Object[] insertedKeys;

    public void writeExternal(OIDObjectOutput out) throws IOException {
        super.writeExternal(out);
        write(out, fmd.getKeyTypeCode(), fmd.isKeyTypePC(), deletedKeys);
        write(out, fmd.getKeyTypeCode(), fmd.isKeyTypePC(), insertedKeys);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        deletedKeys = read(in, fmd.getKeyTypeCode(), fmd.isKeyTypePC());
        insertedKeys = read(in, fmd.getKeyTypeCode(), fmd.isKeyTypePC());
    }

}
