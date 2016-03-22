
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

import java.io.Serializable;

/**
 * For testing deeply nested object graphs.
 *
 * @keep-all
 */
public class DeepGraph implements Serializable {

    private int age;
    private DeepGraph next;

    public DeepGraph(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public DeepGraph getNext() {
        return next;
    }

    public void setNext(DeepGraph next) {
        this.next = next;
    }

}

