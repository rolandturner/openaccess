
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
import java.util.List;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * @keep-all
 */
public class Employee extends EmpSuper {

    private List persons = new ArrayList();
    private int n;
    private String empNo;

    public Employee() {
    }

    public Employee(String name, String empNo) {
        super(name);
        this.empNo = empNo;
    }

    public List getPersons() {
        return persons;
    }

    public void setPersons(List persons) {
        this.persons = persons;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    protected void writeObject(ObjectOutputStream out)
            throws IOException {
        super.writeObject(out);
        out.writeUTF(empNo);
    }

    protected void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        super.readObject(in);
        empNo =  in.readUTF();
    }

}
