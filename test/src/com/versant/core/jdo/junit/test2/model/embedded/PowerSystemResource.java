
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
package com.versant.core.jdo.junit.test2.model.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 16-Nov-2005
 * Time: 10:57:02
 * To change this template use File | Settings | File Templates.
 */
public class PowerSystemResource extends CoreNaming {
    private String valPowerSystemResource;
    private PSRType PSRType;

    public PSRType getPSRType() {
        return PSRType;
    }

    public void setPSRType(PSRType PSRType) {
        this.PSRType = PSRType;
    }

    public String getValPowerSystemResource() {
        return valPowerSystemResource;
    }

    public void setValPowerSystemResource(String valPowerSystemResource) {
        this.valPowerSystemResource = valPowerSystemResource;
    }
}
