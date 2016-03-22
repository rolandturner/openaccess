
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

import java.util.Map;
import java.util.HashMap;

/**
 * This contains maps that convert Strings to int codes for meta data
 * options. They are not static so this class can be completly discarded
 * when the meta data has been read.
 */
public class MetaDataEnums {

    public static final String OPTIMISTIC_LOCKING_NONE = "none";
    public static final String OPTIMISTIC_LOCKING_VERSION = "version";
    public static final String OPTIMISTIC_LOCKING_TIMESTAMP = "timestamp";
    public static final String OPTIMISTIC_LOCKING_CHANGED = "changed";

    public final Map CACHE_ENUM;
    public final Map AUTOSET_ENUM;

    public MetaDataEnums() {
        CACHE_ENUM = new HashMap();
        CACHE_ENUM.put("no", new Integer(MDStatics.CACHE_STRATEGY_NO));
        CACHE_ENUM.put("yes", new Integer(MDStatics.CACHE_STRATEGY_YES));
        CACHE_ENUM.put("all", new Integer(MDStatics.CACHE_STRATEGY_ALL));

        AUTOSET_ENUM = new HashMap();
        AUTOSET_ENUM.put("no", new Integer(MDStatics.AUTOSET_NO));
        AUTOSET_ENUM.put("created", new Integer(MDStatics.AUTOSET_CREATED));
        AUTOSET_ENUM.put("modified", new Integer(MDStatics.AUTOSET_MODIFIED));
        AUTOSET_ENUM.put("both", new Integer(MDStatics.AUTOSET_BOTH));
    }
}
