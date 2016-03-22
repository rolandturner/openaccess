
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
 * Copyright 2003, Versant Corporation. All rights reserved.
 */
package com.versant.core.vds.binding;

import com.versant.odbms.common.CodedRuntimeException;

/**
 * This is the exception class for this package. It extends the
 * {@link com.versant.odbms.common.CodedRuntimeException} class.
 * 
 * @author ppoddar
 */
public class SchemaDefinitionException extends CodedRuntimeException {

    public static final String NOT_SERIALIZABLE_COMPARATOR = "NOT_SERIALIZABLE_COMPARATOR";
    public static final String NOT_EMBEDDABLE_FIELD = "NOT_EMBEDDABLE_FIELD";
    public static final String FATAL_MISSING_FIELD = "FATAL_MISSING_FIELD";
    public static final String NOT_RECOGNIZED_FIELD = "NOT_RECOGNIZED_FIELD";
    public static final String SCHEMA_NOT_SUPPORTED = "SCHEMA_NOT_SUPPORTED";
    public static final String WRONG_EXTENSION_VALUE = "WRONG_EXTENSION_VALUE";

    static {

        addExceptionResource(SchemaDefinitionException.class, "exceptions");
    }

    /**
     * 
     */
    SchemaDefinitionException(String str) {

        super(str);
    }

    /**
     * 
     */
    public SchemaDefinitionException(final String message,
            final Object[] arguments) {

        super(message, arguments);
    }
}
    
