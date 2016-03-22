
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
package com.versant.core.metric;

/**
 * A performance metric derived from the value(s) of other metrics.
 * @keep-all
 */
public abstract class DerivedMetric extends Metric {

    public DerivedMetric(String name, String displayName, String category,
            String descr, int decimals) {
        super(name, displayName, category, descr, decimals);
    }

    /**
     * How many arguments does this metric accept? Return 0 for any number
     * of arguments.
     */
    public abstract int getArgCount();

}

