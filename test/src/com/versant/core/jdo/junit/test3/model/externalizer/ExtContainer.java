
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
package com.versant.core.jdo.junit.test3.model.externalizer;

import java.util.*;

/**
 * For testing externalized fields.
 */
public class ExtContainer {

    // serialized by default as SerializableType is in project ext types
    private SerializableType defExtType;
    // externalizer="STRING"
    private SerializableType stringExtType;
    // externalizer="SERIALIZED"
    private Address address;
    // externalizer in .jdo file only and not in project
    private Address2 address2;
    // externalizer="SERIALIZED"
    private Object object;
    // externalizer="SERIALIZED"
    private List list;
    // externalizer="SERIALIZED"
    private Map map;

    public ExtContainer() {
    }

    public SerializableType getDefExtType() {
        return defExtType;
    }

    public void setDefExtType(SerializableType defExtType) {
        this.defExtType = defExtType;
    }

    public SerializableType getStringExtType() {
        return stringExtType;
    }

    public void setStringExtType(SerializableType stringExtType) {
        this.stringExtType = stringExtType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address2 getAddress2() {
        return address2;
    }

    public void setAddress2(Address2 address2) {
        this.address2 = address2;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public String getMapStr() {
        ArrayList a = new ArrayList();
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            a.add(i.next().toString());
        }
        Collections.sort(a);
        return a.toString();
    }
}

