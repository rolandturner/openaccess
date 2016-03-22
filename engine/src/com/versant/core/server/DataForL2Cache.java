
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
package com.versant.core.server;

import com.versant.core.common.OID;
import com.versant.core.common.State;

/**
 * Holds data to be given to the L2 cache at the end of the transaction.
 * This is used for JDBC when the connection is retained during an opt tx.
 * The oids and states arrays will always be the same size.
 */
public class DataForL2Cache {

    public OID[] oids;
    public State[] states;
    public DataForL2Cache next;

}

