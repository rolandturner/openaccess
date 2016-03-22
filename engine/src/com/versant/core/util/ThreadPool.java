
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
package com.versant.core.util;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;

/**
 * Simple pool of Threads.
 */
public class ThreadPool {

    private String name;
    private HashSet active = new HashSet();
    private LinkedList idle = new LinkedList();
    private int idleCount;
    private int maxActive = 10;
    private int maxIdle = 3;
    private int lastThreadId;
    private boolean closed;

    public ThreadPool(String name) {
        this.name = name;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public synchronized int getActiveCount() {
        return active.size();
    }

    public synchronized int getIdleCount() {
        return idleCount;
    }

    /**
     * Close the pool, stopping all threads. This does not wait for the
     * threads to actually stop before returning. This is a NOP if the
     * pool has already been closed.
     */
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (Iterator i = idle.iterator(); i.hasNext(); ) {
            Worker w = (Worker)i.next();
            w.terminate();
        }
        idle = null;
        idleCount = 0;
        for (Iterator i = active.iterator(); i.hasNext(); ) {
            Worker w = (Worker)i.next();
            w.terminate();
        }
        active = null;
    }

    /**
     * Executed runnable using a Thread from the pool. This will block for
     * timeoutMs and forever if this is 0. Returns true if the task is
     * being executed (i.e. a Thread was available) or false if not (i.e.
     * pool full).
     */
    public synchronized boolean execute(Runnable runnable, int timeoutMs) {
        if (closed) {
            throw new IllegalStateException("Pool has been closed");
        }
        Worker t;
        if (idleCount == 0) {
            for (; isFull(); ) {
                try {
                    wait(timeoutMs);
                    if (isFull()) {
                        return false;
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            t = new Worker();
        } else {
            t = (Worker)idle.removeFirst();
            --idleCount;
        }
        active.add(t);
        t.execute(runnable);
        return true;
    }

    protected boolean isFull() {
        return active.size() >= maxActive;
    }

    private synchronized void finishedWork(Worker t) {
        if (!closed) {
            active.remove(t);
            if (idleCount >= maxIdle) {
                t.terminate();
            } else {
                idle.addLast(t);
                ++idleCount;
            }
        }
    }

    private class Worker extends Thread {

        private boolean stopFlag;
        private Runnable runnable;

        public Worker() {
            super(name + " " + ++lastThreadId);
            setDaemon(true);
        }

        /**
         * Executed runnable.
         */
        public void execute(Runnable runnable) {
            this.runnable = runnable;
            if (!isAlive()) {
                start();
            } else {
                synchronized (this) {
                    notify();
                }
            }
        }

        /**
         * Stop this thread as soon as possible.
         */
        public void terminate() {
            stopFlag = true;
            interrupt();
        }

        public void run() {
            for (; !stopFlag; ) {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath)e;
                    }
                }
                runnable = null;
                finishedWork(this);
                if (stopFlag) break;
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

    }

}

