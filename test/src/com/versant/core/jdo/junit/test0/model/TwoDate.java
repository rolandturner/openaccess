
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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class with two date fields.
 *
 * @keep-all
 * @see com.versant.core.jdo.junit.test0.QueryTests#testDateNullAndDateNull
 */
public class TwoDate {

    private int age;
    private Date date1;
    private Date date2;

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    public TwoDate() {
    }

    public TwoDate(int age, Date date1, Date date2) {
        this.age = age;
        this.date1 = date1;
        this.date2 = date2;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    public String toString() {
        return "TwoDate age " + age +
                " date1 " + (date1 == null ? "null" : FMT.format(date1)) +
                " date2 " + (date2 == null ? "null" : FMT.format(date2));
    }

}
