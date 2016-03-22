
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
public class GcsAdmincommands {
    private int admincommandId;
    private String commanddescription;
    private String commandname;
    private String commandpackage;
    private String commandproc;
    private String commandtype;
    private String createdBy;
    private String isdeleted;
    private String lastModifiedBy;
    private Date lastModifiedDt;

    public GcsAdmincommands() {
    }

    public int getAdmincommandId() {
        return admincommandId;
    }

    public void setAdmincommandId(int admincommandId) {
        this.admincommandId = admincommandId;
    }

    public String getCommanddescription() {
        return commanddescription;
    }

    public void setCommanddescription(String commanddescription) {
        this.commanddescription = commanddescription;
    }

    public String getCommandname() {
        return commandname;
    }

    public void setCommandname(String commandname) {
        this.commandname = commandname;
    }

    public String getCommandpackage() {
        return commandpackage;
    }

    public void setCommandpackage(String commandpackage) {
        this.commandpackage = commandpackage;
    }

    public String getCommandproc() {
        return commandproc;
    }

    public void setCommandproc(String commandproc) {
        this.commandproc = commandproc;
    }

    public String getCommandtype() {
        return commandtype;
    }

    public void setCommandtype(String commandtype) {
        this.commandtype = commandtype;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(String isdeleted) {
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

    /**
     * Application identity objectid-class.
     */
    public static class ID implements java.io.Serializable {
        public int admincommandId;

        public ID() {
        }

        public ID(String s) {
            admincommandId = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GcsAdmincommands.ID)) return false;

            final GcsAdmincommands.ID id = (GcsAdmincommands.ID) o;

            if (this.admincommandId != id.admincommandId) return false;
            return true;
        }

        public int hashCode() {
            int result = 0;
            result = 29 * result + (int) admincommandId;
            return result;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(admincommandId);
            return buffer.toString();
        }
    }
}
