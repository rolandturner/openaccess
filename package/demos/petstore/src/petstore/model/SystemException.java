
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
package petstore.model;

/**
 */
public class SystemException extends RuntimeException {

    private Exception detail;

    public SystemException(Exception e) {
        super(e.getMessage());
        this.detail = e;
    }

    public void printStackTrace(java.io.PrintStream ps) {
        if (detail == null) {
            super.printStackTrace(ps);
        } else {
            synchronized (ps) {
                ps.println(this);
                detail.printStackTrace(ps);
            }
        }
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(java.io.PrintWriter pw) {
        if (detail == null) {
            super.printStackTrace(pw);
        } else {
            synchronized (pw) {
                pw.println(this);
                detail.printStackTrace(pw);
            }
        }
    }


}
