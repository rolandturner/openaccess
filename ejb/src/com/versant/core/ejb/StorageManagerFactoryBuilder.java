
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

import com.versant.core.common.config.ConfigInfo;
import com.versant.core.metadata.parser.JdoRoot;

import javax.persistence.spi.PersistenceInfo;

import java.util.*;

/**
 * Bean to create StorageManagerFactory's. This needs some more work to avoid
 * referencing the SMF implementation classes directly as it does now.
 */
public class StorageManagerFactoryBuilder extends 
				com.versant.core.storagemanager.StorageManagerFactoryBuilder {

    protected void getMetaData() {
        ConfigInfo config = this.getConfig();
        ClassLoader loader = this.getLoader();
        if (config.jdoMetaData == null) {
        	com.versant.core.ejb.MetaDataParser p = 
            	new com.versant.core.ejb.MetaDataParser(loader);
            List<JdoRoot> jdoRootList = new ArrayList<JdoRoot>();
            p.parse(config.jdoResources, jdoRootList);
            PersistenceInfo info = (PersistenceInfo)
            					config.props.get("persistence.info");
            if (info != null) {
            	p.parse(info, jdoRootList);            	
            }
            config.jdoMetaData = new JdoRoot[jdoRootList.size()]; 
            jdoRootList.toArray(config.jdoMetaData);
        }
    }
}

