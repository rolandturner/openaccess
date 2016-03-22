
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
 * A metric not derived from other metrics.
 */
public class BaseMetric extends Metric {

    private int index = -1;
    private final int defaultCalc;

    public BaseMetric(String name, String displayName, String category,
            String descr, int decimals, int defaultCalc) {
        super(name, displayName, category, descr, decimals);
        this.defaultCalc = defaultCalc;
    }

    public final int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDefaultCalc() {
        return defaultCalc;
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
        // these calculations make negative values postive (for int rollover)
        // by and'ing with 0xFFFFFFFFL
        int a, b;
        switch (calc) {

            case CALC_RAW:
                return dataSet.getSample(lastSampleNo, index) & 0xFFFFFFFFL;

            case CALC_DELTA_PER_SECOND:
                if (seconds > 0.0) {
                    a = dataSet.getSample(firstSampleNo, index);
                    b = dataSet.getSample(lastSampleNo, index);
                    return ((b & 0xFFFFFFFFL) - (a & 0xFFFFFFFFL)) / seconds;
                }

            case CALC_DELTA:
                a = dataSet.getSample(firstSampleNo, index);
                b = dataSet.getSample(lastSampleNo, index);
                return (b & 0xFFFFFFFFL) - (a & 0xFFFFFFFFL);

            case CALC_AVERAGE:
                int n = lastSampleNo - firstSampleNo;
                if (n <= 1) { // fast exit for simple common case
                    return dataSet.getSample(lastSampleNo, index);
                }
                double tot = dataSet.getSample(firstSampleNo + 1, index);
                for (int i = firstSampleNo + 2; i <= lastSampleNo; i++) {
                    tot += dataSet.getSample(i, index);
                }
                return tot / n;
        };
        throw new IllegalArgumentException("Unknown calc: " + calc);
    }

}
