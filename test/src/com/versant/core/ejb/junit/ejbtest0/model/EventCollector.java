
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
package com.versant.core.ejb.junit.ejbtest0.model;

import javax.jdo.spi.PersistenceCapable;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;

/**
 * This class collects all the lifecycle events
 */
public class EventCollector {

    public static final int PRE_PERSIST_EVENT = 1;
    public static final int POST_PERSIST_EVENT = 2;
    public static final int PRE_REMOVE_EVENT = 3;
    public static final int POST_REMOVE_EVENT = 4;
    public static final int PRE_UPDATE_EVENT = 5;
    public static final int POST_UPDATE_EVENT = 6;
    public static final int POST_LOAD_EVENT = 7;

    List registeredEvents = new ArrayList();

    public void prePersist(Object src, Class fromClass) {
        addLifecycleEvent(PRE_PERSIST_EVENT, src, fromClass);
    }

    public void postPersist(Object src, Class fromClass) {
        addLifecycleEvent(POST_PERSIST_EVENT, src, fromClass);
    }

    public void preRemove(Object src, Class fromClass) {
        addLifecycleEvent(PRE_REMOVE_EVENT, src, fromClass);
    }

    public void postRemove(Object src, Class fromClass) {
        addLifecycleEvent(POST_REMOVE_EVENT, src, fromClass);
    }

    public void preUpdate(Object src, Class fromClass) {
        addLifecycleEvent(PRE_UPDATE_EVENT, src, fromClass);
    }

    public void postUpdate(Object src, Class fromClass) {
        addLifecycleEvent(POST_UPDATE_EVENT, src, fromClass);
    }

    public void postLoad(Object src, Class fromClass) {
        addLifecycleEvent(POST_LOAD_EVENT, src, fromClass);
    }

    /**
     * @return Returns the registeredEvents.
     */
    public List getRegisteredEvents() {
        return registeredEvents;
    }

    /**
     * @return Returns the registeredEvents.
     */
    public EventTypeAndClass[] getRegisteredEventsAsArray() {
        return (EventTypeAndClass[]) registeredEvents.toArray(new EventTypeAndClass[registeredEvents.size()]);
    }

    /**
     * @param lifecycleEvent The lifecycleEvent to add.
     */
    protected void addLifecycleEvent(int lifecycleEvent, Object src, Class fromClass) {

        System.out.println("                                              "+
                getEventName(lifecycleEvent) + " | " + src.getClass()+"  fromClass = "+ fromClass);
        registeredEvents.add(new EventTypeAndClass(lifecycleEvent,
                src.getClass(), fromClass));
    }


    public String getEventName(int eventType) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getType().equals(Integer.TYPE)) {
                    if (field.getInt(this) == eventType) {
                        return field.getName();
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";

    }

    public static class EventTypeAndClass {
        private int eventType;
        private Class srcClass;
        private Class fromClass;

        public EventTypeAndClass(int eventType, Class srcClass, Class fromClass) {
            this.eventType = eventType;
            this.srcClass = srcClass;
            this.fromClass = fromClass;
        }

        public int getEventType() {
            return eventType;
        }

        public Class getSrcClass() {
            return srcClass;
        }

        public Class getFromClass() {
            return fromClass;
        }
    }
}

