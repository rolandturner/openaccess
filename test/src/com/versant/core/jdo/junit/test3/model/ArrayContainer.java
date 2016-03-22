
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
package com.versant.core.jdo.junit.test3.model;

import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.io.File;

/**
 * For testing arrays[] of non-primitive different types.
 */
public class ArrayContainer {

    private String[] strings;
    private Integer[] ints;
    private Locale[] locales;
    private BigDecimal[] bigdecs;
    private Date[] dates;
    private File[] files;
    private int[] pints;

    public ArrayContainer() {
    }

    public int[] getPints() {
        return pints;
    }

    public void setPints(int[] pints) {
        this.pints = pints;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public String getStringsStr() {
        return Arrays.asList(strings).toString();
    }

    public Integer[] getInts() {
        return ints;
    }

    public void setInts(Integer[] ints) {
        this.ints = ints;
    }

    public String getIntsStr() {
        return Arrays.asList(ints).toString();
    }

    public Locale[] getLocales() {
        return locales;
    }

    public void setLocales(Locale[] locales) {
        this.locales = locales;
    }

    public String getLocalesStr() {
        return Arrays.asList(locales).toString();
    }

    public BigDecimal[] getBigdecs() {
        return bigdecs;
    }

    public void setBigdecs(BigDecimal[] bigdecs) {
        this.bigdecs = bigdecs;
    }

    public String getBigdecsStr() {
        return Arrays.asList(bigdecs).toString();
    }

    public Date[] getDates() {
        return dates;
    }

    public void setDates(Date[] dates) {
        this.dates = dates;
    }

    public String getDatesStr() {
        ArrayList a = new ArrayList();
        SimpleDateFormat f = new SimpleDateFormat("ddMMyy");
        for (int i = 0; i < dates.length; i++) {
            a.add(f.format(dates[i]));
        }
        return a.toString();
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public String getFilesStr() {
        return Arrays.asList(files).toString();        
    }

}

