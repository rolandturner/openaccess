
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

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.*;
import java.io.*;
import java.net.URL;

import javax.sql.DataSource;

/**
 * Extends <code>JarFile</code> to implement a persistence
 * archive <code>ParFile</code>.
 * @author Rick George
 * @version 1.0, 8/15/05
 * @see java.util.jar.JarFile
 * @see javax.persistence.spi.PersistenceInfo
 */
public class ParFile extends JarFile implements
				javax.persistence.spi.PersistenceInfo {
    /**
     * The persistence.xml file name within the .par file.
     */
    public static final String PERSISTENCE_NAME = 
    									"META-INF/persistence.xml";
    /**
     * The entity-mappings.xml file name within the .par file.
     */
    public static final String ENTITY_MAPPINGS_NAME = 
    									"META-INF/entity-mappings.xml";

    private PersistenceXml persistenceXml = new PersistenceXml();
    private EntityMappingsXml entityMappingsXml;

	/**
	 * Returns the name of the entity manager for this par file.
	 * @return the entity manager name.
	 */
	public String getEntityManagerName(){
		return persistenceXml.getEntityManagerName();
	}
	
	/**
	 * Returns the name of the entity manager provider for this par file.
	 * @return the entity manager provider name.
	 */
	public String getProvider(){
		return persistenceXml.getProvider();
	}
	
	/**
	 * Returns the jta data source for the par file.
	 * @return the name of the jta data source.
	 */
	public String getJtaDataSourceName(){
		return persistenceXml.getJtaDataSourceName();
	}
	
	/**
	 * @return the JTA-enabled data source to be used by the
	 * persistence provider.
	 * The data source corresponds to the named <jta-data-source>
	 * element in persistence.xml
	 */
	public DataSource getJtaDataSource() {
		return persistenceXml.getJtaDataSource();
	}

	/**
	 * Returns a list of mapping file names found in the par file.
	 * @return the mapping files for the par.
	 */
	public List<String> getMappingFiles() {
		return persistenceXml.getMappingFiles();
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
		return persistenceXml.getJarFiles();
	}
	
	/**
	 * Returns a list of class names found in the par file.
	 * @return the class names listed in the par file.
	 */
	public List<String> getClasses() {
		return persistenceXml.getClasses();
	}
	
	/**
	 * Returns a list of properties found in the par file.
	 * @return the properties listed in the par file.
	 */
	public Properties getProperties() {
		return persistenceXml.getProperties();
	}
	
	/**
	 * @return The name of the persistence provider implementation
	 * class.
	 * Corresponds to the <provider> element in persistence.xml
	 */
	public String getPersistenceProviderClassName() {
		return persistenceXml.getPersistenceProviderClassName();
	}

	/**
	 * @return The non-JTA-enabled data source to be used by the
	 * persistence provider when outside the container, or inside
	 * the container when accessing data outside the global
	 * transaction.
	 * The data source corresponds to the named <non-jta-data-source>
	 * element in persistence.xml
	 */
	public DataSource getNonJtaDataSource() {
		return persistenceXml.getNonJtaDataSource();
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
		return persistenceXml.getMappingFileNames();
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
		return persistenceXml.getEntityClassNames();
	}

	/**
	* @return ClassLoader that the provider may use to load any
	* classes, resources, or open URLs.
	*/
	public ClassLoader getClassLoader() {
		return ParFile.class.getClassLoader();
	}

	/**
	* @return URL object that points to the persistence.xml
	* file; useful for providers that may need to re-read the
	* persistence.xml file. If no persistence.xml
	* file is present in the persistence archive, null is
	* returned.
	*/
	public URL getPersistenceXmlFileUrl() {
		return null;
	}

	/**
	* @return URL object that points to the entity-mappings.xml
	* file.
	* If no entity-mappings.xml file was present in the persistence
	* archive,null is returned.
	*/
	public URL getEntityMappingsXmlFileUrl() {
		return null;
	}

    /**
     * Uses the default xml parser to parse the persistence.xml 
     * file.
     * @throws IOException if an I/O error has occurred
     */
	private void loadPersistenceXml() throws IOException {
		JarEntry je = this.getJarEntry(PERSISTENCE_NAME);
		if (je != null) {
			InputStream stream = this.getInputStream(je);
			persistenceXml = new PersistenceXml(stream);
		}
	}
	
	/**
     * Parses a <code>ParFile</code> to find: 
     * <li>entity-manager,
     * <li>entity-manager-name, 
     * <li>provider, 
     * <li>jta-data-source, 
     * <li>mapping-file, 
     * <li>jar-file, 
     * <li>class and
     * <li>property tags
     * <p>Uses the name of the par file for the entity 
     * manager name if no entity-manager-name tag is found 
     * in the persistence.xml file.
     * @throws IOException if an I/O error has occurred
	 */
	private void parse() throws IOException {
		this.loadPersistenceXml();
		this.loadEntityMappingsXml();
		String entityManagerName = 
						persistenceXml.getEntityManagerName();
		if (entityManagerName.length() == 0){
			String path = this.getName();
			File file = new File(path);
			String name = file.getName();
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(0, index);
			}
			persistenceXml.setEntityManagerName(name);
		}
	}
	
	private void loadEntityMappingsXml() throws IOException {
		JarEntry je = this.getJarEntry(ENTITY_MAPPINGS_NAME);
		if (je != null) {
			InputStream in = this.getInputStream(je);
			ClassLoader loader = this.getClassLoader();
			this.entityMappingsXml = new EntityMappingsXml(in, loader);
		}
	}
	
	public EntityMappingsXml getEntityMappingsXml() {
		return this.entityMappingsXml;
	}
	
    /**
     * Creates a new <code>ParFile</code> to read from the specified
     * file <code>name</code>. The <code>ParFile</code> will be verified if
     * it is signed.
     * @param name the name of the jar file to be opened for reading
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if access to the file is denied
     *         by the SecurityManager
     */
	public ParFile(String name) throws IOException {
		super(name);
		parse();
	}

	/**
     * Creates a new <code>ParFile</code> to read from the specified
     * file <code>name</code>.
     * @param name the name of the jar file to be opened for reading
     * @param verify whether or not to verify the jar file if
     * it is signed.
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if access to the file is denied
     *         by the SecurityManager 
     */
	public ParFile(String name, boolean verify) throws IOException {
		super(name, verify);
		parse();
	}

    /**
     * Creates a new <code>ParFile</code> to read from the specified
     * <code>File</code> object. The <code>ParFile</code> will be verified if
     * it is signed.
     * @param file the jar file to be opened for reading
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if access to the file is denied
     *         by the SecurityManager
     */
	public ParFile(File file) throws IOException {
		super(file);
		parse();
	}

    /**
     * Creates a new <code>ParFile</code> to read from the specified
     * <code>File</code> object.
     * @param file the par file to be opened for reading
     * @param verify whether or not to verify the par file if
     * it is signed.
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if access to the file is denied
     *         by the SecurityManager.
     */
	public ParFile(File file, boolean verify) throws IOException {
		super(file, verify);
		parse();
	}

    /**
     * Creates a new <code>ParFile</code> to read from the specified
     * <code>File</code> object in the specified mode.  The mode argument
     * must be either <tt>OPEN_READ</tt> or <tt>OPEN_READ | OPEN_DELETE</tt>.
     *
     * @param file the par file to be opened for reading
     * @param verify whether or not to verify the par file if
     * it is signed.
     * @param mode the mode in which the file is to be opened
     * @throws IOException if an I/O error has occurred
     * @throws IllegalArgumentException
     *         if the <tt>mode</tt> argument is invalid
     * @throws SecurityException if access to the file is denied
     *         by the SecurityManager
     */
	public ParFile(File file, boolean verify, int mode) throws IOException {
		super(file, verify, mode);
		parse();
	}
	
}
