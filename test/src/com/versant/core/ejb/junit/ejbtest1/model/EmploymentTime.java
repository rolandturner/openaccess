
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
package com.versant.core.ejb.junit.ejbtest1.model;

import javax.persistence.Embeddable;
import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;

@Embeddable
@Table(name = "QUERY_EMPLOYEE")
public class EmploymentTime implements Serializable {
    private Date startDate;
    private Date endDate;

    public EmploymentTime() {
    }

    public EmploymentTime(Date theStartDate, Date theEndDate) {
        startDate = theStartDate;
        endDate = theEndDate;
    }

    @Column(name = "S_DATE")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date date) {
        this.startDate = date;
    }

    @Column(name = "E_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date date) {
        this.endDate = date;
    }

    /**
     * Print the start & end date
     */
    public String toString() {
        java.io.StringWriter writer = new java.io.StringWriter();

        writer.write("EmploymentTime: ");
        if (this.getStartDate() != null) {
            writer.write(this.getStartDate().toString());
        }
        writer.write("-");
        if (this.getEndDate() != null) {
            writer.write(this.getEndDate().toString());
        }
        return writer.toString();
    }
}

