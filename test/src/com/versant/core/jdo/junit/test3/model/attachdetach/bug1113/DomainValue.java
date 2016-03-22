
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
package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import java.io.Serializable;

/**
 * This interface is for tagging domain values. All non abstract classes
 * implementing this interface must be IMMUTABLE! It is strongly recommended
 * to make all classes implementing this interface FINAL.
 *
 */
public interface DomainValue extends Cloneable, Serializable {

    /**
     * Default equals method has to be reimplemented.
     */
    public boolean equals(Object object);

    /**
     * Default hashCode method has to be reimplemented.
     */
    public int hashCode();

    /**
     * Default toString method has to be reimplemented.
     */
    public String toString();
}

