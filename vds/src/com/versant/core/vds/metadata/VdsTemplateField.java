
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

import com.versant.core.vds.VdsMetaDataBuilder;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.UserSchemaClass;
import com.versant.odbms.model.UserSchemaModel;

/**
 * Base class for fields that use a template class in VDS to hold their
 * data. If the field is not embedded then a fake field is added to the owning
 * class to hold the LOID of the instance of the template class in VDS.
 */
public abstract class VdsTemplateField extends VdsField {

    /**
     * The fake field for the LOID of the VDS template class instance. Null
     * for non-embedded fields.
     */
    protected FieldMetaData loidField;
    protected UserSchemaClass template;

    public VdsTemplateField(FieldMetaData fmd, String schemaFieldName,
            VdsMetaDataBuilder mdb, UserSchemaModel model) {
        super(fmd);
        fmd.storeField = this;
        fmd.includeAllDataInDiff = true;
    }

    protected void initTemplateClass(FieldMetaData fmd, String schemaFieldName,
            VdsMetaDataBuilder mdb, UserSchemaModel model) {
        UserSchemaClass ownerClass = (UserSchemaClass)fmd.classMetaData.storeClass;

        template = findOrCreateTemplate(mdb, model);
        
        int nullity = SchemaField.NULL_ALLOWED | 
                      SchemaField.USE_DEFAULT_VALUE_AS_NULL |
                      SchemaField.NULL_ELEMENTS_ALLOWED;

        if (fmd.embedded) {
            fmd.primaryField = true;
            schemaField = ownerClass.newAggregateField(
                    schemaFieldName, 
                    template, // domain
                    SchemaField.NULL_ALLOWED);
        } else {
            fmd.secondaryField = true;
            schemaField = ownerClass.newField(
                    schemaFieldName, 
                    template, 
                    SchemaField.NULL_ALLOWED);
            
            // Create a fake field to hold the LOID for the template class instance
            // in VDS.
            loidField = new FieldMetaData();
            loidField.fake = true;
            loidField.category = MDStatics.CATEGORY_SIMPLE;
            loidField.primaryField = true;
            loidField.classMetaData = fmd.classMetaData;
            loidField.defaultFetchGroup = true;
            loidField.name = fmd.name + "_LOID";
            loidField.storeField = fmd.storeField;
            loidField.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
            loidField.setType(Long.TYPE);
            mdb.addFakeField(ownerClass, loidField);
        }
    }

    /**
     * Get our template class. This will return an existing instance or
     * create a new one.
     */
    private UserSchemaClass findOrCreateTemplate(VdsMetaDataBuilder mdb,
            UserSchemaModel model) {
        String schemaClassName = getTemplateName(mdb);
        UserSchemaClass result = model.getSchemaClass(schemaClassName);
        if (result == null) {
            result = createTemplate(mdb, schemaClassName, model);
        }
        return result;
    }

    /**
     * Get the name of our template class.
     */
    protected abstract String getTemplateName(VdsMetaDataBuilder mdb);

    /**
     * Create our template class.
     */
    protected abstract UserSchemaClass createTemplate(VdsMetaDataBuilder mdb, String schemaClassName,
            UserSchemaModel model);

    /**
     * Get the field holding the LOID for the template class instance for
     * this field.
     */
    public FieldMetaData getLoidField() {
        return loidField;
    }
}

