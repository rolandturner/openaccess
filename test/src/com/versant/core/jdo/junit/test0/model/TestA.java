
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

import java.util.*;

/**
 * @keep-all
 */
public class TestA {

    private List list = new ArrayList();
    private int listCounter;

    private Set set = new HashSet();
    private int setCounter;

    private int ownCounter;

    private int totalCounter;

    public void addToList(TestB testB) {
        listCounter += testB.getCount();
        totalCounter += testB.getCount();
        list.add(testB);
    }

    public void removeFromList(TestB testB) {
        if (list.remove(testB)) {
            listCounter -= testB.getCount();
            totalCounter -= testB.getCount();
        }
    }

    public void removeFromList(Random random) {
        if (list.size() == 0) return;
        removeFromList((TestB)list.get(random.nextInt(list.size())));
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public void addToSet(TestB testB) {
        setCounter += testB.getCount();
        totalCounter += testB.getCount();
        set.add(testB);
    }

    public void removeFromSet(TestB testB) {
        if (set.remove(testB)) {
            setCounter -= testB.getCount();
            totalCounter -= testB.getCount();
        }
    }

    public void removeFromSet(Random random) {
        if (list.size() == 0) return;
        removeFromSet((TestB)list.get(random.nextInt(list.size())));
    }

    public void modifyOwnCounter(Random random) {
        int n = random.nextInt();
        ownCounter += n;
        totalCounter += n;
    }

    public void isValidate() throws Exception {
        int localLCount = 0;
        int localSCount = 0;
        int localTotalCount = 0;
        List lList = list;
        for (int i = 0; i < lList.size(); i++) {
            TestB testB = (TestB)lList.get(i);
            testB.validate();
            localLCount += testB.getCount();
        }
        if (localLCount != listCounter) {
            throw new Exception("The listCounter is out of synch");
        }

        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            TestB testB = (TestB)iterator.next();
            testB.validate();
            localSCount += testB.getCount();
        }
        if (localSCount != setCounter) {
            throw new Exception("The setCounter is out of synch");
        }

        localTotalCount += localLCount;
        localTotalCount += localSCount;
        localTotalCount += ownCounter;
        if (localTotalCount != totalCounter) {
            throw new Exception("The totalCounter is out of synch");
        }
    }
}


