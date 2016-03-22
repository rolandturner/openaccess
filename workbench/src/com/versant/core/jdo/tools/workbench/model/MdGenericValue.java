
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

import java.util.ArrayList;



/**
 * A value that is the name of a class relative to a package name or a
 * fully qualified class name. This is designed for use with generics
 * where there is only one possible element type picked up with the
 * JDK 1.5 generics API.
 *
 * @keep-all
 */
public class MdGenericValue extends MdValue {

    protected String packageName;

    public MdGenericValue(String text, MdPackage mdPackage, Class genericType) {
        super(text);
        packageName = mdPackage.getName() + ".";
        String s = genericType.getName();
        setDefText(s);
        ArrayList a = new ArrayList();
        a.add(s);
        setPickList(a);
    }

    public boolean isValid() {
        return text == null || pickList.contains(text)
                || pickList.contains(packageName + text);
    }

    public void setText(String text) {
        int l = packageName.length();
        if (text != null && text.startsWith(packageName) && text.lastIndexOf(
                '.') == l) {
            text = text.substring(l);
        }
        super.setText(text);
    }

}

