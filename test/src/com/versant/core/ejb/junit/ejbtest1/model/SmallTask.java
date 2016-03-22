
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
package com.versant.core.ejb.junit.ejbtest1.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Inheritance;

@Entity
@Table(name = "QUERY_TASK")
@Inheritance(discriminatorValue = "S")
public class SmallTask extends Task {
}

