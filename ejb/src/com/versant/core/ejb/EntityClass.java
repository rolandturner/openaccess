
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
package com.versant.core.ejb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.parser.*;
import com.versant.persistence.ems.*;

/**
 * Maps java classes to their persistent representations.
 * @author Rick George
 */
public class EntityClass {
	Map<Callback, Method> callbackMap, listenerCallbackMap; 

	public static final String EAGER = FetchType.EAGER.toString();

    JdoClass jdoClass = new JdoClass();
	Set<JdoElement> jdoElements = new HashSet<JdoElement>();
	AccessType access = null;
	InheritanceType strategy = null;
	EntityClass superclass = null;
	JdoExtension pkjc = null, version = null;
	Boolean embeddableSuperclass = false;

	/**
	 * Construct an EntityClass for building a jdo class.
	 * @param javaClass The java class this entity class is based on.
	 */
	public EntityClass(Class javaClass) {
		jdoClass.javaClass = javaClass;
		jdoClass.name = javaClass.getSimpleName();
		jdoClass.elements = new JdoElement[0];
        jdoClass.identityType = 0;
		jdoClass.objectIdClass = null;
		jdoClass.parent = null;
		jdoClass.pcSuperclass = null;
		jdoClass.queries = null;
		jdoClass.requiresExtent = true;
	}
	
	/**
	 * Get the access mechanism for this class. Valid values are
	 * either FIELD or PROPERTY.
	 * @return the access mechanism for this entity class.
	 */
	private AccessType getAccess() {
		return (access != null) ? access : defaultAccess;
	}
	
	/**
	 * Set the super class.
	 * @param EntityClass the entity super class.
	 */
	public void setSuperclass(EntityClass superclass) {
		if (superclass != null) {
			jdoClass.pcSuperclass = superclass.getName();
			this.superclass = superclass;
		}
	}
	
	/**
	 * Traverse the inheritance hierarchy to find the superclass
	 * inheritance mapping strategy and add optimistic-lock extension
	 * if this is a top-level class. 
	 */
	public void setInheritance() {
		if (superclass != null && superclass.embeddableSuperclass) {
        	if (version == null) {
        		version = superclass.version;
        	}
		}
        if (superclass == null || superclass.embeddableSuperclass) {
        	if (version != null) {
        		jdoElements.add(version);
        	} else {
	            JdoExtension optLock = getExtension(
	            		JdoExtension.JDBC_OPTIMISTIC_LOCKING, "none", null);
	            jdoElements.add(optLock);
        	}
        }
        if (strategy == null) {
			for (EntityClass sc = superclass; sc != null; 
											sc = sc.superclass) {
				if (sc.strategy != null) {
					setInheritance(sc.strategy, null, null); 
					break;
				}
			}
		}
	}
	
	/**
	 * Get the name of the fully-qualified java class used for mapping
	 * this entity class.
	 * @return the java class name.
	 */
	public String getName() {
		return getJavaClass().getName();
	}
	
	/**
	 * Get the java class associated with this entity class.
	 * @return the java <code>Class</code> represented by 
	 * this entity class.
	 */
	public Class getJavaClass() {
		return jdoClass.javaClass;
	}
	
	/**
	 * Get the package name of the java class represented by
	 * this entity class.
	 * @return
	 */
	public String getPackageName() {
		Package pkg = getJavaClass().getPackage();
		return pkg != null ? pkg.getName() : "";
	}
	
	/**
	 * Set the flag that indicates this entity class was mapped
	 * from an entity-mapping xml file.
	 */
	public void setXmlGeneratedFlag() {
		jdoClass.xmlGenerated = true;
	}
	
	/**
	 * The default cascade value for this class that can be
	 * set in an entity mapping xml file for all classes mapped
	 * by that file.
	 */
	private int defaultCascade = MDStatics.CASCADE_ALL;
	
	/**
	 * Set the default cascade value for this class based on a 
	 * value found in an entity mapping xml file.
	 */
	public void setDefaultCascade(List<CascadeType> cascade) {
		defaultCascade = cascadeType(cascade);
	}
	
	/**
	 * The default access value for this class that can be
	 * set in an entity mapping xml file for all classes mapped
	 * by that file.
	 */
	private AccessType defaultAccess = AccessType.PROPERTY;
	
	/**
	 * Set the default access value for this class based on a 
	 * value found in an entity mapping xml file.
	 */
	public void setDefaultAccess(AccessType access) {
		if (access != null) {
			defaultAccess = access;
		}
	}
	
	/**
	 * Copy the jdo class elements gathered by this entity class and 
	 * return a <code>JdoClass</code> object that contains those elements.
	 * 
	 * @return JdoClass A jdo class constructed from entity
	 * mappings.
	 */
	public JdoClass getJdoClass() {
		int size = jdoElements.size();
		jdoClass.elements = new JdoElement[size];
		jdoElements.toArray(jdoClass.elements);
		jdoClass.listenerCallbackMap = getCallbacks(listenerCallbackMap);
		jdoClass.callbackMap = getCallbacks(callbackMap);
		return jdoClass;
	}
	
	private Method[] getCallbacks(Map<Callback, Method> map) {
		Method[] result = null;
		if (map != null && !map.isEmpty()) {
			int count = Callback.values().length;
			result = new Method[count];
			for (Callback c : map.keySet()) {
				Method method = map.get(c);
				result[c.ordinal()] = method;
			}
		}
		return result;
	}
	
	/**
	 * Set the access mechanism for this class.
	 * 
	 * @param access The type of access (FIELD or PROPERTY) for
	 * this class.
	 */
	public void setAccess(AccessType access) {
		jdoClass.objectIdClasssRequired = false;
		jdoClass.identityType = MDStatics.IDENTITY_TYPE_APPLICATION;
        if (access != null) {
        	this.access = access;
        }
	}
	
	/**
	 * Add an embedded-id field element to this entity class.
	 * <p>
	 * The EmbeddedId mapping is used to denote a composite primary key
	 * that is an embeddable class. It may be applied to a persistent field 
	 * or property of the entity class. There should only be one EmbeddedId
	 * mapping and no Id mapping when the EmbeddedId mapping is used.
	 * 
	 * @param attribute The name of the field to use for the embedded id.
	 */
	public void setEmbeddedId(String attribute) {
        JdoField jdoField = getField(attribute);
        jdoField.embedded = MDStatics.TRUE;
        jdoField.defaultFetchGroup = MDStatics.FALSE;
        jdoField.primaryKey = true;
        jdoElements.add(jdoField);
	}
	
	/**
	 * The IdClass mapping is used to denote a composite primary key. It is 
	 * applied to the entity class. The composite primary key class corresponds 
	 * to multiple fields or properties of the entity class, and the names of 
	 * primary key fields or properties in the primary key class and those of the 
	 * entity class must correspond and their types must be the same. The Id 
	 * annotation may also be applied to such fields or properties, however this 
	 * is not required.
	 * 
	 * @param className The name of the class to use for the id class.
	 */
	public void setIdClass(String className) {
		jdoClass.objectIdClass = className;
	}
	
	/**
	 * Add a version field the the mapped elements of this entity class.
	 * <p>
	 * The Version annotation specifies the version property (optimistic lock value) 
	 * of an entity class. This is used to ensure integrity when reattaching and for 
	 * overall optimistic concurrency control. Only a single Version property/field 
	 * should be used per class; applications that use more than one are not expected 
	 * to be portable. The Version property should be mapped to the primary table for 
	 * the entity class; applications that map the Version property to a table other 
	 * than the primary table are not portable. Fields or properties that are specified 
	 * with the Version annotation should not be updated by the application. The 
	 * following types are supported for version properties: int, Integer, short, Short, 
	 * long, Long, Timestamp.
	 * 
	 * @param version contains the version mapping parameters 
	 * 
	 * Version Mapping Elements:
	 * attribute: the java class field name to use for version.
	 * columnName: the database column name for version
	 * field will be mapped to.
	 */
	public void set(Version version) {
		String attribute = version.getAttribute();
		Column column = version.getColumn();
		String columnName = null;
		if (column != null)
			columnName = column.getName();
        JdoField jdoField = getField(attribute);
        if (columnName == null) 
        	columnName = attribute;
        jdoField.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_NONE;
    	this.version = getExtension(JdoExtensionKeys.JDBC_OPTIMISTIC_LOCKING, 
    			columnName, jdoField);
		this.version.nested = new JdoExtension[]{getColumn(columnName)};
		this.version.findCreate(JdoExtension.FIELD_NAME, jdoField.name, true);
        jdoElements.add(jdoField);
	}
	
	/**
	 * Add an EMBEDDED_ONLY extension the mapped elements of this entity class.
	 * <p>
	 * The Embeddable mapping is used to mark an object that is stored as an 
	 * intrinsic part of an owning entity and shares the identity of that entity. 
	 * Each of the persistent properties or fields of the embedded object is 
	 * mapped to the database table. Only Basic, Column, and Lob mapping 
	 * annotations may be used to map embedded objects.
	 * 
	 * @param access the access mechanism (FIELD or PROPERTY)
	 * for this embedded class.
	 */
	public void setEmbeddable(AccessType access) {
		JdoExtension ext = new JdoExtension();
        ext.key = JdoExtension.EMBEDDED_ONLY;
        jdoElements.add(ext);
        if (access != null) {
        	this.access = access;
        }
	}
	
    /**
     * Add the inheritance mapping elements for this entity class. 
     * <p>
     * The three inheritance mapping strategies are the single table per class 
     * hierarchy, table per class, and joined subclass strategies. 
     * <blockquote><pre>
     * InheritanceType.JOINED
     * InheritanceType.SINGLE_TABLE
     * InheritanceType.TABLE_PER_CLASS
     * </pre></blockquote>
     * 
     * @param strategy the <code>InheritanceType</code> strategy
     * to use for this class.
     */
	public void setInheritance(InheritanceType strategy, 
			DiscriminatorType discriminatorType, String discriminatorValue) {
		if (strategy != null) {
			this.strategy = strategy;
			JdoExtension inheritance = new JdoExtension();
	        inheritance.key = JdoExtension.JDBC_INHERITANCE;
	        inheritance.value = strategy == InheritanceType.SINGLE_TABLE ? "flat" : 
	        		strategy == InheritanceType.JOINED ? "vertical" : "horizontal";
	        if (pkjc != null) {
				inheritance.nested = new JdoExtension[]{pkjc};	        	
	        }
	        jdoElements.add(inheritance);
		}
		if (discriminatorType != null && isValid(discriminatorValue)) {
			String type = discriminatorType.toString();
			if (type.equals("CHARACTER")) type = "CHAR";
			else if (type.equals("STRING")) type = "VARCHAR";
			JdoExtension column = getExtension(
					JdoExtensionKeys.JDBC_COLUMN, null, null);
			column.findCreate(JdoExtensionKeys.JDBC_COLUMN_NAME, 
					discriminatorValue, true);
			column.findCreate(JdoExtensionKeys.JDBC_TYPE, 
					type, true);
			JdoExtension discriminator = getExtension(
					JdoExtension.JDBC_CLASS_ID, null, null);
			discriminator.nested = new JdoExtension[]{column};
	        jdoElements.add(discriminator);
		}
	}
	
	/**
	 * Set the inheritance type for a class to embeddable.
	 * <p>
	 * A class designated as an embeddable superclass has no 
	 * separate table defined for it. Its mapping information is 
	 * applied to the entities that inherit from it. 
	 * <p>
	 * A class designated as EmbeddableSuperclass can be mapped in the 
	 * same way as an entity except that the mappings will apply 
	 * only to its subclasses since no table exists for the embeddable 
	 * superclass. When applied to the subclasses the inherited 
	 * mappings will apply in the context of the subclass tables. 
	 * Mapping information may be overridden in such subclasses 
	 * by using the AttributeOverride annotation.
	 */
	public void setEmbeddableSuperclass() {
		embeddableSuperclass = true;
		JdoExtension inheritance = new JdoExtension();
        inheritance.key = JdoExtension.JDBC_INHERITANCE;
        inheritance.value = "horizontal";
        jdoElements.add(inheritance);
		JdoExtension embeddable = new JdoExtension();
        embeddable.key = JdoExtension.JDBC_DO_NOT_CREATE_TABLE;
        embeddable.value = "true";
        jdoElements.add(embeddable);
	}
	
	public void add(AttributeOverride attributeOverride) {
		String name = attributeOverride.getName();
		Column column = attributeOverride.getColumn();
        JdoField jdoField = getField(name);
        jdoField.primaryKey = true;
        addColumn(jdoField, column);
        jdoElements.add(jdoField);
	}
	
	/**
	 * Set the name of the table used to store object instances
	 * of this class.
	 * 
	 * @param tableName The name of the database table used to store
	 * instances of this entity class.
	 */
	public void setTableName(String tableName) {
        JdoExtension jdoExtension = new JdoExtension();
        jdoExtension.key = JdoExtensionKeys.JDBC_TABLE_NAME;
        jdoExtension.value = tableName;
        jdoElements.add(jdoExtension);
	}
	
	public void setEntityListener(Class entityListener) {
		jdoClass.entityListener = entityListener;
		listenerCallbackMap = new EnumMap<Callback, Method>(Callback.class);
		for (Method method : entityListener.getDeclaredMethods()) {
			addListenerCallback(method);
		}
	}
	
    private void addListenerCallback(Method method) {
		for (Annotation a : method.getDeclaredAnnotations()) {
			try {
				String name = a.annotationType().getSimpleName();
				Callback callback = Callback.valueOf(name);
				listenerCallbackMap.put(callback, method);
			} catch (IllegalArgumentException e) {
			}
		}
    }
	
	public void add(Callback callback, String name) {
		if (isValid(name)) {
			if (callbackMap == null)
				callbackMap = new EnumMap<Callback, Method>(Callback.class);
			try {
				Class[] params = {};
				Method method = getJavaClass().getDeclaredMethod(name, params);
				callbackMap.put(callback, method);
			} catch (NoSuchMethodException e) {
			}
		}
	}
	
	/**
	 * The PrimaryKeyJoinColumn mapping specifies the primary key 
	 * columns that are used as a foreign key to join to another table. 
	 * The PrimaryKeyJoinColumn mapping is used to join the primary 
	 * table of an entity subclass in the JOINED mapping strategy to 
	 * the primary table of its superclass; together with a SecondaryTable 
	 * mapping to join a secondary table to a primary table; or in a 
	 * OneToOne mapping in which the primary key of the referencing entity 
	 * is used as a foreign key to the referenced entity.
	 * 
	 * @param name the name of the primary key column
	 * @param referencedColumnName The name of the primary key column of 
	 * the table being joined to.
	 * @param columnDefinition The SQL fragment that is used when generating 
	 * the DDL for the column. This should not be specified for a OneToOne 
	 * primary key association.
	 */
	public void setPrimaryKeyJoinColumn(String name,
			String referencedColumnName, String columnDefinition) {	
		JdoExtension column = getColumn(name);
		if (column != null) {
			pkjc = getExtension(JdoExtension.JDBC_REF, referencedColumnName, null);
			pkjc.nested = new JdoExtension[]{column};
		}
	}
	
	/**
	 * Create a new jdo extension column for a specified field name.
	 * @param name the name of the field to use for the column.
	 * @return the <code>JdoExtension</code> representing a column.
	 */
	private JdoExtension getColumn(String name) {
		JdoExtension column = null;
		if (isValid(name)) {
	        column = getExtension(JdoExtensionKeys.JDBC_COLUMN, null, null);
			column.findCreate(JdoExtensionKeys.JDBC_COLUMN_NAME, name, true);
		}
		return column;
	}
	
	/**
	 * The DiscriminatorColumn mapping is used to define the discriminator 
	 * column for SINGLE_TABLE and JOINED inheritance strategies.
	 * 
	 * @param name The name of column to be used for the discriminator.
	 * @param columnDefinition The SQL fragment that is used when generating 
	 * the DDL for the discriminator column.
	 * @param length The column length for String-based discriminator types. 
	 * Ignored for other discriminator types.
	 */
	public void setDiscriminatorColumn(String name,
			String columnDefinition, int length) {		
	}
	
	/**
     * Add a secondary table to the mapped elements of this entity class.
     * <p>
     * The SecondaryTable annotation is used to specify a secondary table 
     * for an entity class. Specifying one or more secondary tables indicates 
     * that the entity data is stored across multiple tables.
     * 
     * @param table a SecondaryTable used to map this entity class.
     * 
     * SecondaryTable Mapping Elements:
	 * name: name of the secondary table.
	 * catalog: the catalog name of the secondary table.
	 * schema: the schema for the secondary table.
	 * columnList: the columns that should be used to join
	 * with the primary table.
	 * constraintList: Unique constraints that should be placed 
	 * on the table. These are typically only used if table generation
	 * is in effect. These constraints apply in addition to any 
	 * constraints specified by the Column and Join-Column mappings, 
	 * or entailed by primary key mappings.
	 */
	public void add(SecondaryTable table) {
		String name = table.getName();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		List<PrimaryKeyJoinColumn> columnList = table.getPrimaryKeyJoinColumn();
		List<UniqueConstraint> constraintList = table.getUniqueConstraint();
        JdoExtension jdoExtension = new JdoExtension();
        jdoExtension.key = JdoExtensionKeys.JDBC_LINK_TABLE;
        jdoExtension.value = name;
        /** TODO for (PrimaryKeyJoinColumn column : columnList) {
        	String columnName = column.getName();
            jdoField.findCreate(JdoExtensionKeys.JDBC_COLUMN, null, false).
            	findCreate(JdoExtensionKeys.JDBC_COLUMN_NAME, columnName, true);
        }
        jdoElements.add(jdoExtension);
        */
	}
	
	/**
	 * Add a primary key column to the mapped elements of this entity class.
	 * <p>
	 * The Id mapping selects the identifier property of an entity root class. 
	 * By default, the mapped columns of this property are assumed to form the 
	 * primary key of the primary table. If no Column annotation is specified, 
	 * the primary key column name is assumed to be the name of the identifier 
	 * property or field.
	 * <p>
	 * The types of id generation are defined by the GeneratorType enum:
     * <blockquote><pre>
	 * public enum GeneratorType { TABLE, SEQUENCE, IDENTITY, AUTO, NONE };
     * </pre></blockquote>
     * The TABLE strategy indicates identifiers should be assigned using an 
     * underlying database table to ensure uniqueness. The SEQUENCE and IDENTITY 
     * strategies specify the use of a database sequence or identity column, 
     * respectively. AUTO indicates that the persistence provider should pick an 
     * appropriate strategy for the particular database. Specifying NONE indicates 
     * that no primary key generation by the persistence provider should occur, and 
     * that the application will be responsible for assigning the primary key.
     * 
	 * @param id contains the field name 'attribute' to use for the primary key;
	 * 'generate' is the type of primary key generation that should be used to 
	 * generate the entity primary key mapping; and 'column' if present contains 
	 * the primary key column name and other column attributes.
	 */
	public void add(Id id) {
		String attribute = id.getAttribute();
		GeneratorType generate = id.getGenerate();
		String generator = id.getGenerator();
		Column column = id.getColumn();
        JdoField jdoField = getField(attribute);
        jdoField.primaryKey = true;
        addColumn(jdoField, column);
        jdoElements.add(jdoField);
        JdoExtension keygen = new JdoExtension();
        keygen.key = (generate != null && generate != GeneratorType.NONE) ?
        		JdoExtensionKeys.JDBC_KEY_GENERATOR : -1;
        if (generate == GeneratorType.IDENTITY ||
        		generate == GeneratorType.SEQUENCE) {
            keygen.value = "AUTOINC";        	
        } else if (generate == GeneratorType.AUTO) {
        	keygen.value = "HIGHLOW";
        } else if (generate == GeneratorType.TABLE) {
        	keygen.value = (generator != null && 
        			generator.length() != 0) ? 
        					generator : "HIGHLOW";
        }
        jdoElements.add(keygen);
	}

	/**
	 * Add a basic field to the mapped elements of this entity class.
	 * <p>
	 * The Basic mapping is the simplest type of mapping to a 
	 * database column. It can optionally be applied to any persistent 
	 * property or instance variable of the following type: Java 
	 * primitive types, wrappers of the primitive types, java.lang.String, 
	 * java.math.BigInteger, java.math.BigDecimal, java.util.Date, 
	 * java.util.Calendar, java.sql.Date, java.sql.Time, java.sql.Timestamp, 
	 * byte[], Byte[], char[], Character[], enums, and any other type that 
	 * implements Serializable.
	 * 
	 * @param basic contains 'attribute' name of the field to map; whether the 
	 * value of the field or property should be lazy loaded or eagerly fetched; 
	 * whether the data is optional or required; the type used in mapping a 
	 * temporal type, which is either: DATE, TIME, TIMESTAMP, or NONE.
	 */
	public void add(Basic basic) {
		String attribute = basic.getAttribute();
		FetchType fetch = basic.getFetch();
		Boolean optional = basic.isOptional();
		TemporalType temporal = basic.getTemporalType();
		Column column = basic.getColumn();
		JdoField jdoField = getField(attribute);
        jdoField.defaultFetchGroup = fetchType(fetch);
        jdoField.temporal = temporalType(temporal);
        addColumn(jdoField, column);
        setOptional(jdoField, optional);
        jdoElements.add(jdoField);
	}
	
	/**
	 * Used to create columns for <code>Id</code>, <code>Basic</code>, and
	 * <code>Lob</code> types.
	 * @param jdoField the <code>JdoField</code> to add the column to.
	 * @param column the column that will be created and added to jdoField.
	 */
	private void addColumn(JdoField jdoField, Column column) {
		if (column != null) {
			JdoExtension ext = jdoField.findCreate(
					JdoExtensionKeys.JDBC_COLUMN, null, false);
			addColumn(ext, column);
	        Boolean unique = column.isUnique();
	        if (unique == Boolean.TRUE) {
	            JdoExtension index = new JdoExtension();
	            index.key = JdoExtensionKeys.JDBC_INDEX;
	            index.findCreate(JdoExtensionKeys.FIELD_NAME, 
	            		jdoField.name, false);
	            jdoElements.add(index);
	        }
		}
	}
	
	private void addColumn(JdoExtension ext, Column column) {
		String name = column.getName();
        Boolean nullable = column.isNullable();
        BigInteger length = column.getLength();
        BigInteger precision = column.getPrecision();
        BigInteger scale = column.getScale();
        Boolean insertable = column.isInsertable();
        Boolean updatable = column.isUpdatable();
        String type = column.getColumnDefinition();
		add(ext, JdoExtensionKeys.JDBC_COLUMN_NAME, name);
		if (!isDefault(scale, 0)) {
	        add(ext, JdoExtensionKeys.JDBC_SCALE, scale);
		}
		if (!isDefault(length, 255)) {
	        add(ext, JdoExtensionKeys.JDBC_LENGTH, length);
		}
        add(ext, JdoExtensionKeys.JDBC_NULLS, nullable);
        if (isValid(type)) {
        	add(ext, JdoExtensionKeys.JDBC_TYPE, type);
        }
	}
	
	private void addColumn(JdoField jdoField, JoinColumn column) {
		if (column != null) {
			String name = column.getName();
			String referencedColumnName = column.getReferencedColumnName();
            Boolean unique = column.isUnique();
	        Boolean nullable = column.isNullable();
	        Boolean insertable = column.isInsertable();
	        Boolean updatable = column.isUpdatable();
	        String type = column.getColumnDefinition();
			JdoExtension ext = jdoField.findCreate(
					JdoExtensionKeys.JDBC_COLUMN, null, false);
			add(ext, JdoExtensionKeys.JDBC_COLUMN_NAME, name);
	        add(ext, JdoExtensionKeys.JDBC_NULLS, nullable);
	        if (unique == Boolean.TRUE) {
                JdoExtension index = new JdoExtension();
                index.key = JdoExtensionKeys.JDBC_INDEX;
                index.findCreate(JdoExtensionKeys.FIELD_NAME, 
                		jdoField.name, false);
                jdoElements.add(index);
	        }
	        if (isValid(type)) {
	        	add(ext, JdoExtensionKeys.JDBC_TYPE, type);
	        }
		}
	}
	
	private boolean isDefault(BigInteger i, int value) {
		return i == null || i.intValue() == value;
	}

    private boolean isValid(String value) {
        return !(value == null || value.length() == 0);
    }

    private void add(JdoExtension ext, int key, Object o) {
		if (o != null) {
			ext.findCreate(key, o.toString(), true);
		}
	}

    private void setOptional(JdoField jdoField, Boolean optional) {
        if (optional != null && !optional) {
        	jdoField.nullValue = MDStatics.NULL_VALUE_EXCEPTION;
        }
	}
	
	/**
	 * Add an embedded field to the mapped elements of this entity class. 
	 * <p>
	 * The Embedded mapping may be used in an entity class when it is using 
	 * a shared embeddable class. The entity may override the column mappings 
	 * declared within the embeddable class to apply to its own entity table.
	 * 
	 * Embedded Mapping Elements:
	 * Name: The name of the field to map.
	 */
	public void add(Embedded embedded) {
		String attribute = embedded.getAttribute();
        JdoField jdoField = this.getField(attribute);
        jdoField.embedded = MDStatics.TRUE;
        jdoField.defaultFetchGroup = MDStatics.FALSE;
        jdoElements.add(jdoField);
        for (AttributeOverride attributeOverride : 
        						embedded.getAttributeOverride()) {
    		String name = attributeOverride.getName();
    		Column column = attributeOverride.getColumn();
			JdoExtension extension = jdoField.findCreate(
					JdoExtension.JDBC_REF, name, true);
    		addColumn(extension, column);
        }
		
	}
	public void addEmbedded(String fieldName) {
        JdoField jdoField = this.getField(fieldName);
        jdoField.embedded = MDStatics.TRUE;
        jdoField.defaultFetchGroup = MDStatics.FALSE;
        jdoElements.add(jdoField);
	}

	/**
	 * Add a large-object field to the mapped elements of this entity class. 
	 * <p>
	 * A Lob mapping specifies that a persistent property or field should be 
	 * persisted as a large object to a database-supported large object type.
	 * <p> 
	 * A Lob may be either a binary or character type, as defined by the LobType.
	 * 
	 * @param Lob the large-object element to map.
	 * Lob Mapping Elements:
	 * attribute: The name of the field to map.
	 * fetch: Whether the lob should be lazy loaded or eagerly fetched.
	 * optional: Whether the value of the field or property may be null.
	 * This is a hint; it may be used in schema generation.
	 * lobtype: The type of the lob. Either BLOB (default) or CLOB.
	 * column: The database column this field will be mapped to.
	 */
    public void add(Lob lob) {
        String attribute = lob.getAttribute();
        FetchType fetch = lob.getFetch();
        Boolean optional = lob.isOptional();
        LobType lobType =  lob.getLobType();
        Column column = lob.getColumn();

        JdoField jdoField = this.getField(attribute);
        jdoField.defaultFetchGroup = fetchType(fetch);
        addColumn(jdoField, column);
        setOptional(jdoField, optional);
        JdoExtension ext = jdoField.findCreate(
                JdoExtensionKeys.JDBC_COLUMN, null, false);
        add(ext, JdoExtensionKeys.JDBC_TYPE, lobType);
        jdoElements.add(jdoField);
	}

	/**
	 * Add a one-to-one field to the mapped elements of this entity class. 
	 * <p>
	 * The OneToOne mapping defines a single-valued association to another 
	 * entity that has one-to-one multiplicity. It is not normally necessary 
	 * to specify the associated target entity explicitly since it can usually 
	 * be inferred from the type of the object being referenced.
	 * 
	 * @param oneToOne the one-to-one relationship to map.
	 * 
	 * OneToOne Mapping Elements:
	 * attribute: The name of the field to map.
	 * targetEntity: The entity class that is the target of the association.
	 * cascade: The operations that should be cascaded to the target of the 
	 * association.
	 * fetch: Hint to the implementation as to whether the association should 
	 * be lazy loaded or eagerly fetched. The EAGER strategy is a requirement on 
	 * the persistence provider runtime that the associated entity should be 
	 * eagerly fetched.
	 * optional: Whether the association is optional. If set to false then a 
	 * non-null relationship must always exist.
	 */
    public void add(OneToOne oneToOne) {
        String attribute = oneToOne.getAttribute();
        String targetEntity = oneToOne.getTargetEntity();
        List<CascadeType> cascade = oneToOne.getCascade();
        FetchType fetch = oneToOne.getFetch();
        Boolean optional = oneToOne.isOptional();
        String mappedBy = oneToOne.getMappedBy();
        List<JoinColumn> columns = oneToOne.getJoinColumn();
        List<PrimaryKeyJoinColumn> pkjcList = oneToOne.getPrimaryKeyJoinColumn();

        JdoField jdoField = getField(attribute);
        jdoField.cascadeType = cascadeType(cascade);
        jdoField.defaultFetchGroup = fetchType(fetch);
        setOptional(jdoField, optional);
        addColumns(jdoField, columns);

        if (pkjcList != null) {
            for (PrimaryKeyJoinColumn pkjc : pkjcList) {
                String name = pkjc.getName();
                JdoExtension column = getColumn(name);
                String referencedColumnName = pkjc.getReferencedColumnName();
                JdoExtension extension = jdoField.findCreate(
                        JdoExtension.JDBC_REF, referencedColumnName, true);
                extension.nested = new JdoExtension[]{column};
            }
        }
        /*if (!isEmpty(mappedBy)) {
        	jdoField.findCreate(JdoExtensionKeys.INVERSE, mappedBy, true);
        }*/
        jdoElements.add(jdoField);
	}
	
	private void addColumns(JdoField jdoField, List<JoinColumn> columns) {
        if (columns != null) {
        	for (JoinColumn column : columns) {
        		addColumn(jdoField, column);
        	}
        }
	}
	
	/**
	 * Add a many-to-one element to the mapped elements of this entity class. 
	 * <p>
	 * The ManyToOne mapping defines a single-valued association to another 
	 * entity class that has many-to-one multiplicity. It is not normally 
	 * necessary to specify the target entity explicitly since it can  usually be 
	 * inferred from the type of the object being referenced.
	 * 
	 * @param manyToOne the many-to-one relationship to map.
	 * 
	 * ManyToOne Mapping Elements:
	 * attribute: The name of the field to map.
	 * targetEntity: The entity class that is the target of the association.
	 * cascade: The operations that should be cascaded to the target of the 
	 * association.
	 * fetch: Hint to the implementation as to whether the association should 
	 * be lazy loaded or eagerly fetched. The EAGER strategy is a requirement on the 
	 * persistence provider runtime that the associated entity should be eagerly 
	 * fetched.
	 */
	public void add(ManyToOne manyToOne) {
		String attribute = manyToOne.getAttribute();
		String targetEntity = manyToOne.getTargetEntity();
		List<CascadeType> cascade = manyToOne.getCascade();
		FetchType fetch = manyToOne.getFetch();
		Boolean optional = manyToOne.isOptional();
		List<JoinColumn> columns = manyToOne.getJoinColumn();		
		
		JdoField jdoField = getField(attribute);
		jdoField.cascadeType = cascadeType(cascade);
        jdoField.defaultFetchGroup = fetchType(fetch);
        setOptional(jdoField, optional);
        addColumns(jdoField, columns);
		jdoElements.add(jdoField);
	}

	/**
	 * Add a one-to-many element to the mapped elements of this entity class. 
	 * <p>
	 * A OneToMany mapping defines a many-valued association with one-to-many 
	 * multiplicity.
	 * 
	 * @param oneToMany the one-to-many relationship to map.
	 * 
	 * OneToMany Mapping Elements:
	 * attribute: The name of the element to map.
	 * targetEntity: The entity class that is the target of the association. 
	 * Optional only if the Collection property is defined using Java generics.
	 * Must be specified otherwise. The parameter type of the Collection when 
	 * defined using generics.
	 * mappedBy: The field that owns the relationship. Required unless the 
	 * relationship is unidirectional.
	 * joinTableName: The database table used for the join.
	 * cascade: The operations that should be cascaded to the target of the 
	 * association.
	 * fetch: Whether the association should be lazy loaded or eagerly fetched. 
	 * The EAGER strategy is a requirement on the persistence provider runtime that 
	 * the associated entities should be eagerly fetched.
	 */
	public void add(OneToMany oneToMany) {
		String attribute = oneToMany.getAttribute();
		String targetEntity = oneToMany.getTargetEntity();
		String mappedBy = oneToMany.getMappedBy();
		JoinTable joinTable = oneToMany.getJoinTable();
		List<CascadeType> cascade = oneToMany.getCascade();
		FetchType fetch = oneToMany.getFetch();
		String mapKey = oneToMany.getMapKey();
		String orderBy = oneToMany.getOrderBy();
		
        JdoField jdoField = this.getField(attribute);
        addMultiplicity(jdoField, targetEntity, mappedBy, joinTable, mapKey);
		jdoField.cascadeType = cascadeType(cascade);
        jdoField.defaultFetchGroup = fetchType(fetch);
        if (isValid(orderBy)) {
            jdoField.findCreate(JdoExtensionKeys.ORDERING, orderBy, true);
        }
        jdoElements.add(jdoField);
	}
	
	/**
	 * Add a many-to-many element to the mapped elements of this entity class. 
	 * <p>
	 * A ManyToMany mapping defines a many-valued association with 
	 * many-to-many multiplicity. If the Collection is defined using generics 
	 * to specify the element type, the associated target entity class does 
	 * not need to be specified; otherwise it must be specified.
	 * 
	 * @param manyToMany the ManyToMany relationship to map.
	 * 
	 * ManyToMany Mapping Elements:
	 * attribute: The field to use for the many-to-many mapping.
	 * targetEntity: The entity class that is the target of the association. 
	 * Optional only if the Collection property is defined using Java generics.
	 * Must be specified otherwise. The parameter type of the Collection when 
	 * defined using generics.
	 * mappedBy: The field that owns the relationship. Required unless the 
	 * relationship is unidirectional.
	 * joinTableName: The database table used for the join.
	 * cascade: The operations that should be cascaded to the target of the 
	 * association.
	 * fetch: Whether the association should be lazy loaded or eagerly fetched. 
	 * The EAGER strategy is a requirement on the persistence provider runtime that 
	 * the associated entities should be eagerly fetched.
	 */
	public void add(ManyToMany manyToMany) {
		String attribute = manyToMany.getAttribute();
		String targetEntity = manyToMany.getTargetEntity();
		String mappedBy = manyToMany.getMappedBy();
		JoinTable joinTable = manyToMany.getJoinTable();
		List<CascadeType> cascade = manyToMany.getCascade();
		FetchType fetch = manyToMany.getFetch();
		String mapKey = manyToMany.getMapKey();
		String orderBy = manyToMany.getOrderBy();
		
        JdoField jdoField = this.getField(attribute);
        addMultiplicity(jdoField, targetEntity, mappedBy, joinTable, mapKey);
		jdoField.cascadeType = cascadeType(cascade);
        jdoField.defaultFetchGroup = fetchType(fetch);
        if (isValid(orderBy)) {
            jdoField.findCreate(JdoExtensionKeys.ORDERING, orderBy, true);
        }
        jdoElements.add(jdoField);
	}
	
	/**
	 * Determine whether a one-to-many or many-to-many field is an array, 
	 * map, or collection and provide the appropriate mapping.
	 * 
	 * @param jdoField the jdo field to map
	 * @param targetEntity The entity class that is the target of the association. 
	 * Optional only if the Collection property is defined using Java generics.
	 * Must be specified otherwise. The parameter type of the Collection when 
	 * defined using generics.
	 * @param mappedBy The field that owns the relationship. Required unless the 
	 * relationship is unidirectional.
	 * @param joinTable The database table used for the join.
	 * @param mapKey Used to specify the map key for associations of type 
	 * java.util.Map.
	 */
	private void addMultiplicity(JdoField jdoField, String targetEntity, 
			String mappedBy, JoinTable joinTable, String mapKey) {
        try {
            String joinTableName = null;
            if (joinTable != null) {
                joinTableName = joinTable.getName();
            }
            Field field = getJavaClass().getDeclaredField(jdoField.name);
            Class type = field.getType();
            if (Object[].class.isAssignableFrom(type)) {
                addArray(jdoField, targetEntity, mappedBy, joinTableName);
            } else if (java.util.Map.class.isAssignableFrom(type)) {
                addMap(jdoField, targetEntity, mappedBy, joinTableName, mapKey);
            } else {
                addCollection(jdoField, targetEntity, mappedBy, joinTableName);
                if (!java.util.Collection.class.isAssignableFrom(type)) {
                    System.out.println(
                            "Warning: Collection is not assignable from " +
                                    type.getName());
                }
            }
        } catch (NoSuchFieldException e) {
        }
	}

	/**
	 * Create an entity mapping using a java collection.
	 * @see addOneToOne and addManyToMany
	 */
	private void addCollection(JdoField jdoField, String targetEntity,
                               String mappedBy, String joinTableName) {
        jdoField.collection = new JdoCollection();
        jdoField.collection.parent = jdoField;
        if (isValid(targetEntity)) {
            jdoField.collection.elementType = targetEntity;
        }
        if (isValid(mappedBy)) {
            jdoField.collection.findCreate(
                    JdoExtensionKeys.INVERSE, mappedBy, true);
        }
        if (isValid(joinTableName)) {
            JdoExtension ltExt = jdoField.collection.findCreate(
                    JdoExtensionKeys.JDBC_LINK_TABLE, null, false);
            ltExt.findCreate(JdoExtensionKeys.JDBC_TABLE_NAME,
                    joinTableName, true);
        }
    }


    /**
	 * Create an entity mapping using a java map.
	 * @see addOneToOne and addManyToMany
	 */
    private void addMap(JdoField jdoField, String targetEntity,
                        String mappedBy, String joinTableName, String mapKey) {
        jdoField.map = new JdoMap();
        jdoField.map.parent = jdoField;
        jdoField.map.keyType = mapKey;
        jdoField.map.valueType = targetEntity;
        List<JdoExtension> extensionList = new ArrayList<JdoExtension>();
        if (isValid(mappedBy)) {
            JdoExtension extension = getExtension(
                    JdoExtensionKeys.INVERSE, mappedBy, jdoField.map);
            extensionList.add(extension);
        }
        if (isValid(joinTableName)) {
            JdoExtension extension = getExtension(
                    JdoExtensionKeys.JDBC_LINK_TABLE, null, jdoField.map);
            extension.findCreate(JdoExtensionKeys.JDBC_TABLE_NAME,
                    joinTableName, true);
        }
        int size = extensionList.size();
        jdoField.map.extensions = new JdoExtension[size];
        extensionList.toArray(jdoField.map.extensions);
    }
	
	/**
	 * Create an entity mapping from a java array field.
	 * @see addOneToOne and addManyToMany
	 */
	private void addArray(JdoField jdoField, String targetEntity, 
			String mappedBy, String joinTableName) {
        jdoField.array = new JdoArray();
        jdoField.array.parent = jdoField;
        List<JdoExtension> extensionList = new ArrayList<JdoExtension>();
        if (isValid(targetEntity)) {
        }
        if (isValid(mappedBy)) {
            JdoExtension extension = getExtension(
            		JdoExtensionKeys.INVERSE, mappedBy, jdoField.array);
            extensionList.add(extension);
        }
        if (isValid(joinTableName)) {
            JdoExtension extension = getExtension(
            		JdoExtensionKeys.JDBC_LINK_TABLE, null, jdoField.array);
            extension.findCreate(JdoExtensionKeys.JDBC_TABLE_NAME, 
            		joinTableName, true);
            extensionList.add(extension);
        }
        int size = extensionList.size();
    	jdoField.array.extensions = new JdoExtension[size];
    	extensionList.toArray(jdoField.array.extensions);
	}
	
	/**
	 * Get an extension for a jdo field.
	 * @param key the extension key
	 * @param value the extension value
	 * @param parent the extension parent
	 * @return an extension 
	 */
	private JdoExtension getExtension(int key, String value, JdoElement parent) {
        JdoExtension extension = new JdoExtension();
        extension.key = key;
        extension.value = value;
        extension.parent = parent;
        return extension;
	}

	/**
	 * The Transient mapping is used to specify that a property 
	 * or field is not persistent.
	 * @param fieldName The field to map as transient.
	 */
	public void addTransient(String fieldName) {
        JdoField jdoField = this.getField(fieldName);
        jdoField.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_NONE;
        jdoElements.add(jdoField);
	}

	/**
	 * Get or construct a field for mapping.
	 * @param fieldName The name of the field to get.
	 * @return The newly constructed or found field.
	 */
	private JdoField getField(String fieldName) {
        JdoField jdoField = new JdoField();
        jdoField.name = fieldName;
        jdoField.parent = jdoClass;
        try {
            Field field = getJavaClass().getDeclaredField(fieldName);
            Class type = field.getType();
            if (getAccess() == AccessType.PROPERTY) {
                String getterPrefix = type.equals(boolean.class) ||
                        type.equals(Boolean.class) ? "is" : "get";
                String baseName = Character.toUpperCase(fieldName.charAt(0)) +
                        fieldName.substring(1);
                String getterName = getterPrefix + baseName;
                Class[] params = {};
                Method getter = getJavaClass().getDeclaredMethod(getterName, params);
                String setterPrefix = "set";
                String setterName = setterPrefix + baseName;
                params = new Class[]{type};
                Method setter = getJavaClass().getDeclaredMethod(setterName, params);
            }
        } catch (NoSuchFieldException e) {
            System.out.println("No such field: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            System.out.println("No such method: " + e.getMessage());
        }
        return jdoField;
	}

	/**
	 * Convert an enumerated <code>FetchType</code> object to an
	 * equivalent <code>MDStatics</code> value.
	 * @param fetch the <code>FetchType</code> enum to convert.
	 * @return the <code>MDStatics</code> fetch value.
	 */
	private int fetchType(FetchType fetch) {
		String fetchName = EAGER;
		if (fetch != null) {
			fetchName = fetch.toString();
		}
		return fetchName.equals(EAGER) ? 
				MDStatics.TRUE : MDStatics.FALSE;
	}

	/**
	 * Convert a temporal object to its string equivalent.
	 * @param temporal the <code>TemporalType</code> object to convert.
	 * @return the temporal string.
	 */
	private String temporalType(TemporalType temporal) {
		String temporalName = "NONE";
		if (temporal != null) {
			temporalName = temporal.toString();
		}
		return temporalName;
	}

	/**
	 * Convert a list of <code>CascadeType</code> enum objects to a
	 * <code>MDStatics</code> int representation.
	 * @param cascadeList the list of <code>CascadeType</code> enum objects
	 * to convert.
	 * @return the <code>MDStatics</code> int cascade representation.
	 */
	private int cascadeType(List<CascadeType> cascade) {
        if (cascade == null || cascade.size() == 0)
            return defaultCascade;
        int result = 0;
        for (CascadeType cascadeType : cascade) {
            if (cascadeType == CascadeType.ALL)
                result = MDStatics.CASCADE_ALL;
            else if (cascadeType == CascadeType.PERSIST)
                result |= MDStatics.CASCADE_PERSIST;
            else if (cascadeType == CascadeType.MERGE)
                result |= MDStatics.CASCADE_MERGE;
            else if (cascadeType == CascadeType.REMOVE)
                result |= MDStatics.CASCADE_REMOVE;
            else if (cascadeType == CascadeType.REFRESH)
                result |= MDStatics.CASCADE_REFRESH;
        }
        return result;
    }
	
}
