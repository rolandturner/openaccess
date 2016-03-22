
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
package com.versant.core.jdo.junit.test2.model.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 *
 */
public class DelDepParent {

    private ArrayList children = new ArrayList(10);

    public DelDepParent() {
    }

    public List getChildren() {
        return children;
    }

    public boolean addChildren(DelDepChild delDepChild) {
        return children.add(delDepChild);
    }

    public boolean removeChildren(DelDepChild delDepChild) {
        return children.remove(delDepChild);
    }
}
