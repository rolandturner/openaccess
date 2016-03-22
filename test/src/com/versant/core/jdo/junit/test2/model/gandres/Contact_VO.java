
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
package com.versant.core.jdo.junit.test2.model.gandres;

import java.util.List;

/**
 */
public class Contact_VO {
    private Integer _owner;
    private Integer _contacttype;
    private List activityList;

    public Integer get_owner() {
        return _owner;
    }

    public void set_owner(Integer _owner) {
        this._owner = _owner;
    }

    public Integer get_contacttype() {
        return _contacttype;
    }

    public void set_contacttype(Integer _contacttype) {
        this._contacttype = _contacttype;
    }

    public List getActivityList() {
        return activityList;
    }

    public void setActivityList(List activityList) {
        this.activityList = activityList;
    }




}
