
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
package com.versant.core.jdo.tools.workbench.diagram;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @keep-all
 */
public class FieldListColModel {

    private List comps = new ArrayList();
    private int[] colWidths = new int[0];
    private int totWidth;

    public void addComponent(FieldListComponent component) {
        comps.add(component);
        reCalcColWidths();
    }

    public void removeComponent(FieldListComponent component) {
        comps.remove(component);
        reCalcColWidths();
    }

    public void reCalcColWidths() {
        FieldListComponent component;
        List cols = new ArrayList(comps.size());
        int[] temp;
        int maxColCount = 0;
        for (Iterator it = comps.iterator(); it.hasNext();) {
            component = (FieldListComponent)it.next();
            temp = component.getPreferredColWidths();
            maxColCount = Math.max(maxColCount, temp.length);
            cols.add(temp);
        }
        int[] ints = new int[maxColCount];
        for (Iterator it = cols.iterator(); it.hasNext();) {
            temp = (int[])it.next();
            maxColCount = temp.length;
            for (int i = 0; i < maxColCount; i++) {
                ints[i] = Math.max(ints[i], temp[i]);
            }
        }
        totWidth = 0;
        for (int i = ints.length - 1; i >= 0; i--) totWidth += ints[i];
        colWidths = ints;
    }

    public int[] getColWidths() {
        return colWidths;
    }

    public int getTotWidth() {
        return totWidth;
    }
}
