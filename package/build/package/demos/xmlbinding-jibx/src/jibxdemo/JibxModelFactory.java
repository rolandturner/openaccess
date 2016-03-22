
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
package jibxdemo;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

import jibxdemo.model.Group;
import jibxdemo.model.User;

import javax.jdo.PersistenceManager;

/**
 * <p>Static JiBX factory methods to create new instances when an XML document
 * is unmarshalled. These methods look for an id attribute. If it is present
 * it is used to lookup an existing instance instead of making a new instance.
 * Each method could just as well run a query to find an existing instance
 * based on other attribute values. This makes it easy to mix new and existing
 * objects in an incoming XML document.</p>
 *
 * <p>These methods are referenced by the 'factory' attributes in
 * binding.xml.</p>  
 */
public class JibxModelFactory {

    public static Group newGroup(IUnmarshallingContext ictx) {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        String id = ctx.attributeText(null, "id", null);
        if (id == null) {
            return new Group();
        } else {
            PersistenceManager pm = Sys.pm();
            Object oid = pm.newObjectIdInstance(Group.class, id);
            return (Group)pm.getObjectById(oid, true);
        }
    }

    public static User newUser(IUnmarshallingContext ictx) {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        String id = ctx.attributeText(null, "id", null);
        if (id == null) {
            return new User();
        } else {
            PersistenceManager pm = Sys.pm();
            Object oid = pm.newObjectIdInstance(User.class, id);
            return (User)pm.getObjectById(oid, true);
        }
    }

}

