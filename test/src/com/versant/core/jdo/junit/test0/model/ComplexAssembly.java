
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

import javax.jdo.PersistenceManager;
import java.util.HashSet;

/**
 * @keep-all
 */
public class ComplexAssembly extends Assembly {

    private HashSet subAssemblies = new HashSet();  // <-> Assembly.superAssembly

    public ComplexAssembly(PersistenceManager pm, Module module, int moduleNum,
            ComplexAssembly parentAssembly, int levelNo) {
        super(module, parentAssembly);
        subAssemblies = new HashSet();
        // Create NumAssmPerAssm children objects per parent
        for (int i = 0; i < 1; i++) {
            // Create total of NumAssmLevels - 1 levels of ComplexAssembly and
            // a last level of BaseAssembly.
            if (levelNo < 2 - 1) {
                // Create a complex assembly as the subassembly.
                subAssemblies.add(
                        new ComplexAssembly(pm, module, moduleNum, this,
                                levelNo + 1));
            } else {
                // Create a base assembly as the subassembly.
                BaseAssembly assm = new BaseAssembly(pm, module, this);
                /* JACO commenting out the next line solves the problem
                */
                pm.makePersistent(assm);  // Make persistent to get oid.
                Object assmId = pm.getObjectId(assm);
                subAssemblies.add(assm);
            }
        }
    }

    public HashSet getSubAssemblies() {
        return subAssemblies;
    }

    public void addSubAssembly(Assembly sub) {
        subAssemblies.add(sub);
        sub.setSuperAssembly(this);
    }

}

