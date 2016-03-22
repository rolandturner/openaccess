
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
package petstore.db;

import petstore.db.Account;

import java.io.Serializable;

/**
 */
public class Customer implements Serializable {

    private User user;
    private Account account;

    public Customer() {
        super();
    }

    public Customer(User user) {
        this.user = user;
        this.account = new Account();
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
