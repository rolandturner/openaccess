
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

import com.versant.core.common.Debug;
import com.versant.core.metric.BaseMetric;
import com.versant.core.metric.Metric;
import com.versant.core.metric.HasMetrics;
import com.versant.core.logging.LogEventStore;

import java.util.List;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metric.HasMetrics;
import com.versant.core.logging.LogEventStore;

/**
 * This is a pool for PMs. This pool does not have a max limit. It is
 * thread safe.
 */
public class PMPool implements HasMetrics {

    private final VersantPMFInternal pmf;
    private int maxIdle;

    private VersantPersistenceManagerImp idleHead;  // next con to be given out
    private VersantPersistenceManagerImp idleTail;  // last con returned
    private int idleCount;
    private int allocatedCount;
    private int returnedCount;

    private LogEventStore pes;

    private BaseMetric metricLocalPMIdle;
    private BaseMetric metricLocalPMAllocated;
    private BaseMetric metricLocalPMReturned;

    public PMPool(VersantPMFInternal pmf, int maxIdle, LogEventStore pes) {
        this.pmf = pmf;
        this.maxIdle = maxIdle;
        this.pes = pes;
    }

    /**
     * Close all connections and shutdown the pool.
     */
    public void shutdown() {
        for (; ;) {
            VersantPersistenceManagerImp pm = removeFromIdleHead();
            if (pm == null) break;
            try {
                pm.destroy();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public String getStatus() {
        return "idle " + idleCount + "/" + maxIdle;
    }

    /**
     * Allocate a PM from the pool.
     */
    public synchronized VersantPersistenceManagerImp getPM() {
        VersantPersistenceManagerImp pm;
        for (; ;) {
            pm = removeFromIdleHead();
            if (pm == null) {
                pm = pmf.createVersantPersistenceManagerImp();
                break;
            }
            if (!pm.isActualClosed()) {
                break;
            }
        }
        pm.setInPool(false);
        ++allocatedCount;
        if (pes != null && pes.isFine()) {
            pes.log(new PmPoolEvent(PmPoolEvent.PM_ALLOC, idleCount, maxIdle));
        }
        return pm;
    }

    /**
     * Return a PM to the pool. This is called when the PM is closed.
     *
     * @see VersantPersistenceManagerImp#close
     */
    public void returnPM(VersantPersistenceManagerImp pm) {
        if (!pm.isActualClosed()) {
            if (idleCount >= maxIdle) {
                pm.destroy();
            } else {
                pm.setInPool(true);
                pm.resetForPooling();
                addToIdleTail(pm);
            }
            ++returnedCount;
            if (pes != null && pes.isFine()) {
                pes.log(new PmPoolEvent(PmPoolEvent.PM_RELEASE,
                        idleCount, maxIdle));
            }
        }
    }

    /**
     * Add pm to the tail of the idle list. The con has its idle flag set.
     */
    private synchronized void addToIdleTail(VersantPersistenceManagerImp pm) {
        if (Debug.DEBUG) {
            if (pm.prev != null || pm.next != null) {
                throw BindingSupportImpl.getInstance().internal("pm belongs to a list");
            }
        }
        pm.idle = true;
        if (idleTail == null) {
            idleHead = idleTail = pm;
        } else {
            pm.next = idleTail;
            idleTail.prev = pm;
            idleTail = pm;
        }
        ++idleCount;

        if (Debug.DEBUG) checkList(idleTail, idleHead, idleCount);
    }

    /**
     * Check the integrity of the double linked with head and tail.
     */
    private void checkList(VersantPersistenceManagerImp tail,
            VersantPersistenceManagerImp head, int size) {
        if (tail == null) {
            testTrue(head == null);
            return;
        }
        if (head == null) {
            testTrue(tail == null);
            return;
        }
        checkList(tail, size);
        checkList(head, size);
        testTrue(tail.prev == null);
        testTrue(head.next == null);
    }

    /**
     * Check the integrity of the double linked list containing pm.
     */
    private void checkList(VersantPersistenceManagerImp pm, int size) {
        if (pm == null) return;
        int c = -1;
        // check links to tail
        for (VersantPersistenceManagerImp i = pm; i != null; i = i.prev) {
            if (i.prev != null) testTrue(i.prev.next == i);
            ++c;
        }
        // check links to head
        for (VersantPersistenceManagerImp i = pm; i != null; i = i.next) {
            if (i.next != null) testTrue(i.next.prev == i);
            ++c;
        }
        if (size >= 0) {
            testEquals(size, c);
        }
    }

    private static void testEquals(int a, int b) {
        if (a != b) {
            throw BindingSupportImpl.getInstance().internal(
                    "assertion failed: expected " + a + " got " + b);
        }
    }

    private static void testTrue(boolean t) {
        if (!t) {
            throw BindingSupportImpl.getInstance().internal(
                    "assertion failed: expected true");
        }
    }

    /**
     * Return the pm at the head of the idle list removing it from the list.
     * If there is no idle pm then null is returned. The pm has its idle
     * flag cleared.
     */
    private synchronized VersantPersistenceManagerImp removeFromIdleHead() {
        VersantPersistenceManagerImp pm = idleHead;
        if (pm == null) return null;
        idleHead = pm.prev;
        pm.prev = null;
        if (idleHead == null) {
            idleTail = null;
        } else {
            idleHead.next = null;
        }
        --idleCount;
        pm.idle = false;
        if (Debug.DEBUG) checkList(idleTail, idleHead, idleCount);
        return pm;
    }

    /**
     * Add our base performance metrics to list. This is only used for the
     * local PM pool.
     */
    public void addMetrics(List list) {
        String cat = "PM";
        list.add(metricLocalPMIdle =
                new BaseMetric("PMIdle", "PM Idle", cat,
                        "Number of idle PMs in pool", 0,
                        Metric.CALC_AVERAGE));
        list.add(
                metricLocalPMAllocated =
                new BaseMetric("PMAlloc", "PM Allocated", cat,
                        "Number of PMs given out by the pool", 3,
                        Metric.CALC_DELTA_PER_SECOND));
        list.add(
                metricLocalPMReturned =
                new BaseMetric("PMReturned", "PM Returned", cat,
                        "Number of PMs returned to the pool", 3,
                        Metric.CALC_DELTA_PER_SECOND));
    }

    /**
     * Get values for our metrics. This is only called for the local PM pool.
     */
    public void sampleMetrics(int[][] buf, int pos) {
        buf[metricLocalPMIdle.getIndex()][pos] = idleCount;
        buf[metricLocalPMAllocated.getIndex()][pos] = allocatedCount;
        buf[metricLocalPMReturned.getIndex()][pos] = returnedCount;
    }

}
