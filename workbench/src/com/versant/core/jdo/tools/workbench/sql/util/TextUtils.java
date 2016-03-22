
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
package com.versant.core.jdo.tools.workbench.sql.util;

import java.util.StringTokenizer;

/**
 * Text utilities for a JEditTextArea
 * @keep-all
 */
public class TextUtils {

    /**
     * Check if a string is empty or null.
     */
    public static boolean empty(String s) {
        return s == null || s.trim().length() == 0;
    }


    /**
     * Format our tooltip text into an html table so it can
     * display on multiple lines.
     *
     * @param txt String to format
     * @return Html formatted string
     */
    public static String formatToolTip(String txt) {
        StringTokenizer t = new StringTokenizer(txt, "\n", false);

        StringBuffer toolTip = new StringBuffer();
        toolTip.append("<html><table>");

        for (; t.hasMoreElements();) {
            String s = (String)t.nextElement();
            toolTip.append("<tr>" + s + "</tr>");
        }

        toolTip.append("</table></html>");
        return toolTip.toString();
    }

}

