
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.GenericOID;
import com.versant.core.common.OID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.vds.util.Loid;

/**
 * This includes only the loid in the hashcode calculation so typed and
 * untyped OIDs can be used as keys in the same maps. The hyperdrive OID
 * classes for VDS must use the same hashing function.
 */
public class VdsGenericOID extends GenericOID {

    public VdsGenericOID() {
    }

    public VdsGenericOID(ClassMetaData cmd, boolean resolved) {
        super(cmd, resolved);
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            throw BindingSupportImpl.getInstance().unsupported(
                    cmd + " using unsupported Application Identity");
        }
    }

    protected GenericOID newInstance() {
        return new VdsGenericOID();
    }

    /**
     * Use same hashing function as {@link VdsUntypedOID}.
     */
    public int hashCode() {
        return (int)((Number)pk[0]).longValue();
    }

    public String toSString() {
        StringBuffer s = new StringBuffer();
        s.append("GenericOID@");
        s.append(Integer.toHexString(System.identityHashCode(this)));
        s.append(' ');
        if (cmd == null) {
            s.append("classIndex ");
            s.append(cmd.index);
        } else {
            String n = cmd.qname;
            int i = n.lastIndexOf('.');
            if (i >= 0) n = n.substring(i + 1);
            s.append(n);
            s.append(' ');
            if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
                s.append(Loid.asString(getLongPrimaryKey()));
            } else {
                for (i = 0; i < pk.length; i++) {
                    if (i > 0) s.append(' ');
                    s.append(pk[i]);
                }
            }
        }
        if (!isResolved()) s.append(" NOTRES ");
        return s.toString();
    }

}

