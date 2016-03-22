
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

import com.versant.core.metadata.ClassMetaData;

import java.util.HashMap;

/**
 * The keeps EntityLifecycleManager track of all the Lifecycle callbacks and
 * listeners, there is just one of these classes for a EntityManagerFactory and
 * is immutable.
 * There is a all listener at the end of the managers array, it is used
 * to check if we have a spesific type of listener if we do not first want to
 * lookup the class (it is alot faster this way).
 */
public class EntityLifecycleManager {
    private int allListener;
    private EntityClassLifecycleManager[] managers;
    private HashMap classToCmd;

    public EntityLifecycleManager(EntityClassLifecycleManager[] managers) {
        this.managers = managers;
        if (managers != null) {
            this.allListener = managers.length - 1;
        }
    }

    public void setCassToCmd(HashMap classMap) {
        this.classToCmd = classMap;
    }

    private int getIndex(Class cls) {
        return ((ClassMetaData) classToCmd.get(cls)).index;
    }

    /**
     * The PrePersist and PreRemove callback methods are invoked for a given
     * entity before the respective EntityManager persist and remove operations
     * for that entity are executed, as specified in section 3.2.
     * These callbacks will also be invoked on all entities to which these
     * operations are cascaded. The PrePersist and PreRemove methods will always
     * be invoked as part of the synchronous persist and remove operations.
     * Exceptions thrown by any of these callbacks cause the current transaction
     * to be rolled back.
     * Note: Before calling this method, we must first check hasPrePersistListeners()
     */
    public void firePrePersist(Object src) {
        EntityClassLifecycleManager classManager =
                managers[getIndex(src.getClass())];
        if (classManager != null) {
            classManager.firePrePersist(src);
        }
    }

    /**
     * The PostPersist and PostRemove callback methods are invoked for an entity
     * after the respective EntityManager persist and remove operations for that
     * entity are executed. These callbacks will also be invoked on all entities
     * to which these operations are cascaded. The PostPersist and PostRemove
     * methods will be invoked after the database insert and delete operations
     * respectively.
     * This may be directly after the persist or remove operations have been
     * invoked or it may be directly after a flush operation has occurred or it
     * may be at the end of the transaction.
     * Exceptions thrown by any of these callbacks cause the current transaction
     * to be rolled back.
     *
     * Note: Before calling this method, we must first check hasPostPersistListeners()
     */
    public void firePostPersist(Object src, int classIndex) {
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePostPersist(src);
        }
    }

    /**
     * The PrePersist and PreRemove callback methods are invoked for a given
     * entity before the respective EntityManager persist and remove operations
     * for that entity are executed, as specified in section 3.2.
     * These callbacks will also be invoked on all entities to which these
     * operations are cascaded. The PrePersist and PreRemove methods will always
     * be invoked as part of the synchronous persist and remove operations.
     * Exceptions thrown by any of these callbacks cause the current transaction
     * to be rolled back.
     *
     * Note: Before calling this method, we must first check hasPreRemoveListeners()
     */
    public void firePreRemove(Object src, int classIndex) {
        if (src == null) return;
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePreRemove(src);
        }
    }

    /**
     * The PostPersist and PostRemove callback methods are invoked for an entity
     * after the respective EntityManager persist and remove operations for that
     * entity are executed. These callbacks will also be invoked on all entities
     * to which these operations are cascaded. The PostPersist and PostRemove
     * methods will be invoked after the database insert and delete operations
     * respectively.
     * This may be directly after the persist or remove operations have been
     * invoked or it may be directly after a flush operation has occurred or it
     * may be at the end of the transaction.
     * Exceptions thrown by any of these callbacks cause the current transaction
     * to be rolled back.
     *
     * Note: Before calling this method, we must first check hasPostRemoveListeners()
     */
    public void firePostRemove(Object src, int classIndex) {
        if (src == null) return;
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePostRemove(src);
        }
    }

    /**
     * The PreUpdate and PostUpdate callbacks occur before and after the database
     * update operations to entity data respectively.
     * This may be at the time the entity state is updated or it may be at the
     * time state is flushed to the database or at the end of the transaction.
     *
     * Note: Before calling this method, we must first check hasPreUpdateListeners()
     */
    public void firePreUpdate(Object src, int classIndex) {
        if (src == null) return;
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePreUpdate(src);
        }
    }

    /**
     * The PreUpdate and PostUpdate callbacks occur before and after the database
     * update operations to entity data respectively.
     * This may be at the time the entity state is updated or it may be at the
     * time state is flushed to the database or at the end of the transaction.
     *
     * Note: Before calling this method, we must first check hasPostUpdateListeners()
     */
    public void firePostUpdate(Object src, int classIndex) {
        if (src == null) return;
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePostUpdate(src);
        }
    }

    /**
     * The PostLoad method for an entity is invoked after the entity has been
     * loaded into the current persistence context from the database or after
     * the refresh operation has been applied to it. The PostLoad method is
     * invoked before a query result is returned or accessed or before an
     * association is traversed.
     *
     * Note: Before calling this method, we must first check hasPostLoadListeners()
     */
    public void firePostLoad(Object src, int classIndex) {
        if (src == null) return;
        EntityClassLifecycleManager classManager = managers[classIndex];
        if (classManager != null) {
            classManager.firePostLoad(src);
        }
    }

    /**
     * Do we have any PrePersist listeners?
     */
    public boolean hasPrePersistListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPrePersistListeners();
    }

    /**
     * Do we have any PostPersist listeners?
     */
    public boolean hasPostPersistListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPostPersistListeners();
    }

    /**
     * Do we have any PreRemove listeners?
     */
    public boolean hasPreRemoveListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPreRemoveListeners();
    }

    /**
     * Do we have any PostRemove listeners?
     */
    public boolean hasPostRemoveListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPostRemoveListeners();
    }

    /**
     * Do we have any PreUpdate listeners?
     */
    public boolean hasPreUpdateListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPreUpdateListeners();
    }

    /**
     * Do we have any PostUpdate listeners?
     */
    public boolean hasPostUpdateListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPostUpdateListeners();
    }

    /**
     * Do we have any PostLoad listeners?
     */
    public boolean hasPostLoadListeners() {
        if (managers == null) return false;
        return managers[allListener].hasPostLoadListeners();
    }

}

