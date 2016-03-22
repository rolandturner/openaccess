
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

import java.io.Serializable;

/**
 * A performance metric. Flyweight GOF pattern.
 */
public abstract class Metric implements Serializable, Comparable {

    private final String name;
    private final String displayName;
    private final String category;
    private final String descr;
    private final int decimals;

    public static final int CALC_RAW = 1;
    public static final int CALC_AVERAGE = 2;
    public static final int CALC_DELTA = 3;
    public static final int CALC_DELTA_PER_SECOND = 4;

    public Metric(String name, String displayName, String category, String descr,
            int decimals) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
        this.descr = descr;
        this.decimals = decimals;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescr() {
        return descr;
    }

    public int getDecimals() {
        return decimals;
    }

    public String toString() {
        return name;
    }

    /**
     * What calculation method makes the most sense for this Metric.
     */
    public abstract int getDefaultCalc();

    /**
     * Get the value of this metric for the given range of samples in the
     * data set.
     * @param dataSet The raw data
     * @param firstSampleNo The first sample
     * @param lastSampleNo The last sample (inclusive)
     * @param calc The duration of the sample range
     */
    public abstract double get(MetricDataSource dataSet, int firstSampleNo,
            int lastSampleNo, int calc, double seconds);

    /**
     * Sort by name.
     */
    public int compareTo(Object o) {
        return getName().compareTo(((Metric)o).getName());
    }

}
