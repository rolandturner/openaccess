
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

import com.versant.core.metadata.FieldMetaData;

import java.util.Comparator;

/**
 * Meta data for a persistent or transactional field.
 */
public interface VersantFieldMetaData {

    /**
     * Return the fully qualified name of this field.
     */
    public String getQName();

    /**
     * The name of the field.
     */
    public String getName();

    /**
     * The absolute fieldNo for this field.
     */
    public int getManagedFieldNo();

    /**
     * Is this an ordered collection?
     */
    public boolean isOrdered();

    /**
     * Is the element type (or value for maps) a persistent class?
     */
    public boolean isElementTypePC();

    /**
     * Is this a map with a persistent class key?
     */
    public boolean isKeyTypePC();

    /**
     * The type stored in the collection or the value type for a map or the
     * component type for an array.
     */
    public Class getElementType();

    /**
     * The key type (null if not a map).
     */
    public Class getKeyType();

    /**
     * The type code for elementType (0 if none).
     *
     * @see com.versant.core.metadata.MDStatics
     * @see #getElementType()
     */
    public int getElementTypeCode();

    /**
     * The type code for keyType (0 if none).
     *
     * @see com.versant.core.metadata.MDStatics
     * @see #getKeyType()
     */
    public int getKeyTypeCode();

    /**
     * Is isMaster or isManyToMany is set then this indicates if
     * the relationship is managed by the SCOs or not.
     */
    public boolean isManaged();

    /**
     * Is this field a master (one) in a master/detail (one-to-many)
     * relationship
     */
    public boolean isMaster();

    /**
     * Is this field in a many-to-many relationship?
     */
    public boolean isManyToMany();

    /**
     * If isMaster, isDetail or isManyToMany is set then this is the fieldNo of
     * the field on the other side of the relationship.
     */
    public int getInverseFieldNo();

    /**
     * If isMaster, isDetail or isManyToMany is set then this is the field
     * on the other side of the relationship.
     */
    public VersantFieldMetaData getInverseFieldMetaData();

    /**
     * Get the comparator for this field if it makes sense i.e. this
     * is a sorted Collection or Map with Comparator. This is cached.
     */
    public Comparator getComparator();

    /**
     * If this is a collection, array or map and this field is true then all
     * data must be provided in the diff instance instead of just the changes
     * on commit or flush. This is used for datastores like VDS that always
     * write everything.
     */
    boolean isIncludeAllDataInDiff();

    /**
     * Is this an artificial field created to hold some store specific
     * information (e.g. row version column values for a JDBC store)?
     */
    boolean isFake();
}
