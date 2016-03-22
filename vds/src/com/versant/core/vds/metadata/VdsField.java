
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.UserSchemaField;

/**
 * Extension of FieldMetaData for VDS fields. Subclasses must initialize
 * their meta data and VDS schema information in the constructor. These
 * are created by {@link com.versant.core.vds.VdsMetaDataBuilder}
 * based on the standard JDO meta data.
 */
public abstract class VdsField {

    public FieldMetaData fmd;
    public UserSchemaField schemaField;

    protected VdsField(FieldMetaData fmd) {
        this.fmd = fmd;
    }

    /**
     * Called by State to create and fill a DSO holding the data for this
     * field if it is not embedded (has fmd.secondaryField == true).
     */
    public DatastoreObject createAndFillDSO(DatastoreManager dsi, Object value,
            State state) {
        throwAbstractError("createAndFillDSO", fmd);
        return null;
    }

    /**
	 * Created an DSO for Agragated field. This object will be used to fill the
	 * embedded field.
	 */
    public DatastoreObject createEmbeddedDSO(Object data,
    		DatastoreManager dsi) {
        throwAbstractError("createEmbeddedDSO", fmd);
        return null;
    }

    /**
	 * Called by State to read a embedded externilized field of a DSO. (i.e. has
	 * fmd.secondaryField == false).
	 */
    public Object readEmbeddedField(DatastoreObject dso) {
        throwAbstractError("readEmbeddedField", fmd);
        return null;
    }

    /**
	 * Called by State to read a not embedded externilized field of a DSO. (i.e. has
	 * has fmd.secondaryField == true).
	 */
    public Object readFromDSO(DatastoreObject dso) {
        throwAbstractError("readFromDSO", fmd);
        return null;
    }

    /**
     * Get the field holding the LOID for the template class instance for
     * this field.
     */
    public FieldMetaData getLoidField() {
        throwAbstractError("getLoidField", fmd);
        return null;
    }
    void throwAbstractError(String method, FieldMetaData fmd){
        throw BindingSupportImpl.getInstance().internal(
                "Abstract Method [" + method + "] called on [" + fmd.getQName() + "]");
    }
}

