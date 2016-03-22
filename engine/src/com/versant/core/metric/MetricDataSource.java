
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
 * Provides data for a number of Metric's.
 * @keep-all
 */
public interface MetricDataSource {

    /**
     * Get the value of sampleNo for the given metricIndex. If sampleNo is
     * before the first sample then return the first sample. If sampleNo is
     * after the last sample then return the last sample.
     */
    public int getSample(int sampleNo, int metricIndex);

}

