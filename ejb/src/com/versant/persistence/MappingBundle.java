
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
package com.versant.persistence;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityMappingsXml;
import javax.persistence.spi.PersistenceInfo;

import com.versant.persistence.ems.*;

/**
 * Maps a set of entity mapping xml files to their associated java class 
 * representations as defined in the package com.versant.persistence.ems.
 * These are the classes represented by the JSR220 xml mapping schema.
 * @author Rick George
 */
public class MappingBundle {
	List<EntityMappingsXml> entityMappingsList = 
						new ArrayList<EntityMappingsXml>();
	ClassLoader loader;
	
	/**
	 * Construct a mapping bundle by extracting the mapping files from the
	 * supplied <code>PersistenceInfo</code> object. The mapping files are
	 * added to a entity mappings list.
	 * @param info The <code>PersistenceInfo</code> object that contains the
	 * xml mapping file names.
	 * @param loader The class loader passed to <code>EntityMappingsXml</code>
	 * objects for loading xml mapping files.
	 */
	public MappingBundle(PersistenceInfo info,
								ClassLoader loader) {
		this.loader = loader;
		URL url = info.getEntityMappingsXmlFileUrl();
		if (url != null) {
			EntityMappingsXml emx = new EntityMappingsXml(url, loader);
			entityMappingsList.add(emx);
		}
		List<String> mappingFileNames = info.getMappingFileNames();
		for (String filename : mappingFileNames) {
			this.add(filename);
		}
	}
	
	/**
	 * Add a file to the entity mappings list mainted by this bundle.
	 * @param filename The name of the file that represents an entity
	 * mappings xml file.
	 */
	private void add(String filename) {
		// TODO
	}

	/**
	 * Look through the entity mappings files maintained by this bundle and
	 * pass back the entity object requested.
	 * @param className The name of the class to look up.
	 * @return The <code>Entity</code> object associated with the passed-in
	 * class name if found; otherwise, return null.
	 */
	public com.versant.persistence.ems.Entity getEntity(String className) {
		for (EntityMappingsXml emx : entityMappingsList) {
			com.versant.persistence.ems.EntityMappings ems = 
										emx.getEntityMappings();
			for (com.versant.persistence.ems.Entity entity : 
												ems.getEntity()) {
				if (entity.getClassName().equals(className)) {
					return entity;
				}
			}
		}
		return null;
	}
	
	/**
	 * Look through the entity mappings files maintained by this bundle and
	 * pass back the embeddable object requested.
	 * @param className The name of the class to look up.
	 * @return The <code>Entity</code> object associated with the passed-in
	 * class name if found; otherwise, return null.
	 */
	public com.versant.persistence.ems.Embeddable getEmbeddable(String className) {
		for (EntityMappingsXml emx : entityMappingsList) {
			com.versant.persistence.ems.EntityMappings ems = 
										emx.getEntityMappings();
			for (com.versant.persistence.ems.Embeddable embeddable : 
												ems.getEmbeddable()) {
				if (embeddable.getClassName().equals(className)) {
					return embeddable;
				}
			}
		}
		return null;
	}
	
	/**
	 * Look through the entity mappings files maintained by this bundle and
	 * pass back the <code>EntityMappings</code> object requested.
	 * @param entity The <code>Entity</code> to look up.
	 * @return The <code>EntityMappings</code> object associated with the 
	 * passed-in class name if found; otherwise, return null.
	 */
	public com.versant.persistence.ems.EntityMappings getEntityMappings(
			com.versant.persistence.ems.Entity entity) {
		for (EntityMappingsXml emx : entityMappingsList) {
			com.versant.persistence.ems.EntityMappings ems = 
										emx.getEntityMappings();
			for (com.versant.persistence.ems.Entity e : 
												ems.getEntity()) {
				if (e.equals(entity)) {
					return ems;
				}
			}
		}
		return null;
	}

	/**
	 * Look through the entity mappings files maintained by this bundle and
	 * pass back the embeddable super class object requested.
	 * @param className The name of the class to look up.
	 * @return The <code>EmbeddableSuperclass</code> object associated with 
	 * the passed-in class name if found; otherwise, return null.
	 */
	public com.versant.persistence.ems.EmbeddableSuperclass 
								getEmbeddableSuperclass(String className) {
		for (EntityMappingsXml emx : entityMappingsList) {
			com.versant.persistence.ems.EntityMappings ems = 
										emx.getEntityMappings();
			for (com.versant.persistence.ems.EmbeddableSuperclass 
					embeddableSuperclass : ems.getEmbeddableSuperclass()) {
				if (embeddableSuperclass.getClassName().equals(className)) {
					return embeddableSuperclass;
				}
			}
		}
		return null;
	}
	
	/**
	 * Look through the entity mappings files maintained by this bundle 
	 * and pass back the object associated with the request.
	 * @param className The name of the class to look up.
	 * @return The <code>Entry</code> object associated with 
	 * the passed-in class name if found; otherwise, return null.
	 */
	public Entry find(String className) {
		for (EntityMappingsXml emx : entityMappingsList) {
			com.versant.persistence.ems.EntityMappings ems = 
										emx.getEntityMappings();
			for (Entity entity : ems.getEntity()) {
				if (entity.getClassName().equals(className)) {
					return new Entry(entity, ems);
				}
			}
			for (Embeddable embeddable : ems.getEmbeddable()) {
				if (embeddable.getClassName().equals(className)) {
					return new Entry(embeddable, ems);
				}
			}
			for (EmbeddableSuperclass es : ems.getEmbeddableSuperclass()) {
				if (es.getClassName().equals(className)) {
					return new Entry(es, ems);
				}
			}
		}
		return null;
	}
	
	/**
	 * An entry in a mapping file that represents one of the following:
	 * Entity, Embeddable, or EmbeddableSuperclass and its associated
	 * mapping file.
	 */
	public class Entry {
		private Entry(Object object, EntityMappings ems) {
			this.object = object;
			this.ems = ems;
		}
		private Object object;
		private EntityMappings ems;
		public boolean isEntity(){return object instanceof Entity;}
		public boolean isEmbeddable(){return object instanceof Embeddable;}
		public boolean isEmbeddableSuperclass(){
			return object instanceof EmbeddableSuperclass;}
		public Entity getEntity() {return Entity.class.cast(object);}
		public Embeddable getEmbeddable() {return Embeddable.class.cast(object);}
		public EmbeddableSuperclass getEmbeddableSuperclass() {
			return EmbeddableSuperclass.class.cast(object);}
		public EntityMappings getEntityMappings() {return ems;}
	}
	
}
