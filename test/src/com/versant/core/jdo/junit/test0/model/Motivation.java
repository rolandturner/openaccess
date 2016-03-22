
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
package com.versant.core.jdo.junit.test0.model;

/**
 * @keep-all
 */
public class Motivation {

    private Type type;
    private String description;

    public Motivation(Type argType, String argDescription) {
        type = argType;
        description = argDescription;
    }

    public String toString() {
        return type + ": " + description;
    }

    public Type getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public static class Type {

        private String type;

        private Type(String argType) {
            type = argType;
        }

        public String toString() {
            return type;
        }

        public static final Type DEAL = new Type("Deal");
        public static final Type USER = new Type("User");
        public static final Type INTERNAL = new Type("Internal");
    }
}

