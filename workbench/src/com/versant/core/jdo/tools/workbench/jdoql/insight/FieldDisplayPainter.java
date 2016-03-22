
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
package com.versant.core.jdo.tools.workbench.jdoql.insight;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableModel;

import za.co.hemtech.gui.painter.IconAndTextCellPainter;
import za.co.hemtech.gui.painter.AlternateCellPainter;
import za.co.hemtech.gui.painter.IconPainter;
import za.co.hemtech.gui.Icons;
import za.co.hemtech.gui.util.IconAndText;
import za.co.hemtech.gui.util.GuiUtils;

/**
 * @keep-all
 */
public class FieldDisplayPainter extends IconAndTextCellPainter implements AlternateCellPainter{
    private Icon field = Icons.getIcon("Field16.gif");
    private Icon method = Icons.getIcon("Method16.gif");
    private Icon var = Icons.getIcon("Value16.gif");
    private Icon param = Icons.getIcon("Parameter16.gif");


    protected IconAndText getIconAndText(Object value) {
        return new IconAndText(getIcon(value), " "+((FieldDisplay)value).getDisplayName());
    }

    public Icon getIcon(Object value) {
        if (value instanceof DisplayVariable) {
            return var;
        } else if (value instanceof DisplayParam) {
            return param;
        } else if (value instanceof DisplayMethod){
            return method;
        } else if (value instanceof DisplayReserved) {
            return null;
        } else {
            return field;
        }
    }

    protected Color getTextColor(Component c, TableModel model, int row,
                                 int col, Object value, boolean enabled, boolean focus,
                                 boolean selected, boolean cursor) {
        if (value instanceof DisplayInheritedField){
            return Color.gray;
        } else {
            return Color.black;
        }
    }

    // 8528809 Lee

    public String getForType() {
        return "com.versant.core.jdo.tools.workbench.jdoql.insight.FieldDisplay";
    }

    public String getName() {
        return "PopupPainter";
    }

}

