
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
package petstore.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Collection;

/**
 */
public class Page implements Serializable {

    public static final Page EMPTY_PAGE =  new Page();

    Collection objects;
    Collection oids;
    int start;

    boolean hasNext;

    public Page() {
        this(Collections.EMPTY_LIST, 0, false);
    }

    public Page(Collection c, int start, boolean hasNext) {
        this.objects = c;
        this.start = start;

        this.hasNext = hasNext;
    }

    public Page(Collection c, Collection oids, int start, boolean hasNext) {
        this(c, start, hasNext);
        this.oids = oids;
    }

    public Collection getObjects() { return objects; }

    public Collection getOids() {
        return oids;
    }

    public void setOids(Collection oids) {
        this.oids = oids;
    }

    public boolean getHasNextPage() {
        return hasNext;
    }

    public boolean getHasPreviousPage() {
        return start > 0;
    }

    public int getStartOfNextPage() { return start + objects.size(); }

    public int getStartOfPreviousPage() {
        return Math.max(start-objects.size(), 0);
    }

    public int getSize() { return objects.size(); }
}
