
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.*;
import com.versant.core.vds.VdsMetaDataBuilder;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.*;
import com.versant.odbms.net.DynamicType;

import java.lang.reflect.Array;

/**
 * Maps are defined as container objects that contain their keys and values
 * in arrays. The container object can either be embedded or not embedded.
 */
public class VdsMapField extends VdsTemplateField {

    private static final String MAP_KEY = "keys";
    private static final String MAP_VALUE = "values";

    private UserSchemaField _keyField;
    private UserSchemaField _valueField;
    
    public VdsMapField(FieldMetaData fmd, String schemaFieldName,
            VdsMetaDataBuilder mdb, UserSchemaModel model) {
        super(fmd, schemaFieldName, mdb, model);
        initTemplateClass(fmd, schemaFieldName, mdb, model);
    }

    /**
     * Get the name of our template class.
     */
    protected String getTemplateName(VdsMetaDataBuilder mdb) {
        String elementName = mdb.getSchemaClassNameForJavaClass(
                fmd.elementTypeMetaData, fmd.elementType);
        String keyName = mdb.getSchemaClassNameForJavaClass(
                fmd.keyTypeMetaData, fmd.keyType);
        return "Map<" + keyName + "," + elementName + ">";
    }

    /**
     * Create our template class.
     */
    protected UserSchemaClass createTemplate(final VdsMetaDataBuilder mdb, final String schemaClassName,
            final UserSchemaModel model) {
        UserSchemaClass templatedSchemaClass = new UserSchemaClass(schemaClassName,
                null, // No superclass
                model);
        templatedSchemaClass.addListener(mdb.getBinder());
        UserSchemaField keyField = mdb.createNewArrayField(model,
                templatedSchemaClass, MAP_KEY, null, //		applicationName,
                Array.newInstance(fmd.keyType, 0).getClass(),
                fmd.keyTypeMetaData,
                SchemaField.NULL_ALLOWED | 
				SchemaField.USE_DEFAULT_VALUE_AS_NULL);
        UserSchemaField valueField = mdb.createNewArrayField(model,
                templatedSchemaClass, MAP_VALUE, null, //		applicationName,
                Array.newInstance(fmd.elementType, 0).getClass(),
                fmd.elementTypeMetaData,
                SchemaField.NULL_ALLOWED | 
				SchemaField.USE_DEFAULT_VALUE_AS_NULL |
                SchemaField.NULL_ELEMENTS_ALLOWED);
        // TODO look at meta data to decide if null keys are allowed
        //keyField.setCheckElementNullity(getNullityExtension(fmd, EXTENSION_NULL_KEY));
        // TODO look at meta data to decide if null values are allowed
        //valueField.setCheckElementNullity(getNullityExtension(fmd, EXTENSION_NULL_VALUE));
        templatedSchemaClass.setApplicationFieldCount(2);
        templatedSchemaClass.setApplicationFieldIndex(0, keyField);
        templatedSchemaClass.setApplicationFieldIndex(1, valueField);
        templatedSchemaClass.setResolved(isFieldResolved(keyField) && 
                                         isFieldResolved(valueField));
        return templatedSchemaClass;
    }
    boolean isFieldResolved(SchemaField f){
        return f!=null &&
               (f.getDomain().isPrimitive() ||
                f.getDomain() instanceof  SystemSchemaClass ||      
                ((UserSchemaClass)f.getDomain()).isResolved());       
    }
    /**
     * Called by State to create and fill a DSO holding the data for this
     * field if it is not embedded (has fmd.secondaryField == true).
     */
    public DatastoreObject createAndFillDSO(DatastoreManager dsi,
            Object value, State state) {
        long loid = state.getLongFieldInternal(loidField.stateFieldNo);
        boolean isNew = (loid == 0L);
        if (isNew) {
            if (value == null) {
                state.setFilled(loidField.stateFieldNo);
                return null;
            } else {
                loid = dsi.getNewLoid();
                state.setInternalLongField(loidField.stateFieldNo,loid);
            }
        }
        DatastoreObject dso = template.getInstance(dsi,
                   dsi.getDefaultDatastore(), loid, isNew);

    	Object[] keys = null;
    	Object[] values = null;

		if (value != null) {
		    keys = ((MapDiff)value).insertedKeys;
	    	values = ((MapDiff)value).insertedValues;
		}

        _keyField   = template.getFieldByApplicationIndex(0);
        _valueField = template.getFieldByApplicationIndex(1);

		dso.writeObject(_keyField, keys);
        dso.writeObject(_valueField, values);

        // fill in the timestamp from the owning class
        int stateFieldNo = state.getClassMetaData().optimisticLockingField.stateFieldNo;
        dso.setTimestamp(state.getIntField(stateFieldNo));

        return dso;
    }

    public DatastoreObject createEmbeddedDSO(Object value,
    		DatastoreManager dsi) {
    	Object[] keys = null;
    	Object[] values = null;
    	
        if (value instanceof MapDiff) {
        	if (value != null) {
        		keys = ((MapDiff)value).insertedKeys;
        		values = ((MapDiff)value).insertedValues;
        	}
        }
        else {
        	if (value != null) {
        		keys = ((MapEntries)value).keys;
        		values = ((MapEntries)value).values;
        	}
        }
        DatastoreObject dso = template.newAggregateInstance(dsi,
        		dsi.getDefaultDatastore());
    	_keyField = template.getFields()[0];
        _valueField = template.getFields()[1];

        dso.writeObject(_keyField, keys);
        dso.writeObject(_valueField, values);
        
        return dso;
    }

    /**
	 * Called by State to read a embedded externilized field of a DSO. (i.e. has
	 * fmd.secondaryField == false).
	 */
    public Object readEmbeddedField(DatastoreObject dso) {
    	DatastoreObject embeddedDSO = (DatastoreObject)dso.readObject(
                ((VdsField)fmd.storeField).schemaField);
        return convertReadData(embeddedDSO);
    }

    /**
     * Read contents of a templated container. If the contents are
     * LOIDs then create an array of unresolved OIDs. Otherwise
     * the appropriate transcriber will read the data in an array
     * form from the DatastoreObject.
     * <p>
     * @return the element value(s) contained in the templated 
     * container in a form that is ammenable to be stored
     * in GenericState.
     */
    public Object readFromDSO(DatastoreObject dso) {
        return convertReadData(dso);
    }

    /**
     * If create MapEntries for the map object.
     */
    private Object convertReadData(DatastoreObject dso) {
        _keyField = template.getFieldByApplicationIndex(0);
        _valueField = template.getFieldByApplicationIndex(1);
    	Object keys = dso.readObject(_keyField);
        Object values = dso.readObject(_valueField);
//        ClassMetaData keyMetaData   = fmd.keyTypeMetaData;
//        ClassMetaData valueMetaData = fmd.elementTypeMetaData;
//        boolean keyUntyped = fmd.keyType == Object.class;
//        boolean valueUntyped = fmd.keyType == Object.class;


        if (keys == null) {
            if (Debug.DEBUG) {
                Debug.assertInternal(values == null,
                        "values is not equal to null");
            }
            return null;
        }

        return new MapEntries((Object[])keys, (Object[]) values);
    }
}

