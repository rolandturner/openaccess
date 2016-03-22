
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
 * A performance metric that is the fraction a / (a + b) expressed
 * as a percentage.
 * @keep-all
 */
public class PercentageSumMetric extends DerivedMetric {

    private final Metric a;
    private final Metric b;

    public PercentageSumMetric(String name, String displayName, String category,
            String descr, Metric[] args) {
        this(name, displayName, category, descr, args[0], args[1]);
    }

    public PercentageSumMetric(String name, String displayName, String category,
            String descr, Metric a, Metric b) {
        super(name, displayName, category, descr, 1);
        this.a = a;
        this.b = b;
    }

    /**
     * How many arguments does this metric accept? Return 0 for any number
     * of arguments.
     */
    public int getArgCount() {
        return 2;
    }

    /**
     * Get the value of this metric for the given range of samples in the
     * data set.
     * @param dataSet The raw data
     * @param firstSampleNo The first sample
     * @param lastSampleNo The last sample (inclusive)
     * @param calc The duration of the sample range in seconds
     */
    public double get(MetricDataSource dataSet, int firstSampleNo, int lastSampleNo,
            int calc, double seconds) {
        double a = this.a.get(dataSet, firstSampleNo, lastSampleNo, calc, seconds);
        double b = this.b.get(dataSet, firstSampleNo, lastSampleNo, calc, seconds);
        double tot = a + b;
        if (tot == 0.0) return 0.0;
        return a * 100.0 / tot;
    }

    /**
     * What calculation method makes the most sense for this Metric.
     */
    public int getDefaultCalc() {
        return CALC_AVERAGE;
    }

}

