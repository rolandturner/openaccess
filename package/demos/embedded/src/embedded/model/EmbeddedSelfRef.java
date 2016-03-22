
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
package embedded.model;

/**
 */
public class EmbeddedSelfRef {
    private String val;
    private EmbeddedSelfRef next;

    public EmbeddedSelfRef(String val, EmbeddedSelfRef next) {
        this.val = val;
        this.next = next;
    }

    public EmbeddedSelfRef(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public EmbeddedSelfRef getNext() {
        return next;
    }

    public void setNext(EmbeddedSelfRef next) {
        this.next = next;
    }
}
