
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

 * Created on Feb 7, 2004

 *

 * Copyright Versant Corporation 2003-2005, All rights reserved

 */

package com.versant.core.vds.binding;

import com.versant.odbms.model.UserSchemaClass;
import com.versant.odbms.model.UserSchemaModel;

/**
 * SchemaDefinitionPolicy defines the interface to define schema to Versant
 * <p/>
 * database. Schema in Versant is defined by populating a model consisting
 * <p/>
 * of User Schema Class. The user schema classes within a model are related
 * <p/>
 * to each other by inheritence or association.
 * <p/>
 * <p/>
 * <p/>
 * Input to define the user schema class is open.
 *
 * @author ppoddar
 */

public interface SchemaDefinitionPolicy {

    /**
     * Gets or creates a schema class definition for given metadata. The schema
     * <p/>
     * class is optionally defined in the datastore, but always verified against
     * <p/>
     * the datastore.
     *
     * @param model         is both a container and factory for application defined schema
     *                      <p/>
     *                      classes. The resultant schema class of this method is added to the model.
     * @param classMetaData contains input metadata information from a different space
     *                      <p/>
     *                      (e.g. Java types or JDO metadata enhanced Java class definitions or a
     *                      <p/>
     *                      XML Document node). This interface is agnostic to a specific input form
     *                      <p/>
     *                      and hence the argument is a generic object.
     * @return a schema class as per the class metadata.
     */

    UserSchemaClass getUserSchemaClass(final UserSchemaModel model,

            final Object classMetaData);

    /**
     * Gets the type of the input metadata information that this receiver can
     * <p/>
     * process. The caller of <code>getUserSchemaClass</code> must supply the
     * <p/>
     * input metadata information as a type that is assignable from this type.
     *
     * @return Class of the input metadata information that this receiver can
     *         <p/>
     *         process.
     */

    Class getSupportedMetaClassType();

    Class getSupportedMetaFieldType();

}
