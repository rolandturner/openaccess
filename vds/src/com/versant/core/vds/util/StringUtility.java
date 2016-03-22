
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
package com.versant.core.vds.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class StringUtility {

    /**
     * Split a string into an array of substrings.
     */
    public static String[] tokenize(String string, String separator) {
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(string, separator);
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        return (String[])list.toArray(new String[0]);
    }

    /**
     * Gets the head of a string delimited by separator. For example,
     * if separator is '.' then headOf["a.b.c.d"] = "a" or headOf[".x.y.z"] = ""
     */
    public static String headOf(String s, char separator) {
        int index = s.indexOf(separator);
        if (index < 0) return s;
        return s.substring(0, index);
    }

    public static String tailOf(String s, char separator) {
        int index = s.indexOf(separator);
        if (index < 0) return "";
        return s.substring(index + 1);
    }

    public static void main(String[] args) {
        System.err.println(
                "HeadOf[" + args[0] + "] = [" + StringUtility.headOf(args[0],
                        '.') + "]");
        System.err.println(
                "TailOf[" + args[0] + "] = [" + StringUtility.tailOf(args[0],
                        '.') + "]");
    }
}
