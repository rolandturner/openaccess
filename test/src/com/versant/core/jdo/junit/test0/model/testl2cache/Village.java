
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
/*
 * Created on 5-mrt-04
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.versant.core.jdo.junit.test0.model.testl2cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;

/**
 * @author uhksa208
 */
public class Village implements Serializable {

    private String code;
    private String name;

    private int companyCode;
    private Collection arrangementTypes = new LinkedList();
    private CountryRegion region;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection getArrangementTypes() {
        return arrangementTypes;
    }

    public void setArrangementTypes(Collection collection) {
        arrangementTypes = collection;
    }

    public CountryRegion getRegion() {
        return region;
    }

    public void setRegion(CountryRegion region) {
        this.region = region;
    }

    public Collection getDeliveryArrangementTypes() {
        Collection deliveryArrangementTypes = new LinkedList();
//		ArrangementType arrangementType;
//		for (Iterator iter = getArrangementTypes().iterator(); iter.hasNext(); ) {
//			arrangementType = (ArrangementType) iter.next();
//			if (arrangementType.getWcccArrangement() != null && arrangementType.getWcccArrangement().getIsdeliveryservice() == 'Y') {
//				deliveryArrangementTypes.add(arrangementType);
//			}
//		}
        return deliveryArrangementTypes;
    }

    /**
     * @param i
     */
    public void setCompanyCode(int i) {
        companyCode = i;
    }

    /**
     * @return
     */
    public int getCompanyCode() {
        return companyCode;
    }

}
