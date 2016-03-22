
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
package petstore.model;

import petstore.db.*;
import petstore.www.ActionFormUtils;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;



/**
 */
public class CatalogDelegate {

    private PersistenceManager pm;

    public CatalogDelegate(PersistenceManager pm) {
        this.pm = pm;
    }


    public void populateCatalog() {
        try {

            // BIRDS
            Category category = new Category("BI7","Birds",
                    "Flying fowls");
            pm.makePersistent(category);

            Item item = new Item(category,
                    "AB-CV-01",
                    "Amazon Parrot",
                    "Great companion for up to 75 years",
                    Item.STATUS_ACTIVE,
                    "/images/bird4.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);

            item = new Item(category,
                    "AB-SB-02",
                    "Finch",
                    "Great stress reliever",
                    Item.STATUS_ACTIVE,
                    "/images/bird1.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);


            // CATS
            category = new Category("C21","Cats","Every spinster's best friend");
            pm.makePersistent(category);

            item = new Item(category,
                    "FL-DSH-01",
                    "Manx",
                    "Friendly house cat, doubles as a princess",
                    Item.STATUS_ACTIVE,
                    "/images/cat3.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);


            // DOGS
            category = new Category("DI56","Dogs","Man's best friend");
            category.setDescription("Dogs");
            pm.makePersistent(category);

            item = new Item(category,
                    "K9-BD-01",
                    "Bulldog",
                    "Friendly dog from England",
                    Item.STATUS_ACTIVE,
                    "/images/dog2.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);

            item = new Item(category,
                    "K9-CW-01",
                    "Chihuahua",
                    "Great companion dog",
                    Item.STATUS_ACTIVE,
                    "/images/dog4.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);

            item = new Item(category,
                    "K9-DL-01",
                    "Dalmation",
                    "Great dog for a Fire Station",
                    Item.STATUS_ACTIVE,
                    "/images/dog5.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);

            item = new Item(category,
                    "K9-RT-01",
                    "Golden Retriever",
                    "Great family dog",
                    Item.STATUS_ACTIVE,
                    "/images/dog1.gif",
                    "","","","","",
                    10.10,8.50);
            pm.makePersistent(item);

            pm.currentTransaction().commit();
        } catch (Exception e) {
            try {pm.currentTransaction().rollback();} catch (Exception e1) {}
            throw ExceptionUtil.createSystemException(e);
        } finally {
            try {pm.currentTransaction().begin();} catch (Exception e1) {}
        }
    }

    public Page getCategories(int start, int count) {
        Query q = null;
        try {
            q = pm.newQuery();
//            q.setCandidates(new DummyExtent(Category.class, false));
            q.setClass(Category.class);
            q.setIgnoreCache(true);
            Collection col = (Collection) q.execute();

            ArrayList list = new ArrayList();
            ArrayList oids = new ArrayList();
            Iterator itr = col.iterator();
            int idx = start;
            while (idx-- > 0 && itr.hasNext()) {
                itr.next();
            }
            while (count-- > 0 && itr.hasNext()) {
                Category cat = (Category) itr.next();
                list.add(cat);
                oids.add(getStringObjectID(cat));
            }

            return new Page(list, oids, start, itr.hasNext());
        } finally {
            if (q!= null) try {q.closeAll();} catch (Exception e) {}
        }
    }

    public Page getItems(String categoryId, int start, int count) {
        Query q = null;
        try {
            Category category = getCategory(categoryId);
            q = pm.newQuery();
            q.setClass(Item.class);
            q.declareParameters("Object cat,String stat");
            q.setFilter("category == cat && status == stat");
            q.setOrdering("name ascending");
            q.setIgnoreCache(true);
            Collection col = (Collection) q.execute(category, Item.STATUS_ACTIVE);

            ArrayList list = new ArrayList();
            ArrayList oids = new ArrayList();
            Iterator itr = col.iterator();
            int idx = start;
            while (idx-- > 0 && itr.hasNext()) {
                itr.next();

            }
            Item item;
            while (count-- > 0 && itr.hasNext()) {
                item = (Item) itr.next();
                list.add(item);
                oids.add(getStringObjectID(item));
            }

            return new Page(list, oids, start, itr.hasNext());
        } finally {
            if (q!= null) try {q.closeAll();} catch (Exception e) {}
        }

    }

    public Category getCategory(String categoryId) {

        Object objectId = pm.newObjectIdInstance(Category.class, categoryId);
        return (Category) pm.getObjectById(objectId, true);

    }

    public Item getItem(String itemId) {

        Object objectId = pm.newObjectIdInstance(Item.class, itemId);
        return (Item) pm.getObjectById(objectId, true);

    }

    public String getStringObjectID(Object object) {
        return pm.getObjectId(object).toString();
    }

    public static void main(String[] args) {
        try {
            JDOSupport.init();
            PersistenceManager pm = JDOSupport.getInstance().getPMFactory().getPersistenceManager();
            pm.currentTransaction().begin();
            CatalogDelegate catalog = new CatalogDelegate(pm);
            catalog.populateCatalog();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

}
