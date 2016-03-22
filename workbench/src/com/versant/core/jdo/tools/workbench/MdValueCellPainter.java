
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
package com.versant.core.jdo.tools.workbench;

import za.co.hemtech.gui.painter.StringCellPainter;
import com.versant.core.jdo.tools.workbench.model.MdValue;

import javax.swing.table.TableModel;
import java.awt.*;

/**
 * This paints an MdValue. It automatically paints the default value if the
 * text is null. The text is painted in red if it is invalid.
 * @keep-all
 */
public class MdValueCellPainter extends StringCellPainter {

    protected Color getTextColor(Component c, TableModel model, int row,
            int col, Object value, boolean enabled, boolean focus,
            boolean selected, boolean cursor) {
        MdValue v = (MdValue)value;
        if (v != null && v.isValid()) {
            if (v.getText() == null) {
                Color cc = v.getDefaultColor();
                if (cc == null) {
                    cc = super.getTextColor(c, model, row, col, value, enabled,
                        focus, selected, cursor);
                }
                return cc;
            } else {
                return v.getColor();
            }
        } else {
            if (v.isWarningOnError()){
                return Color.magenta.darker();
            } else {
                return Color.red;
            }
        }
    }
}

