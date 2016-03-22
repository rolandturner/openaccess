
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
package com.versant.core.jdo.tools.workbench.model;

import java.util.Iterator;

/**
 * Base class for MD model objects.
 */
public class MdBase {

    protected MdEventListenerList listenerList = new MdEventListenerList();

    public void addMdChangeListener(MdChangeListener listener) {
        listenerList.addListener(listener);
    }

    public void removeMdChangeListener(MdChangeListener listener) {
        listenerList.removeListener(listener);
    }

    /**
     * Fire ChangeEvent.
     */
    public void fireMdChangeEvent(MdProject project, MdDataStore datastore,
            int flags) {
        MdChangeEvent event = new MdChangeEvent(this, project, datastore,
                flags);
        Iterator it = listenerList.getListeners(/*CHFC*/MdChangeListener.class/*RIGHTPAR*/);
        while (it.hasNext() && !event.isConsumed()) {
            MdChangeListener listener = (MdChangeListener)it.next();
            listener.metaDataChanged(event);
        }
    }

    /**
     * Fire ChangeEvent.
     */
    public void fireMdChangeEvent(MdProject project, MdDataStore datastore) {
        fireMdChangeEvent(project, datastore, 0);
    }

}

