
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
package com.versant.core.jdo.junit.test0.model;

import javax.jdo.listener.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;

public class AllInstanceLifecycleListener implements CreateLifecycleListener,
        DeleteLifecycleListener, LoadLifecycleListener, StoreLifecycleListener,
        DetachLifecycleListener, AttachLifecycleListener, ClearLifecycleListener,
        DirtyLifecycleListener {

    public static final int EVENT_POST_CREATE = 1;

    public static final int EVENT_POST_LOAD = 2;

    public static final int EVENT_PRE_STORE = 3;
    public static final int EVENT_POST_STORE = 4;

    public static final int EVENT_PRE_CLEAR = 5;
    public static final int EVENT_POST_CLEAR = 6;

    public static final int EVENT_PRE_DELETE = 7;
    public static final int EVENT_POST_DELETE = 8;

    public static final int EVENT_PRE_DIRTY = 9;
    public static final int EVENT_POST_DIRTY = 10;

    public static final int EVENT_PRE_DETACH = 11;
    public static final int EVENT_POST_DETACH = 12;

    public static final int EVENT_PRE_ATTACH = 13;
    public static final int EVENT_POST_ATTACH = 14;

    List registeredEvents = new ArrayList();

    public AllInstanceLifecycleListener() {}

    /* (non-Javadoc)
     * @see javax.jdo.CreateLifecycleListener#postCreate(javax.jdo.LifecycleEvent)
     */
    public void postCreate(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.CREATE, event.getEventType());
        addLifecycleEvent(EVENT_POST_CREATE, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.DeleteLifecycleListener#preDelete(javax.jdo.LifecycleEvent)
     */
    public void preDelete(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DELETE, event.getEventType());
        addLifecycleEvent(EVENT_PRE_DELETE, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.DeleteLifecycleListener#postDelete(javax.jdo.LifecycleEvent)
     */
    public void postDelete(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DELETE, event.getEventType());
        addLifecycleEvent(EVENT_POST_DELETE, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
    * @see javax.jdo.LoadLifecycleListener#load(javax.jdo.LifecycleEvent)
    */
    public void postLoad(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.LOAD, event.getEventType());
        addLifecycleEvent(EVENT_POST_LOAD, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.StoreLifecycleListener#preStore(javax.jdo.LifecycleEvent)
     */
    public void preStore(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.STORE, event.getEventType());
        addLifecycleEvent(EVENT_PRE_STORE, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.StoreLifecycleListener#postStore(javax.jdo.LifecycleEvent)
     */
    public void postStore(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.STORE, event.getEventType());
        addLifecycleEvent(EVENT_POST_STORE, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
    * @see javax.jdo.DetachLifecycleListener#preDetach(javax.jdo.LifecycleEvent)
    */
    public void preDetach(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DETACH, event.getEventType());
        addLifecycleEvent(EVENT_PRE_DETACH, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
    * @see javax.jdo.DetachLifecycleListener#postDetach(javax.jdo.LifecycleEvent)
    */
    public void postDetach(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DETACH, event.getEventType());
        addLifecycleEvent(EVENT_POST_DETACH, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
    * @see javax.jdo.AttachLifecycleListener#preAttach(javax.jdo.LifecycleEvent)
    */
    public void preAttach(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.ATTACH, event.getEventType());
        addLifecycleEvent(EVENT_PRE_ATTACH, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.AttachLifecycleListener#postAttach(javax.jdo.LifecycleEvent)
     */
    public void postAttach(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.ATTACH, event.getEventType());
        addLifecycleEvent(EVENT_POST_ATTACH, event.getSource(), event.getTarget());
    }


    /* (non-Javadoc)
    * @see javax.jdo.ClearLifecycleListener#preClear(javax.jdo.LifecycleEvent)
    */
    public void preClear(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.CLEAR, event.getEventType());
        addLifecycleEvent(EVENT_PRE_CLEAR, event.getSource(), event.getTarget());
//        try {
//            throw new Exception("preClear was called");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.ClearLifecycleListener#postClear(javax.jdo.LifecycleEvent)
     */
    public void postClear(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.CLEAR, event.getEventType());
        addLifecycleEvent(EVENT_POST_CLEAR, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.DirtyLifecycleListener#preDirty(javax.jdo.LifecycleEvent)
     */
    public void preDirty(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DIRTY, event.getEventType());
        addLifecycleEvent(EVENT_PRE_DIRTY, event.getSource(), event.getTarget());
    }

    /* (non-Javadoc)
     * @see javax.jdo.DirtyLifecycleListener#postDirty(javax.jdo.LifecycleEvent)
     */
    public void postDirty(InstanceLifecycleEvent event) {
        assertEvent(InstanceLifecycleEvent.DIRTY, event.getEventType());
        addLifecycleEvent(EVENT_POST_DIRTY, event.getSource(), event.getTarget());
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
    protected void addLifecycleEvent(int lifecycleEvent, Object src, Object target) {
        System.out.println(getEventName(lifecycleEvent) + " | " + src.getClass());

        registeredEvents.add(new EventTypeAndClass(lifecycleEvent,
                src.getClass(),
                (target == null ? null : target.getClass()) ));
    }

    private void assertEvent(int expected, int received) {
        if (expected != received) {
            throw new RuntimeException("Expected event: " + expected + " but received: " + received);
        }
    }

    public String getEventName(int eventType) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getType().equals(Integer.TYPE)){
                    if (field.getInt(this) == eventType){
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
        private Class targetClass;

        public EventTypeAndClass(int eventType, Class srcClass, Class targetClass) {
            this.eventType = eventType;
            this.srcClass = srcClass;
            this.targetClass = targetClass;
        }

        public int getEventType() {
            return eventType;
        }

        public Class getSrcClass() {
            return srcClass;
        }

        public Class getTargetClass() {
            return targetClass;
        }
    }
}

