
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

import javax.jdo.listener.*;

/**
 * This keeps track of lists of InstanceLifecycleListener's for the different
 * InstanceLifecycleEvent's. It implements a copy-on-write scheme so the lists
 * on the PMF can be shared by all PMs until first modified. Any modification to
 * a LifecycleListenerManager via add or remove will return a reference
 * to a new LifecycleListenerManager with the change applied. If a
 * LifecycleListenerManager becomes empty then null is returned.
 */
public class LifecycleListenerManager {
    private AttachNode attachList;
    private ClearNode clearList;
    private CreateNode createList;
    private DeleteNode deleteList;
    private DetachNode detachList;
    private DirtyNode dirtyList;
    private LoadNode loadList;
    private StoreNode storeList;

    /**
     * Create new LifecycleListenerManager containing toAdd.
     */
    public LifecycleListenerManager(InstanceLifecycleListener toAdd) {
        addImp(toAdd);
    }

    /**
     * Copy constructor.
     */
    private LifecycleListenerManager(LifecycleListenerManager toCopy) {
        attachList = toCopy.attachList == null ? null : toCopy.attachList.copy();
        clearList = toCopy.clearList == null ? null : toCopy.clearList.copy();
        createList = toCopy.createList == null ? null : toCopy.createList.copy();
        deleteList = toCopy.deleteList == null ? null : toCopy.deleteList.copy();
        detachList = toCopy.detachList == null ? null : toCopy.detachList.copy();
        dirtyList = toCopy.dirtyList == null ? null : toCopy.dirtyList.copy();
        loadList = toCopy.loadList == null ? null : toCopy.loadList.copy();
        storeList = toCopy.storeList == null ? null : toCopy.storeList.copy();
    }

    /**
     * Return a copy of this LifecycleListenerManager with l added.
     */
    public LifecycleListenerManager add(InstanceLifecycleListener l) {
        return new LifecycleListenerManager(this).addImp(l);
    }

    private LifecycleListenerManager addImp(InstanceLifecycleListener l) {
        if (l instanceof AttachLifecycleListener) {
            attachList = new AttachNode((AttachLifecycleListener) l,
                    attachList);
        }
        if (l instanceof ClearLifecycleListener) {
            clearList = new ClearNode((ClearLifecycleListener) l,
                    clearList);
        }
        if (l instanceof CreateLifecycleListener) {
            createList = new CreateNode((CreateLifecycleListener) l,
                    createList);
        }
        if (l instanceof javax.jdo.listener.DeleteLifecycleListener) {
            deleteList = new DeleteNode((DeleteLifecycleListener) l,
                    deleteList);
        }
        if (l instanceof DetachLifecycleListener) {
            detachList = new DetachNode((DetachLifecycleListener) l,
                    detachList);
        }
        if (l instanceof DirtyLifecycleListener) {
            dirtyList = new DirtyNode((DirtyLifecycleListener) l,
                    dirtyList);
        }
        if (l instanceof LoadLifecycleListener) {
            loadList = new LoadNode((LoadLifecycleListener) l,
                    loadList);
        }
        if (l instanceof StoreLifecycleListener) {
            storeList = new StoreNode((StoreLifecycleListener) l,
                    storeList);
        }
        return this;
    }

    /**
     * Return a copy of this LifecycleListenerManager with l removed or
     * null if the resulting LifecycleListenerManager would contain no
     * listeners.
     */
    public LifecycleListenerManager remove(InstanceLifecycleListener l) {
        return new LifecycleListenerManager(this).removeImp(l);
    }

    private LifecycleListenerManager removeImp(InstanceLifecycleListener l) {

        if (l instanceof AttachLifecycleListener) {
            AttachNode prev = null;
            for (AttachNode i = attachList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        attachList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof ClearLifecycleListener) {
            ClearNode prev = null;
            for (ClearNode i = clearList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        clearList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof CreateLifecycleListener) {
            CreateNode prev = null;
            for (CreateNode i = createList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        createList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof DeleteLifecycleListener) {
            DeleteNode prev = null;
            for (DeleteNode i = deleteList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        deleteList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof DetachLifecycleListener) {
            DetachNode prev = null;
            for (DetachNode i = detachList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        detachList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof DirtyLifecycleListener) {
            DirtyNode prev = null;
            for (DirtyNode i = dirtyList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        dirtyList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof LoadLifecycleListener) {
            LoadNode prev = null;
            for (LoadNode i = loadList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        loadList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (l instanceof StoreLifecycleListener) {
            StoreNode prev = null;
            for (StoreNode i = storeList; i != null; i = i.next) {
                if (i.listener == l) {
                    if (prev == null) {
                        storeList = i.next;
                    } else {
                        prev.next = i.next;
                    }
                }
                prev = i;
            }
        }
        if (attachList == null &&
                clearList == null &&
                createList == null &&
                deleteList == null &&
                detachList == null &&
                dirtyList == null &&
                loadList == null &&
                storeList == null) {
            return null;
        }
        return this;
    }

    /**
     * This method is called before a detached instance is attached.
     *
     * @param src is the detached instance
     */
    public boolean firePreAttach(Object src) {
        if (attachList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.ATTACH);
        for (AttachNode i = attachList; i != null; i = i.next) {
            i.listener.preAttach(ev);
        }
        return true;
    }

    /**
     * This method is called after a detached instance is attached.
     *
     * @param src    is the corresponding persistent instance in the cache.
     * @param target is the detached instance.
     */
    public void firePostAttach(Object src, Object target) {
        if (attachList == null) return;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.ATTACH,
                target);
        for (AttachNode i = attachList; i != null; i = i.next) {
            i.listener.postAttach(ev);
        }
    }

    /**
     * This method is called whenever a persistent instance is cleared,
     * for example during afterCompletion. It is called before the jdoPreClear
     * method is invoked on the instance.
     */
    public boolean firePreClear(Object src) {
        if (clearList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.CLEAR);
        for (ClearNode i = clearList; i != null; i = i.next) {
            i.listener.preClear(ev);
        }
        return true;
    }

    /**
     * This method is called whenever a persistent instance is cleared,
     * for example during afterCompletion. It is called after the jdoPreClear
     * method is invoked on the instance and the fields have been cleared by the
     * JDO implementation.
     */
    public void firePostClear(Object src) {
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.CLEAR);
        for (ClearNode i = clearList; i != null; i = i.next) {
            i.listener.postClear(ev);
        }
    }

    /**
     * This method is called whenever a persistent instance is created,
     * during makePersistent.
     * It is called after the instance transitions to persistent-new.
     */
    public void firePostCreate(Object src) {
        if (createList == null) return;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.CREATE);
        for (CreateNode i = createList; i != null; i = i.next) {
            i.listener.postCreate(ev);
        }
    }

    /**
     * This method is called whenever a persistent instance is deleted,
     * during deletePersistent. It is called before the state transition and
     * before the jdoPreDelete method is invoked on the instance.
     */
    public boolean firePreDelete(Object src) {
        if (deleteList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DELETE);
        for (DeleteNode i = deleteList; i != null; i = i.next) {
            i.listener.preDelete(ev);
        }
        return true;
    }

    /**
     * This method is called whenever a persistent instance is deleted,
     * during deletePersistent. It is called after the jdoPreDelete method is
     * invoked on the instance and after the state transition.
     */
    public void firePostDelete(Object src) {
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DELETE);
        for (DeleteNode i = deleteList; i != null; i = i.next) {
            i.listener.postDelete(ev);
        }
    }

    /**
     * This method is called before a persistent instance is copied for
     * detachment.
     */
    public boolean firePreDetach(Object src) {
        if (detachList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DETACH);
        for (DetachNode i = detachList; i != null; i = i.next) {
            i.listener.preDetach(ev);
        }
        return true;
    }

    /**
     * This method is called whenever a persistent instance is copied for
     * detachment.
     *
     * @param src    is the detached copy
     * @param target is the persistent instance
     */
    public void firePostDetach(Object src, Object target) {
        if (detachList == null) return;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DETACH,
                target);
        for (DetachNode i = detachList; i != null; i = i.next) {
            i.listener.postDetach(ev);
        }
    }

    /**
     * This method is called whenever a persistent clean instance is first made
     * dirty, during an operation that modifies the value of a persistent or
     * transactional field.
     * It is called before the field value is changed.
     */
    public boolean firePreDirty(Object src) {
        if (dirtyList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DIRTY);
        for (DirtyNode i = dirtyList; i != null; i = i.next) {
            i.listener.preDirty(ev);
        }
        return true;
    }

    /**
     * This method is called whenever a persistent clean instance is first made
     * dirty, during an operation that modifies the value of a persistent or
     * transactional field. It is called after the field value was changed.
     */
    public void firePostDirty(Object src) {
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.DIRTY);
        for (DirtyNode i = dirtyList; i != null; i = i.next) {
            i.listener.postDirty(ev);
        }
    }

    /**
     * This method is called whenever a persistent instance is loaded.
     * It is called after the jdoPostLoad method is invoked on the instance.
     */
    public void firePostLoad(Object src) {
        if (loadList == null) return;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.LOAD);
        for (LoadNode i = loadList; i != null; i = i.next) {
            i.listener.postLoad(ev);
        }
    }

    /**
     * This method is called whenever a persistent instance is stored,
     * for example during flush or commit. It is called before the jdoPreStore
     * method is invoked on the instance. An object identity for a persistent-new
     * instance might not have been assigned to the instance when this callback
     * is invoked.
     */
    public boolean firePreStore(Object src) {
        if (storeList == null) return false;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.STORE);
        for (StoreNode i = storeList; i != null; i = i.next) {
            i.listener.preStore(ev);
        }
        return true;
    }

    /**
     * This method is called whenever a persistent instance is stored,
     * for example during flush or commit.
     * It is called after the jdoPreStore method is invoked on the instance.
     * An object identity for a persistent-new instance must have been assigned
     * to the instance when this callback is invoked.
     */
    public void firePostStore(Object src) {
        if (storeList == null) return;
        InstanceLifecycleEvent ev = new InstanceLifecycleEvent(src,
                InstanceLifecycleEvent.STORE);
        for (StoreNode i = storeList; i != null; i = i.next) {
            i.listener.postStore(ev);
        }
    }

    /**
     * Do we have any Store listeners?
     */
    public boolean hasStoreListeners() {
        return storeList != null;
    }

    /**
     * Do we have any Attach listeners?
     */
    public boolean hasAttachListeners() {
        return attachList != null;
    }

    /**
     * Do we have any Clear listeners?
     */
    public boolean hasClearListeners() {
        return clearList != null;
    }

    /**
     * Do we have any Create listeners?
     */
    public boolean hasCreateListeners() {
        return createList != null;
    }

    /**
     * Do we have any Delete listeners?
     */
    public boolean hasDeleteListeners() {
        return deleteList != null;
    }

    /**
     * Do we have any Detach listeners?
     */
    public boolean hasDetachListeners() {
        return detachList != null;
    }

    /**
     * Do we have any Dirty listeners?
     */
    public boolean hasDirtyListeners() {
        return dirtyList != null;
    }

    /**
     * Do we have any Load listeners?
     */
    public boolean hasLoadListeners() {
        return loadList != null;
    }

    private static class StoreNode {
        public StoreLifecycleListener listener;
        public StoreNode next;

        public StoreNode(StoreLifecycleListener listener, StoreNode next) {
            this.listener = listener;
            this.next = next;

        }

        public StoreNode copy() {
            return new StoreNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class DeleteNode {
        public DeleteLifecycleListener listener;
        public DeleteNode next;

        public DeleteNode(DeleteLifecycleListener listener, DeleteNode next) {
            this.listener = listener;
            this.next = next;
        }

        public DeleteNode copy() {
            return new DeleteNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class ClearNode {
        public ClearLifecycleListener listener;
        public ClearNode next;

        public ClearNode(ClearLifecycleListener listener, ClearNode next) {
            this.listener = listener;
            this.next = next;
        }

        public ClearNode copy() {
            return new ClearNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class DirtyNode {
        public DirtyLifecycleListener listener;
        public DirtyNode next;

        public DirtyNode(DirtyLifecycleListener listener, DirtyNode next) {
            this.listener = listener;
            this.next = next;
        }

        public DirtyNode copy() {
            return new DirtyNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class DetachNode {
        public DetachLifecycleListener listener;
        public DetachNode next;

        public DetachNode(DetachLifecycleListener listener, DetachNode next) {
            this.listener = listener;
            this.next = next;
        }

        public DetachNode copy() {
            return new DetachNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class AttachNode {
        public AttachLifecycleListener listener;
        public AttachNode next;

        public AttachNode(AttachLifecycleListener listener, AttachNode next) {
            this.listener = listener;
            this.next = next;
        }

        public AttachNode copy() {
            return new AttachNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class CreateNode {
        public CreateLifecycleListener listener;
        public CreateNode next;

        public CreateNode(CreateLifecycleListener listener, CreateNode next) {
            this.listener = listener;
            this.next = next;
        }

        public CreateNode copy() {
            return new CreateNode(listener,
                    next == null ? null : next.copy());
        }
    }

    private static class LoadNode {
        public LoadLifecycleListener listener;
        public LoadNode next;

        public LoadNode(LoadLifecycleListener listener, LoadNode next) {
            this.listener = listener;
            this.next = next;
        }

        public LoadNode copy() {
            return new LoadNode(listener,
                    next == null ? null : next.copy());
        }
    }
}
