
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
package com.versant.core.jdo.junit.test2.model;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 21-Nov-2005
 * Time: 10:49:31
 * To change this template use File | Settings | File Templates.
 */
public class GoldStudent extends Student {
    private String goldStudentVal;
    
    public GoldStudent(String name, Country country) {
        super(name, country);
    }

    public String getGoldStudentVal() {
        return goldStudentVal;
    }

    public void setGoldStudentVal(String goldStudentVal) {
        this.goldStudentVal = goldStudentVal;
    }
}
