
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

import za.co.hemtech.gui.ExceptionListener;

import java.util.HashMap;

public class ExceptionListenerManager {

    private static HashMap exceptionListeners = new HashMap();
    private static final Object NULL_KEY = "NULL_KEY";

    public static ExceptionListener getDefaultExceptionListenerManager() {
        return getExceptionListenerManager(NULL_KEY);
    }

    public static ExceptionListener getExceptionListenerManager(Object key) {
        if(key == null){
            key = NULL_KEY;
        }
        return (ExceptionListener) exceptionListeners.get(key);
    }

    public static void setExceptionListenerManager(ExceptionListener exceptionListener, Object key) {
        if(key == null){
            key = NULL_KEY;
        }
        exceptionListeners.put(key, exceptionListener);
    }
}
