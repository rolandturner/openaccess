
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
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.vds.VdsUntypedOID;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.SchemaField;
import com.versant.odbms.model.transcriber.AssociationArrayTranscriber;
import com.versant.odbms.net.DynamicType;
import com.versant.odbms.net.Types;

/** Transcribes an array of Object Identifiers to/from Versant net data
 * stream. 
 * <p>
 * The net datastream uses long numbers to represent datastore identifier
 * whereas Object Identifier can use some other mechanics. Here, this
 * receiver assumes Genie Object Identifiers are being used and uses
 * Genie's feature to create new Genie Object Identifiers from LOID
 * and vice versa.
 *
 */
public class OIDArrayTranscriber extends AssociationArrayTranscriber {
    
    final ClassMetaData _meta;
    
    public OIDArrayTranscriber(
            ClassMetaData meta,
            boolean hasNullSyntheticField) {
        
        super(hasNullSyntheticField);
        
        _meta = meta;
    }
    
    /* Writes an array of LOID into the given Datastore record. Extracts the
     * LOIDs from given array of OIDs.
     * @param  obj is DatastoreObject to be written to. Must not be null.
     * @param  field denotes SchemaField where the data should be written. Must
     * not be null.
     * @param value is expected to be an array of Genie OID. However, it is
     * passed as an Object[]. 
     * Can be null.
     * @see com.versant.odbms.model.transcriber.Transcriber#writeObject(com.versant.odbms.model.DatastoreObject, com.versant.odbms.model.SchemaField, java.lang.Object)
     */
    public void writeObject(DatastoreObject obj, SchemaField field, Object value) {
        
        writeObject(obj, field, value, Object[].class);

        if (value != null) {
            
            Object[] oids = (Object[]) value;
            
            long[] loids = new long[oids.length];
            
            for (int i = 0; i < oids.length; i++) {
            	loids[i] = (oids[i] == null) ? 0L : ((OID) oids[i]).getLongPrimaryKey();
            }

            obj.writeDynamicType(field, loids);
        }
        
        else {
            
            obj.writeDynamicType(field, (long[]) value);
        }
    }

    /* Reads an array of LOIDs and creates an array of Genie OIDs.
     * @return an array of OID instances.
     * @see com.versant.odbms.model.transcriber.Transcriber#readObject(com.versant.odbms.model.DatastoreObject, com.versant.odbms.model.SchemaField)
     */
    public Object readObject(DatastoreObject obj, SchemaField field) {
        
        if (_hasNullSyntheticField && obj.readNullField(field))
            return null;

        DynamicType value = obj.readDynamicType(field);
        
        OID[] oids  = new OID[value.size(Types.LOID)];
        
        if (_meta != null) {
            
            for (int i=0; i < oids.length; i++) {
                
            	long oid = value.getLong(i);
            	
            	if (oid != 0) {
            		
	                oids[i] = _meta.createOID(false);
	                oids[i].setLongPrimaryKey(value.getLong(i));
            	}
            	
            	else {
            		
            		oids[i] = null;
            	}
            }
        }
        
        else {
            
            for (int i =0; i < oids.length; i++) {
                
            	long oid = value.getLong(i);
            	
            	if (oid != 0) {
            		
            		oids[i] = new VdsUntypedOID(value.getLong(i));
            	}
            	
            	else {
            		
            		oids[i] = null;
            	}
            }
        }
        
        return oids;
    }

    /* (non-Javadoc)
     * @see com.versant.odbms.model.transcriber.Transcriber#toBytes(java.lang.Object)
     */
    public byte[] toBytes(Object value) {

        check(value, Object[].class);

        Object[] oids = (Object[]) value;
        
        long[] loids = new long[oids.length];
        
        for (int i = 0; i < oids.length; i++) {

        	loids[i] = (oids[i] == null) ? 0L : ((OID) oids[i]).getLongPrimaryKey();

        }

        return value != null
            ? DynamicType.toBytes(loids)
            : DynamicType.EMPTY_BYTE_ARRAY;
    }
}
