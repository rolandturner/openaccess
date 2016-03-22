
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

 * Created on Feb 6, 2004

 *

 * Copyright Versant Corporation 2003-2005, All rights reserved

 */

package com.versant.core.vds.binding.xml;

/**
 * XMLTags
 *
 * @author ppoddar
 */

public interface XMLTags {

    public static final String SCHEMA_NODE = "versant-schema";

    public static final String CLASS_NODE = "class";

    public static final String FIELD_NODE = "field";

    public static final String NAME_ATTR = "name";

    public static final String EMBEDDED_ATTR = "embedded";

    public static final String AUXINFO_ELEMENT = "auxilary-info";

    public static final String CARDINALITY_ELEMENT = "cardinality";

    public static final String DOMAIN_ELEMENT = "domain";

    public static final String SUPERCLASS_ELEMENT = "superclass";

    public static final String SYNTHETIC_BYTES = "has-synthetic-bytes";

    public static final String SYNTHETIC_CLASS = "has-synthetic-class";

    public static final String SYNTHETIC_NULL = "has-synthetic-null";

    public static final String SYNTHETIC_NULL_ELEMENTS = "has-synthetic-null-element";

    public static final String XSD_NS = "xmlns:xs";

    public static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";

    public static final String XSD_INSTANCE_NS = "xmlns:xsi";

    public static final String XSD_INSTANCE_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String VERSANT_XSD_NS = "xsi:noNamespaceSchemaLocation";

    public static final String VERSANT_XSD_NS_URI = "versant-schema.xsd";

}

