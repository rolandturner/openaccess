
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

import java.util.List;

/**
 * Classes that keep performance metrics implement this.
 */
public interface HasMetrics {

    /**
     * Add all Metric's to the List.
     */
    public void addMetrics(List list);

    /**
     * Get values for our metrics.
     */
    public void sampleMetrics(int[][] buf, int pos);

}

