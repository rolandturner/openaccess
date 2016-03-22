
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

import java.util.Properties;
import com.versant.core.common.config.ConfigInfo;

public class ConfigParser extends 
					com.versant.core.common.config.ConfigParser {

    private ClassLoader classLoader = ConfigParser.class.getClassLoader();

    /**
     * Parse the supplied properties resource and create a Config instance.
     */
    public ConfigInfo parseResource(String filename, ClassLoader cl) {
        classLoader = cl;
        return super.parseResource(filename, cl);
    }
    
	public ConfigInfo parse(Properties p) {
        javax.persistence.Persistence.addPersistenceInfo(p, classLoader);
		return super.parse(p);
	}

}
