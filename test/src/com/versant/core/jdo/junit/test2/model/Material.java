
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
package com.versant.core.jdo.junit.test2.model;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * For reproducing Randy Watsons bugs.
 * @keep-all
 */
public class Material {

    private String materialName;
    private String materialPartNumber;
    private Collection aliases = new ArrayList();  //Collection of Materials
    private MaterialCategory materialCategory;
    private String materialStatus;
    private String description;
    private Material aliasedTo;

    public Material(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialPartNumber() {
        return materialPartNumber;
    }

    public void setMaterialPartNumber(String materialPartNumber) {
        this.materialPartNumber = materialPartNumber;
    }

    public MaterialCategory getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialCategory(MaterialCategory materialCategory) {
        this.materialCategory = materialCategory;
    }

    public String getMaterialStatus() {
        return materialStatus;
    }

    public void setMaterialStatus(String materialStatus) {
        this.materialStatus = materialStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Material getAliasedTo() {
        return aliasedTo;
    }

    public void setAliasedTo(Material aliasedTo) {
        this.aliasedTo = aliasedTo;
    }

    public void addAlias(Material materialAlias) {
        this.aliases.add(materialAlias);
    }

    public Collection getAliases() {
        return aliases;
    }

    public String toString() {
        if (aliases == null || aliases.isEmpty()) return materialName;
        else return materialName + aliases;
    }

    /**
     * Delete all of our aliases then ourselves.
     */
    public void nuke() {
        if (aliases != null) {
            for (Iterator i = aliases.iterator(); i.hasNext(); ) {
                ((Material)i.next()).nuke();
            }
        }
        PersistenceManager pm = JDOHelper.getPersistenceManager(this);
        if (pm != null) pm.deletePersistent(this);
    }

}
