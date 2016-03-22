
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FetchGroupSelfRef {

    private int no;
    private FetchGroupSelfRef selfRef;
    private List selfRefList = new ArrayList(3);

    public FetchGroupSelfRef() {
    }

    public FetchGroupSelfRef(int no) {
        this.no = no;
    }

    public FetchGroupSelfRef(int no, FetchGroupSelfRef selfRef) {
        this.no = no;
        this.selfRef = selfRef;
        selfRefList.add(selfRef);
    }

    public FetchGroupSelfRef getSelfRef() {
        return selfRef;
    }

    public void setSelfRef(FetchGroupSelfRef selfRef) {
        this.selfRef = selfRef;
        selfRefList.add(selfRef);
    }

    public void addSelfRef(FetchGroupSelfRef selfRef) {
        selfRefList.add(selfRef);
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public List getSelfRefList() {
        return selfRefList;
    }

    public void setSelfRefList(List selfRefList) {
        this.selfRefList = selfRefList;
    }

    public String toString() {
        return "FetchGroupSelfRef{" +
                "no=" + no +
                ", selfRef=" + selfRef +
                ", selfRefList.size()=" + (selfRefList == null ? "null" : String.valueOf(
                        selfRefList.size())) +
                "}";
    }
}
