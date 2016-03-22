
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
package com.versant.core.common;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.util.FastExternalizable;

/**
 * This is an abstract base class for OID classes. Each PC class has its own
 * implementation generated for it at runtime. These implementations have fast
 * implementations of various methods in the base class designed for different
 * data stores.
 * </p>
 * Implementations must define equals and hashcode so that OIDs for the same
 * class hierarchy with the same datastore or application identity are equal and
 * have the same hashcode.
 * </p>
 * Some of the methods in this interface can be implemented by calling other
 * methods. This is deliberate as a generated hyperdrive OID class can
 * hardcode the return value and avoid having to dereference a field every
 * time. Example: OID.getClassIndex() == OID.getClassMetaData().index.
 * </p>
 * This interface must not extend Externalizable. Subclasses except for
 * NewObjectOID must implement Externalizable so they can be serialized
 * using writeObject. NewObjectOID must be serialized using the
 * FastExternalizable methods instead and this restriction enforces this
 * (NotSerializable exception if it happens).
 */
public interface OID extends Comparable, FastExternalizable {

    /**
     * Is this an OID for a new object?
     */
    public boolean isNew();

    /**
     * If this is a real OID (i.e. it has been assigned in the database)
     * then return itself. Otherwise return the corresponding real OID or
     * null if none has been allocated yet.
     */
    public OID getRealOID();

    /**
     * If this is a real OID (i.e. it has been assigned in the database)
     * then return itself. Otherwise return the corresponding real OID or
     * this if none has been allocated yet. This differs from
     * {@link #getRealOID()} in that it always returns an OID reference
     * even if this is a new OID and no real OID has been allocated.
     */
    public OID getAvailableOID();

    /**
     * Do we know the actual class of the object we are referencing? An
     * OID may be created from a reference to a base class. The actual
     * class of the object referenced might only be detirmined when its
     * State is fetched from the store.
     *
     * @see #resolve
     */
    public boolean isResolved();

    /**
     * Resolve this OID from the state. This will update our class index
     * to reflect the state. It is a NOP to call this on an already
     * resolved OID.
     *
     * @see #isResolved
     */
    public void resolve(State state);

    /**
     * Get the meta data for our class. This will throw a RuntimeException
     * if called on an unresolved or untyped OID.
     *
     * @see #isResolved
     * @see #resolve
     */
    public ClassMetaData getClassMetaData();

    /**
     * Get whatever meta data is currently available for our class. The
     * actual class may be a subclass of this. This will return null for
     * an untyped OID.
     */
    public ClassMetaData getAvailableClassMetaData();

    /**
     * Get the meta data for the least derived class in our hierarchy.
     * This is getAvailableClassMetaData().top but this method can be
     * hardcoded in a generated class.  This will return null for
     * an untyped OID.
     */
    public ClassMetaData getBaseClassMetaData();

    /**
     * Return the index of our PC class in the meta data. This can be
     * called on an unresolved OID and the actual class may
     * be a subclass of the class for the returned index.
     * This is getAvailableClassMetaData().index but this method can be
     * hardcoded in a generated class.
     */
    public int getClassIndex();

    /**
     * Get the identity type of the class we are referencing. It is ok to
     * call this for an unresolved OID as the identity type is the same for
     * all classes in a hierarchy.
     */
    public int getIdentityType();

    /**
     * Populate this OID from the array of Objects supplied. These will
     * come from some sort of key generator (e.g. a JdbcKeyGenerator).
     * This is used to construct OIDs for newly created objects. Note that
     * data may contain extra garbage objects from previously usages.
     */
    public void copyKeyFields(Object[] data);

    /**
     * Return a copy of the oid.
     */
    public OID copy();

    /**
     * Fill in this OID from an instance of the objectid-class for the
     * hierarchy.
     */
    public void fillFromPK(Object pk);

    /**
     * Fill this OID from its toString. The classid will have already been
     * parsed out with index indicating the first character after the
     * separator.
     */
    public void fillFromIDString(String idString, int index);

    /**
     * Fill this OID from the given id. If objectIdClass is mapped then 'id' will
     * be assumed to be a instance of the objectIdClass, else 'id' is assumed to
     * be a 'pk' for a class with a single pk field.
     * @param id
     */
    public OID fillFromIDObject(Object id);

    /**
     * Encode the 'primary key' of this OID as a String. This is used for
     * pretty printing OIDs in the workbench.
     */
    public String toPkString();

    /**
     * Encode the 'primary key' of this OID as a String. This is used for
     * debugging.
     */
    public String toSString();

    /**
     * Get the toString of this OID even if it has not been resolved.
     */
    public String toStringImp();

    /**
     * The IBM VMs have a problem with unimplemented interface methods in
     * classes so we need this.
     */
    public int compareTo(Object o);

    /**
     * Return the primary key stored in this OID as an int. This will only
     * be called for datastore identity classes.
     */
    public long getLongPrimaryKey();

    /**
     * Set the primary key stored in this OID as an int. This will only be
     * called for datastore identity classes.
     */
    public void setLongPrimaryKey(long pk);

    /**
     * Create and populate an instance of the objectid-class for our class from
     * this OID. This must throw a JDOFatalInternalException if invoked on an
     * OID for a datastore identity class.
     */
    public Object createObjectIdClassInstance();
    
    /**
     * Get the classId from the available class meta data or -1 if this is
     * an untyped OID.
     */
    public int getAvailableClassId();

}
