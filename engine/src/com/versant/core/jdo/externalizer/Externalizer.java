
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
package com.versant.core.jdo.externalizer;

/**
 * Externalizers convert an object to/from some other object for storage. This
 * is done when the object is stored/retrieved from the data store. The
 * transformation takes place on the "client side" in the context of
 * a PersistenceManager so references to PC instances can be converted
 * into OIDs and so on.
 * <p/>
 * If the class implementing this Interface has a constructor that accepts
 * a single Class argument then this will be invoked with the target field
 * type. Throw an IllegalArgumentException if this type is not suitable.
 * Otherwise the default constructor is used.
 *
 * @see SerializedExternalizer
 * @see TypeAsBytesExternalizer
 * @see TypeAsStringExternalizer
 */
public interface Externalizer {

    /**
     * Convert to an object for storage.
     */
    public Object toExternalForm(Object pm, Object o);

    /**
     * Create from an object read from storage.
     */
    public Object fromExternalForm(Object pm, Object o);

    /**
     * Return the class that we convert to/from. This is used to decide
     * how to map the field (e.g. what sort of database column to create).
     */
    public Class getExternalType();

}
