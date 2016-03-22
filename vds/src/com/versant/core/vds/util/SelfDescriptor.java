
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
/** This class can be marked as Main-Class in a JAR archive to
 * print some specific information. 
 * Prints the content of a text file to standard output.
 *  By default assumes the file named "version.txt" relative to
 *  the URI where this class is loaded.
 *  Used for printing the build information of the entire product  
 * @author ppoddar
 * @since 1.0
 */
package com.versant.core.vds.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SelfDescriptor {

    private static String BUILD_INFO = "version.txt";

    /**
     * 
     */
    public SelfDescriptor() {
        super();
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) BUILD_INFO = args[0];
        InputStream is = SelfDescriptor.class.getClassLoader().getResourceAsStream(
                BUILD_INFO);
        if (is != null) {
            BufferedReader f = new BufferedReader(new InputStreamReader(is));
            String line = f.readLine();
            while (line != null) {
                System.out.println(line);
                line = f.readLine();
            }
        } else {
            System.err.println(
                    "Build information file [" + BUILD_INFO + "] not found");
        }

    }
}
