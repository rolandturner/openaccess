
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
 * A value that is the name of a class relative to a package name. The
 * picklist contains all the PC classes in the project.
 *
 * @keep-all
 */
public class MdClassValue extends MdValue {

    protected String packageName;

    public MdClassValue(String text, MdPackage mdPackage) {
        super(text);
        packageName = mdPackage.getName() + ".";
        initPickList(mdPackage);
    }

    protected void initPickList(MdPackage mdPackage) {
        setPickList(mdPackage.getJdoFile().getProject().getAllClassNames());
    }

    public boolean isValid() {
        if (!onlyFromPickList || pickList == null) return true;
        return text == null || pickList.contains(text)
                || pickList.contains(packageName + text);
    }

    public void setText(String text) {
        int l = packageName.length();
        if (text != null && text.startsWith(packageName) && text.lastIndexOf(
                '.') + 1 == l) {
            text = text.substring(l);
        }
        super.setText(text);
    }

}

