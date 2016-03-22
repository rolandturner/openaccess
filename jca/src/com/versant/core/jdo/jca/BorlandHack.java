
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
package com.versant.core.jdo.jca;

import com.versant.core.jdo.VersantPersistenceManagerFactory;

/**
 * This class is a workaround for Borland app server, there is a bug is borlands
 * veryfier that does not pick up that it implements java.io.Serializable, if we
 * extend this abstract class then the bug does not come into play.
 */
public abstract class BorlandHack implements VersantPersistenceManagerFactory {
}

