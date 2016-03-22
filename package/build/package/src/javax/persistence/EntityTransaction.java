
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
package javax.persistence;

/**
 * Warning: this class is from the EJB 3.0 Persistence API Early Draft 2 published
 * on February 7, 2005, and as such is likely to change signifcantly in the future
 * as the specification nears finalization.
 */
public interface EntityTransaction {

    /**
     * Start a resource transaction.
     *
     * @throws IllegalStateException if isActive() is true.
     */
    public abstract void begin();

    /**
     * Commit the current transaction, writing any unflushed changes to the database.
     *
     * @throws IllegalStateException - if isActive() is false.
     */
    public abstract void commit();

    /**
     * Roll back the current transaction.
     *
     * @throws IllegalStateException - if isActive() is false.
     */
    public abstract void rollback();

    /**
     * Check to see if a transaction is in progress.
     */
    public abstract boolean isActive();
}
