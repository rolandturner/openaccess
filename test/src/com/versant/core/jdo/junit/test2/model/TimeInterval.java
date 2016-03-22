
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

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * For testing Jean Calvellis nasty query bug.
 * @keep-all
 */
public class TimeInterval {

    private int age;
    private Date start;
    private Date end;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyMMdd");

    public TimeInterval(int age, Date start, Date end) {
        this.age = age;
        this.start = start;
        this.end = end;
    }

    public TimeInterval(int age, String start, String end) throws ParseException {
        this(age, parseDate(start), parseDate(end));
    }

    public static Date parseDate(String s) throws ParseException {
        return s == null ? null : DATE_FORMAT.parse(s);
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public int getAge() {
        return age;
    }

    public String toString() {
        return (start == null ? "null" : DATE_FORMAT.format(start)) + "-" +
               (end == null ? "null" : DATE_FORMAT.format(end));
    }

}
