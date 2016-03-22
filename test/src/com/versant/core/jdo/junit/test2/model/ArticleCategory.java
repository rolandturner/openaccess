
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
package com.versant.core.jdo.junit.test2.model;

import java.util.HashMap;
import java.util.Vector;

/** 
 * For testing tree fetch of pass 2 fields (Bernd bug forums [507]).
 * @keep-all
 */
public class ArticleCategory {

    private HashMap lnkProperties = new HashMap();  // dfg=true, String->Property
    private ArticleCategory lnkParent;
    private Vector lnkChildren = new Vector();

    public ArticleCategory() {
    }

    public HashMap getLnkProperties() {
        return lnkProperties;
    }

    public ArticleCategory getLnkParent() {
        return lnkParent;
    }

    public void setLnkParent(ArticleCategory lnkParent) {
        this.lnkParent = lnkParent;
    }

    public Vector getLnkChildren() {
        return lnkChildren;
    }

    public void addChild(ArticleCategory ac) {
        lnkChildren.add(ac);
        ac.lnkParent = this;
    }

}

