
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
package com.versant.core.jdo.tools.workbench.model;

/**
 * This value holds the name of a class. The toString separates the package
 * from the class name and puts the class name first.
 */
public class MdClassNameValue extends MdValue {

    private boolean invalid;

    public MdClassNameValue() {
    }

    public MdClassNameValue(String text) {
        super(text);
    }

    public String toString() {
        if (text == null) {
            return MdUtils.putClassNameFirst(defText);
        } else {
            return MdUtils.putClassNameFirst(text);
        }
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public boolean isValid() {
        if (invalid) return false;
        if (!onlyFromPickList || pickList == null || text == null) return true;
        if (pickList.contains(text)) return true;
        // text may be fully qualified so check without package
        int i = MdUtils.packageEndIndex(text);
        if (i < 0) return false;
        return pickList.contains(text.substring(i + 1));
    }
}

