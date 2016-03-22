
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
import com.versant.odbms.model.UserSchemaField;
import com.versant.odbms.model.UserSchemaClass;
import com.versant.odbms.model.transcriber.TranscriberAdapter;
import com.versant.odbms.model.transcriber.TranscriberAdapterFactory;

/**
 * For timestamp field support
 */
public class VdsTimeStampField extends VdsField {
    private static final String TIMESTAMP_FIELD_NAME = "o_ts_timestamp";

    public VdsTimeStampField(FieldMetaData fmd) {
        super(fmd);
        UserSchemaClass usc = ((UserSchemaClass)fmd.classMetaData.storeClass);
        String tsname = usc.getName() + "::" + TIMESTAMP_FIELD_NAME; 
        schemaField = usc.getField(tsname);
        TranscriberAdapter adapter = 
        	TranscriberAdapterFactory.getAdapter(int.class);
        schemaField.setTranscriber(adapter.newTranscriber(int.class, 
        		false, false, true));
        fmd.storeField = this;
        fmd.primaryField = true;
    }
        
    public UserSchemaField getSchemaField() {
        return schemaField;
    }
}

