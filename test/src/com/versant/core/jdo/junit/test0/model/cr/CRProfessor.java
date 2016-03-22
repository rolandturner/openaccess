
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
import java.util.ArrayList;

/**
 */
public class CRProfessor extends CREmployee {
//    [Versant.ItemType(typeof(Course))]
    protected List teaches = new ArrayList();

    public CRProfessor() {
    }

    public CRProfessor(String name,
            java.util.Date birthday,
            String passportNo,
            CRAddress address,
            int seniority,
            int salery,
            int department) {
        super(name, birthday, passportNo, address, seniority, salery, department);
    }

    public void addTeaches(CRCourse c) {
        teaches.add(c);
    }

    public String getActivity() {
        return ("ennoying students");
    }

    public List getTeaches() {
        return teaches;
    }

    public void removeTeaches(CRCourse aCourse) {
        teaches.remove(aCourse);

    }

}
