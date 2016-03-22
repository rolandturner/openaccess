
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
package com.versant.core.jdo.junit.test0.model;

/**
 * @keep-all
 */
public class MetaTestB extends MetaTestA {

    private int f0;             //persistent
    private int f1;             //transactional
    private int f2;             //none
    private transient int f3;   //transient default behaviour
    private transient int f4;   //transaction
    private transient int f5;   //persistent
    private static int f6;      //static
    private final int f7 = 0;       //final
    private MetaTestA f8;       //persistent

    public static String[] managedFieldNames = {
        "f0", "f1", "f4", "f5", "f8", "f0", "f1", "f4", "f5", "f8"};
    public static int[] managedFieldNos = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};

    public static String[] txFieldsNames = {"f1", "f4", "f1", "f4"};
    public static int[] txFieldsNos = {1, 4, 1, 4};
}
