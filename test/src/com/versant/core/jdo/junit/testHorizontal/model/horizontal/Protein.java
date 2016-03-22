
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
package com.versant.core.jdo.junit.testHorizontal.model.horizontal;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 09-Nov-2005
 * Time: 17:55:10
 * To change this template use File | Settings | File Templates.
 */
public class Protein extends AminoAcidSequence {
    private int newMember = 42;

    public int getNewMember() {
        return newMember;
    }

    public void setNewMember(int newMember) {
        this.newMember = newMember;
    }
}
