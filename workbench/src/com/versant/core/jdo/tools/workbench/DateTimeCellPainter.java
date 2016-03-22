
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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Paints full date and time for a java.util.Date or a Long.
 * @keep-all
 */
public class DateTimeCellPainter extends StringCellPainter {

    private static final SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    /**
     * Convert the value into a String.
     */
    public String formatValue(Object value) {
        if (value == null) {
            return nullValue;
        } else {
            if (value instanceof Date) {
                return DATE_FORMAT.format((Date)value);
            } if (value instanceof Long) {
                long t = ((Long)value).longValue();
                if (t == 0) return "";
                Date d = new Date(t);
                return DATE_FORMAT.format(d);
            } else {
                return value.toString();
            }
        }
    }
}

