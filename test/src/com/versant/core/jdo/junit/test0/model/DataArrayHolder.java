
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

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 02-Aug-2005
 * Time: 11:32:06
 * To change this template use File | Settings | File Templates.
 */
public class DataArrayHolder {
    private Date[] dateArray;

    public Date[] getDateArray() {
        return dateArray;
    }

    public void setDateArray(Date[] dateArray) {
        this.dateArray = dateArray;
    }

}
