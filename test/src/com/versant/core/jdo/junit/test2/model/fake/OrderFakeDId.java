
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
package com.versant.core.jdo.junit.test2.model.fake;

import java.util.ArrayList;

/**
 * One side of unmanaged one-to-many for testing the consistency checking.
 * This code does not fix up the back reference on OrderLineAL as the tests
 * need to check that this is not done.
 */
public class OrderFakeDId {

    private String name;
    private ArrayList lines = new ArrayList(); // inverse OrderLineFakeDId.order

    public OrderFakeDId(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList getLines() {
        return lines;
    }

    public void add(OrderLineFakeDId ol) {
        lines.add(ol);
    }

    public void remove(OrderLineFakeDId ol) {
        lines.remove(ol);
    }

    public void setLines(ArrayList lines) {
        this.lines = lines;
    }

    /**
     * Get list of OrderLines as a space separated String.
     */
    public String getLinesString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) s.append(' ');
            s.append(lines.get(i));
        }
        return s.toString();
    }

}

