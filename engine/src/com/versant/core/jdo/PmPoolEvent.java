
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
package com.versant.core.jdo;

/**
 * Event logged for PM pool operations.
 */
public class PmPoolEvent extends ServerLogEvent {

    private int maxIdle;
    private int idle;

    public PmPoolEvent(int type, int idle, int maxIdle) {
        super(type, null);
        this.maxIdle = maxIdle;
        this.idle = idle;
        totalMs = 0;
    }

    public String getDescription() {
        if (description == null) {
            description = "idle " + idle + "/" + maxIdle;
        }
        return description;
    }

}
