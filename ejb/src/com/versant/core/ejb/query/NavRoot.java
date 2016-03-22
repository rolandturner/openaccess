
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.CmdBitSet;

/**
 * Root of tree navigated through fields starting at a class.
 */
public class NavRoot extends NavBase {

    private IdentificationVarNode node;
    private ClassMetaData cmd; // the class starting this navigation

    public NavRoot(IdentificationVarNode node, ClassMetaData cmd) {
        this.node = node;
        this.cmd = cmd;
    }

    public String getIdentifier() {
        return getNode().getIdentifier();
    }

    public IdentificationVarNode getNode() {
        return node;
    }

    public NavRoot getRoot() {
        return this;
    }

    public ClassMetaData getNavClassMetaData() {
        return cmd;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(cmd.qname);
        childrenToString(s);
        return s.toString();
    }

}

