
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
package com.versant.core.util;

/**
 * Static utility methods for parsing primitives from String's without
 * creating any objects.
 */
public class FastParser {

    /**
     * Parse the int at index from value. The int is assumed to run until
     * the end of the String.
     */
    public static int parseInt(String value, int index) {
        char c = value.charAt(index++);
        int ans;
        if (c == '-') ans = - (value.charAt(index++) - '0');
        else ans = c - '0';
        int n = value.length();
        for (; index < n; ) {
            ans = ans * 10 + (value.charAt(index++) - '0');
        }
        return ans;
    }

    /**
     * Parse the long at index from value. The long is assumed to run until
     * the end of the String.
     */
    public static long parseLong(String value, int index) {
        char c = value.charAt(index++);
        long ans;
        if (c == '-') ans = - (value.charAt(index++) - '0');
        else ans = c - '0';
        int n = value.length();
        for (; index < n; ) {
            ans = ans * 10 + (value.charAt(index++) - '0');
        }
        return ans;
    }

}
