
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

import inheritance.model.*;

import javax.jdo.PersistenceManager;

public class InsertData {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pm();

            pm.currentTransaction().begin();
            pm.makePersistent(new Pet("fish"));
            pm.makePersistent(new Dog("mongrel", true));
            pm.makePersistent(new WienerDog("sausage", true, 95));
            pm.makePersistent(new Rottweiler("fang", false, 10));
            pm.makePersistent(new Cat("dogfood", 9));
            pm.makePersistent(new Sheep("soft"));
            pm.makePersistent(new Cow(10, 345));
            pm.currentTransaction().commit();

            Sys.cleanup();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        Sys.shutdown();
    }

}
