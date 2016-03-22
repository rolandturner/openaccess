
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
package com.versant.core.vds.metadata;

import com.versant.core.metadata.FieldMetaData;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.UserSchemaField;
import com.versant.odbms.model.UserSchemaClass;

/**
 * An type natively supported by VDS e.g. int, String etc.
 */
public class VdsSimpleField extends VdsField {

    private void init(FieldMetaData fmd, String schemaFieldName, Class type) {
    	Class javaClass = type;
        int nullity = javaClass.isArray()
                ? SchemaField.NULL_ALLOWED | SchemaField.NULL_ELEMENTS_ALLOWED
                : SchemaField.NULL_ALLOWED;
        schemaField = ((UserSchemaClass)fmd.classMetaData.storeClass).newField(
                schemaFieldName, javaClass, nullity);
        fmd.storeField = this;
        fmd.primaryField = true;
    }

    public VdsSimpleField(FieldMetaData fmd, String schemaFieldName, Class type) {
        super(fmd);
        init(fmd, schemaFieldName, type);
    }
        
    public VdsSimpleField(FieldMetaData fmd, String schemaFieldName) {
        super(fmd);
        init(fmd, schemaFieldName, fmd.type);
    }

    public VdsSimpleField(FieldMetaData fmd, UserSchemaField schemaField) {
        super(fmd);
        this.schemaField = schemaField;
        fmd.storeField = this;
    }

    public UserSchemaField getSchemaField() {
        return schemaField;
    }

    public Object readEmbeddedField(DatastoreObject dso) {
    	return dso.readObject(schemaField);
    }
}

