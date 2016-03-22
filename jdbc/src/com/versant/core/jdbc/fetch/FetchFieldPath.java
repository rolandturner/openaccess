
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
package com.versant.core.jdbc.fetch;

import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.exp.SelectExp;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a class to maintains the 'stack' of fields that describes the path to the current possition.
 */
public class FetchFieldPath {
    private List pathList = new ArrayList();

    public FetchFieldPath() {
    }

    FetchFieldPath(List pathList) {
        this.pathList = pathList;
    }

    /**
     * return a copy of the current path.
     */
    public FetchFieldPath getCopy() {
        return new FetchFieldPath(new ArrayList(pathList));
    }

    public FetchFieldPath add(JdbcRefField field) {
        pathList.add(new PathFieldWrapper(field, false));
        return this;
    }

    public FetchFieldPath add(JdbcCollectionField field) {
        pathList.add(new PathFieldWrapper(field, false));
        return this;
    }

    public FetchFieldPath add(JdbcMapField field, boolean keyPath) {
        pathList.add(new PathFieldWrapper(field, keyPath));
        return this;
    }

    public SelectExp createJoinPath(SelectExp start) {
        SelectExp root = start;
        //do the first join. Depending on the mapping the 'start' selectExp's table
        //might be correct for the first field and then no join is needed.
        if (pathList.isEmpty()) return start;

        for (int i = 0; i < pathList.size(); i++) {
            PathFieldWrapper pathFieldWrapper = (PathFieldWrapper) pathList.get(i);
            root = pathFieldWrapper.field.addParColJoin(root, pathFieldWrapper.keyJoin);
        }
        return root;
    }

    class PathFieldWrapper {
        private final JdbcField field;
        private final boolean keyJoin;

        public PathFieldWrapper(JdbcField field, boolean keyPath) {
            this.field = field;
            this.keyJoin = keyPath;
        }
    }
}
