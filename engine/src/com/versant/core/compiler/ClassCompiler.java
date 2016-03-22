
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

import java.util.Map;
import java.util.Collection;

/**
 * Something that will take a Map of fully qualified name -> src code and
 * return a Map of fully qualified name -> bytecode for each class.
 */
public interface ClassCompiler {

    /**
     * Compile each class in classMap and return a corresponding bytecode.
     *
     * @param classMap Fully qualified class name -> source code
     * @param loader Use this to load referenced classes
     * @return Map of class name -> byte[] containing bytecode
     */
    public Map compile(Map classMap, ClassLoader loader);

}

