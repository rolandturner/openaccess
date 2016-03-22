
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
package com.versant.core.ejb.junit.ejbtest0.model;

import javax.persistence.*;

@Entity(access=AccessType.FIELD)
@TableGenerator(
        name="GEN1", 
        table=@Table(name="GEN1"), 
        pkColumnName="KEY_NAME",
        valueColumnName="LAST_NUMBER",
        pkColumnValue="TABLEGEN",
        initialValue=1,
        allocationSize=100)
public class TableGen {

    @Id(generate=GeneratorType.TABLE, generator="GEN1")
    private int id;
    
    private String name;

    public TableGen() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString() {
        return "TableGen@" + 
            Integer.toHexString(System.identityHashCode(this)) +
            " id=" + id + " name='" + name + "'";
    }
    
}
