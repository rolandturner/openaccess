
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
 * 
 */
package javax.persistence;

import java.net.URL;
import java.util.*;

import javax.persistence.spi.*;
import javax.sql.DataSource;

/**
 * Maintains a list of .par files and persistence.xml files.
 * @author Rick George
 * @version 1.0, 8/15/05
 * @see javax.persistence.ParFile
 * @see javax.persistence.PersistenceXml
 * @see javax.persistence.spi.PersistenceInfo
 */
public class PersistenceBundle implements 
							javax.persistence.spi.PersistenceInfo {
	/**
	 * A list of <code>PersistenceInfo</code> objects that 
	 * can be either <code>ParFile</code> or <code>
	 * PersistenceXml</code> objects.
	 */
	List<PersistenceInfo> persistenceInfoList =
						new ArrayList<PersistenceInfo>();
    private ClassLoader classLoader;
	
	public PersistenceBundle(ClassLoader loader) {
        super();
        classLoader = loader;
    }

    /**
	 * Adds a persistence.xml object to the <code>PersistenceInfo</code>
	 * list maintained by this <code>PersistenceBundle</code>.
	 * @param persistenceXml to add to this <code>PersistenceBundle</code>.
	 */
	public void add(PersistenceXml persistenceXml) {
		persistenceInfoList.add(persistenceXml);
	}
	
    /**
	 * Adds a entity-mappings.xml object to the <code>PersistenceInfo</code>
	 * list maintained by this <code>PersistenceBundle</code>.
	 * @param entityMappingsXml to add to this <code>PersistenceBundle</code>.
	 */
	public void add(EntityMappingsXml entityMappingsXml) {
		persistenceInfoList.add(entityMappingsXml);
	}
	
	/**
	 * Checks to see if there are multiple persistence units
	 * in this <code>PersistenceBundle</code>.
	 * @return <code>true</code> if multiple persistence units;
	 * <code>false</code> otherwise.
	 */
	public boolean hasMultiplePersistenceUnits() {
		Set<String> units = new HashSet<String>();
		for (PersistenceInfo pi : persistenceInfoList) {
	       String name = pi.getEntityManagerName();
	       units.add(name);
		}
		return units.size() > 1;
	}
	
	/**
	 * Returns a list of all properties associated with this
	 * <code>PersistenceBundle</code>.
	 * @return <code>Properties</code> found in this 
	 * <code>PersistenceBundle</code>.
	 */
	public Properties getProperties() {
		Properties result = new Properties();
		for (PersistenceInfo pi : persistenceInfoList) {
		    Properties properties = pi.getProperties();
		    result.putAll(properties);
		}
		return result;
	}
	
	/**
	 * Returns a list of  properties associated with a
	 * specific persistence unit.
	 * @return <code>Properties</code> found in this 
	 * <code>PersistenceBundle</code>.
	 */
	public Properties getProperties(String emName) {
		Properties result = new Properties();
		for (PersistenceInfo pi : persistenceInfoList) {
	       String entityManagerName = pi.getEntityManagerName();
	       if (emName.equals(entityManagerName)) {
			    Properties properties = pi.getProperties();
			    result.putAll(properties);
	       }
		}
		return result;
	}
	
	/**
	 * Adds a <code>ParFile</code> object to the <code>PersistenceInfo</code>
	 * list maintained by this <code>PersistenceBundle</code>.
	 * @param ParFile to add to this <code>PersistenceBundle</code>.
	 */
	public void add(ParFile parFile) {
		persistenceInfoList.add(parFile);
	}

	/**
	 * @return The list of mapping file names that the persistence
	 * provider must load to determine the mappings for the entity
	 * classes. The mapping files must be in the standard XML
	 * mapping format, be uniquely named and be resource-loadable
	 * from the application classpath. This list will not include
	 * the entity-mappings.xml file if one was specified.
	 * Each mapping file name corresponds to a <mapping-file>
	 * element in persistence.xml
	 */
	public List<String> getMappingFileNames() {
		List<String> result = new ArrayList<String>();
		for (PersistenceInfo pi : persistenceInfoList) {
	       List<String> mappingFileNames = pi.getMappingFileNames();
	       result.addAll(mappingFileNames);       
		}
		return result;
	}

	/**
	* @return The list of JAR file URLs that the persistence
	* provider must look in to find the entity classes that must
	* be managed by EntityManagers of this name. The persistence
	* archive jar itself will always be the last entry in the
	* list. Each jar file URL corresponds to a named <jar-file>
	* element in persistence.xml
	*/
	public List<URL> getJarFiles() {
		List<URL> result = new ArrayList<URL>();
		for (PersistenceInfo pi : persistenceInfoList) {
	       List<URL> jarFiles = pi.getJarFiles();
	       result.addAll(jarFiles);       
		}
		return result;
	}

	/**
	* @return The list of class names that the persistence
	* provider must inspect to see if it should add it to its
	* set of managed entity classes that must be managed by
	* EntityManagers of this name.
	* Each class name corresponds to a named <class> element
	* in persistence.xml
	*/
	public List<String> getEntityClassNames() {
		Set<String> result = new HashSet<String>();
		for (PersistenceInfo pi : persistenceInfoList) {
			List<String> entityClassNames = pi.getEntityClassNames();
			result.addAll(entityClassNames);
		}
		return new ArrayList<String>(result);
	}

	public String getEntityManagerName() {
		String result = "";
		for (PersistenceInfo pi : persistenceInfoList) {
			if (result.length() != 0)
				result += ":";
			result += pi.getEntityManagerName();
		}
		return result;
	}

	public String getPersistenceProviderClassName() {
		return null;
	}

	public DataSource getJtaDataSource() {
		return null;
	}

	public DataSource getNonJtaDataSource() {
		return null;
	}

	public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        if(classLoader == null){
            classLoader = PersistenceBundle.class.getClassLoader();
        }
		return classLoader;
	}

	URL persistenceXmlFileUrl;

	public URL getPersistenceXmlFileUrl() {
		return persistenceXmlFileUrl;
	}
	
	public void setPersistenceXmlFileUrl(URL url) {
		persistenceXmlFileUrl = url;
	}
	
	URL entityMappingsXmlFileUrl;

	public URL getEntityMappingsXmlFileUrl() {
		return entityMappingsXmlFileUrl;
	}

	public void setEntityMappingsXmlFileUrl(URL url) {
		entityMappingsXmlFileUrl = url;
	}
}
