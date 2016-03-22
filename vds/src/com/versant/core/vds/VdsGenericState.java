
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
package com.versant.core.vds;

import com.versant.core.common.GenericState;
import com.versant.core.common.OID;
import com.versant.core.common.NotImplementedException;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.vds.metadata.VdsField;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.DatastoreManager;

import java.util.ArrayList;

/**
 * Hand written State class for VDS.
 */
public class VdsGenericState extends GenericState implements VdsState {

    public VdsGenericState() {
    }

    public VdsGenericState(ClassMetaData cmd) {
        super(cmd);
    }

    /**
     * Write all the primary fields to the DSO. All of the primary fields
     * must be filled or a JDOFatalInternalException is thrown.
     */
    public void writePrimaryFieldsToDSO(DatastoreObject dso,
                                        DatastoreManager dsi) {
        checkCmd();
        dso.allocate();
        for (int i = 0; i < data.length; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
            if (!fmd.primaryField) continue;
            VdsField vdsField = (VdsField)fmd.storeField;
            switch (fmd.category) {
                case MDStatics.CATEGORY_SIMPLE:
                    dso.writeObject(vdsField.schemaField, data[i]);
                    break;
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    OID oid = (OID)data[i];
                    dso.writeObject(vdsField.schemaField, oid);
                    break;
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_EXTERNALIZED:
               		dso.writeObject(vdsField.schemaField, data[i]);
                	break;
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                	DatastoreObject embeddedDSO = vdsField.createEmbeddedDSO(data[i], dsi);
                    dso.writeObject(vdsField.schemaField, embeddedDSO);
                    break;
                default:
                    throw new NotImplementedException("Category " +
                            MDStaticUtils.toCategoryString(fmd.category));
            }
        }
    }

    /**
     * Read all primary fields from the DSO.
     */
    public void readPrimaryFieldsFromDSO(DatastoreObject dso) {
        try {
            checkCmd();
            long loid;
            for (int i = 0; i < data.length; i++) {
                FieldMetaData fmd = cmd.stateFields[i];
                if (!fmd.primaryField) continue;
                VdsField vdsField = (VdsField)fmd.storeField;
                switch (fmd.category) {
                    case MDStatics.CATEGORY_SIMPLE:
                        data[i] = dso.readObject(vdsField.schemaField);
                        filled[i] = true;
                        break;
                    case MDStatics.CATEGORY_REF:
                    	loid = dso.readLOID(vdsField.schemaField);
                    	if (loid == 0) {
                    		data[i] = null;
                    	} else {
                    		OID oid = fmd.typeMetaData.createOID(false);
                    		oid.setLongPrimaryKey(loid);
                    		data[i] = oid;
                    	}
                        filled[i] = true;
                        break;
                    case MDStatics.CATEGORY_POLYREF:
                    	loid = dso.readLOID(vdsField.schemaField);
                    	if (loid == 0) {
                    		data[i] = null;
                    	} else {
                    		OID oid = new VdsUntypedOID();
                    		oid.setLongPrimaryKey(loid);
                    		data[i] = oid;
                    	}
                        filled[i] = true;
                        break;
                    case MDStatics.CATEGORY_ARRAY:
                    case MDStatics.CATEGORY_COLLECTION:
                    case MDStatics.CATEGORY_MAP:
                    case MDStatics.CATEGORY_EXTERNALIZED:
                        data[i] = vdsField.readEmbeddedField(dso);
                    	filled[i] = true;
                        break;
                    default:
                        throw new NotImplementedException("Category " +
                                MDStaticUtils.toCategoryString(fmd.category));
                }
            }
        } finally {
            dso.release();
        }
    }

    /**
     * Create and fill DSOs for all the filled secondary fields and add them to
     * the DSOList. Each secondary field must update its fake loidField
     * on this State if required.
     */
    public void writeSecondaryFieldsToDSOList(DatastoreManager dsi,
            DSOList list) {
        checkCmd();
//        System.out.println("cmd = " + cmd);
        for (int i = 0; i < data.length; i++) {
            FieldMetaData fmd = cmd.stateFields[i];
//            System.out.println("[" + i + "] " + fmd.name + " sec " +
//                    fmd.secondaryField + " filled " + filled[i]);
            if (!fmd.secondaryField) continue;
            if (!filled[i]) continue;
            list.add(((VdsField)fmd.storeField).createAndFillDSO(dsi, data[i], this));
        }
    }

    /**
     * Fill the secondary field's as DatastoreObjects and mark them as deleted
     * so that the correspoding SCO can be deleted
     * along with the owner class.
     */
    public  void deleteSecondaryFields(ArrayList dsoList) {
    	checkCmd();
    	for (int i = 0; i < data.length; i++) {
    		FieldMetaData fmd = cmd.stateFields[i];
    		if (!fmd.secondaryField) continue;
            long loid = this.getLongFieldInternal(
                ((VdsField)fmd.storeField).getLoidField().stateFieldNo);
			//OA-185 = It is possible that the LOID for some SCO fields is null,
			//in which case, the loid will be null and we should not pass
			//such loids to the server.
			if (loid > 0 ) {
            	DatastoreObject dso = new DatastoreObject(loid);
            	dso.setIsDeleted(true);
            	dsoList.add(dso);
			}
    	}
    }

}

