
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

import java.awt.event.KeyEvent;

/**
 * @keep-all
 */
public class KeyEventWorkaround {
    //{{{ processKeyEvent() method
    public static KeyEvent processKeyEvent(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        char ch = evt.getKeyChar();

        switch (evt.getID()) {
            //{{{ KEY_PRESSED...
            case KeyEvent.KEY_PRESSED:
                // get rid of keys we never need to handle
                switch (keyCode) {
                    case KeyEvent.VK_ALT:
                    case KeyEvent.VK_ALT_GRAPH:
                    case KeyEvent.VK_CONTROL:
                    case KeyEvent.VK_SHIFT:
                    case KeyEvent.VK_META:
                    case KeyEvent.VK_DEAD_GRAVE:
                    case KeyEvent.VK_DEAD_ACUTE:
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                    case KeyEvent.VK_DEAD_TILDE:
                    case KeyEvent.VK_DEAD_MACRON:
                    case KeyEvent.VK_DEAD_BREVE:
                    case KeyEvent.VK_DEAD_ABOVEDOT:
                    case KeyEvent.VK_DEAD_DIAERESIS:
                    case KeyEvent.VK_DEAD_ABOVERING:
                    case KeyEvent.VK_DEAD_DOUBLEACUTE:
                    case KeyEvent.VK_DEAD_CARON:
                    case KeyEvent.VK_DEAD_CEDILLA:
                    case KeyEvent.VK_DEAD_OGONEK:
                    case KeyEvent.VK_DEAD_IOTA:
                    case KeyEvent.VK_DEAD_VOICED_SOUND:
                    case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                    case '\0':
                        return null;
                    default:

                        if (!OperatingSystem.isMacOS())
                            handleBrokenKeys(evt, keyCode);
                        else
                            last = LAST_NOTHING;
                        break;
                }

                return evt;
                //}}}
                //{{{ KEY_TYPED...
            case KeyEvent.KEY_TYPED:
                // need to let \b through so that backspace will work
                // in HistoryTextFields
                if ((ch < 0x20 || ch == 0x7f || ch == 0xff) && ch != '\b')
                    return null;

                // "Alt" is the option key on MacOS, and it can generate
                // user input
                if (OperatingSystem.isMacOS()) {
                    if (evt.isControlDown() || evt.isMetaDown())
                        return null;
                } else {
                    if ((evt.isControlDown() ^ evt.isAltDown())
                            || evt.isMetaDown())
                        return null;
                }

                // On JDK 1.4 with Windows, some Alt-key sequences send
                // bullshit in a KEY_TYPED afterwards. We filter it out
                // here
                if (last == LAST_MOD) {
                    switch (ch) {
                        case 'B':
                        case 'M':
                        case 'X':
                        case 'c':
                        case '!':
                        case ',':
                        case '?':
                            last = LAST_NOTHING;
                            return null;
                    }
                }

                // if the last key was a broken key, filter
                // out all except 'a'-'z' that occur 750 ms after.
                else if (last == LAST_BROKEN && System.currentTimeMillis()
                        - lastKeyTime < 750 && !Character.isLetter(ch)) {
                    last = LAST_NOTHING;
                    return null;
                }
                // otherwise, if it was ALT, filter out everything.
                else if (last == LAST_ALT && System.currentTimeMillis()
                        - lastKeyTime < 750) {
                    last = LAST_NOTHING;
                    return null;
                }

                return evt;
                //}}}
                //{{{ KEY_RELEASED...
            case KeyEvent.KEY_RELEASED:
                if (keyCode == KeyEvent.VK_ALT) {
                    // bad workaround... on Windows JDK 1.4, some
                    // Alt-sequences generate random crap afterwards
                    if (OperatingSystem.isWindows()
                            && OperatingSystem.hasJava14())
                        last = LAST_MOD;
                }
                return evt;
                //}}}
            default:
                return evt;
        }
    } //}}}

    //{{{ numericKeypadKey() method
    /**
     * A workaround for non-working NumLock status in some Java versions.
     * @since jEdit 4.0pre8
     */
    public static void numericKeypadKey() {
        last = LAST_NOTHING;
    } //}}}

    //{{{ Private members

    //{{{ Static variables
    private static long lastKeyTime;

    private static int last;
    private static final int LAST_NOTHING = 0;
    private static final int LAST_ALT = 1;
    private static final int LAST_BROKEN = 2;
    private static final int LAST_NUMKEYPAD = 3;
    private static final int LAST_MOD = 4;
    //}}}

    //{{{ handleBrokenKeys() method
    private static void handleBrokenKeys(KeyEvent evt, int keyCode) {
        if (evt.isAltDown() && evt.isControlDown()
                && !evt.isMetaDown()) {
            last = LAST_NOTHING;
            return;
        } else if (!(evt.isAltDown() || evt.isControlDown() || evt.isMetaDown())) {
            last = LAST_NOTHING;
            return;
        }

        if (evt.isAltDown())
            last = LAST_ALT;

        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_ENTER:
                last = LAST_NOTHING;
                break;
            default:
                if (keyCode < KeyEvent.VK_A || keyCode > KeyEvent.VK_Z)
                    last = LAST_BROKEN;
                else
                    last = LAST_NOTHING;
                break;
        }

        lastKeyTime = System.currentTimeMillis();
    }
}
