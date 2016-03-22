
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

import java.io.ObjectOutput;
import java.io.IOException;

/**
 * ObjectOutput with special support for writing out OIDs.
 */
public interface OIDObjectOutput extends ObjectOutput {

    /**
     * Write out the OID with enough information so it can be read back with
     * readOID without any supplied ClassMetaData.
     */
    public void write(OID oid) throws IOException;

    /**
     * Write out the OID. If it is a NewObjectOID then the corresponding
     * {@link OIDObjectInput#readOID(com.versant.core.metadata.ClassMetaData)}
     * call will make sure extra instances are not created.
     */
    public void writeWithoutCMD(OID oid) throws IOException;
}

