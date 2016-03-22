
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
package jibxdemo.model;

import javax.jdo.JDOHelper;

/**
 * Base class for persistent classes in the model. This class is not itself
 * persistent. It adds methods to implement an id property to fill the id
 * attribute with the JDO identity of the instance when marshalling to XML.
 */
public class ModelObject {

    /**
     * Does this instance have a JDO identity (i.e. is it persistent)?
     */
    public boolean hasId() {
        return JDOHelper.isPersistent(this);
    }

    /**
     * Return the JDO identity of this instance if it is persistent otherwise
     * return null (i.e. id attribute left out of XML).
     */
    public String getId() {
        Object oid = JDOHelper.getObjectId(this);
        return oid == null ? null : oid.toString();
    }

    public void setId(String id) {
        // ignore - just to keep JiBX happy
    }
}

