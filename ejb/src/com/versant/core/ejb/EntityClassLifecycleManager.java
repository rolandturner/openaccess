
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
package com.versant.core.ejb;

import com.versant.core.jdo.EntityLifecycleEvent;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * The EntityClassLifecycleManager keeps track of Lifecycle callbacks and
 * listeners for a specific class.
 *
 * This class can currently only take only one listener or callback for a event
 * per class it is unclear in the spec if this is correct, it this is incorrect,
 * then we can change it later.
 */
public class EntityClassLifecycleManager {
    private static final Object[] EMPTY_ARRAY = new Object[]{};

    public static final int PRE_PERSIST_EVENT = 1;
    public static final int POST_PERSIST_EVENT = 2;
    public static final int PRE_REMOVE_EVENT = 3;
    public static final int POST_REMOVE_EVENT = 4;
    public static final int PRE_UPDATE_EVENT = 5;
    public static final int POST_UPDATE_EVENT = 6;
    public static final int POST_LOAD_EVENT = 7;

    private PrePersistNode prePersistNode;
    private PostPersistNode postPersistNode;
    private PreRemoveNode preRemoveNode;
    private PostRemoveNode postRemoveNode;
    private PreUpdateNode preUpdateNode;
    private PostUpdateNode postUpdateNode;
    private PostLoadNode postLoadNode;

    public EntityClassLifecycleManager() {
    }

    /**
     * The sequence in which the events are added is important, they have to be
     * added from top class in the hierarchy down.
     */
    public void add(EntityLifecycleEvent event) {
        switch (event.getEvent()) {
            case PRE_PERSIST_EVENT:
                prePersistNode = new PrePersistNode(event.getListener(),
                        event.getMethod());
                break;
            case POST_PERSIST_EVENT:
                postPersistNode = new PostPersistNode(event.getListener(),
                        event.getMethod());
                break;
            case PRE_REMOVE_EVENT:
                preRemoveNode = new PreRemoveNode(event.getListener(),
                        event.getMethod());
                break;
            case POST_REMOVE_EVENT:
                postRemoveNode = new PostRemoveNode(event.getListener(),
                        event.getMethod());
                break;
            case PRE_UPDATE_EVENT:
                preUpdateNode = new PreUpdateNode(event.getListener(),
                        event.getMethod());
                break;
            case POST_UPDATE_EVENT:
                postUpdateNode = new PostUpdateNode(event.getListener(),
                        event.getMethod());
                break;
            case POST_LOAD_EVENT:
                postLoadNode = new PostLoadNode(event.getListener(),
                        event.getMethod());
                break;
        }
    }

    public void firePrePersist(Object src) {
        if (prePersistNode != null) {
            prePersistNode.fire(src);
        }
    }

    public void firePostPersist(Object src) {
        if (postPersistNode != null) {
            postPersistNode.fire(src);
        }
    }

    public void firePreRemove(Object src) {
        if (preRemoveNode != null) {
            preRemoveNode.fire(src);
        }
    }

    public void firePostRemove(Object src) {
        if (postRemoveNode != null) {
            postRemoveNode.fire(src);
        }
    }

    public void firePreUpdate(Object src) {
        if (preUpdateNode != null) {
            preUpdateNode.fire(src);
        }
    }

    public void firePostUpdate(Object src) {
        if (postUpdateNode != null) {
            postUpdateNode.fire(src);
        }
    }

    public void firePostLoad(Object src) {
        if (postLoadNode != null) {
            postLoadNode.fire(src);
        }
    }


    /**
     * Do we have any PrePersist listeners?
     */
    public boolean hasPrePersistListeners() {
        return prePersistNode != null;
    }

    /**
     * Do we have any PostPersist listeners?
     */
    public boolean hasPostPersistListeners() {
        return postPersistNode != null;
    }

    /**
     * Do we have any PreRemove listeners?
     */
    public boolean hasPreRemoveListeners() {
        return preRemoveNode != null;
    }

    /**
     * Do we have any PostRemove listeners?
     */
    public boolean hasPostRemoveListeners() {
        return postRemoveNode != null;
    }

    /**
     * Do we have any PreUpdate listeners?
     */
    public boolean hasPreUpdateListeners() {
        return preUpdateNode != null;
    }

    /**
     * Do we have any PostUpdate listeners?
     */
    public boolean hasPostUpdateListeners() {
        return postUpdateNode != null;
    }

    /**
     * Do we have any PostLoad listeners?
     */
    public boolean hasPostLoadListeners() {
        return postLoadNode != null;
    }

    public static class Node {
        public Object listener;
        public Method method;

        public Node(Object listener, Method method) {
            this.listener = listener;
            this.method = method;
        }

        public void fire(Object entity) {
            if (listener != null) { // we are calling a listener
                try {
                    method.invoke(listener, new Object[]{entity});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {  // we are doing a callback
                try {
                    method.invoke(entity, EMPTY_ARRAY);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    private static class PrePersistNode extends Node {
        public PrePersistNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PostPersistNode extends Node {
        public PostPersistNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PreRemoveNode extends Node {
        public PreRemoveNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PostRemoveNode extends Node {
        public PostRemoveNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PreUpdateNode extends Node {
        public PreUpdateNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PostUpdateNode extends Node {
        public PostUpdateNode(Object listener, Method method) {
            super(listener, method);
        }
    }

    private static class PostLoadNode extends Node {
        public PostLoadNode(Object listener, Method method) {
            super(listener, method);
        }
    }
}

