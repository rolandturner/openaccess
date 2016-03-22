
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
 * A value that is the name of a class relative to a package name or the name
 * of a type allowed in a collection or map. The picklist contains all the
 * PC classes in the project and all the other allowed types (String, Integer
 * and so on).
 *
 * @keep-all
 */
public class MdElementValue extends MdClassValue {

    private MdProject project;

    public MdElementValue(String text, MdPackage mdPackage) {
        super(text, mdPackage);
        project = mdPackage.getJdoFile().getProject();
    }

    protected void initPickList(MdPackage mdPackage) {
        setPickList(mdPackage.getJdoFile().getProject().getAllElementNames());
    }

    public boolean isValid() {
        boolean valid = super.isValid();
        if (!valid && project.isVds()) {
            String text = this.text;
            if (MdUtils.isStringNotEmpty(packageName) && text != null && text.indexOf(
                    '.') < 0) {
                text = packageName + text;
            }
            Class clazz = project.loadClass(text);
            return clazz != null && clazz.isInterface();
        }
        return valid;
    }
}

