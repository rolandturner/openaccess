
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
package com.versant.core.jdo.query;

import com.versant.core.metadata.FieldMetaData;


import com.versant.lib.bcel.generic.InstructionHandle;


public class BCField extends Field {

    public InstructionHandle ih;


    /**
     * The type of field.
     */
    public Class classType;

    /**
     * This is equivalent to MDStatics typeCode.
     */
    public int bcType = -1;

    public int getBcType() {
        return bcType;
    }

    public void setBcType(int bcType) {
        this.bcType = bcType;
    }

    public boolean isPrimitive() {
        return classType.isPrimitive();
    }

    public FieldMetaData getFMD() {
        return null;
    }
}
