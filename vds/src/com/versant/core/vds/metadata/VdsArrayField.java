
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

import java.lang.reflect.Array;
import com.versant.core.common.State;
import com.versant.core.common.Debug;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.vds.VdsMetaDataBuilder;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.UserSchemaClass;
import com.versant.odbms.model.UserSchemaField;
import com.versant.odbms.model.UserSchemaModel;
import com.versant.odbms.model.SystemSchemaClass;

/**
 * Arrays are defined as multi-cardinality persistent <code>Link</code> i.e.
 * <code>LinkVstr</code>. This class will expect only arrays of user defined
 * types as primitive and primitive wrapper arrays had been accounted for
 * in a previous block.
 * Arrays are embedded for known classes by and not embedded for user classes.
 */
public class VdsArrayField extends VdsField {

	public VdsField vdsField = null;
	
	public class VdsEmbeddedArrayField extends VdsField {
		public VdsEmbeddedArrayField(FieldMetaData fmd, String schemaFieldName,
				VdsMetaDataBuilder mdb, UserSchemaModel model) {
			super(fmd);
			fmd.primaryField = true;
			fmd.includeAllDataInDiff = true;
		    int nullity;
		    if (fmd.type.getComponentType().isPrimitive()) {
			    nullity = SchemaField.NULL_ALLOWED;
		    } else {
			    nullity = SchemaField.NULL_ALLOWED | 
						SchemaField.NULL_ELEMENTS_ALLOWED;
		    }
		    schemaField = mdb.createNewArrayField(model,
                    ((UserSchemaClass)fmd.classMetaData.storeClass),
                    schemaFieldName, fmd.name, fmd.type,
                    fmd.elementTypeMetaData, nullity);
		}

		public VdsEmbeddedArrayField(FieldMetaData fmd, String schemaFieldName,
				VdsMetaDataBuilder mdb, UserSchemaModel model, Class arrayType) {
			super(fmd);
			fmd.primaryField = true;
			fmd.includeAllDataInDiff = true;
		    int nullity;
		    if (arrayType.getComponentType().isPrimitive()) {
			    nullity = SchemaField.NULL_ALLOWED;
		    } else {
			    nullity = SchemaField.NULL_ALLOWED | 
						SchemaField.NULL_ELEMENTS_ALLOWED;
		    }
			schemaField = mdb.createNewArrayField(model,
                    ((UserSchemaClass)fmd.classMetaData.storeClass),
                    schemaFieldName, fmd.name, arrayType,
                    fmd.elementTypeMetaData, nullity);
		}

		public Object readEmbeddedField(DatastoreObject dso) {
			return dso.readObject(schemaField);
		}
	}

	public class VdsSCOArrayField extends VdsTemplateField {

		private static final String ARRAY_ELEMENT = "elements";
		private UserSchemaField elementsField = null;
		private Class componentType = null;
		private ClassMetaData componentTypeMedaData = null;

		public VdsSCOArrayField(FieldMetaData fmd, String schemaFieldName,
				VdsMetaDataBuilder mdb, UserSchemaModel model) {
	        super(fmd, schemaFieldName, mdb, model);
			componentType = fmd.elementType;
			componentTypeMedaData = fmd.elementTypeMetaData;
	        initTemplateClass(fmd, schemaFieldName, mdb, model);
		}

		public VdsSCOArrayField(FieldMetaData fmd, String schemaFieldName,
				VdsMetaDataBuilder mdb, UserSchemaModel model, Class arrayType) {
	        super(fmd, schemaFieldName, mdb, model);
			componentType = arrayType.getComponentType();
			componentTypeMedaData = null;
			initTemplateClass(fmd, schemaFieldName, mdb, model);
		}

		/**
		 * Create our template class.
		 */
		protected UserSchemaClass createTemplate(final VdsMetaDataBuilder mdb,
				final String schemaClassName, final UserSchemaModel model) {
		    UserSchemaClass templatedSchemaClass = 
			new UserSchemaClass(schemaClassName, null, // No superclass
					    model);
		    templatedSchemaClass.addListener(mdb.getBinder());
		    templatedSchemaClass.setApplicationFieldCount(1);

		    int nullity;
		    if (componentType.isPrimitive()) {
			    nullity = SchemaField.NULL_ALLOWED |
						SchemaField.USE_DEFAULT_VALUE_AS_NULL;

		    } else {
			    nullity = SchemaField.NULL_ALLOWED | 
						SchemaField.USE_DEFAULT_VALUE_AS_NULL |
						SchemaField.NULL_ELEMENTS_ALLOWED;
		    }

		    UserSchemaField elementField = mdb.createNewArrayField(model,
					templatedSchemaClass,
					ARRAY_ELEMENT,
					null, //		applicationName,
					Array.newInstance(componentType, 0).getClass(),
						fmd.elementTypeMetaData,
                        nullity);
			templatedSchemaClass.setApplicationFieldIndex(0, elementField);
			boolean isElementResolved = elementField.getDomain().isPrimitive()
					|| elementField.getDomain() instanceof SystemSchemaClass
					|| ((UserSchemaClass) elementField.getDomain()).isResolved();
			templatedSchemaClass.setResolved(isElementResolved);
			return templatedSchemaClass;
		}

		/**
		 * Get the name of our template class.
		 */
		protected String getTemplateName(VdsMetaDataBuilder mdb) {
			String containerName = "Array";
			String elementName = null;
			if (componentType == String.class)
				elementName = "char";
			else if (componentType == String.class)
				elementName = "String";
			else if (componentType == byte.class)
				elementName = "byte";
			else if (componentType == int.class)
				elementName = "int";
			else if (componentType == Integer.class)
				elementName = "Integer";
			else if (componentType == long.class)
				elementName = "long";
			else if (componentType == Long.class)
				elementName = "Long";
			else if (componentType.getComponentType() == null) {
				elementName = mdb.getSchemaClassNameForJavaClass(
						componentTypeMedaData, componentType);
			} else
				throw new RuntimeException("Unsupported Array type: "
						+ componentType.toString());
			return containerName + "<" + elementName + ">";
		}

		/**
		 * Called by State to create and fill a DSO holding the data for this
		 * field if it is not embedded (has fmd.secondaryField == true).
		 */
		public DatastoreObject createAndFillDSO(DatastoreManager dsi,
				Object value, State state) {
			long loid = state.getLongFieldInternal(loidField.stateFieldNo);
			boolean isNewLoid = loid == 0L;
			if (isNewLoid) {
				if (value == null) {
					state.setFilled(loidField.stateFieldNo);
					return null;
				} else {
					loid = dsi.getNewLoid();
					state.setInternalLongField(loidField.stateFieldNo, loid);
				}
			}
//			assert loid != 0;
            if (Debug.DEBUG) {
                Debug.assertInternal(loid != 0,
                        "loid is zero");
            }
			DatastoreObject dso = template.getInstance(dsi, dsi
					.getDefaultDatastore(), loid, isNewLoid);
			elementsField = template.getFields()[0];
			dso.writeObject(elementsField, value);

			// fill in the timestamp from the owning class
			int stateFieldNo = state.getClassMetaData().optimisticLockingField.stateFieldNo;
			dso.setTimestamp(state.getIntField(stateFieldNo));

			return dso;
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
			elementsField = template.getFields()[0];
			return dso.readObject(elementsField);
//			return convertReadData(dso.readObject(elementsField));
		}
	}
	
	public VdsArrayField(FieldMetaData fmd, String schemaFieldName,
			VdsMetaDataBuilder mdb, UserSchemaModel model) {
		super(fmd);
		if (fmd.embedded) {
			vdsField = new VdsEmbeddedArrayField(fmd, schemaFieldName, mdb, model);
			fmd.primaryField = true;
		} else {
			vdsField = new VdsSCOArrayField(fmd, schemaFieldName, mdb, model);
			fmd.secondaryField = true;
		}
		fmd.storeField = this;
		this.schemaField = vdsField.schemaField;
	}

	public VdsArrayField(FieldMetaData fmd, String schemaFieldName,
			VdsMetaDataBuilder mdb, UserSchemaModel model, Class arrayType) {
		super(fmd);
		if (fmd.embedded) {
			vdsField = new VdsEmbeddedArrayField(fmd, schemaFieldName, mdb, model, arrayType);
			fmd.primaryField = true;
		} else {
			vdsField = new VdsSCOArrayField(fmd, schemaFieldName, mdb, model, arrayType);
			fmd.secondaryField = true;
		}
		fmd.storeField = this;
		this.schemaField = vdsField.schemaField;
	}

	public Object readEmbeddedField(DatastoreObject dso) {
		return vdsField.readEmbeddedField(dso);
	}

	public Object readFromDSO(DatastoreObject dso) {
		return vdsField.readFromDSO(dso);
	}

	public DatastoreObject createAndFillDSO(DatastoreManager dsi,
			Object value, State state) {
		return vdsField.createAndFillDSO(dsi, value, state); 		
	}

	public FieldMetaData getLoidField() {
		return vdsField.getLoidField();
	}
}

