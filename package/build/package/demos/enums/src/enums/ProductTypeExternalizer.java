
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
package enums;

import com.versant.core.jdo.externalizer.Externalizer;

import javax.jdo.PersistenceManager;
import java.io.Serializable;

import enums.model.ProductType;

/**
 * Converts ProductType to/from int so it can be used as a persistent field
 * type.
 */
public class ProductTypeExternalizer implements Externalizer, Serializable {

    public Object toExternalForm(Object persistenceManager,
            Object o) {
        return ((ProductType)o).getType();
    }

    public Object fromExternalForm(Object persistenceManager,
            Object o) {
        int t = (Integer)o;
        for (ProductType p : ProductType.values()) {
            if (p.getType() == t) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid ProductType: " + t);
    }

    public Class getExternalType() {
        return Integer.TYPE;
    }

}

