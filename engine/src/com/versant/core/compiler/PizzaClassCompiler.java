
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
package com.versant.core.compiler;

import com.versant.lib.pizzacompiler.compiler.SrcCompiler;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import com.versant.core.common.BindingSupportImpl;

/**
 * ClassCompiler implementation that compiles classes using the Pizza compiler.
 * Pizza is a superset of Java and the compiler is very fast.
 */
public class PizzaClassCompiler implements ClassCompiler {

    public Map compile(Map classMap, ClassLoader loader) {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buf, false);
            Collection c = SrcCompiler.compile(classMap, loader, out);
            out.close();
            if (buf.size() > 0) {
                String msg = buf.toString();
                throw BindingSupportImpl.getInstance().internal(msg);
            }
            HashMap ans = new HashMap();
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                byte[] bytecode = (byte[])i.next();
                ans.put(ClassFileUtils.getClassName(bytecode), bytecode);
            }
            return ans;
        } catch (Throwable t) {
            if (BindingSupportImpl.getInstance().isOwnException(t)) {
                throw (RuntimeException)t;
            }
            throw BindingSupportImpl.getInstance().internal(t.toString(), t);
        }
    }

}

