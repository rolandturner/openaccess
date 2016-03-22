
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
/*
 * Created on Sep 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.versant.core.jdo.junit.test3;

import com.versant.core.jdo.junit.test3.model.colreuse.SubClassA;
import com.versant.core.jdo.junit.test3.model.colreuse.SubClassB;
import com.versant.core.jdo.junit.test3.model.colreuse.SubClassC;
import com.versant.core.jdo.junit.test3.model.colreuse.ClassB;
import com.versant.core.jdo.junit.VersantTestCase;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOHelper;
import javax.jdo.Extent;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

/**
 * @author rgreene
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestColReuse extends VersantTestCase {

    public void testColReuse() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();

        SubClassA saI = new SubClassA("subvalueAI");
        saI.setSuperAttr("SuperAValueI");
        SubClassA saF = new SubClassA("subvalueAF");
        saF.setSuperAttr("SuperAValueF");
        SubClassB sbI = new SubClassB("subvalueBI");
        sbI.setSuperAttr("SuperBValueI");
        SubClassB sbF = new SubClassB("subvalueBF");
        sbF.setSuperAttr("SuperBValueF");
        SubClassC scI = new SubClassC("subvalueCI");
        scI.setSuperAttr("SuperCValueI");
        SubClassC scF = new SubClassC("subvalueCF");
        scF.setSuperAttr("SuperCValueF");

        ClassB b = new ClassB();
        b.addToAI(saI);
        b.addToBI(sbI);
        b.addToCI(scI);
        b.addToAF(saF);
        b.addToBF(sbF);
        b.addToCF(scF);

        pm.makePersistent(b);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        Extent e = pm.getExtent(ClassB.class, false);
        ClassB found = (ClassB)e.iterator().next();

        if (found != null) {
            Assert.assertEquals(1, found.getaInv().size());
            Assert.assertEquals(1, found.getbInv().size());
            Assert.assertEquals(1, found.getcInv().size());
            Assert.assertEquals(1, found.getaFake().size());
            Assert.assertEquals(1, found.getbFake().size());
            Assert.assertEquals(1, found.getcFake().size());
        }
        pm.deletePersistentAll(new Object[]{saI, sbI, scI, saF, sbF, scF, b});
        pm.currentTransaction().commit();
        pm.close();
    }
}
