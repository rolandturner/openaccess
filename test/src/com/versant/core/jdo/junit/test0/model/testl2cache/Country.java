
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
 * Created on 16-feb-04
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.versant.core.jdo.junit.test0.model.testl2cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;

/**
 * @author uhksa117
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *         TODO Synchronize with CMS
 *         TODO Move to geographics package.
 */
public class Country implements Serializable {

    private String code;
    private String name;
    private Collection regions;
    private String isoCode;

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

    public Collection getRegions() {
        if (regions == null) {
            regions = new ArrayList();
        }
        return regions;
    }

    public void addRegion(CountryRegion region) {
        region.setCountry(this);
        getRegions().add(region);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Country) {
            return code.equals(((Country)obj).getCode());
        }
        return false;
    }

    public int hashCode() {
        return code.hashCode();
    }

    public String getIsoCode() {
        return isoCode;
    }

    /**
     * @param string
     */
    public void setIsoCode(String string) {
        isoCode = string;
    }

}
