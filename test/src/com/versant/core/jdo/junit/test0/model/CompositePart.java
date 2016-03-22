
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

import javax.jdo.PersistenceManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @keep-all
 */
public class CompositePart extends DesignObj {

    private Document documentation;  // <-> Document.part

    // <->BaseAssembly.componentsPriv
    private ArrayList usedInPriv = new ArrayList();

    // <->BaseAssembly.componentsShar
    private ArrayList usedInShar = new ArrayList();
    private HashSet parts = new HashSet();   // <-> AtomicPart.partOf
    private AtomicPart rootPart;

    private int value;

    public CompositePart() {
    }

    public void delete(PersistenceManager pm) {
//        pm.deletePersistent(documentation);

        for (Iterator it = parts.iterator(); it.hasNext();) {
            AtomicPart a = (AtomicPart)it.next();
            //First delete all this AtomicParts Connections (to side).
            pm.deletePersistentAll(a.getTo());
        }
        pm.deletePersistentAll(parts);

        // delete references from BaseAssemblys which use this CompositePart in share.
        for (Iterator sIt = usedInShar.iterator(); sIt.hasNext();) {
            BaseAssembly ba = (BaseAssembly)sIt.next();
            ba.removeSharedComponent(this);
        }

        // delete references from BaseAssemblys which use this CompositePart in private.
        for (Iterator pIt = usedInPriv.iterator(); pIt.hasNext();) {
            BaseAssembly ba = (BaseAssembly)pIt.next();
            ba.removePrivateComponent(this);
        }

        // Finally delete this CompositePart.
        pm.deletePersistent(this);
    }

    public Document getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Document documentation) {
        this.documentation = documentation;
        documentation.setPart(this);
    }

    public AtomicPart getRootPart() {
        return rootPart;
    }

    public void setRootPart(AtomicPart rootPart) {
        this.rootPart = rootPart;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ArrayList getUsedInPriv() {
        return usedInPriv;
    }

    public ArrayList getUsedInShar() {
        return usedInShar;
    }

    public HashSet getParts() {
        return parts;
    }

    public void addPart(AtomicPart part) {
        parts.add(part);
        part.setPartOf(this);
    }

}

 
