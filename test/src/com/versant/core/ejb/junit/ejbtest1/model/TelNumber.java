
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

import javax.persistence.*;
import java.io.Serializable;
import java.io.StringWriter;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(name = "QUERY_TEL_NUMBER")
public class TelNumber implements Serializable {
    private String number;
    private String type;
    private QueryEmployee owner;
    private Integer id;
    private String areaCode;

    public TelNumber() {
        this("", "###", "#######");
    }

    public TelNumber(String type, String theAreaCode, String theNumber) {
        this.type = type;
        this.areaCode = theAreaCode;
        this.number = theNumber;
        this.owner = null;
    }

    @Id(generate = TABLE, generator = "PHONE_TABLE_GENERATOR")
    @TableGenerator(name = "PHONE_TABLE_GENERATOR", table = @Table(name = "QUERY_EMPLOYEE_GENERATOR_TABLE"), pkColumnValue = "PHONE_SEQ")
    @Column(name = "ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "NUMB")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "AREA_CODE")
    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "EMP_ID")
    public QueryEmployee getOwner() {
        return owner;
    }

    public void setOwner(QueryEmployee owner) {
        this.owner = owner;
    }

    /**
     * Example: Phone[Work]: (613) 225-8812
     */
    public String toString() {
        StringWriter writer = new StringWriter();

        writer.write("TelNumber[");
        writer.write(getType());
        writer.write("]: (");
        writer.write(getAreaCode());
        writer.write(") ");

        int numberLength = this.getNumber().length();
        writer.write(getNumber().substring(0, Math.min(3, numberLength)));
        if (numberLength > 3) {
            writer.write("-");
            writer.write(getNumber().substring(3, Math.min(7, numberLength)));
        }

        return writer.toString();
    }
}



