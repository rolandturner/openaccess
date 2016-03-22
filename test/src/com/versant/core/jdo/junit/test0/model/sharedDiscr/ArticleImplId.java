
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
package com.versant.core.jdo.junit.test0.model.sharedDiscr;

import java.io.Serializable;

public class ArticleImplId implements Serializable {
	static {
		// register persistent class in JVM
		Class c = Article.class;
	}

	public Long id;

	public ArticleImplId() {
	}

	public ArticleImplId(String fromString) {
		if ("null".equals(fromString))
			id = null;
		else
			id = new Long(fromString);
	}

	public String toString() {
		return String.valueOf(id);
	}

	public int hashCode() {
		return ((id == null) ? 0 : id.hashCode());
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ArticleImplId))
			return false;

		ArticleImplId other = (ArticleImplId) obj;
		return ((id == null && other.id == null) || (id != null && id
				.equals(other.id)));
	}
}
