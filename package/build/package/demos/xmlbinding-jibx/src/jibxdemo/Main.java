
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

import jibxdemo.model.User;

import javax.jdo.PersistenceManager;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IMarshallingContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * This constructs a graph of instances from an XML message and persists
 * them using JDO.
 */
public class Main {

    public static void main(String[] args) {
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(User.class);
            IUnmarshallingContext uctx;
            IMarshallingContext mctx;

            PersistenceManager pm = Sys.pm();

            // persist a graph of User's and Group's from doc1.xml
            pm.currentTransaction().begin();
            uctx = bfact.createUnmarshallingContext();
            User user = (User)uctx.unmarshalDocument(
                    new FileInputStream("in.xml"), null);
            pm.makePersistent(user);
            pm.currentTransaction().commit();

            // marshal the same graph back to XML
            pm.currentTransaction().begin();
            mctx = bfact.createMarshallingContext();
            FileOutputStream out = new FileOutputStream("out.xml");
            mctx.marshalDocument(user, "UTF-8", null, out);
            out.close();
            pm.currentTransaction().commit();

            // read it back in again
            pm.currentTransaction().begin();
            uctx = bfact.createUnmarshallingContext();
            uctx.unmarshalDocument(new FileInputStream("out.xml"), null);
            pm.currentTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        Sys.shutdown();
    }

}
