
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
package com.versant.core.jdo.junit.test0.model.cr;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

/**
 */
public class CRPerson {
    protected String name = "";
    protected Date birthday = new Date();
    protected String passport_number = "";
    protected CRPerson spouse_wI = null;
    protected CRPerson spouse_woI = null;

    protected List children = new ArrayList();
    protected CRAddress address = null;

    public CRPerson() {
    }

    public CRPerson(String name, Date birthday, String passportNo,
            CRAddress address) {
        this.name = name;
        this.birthday = birthday;
        this.passport_number = passportNo;
        this.address = address;
    }

    public void addChild(CRPerson p) {
        children.add(p);
    }

    public String getActivity() {
        return ("playing around");
    }

    public CRAddress getAddress() {
        return address;
    }

    public Date getBirthday() {
        return birthday;
    }

    public List getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public String getPassport_number() {
        return passport_number;
    }

    public CRPerson getSpouse_wI() {
        return spouse_wI;
    }

    public CRPerson getSpouse_woI() {
        return spouse_woI;
    }

    public void removeChild(CRPerson aPerson) {
    }

    public void setAddress(CRAddress a) {
        address = a;
    }

    public void setBirthday(Date newValue) {
        this.birthday = newValue;
    }

    public void setName(String s) {
        name = s;
    }

    public void setPassport_number(String newValue) {
        this.passport_number = newValue;
    }

    public void setSpouse(CRPerson s) {
        spouse_wI = s;
        spouse_woI = s;
    }
}
