
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

import java.io.Serializable;

/**
 * Non-dangerous farm animal.
 */
public class Cow extends FarmAnimal {

	private int number;
	private int milkRating;

    public Cow() {
	}
    
	public Cow(int number, int milkRating) {
		this.number = number;
		this.milkRating = milkRating;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getMilkRating() {
		return milkRating;
	}

	public void setMilkRating(int milkRating) {
		this.milkRating = milkRating;
	}    

    /**
     * Application identity objectid-class.
     */
    public static class ID implements Serializable {

        public int number;

        public ID() {
        }

        public ID(String s) {
            number = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (number != id.number) return false;

            return true;
        }

        public int hashCode() {
            return number;
        }

        public String toString() {
            return Integer.toString(number);
        }

    }
}
