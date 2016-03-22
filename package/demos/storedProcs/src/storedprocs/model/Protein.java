
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
package storedprocs.model;

import java.util.Date;

/*
 * Generated by JDO Genie 
 */
public class Protein extends Molecule {
    private String createdBy;
    private String functionaldescription;
    private char isdeleted;
    private String lastModifiedBy;
    private Date lastModifiedDt;

    public Protein() {
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getFunctionaldescription() {
        return functionaldescription;
    }

    public void setFunctionaldescription(String functionaldescription) {
        this.functionaldescription = functionaldescription;
    }

    public char getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(char isdeleted) {
        this.isdeleted = isdeleted;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDt() {
        return lastModifiedDt;
    }

    public void setLastModifiedDt(Date lastModifiedDt) {
        this.lastModifiedDt = lastModifiedDt;
    }
}
