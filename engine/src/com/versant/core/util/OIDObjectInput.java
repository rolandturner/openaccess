
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
package com.versant.core.util;

import com.versant.core.common.OID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;

import java.io.ObjectInput;
import java.io.IOException;

/**
 * ObjectInput with special support for reading in OIDs. This ensures that
 * there is only one instance for each NewObjectOID read for a flattened
 * graph.
 */
public interface OIDObjectInput extends ObjectInput {

    /**
     * Read an OID.
     */
    public OID readOID() throws IOException, ClassNotFoundException;

    /**
     * Read the OID for cmd. If it is a NewObjectOID that has been previously
     * read then return the previously read instance.
     */
    public OID readOID(ClassMetaData cmd) throws IOException,
            ClassNotFoundException;

    /**
     * Get the meta data associated with this read operation.
     */
    public ModelMetaData getModelMetaData();
}

