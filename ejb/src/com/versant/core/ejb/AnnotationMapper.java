
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
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

import java.util.*;

import com.versant.persistence.ems.*;

/**
 * The <code>AnnotationMapper</code> class converts annotated java classes to
 * to their jsr220 xml schema equivalent. This provides one standardized format
 * for generating enhanced classes and database schema.
 * @author Rick George
 * @version 1.0, 10/1/05
 */
public class AnnotationMapper {
	Mapping mapping;
	OneToOne oneToOne; 
	OneToMany oneToMany; 
	ManyToMany manyToMany;
	Embedded embedded;
	Set<Annotation> done = new HashSet<Annotation>(); 
	Annotation[] annotations;
	String attribute;
	Class type, javaClass;
	String method;
	
	enum State {Mapping, OneToOne, OneToMany, ManyToMany, Embedded, Other};
	State state = State.Mapping;
	
	List<Embeddable> embeddable = new ArrayList<Embeddable>();
	public List<Embeddable> getEmbeddable() {
		return embeddable;
	}

	List<TableGenerator> tableGenerator = new ArrayList<TableGenerator>();
	public List<TableGenerator> getTableGenerator() {
		return tableGenerator;
	}
	
	List<SequenceGenerator> sequenceGenerator = new ArrayList<SequenceGenerator>();
	public List<SequenceGenerator> getSequenceGenerator() {
		return sequenceGenerator;
	}

	/**
	 * The <code>AnnotationComparator</code> is used to keep the annotations in a
	 * sorted order. This is necessary because some annotations are encapsulated by
	 * others and the encapsulating object must be encounterd first. For example,
	 * the OneToMany annotation must be handled prior to its OrderBy annotation. 
	 *
	 */
	class AnnotationComparator implements Comparator<Annotation> {
		Integer low = Integer.valueOf(0);
		Integer mid = Integer.valueOf(1);
		Integer high = Integer.valueOf(2);
		Map<Class, Integer> order = new HashMap<Class, Integer>();
		public int compare(Annotation a1, Annotation a2) {
			return ord(a1) - ord(a2);
		}
		private int ord(Annotation a) {
			Class c = a.annotationType();
			return !order.containsKey(c) ? mid : order.get(c);
		}
		protected AnnotationComparator() {
			order.put(javax.persistence.Entity.class, low);
			order.put(javax.persistence.EmbeddableSuperclass.class, low);
			order.put(javax.persistence.Embedded.class, low);
			order.put(javax.persistence.Column.class, high);
			order.put(javax.persistence.JoinColumn.class, high);
			order.put(javax.persistence.JoinTable.class, high);
			order.put(javax.persistence.MapKey.class, high);
			order.put(javax.persistence.OrderBy.class, high);
			order.put(javax.persistence.PrimaryKeyJoinColumn.class, high);
		}
	}
	
	AnnotationComparator annotationComparator = new AnnotationComparator();

	/**
	 * Used by the caller to convert an annotated java class to its jsr220
	 * schema equivalent.
	 * @param javaClass is the java class to convert.
	 * @return the newly created mapped class, which is either an <code>
	 * Entity</code> class or an <code>EmbeddedSuperclass</code>.
	 */
	public Mapping createMapping(Class javaClass) {
		this.javaClass = javaClass;
		addClassAnnotations(javaClass);
		addFieldAnnotations(javaClass);
		addPropertyAnnotations(javaClass);
		return mapping;
	}

	/*
	 * Adds the top-level annotations for the input java class. i.e. the
	 * <code>Entity</code>, <code>Table</code>, <code>Inheritance</code> 
	 * and other class-level annotations.
	 */
	private void addClassAnnotations(Class cls) {
		annotations = cls.getDeclaredAnnotations();
		if (annotations.length == 0)
			throw new RuntimeException("Class '" + cls.getName() +
					"' has not been annotated.");
		add(annotations);
	}
	
	/*
	 * Adds the field-level annotations for a class.
	 */
	private void addFieldAnnotations(Class cls) {
		for (Field f : cls.getDeclaredFields()) {
			type = f.getType();
			attribute = f.getName();
			annotations = f.getAnnotations();
			add(annotations);
		}
	}
	
	/*
	 * Adds the property-level annotations for a class.
	 */
	private void addPropertyAnnotations(Class cls) {
		for (Method m : cls.getDeclaredMethods()) {
			method = m.getName();
        	type = m.getReturnType();
			String prefix = type.equals(boolean.class) ||
					type.equals(Boolean.class) ? "is" : "get";
			int i = prefix.length();
			attribute = Character.toLowerCase(method.charAt(i)) +
					method.substring(i + 1);
			annotations = m.getAnnotations();
			add(annotations);
		}
	}
	
	/*
	 * Invokes the appropriate annotation handlers for a series of annotations.
	 */
	private void add(Annotation[] annotations) {
		Arrays.sort(annotations, annotationComparator);
		for (Annotation a : annotations) {
			Class[] param = {a.annotationType()};
            try {
                Method m = AnnotationMapper.class.getDeclaredMethod("add", param);
                m.invoke(this, a);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                t.printStackTrace();
            }
		}
		state = State.Other;
		done.clear();
	}

	/**
	 * Create a new <code>Entity</code> mapping object and copies its
	 * attributes from the <code>javax.persistence.Entity</code>
	 * annotation object.
	 * @param e the <code>javax.persistence.Entity</code> annotation 
	 * to copy from.
	 */
	protected void add(javax.persistence.Entity e) {
		Entity entity = new Entity();
		String name = e.name();
		if (name.length() == 0) name = javaClass.getName();
		String s = e.access().toString();
		AccessType access = AccessType.valueOf(s);
		entity.setClassName(name);
		entity.setAccess(access);		
		mapping = entity;
	}
	
	protected void add(javax.persistence.AttributeOverride a) {
		String name = a.name();
		javax.persistence.Column c = a.column();
		Column column = createColumn(c);
		AttributeOverride ao = new AttributeOverride();
		ao.setName(name);
		ao.setColumn(column);
		if (state == State.Mapping) {
			((Entity)mapping).getAttributeOverride().add(ao);
		}
		else if (state == State.Embedded) {
			embedded.getAttributeOverride().add(ao);
		}
	}
	
	protected void add(javax.persistence.AttributeOverrides a) {
		for (javax.persistence.AttributeOverride ao : a.value()) {
			add(ao);
		}
	}

	static final boolean OPTIONAL_DEFAULT = true;
	static final TemporalType TEMPORAL_DEFAULT = TemporalType.NONE;
	static final FetchType FETCH_DEFAULT = FetchType.EAGER;

	protected void add(javax.persistence.Basic a) {
		Basic basic = new Basic();
		Boolean opt = a.optional();
		String tt = a.temporalType().toString();
		String ft = a.fetch().toString(); 
		TemporalType temporal = TemporalType.valueOf(tt);
		FetchType fetch = FetchType.valueOf(ft);
		basic.setAttribute(attribute);
		if (opt.booleanValue() != OPTIONAL_DEFAULT)
			basic.setOptional(opt);
		if (temporal != TEMPORAL_DEFAULT)
			basic.setTemporalType(temporal);
		if (fetch != FETCH_DEFAULT)
			basic.setFetch(fetch);
		Column column = getColumn();
		basic.setColumn(column);
		mapping.getBasic().add(basic);
	}
	
	protected Column getColumn() {
		Column column = null;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(javax.persistence.Column.class)) {
				javax.persistence.Column c = javax.persistence.Column.class.cast(a);
				column = createColumn(c);
				done.add(c);
			}
		}
		return column;
	}
	
	static final boolean UNIQUE_DEFAULT = false;
	static final boolean NULLABLE_DEFAULT = true;
	static final boolean INSERTABLE_DEFAULT = true;
	static final boolean UPDATABLE_DEFAULT = true;

	static final int LENGTH_DEFAULT = 255;
	static final int PRECISION_DEFAULT = 0;
	static final int SCALE_DEFAULT = 0;

	protected Column createColumn(javax.persistence.Column c) {
		Column column = new Column();
		String name = c.name();
		String secondaryTable = c.secondaryTable();
		Boolean unique = c.unique();
		Boolean nullable = c.nullable();
		BigInteger length = BigInteger.valueOf(c.length());
		BigInteger precision = BigInteger.valueOf(c.precision());
		BigInteger scale = BigInteger.valueOf(c.scale());
		Boolean insertable = c.insertable();
		Boolean updatable = c.updatable();
		String columnDefinition = c.columnDefinition();
		if (name.length() != 0)
			column.setName(name);
		if (secondaryTable.length() != 0)
			column.setSecondaryTable(secondaryTable);
		if (unique.booleanValue() != UNIQUE_DEFAULT)
			column.setUnique(unique);
		if (nullable.booleanValue() != NULLABLE_DEFAULT)
			column.setNullable(nullable);
		if (length.intValue() != LENGTH_DEFAULT)
			column.setLength(length);
		if (precision.intValue() != PRECISION_DEFAULT)
			column.setPrecision(precision);
		if (scale.intValue() != SCALE_DEFAULT)
			column.setScale(scale);
		if (insertable.booleanValue() != INSERTABLE_DEFAULT)
			column.setInsertable(insertable);
		if (updatable.booleanValue() != UPDATABLE_DEFAULT)
			column.setUpdatable(updatable);
		if (columnDefinition.length() != 0)
			column.setColumnDefinition(columnDefinition);
		return column;
	}
	
	protected void add(javax.persistence.Column c) {
		if (!done.contains(c)) {
			Basic basic = new Basic();
			basic.setAttribute(attribute);
			Column column = createColumn(c);
			basic.setColumn(column);
			mapping.getBasic().add(basic);
		}
	}
	
	protected void add(javax.persistence.ColumnResult a) {
	}
	
	protected void add(javax.persistence.DiscriminatorColumn a) {
		DiscriminatorColumn dc = new DiscriminatorColumn();
		String name = a.name();
		BigInteger length = BigInteger.valueOf(a.length());
		String columnDefinition = a.columnDefinition();
		dc.setName(name);
		dc.setLength(length);
		dc.setColumnDefinition(columnDefinition);
		((Entity)mapping).setDiscriminatorColumn(dc);
	}
	
	protected void add(javax.persistence.Embeddable a) {
		Embeddable embeddable = new Embeddable();
		String name = javaClass.getName();
		String s = a.access().toString();
		AccessType access = AccessType.valueOf(s);
		embeddable.setClassName(name);
		embeddable.setAccess(access);
		this.embeddable.add(embeddable);
	}
	
	protected void add(javax.persistence.EmbeddableSuperclass a) {
		mapping = new EmbeddableSuperclass();
		String s = a.access().toString();
		AccessType access = AccessType.valueOf(s);
		mapping.setAccess(access);
	}
	
	protected void add(javax.persistence.Embedded a) {
		state = State.Embedded;
		embedded = new Embedded();
		embedded.setAttribute(attribute);
		mapping.getEmbedded().add(embedded);
	}
	
	protected void add(javax.persistence.EmbeddedId a) {
		mapping.setEmbeddedId(attribute);
	}
	
	protected void add(javax.persistence.EntityListener a) {
		String name = a.value().getName();
		((Entity)mapping).setEntityListener(name);
	}
	
	protected void add(javax.persistence.EntityResult a) {
	}
	
	protected void add(javax.persistence.FieldResult a) {
	}
	
	protected void add(javax.persistence.FlushMode a) {
	}
	
	protected void add(javax.persistence.GeneratedIdTable a) {
	}
	
	protected void add(javax.persistence.Id a) {
		Id id = new Id();
		String type = a.generate().toString();
		String generator = a.generator();
		GeneratorType generate = GeneratorType.valueOf(type);
		Column column = getColumn();
		id.setAttribute(attribute);
		id.setGenerate(generate);
		if (generator.length() != 0)
			id.setGenerator(generator);
		id.setColumn(column);
		mapping.getId().add(id);
	}
	
	protected void add(javax.persistence.IdClass a) {
		String value = a.value().getName();
		mapping.setIdClass(value);
	}
	
	protected void add(javax.persistence.Inheritance a) {
		String st = a.strategy().toString();
		String dt = a.discriminatorType().toString();
		String value = a.discriminatorValue();
		InheritanceType strategy = InheritanceType.valueOf(st);
		DiscriminatorType type = DiscriminatorType.valueOf(dt);
		((Entity)mapping).setInheritanceStrategy(strategy);
		((Entity)mapping).setDiscriminatorType(type);
		if (value.length() != 0)
			((Entity)mapping).setDiscriminatorValue(value);
	}
	
	/**
	 * The join-column annotations are handled by their enclosing elements.
	 */
	protected void add(javax.persistence.JoinColumn a) {
	}
	
	protected void add(javax.persistence.JoinColumns a) {
	}
	
	/**
	 * The join-table annotations are handled by their enclosing elements 
	 * OneToMany and ManyToMany.
	 */
	protected void add(javax.persistence.JoinTable a) {
	}
	
	protected JoinTable getJoinTable() {
		JoinTable table = null;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(javax.persistence.JoinTable.class)) {
				javax.persistence.JoinTable c = javax.persistence.JoinTable.class.cast(a);
				table = createJoinTable(c);
				done.add(c);
			}
		}
		return table;
	}
	
	protected JoinTable createJoinTable(javax.persistence.JoinTable a) {
		JoinTable joinTable = new JoinTable();
		javax.persistence.Table t = a.table();
		if (t != null) {
			String name = t.name();
			String schema = t.schema();
			String catalog = t.catalog();
			if (name.length() != 0)
				joinTable.setName(name);
			if (schema.length() != 0)
				joinTable.setSchema(schema);
			if (catalog.length() != 0)
				joinTable.setCatalog(catalog);
		}
		copy(a.joinColumns(), joinTable.getJoinColumn());
		copy(a.inverseJoinColumns(), joinTable.getInverseJoinColumn());
		return joinTable;
	}

	protected void copy(javax.persistence.JoinColumn[] jca, 
			List<JoinColumn> joinColumnList) {
		for (javax.persistence.JoinColumn jc : jca) {
			JoinColumn joinColumn = createJoinColumn(jc);
			joinColumnList.add(joinColumn);
		}
	}
	
	protected JoinColumn getJoinColumn() {
		JoinColumn column = null;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(javax.persistence.JoinColumn.class)) {
				javax.persistence.JoinColumn c = javax.persistence.JoinColumn.class.cast(a);
				column = createJoinColumn(c);
				done.add(c);
			}
		}
		return column;
	}

	protected JoinColumn createJoinColumn(javax.persistence.JoinColumn c) {
		JoinColumn column = new JoinColumn();
		String name = c.name();
		String referencedColumnName = c.referencedColumnName();
		String secondaryTable = c.secondaryTable();
		Boolean unique = c.unique();
		Boolean nullable = c.nullable();
		Boolean insertable = c.insertable();
		Boolean updatable = c.updatable();
		String columnDefinition = c.columnDefinition();
		if (name.length() != 0)
			column.setName(name);
		if (referencedColumnName.length() != 0)
			column.setReferencedColumnName(referencedColumnName);
		if (secondaryTable.length() != 0)
			column.setSecondaryTable(secondaryTable);
		if (unique.booleanValue() != UNIQUE_DEFAULT)
			column.setUnique(unique);
		if (nullable.booleanValue() != NULLABLE_DEFAULT)
			column.setNullable(nullable);
		if (insertable.booleanValue() != INSERTABLE_DEFAULT)
			column.setInsertable(insertable);
		if (updatable.booleanValue() != UPDATABLE_DEFAULT)
			column.setUpdatable(updatable);
		if (columnDefinition.length() != 0)
			column.setColumnDefinition(columnDefinition);
		return column;
	}
	
	protected void add(javax.persistence.Lob a) {
		Lob lob = new Lob();
		Boolean opt = a.optional();
		String ft = a.fetch().toString(); 
		String lt = a.type().toString();
		LobType type = LobType.valueOf(lt);
		FetchType fetch = FetchType.valueOf(ft);
		lob.setAttribute(attribute);
		if (opt.booleanValue() != OPTIONAL_DEFAULT)
			lob.setOptional(opt);
		if (type != LobType.BLOB)
			lob.setLobType(type);
		if (fetch != FetchType.LAZY)
			lob.setFetch(fetch);
		Column column = getColumn();
		lob.setColumn(column);
		mapping.getLob().add(lob);
	}
	
	protected void add(javax.persistence.ManyToMany a) {
		state = State.ManyToMany;
		manyToMany = new ManyToMany();
		Class targetEntity = a.targetEntity();
		String ft = a.fetch().toString(); 
		FetchType fetch = FetchType.valueOf(ft);
		String mappedBy = a.mappedBy();
		JoinTable joinTable = getJoinTable();
		manyToMany.setAttribute(attribute);
		if (targetEntity != void.class) {
			String target = targetEntity.getName();
			manyToMany.setTargetEntity(target);
		}
		manyToMany.setFetch(fetch);
		manyToMany.setMappedBy(mappedBy);
		manyToMany.setJoinTable(joinTable);
		copy(a.cascade(), manyToMany.getCascade());
		mapping.getManyToMany().add(manyToMany);
	}

	protected void copy(javax.persistence.CascadeType[] ca, 
			List<CascadeType> cascadeList) {
		for (javax.persistence.CascadeType ct : ca) {
			String s = ct.toString();
			CascadeType cascade = CascadeType.valueOf(s);
			cascadeList.add(cascade);
		}
	}
	
	protected void add(javax.persistence.ManyToOne a) {
		ManyToOne element = new ManyToOne();
		Class targetEntity = a.targetEntity();
		String ft = a.fetch().toString(); 
		FetchType fetch = FetchType.valueOf(ft);
		Boolean opt = a.optional();
		element.setAttribute(attribute);
		if (targetEntity != void.class) {
			String target = targetEntity.getName();
			element.setTargetEntity(target);
		}
		copy(a.cascade(), element.getCascade());
		if (fetch != FETCH_DEFAULT)
			element.setFetch(fetch);
		if (opt.booleanValue() != OPTIONAL_DEFAULT)
			element.setOptional(opt);
		mapping.getManyToOne().add(element);
	}
	
	protected void add(javax.persistence.MapKey a) {
		String value = a.name();
		if (state == State.OneToMany) {
			oneToMany.setMapKey(value);
		}
		else if (state == State.ManyToMany) {
			manyToMany.setMapKey(value);
		}
	}
	
	protected void add(javax.persistence.NamedNativeQueries a) {
	}
	
	protected void add(javax.persistence.NamedNativeQuery a) {
	}
	
	protected void add(javax.persistence.NamedQueries a) {
	}
	
	protected void add(javax.persistence.NamedQuery a) {
	}
	
	protected void add(javax.persistence.OneToMany a) {
		state = State.OneToMany;
		oneToMany = new OneToMany();
		Class targetEntity = a.targetEntity();
		String ft = a.fetch().toString(); 
		FetchType fetch = FetchType.valueOf(ft);
		String mappedBy = a.mappedBy();
		JoinTable joinTable = getJoinTable();
		oneToMany.setAttribute(attribute);
		if (targetEntity != void.class) {
			String target = targetEntity.getName();
			oneToMany.setTargetEntity(target);
		}
		oneToMany.setFetch(fetch);
		oneToMany.setMappedBy(mappedBy);
		oneToMany.setJoinTable(joinTable);
		copy(a.cascade(), oneToMany.getCascade());
		mapping.getOneToMany().add(oneToMany);
	}
	
	protected void add(javax.persistence.OneToOne a) {
		state = State.OneToOne;
		oneToOne = new OneToOne();
		Class targetEntity = a.targetEntity();
		String ft = a.fetch().toString(); 
		FetchType fetch = FetchType.valueOf(ft);
		Boolean opt = a.optional();
		String mappedBy = a.mappedBy();
		oneToOne.setAttribute(attribute);
		if (targetEntity != void.class) {
			String target = targetEntity.getName();
			oneToOne.setTargetEntity(target);
		}
		copy(a.cascade(), oneToOne.getCascade());
		oneToOne.setFetch(fetch);
		oneToOne.setOptional(opt);
		if (mappedBy.length() != 0)
			oneToOne.setMappedBy(mappedBy);
		mapping.getOneToOne().add(oneToOne);
	}
	
	protected void add(javax.persistence.OrderBy a) {
		String value = a.value();
		if (state == State.OneToMany) {
			oneToMany.setOrderBy(value);
		}
		else if (state == State.ManyToMany) {
			manyToMany.setOrderBy(value);
		}
	}
	
	protected void add(javax.persistence.PersistenceContext a) {
	}
	
	protected void add(javax.persistence.PersistenceContexts a) {
	}
	
	protected void add(javax.persistence.PersistenceUnit a) {
	}
	
	protected void add(javax.persistence.PersistenceUnits a) {
	}
	
	protected void add(javax.persistence.PostLoad a) {
		((Entity)mapping).setPostLoad(method);
	}
	protected void add(javax.persistence.PostPersist a) {
		((Entity)mapping).setPostPersist(method);
	}
	protected void add(javax.persistence.PostRemove a) {
		((Entity)mapping).setPostRemove(method);
	}
	protected void add(javax.persistence.PostUpdate a) {
		((Entity)mapping).setPostUpdate(method);
	}
	protected void add(javax.persistence.PrePersist a) {
		((Entity)mapping).setPrePersist(method);
	}
	protected void add(javax.persistence.PreRemove a) {
		((Entity)mapping).setPreRemove(method);
	}
	protected void add(javax.persistence.PreUpdate a) {
		((Entity)mapping).setPreUpdate(method);
	}
	
	protected void add(javax.persistence.PrimaryKeyJoinColumn a) {
		PrimaryKeyJoinColumn pkJoin = createPrimaryKeyJoinColumn(a);
		if (state == State.Mapping) {
			((Entity)mapping).getPrimaryKeyJoinColumn().add(pkJoin);
		}
		else if (state == State.OneToOne) {
			oneToOne.getPrimaryKeyJoinColumn().add(pkJoin);
		}
	}
	
	protected void add(javax.persistence.PrimaryKeyJoinColumns a) {
		for (javax.persistence.PrimaryKeyJoinColumn pkjc : a.value()) {
			add(pkjc);
		}
	}
	
	protected void add(javax.persistence.SecondaryTable a) {
		SecondaryTable table = new SecondaryTable();
		String name = a.catalog();
		String schema = a.schema();
		String catalog = a.catalog();
		table.setName(name);
		if (schema.length() != 0)
			table.setSchema(schema);
		if (catalog.length() != 0)
			table.setCatalog(catalog);
		copy(a.pkJoin(), table.getPrimaryKeyJoinColumn());
		copy(a.uniqueConstraints(), table.getUniqueConstraint());
		((Entity)mapping).getSecondaryTable().add(table);
	}
	
	protected void copy(javax.persistence.PrimaryKeyJoinColumn[] pkjca, 
			List<PrimaryKeyJoinColumn> joinColumnList) {
		for (javax.persistence.PrimaryKeyJoinColumn pkjc : pkjca) {
			PrimaryKeyJoinColumn joinColumn = createPrimaryKeyJoinColumn(pkjc);
			joinColumnList.add(joinColumn);
		}
	}
		
	protected PrimaryKeyJoinColumn createPrimaryKeyJoinColumn(
			javax.persistence.PrimaryKeyJoinColumn c) {
		PrimaryKeyJoinColumn column = new PrimaryKeyJoinColumn();
		String name = c.name();
		String referencedColumnName = c.referencedColumnName();
		String columnDefinition = c.columnDefinition();
		if (name.length() != 0)
			column.setName(name);
		if (referencedColumnName.length() != 0)
			column.setReferencedColumnName(referencedColumnName);
		if (columnDefinition.length() != 0)
			column.setColumnDefinition(columnDefinition);
		return column;
	}
	
	protected void copy(javax.persistence.UniqueConstraint[] uca, 
			List<UniqueConstraint> constraintList) {
		for (javax.persistence.UniqueConstraint uc : uca) {
			UniqueConstraint constraint = createUniqueConstraint(uc);
			constraintList.add(constraint);
		}
	}
		
	protected UniqueConstraint createUniqueConstraint(
			javax.persistence.UniqueConstraint c) {
		UniqueConstraint element = new UniqueConstraint();
		for (String s : c.columnNames()) {
			element.getColumnName().add(s);
		}
		return element;
	}
	
	protected void add(javax.persistence.SecondaryTables a) {
		for (javax.persistence.SecondaryTable st : a.value()) {
			add(st);
		}
	}
	
	protected void add(javax.persistence.SequenceGenerator a) {
		SequenceGenerator sequenceGenerator = new SequenceGenerator();
		String name = a.name();
		String sequenceName = a.sequenceName();
		BigInteger initialValue = BigInteger.valueOf(a.initialValue());
		BigInteger allocationSize = BigInteger.valueOf(a.allocationSize());
		sequenceGenerator.setName(name);
		sequenceGenerator.setSequenceName(sequenceName);
		sequenceGenerator.setInitialValue(initialValue);
		sequenceGenerator.setAllocationSize(allocationSize);
	}
	
	protected void add(javax.persistence.Table a) {
		Table table = createTable(a);
		copy(a.uniqueConstraints(), table.getUniqueConstraint());
		((Entity)mapping).setTable(table);
	}
	
	protected Table createTable(javax.persistence.Table a) {
		Table table = new Table();
		String name = a.name();
		String schema = a.schema();
		String catalog = a.catalog();
		if (name.length() != 0)
			table.setName(name);
		if (schema.length() != 0)
			table.setSchema(schema);
		if (catalog.length() != 0)
			table.setCatalog(catalog);
		return table;
	}
	
	protected void add(javax.persistence.TableGenerator a) {
		TableGenerator tableGenerator = new TableGenerator();
		String name = a.name();
		Table table = createTable(a.table());
		String pkColumnName = a.pkColumnName();
		String pkColumnValue = a.pkColumnValue();
		String valueColumnName = a.valueColumnName();
		BigInteger initialValue = BigInteger.valueOf(a.initialValue());
		BigInteger allocationSize = BigInteger.valueOf(a.allocationSize());
		tableGenerator.setName(name);
		tableGenerator.setTable(table);
		tableGenerator.setPkColumnName(pkColumnName);
		tableGenerator.setPkColumnName(pkColumnValue);
		tableGenerator.setValueColumnName(valueColumnName);
		tableGenerator.setInitialValue(initialValue);
		tableGenerator.setAllocationSize(allocationSize);
		this.tableGenerator.add(tableGenerator);
	}
	
	protected void add(javax.persistence.Transient a) {
		mapping.getTransient().add(attribute);
	}
	
	/**
	 * This is handled by add Table and SecondaryTable
	 */
	protected void add(javax.persistence.UniqueConstraint a) {
	}
	
	protected void add(javax.persistence.Version a) {
		Version version = new Version();
		version.setAttribute(attribute);
		Column column = getColumn();
		version.setColumn(column);
		mapping.setVersion(version);
	}
	
}
