
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

import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.CollectionDiff;
import com.versant.core.common.Debug;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.vds.VdsMetaDataBuilder;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.*;
import com.versant.odbms.net.DynamicType;

import java.lang.reflect.Array;

/**
 * Collections are defined as container objects that contain their elements
 * in arrays. The container object can either be embedded or not embedded.
 */
public class VdsCollectionField extends VdsTemplateField {

    private static final String COLLECTION_ELEMENT = "elements";

    private UserSchemaField _elementsField;

    public VdsCollectionField(FieldMetaData fmd, String schemaFieldName,
            VdsMetaDataBuilder mdb, UserSchemaModel model) {
        super(fmd, schemaFieldName, mdb, model);
        initTemplateClass(fmd, schemaFieldName, mdb, model);
    }

    /**
     * Get the name of our template class.
     */
    protected String getTemplateName(VdsMetaDataBuilder mdb) {
        Class container = fmd.type;
        String containerName = null;
        if (java.util.Collection.class.isAssignableFrom(container)) {
            containerName = "List";
            if (java.util.Set.class.isAssignableFrom(container)) {
                containerName = "Set";
            }
        } else {
//            assert false : container + " is not recognized";
            if (Debug.DEBUG) {
                Debug.assertInvalidOperation(false,
                        container + " is not recognized");
            }
        }
        String elementName = mdb.getSchemaClassNameForJavaClass(
                fmd.elementTypeMetaData, fmd.elementType);
        return containerName + "<" + elementName + ">";
    }

    /**
     * Create our template class.
     */
    protected UserSchemaClass createTemplate(
            final VdsMetaDataBuilder mdb, 
            final String schemaClassName,
            final UserSchemaModel model) {
        UserSchemaClass templatedSchemaClass = 
            new UserSchemaClass(schemaClassName, null, // No superclass
            		model);
        templatedSchemaClass.addListener(mdb.getBinder());
        templatedSchemaClass.setApplicationFieldCount(1);
        
        int nullity = SchemaField.NULL_ALLOWED | 
				SchemaField.USE_DEFAULT_VALUE_AS_NULL |
				SchemaField.NULL_ELEMENTS_ALLOWED;
        UserSchemaField elementField = mdb.createNewArrayField(model,
                templatedSchemaClass, 
                COLLECTION_ELEMENT,
                null, //		applicationName,
                Array.newInstance(fmd.elementType, 0).getClass(),
                fmd.elementTypeMetaData,
                nullity);
        templatedSchemaClass.setApplicationFieldIndex(0, elementField);
        boolean isElementResolved = elementField.getDomain().isPrimitive()
                || elementField.getDomain() instanceof SystemSchemaClass
                || ((UserSchemaClass)elementField.getDomain()).isResolved();
        templatedSchemaClass.setResolved(isElementResolved);
        return templatedSchemaClass;
    }

    /**
     * Called by State to create and fill a DSO holding the data for this
     * field if it is not embedded (has fmd.secondaryField == true).
     */
    public DatastoreObject createAndFillDSO(DatastoreManager dsi,
            Object value, State state) {
        long loid = state.getLongFieldInternal(loidField.stateFieldNo);
        boolean isNewLoid = (loid == 0L);
        if (isNewLoid) {
            if (value == null) {
                state.setFilled(loidField.stateFieldNo);
                return null;
            } else {
                loid = dsi.getNewLoid();
                state.setInternalLongField(loidField.stateFieldNo,loid);
            }
        }
        DatastoreObject dso = template.getInstance(
                                         dsi, 
                                         dsi.getDefaultDatastore(), 
                                         loid, 
                                         isNewLoid);
        CollectionDiff diff = (CollectionDiff)value;
        _elementsField       = template.getFields()[0];
        dso.writeObject(_elementsField, (diff==null) ? null : diff.insertedValues);
        
        // fill in the timestamp from the owning class
        int stateFieldNo = state.getClassMetaData().optimisticLockingField.stateFieldNo;
        dso.setTimestamp(state.getIntField(stateFieldNo));

        return dso;
    }

    /**
     * Called by State to create and fill a embedded field (has
     * fmd.primaryField == true).
     */
    public DatastoreObject fillEmbeddedField(DatastoreObject dso,
            Object value, State state) {
    	
        CollectionDiff diff = (CollectionDiff)value;
        Object[] elements   = removeSentinel(diff.insertedValues);
        _elementsField       = template.getFields()[0];
        
//        if (fmd.isElementTypePC()) {
//            long[] loids = VdsDataStore.convertOIDsToLOIDs(elements);
//            dso.writeDynamicType(_elementsField, new DynamicType(loids));
//        } else {
//            dso.writeObject(_elementsField, elements);
//        }
        dso.writeObject(_elementsField, elements);

        // fill in the timestamp from the owning class
        int stateFieldNo = state.getClassMetaData().optimisticLockingField.stateFieldNo;
        dso.setTimestamp(state.getIntField(stateFieldNo));

        return dso;
    }

    public DatastoreObject createEmbeddedDSO(Object data,
    		DatastoreManager dsi) {
    	Object[] elements;
		if (data instanceof CollectionDiff)
        {
        	CollectionDiff diff = (CollectionDiff)data;
        	elements = removeSentinel(diff.insertedValues);
        }
        else
        {
        	elements = (OID[])data;
        }
        	
        DatastoreObject dso = template.newAggregateInstance(dsi,
        		dsi.getDefaultDatastore());

        SchemaField elementsField = template.getFields()[0];
//        if (fmd.isElementTypePC()) {
//            long[] loids = VdsDataStore.convertOIDsToLOIDs(elements);
//            dso.writeDynamicType(elementsField, new DynamicType(loids));
//        } else {
//            dso.writeObject(elementsField, elements);
//        }
        dso.writeObject(elementsField, elements);
    	return dso;
    }

    /**
	 * Called by State to read a embedded collection field of a DSO. (i.e. has
	 * fmd.secondaryField == false).
	 */
    public Object readEmbeddedField(DatastoreObject dso) {
        SchemaField field = schemaField.getAggregateFields()[0];
        return dso.readObject(field);
    }

/** Removes the last element if it is null. A sentinel element is maintained
 * to signal end of the array for reasons unknown.
 * @param elements
 * @return the pruned array without the last null element or the original if the
 * last element is non-null.
 */     
    Object[] removeSentinel(Object[] elements){
        if (elements==null) return null;
        if (elements.length==0) return new Object[0];
        int last = elements.length-1;
        boolean hasSentinel = elements[last]==null;
        if (!hasSentinel) return elements;
        Object[] result = new Object[last-1];
        System.arraycopy(elements,0,result,0,last-1);
        return result;
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
        _elementsField = template.getFields()[0];
        return dso.readObject(_elementsField);
    }
}

