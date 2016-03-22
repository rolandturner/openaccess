
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
package com.versant.core.jdo.junit.test2.model;

import java.sql.Timestamp;

/**
 * For testing persisting extra simple field types that are mutable e.g.
 * java.sql.Timestamp.
 * @keep-all
 */
public class ExtraSimpleMutableTypes {

    private Timestamp timestamp;

    public ExtraSimpleMutableTypes() {
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}


