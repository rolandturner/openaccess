
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

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.PersistenceInfo;
import javax.sql.DataSource;

import com.versant.persistence.SchemaContext;
import com.versant.persistence.XmlException;
import com.versant.persistence.ems.*;

/**
 * @author Rick George
 *
 */
public class EntityMappingsXml implements PersistenceInfo {
	private SchemaContext<EntityMappings> mappingSchemaContext = null;

	private EntityMappings entityMappings = new EntityMappings();
	
	/**
	 * Construct a <code>PersistenceXml</code> object from
	 * a give <code>InputStream</code>.
	 * @param inputStream that contains the persistence.xml
	 */
	public EntityMappingsXml(InputStream inputStream, ClassLoader loader) {
		this(loader);
		load(inputStream);
	}
	
	public EntityMappingsXml(URL url, ClassLoader loader) {
		this(loader);
		InputStream inputStream = null;
		try {
			inputStream = url.openStream();
			load(inputStream);
		}
		catch (IOException e) {
			e.printStackTrace();			
		}
		finally {
			this.close(inputStream);
		}
	}
	
    private void close(Closeable stream) { 
		try {
			if (stream != null) {
				stream.close();
			}
		}
		catch (IOException e) {
		}
	}
    
    public void add(com.versant.persistence.ems.Entity entity) {
    	entityMappings.getEntity().add(entity);
    }
    
    public void add(com.versant.persistence.ems.EmbeddableSuperclass es) {
    	entityMappings.getEmbeddableSuperclass().add(es);
    }
    
    public void add(com.versant.persistence.ems.Embeddable embeddable) {
    	entityMappings.getEmbeddable().add(embeddable);
    }
    
    public void store(String mappingFile) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(mappingFile);
        	mappingSchemaContext.marshal(entityMappings, out);
        }
        catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
        catch (XmlException e) {
        	e.printStackTrace();
        }
        finally {
        	close(out);
        }
    }
    
	public void load(InputStream inputStream) {
		try {
			this.entityMappings = 
					mappingSchemaContext.unmarshal(inputStream);
		}
		catch (XmlException e) {
			e.printStackTrace();			
		}
	}
	
	public EntityMappingsXml(ClassLoader loader) {
		mappingSchemaContext = new SchemaContext<EntityMappings>(
			"com.versant.persistence.ems", loader);
	}
	
	public EntityMappings getEntityMappings() {
		return entityMappings;
	}

	public void setEntityMappings(EntityMappings entityMappings) {
		this.entityMappings = entityMappings;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getEntityManagerName()
	 */
	public String getEntityManagerName() {
		return entityMappings.getPackage();
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getPersistenceProviderClassName()
	 */
	public String getPersistenceProviderClassName() {
		return com.versant.persistence.PersistenceProvider.class.getName();
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getJtaDataSource()
	 */
	public DataSource getJtaDataSource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getNonJtaDataSource()
	 */
	public DataSource getNonJtaDataSource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getMappingFileNames()
	 */
	public List<String> getMappingFileNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getJarFiles()
	 */
	public List<URL> getJarFiles() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getEntityClassNames()
	 */
	public List<String> getEntityClassNames() {
		List<String> result = new ArrayList<String>();
		for (com.versant.persistence.ems.Entity e :
							entityMappings.getEntity()) {
			String className = e.getClassName();
			result.add(className);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getProperties()
	 */
	public Properties getProperties() {
		return new Properties();
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getClassLoader()
	 */
	public ClassLoader getClassLoader() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getPersistenceXmlFileUrl()
	 */
	public URL getPersistenceXmlFileUrl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.persistence.spi.PersistenceInfo#getEntityMappingsXmlFileUrl()
	 */
	public URL getEntityMappingsXmlFileUrl() {
		return null;
	}

}
