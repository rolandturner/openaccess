
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
package com.versant.core.jdbc;

import com.versant.core.util.classhelper.ClassHelper;

import com.versant.core.common.BindingSupportImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * This manages and creates JdbcKeyGeneratorFactory instances from the names
 * of the factory classes. It makes sure that a given factory is only
 * created once.
 */
public class JdbcKeyGeneratorFactoryRegistry {

    private ClassLoader loader;
    private Map map = new HashMap();

    public JdbcKeyGeneratorFactoryRegistry(ClassLoader loader) {
        this.loader = loader;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    /**
     * Get the factory with class name.
     */
    public JdbcKeyGeneratorFactory getFactory(String name) {
        JdbcKeyGeneratorFactory ans = (JdbcKeyGeneratorFactory)map.get(name);
        if (ans == null) {
            Class t = null;
            try {
                t = ClassHelper.get().classForName(name, true, loader);
            } catch (ClassNotFoundException e) {
                throw BindingSupportImpl.getInstance().runtime(
                    "Unable to load JdbcKeyGeneratorFactory class " + name, e);
            }
            try {
                ans = (JdbcKeyGeneratorFactory)t.newInstance();
            } catch (Exception x) {
                throw BindingSupportImpl.getInstance().runtime(
                        "Unable to create JdbcKeyGeneratorFactory instance " +
                        t.getName(), x);
            }
            map.put(name, ans);
        }
        return ans;
    }

    /**
     * Add an alias for a factory.
     */
    public void add(String alias, JdbcKeyGeneratorFactory f) {
        map.put(alias, f);
    }

}
