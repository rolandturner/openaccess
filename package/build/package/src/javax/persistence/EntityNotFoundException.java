
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
package javax.persistence;

public class EntityNotFoundException extends RuntimeException {
	
	public EntityNotFoundException(){
		super();
	}

	public EntityNotFoundException(String message){
		super(message);
	}
};
