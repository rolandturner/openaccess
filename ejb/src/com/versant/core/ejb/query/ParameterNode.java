
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
package com.versant.core.ejb.query;

/**
 * A parameter.
 */
public class ParameterNode extends Node {

    private String name;
    private int position;
    private ResolveContext.ParamUsage usage;

    public ParameterNode(boolean positional, String name) {
        this.name = name;
        if (positional) {
            position = Integer.parseInt(name);
            if (position <= 0) {
                throw new IllegalArgumentException(
                        "Invalid positional parameter: " + position);
            }
        } else {
            position = -1;
        }
    }

    public boolean isPositional() {
        return position >= 1;
    }

    /**
     * This returns the String form of the position for positional parameters
     * and the name for named parameters.
     */
    public String getName() {
        return name;
    }

    /**
     * This returns -1 for named parameters.
     */
    public int getPosition() {
        return position;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveParameterNode(this, msg);
    }

    public String toStringImp() {
        return (isPositional() ? "?" : ":") + name;
    }

    public void resolve(ResolveContext rc) {
        usage = rc.addParameterNode(this);
    }

    /**
     * Get our usage entry
     */
    public ResolveContext.ParamUsage getUsage() {
        return usage;
    }

}

