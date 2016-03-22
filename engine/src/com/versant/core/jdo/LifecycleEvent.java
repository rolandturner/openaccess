
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

import com.versant.core.common.Utils;

import java.util.EventObject;

/**
 * For JDO 2 LifeCycle support.
 *
 * @see VersantPersistenceManagerFactory#addLifecycleListener
 * @see VersantPersistenceManagerFactory#removeLifecycleListener
 */
public class LifecycleEvent extends EventObject {

    public static final int CREATE = 0;
    public static final int LOAD = 1;
    public static final int PRESTORE = 2;
    public static final int POSTSTORE = 3;
    public static final int CLEAR = 4;
    public static final int DELETE = 5;
    public static final int DIRTY = 6;
    public static final int DETACH = 7;
    public static final int ATTACH = 8;

    private int type;
    private Object target;

    public LifecycleEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    public LifecycleEvent(Object source, int type, Object target) {
        this(source, type);
        this.target = target;
    }

    /**
     * This method returns the event type that triggered the event.
     */
    public int getEventType() {
        return type;
    }

    /**
     * This method returns the other object associated withthe event.
     * Specifically, the target object is the detached instance in the case
     * of postAttach, and the persistent instance in the case of postDetach.
     */
    public Object getTarget() {
        return target;
    }

    public static String toTypeString(int type) {
        switch (type) {
            case CREATE:    return "CREATE";
            case LOAD:      return "LOAD";
            case PRESTORE:  return "PRESTORE";
            case POSTSTORE: return "POSTSTORE";
            case CLEAR:     return "CLEAR";
            case DELETE:    return "DELETE";
            case DIRTY:     return "DIRTY";
            case DETACH:    return "DETACH";
            case ATTACH:    return "ATTACH";
        }
        return "UNKNOWN(" + type + ")";
    }

    public String toString() {
        return "LifecycleEvent[source=" + Utils.toString(source) +
                ", " + toTypeString(type) + ", target=" +
                Utils.toString(target) + "]";
    }

}

