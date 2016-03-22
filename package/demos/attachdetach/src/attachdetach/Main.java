
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
package attachdetach;

import attachdetach.model.Contact;

import javax.jdo.JDOUserException;
import javax.jdo.Query;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import com.versant.core.jdo.VersantPersistenceManager;

/**
 * Detach a graph, serialize to a file, de-serialize, change it and reattach.
 * This simulates sending a detached graph to another tier, changing it there
 * and sending it back to the server.
 */
public class Main {

    public static void main(String[] args) {
        try {
            VersantPersistenceManager pm = (VersantPersistenceManager)Sys.pm();

            // look up a Contact and detach it
            Query q = pm.newQuery(Contact.class, "name == p");
            q.declareParameters("String p");
            Collection ans = (Collection)q.execute("david");
            Collection detached = pm.versantDetachCopy(ans, "demo");
            q.closeAll();

            // The demo fetch group includes all of the fields of Contact
            // but not the Country field from Contact.address

            // serialize it to a byte buffer
            ByteArrayOutputStream obuf = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(obuf);
            out.writeObject(detached);
            out.close();

            // Attempt to navigate to the address field. This triggers an
            // exception as this field is not in the detached graph
            Contact david = (Contact)detached.iterator().next();
            try {
                System.out.println("david.getAddress().getCountry() = " +
                        david.getAddress().getCountry());
            } catch (JDOUserException e) {
                System.out.println(e);
            }

            // close the PM and get a new one
            Sys.cleanup();
            pm = (VersantPersistenceManager)Sys.pm();

            // read the Collection back in again
            ByteArrayInputStream ibuf = new ByteArrayInputStream(
                    obuf.toByteArray());
            ObjectInputStream in = new ObjectInputStream(ibuf);
            Collection toAttach = (Collection)in.readObject();
            in.close();

            // change the first Contact
            Contact con = (Contact)toAttach.iterator().next();
            con.setPhone("555-9999");

            // start a tx and attach the graph - the phone number change
            // will be persisted
            pm.currentTransaction().begin();
            pm.versantAttachCopy(toAttach, true);
            pm.currentTransaction().commit();

            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

}

