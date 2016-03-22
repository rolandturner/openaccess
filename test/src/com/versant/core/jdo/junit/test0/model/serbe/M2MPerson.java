
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
package com.versant.core.jdo.junit.test0.model.serbe;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class M2MPerson {
    private String name;
    private List books;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass()).append("\nName             : ").append(getName());
        return sb.toString();
    }


    public static String getDefaultName1() {
        return "Person number 1";
    }

    public static String getDefaultName2() {
        return "Person number 2";
    }

    public static String getDefaultName3() {
        return "Person number 3";
    }

    public static String getDefaultName4() {
        return "New person 1";
    }

    public static String getDefaultName5() {
        return "New person 2";
    }

    public static String getDefaultName6() {
        return "Person number 6";
    }

    public static String getDefaultName7() {
        return "Person number 7";
    }


    public void addBook(M2MBook book) {
        if (this.books == null) {
            this.books = new ArrayList();
        }
        this.books.add(book);
    }


    public void removeBook(M2MBook book) {
        if (this.books == null) {
            this.books = new ArrayList();
        }
        this.books.remove(book);
    }


    public List getBooks() {
        return this.books;
    }

    public String getName() {
        return this.name;
    }

    public void setBooks(List list) {
        this.books = list;
    }

    public void setName(String string) {
        this.name = string;
    }
}
