
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

import com.versant.core.common.State;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.vds.VdsMetaDataBuilder;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.*;

/**
 * Collections are defined as container objects that contain their elements
 * in arrays. The container object can either be embedded or not embedded.
 */
public class VdsExternalizedField extends VdsField {
	private VdsField vdsField = null;
	
    public VdsExternalizedField(FieldMetaData fmd, String schemaFieldName,
           VdsMetaDataBuilder mdb, UserSchemaModel model) {
    	super(fmd);
    	Class externalType = fmd.externalizer.getExternalType();
		Class elemType = externalType.getComponentType();
		if (elemType == null) {
//			if (elemType != String.class) {
			// Primitive type. Force embedding.
			fmd.embedded = true;
			vdsField = new VdsSimpleField(fmd, schemaFieldName, externalType);
//			}
		} else {
			vdsField = new VdsArrayField(fmd, schemaFieldName, mdb, model, externalType);
		}
		fmd.storeField = this;
		this.schemaField = vdsField.schemaField;
    }

    /**
     * Called by State to create and fill a DSO holding the data for this
     * field if it is not embedded (has fmd.secondaryField == true).
     */
    public DatastoreObject createAndFillDSO(DatastoreManager dsi,
            Object value, State state) {
    	return vdsField.createAndFillDSO(dsi, value, state);
    }

    public DatastoreObject createEmbeddedDSO(Object data,
    		DatastoreManager dsi) {
    	return vdsField.createEmbeddedDSO(data, dsi);
    }

    /**
	 * Called by State to read a embedded externilized field of a DSO. (i.e. has
	 * fmd.secondaryField == false).
	 */
    public Object readEmbeddedField(DatastoreObject dso) {
   		return vdsField.readEmbeddedField(dso);
    }

    /**
	 * Called by State to read a not embedded externilized field of a DSO. (i.e. has
	 * has fmd.secondaryField == true).
	 */
    public Object readFromDSO(DatastoreObject dso) {
    	return vdsField.readFromDSO(dso);
    }

    public FieldMetaData getLoidField() {
    	return vdsField.getLoidField();
    }
}
