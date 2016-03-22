
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

import com.versant.core.common.Debug;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/** 
 * Ring buffer for performance metric samples. The data is stored in a set
 * of parallel arrays one for each metric, one for the dates and one for
 * unique sample ID. All the HasMetric sources are polled at regular
 * intervals by a background thread.
 */
public final class MetricSnapshotStore implements Runnable {

    private HasMetrics[] sources;
    private BaseMetric[] baseMetrics;
    private List otherMetrics = new ArrayList();
    private Metric[] all;
    private int capacity;
    private Date[] dates;   // date for each set of samples
    private int[] ids;      // unique ID for each set of samples
    private int[][] buf;
    private int pos;        // where the next samples are stored in buf
    private int count;
    private boolean locked;
    private int lastID;

    private Thread snapshotThread;
    private boolean run;
    private int sampleIntervalMs;
    private long nextSampleTime;

    public MetricSnapshotStore(int capacity, int sampleIntervalMs) {
        this.capacity = capacity;
        this.sampleIntervalMs = sampleIntervalMs;
        dates = new Date[capacity];
        ids = new int[capacity];
    }

    /**
     * Add a new source of metrics.
     */
    public void addSource(HasMetrics source) {
        lock();
        try {
            if (sources == null) {
                sources = new HasMetrics[]{source};
            } else {
                HasMetrics[] a = new HasMetrics[sources.length + 1];
                System.arraycopy(sources, 0, a, 0, sources.length);
                a[sources.length] = source;
                sources = a;
            }
            
            ArrayList list = new ArrayList();
            source.addMetrics(list);

            // extract the base metrics, sort by name, add them + set indexes
            int n = list.size();
            ArrayList base = new ArrayList(n);
            for (int i = 0; i < n; i++) {
                Object o = list.get(i);
                if (o instanceof BaseMetric) {
                    base.add(o);
                }
            }
            Collections.sort(base);
            int firstNewBase;
            int baseSize = base.size();
            if (baseMetrics == null || baseMetrics.length == 0) {
                firstNewBase = 0;
                baseMetrics = new BaseMetric[baseSize];
                base.toArray(baseMetrics);
            } else {
                firstNewBase = baseMetrics.length;
                BaseMetric[] a = new BaseMetric[firstNewBase + baseSize];
                System.arraycopy(baseMetrics, 0, a, 0, firstNewBase);
                for (int i = 0; i < baseSize; i++) {
                    a[firstNewBase + i] = (BaseMetric)base.get(i);
                }
                baseMetrics = a;
            }
            for (int i = firstNewBase; i < baseMetrics.length; i++) {
                baseMetrics[i].setIndex(i);
            }

            // add more arrays for samples to buf
            if (buf == null) {
                buf = new int[baseMetrics.length][];
            } else {
                int[][] a = new int[baseMetrics.length][];
                System.arraycopy(buf, 0, a, 0, firstNewBase);
                buf = a;
            }
            for (int i = firstNewBase; i < buf.length; i++) {
                buf[i] = new int[capacity];
            }

            // extract the non-base metrics and add them
            ArrayList other = new ArrayList();
            for (int i = 0; i < n; i++) {
                Object o = list.get(i);
                if (!(o instanceof BaseMetric)) {
                    other.add(o);
                }
            }
            Collections.sort(other);
            otherMetrics.addAll(other);
            int otherSize = otherMetrics.size();
            all = new Metric[baseMetrics.length + otherSize];
            System.arraycopy(baseMetrics, 0, all, 0, baseMetrics.length);
            for (int i = 0; i < otherSize; i++) {
                all[baseMetrics.length + i] = (Metric)otherMetrics.get(i);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Start our background snapshot thread if not already done.
     */
    public void start(String id) {
        if (snapshotThread == null) {
            run = true;
            snapshotThread = new Thread(this, "VOA Metric Store " + id);
            snapshotThread.setDaemon(true);
            snapshotThread.start();
        }
    }

    /**
     * Begin adding a new set of samples and return the index in buf to
     * modify to update the data. Readers are locked out until endUpdate
     * is invoked. A unique ID is assigned to the set of samples and the
     * current Date is recorded. The caller must use getBuf and the index
     * return by this method to fill in the sample data.
     * @see #endUpdate()
     */
    private int beginUpdate() {
        lock();
        ids[pos] = ++lastID;
        dates[pos] = new Date();
        return pos;
    }

    /**
     * Finish updating samples.
     * @see #beginUpdate()
     */
    private void endUpdate() {
        pos = (pos + 1) % capacity;
        if (count < capacity) count++;
        unlock();
    }

    /**
     * Return only when we have acquired the lock.
     */
    private synchronized void lock() {
        for (; locked; ) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        locked = true;
    }

    /**
     * Release the lock so other threads can get access.
     */
    private synchronized void unlock() {
        if (Debug.DEBUG) {
            if (!locked) {
                throw BindingSupportImpl.getInstance().internal(
                    "unlock() called with locked == false");
            }
        }
        locked = false;
        notify();
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the maximum number of samples to store in the buffer.
     */
    public void setCapacity(int max) {
        lock();
        try {
            MetricSnapshotPacket old = getNewSnapshotsImp(0);
            capacity = max;
            buf = new int[baseMetrics.length][];
            for (int i = 0; i < baseMetrics.length; i++) buf[i] = new int[capacity];
            dates = new Date[capacity];
            ids = new int[capacity];
            if (old != null) {
                int n = old.getSize();
                if (n > max) n = max;
                int first = old.getSize() - n;
                for (int i = 0; i < baseMetrics.length; i++) {
                    System.arraycopy(old.getBuf()[i], first, buf[i], 0, n);
                }
                System.arraycopy(old.getDates(), first, dates, 0, n);
                System.arraycopy(old.getIds(), first, ids, 0, n);
                count = n;
                pos = n % capacity;
            } else {
                count = 0;
                pos = 0;
            }
        } finally {
            unlock();
        }
    }

    /**
     * Get the samples added since the set of samples with unique ID of id.
     * Returns null if none. Use an ID of 0 to get all samples.
     */
    public MetricSnapshotPacket getNewSnapshots(int id) {
        lock();
        try {
            return getNewSnapshotsImp(id);
        } finally {
            unlock();
        }
    }

    private MetricSnapshotPacket getNewSnapshotsImp(int id) {
        if (count < capacity) {
            int first;
            for (first = pos - 1; first >= 0; first--) {
                if (ids[first] == id) break;
            }
            first++;
            int n = pos - first;
            if (n == 0) return null;
            int[][] sbuf = new int[baseMetrics.length][];
            for (int i = 0; i < baseMetrics.length; i++) {
                sbuf[i] = new int[n];
                System.arraycopy(buf[i], first, sbuf[i], 0, n);
            }
            Date[] dbuf = new Date[n];
            System.arraycopy(dates, first, dbuf, 0, n);
            int[] ibuf = new int[n];
            System.arraycopy(ids, first, ibuf, 0, n);
            return new MetricSnapshotPacket(dbuf, ibuf, sbuf);
        } else {
            if (ids[(pos + capacity - 1) % capacity] == id) return null;
            int first = pos;
            int c = capacity;
            for (; c > 0; first = (first + 1) % capacity, c--) {
                if (ids[first] == id) break;
            }
            first = (first + 1) % capacity;
            if (first >= pos) {
                int h1 = capacity - first;
                int h2 = pos;
                int n = h1 + h2;
                int[][] sbuf = new int[baseMetrics.length][];
                for (int i = 0; i < baseMetrics.length; i++) {
                    sbuf[i] = new int[n];
                    System.arraycopy(buf[i], first, sbuf[i], 0, h1);
                    System.arraycopy(buf[i], 0, sbuf[i], h1, h2);
                }
                Date[] dbuf = new Date[n];
                System.arraycopy(dates, first, dbuf, 0, h1);
                System.arraycopy(dates, 0, dbuf, h1, h2);
                int[] ibuf = new int[n];
                System.arraycopy(ids, first, ibuf, 0, h1);
                System.arraycopy(ids, 0, ibuf, h1, h2);
                return new MetricSnapshotPacket(dbuf, ibuf, sbuf);
            } else {
                int n = pos - first;
                int[][] sbuf = new int[baseMetrics.length][];
                for (int i = 0; i < baseMetrics.length; i++) {
                    sbuf[i] = new int[n];
                    System.arraycopy(buf[i], first, sbuf[i], 0, n);
                }
                Date[] dbuf = new Date[n];
                System.arraycopy(dates, first, dbuf, 0, n);
                int[] ibuf = new int[n];
                System.arraycopy(ids, first, ibuf, 0, n);
                return new MetricSnapshotPacket(dbuf, ibuf, sbuf);
            }
        }
    }

    /**
     * Get the most recent performance metric snapshot.
     */
    public MetricSnapshotPacket getMostRecentSnapshot(int lastId) {
        lock();
        try {
            if (count == 0) return null;
            int p = (pos + capacity - 1) % capacity;
            if (ids[p] == lastId) return null;
            int[][] sbuf = new int[baseMetrics.length][];
            for (int i = 0; i < baseMetrics.length; i++) {
                sbuf[i] = new int[]{buf[i][p]};
            }
            return new MetricSnapshotPacket(new Date[]{dates[p]},
                    new int[]{ids[p]}, sbuf);
        } finally {
            unlock();
        }
    }

    public int getSampleIntervalMs() {
        return sampleIntervalMs;
    }

    public void setSampleIntervalMs(int sampleIntervalMs) {
        if (sampleIntervalMs < 100) sampleIntervalMs = 100;
        int diff = sampleIntervalMs - this.sampleIntervalMs;
        if (diff != 0) {
            this.sampleIntervalMs = sampleIntervalMs;
            nextSampleTime += diff;
            snapshotThread.interrupt();
        };
    }

    public void shutdown() {
        if (snapshotThread != null) {
            run = false;
            snapshotThread.interrupt();
            snapshotThread = null;
        }
    }

    public void run() {
        nextSampleTime = System.currentTimeMillis();
        nextSampleTime += sampleIntervalMs - nextSampleTime % sampleIntervalMs;
        for (; run; ) {
            long now = System.currentTimeMillis();
            long diff = nextSampleTime - now;
            if (diff <= 0) {
                if (sources != null) {
                    int pos = beginUpdate();
                    try {
                        for (int i = 0; i < sources.length; i++) {
                            sources[i].sampleMetrics(buf, pos);
                        }
                    } finally {
                        endUpdate();
                    }
                }
                for (;;) {
                    nextSampleTime += sampleIntervalMs;
                    diff = nextSampleTime - now;
                    if (diff > 0) break;
                }
            }
            try {
                Thread.sleep(diff);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public Metric[] getMetrics() {
        lock();
        try {
            return all;
        } finally {
            unlock();
        }
    }

}
