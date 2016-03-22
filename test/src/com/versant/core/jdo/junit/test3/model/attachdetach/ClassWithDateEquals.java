
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
package com.versant.core.jdo.junit.test3.model.attachdetach;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 08-Sep-2005
 * Time: 15:20:20
 * To change this template use File | Settings | File Templates.
 */
public class ClassWithDateEquals {
    private Date date;

    public ClassWithDateEquals(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClassWithDateEquals that = (ClassWithDateEquals) o;

        if (!date.equals(that.date)) return false;

        return true;
    }

    public int hashCode() {
        return date.hashCode();
    }
}
