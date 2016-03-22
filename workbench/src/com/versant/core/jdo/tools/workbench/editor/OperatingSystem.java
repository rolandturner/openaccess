
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

import javax.swing.*;
import java.io.File;

/**
 * @keep-all
 */
public class OperatingSystem {
//{{{ isDOSDerived() method
    /**
     * Returns if we're running Windows 95/98/ME/NT/2000/XP, or OS/2.
     */
    public static final boolean isDOSDerived() {
        return isWindows() || isOS2();
    } //}}}

    //{{{ isWindows() method
    /**
     * Returns if we're running Windows 95/98/ME/NT/2000/XP.
     */
    public static final boolean isWindows() {
        return os == WINDOWS_9x || os == WINDOWS_NT;
    } //}}}

    //{{{ isWindows9x() method
    /**
     * Returns if we're running Windows 95/98/ME.
     */
    public static final boolean isWindows9x() {
        return os == WINDOWS_9x;
    } //}}}

    //{{{ isWindowsNT() method
    /**
     * Returns if we're running Windows NT/2000/XP.
     */
    public static final boolean isWindowsNT() {
        return os == WINDOWS_NT;
    } //}}}

    //{{{ isOS2() method
    /**
     * Returns if we're running OS/2.
     */
    public static final boolean isOS2() {
        return os == OS2;
    } //}}}

    //{{{ isUnix() method
    /**
     * Returns if we're running Unix (this includes MacOS X).
     */
    public static final boolean isUnix() {
        return os == UNIX || os == MAC_OS_X;
    } //}}}

    //{{{ isMacOS() method
    /**
     * Returns if we're running MacOS X.
     */
    public static final boolean isMacOS() {
        return os == MAC_OS_X;
    } //}}}

    //{{{ isMacOSLF() method
    /**
     * Returns if we're running MacOS X and using the native L&F.
     */
    public static final boolean isMacOSLF() {
        return (isMacOS() && UIManager.getLookAndFeel().isNativeLookAndFeel());
    } //}}}

    //{{{ isJava14() method
    /**
     * Returns if Java 2 version 1.4 is in use.
     */
    public static final boolean hasJava14() {
        return java14;
    } //}}}

    //{{{ Private members
    private static final int UNIX = 0x31337;
    private static final int WINDOWS_9x = 0x640;
    private static final int WINDOWS_NT = 0x666;
    private static final int OS2 = 0xDEAD;
    private static final int MAC_OS_X = 0xABC;
    private static final int UNKNOWN = 0xBAD;

    private static int os;
    private static boolean java14;

    //{{{ Class initializer
    static {
        if (System.getProperty("mrj.version") != null) {
            os = MAC_OS_X;
        } else {
            String osName = System.getProperty("os.name");
            if (osName.indexOf("Windows 9") != -1
                    || osName.indexOf("Windows ME") != -1) {
                os = WINDOWS_9x;
            } else if (osName.indexOf("Windows") != -1) {
                os = WINDOWS_NT;
            } else if (osName.indexOf("OS/2") != -1) {
                os = OS2;
            } else if (File.separatorChar == '/') {
                os = UNIX;
            } else {
                os = UNKNOWN;
            }
        }

        if (System.getProperty("java.version").compareTo("1.4") >= 0)
            java14 = true;
    } //}}}

    //}}}
}

