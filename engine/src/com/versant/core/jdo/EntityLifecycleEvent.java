
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

import java.lang.reflect.Method;

/**
 * This class is just a placeholder for the information to be used later, to
 * build the EntityLifecycleManager. 
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class EntityLifecycleEvent implements Comparable {
    private Object listener;
    private Method method;
    private int event;
    private Class forClass;
    private int weight;

    public EntityLifecycleEvent(int event, Class forClass, Object listener, Method method) {
        this.event = event;
        this.forClass = forClass;
        this.listener = listener;
        this.method = method;
    }

    public int getEvent() {
        return event;
    }

    public Class getForClass() {
        return forClass;
    }

    public Object getListener() {
        return listener;
    }

    public Method getMethod() {
        return method;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return this.weight;
    }

    public int compareTo(Object o) {
        EntityLifecycleEvent otherEvent = (EntityLifecycleEvent) o;
        return this.weight - otherEvent.getWeight();
    }

    public String toString() {
        return "listener = "+ listener+ "\n" +
                "method = " + method.getName() + "\n" +
                "event = " + event + "\n" +
                "forClass = " + forClass.getName() + "\n" +
                "weight = " + weight ;
    }
}

