
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
package inheritance;

import inheritance.model.Pet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Iterator;

public class ListPets {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pm();

            // list all the pets
            pm.currentTransaction().begin();
            Query q = pm.newQuery(Pet.class);
            Collection ans = (Collection)q.execute();
            System.out.println("Pets:");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Pet p = (Pet)i.next();
                System.out.println(p.getClass() + " " + p.getName() + " " +
                        p.getNoise());
            }
            q.closeAll();
            System.out.println("---");
            pm.currentTransaction().commit();

            Sys.cleanup();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        Sys.shutdown();
    }

}
