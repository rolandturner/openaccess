
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
package com.versant.core.jdo.junit.test2.model.drake2k;

import java.util.Vector;
import java.util.Collection;

/**
 */
public class Request {
    private Collection notes = new Vector();

    public void removeNote(Note n){
        notes.remove(n);
    }

    public Collection getNotes() {
        return notes;
    }

    public void setNotes(Collection notes) {
        this.notes = notes;
    }
}