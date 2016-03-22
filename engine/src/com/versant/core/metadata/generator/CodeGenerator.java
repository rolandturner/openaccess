
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
package com.versant.core.metadata.generator;

/**
 * Manages generation of code for all layers. Components register code
 * for classes during startup and the code generator uses
 * these to generate the final State and OID (and in future other)
 * classes.
 */
public interface CodeGenerator {

    /**
     * Add a code provider to the cls. Typically one or more interfaces
     * will be supplied and the codeProvider supplies the implementations
     * of the methods and declarations for fields.
     */
    public void addCodeSpecification(Class cls, Class[] interfacesToAdd,
            String javaCode);

}

