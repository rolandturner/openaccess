/**
 * 
 */
package javax.persistence.spi;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * @author Rick George
 *
 * Interface implemented and used by the Container to pass
 * persistence metadata to the persistence provider as part of
 * the createContainerEntityManagerFactory() call. The provider
 * will use this metadata to obtain the mappings and initialize
 * its structures.
 */
public interface PersistenceInfo {

	/**
	 * @return The name of the EntityManager that is being created.
	 * Corresponds to the <name> element in persistence.xml
	 */
	public String getEntityManagerName();
	
	/**
	 * @return The name of the persistence provider implementation
	 * class.
	 * Corresponds to the <provider> element in persistence.xml
	 */
	public String getPersistenceProviderClassName();
	
	/**
	 * @return the JTA-enabled data source to be used by the
	 * persistence provider.
	 * The data source corresponds to the named <jta-data-source>
	 * element in persistence.xml
	 */
	public DataSource getJtaDataSource();
	
	/**
	 * @return The non-JTA-enabled data source to be used by the
	 * persistence provider when outside the container, or inside
	 * the container when accessing data outside the global
	 * transaction.
	 * The data source corresponds to the named <non-jta-data-source>
	 * element in persistence.xml
	 */
	public DataSource getNonJtaDataSource();
	
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
	public List<String> getMappingFileNames();
	
	/**
	* @return The list of JAR file URLs that the persistence
	* provider must look in to find the entity classes that must
	* be managed by EntityManagers of this name. The persistence
	* archive jar itself will always be the last entry in the
	* list. Each jar file URL corresponds to a named <jar-file>
	* element in persistence.xml
	*/
	public List<URL> getJarFiles();
	
	/**
	* @return The list of class names that the persistence
	* provider must inspect to see if it should add it to its
	* set of managed entity classes that must be managed by
	* EntityManagers of this name.
	* Each class name corresponds to a named <class> element
	* in persistence.xml
	*/
	public List<String> getEntityClassNames();
	
	/**
	* @return Properties object that may contain vendor-specific
	* properties contained in the persistence.xml file.
	* Each property corresponds to a <property> element in
	* persistence.xml
	*/
	public Properties getProperties();
	
	/**
	* @return ClassLoader that the provider may use to load any
	* classes, resources, or open URLs.
	*/
	public ClassLoader getClassLoader();
	
	/**
	* @return URL object that points to the persistence.xml
	* file; useful for providers that may need to re-read the
	* persistence.xml file. If no persistence.xml
	* file is present in the persistence archive, null is
	* returned.
	*/
	public URL getPersistenceXmlFileUrl();
	
	/**
	* @return URL object that points to the entity-mappings.xml
	* file.
	* If no entity-mappings.xml file was present in the persistence
	* archive,null is returned.
	*/
	public URL getEntityMappingsXmlFileUrl();
}