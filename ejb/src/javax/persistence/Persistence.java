
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
/**
 * class: javax.persistence.Persistence
 * 
 * A container-managed entity manager is obtained by the 
 * application through dependency injection or through JNDI 
 * lookup, or by calling EntityManagerFactory.getEntityManager().
 *  
 * The container manages the creation of the entity manager and 
 * handles the closing of the entity manager transparently to the 
 * application.
 * 
 * Entity managers can be injected using the PersistenceContext 
 * annotation. If multiple persistence units exist, the unitName 
 * element must be specified. The type element specifies whether 
 * a transaction-scoped or extended persistence context is to be 
 * used.
 * 
 * For example,
 * 		@PersistenceContext(unitName="order")
 * 		EntityManager em;
 * 		
 * 		//here only one persistence unit exists
 * 		@PersistenceContext(type=PersistenceContextType.EXTENDED)
 * 		EntityManager orderEM;
 * 
 * The JNDI lookup of an entity manager is illustrated below:
 * 		@Stateless
 * 		@PersistenceContext(name="OrderEM", unitName="Order")
 * 		public class MySessionBean implements MyInterface {
 * 			@Resource SessionContext ctx;
 * 			public void doSomething() {
 * 				EntityManager em = (EntityManager)
 * 				ctx.lookup("OrderEM");
 * 				...
 * 			}
 * 		}
 * 
 * Outside a J2EE container environment, the Persistence class 
 * is the bootstrap class that provides access to an entity 
 * manager factory. 
 * 
 * The application creates an entity manager factory by calling 
 * the createEntityManagerFactory method of the Persistence class.
 * 
 * No name needs to be specified in the case where only one
 * persistence unit exists in the application. 
 * 
 * If a name is not passed, but multiple persistence units exist, 
 * a PersistenceException is thrown.
 */
package javax.persistence;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Rick George
 * @version 1.1, 8/15/05
 * @see javax.persistence.EntityManagerFactory
 */
public class Persistence {
	public static final String INFO_KEY = "persistence.info";
	/**
	 * The bootstrap function that provides access to an entity 
	 * manager factory for an application outside a J2EE container.
	 * No name needs to be specified in the case where only one 
	 * persistence unit exists in the application.
	 * @throws PersistenceException if a name is not passed and
	 * 			and multple persistence units exist
	 */
	public static EntityManagerFactory createEntityManagerFactory()
	 						throws PersistenceException {
		EntityManagerFactory result = null;
		try {
			PersistenceBundle persistenceBundle = createPersistenceBundle(Persistence.class.getClassLoader());
			if (persistenceBundle.hasMultiplePersistenceUnits()) {
				throw new PersistenceException(
						"The name of a persistence unit is required " +
							"when multiple persistence units exist.");
			}
			Properties props = persistenceBundle.getProperties();
	 	   	props.put(INFO_KEY, persistenceBundle);
			result = com.versant.core.ejb.EntityManagerFactoryImp.getEntityManagerFactory(props);
		}
		catch (IOException e) {
			throw new PersistenceException(e);
		}
		return result;
	} 
	
	/** 
	 * The bootstrap function for applications that provides 
	 * access to an entity manager factory where multiple 
	 * persistence units exist.
	 * For example,
	 * 		EntityManagerFactory emf = javax.persistence.Persistence
	 * 				.createEntityManagerFactory("Order");
	 * 		EntityManager em = emf.createEntityManager();
	 */
	public static EntityManagerFactory createEntityManagerFactory(
													String unit)
							throws PersistenceException {
		EntityManagerFactory result = null;
		try {
			PersistenceBundle persistenceBundle  = createPersistenceBundle(Persistence.class.getClassLoader());
			Properties props = persistenceBundle.getProperties(unit);
	 	   	props.put(INFO_KEY, persistenceBundle);
			com.versant.persistence.PersistenceProvider provider = 
						new com.versant.persistence.PersistenceProvider();
			result = provider.createEntityManagerFactory(unit, props);
		}
		catch (IOException e) {
			throw new PersistenceException(e);
		}
		return result; 
	}
	
	/**
	 * Add persistence info collected from persistence.xml and .par
	 * files located on the class path.
	 * @param props The <code>Properties</code> object that the
	 * persistence info will be added to.
	 */
	public static void addPersistenceInfo(Properties props, ClassLoader classLoader) {
		try {
			PersistenceBundle persistenceBundle = 
									createPersistenceBundle(classLoader);
			Properties persistenceProps = 
								persistenceBundle.getProperties();
			props.putAll(persistenceProps);
			props.put(INFO_KEY, persistenceBundle);
			List<String> entityClassNames = 
						persistenceBundle.getEntityClassNames();
			props.put("entity.class.names", 
									entityClassNames.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a <code>PersistenceBundle</code> and add persistence
	 * info from persistence.xml and .par files found on the class path.
	 * @return <code>PersistenceBundle</code> that holds all of the
	 * persistence info found in the persistence files.
	 * @throws IOException
	 */
	private static PersistenceBundle createPersistenceBundle(ClassLoader classLoader) 
									throws IOException {
		PersistenceBundle persistenceBundle = new PersistenceBundle(classLoader);
		loadPersistenceInfo(persistenceBundle);
		return persistenceBundle;
	}
	
	/**
	 * Search for all .par files located on the class path.
	 * @return a list of .par file names located on the class path.
	 */
	private static List<String> getParFiles() {
		List<String> result = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path");
		String fileSeparator = System.getProperty("file.separator");
		String pathSeparator = System.getProperty("path.separator");
		StringTokenizer parser = new StringTokenizer(
										classPath, pathSeparator);
		while (parser.hasMoreTokens()) {
			String filename = parser.nextToken();
			File file = new File(filename);
			if (!file.isDirectory()) {
				if (filename.endsWith(".par")) {
					result.add(filename);						
				}
			}
			else {
				ParFilter parFilter = new ParFilter();
				String[] list = file.list(parFilter);
				if (list != null) {
					for (String s : list) {
						result.add(filename +
								fileSeparator + s);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Load the properties from the persistence.xml file and any
	 * .par files that exist on the class path.
	 * @throws IOException
	 */
	private static void loadPersistenceInfo(
			PersistenceBundle persistenceBundle) throws IOException {
	    loadPersistenceXml(persistenceBundle);
	    addEntityMappingsXml(persistenceBundle);
	    List<String> parFiles = getParFiles();
	    for (String filename : parFiles) {
	    	ParFile parFile = new ParFile(filename);
	    	persistenceBundle.add(parFile);
	    }
	}
	
	private static void addEntityMappingsXml(
			PersistenceBundle persistenceBundle) throws IOException {
		String value = "entity-mappings.xml";
		ClassLoader loader = persistenceBundle.getClassLoader();
    	URL url = getURL(value);
    	if (url == null) {
       		url = loader.getResource(value);
    		if (url == null) {
    			String s = "/" + value;
    			url = loader.getResource(s);
    		}
    	}
		persistenceBundle.setEntityMappingsXmlFileUrl(url);
	}
	private static URL getURL(String s) {
		URL result = null;
    	try {
    		result = new URL(s);
    	}
    	catch (MalformedURLException e) {
    	}
		return result;
	}
	
	/**
	 * Load information from the persistence.xml file
	 * @throws IOException
	 */
	private static void loadPersistenceXml(
							PersistenceBundle persistenceBundle) 
										throws IOException {
	    InputStream in = null;
	    try {
	       ClassLoader classLoader = 
	    	   					persistenceBundle.getClassLoader();
	       in = findResource("persistence.xml", classLoader);
	       if (in != null) {
	    	   PersistenceXml persistenceXml = new PersistenceXml(in);
	    	   persistenceBundle.add(persistenceXml);
	       }
	    } 
	    finally {
	       if (in != null) in.close();
	    }
	}
	
	private static InputStream findResource(
						String name, ClassLoader classLoader) 
										throws IOException {
		InputStream in = null;
		String[] stack = {"/", "META-INF", "/", null};
		for (String top : stack) {
			in = classLoader.getResourceAsStream(name);
			if (in == null) {
				name = top + name;
			}
			else {
				break;
			}
		}
		return in;
	}
	
}
