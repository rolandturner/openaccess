
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
package com.versant.core.vds;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.Debug;

/**
 * Strips package name from class names and leaves field names as is.
 */
public class BriefNamingPolicy implements NamingPolicy {

    public static final String ALIAS = "BRIEF";

    public String mapClassName(final ClassMetaData cmd) {
//        assert cmd != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(cmd != null,
                    "ClassMetaData is null");
        }

        return cmd.jdoClass.name;
    }

    public String mapFieldName(FieldMetaData fm) {
//        assert fm != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(fm != null,
                    "FieldMetaData is null");
        }
        return fm.name;
    }

}
