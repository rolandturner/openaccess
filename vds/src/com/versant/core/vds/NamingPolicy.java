
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;

/**
 * Defines a protocol to name schema classes/fields given their JDO metadata information.
 * <p/>
 * This policy acts as a plug-in to customize how persistent Java class names and their
 * field names appear in the datastore schema.
 * <p/>
 */
public interface NamingPolicy {

    /**
     * Maps a application class name to a schema class name.
     *
     * @param cm metadata for a domain class.
     * @return name of the schema class corresponding to the domain class.
     */
    String mapClassName(ClassMetaData cm);

    /**
     * Maps a application field name to a schema field name.
     *
     * @param fm metadata for a field in a domain class.
     * @return name of the schema field corresponding to the domain field.
     */
    String mapFieldName(FieldMetaData fm);

}

