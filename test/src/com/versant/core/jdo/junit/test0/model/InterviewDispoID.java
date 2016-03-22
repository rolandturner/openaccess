
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
package com.versant.core.jdo.junit.test0.model;

import java.io.Serializable;

/**
 * @keep-all
 */
public class InterviewDispoID implements Serializable {

    public long ivDispoID;
    String _strValue;

    public InterviewDispoID() {
        _strValue = null;
    }

    public InterviewDispoID(String str) {
        _strValue = null;
        _strValue = str;
    }

    public boolean equals(Object obj) {
        try {
            InterviewDispoID oid = (InterviewDispoID)obj;
            return ivDispoID == oid.ivDispoID;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        StringBuffer str = new StringBuffer("de.jquest.db.jdo.InterviewDispo");
        str.append(ivDispoID);
        str.append("|");
        int hashCode = 0;
        int off = 0;
        for (int i = 0; i < str.length(); i++) {
            hashCode = 31 * hashCode + str.charAt(off++);
        }

        return hashCode;
    }

    public String toString() {
        if (_strValue != null) {
            return _strValue;
        } else {
            StringBuffer str = new StringBuffer("");
            str.append(ivDispoID);
            str.append("|");
            return str.toString();
        }
    }

}

