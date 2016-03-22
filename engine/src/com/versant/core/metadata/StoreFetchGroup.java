
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
package com.versant.core.metadata;

/**
 * Extra store specific into attached to a FetchGroup. Implementing classes
 * must have a public noarg constructor.
 */
public interface StoreFetchGroup {

    /**
     * This is called once after the StoreFetchGroup instance has been created.
     */
    public void setFetchGroup(FetchGroup fg);

    /**
     * A field has been newly added to the group.
     */
    public void fieldAdded(FieldMetaData fmd);

    /**
     * Finish initialization of this fetch group. This is called at the
     * end of {@link FetchGroup#finish}.
     */
    public void finish();

}
