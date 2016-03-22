
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
package com.versant.core.jdo.tools.workbench.editor;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

/**
 * @keep-all
 */
public class JavaSupport {
    public static void setMouseWheel(final JEditTextArea area) {
        area.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    area.setFirstLine(area.getFirstLine() + e.getUnitsToScroll());
                }
            }
        });
    }
}
