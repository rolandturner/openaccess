
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
package com.versant.core.ejb;

import com.versant.core.metadata.parser.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.persistence.MappingBundle;
import com.versant.persistence.ems.*;

import javax.persistence.EntityMappingsXml;
import javax.persistence.spi.PersistenceInfo;

/**
 * This produces a tree of Jdo objects representing the structure of a
 * JDO meta data file (.jdo) or a <code>PersistenceInfo</code> object. 
 * All of our vendor extensions are valdidated (correct keys) as part 
 * of this process.
 * @author Rick George
 */
public class MetaDataParser extends 
	com.versant.core.metadata.parser.MetaDataParser
{
	ClassLoader loader;
	EntityMappingsXml entityMappingsXml;
	Set<TableGenerator> tableGeneratorSet;
	Set<SequenceGenerator> sequenceGeneratorSet;
	/**
	 * Construct a <code>MetaDataParser</code>.
	 * @param loader the <code>ClassLoader</code> used to load java classes
	 * and file resources.
	 */
	public MetaDataParser(ClassLoader loader) {
		this.loader = loader;
	}
	
    /**
     * Load all the jdoNames as resources using loader and return the JdoRoot's.
     */
    public void parse(Collection jdoNames, List<JdoRoot> jdoRootList) {
         for (Iterator i = jdoNames.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            InputStream in = null;
            try {
                in = loader.getResourceAsStream(
                        name.startsWith("/") ? name.substring(1) : name);
                if (in == null) {
                    throw BindingSupportImpl.getInstance().runtime("Unable to load resource: " +
                            name);
                }
                JdoRoot jdoRoot = parse(in, name);
                jdoRootList.add(jdoRoot);
            } 
            finally {
            	close(in);
            }
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

    
    /**
     * Generate the entity classes from <code>PeristenceInfo</code> and
     * add the results to the <code>jdoRootList</code>. The entity
     * class information that is captured may come from persistence.xml
     * files and mappings xml files described in <code>PersistenceInfo</code>.
     * @param info the <code>PersistenceInfo</code> that describes the
     * entities and their mappings.
     * @param jdoRootList the resulting list of parsed classes and mappings. 
     */
    public void parse(PersistenceInfo info, 
    								List<JdoRoot> jdoRootList) {
    	List<String> entityClassNameList = info.getEntityClassNames();
    	Map<String, List<JdoClass>> packageMap =  
    							new HashMap<String, List<JdoClass>>();
    	Map<Class, EntityClass> entityClassMap = new HashMap<Class, EntityClass>();
    	entityMappingsXml = new EntityMappingsXml(loader);
    	tableGeneratorSet = new HashSet<TableGenerator>();
    	sequenceGeneratorSet = new HashSet<SequenceGenerator>();
    	buildEntityClasses(info, entityClassNameList, entityClassMap);
    	if (entityMappingsXml.getEntityMappings().getEntity().size() != 0)
    		entityMappingsXml.store("gen-orm.xml");
    	createPackageMap(entityClassMap, packageMap);
    	JdoRoot jdoRoot = new JdoRoot();
    	jdoRoot.name = info.getEntityManagerName();
    	if (jdoRoot.name == null || jdoRoot.name.length() == 0)
    		jdoRoot.name = "root";
    	jdoRoot.packages = null;
    	List<JdoPackage> jdoPackageList = new ArrayList<JdoPackage>();
    	for (String packageName : packageMap.keySet()) {
    		JdoPackage jdoPackage = new JdoPackage();
    		jdoPackage.parent = jdoRoot;
    		jdoPackage.name = packageName;
    		jdoPackage.extensions = null;
    		List<JdoClass> classList = packageMap.get(packageName);
    		for (JdoClass c : classList) {
    			c.parent = jdoPackage;
    		}
    		jdoPackage.classes = new JdoClass[classList.size()];
    		classList.toArray(jdoPackage.classes);
    		jdoPackageList.add(jdoPackage);
    	}
    	jdoRoot.packages = new JdoPackage[jdoPackageList.size()];
    	jdoPackageList.toArray(jdoRoot.packages);
    	jdoRootList.add(jdoRoot);
    }
    
    /**
     * Create a map that maps package names to jdo classes.
     * @param entityClassMap the class map used to generate the
     * package map.
     * @param packageMap the <code>Map</code> generated from
     * the supplied classes.
     */
    private void createPackageMap(Map<Class, EntityClass> entityClassMap,
    		Map<String, List<JdoClass>> packageMap) {
    	for (EntityClass entityClass : entityClassMap.values()) {
    		String packageName = entityClass.getPackageName(); 
    		List<JdoClass> jdoClassList = packageMap.get(packageName);
    		if (jdoClassList == null) {
    			jdoClassList = new ArrayList<JdoClass>();
    			packageMap.put(packageName, jdoClassList);
    		}
    		JdoClass jdoClass = entityClass.getJdoClass();
			jdoClassList.add(jdoClass);
     	}
    }
    
    /**
     * Take a <code>PersistenceInfo</code> object and a list of class
     * names and generate a entity class map that maps java classes to
     * entity classes.
     * @param info the <code>PersistenceInfo</code> object that is used
     * to map the classes.
     * @param entityClassNameList a <code>List</code> of class names.
     * @param entityClassMap the resulting <code>Map</code> that maps
     * java classes to entity classes.
     */
    private void buildEntityClasses(PersistenceInfo info,
    		List<String> entityClassNameList, 
    		Map<Class, EntityClass> entityClassMap) {
    	MappingBundle mappingBundle = new MappingBundle(info, loader);
    	createEntityClasses(mappingBundle, entityClassNameList,
    			entityClassMap);
    	setPersistentSuperclasses(entityClassMap);
    	for (EntityClass entityClass : entityClassMap.values()) {
    		entityClass.setInheritance();
    	}
    }
    
    /**
     * Create a set of entity classes from a <code>MappingBundle</code>
     * and a list of class names.
     * @param mappingBundle the <code>MappingBundle</code> that maintains
     * a list of the entity mappings files.
     * @param entityClassNameList a <code>List</code> of class names.
     * @param entityClassMap the resulting <code>Map</code> that maps
     * java classes to entity classes.
     * @throws ClassNotFoundException 
     */
    private void createEntityClasses(MappingBundle mappingBundle,
    		List<String> entityClassNameList, 
    		Map<Class, EntityClass> entityClassMap) {
        for (String name : entityClassNameList) {
            try {
        		Class javaClass = loader.loadClass(name);
        		EntityClass entityClass = createEntityClass(
        				javaClass, mappingBundle);
    			entityClassMap.put(javaClass, entityClass);			
            } catch (ClassNotFoundException e) {
                throw BindingSupportImpl.getInstance().runtime(
                        "Unable to load Entity class '" + name + "': " + e, 
                        e);
            }
        }
    }

    /**
     * Create an entity class and map its contents if there is an
     * entry in the mapping bundle. Otherwise, convert the Java class
     * to and entity mapping structure and generate the entity class.
     * 
     * @param cls the java class that will be mapped.
     * @param mappingBundle the <code>MappingBundle</code> that maintains
     * a list of the entity mappings files.
     * @return the newly created <code>EntityClass</code>.
     */
    private EntityClass createEntityClass(Class cls,
    						MappingBundle mappingBundle) {
    	EntityClass entityClass = new EntityClass(cls);
    	String className = cls.getCanonicalName();
    	MappingBundle.Entry entry = mappingBundle.find(className);
    	if (entry == null) {
    		AnnotationMapper annotationMapper = new AnnotationMapper();
    		Mapping mapping = annotationMapper.createMapping(cls);
    		if (mapping instanceof Entity) {
    			Entity entity = (Entity)mapping;
    			mapClass(entityClass, entity);
    			entityMappingsXml.add(entity);
    		}
    		else if (mapping instanceof EmbeddableSuperclass) {
       	    	entityClass.setEmbeddableSuperclass();
       	    	EmbeddableSuperclass embeddableSuperclass =
       	    		(EmbeddableSuperclass)mapping;
    			mapEmbeddableSuperclass(entityClass, 
    					embeddableSuperclass);
    			entityMappingsXml.add(embeddableSuperclass);
   		}
    		for (Embeddable embeddable : annotationMapper.getEmbeddable()) {
	        	AccessType access = embeddable.getAccess();
	        	entityClass.setEmbeddable(access);
    			entityMappingsXml.add(embeddable);
    		}
    		tableGeneratorSet.addAll(annotationMapper.getTableGenerator());
    		sequenceGeneratorSet.addAll(annotationMapper.getSequenceGenerator());
    	}
    	else {
    		entityClass.setXmlGeneratedFlag();
    		EntityMappings mappings = entry.getEntityMappings();
    		tableGeneratorSet.addAll(mappings.getTableGenerator());
    		sequenceGeneratorSet.addAll(mappings.getSequenceGenerator());
    		List<CascadeType> cascade =	mappings.getDefaultCascade();
    		if (cascade != null) {
    			entityClass.setDefaultCascade(cascade);
    		}
    		AccessType defaultAccess = mappings.getDefaultAccess();
    		if (defaultAccess != null) {
    			entityClass.setDefaultAccess(defaultAccess);
    		}
    		if (entry.isEntity()) {
    			Entity entity = entry.getEntity();
    			mapClass(entityClass, entity);
    		}
	    	else if (entry.isEmbeddableSuperclass()) {
	    		EmbeddableSuperclass embeddableSuperclass = 
	    			entry.getEmbeddableSuperclass();
	   	    	entityClass.setEmbeddableSuperclass();
	    		mapEmbeddableSuperclass(entityClass, embeddableSuperclass);
	    	}
	    	else if (entry.isEmbeddable()) {
	        	Embeddable embeddable = entry.getEmbeddable();
	        	AccessType access = embeddable.getAccess();
	        	entityClass.setEmbeddable(access);
	    	}
    	}
		return entityClass;
    }
    
    /**
     * Set the super classes for the entity classes once they have all
     * been created.
     * @param entityClassMap the set of classes that will have their
     * super classes set.
     */
    private void setPersistentSuperclasses(
    		Map<Class, EntityClass> entityClassMap) {
    	for (Class javaClass : entityClassMap.keySet()) {
    		Class javaSuperclass = javaClass.getSuperclass();
    		if (javaSuperclass != null) {
    			EntityClass entityClass = entityClassMap.get(javaClass);
    			EntityClass entitySuperclass = entityClassMap.get(
    					javaSuperclass);
    			if (entitySuperclass != null) {
    				entityClass.setSuperclass(entitySuperclass);
    			}
    		}
     	}
    }
   
    /**
     * Map an entity class using an <code>Entity</code> object.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param entity the <code>Entity</code> object used to map
     * the resulting entity class.
     */
    private void mapClass(EntityClass entityClass, Entity entity) {
    	mapAccess(entityClass, entity);
    	mapInheritance(entityClass, entity);
    	mapTable(entityClass, entity);
    	mapSecondaryTable(entityClass, entity);
    	mapEntityListener(entityClass, entity);
    	mapCallbacks(entityClass, entity);

    	mapDiscriminatorColumn(entityClass, entity);
    	mapPrimaryKeyJoinColumn(entityClass, entity);
    	
    	mapEmbeddedId(entityClass, entity);
    	mapIdClass(entityClass, entity);
    	mapId(entityClass, entity);

    	mapVersion(entityClass, entity);
    	mapBasic(entityClass, entity);
    	mapLob(entityClass, entity);
    	mapEmbedded(entityClass, entity);
		mapOneToOne(entityClass, entity);
		mapManyToOne(entityClass, entity);
		mapOneToMany(entityClass, entity);
    	mapManyToMany(entityClass, entity);
    	mapTransient(entityClass, entity);
    }

    /**
     * Map an entity class using an <code>EmbeddableSuperclass</code>
     * object.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param embeddableSuperclass the <code>Entity</code> object 
     * used to map the resulting entity class.
     */
    private void mapEmbeddableSuperclass(EntityClass entityClass, 
    		EmbeddableSuperclass embeddableSuperclass) {
    	mapAccess(entityClass, embeddableSuperclass);

    	mapEmbeddedId(entityClass, embeddableSuperclass);
    	mapIdClass(entityClass, embeddableSuperclass);
    	mapId(entityClass, embeddableSuperclass);

    	mapVersion(entityClass, embeddableSuperclass);
    	mapBasic(entityClass, embeddableSuperclass);
    	mapLob(entityClass, embeddableSuperclass);
    	mapEmbedded(entityClass, embeddableSuperclass);
		mapOneToOne(entityClass, embeddableSuperclass);
		mapManyToOne(entityClass, embeddableSuperclass);
		mapOneToMany(entityClass, embeddableSuperclass);
    	mapManyToMany(entityClass, embeddableSuperclass);
    	mapTransient(entityClass, embeddableSuperclass);
    }

    /**
     * Set the access mechanism for an entity class. Access is
     * either by <code>AccessType.FIELD</code> or <code>
     * AccessType.PROPERTY</code>.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapAccess(EntityClass entityClass,
			Mapping mapping) {
    	AccessType access = mapping.getAccess();
    	entityClass.setAccess(access);
    }
    
    /**
     * Set the version field for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapVersion(EntityClass entityClass, Mapping mapping) {
    	Version version = mapping.getVersion();
    	if (version != null) {
    		entityClass.set(version);
    	}
    }
    
    /**
     * Add the inheritance mapping strategy to the entity class. 
     * <p>
     * The three inheritance mapping strategies are the single table per class 
     * hierarchy, table per class, and joined subclass strategies. 
     * <blockquote><pre>
     * InheritanceType.JOINED
     * InheritanceType.SINGLE_TABLE
     * InheritanceType.TABLE_PER_CLASS
     * </pre></blockquote>
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapInheritance(EntityClass entityClass, Entity entity) {
    	InheritanceType strategy = entity.getInheritanceStrategy();
    	DiscriminatorType discriminatorType = entity.getDiscriminatorType();
    	String discriminatorValue = entity.getDiscriminatorValue();
    	entityClass.setInheritance(strategy, discriminatorType,
    			discriminatorValue);
    }
    
    /**
     * Set the table for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapTable(EntityClass entityClass, Entity entity) {
    	Table table = entity.getTable();
    	if (table != null) {
    		entityClass.setTableName(table.getName());
    	}
    }    
    
    private void mapAttributeOverride(EntityClass entityClass, Entity entity) {
    	List<AttributeOverride> overrideList = entity.getAttributeOverride();
    	if (overrideList != null) {
			for (AttributeOverride attributeOverride : overrideList) {
				entityClass.add(attributeOverride);
			}
    	}
    }

    private void mapEntityListener(EntityClass entityClass, Entity entity) {
    	String entityListener = entity.getEntityListener();
    	if (entityListener != null) {
    		try {
    			Class listenerClass = loader.loadClass(entityListener);
        		entityClass.setEntityListener(listenerClass);
    		}
    		catch (ClassNotFoundException e) {
    		}
    	}
    }

    private void mapCallbacks(EntityClass entityClass, Entity entity) {
    	String postLoad = entity.getPostLoad();
    	String postPersist = entity.getPostPersist();
    	String postRemove = entity.getPostRemove();
    	String postUpdate = entity.getPostUpdate();
    	String prePersist = entity.getPrePersist();
    	String preRemove = entity.getPreRemove();
    	String preUpdate = entity.getPreUpdate();
    	entityClass.add(Callback.PostLoad, postLoad);
    	entityClass.add(Callback.PostPersist, postPersist);
    	entityClass.add(Callback.PostRemove, postRemove);
    	entityClass.add(Callback.PostUpdate, postUpdate);
    	entityClass.add(Callback.PrePersist, prePersist);
    	entityClass.add(Callback.PreRemove, preRemove);
    	entityClass.add(Callback.PreUpdate, preUpdate);
    }

    /**
     * Map the discriminator column for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param entity the <code>Entity</code> object used to map
     * the entity class.
     */
    private void mapDiscriminatorColumn(EntityClass entityClass,
			Entity entity) {
    	DiscriminatorColumn dc = entity.getDiscriminatorColumn();
    	if (dc != null) {
    		String name = dc.getName();
    		String columnDefinition = dc.getColumnDefinition();
    		int length = dc.getLength().intValue();
    		entityClass.setDiscriminatorColumn(name, 
    				columnDefinition, length);
    	}
    }    

    /**
     * Map the primary key join columns for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param entity the <code>Entity</code> object used to map
     * the entity class.
     */
    private void mapPrimaryKeyJoinColumn(EntityClass entityClass,
			Entity entity) {
    	List<PrimaryKeyJoinColumn> pkjcList = 
    		entity.getPrimaryKeyJoinColumn();
    	if (pkjcList != null) {
    		for (PrimaryKeyJoinColumn pkjc : pkjcList) {
    			String name = pkjc.getName();
    			String referencedColumnName = pkjc.getReferencedColumnName();
    			String columnDefinition = pkjc.getColumnDefinition();
    			entityClass.setPrimaryKeyJoinColumn(name,
    					referencedColumnName, columnDefinition);
    		}
    	}
    }    

    /**
     * Map the secondary tables for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapSecondaryTable(EntityClass entityClass,
			Entity entity) {
    	List<SecondaryTable> tableList = entity.getSecondaryTable();
    	if (tableList != null) {
			for (SecondaryTable secondaryTable : tableList) {
				entityClass.add(secondaryTable);
			}
    	}
    }    

    /**
     * Map the id class fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapEmbeddedId(EntityClass entityClass,
			Mapping mapping) {
    	String fieldName = mapping.getEmbeddedId();
		if (fieldName != null) {
			entityClass.setEmbeddedId(fieldName);
		}
    }

    /**
     * Map the id class fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapIdClass(EntityClass entityClass, Mapping mapping) {
    	String className = mapping.getIdClass();
		if (className != null) {
			entityClass.setIdClass(className);
		}
    }

    /**
     * Map the id fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapId(EntityClass entityClass, Mapping mapping) {
    	List<Id> idList = mapping.getId();
		if (idList != null) {
			for (Id id : idList) {
				entityClass.add(id);
			}   	
		}
    }

    /**
     * Map the basic fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapBasic(EntityClass entityClass, Mapping mapping) {
    	List<Basic> basicList = mapping.getBasic();
		if (basicList != null) {
			for (Basic basic : basicList) {
				entityClass.add(basic);
			}   	
		}
    }

    /**
     * Set the lob fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapLob(EntityClass entityClass, Mapping mapping) {
    	List<Lob> lobList = mapping.getLob();
		if (lobList != null) {
			for (Lob lob : lobList) {
				entityClass.add(lob);
			}   	
		}
    }

    /**
     * Map the embedded fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapEmbedded(EntityClass entityClass, Mapping mapping) {
    	List<Embedded> embeddedList = mapping.getEmbedded();
		if (embeddedList != null) {
			for (Embedded embedded : embeddedList) {
				entityClass.add(embedded);
			}   	
		}
    }

    /**
     * Map the one-to-one fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapOneToOne(EntityClass entityClass, Mapping mapping) {
    	List<OneToOne> oneToOneList = mapping.getOneToOne();
		if (oneToOneList != null) {
			for (OneToOne oneToOne : oneToOneList) {
				entityClass.add(oneToOne);
			}   	
		}
    }

    /**
     * Map the many-to-one fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapManyToOne(EntityClass entityClass, Mapping mapping) {
    	List<ManyToOne> manyToOneList = mapping.getManyToOne();
		if (manyToOneList != null) {
			for (ManyToOne manyToOne : manyToOneList) {
				entityClass.add(manyToOne);
			}   	
		}
    }

    /**
     * Map the one-to-many fields  for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapOneToMany(EntityClass entityClass, Mapping mapping) {
    	List<OneToMany> oneToManyList = mapping.getOneToMany();
		if (oneToManyList != null) {
			for (OneToMany oneToMany : oneToManyList) {
				entityClass.add(oneToMany);
			}   	
		}
    }

    /**
     * Map the many-to-many fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapManyToMany(EntityClass entityClass, Mapping mapping) {
    	List<ManyToMany> manyToManyList = mapping.getManyToMany();
    	if (manyToManyList != null) {
        	for (ManyToMany manyToMany : manyToManyList) {
        		entityClass.add(manyToMany);
        	}
    	}
    }

    /**
     * Map the transient fields for an entity class.
     * @param entityClass the <code>EntityClass</code> to map.
     * @param mapping the <code>Mapping</code> object used to map
     * the entity class.
     */
    private void mapTransient(EntityClass entityClass, 
    		Mapping mapping) {
    	List<String> transientList = mapping.getTransient();
		if (transientList != null) {
			for (String fieldName : transientList) {
				entityClass.addTransient(fieldName);
			}   	
		}
    }
        
}
