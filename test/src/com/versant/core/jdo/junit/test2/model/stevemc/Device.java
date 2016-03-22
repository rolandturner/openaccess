
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
package com.versant.core.jdo.junit.test2.model.stevemc;

public class Device extends Span {
    
    private int accessMode;
    private String address;
    private int deviceTypeId;
    private String enaNatAddress;
    private int transferMode;
    
    public int getAccessMode() {
        return accessMode;
    }
    
    public void setAccessMode(int accessMode) {
        this.accessMode = accessMode;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getDeviceTypeId() {
        return deviceTypeId;
    }
    
    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }
    
    public String getEnaNatAddress() {
        return enaNatAddress;
    }
    
    public void setEnaNatAddress(String enaNatAddress) {
        this.enaNatAddress = enaNatAddress;
    }
    
    public int getTransferMode() {
        return transferMode;
    }
    
    public void setTransferMode(int transferMode) {
        this.transferMode = transferMode;
    }    
    
}
