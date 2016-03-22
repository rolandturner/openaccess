
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
/**
 * 
 */
package com.versant.persistence;

import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceInfo;

/**
 * Implements the <code>javax.persistence.spi.PersistenceProvider</code>
 * interface.
 * @author Rick George
 * @version 1.0, 8/15/05
 * @see javax.persistence.spi.PersistenceProvider
 */
public class PersistenceProvider implements
		javax.persistence.spi.PersistenceProvider {

	/**
	 * @see javax.persistence.spi.PersistenceProvider#createEntityManagerFactory(java.lang.String, java.util.Map)
	 */
	public EntityManagerFactory createEntityManagerFactory(String emName, Map map) {
		EntityManagerFactory emf = null;
		try {
			emf = com.versant.core.ejb.EntityManagerFactoryImp.getEntityManagerFactory(
							(Properties)map);		
		}
		catch (PersistenceException e) {
		}
		return emf; 
	}

	/**
	 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory(javax.persistence.spi.PersistenceInfo)
	 */
	public EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceInfo info) {
		EntityManagerFactory emf = null;
		Properties properties = info.getProperties();
		try {
			emf = com.versant.core.ejb.EntityManagerFactoryImp.getEntityManagerFactory(
							properties);		
		}
		catch (PersistenceException e) {
		}
		return emf; 
	}

}
