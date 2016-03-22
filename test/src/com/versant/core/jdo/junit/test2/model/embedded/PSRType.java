
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

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 16-Nov-2005
 * Time: 10:57:38
 * To change this template use File | Settings | File Templates.
 */
public class PSRType extends CoreNaming {
    private String psrTypeVal;
    private String psrTypeVal2;
    private PowerSystemResource PowerSystemResource[];
    private List listField;
    private Map mapField;

    public List getListField() {
        return listField;
    }

    public void setListField(List listField) {
        this.listField = listField;
    }

    public Map getMapField() {
        return mapField;
    }

    public void setMapField(Map mapField) {
        this.mapField = mapField;
    }

    public String getPsrTypeVal2() {
        return psrTypeVal2;
    }

    public void setPsrTypeVal2(String psrTypeVal2) {
        this.psrTypeVal2 = psrTypeVal2;
    }

    public String getPsrTypeVal() {
        return psrTypeVal;
    }

    public void setPsrTypeVal(String psrTypeVal) {
        this.psrTypeVal = psrTypeVal;
    }

    public PowerSystemResource[] getPowerSystemResource() {
        return PowerSystemResource;
    }

    public void setPowerSystemResource(
            PowerSystemResource[] powerSystemResource) {
        PowerSystemResource = powerSystemResource;
    }
}
