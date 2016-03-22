
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

/**
 * @keep-all
 */
public class InterviewDispo {

    private long ivDispoID;
    private int state;

    public InterviewDispo() {
    }

    public long getIvDispoID() {
        return ivDispoID;
    }

    public int getSate() {
        return state;
    }

    public void setState(int i_State) {
        state = i_State;
    }

}

