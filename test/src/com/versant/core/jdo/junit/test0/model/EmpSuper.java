
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @keep-all
 */
public class EmpSuper implements Serializable {

    private int a;
    private int b;
    private int c;
    private String name;
    private int n;
    private List superList = new ArrayList();
    private Date date;

    public EmpSuper() {
    }

    public EmpSuper(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getSuperList() {
        return superList;
    }

    public void setSuperList(List superList) {
        this.superList = superList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    protected void writeObject(ObjectOutputStream out)
            throws IOException {
        out.writeUTF(name);

    }

    protected void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        name = in.readUTF();

    }
}
