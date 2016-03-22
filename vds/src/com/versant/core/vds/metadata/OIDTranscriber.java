
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
/*
 * Created on Sep 17, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.vds.metadata;

import com.versant.core.common.OID;
import com.versant.core.common.Debug;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.vds.VdsUntypedOID;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.transcriber.AssociationTranscriber;

/**
 *
 */
public class OIDTranscriber extends AssociationTranscriber {

    ClassMetaData _meta;
    
    public OIDTranscriber(ClassMetaData meta) {
        
        _meta = meta;
    }
    
    /* (non-Javadoc)
     * @see com.versant.odbms.model.transcriber.Transcriber#writeObject(com.versant.odbms.model.DatastoreObject, com.versant.odbms.model.SchemaField, java.lang.Object)
     */
    public void writeObject(DatastoreObject obj, SchemaField field, Object value) {

        if (Debug.DEBUG) {
//            assert obj != null;
            Debug.assertInternal(obj != null,
                    "DatastoreObject is null");
//            assert field != null;
            Debug.assertInternal(field != null,
                    "SchemaField is null");
//            assert field.getCardinality() == SchemaField.SINGLE_CARDINALITY;
            Debug.assertInternal(field.getCardinality() == SchemaField.SINGLE_CARDINALITY,
                    "Cardinality is not single");
        }

        super.writeObject(obj, field, value, OID.class);

        obj.writeLOID(field, value != null ? ((OID) value).getLongPrimaryKey() : 0);
    }

    /* (non-Javadoc)
     * @see com.versant.odbms.model.transcriber.Transcriber#readObject(com.versant.odbms.model.DatastoreObject, com.versant.odbms.model.SchemaField)
     */
    public Object readObject(DatastoreObject obj, SchemaField field) {
        if (Debug.DEBUG) {
//            assert obj != null;
            Debug.assertInternal(obj != null,
                    "DatastoreObject is null");
//            assert field != null;
            Debug.assertInternal(field != null,
                    "SchemaField is null");
//            assert field.getCardinality() == SchemaField.SINGLE_CARDINALITY;
            Debug.assertInternal(field.getCardinality() == SchemaField.SINGLE_CARDINALITY,
                    "Cardinality is not single");
        }

        if (_hasNullSyntheticField && obj.readNullField(field))
            return null;

        long loid = obj.readLOID(field);
        
        if (loid == 0)
            return null;
        
        OID oid = null;
        
        if (_meta != null) {
            
            oid = _meta.createOID(false);
            oid.setLongPrimaryKey(loid);
        }
        
        else {
            
            oid = new VdsUntypedOID(loid);
        }
        
        return oid;
    }

    /* (non-Javadoc)
     * @see com.versant.odbms.model.transcriber.Transcriber#toBytes(java.lang.Object)
     */
    public byte[] toBytes(Object value) {

        check(value, OID.class);
        
        return DatastoreObject.toBytes(value != null ? ((OID) value).getLongPrimaryKey() : 0);
    }
}
