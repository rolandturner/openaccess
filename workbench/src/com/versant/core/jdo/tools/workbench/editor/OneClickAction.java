
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
package com.versant.core.jdo.tools.workbench.editor;

import java.awt.event.ActionEvent;

/**
 * @keep-all
 */
public class OneClickAction extends MenuAction {
    private MenuAction action;

    public OneClickAction(String name) {
        super(name);
    }

    public OneClickAction(String name, String action) {
        super(name);
//        this.action = Jext.getAction(action);
    }

    public void actionPerformed(ActionEvent evt) {
        getTextArea(evt).setOneClick(this, evt);
    }

    public void oneClickActionPerformed(ActionEvent evt) {
        if (action != null)
            action.actionPerformed(evt);
    }

    /***************************************************************************
     Patch
     -> Memory management improvements : it may help the garbage collector.
     -> Author : Julien Ponge (julien@izforge.com)
     -> Date : 23, May 2001
     ***************************************************************************/
    protected void finalize() throws Throwable {
        super.finalize();

        action = null;
    }
}


