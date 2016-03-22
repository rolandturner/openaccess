
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jdo.JDOHelper;

/**
 * @author uhksa208
 */
public class ArrangementType implements Serializable {

    private String name;

    private String code;
    private String dutch;
    private String english;
    private String french;

    private String german;

    private Collection villages;

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ArrangementType)) {
            return false;
        }

        return code == null
                ? ((ArrangementType)other).code == null
                : code.equals(((ArrangementType)other).code);
    }

    /**
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * @return
     */
    public String getDutch() {
        return dutch;
    }

    /**
     * @return
     */
    public String getEnglish() {
        return english;
    }

    /**
     * @return
     */
    public String getFrench() {
        return french;
    }

    /**
     * @return
     */
    public String getGerman() {
        return german;
    }

    public Collection getVillages() {
        if (villages == null) {
            villages = new LinkedList();
        }
        return villages;
    }

    public int hashCode() {
        return code == null ? 0 : code.hashCode();
    }

    /**
     * @param string
     */
    public void setCode(String string) {
        code = string;
    }

    /**
     * @param string
     */
    public void setDutch(String string) {
        dutch = string;
    }

    /**
     * @param string
     */
    public void setEnglish(String string) {
        english = string;
    }

    /**
     * @param string
     */
    public void setFrench(String string) {
        french = string;
    }

    /**
     * @param string
     */
    public void setGerman(String string) {
        german = string;
    }

    /**
     * @param collection
     */
    public void setVillages(Collection collection) {
        villages = collection;
    }

    private WcccArrangement wcccArrangement;

    /**
     * @return
     */
    public WcccArrangement getWcccArrangement() {
        return wcccArrangement;
    }

    /**
     * @param arrangement
     */
    public void setWcccArrangement(WcccArrangement arrangement) {
        wcccArrangement = arrangement;
        if (arrangement != null) {
            wcccArrangement.setArrangementType(this);
        }
    }

}
