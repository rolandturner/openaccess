
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
package com.versant.core.jdo.junit.test2.model.alex;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * For testing Alex's tree persistence bug.
 */
public class MenuItem implements Comparable {

    private int recordNo;
    private short sequenceNo;
    private String className;
    private String title;
    private String iconName;
    private MenuItem parent;
    private List subMenuList = new ArrayList(); // inverse MenuItem.parent

    public MenuItem(String title) {
        this.title = title;
    }

    public int getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(int recordNo) {
        this.recordNo = recordNo;
    }

    public short getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(short sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public MenuItem getParent() {
        return parent;
    }

    public List getSubMenuList() {
        return subMenuList;
    }

    public String getSubMenuListStr() {
        ArrayList a = new ArrayList(subMenuList);
        Collections.sort(a);
        return a.toString();
    }

    public void addSubMenu(MenuItem i) {
        subMenuList.add(i);
        i.parent = this;
    }

    public int compareTo(Object o) {
        return title.compareTo(((MenuItem)o).title);
    }

    public String toString() {
        return title;
    }
}
