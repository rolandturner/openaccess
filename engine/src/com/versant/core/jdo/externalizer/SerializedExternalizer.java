
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
package com.versant.core.jdo.externalizer;

import com.versant.core.common.BindingSupportImpl;

import java.io.*;

/**
 * Externalizer that converts to/from byte[] using Java serialization.
 * This class is Serializable. This is only a requirement if you are using
 * remote PMs.
 */
public class SerializedExternalizer implements Externalizer, Serializable {

    public static final String SHORT_NAME = "SERIALIZED";

    public Object toExternalForm(Object pm, Object o) {
        if (o == null) return null;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(256);
            ObjectOutputStream os = new ObjectOutputStream(bo);
            os.writeObject(o);
            os.close();
            return bo.toByteArray();
        } catch (IOException e) {
            throw BindingSupportImpl.getInstance().fatal(e.toString(), e);
        }
    }

    public Object fromExternalForm(Object pm, Object o) {
        if (o == null) return o;
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream((byte[])o);
            ObjectInputStream oi = new ObjectInputStream(bi);
            Object ans = oi.readObject();
            oi.close();
            return ans;
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().fatal(e.toString(), e);
        }
    }

    public Class getExternalType() {
        return /*CHFC*/byte[].class/*RIGHTPAR*/;
    }
}
