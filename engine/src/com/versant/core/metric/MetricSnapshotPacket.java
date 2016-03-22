
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

import com.versant.core.jdo.VersantPersistenceManagerFactory;

import java.util.Date;
import java.io.Serializable;
import java.io.PrintStream;

/**
 * A number of metric snapshots stored in parallel arrays (one array per metric).
 * 
 * @see VersantPersistenceManagerFactory#getMetrics()
 * @see com.versant.core.jdo.VersantPersistenceManagerFactory#getNewMetricSnapshots(int)
 */
public final class MetricSnapshotPacket
        implements Serializable, MetricDataSource {

    private final Date[] dates;   // date for each snapshot
    private final int[] ids;      // unique ID for each snapshot
    private final int[][] buf;    // the snapshot data in parallel arrays

    public MetricSnapshotPacket(Date[] dates, int[] ids, int[][] buf) {
        this.dates = dates;
        this.ids = ids;
        this.buf = buf;
    }

    /**
     * Get the number of snapshots.
     */
    public int getSize() {
        return ids.length;
    }

    /**
     * Get the Date of each snapshot.
     */
    public Date[] getDates() {
        return dates;
    }

    /**
     * Get the unique ID of each snapshot.
     */
    public int[] getIds() {
        return ids;
    }

    /**
     * Get the snapshot data. There is one int[] per configured server metric
     * holding the data for all the snapshots in the packet.
     * @see com.versant.core.jdo.VersantPersistenceManagerFactory#getMetrics()
     */
    public int[][] getBuf() {
        return buf;
    }

    /**
     * Get the ID of the most recent sample.
     */
    public int getMostRecentID() {
        return ids[ids.length - 1];
    }

    /**
     * Get the Date of the most recent sample.
     */
    public Date getMostRecentDate() {
        return dates[dates.length - 1];
    }

    /**
     * Get the value of sampleNo for the given metricIndex. If sampleNo is
     * before the first sample then return the first sample. If sampleNo is
     * after the last sample then return the last sample.
     */
    public int getSample(int sampleNo, int metricIndex) {
        if (sampleNo < 0) sampleNo = 0;
        else if (sampleNo >= ids.length) sampleNo = ids.length - 1;
        return buf[metricIndex][sampleNo];
    }

    /**
     * Get the time in seconds between the two samples. The sampleNo's are
     * adjusted to fit in range.
     */
    public double getSeconds(int firstSampleNo, int lastSampleNo) {
        if (firstSampleNo < 0) firstSampleNo = 0;
        if (lastSampleNo >= ids.length) lastSampleNo = ids.length - 1;
        if (lastSampleNo > firstSampleNo) {
            long diff = dates[lastSampleNo].getTime()
                    - dates[firstSampleNo].getTime();
            return diff / 1000.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Get the most recent sample in this packet for the metric using the
     * supplied calculation method. If calc < 0 then the default calculation
     * method for the metric is used.
     * @see Metric#CALC_AVERAGE
     * @see Metric#CALC_DELTA
     * @see Metric#CALC_DELTA_PER_SECOND
     * @see Metric#CALC_RAW
     */
    public double getMostRecentValue(Metric m, int calc) {
        int lastSampleNo = ids.length - 1;
        int firstSampleNo = lastSampleNo - 1;
        return m.get(this, firstSampleNo, lastSampleNo,
                calc < 0 ? m.getDefaultCalc() : calc,
                getSeconds(firstSampleNo, lastSampleNo));
    }

    /**
     * Get the most recent sample in this packet for the metric using its
     * default calculation method.
     * @see #getMostRecentValue(Metric, int)
     */
    public double getMostRecentValue(Metric m) {
        return getMostRecentValue(m, -1);
    }

    /**
     * Get the most recent raw int sample in this packet for the metric.
     * @see #getMostRecentValue(Metric, int)
     */
    public int getMostRecentSample(BaseMetric m) {
        return getSample(ids.length - 1, m.getIndex());
    }

    /**
     * Dump raw sample data to out (for debugging).
     */
    public void dump(Metric[] all, int sampleNo, PrintStream out) {
        for (int i = 0; i < all.length; i++) {
            if (all[i] instanceof BaseMetric) {
                BaseMetric m = (BaseMetric)all[i];
                int mi = m.getIndex();
                out.println(m + " buf[" + mi + "][" + sampleNo + "] = " + buf[mi][sampleNo]);
            }
        }
    }

}
