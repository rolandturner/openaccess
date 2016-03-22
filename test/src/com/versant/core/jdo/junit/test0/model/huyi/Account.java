
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
package com.versant.core.jdo.junit.test0.model.huyi;

import java.util.ArrayList;
import java.io.Serializable;

/**
 */
public class Account implements Serializable {
    final static long serialVersionUID = 20040910l;
    private String id;
    private String password;
    private ArrayList roles = new ArrayList();

    public Account() {
        super();
    }

    public static Account newInstance(String id, String password){
        if(id == null) throw new NullPointerException("id can't be null");
        Account account = new Account();
        account.id = id;
        account.setPassword(password);
        return account;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public void addRole(Role role){
        if(role == null) throw new NullPointerException("role can't be null");
        roles.add(role);
    }

    public void removeRole(Role role){
        if(role == null) throw new NullPointerException("role can't be null");
        roles.remove(role);
    }
}
