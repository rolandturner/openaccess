
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
package com.versant.core.jdo.junit.test2.model.mbevan1;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Item {
    private List timeLines = new ArrayList();
    private Props props = new Props();

    public Props getProps() {
        return props;
    }

    public void setProps(Props props) {
        this.props = props;
    }

    public List getTimeLines() {
        return timeLines;
    }

    public void setTimeLines(List timeLines) {
        this.timeLines = timeLines;
    }
}
