
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

import java.util.HashMap;
import java.awt.*;

public class MdProjectProviderManager {

    private static HashMap providers = new HashMap();
    private static final Object NULL_KEY = "NULL_KEY";

    public static MdProjectProvider getDefaultProjectProvider() {
        return getProjectProvider(NULL_KEY);
    }

    public static MdProjectProvider getProjectProvider(Object key) {
        if(key == null){
            key = NULL_KEY;
        }
        return (MdProjectProvider) providers.get(key);
    }

    public static void setProjectProvider(MdProjectProvider projectProvider, Object key) {
        if(key == null){
            key = NULL_KEY;
        }
        providers.put(key, projectProvider);
    }

    public static MdProjectProvider findProjectProvider(Object[] o) {
        for (int i = 0; i < o.length; i++) {
            Object o1 = o[i];
            MdProjectProvider provider = findProjectProvider(o1);
            if(provider != null){
                return provider;
            }
        }
        return getDefaultProjectProvider();
    }

    private static MdProjectProvider findProjectProvider(Object o) {
        if (o instanceof MdProjectProvider) {
            return (MdProjectProvider) o;
        }else if (o instanceof Component) {
            for(Component c = (Component) o; c != null; c = c.getParent()){
                if(c instanceof MdProjectProvider){
                    return (MdProjectProvider)c;
                }
            }
        }
        return null;
    }
}
