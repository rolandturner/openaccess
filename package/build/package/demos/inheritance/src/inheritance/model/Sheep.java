
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
package inheritance.model;

/**
 * Non-dangerous farm animal.
 */
public class Sheep extends FarmAnimal {

	private String woolType;

    public Sheep() {
	}
    
    public Sheep(String woolType) {
    	this.woolType = woolType;
    }

	public String getWoolType() {
		return woolType;
	}

	public void setWoolType(String woolType) {
		this.woolType = woolType;
	}
    
}
