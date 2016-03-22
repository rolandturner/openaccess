
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
package com.versant.core.jdo.tools.workbench.editor;

/**
 * @keep-all
 */
class TokenMarkerDebugger {
	public static final int MAX_COUNT = 100;

	TokenMarkerDebugger() {}

	// Following is a way to detect whether tokenContext.pos is not 
	// correctly incremented. This is for debugging purposes
	public boolean isOK(final TokenMarkerContext tokenContext) {
		if (tokenContext.pos <= this.pos) {
			this.count++;
			if (this.count > MAX_COUNT) {
				// Seems that we got stuck somewhere
				this.pos   = tokenContext.pos + 1;
				this.count = 0;
				return false;
			}
			return true;
		} else {
			this.pos   = tokenContext.pos;
			this.count = 0;
			return true;
		}
	}

	public void reset() {
		this.pos   = -1;
		this.count = 0;
	}
	private int pos   = -1;
	private int count = 0;
}
