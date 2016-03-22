
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
package com.versant.core.jdo.junit.test1;

import java.util.*;
import javax.jdo.*;
import com.versant.core.jdo.junit.test1.model.QueryTest1;
import java.io.*;




public class TestQueries

{
   private static final String PROJECT_FILE = "test.jdogenie";

	public TestQueries ()
	{

	}


	public void setUp ()
	{

		PersistenceManager pm = getPM ();
		pm.currentTransaction ().begin ();
		for (int i = 0; i <= 100; i++)
		{
			QueryTest1 ob = new QueryTest1 ();
			ob.setNum (i);
//			System.out.println("persisting : " + i + " instance");
			pm.makePersistent (ob);
		}
		pm.currentTransaction ().commit ();
                pm.close();

		for (int i = 0; i < 100; i++)
		  {
           System.out.println("querying : " + i);
           pm = getPM();
           pm.currentTransaction().setOptimistic(false);
           pm.currentTransaction ().begin ();
           Query q = pm.newQuery (QueryTest1.class, "num == " + i);
           q.execute();
           
           pm.currentTransaction ().commit ();
           pm.close();
		 }
	}

		public PersistenceManager getPM ()
			{
				PersistenceManagerFactory fact = GetPMFactory ();
				PersistenceManager pm = fact.getPersistenceManager ();

				return pm;

		}


		public PersistenceManagerFactory GetPMFactory()
		  {


				// setup the persistence manager factory
			System.out.println("get factory");
			PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory(loadProperties());
			System.out.println("got factory");


			return factory;
		   }
		private static Properties _config;
		  private static Properties loadProperties() {
		      if (_config!=null) return _config;
			  System.out.println("load prop from " + PROJECT_FILE);
			  _config = new Properties();
		        InputStream is = Thread.currentThread()
		                               .getContextClassLoader()
		                               .getResourceAsStream(PROJECT_FILE);
		        if (is == null) {
		            throw new RuntimeException("Resource not found: " + PROJECT_FILE);
		        }
		        try {
		            _config.load(is);
		            is.close();
		           // p.store(System.out,"prop loaded");
		            return _config;
		        } catch (IOException e) {
					System.out.println("ex : " +e);
		            throw new RuntimeException(e.toString(), e);
		        }

		    }




	public static void main (String [] args)
	{
	  TestQueries m = new TestQueries();
	  m.setUp();
	  System.exit(0);
   }
}

