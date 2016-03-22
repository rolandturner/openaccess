
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
package com.versant.core.jdo.junit.test3.model.attachdetach.externalizer;

import com.versant.core.jdo.externalizer.Externalizer;

import java.io.Serializable;

public class CardTypes {

    public static CardTypes AmericanExpress = new CardTypes(0,"American Express");
    public static CardTypes DinersClub = new CardTypes(1,"Diners Club");
    public static CardTypes Visa = new CardTypes(2,"Visa");
    public static CardTypes MasterCard = new CardTypes(3,"Mastercard");

    private static CardTypes[] values = new CardTypes[]{AmericanExpress, DinersClub, Visa, MasterCard};

    private int value;
    private String label;

    CardTypes(int value,String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static CardTypes[] values() {
        return values;
    }

    public String toString() {
        return label;
    }

    public static class TypeExternalizer implements Externalizer, Serializable  {

        /**
         *
         * @param pm
         * @param o
         */
        public Object toExternalForm(Object pm, Object o) {
            if (o == null){
                return null;
            } else {
                return new Integer(((CardTypes) o).getValue());
            }
        }

        /**
         *
         * @param pm
         * @param o
         */
        public Object fromExternalForm(Object pm, Object o) {
            int value = ((Integer)o).intValue();
            CardTypes[] types = CardTypes.values();
            if (value >= 0 && value < types.length) {
                return types[value];
            }
            throw new IllegalArgumentException("Invalid Card Type: " + value);
        }

        /**
         *
         */
        public Class getExternalType() {
            return Integer.TYPE;
        }
    }
}

