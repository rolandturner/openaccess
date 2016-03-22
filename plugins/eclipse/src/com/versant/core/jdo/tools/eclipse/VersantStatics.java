
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
package com.versant.core.jdo.tools.eclipse;

/**
 */
public interface VersantStatics {
    public static boolean ECLIPSE_DEBUG = false;

    public static final String ID_JDGENIE_NATURE = "Versant.jdoNature";

    public static final String PROP_CONFIG_FILE = "jdogenie-config-file-name";
    public static final String PROP_TOKEN_FILE = "jdogenie-token-file-name";
    public static final String PROP_REL_PATH = "jdogenie-relative-search-path";
    public static final String PROP_COPY_PROJECT_FILE = "jdogenie-copy-project-file";

    public static final boolean DEFAULT_COPY_PROJECT_FILE = true;
    public static final boolean DEFAULT_RELATIVE_PATHS = true;
    public static final boolean DEFAULT_AUTO_ENHANCE = false;
    public static final String DEFAULT_PROJECT_FILE = "versant.properties";
}
