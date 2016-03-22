
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
import com.versant.odbms.model.SchemaClass;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.UserSchemaModel;
import com.versant.odbms.model.UserSchemaClass;

/**
 * A reference to another persistent class defined as uni-cardinality
 * persistent <code>Link</code> to other user defined schema class.
 * A first-class object can not be embedded.
 */
public class VdsRefField extends VdsField {

    public VdsRefField(FieldMetaData fmd, String schemaFieldName,
            SchemaClass domain) {
        super(fmd);
        if (fmd.embedded) {
//            System.err.println("NOT Embedding " + fmd.getQName());
        }
        schemaField = ((UserSchemaClass)fmd.classMetaData.storeClass).newField(
                schemaFieldName, domain, 
                SchemaField.NULL_ALLOWED);
        schemaField.setTranscriber(new OIDTranscriber(fmd.classMetaData));
        fmd.storeField = this;
        fmd.primaryField = true;
    }
}

