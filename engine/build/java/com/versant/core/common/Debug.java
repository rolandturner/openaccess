
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
package com.versant.core.common;

import java.io.PrintStream;

/**
 * Master debugging output flags. This class must be final so that the
 * debugging code is stripped by javac/jikes at compile time if the flag is
 * false. This is copied to a java file with token replacement and compiled
 * by the Ant build.
 */
public final class Debug {

    public Debug() {}

    public static final String BUILD_DATE = "1134511200000";
    public static final String VERSION = "4.0.2";

    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;

    public static final boolean DEBUG = false;

    public static final void assertRuntime(boolean _assert, String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().runtime(message);
        }
    }

    public static final void assertFatal(boolean _assert, String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().fatal(message);
        }
    }

    public static final void assertInternal(boolean _assert, String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().internal(message);
        }
    }

    public static final void assertSecurity(boolean _assert, String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().security(message);
        }
    }

    public static final void assertIllegalArgument(boolean _assert,
                                                   String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().illegalArgument(message);
        }
    }

    public static final void assertIndexOutOfBounds(boolean _assert,
                                                    String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().indexOutOfBounds(message);
        }
    }

    public static final void assertInvalidOperation(boolean _assert,
                                                    String message) {
        if (!_assert) {
            throw BindingSupportImpl.getInstance().invalidOperation(message);
        }
    }
}
