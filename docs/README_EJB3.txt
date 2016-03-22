Versant Open Access JDO @JDO.VERSION@ (@JDO.VERSION.DATE@)

This release includes alpha level support for EJB 3 persistence (JSR220).

Features
--------
* Use the EJB 3 (JSR220) and JDO API (JSR243) in the same application.
* Persistent classes may be JDO POJO's or EJB 3 entities and may be mixed
  in the same model (they can reference each other etc.).
* Eclipse Plugin with live visual mapping editor.

Limitations
-----------
* Limited implementation of EJBQL.
* Limited annotation processing.
* Tools only edit meta data stored in XML and do not edit source code.
* Composite primary key not supported.

We are putting a lot of resources into our EJB 3 effort to address these
limitations.


Obtaining an EntityManagerFactory
---------------------------------
VersantPersistenceManagerFactory pmf =
                (VersantPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(
                    getProperties());
EntityManagerFactory emf = (EntityManagerFactory) pmf.getEntityManagerFactory();

Obtaining an EntityManager
--------------------------
See obtaining an EntityManagerFactory.

EntityManager em = emf.getEntityManager();


Using EJBQL from a JDO PersistenceManager
-----------------------------------------

Specify "EJBQL" as the query language when creating the query:

PersistenceManager pm = ...
Query q = pm.newQuery("EJBQL",
        "SELECT o FROM Order o " +
        "WHERE o.shippingAddress.state = ?1 " +
        "  AND o.shippingAddress.city = ?2");
Collection ans = (Collection)q.execute("CA", "Fremont");
for (Iterator i = ans.iterator(); i.hasNext(); ) {
    Order o = (Order)i.next();
    ...
}

Named parameters can be used but they are treated as positional parameters
i.e. the parameter values must be supplied in the same order as the parameters
appear in the query.
