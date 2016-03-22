
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
/*
* ====================================================================
*
* TORPEDO
* A Testbed of Object Relational Products for Enterprise Distributed Objects
* Copyright (c) 2004 The Middleware Company All Rights Reserved
* @author Bruce Martin
*
* ====================================================================
*/

package com.versant.core.jdo.junit.torpedo.model;

import java.io.Serializable;

public class AuctionPK implements Serializable {
    public String id;

    public AuctionPK () {
    }

    public AuctionPK (String id) {
        this.id = id;
    }

    public String toString () {
        return id;
    }

    public boolean equals (Object other) {
        if (other == this)
            return true;
        if (!(other instanceof AuctionPK))
            return false;

        return id == null
                         ? ((AuctionPK)other).id == null
        : id.equals (((AuctionPK)other).id);
    }

    public int hashCode () {
        return id == null ? 0 : id.hashCode ();
    }
}
