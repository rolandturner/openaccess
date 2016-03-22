
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

import java.util.Date;

public class BookLoan {
    private String bookName;
    private String isbn;
    private long borrowerId;
    private Date dateDue;
    private String status;

    public String getBookName() {
        return bookName;
    }

    public long getBorrowerId() {
        return borrowerId;
    }

    public Date getDateDue() {
        return dateDue;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getStatus() {
        return status;
    }

    public void setBookName(String string) {
        bookName = string;
    }

    public void setBorrowerId(long id) {
        borrowerId = id;
    }

    public void setDateDue(Date date) {
        dateDue = date;
    }

    public void setIsbn(String string) {
        isbn = string;
    }

    public void setStatus(String string) {
        status = string;
    }
}

