
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

/**
 * Utility class to open URLs in whatever browser the user has. From
 * <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip66.html?">
 * http://www.javaworld.com/javaworld/javatips/jw-javatip66.html?</a>.
 *
 * @keep-all
 *
 */
public class BrowserControl {

    public static final String WIN_CMD = "cmd.exe /c start iexplore ";
    public static final String UNIX_CMD = "mozilla";

    private static final String UNIX_ARGS = "-remote openURL";

    /**
     * Display a file in the system browser.  If you want to display a
     * file, you must include the absolute path name.
     * @param url the file's url (http:// or file://).
     */
    public static void displayURL(String wincmd, String unixcmd, String url)
            throws Exception {
        if (isWindowsPlatform()) {
            // cmd = cmd.exe /c start iexplore url
            String cmd = wincmd + url;
            Runtime.getRuntime().exec(cmd);
        } else {
            // Under Unix, Netscape has to be running for the "-remote"
            // command to work.  So, we try sending the command and
            // check for an exit value.  If the exit command is 0,
            // it worked, otherwise we need to start the browser.
            // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
            String cmd = unixcmd + " " + UNIX_ARGS + "(" + url + ")";
            System.out.println("cmd = " + cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            // wait for exit code -- if it's 0, command worked,
            // otherwise we need to start the browser up.
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                // Command failed, start up the browser
                // cmd = 'netscape http://www.javaworld.com'
                cmd = unixcmd + " " + url;
                Runtime.getRuntime().exec(cmd);
            }
        }
    }

    private  static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith("Windows");
    }

}

