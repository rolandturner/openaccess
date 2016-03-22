
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
 * A value that must be an int.
 * @keep-all
 */
public class MdValueInt extends MdValue {

    public MdValueInt(String text) {
        super(text);
    }

    public boolean isValid() {
        if (text == null) return true;
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

 
