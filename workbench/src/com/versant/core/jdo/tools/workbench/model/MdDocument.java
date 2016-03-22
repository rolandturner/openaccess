
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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.DocType;

import java.util.Iterator;

/**
 * @keep-all This is a JDom Document that also maintains a dirty flag. The methods
 * in XmlUtils that change elements will follow parent links up to the
 * document and set this flag. A ChangeEvent is fired when the flag
 * changes state.
 */
public class MdDocument extends Document {

    private boolean dirty;
    protected MdEventListenerList listenerList = new MdEventListenerList();

    public MdDocument(Element rootElement, DocType docType) {
        super(rootElement, docType);
    }

    public MdDocument(Element rootElement) {
        super(rootElement);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
        }
        fireChangeEvent();
    }

    public void addMdChangeListener(MdChangeListener listener) {
        listenerList.addListener(listener);
    }

    public void removeMdChangeListener(MdChangeListener listener) {
        listenerList.removeListener(listener);
    }

    /**
     * Fire ChangeEvent.
     */
    public void fireChangeEvent() {
        MdChangeEvent event = new MdChangeEvent(this, null, null, 0);
        Iterator it = listenerList.getListeners(/*CHFC*/MdChangeListener.class/*RIGHTPAR*/);
        while (it.hasNext() && !event.isConsumed()) {
            MdChangeListener listener = (MdChangeListener)it.next();
            listener.metaDataChanged(event);
        }
    }

}

