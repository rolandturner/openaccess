
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
package enums;

import enums.model.Product;
import enums.model.ProductType;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

public class InsertData {

    public static void main(String[] args) {
        try {
            PersistenceManagerFactory pmf =
                    JDOHelper.getPersistenceManagerFactory(loadProperties());
            PersistenceManager pm = pmf.getPersistenceManager();

            // create some Product's
            pm.currentTransaction().begin();
            pm.makePersistent(new Product("Blue Widget", ProductType.STOCK));
            pm.makePersistent(new Product("Red Widget", ProductType.NON_STOCK));
            pm.makePersistent(new Product("Green Widget", ProductType.STOCK));
            pm.currentTransaction().commit();

            // list all the stock products
            pm.currentTransaction().begin();
            Query q = pm.newQuery(Product.class, "type == t");
            q.declareParameters("ProductType t");
            Collection ans = (Collection)q.execute(ProductType.STOCK.getType());
            for (Iterator i = ans.iterator(); i.hasNext(); ) {
                Product p = (Product)i.next();
                System.out.println("p = " + p);
            }
            q.closeAll();
            pm.currentTransaction().commit();

            pm.close();
            pmf.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = InsertData.class.getResourceAsStream("/versant.properties");
            if (in == null) throw new IOException("versant.properties not on classpath");
            p.load(in);
        } finally {
            if (in != null) in.close();
        }
        return p;
    }

}
