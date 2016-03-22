
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

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.*;

import java.util.regex.*;

/**
 * Represents a persistence.xml file.
 * @author Rick George
 * @version 1.0, 8/15/05
 * @see javax.persistence.spi.PersistenceInfo
 */
public class PersistenceXml implements
					javax.persistence.spi.PersistenceInfo {
	private String entityManagerName = "";
	private String entityManagerProvider = "";
	private String jtaDataSource = "";
	private List<String> mappingFileList = new ArrayList<String>();
	private List<URL> jarFileList = new ArrayList<URL>();
	private List<String> classList = new ArrayList<String>();
	private Properties properties = new Properties();
	
	/**
	 * Pattern for validating class names.
	 */
	private static Pattern classNamePattern = null;

	/**
	 * Returns the name of the entity manager for this 
	 * persistence.xml file.
	 * @return the entity manager name.
	 */
	public String getEntityManagerName(){
		return new String(entityManagerName);
	}
	
	/**
	 * Sets the name of the entity manager for this 
	 * persistence unit.
	 */
	public void setEntityManagerName(String name){
		this.entityManagerName = new String(name);
	}
	
	/**
	 * Returns the name of the entity manager provider for this par file.
	 * @return the entity manager provider name.
	 */
	public String getProvider(){
		return new String(entityManagerProvider);
	}
	
	/**
	 * Returns the jta data source for the persistence unit.
	 * @return the name of the jta data source.
	 */
	public String getJtaDataSourceName(){
		return new String(jtaDataSource);
	}
	
	/**
	 * Returns a list of mapping file names found in the par file.
	 * @return the mapping files for the par.
	 */
	public List<String> getMappingFiles() {
		return new ArrayList<String>(mappingFileList);
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
		return new ArrayList<URL>(jarFileList);
	}
	
	/**
	 * Returns a list of class names found in the par file.
	 * @return the class names listed in the par file.
	 */
	public List<String> getClasses() {
		return new ArrayList<String>(classList);
	}
	
	/**
	 * Returns a list of properties found in the par file.
	 * @return the properties listed in the par file.
	 */
	public Properties getProperties() {
		Properties result = new Properties();
		result.putAll(properties);
		return result;
	}
	
	/**
	 * @return The name of the persistence provider implementation
	 * class.
	 * Corresponds to the <provider> element in persistence.xml
	 */
	public String getPersistenceProviderClassName() {
		return new String(entityManagerProvider);
	}

	/**
	 * @return the JTA-enabled data source to be used by the
	 * persistence provider.
	 * The data source corresponds to the named <jta-data-source>
	 * element in persistence.xml
	 */
	public DataSource getJtaDataSource() {
		return null;
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
		return null;
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
		return new ArrayList<String>(mappingFileList);
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
		return new ArrayList<String>(classList);
	}

	/**
	* @return ClassLoader that the provider may use to load any
	* classes, resources, or open URLs.
	*/
	public ClassLoader getClassLoader() {
		return PersistenceXml.class.getClassLoader();
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
	 * Construct a <code>PersistenceXml</code> object from
	 * a give <code>InputStream</code>.
	 * @param inputStream that contains the persistence.xml
	 */
	public PersistenceXml(InputStream inputStream) {
		this();
		try {
			parseWithDefaultXmlParser(inputStream);
		}
		catch(IOException e) {
			
		}
        properties.put("versant.jdbcNamegen","com.versant.core.jdbc.sql.Ejb3JdbcNameGenerator");
	}
	
	/**
	 * Null constructor.
	 */
	public PersistenceXml() {
		if (classNamePattern == null) {
			/**
			 * Compile class name validation pattern.
			 */
			classNamePattern = Pattern.compile(
					"([a-zA-Z_](\\w)*\\.)*[a-zA-Z_](\\w)*");		
		}
	};
	
    /**
     * Uses the default xml parser to parse the persistence.xml 
     * file.
     * @throws IOException if an I/O error has occurred
     */
	private void parseWithDefaultXmlParser(InputStream inputStream) throws IOException {
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(inputStream);
	        entityManagerName = this.getElementByTagName(doc, "name");
	        entityManagerProvider = this.getElementByTagName(doc, "provider");
	        jtaDataSource = this.getElementByTagName(doc, "jta-data-source");
	        mappingFileList = this.getElementsByTagName(doc, "mapping-file");
	        jarFileList = this.getURLsByTagName(doc, "jar-file");
	        classList = this.getElementsByTagName(doc, "class", classNamePattern);
	        NodeList propertyNodes = doc.getElementsByTagName("property");
	        for (int i = 0; i < propertyNodes.getLength(); i++){
	        	Node item = propertyNodes.item(i);
	        	NamedNodeMap attrs = item.getAttributes();
	        	Node nameNode = attrs.getNamedItem("name");
	        	Node valueNode = attrs.getNamedItem("value");
	        	String name = nameNode.getNodeValue();
	        	String value = valueNode.getNodeValue();
	        	properties.put(name, value);
	        }
		}
		catch (SAXException e){
			
		}
		catch (ParserConfigurationException e){
			
		}
	}
	
	/**
	 * Returns the <code>String</code> for the given tag name
	 * and an empty string <code>""</code> if not found.
	 * @param doc the <code>org.w3c.dom.Document</code> to search
	 * @param tagName the name of the xml tag to find
	 * @return the <code>String</code> for the given tag name
	 */
	private String getElementByTagName(Document doc, String tagName){
		String result = "";
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() != 0) {
	        Node node = nodeList.item(0);
	        if (node != null) {
	            Node child = node.getFirstChild();
	            if (child != null) {
	                result = child.getNodeValue();
	            }
	        }
        }
        return result;
	}
	
	/**
	 * Returns a list of strings <code>List</code> for the given 
	 * tag name and an empty list if the tag is not found.
	 * @param doc the <code>org.w3c.dom.Document</code> to search
	 * @param tagName the name of the xml tag to find
	 * @return the list of strings <code>List<String></code> 
	 * for the given tag name
	 */
	private List<String> getElementsByTagName(Document doc, String tagName){
		List<String> result = new ArrayList<String>();
        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++){
        	Node item = nodes.item(i);
            Node child = item.getFirstChild();
        	String value = child.getNodeValue();
        	result.add(value);
        }
        return result;
	}
	
	/**
	 * Returns a list of strings <code>List</code> for the given 
	 * tag name and an empty list if the tag is not found.
	 * @param doc the <code>org.w3c.dom.Document</code> to search
	 * @param tagName the name of the xml tag to find
     * @throws IllegalArgumentException if an invalid element is found.
	 * @return the list of strings <code>List<String></code> 
	 * for the given tag name
	 */
	private List<String> getElementsByTagName(Document doc, 
						String tagName, Pattern validationPattern){
		List<String> result = new ArrayList<String>();
        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++){
        	Node item = nodes.item(i);
            Node child = item.getFirstChild();
        	String value = child.getNodeValue();
        	if (!validationPattern.matcher(value).matches()) {
        		throw new IllegalArgumentException(
        				"Invalid " + tagName + " '" +
        				value + "' in persistence.xml");
        	}
        	result.add(value);
        }
        return result;
	}
	
	/**
	 * Returns a list of URLs <code>List</code> for the given 
	 * tag name and an empty list if the tag is not found.
	 * @param doc the <code>org.w3c.dom.Document</code> to search
	 * @param tagName the name of the xml tag to find
	 * @return the list of strings <code>List<String></code> 
	 * for the given tag name
	 */
	private List<URL> getURLsByTagName(Document doc, String tagName){
		List<URL> result = new ArrayList<URL>();
        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++){
        	Node item = nodes.item(i);
            Node child = item.getFirstChild();
        	String value = child.getNodeValue();
        	URL url = getURL(value);
        	if (url == null) {
           		url = PersistenceXml.class.getResource(value);
        		if (url == null) {
        			String s = "/" + value;
        			url = PersistenceXml.class.getResource(s);
        		}
        	}
    		if (url != null) {
            	result.add(url);
    		}
        }
        return result;
	}
	
	/**
	 * Returns a <code>URL</code> given a <code>String</code> or
	 * <code>null</code> if the string passed in is malformed.
	 * @param s the <code>String</code> used to construct a <code>URL</code>
	 * @return a valid <code>URL</code> or <code>null</code> if
	 * the input string is malformed.
	 */
	private URL getURL(String s) {
		URL result = null;
    	try {
    		result = new URL(s);
    	}
    	catch (MalformedURLException e) {
    	}
		return result;
	}
    	
}
