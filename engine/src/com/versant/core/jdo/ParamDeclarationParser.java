
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
package com.versant.core.jdo;

import com.versant.core.util.CharBuf;

import com.versant.core.common.BindingSupportImpl;

/**
 * This parses a comma separated list of query parameters into types and names.
 * TODO Make this parse array types e.g. String[]
 */
public class ParamDeclarationParser {

    /**
     * Callback interface for parsing results.
     */
    public static interface Handler {

        /**
         * The parser has sucessfully parsed a parameter type and name at
         * position index in the parameter list (first param is 0).
         */
        public void parameterParsed(int index, String type, String name);
    }

    private static final int STATE_START = 0;
    private static final int STATE_LOOK_FOR_TYPE = 1;
    private static final int STATE_IN_TYPE = 2;
    private static final int STATE_LOOK_FOR_NAME = 3;
    private static final int STATE_IN_NAME = 4;
    private static final int STATE_LOOK_FOR_COMMA = 5;

    /**
     * Parse params into types and names and return the number of parameters
     * parsed.
     */
    public static int parse(String params, Handler handler) {
        if (params == null) return 0;
        int len = params.length();
        if (len == 0) return 0;
        int state = STATE_START;
        String type = null;
        int count = 0;
        CharBuf buf = new CharBuf();
        for (int pos = 0; ; pos++) {
            if (pos < len) {
                char c = params.charAt(pos);
                switch (state) {
                    case STATE_START:
                    case STATE_LOOK_FOR_TYPE:
                        if (isIdStart(c)) {
                            buf.append(c);
                            state = STATE_IN_TYPE;
                        } else if (!isWs(c)) {
                            error(pos, "parameter type", c);
                        }
                        break;
                    case STATE_IN_TYPE:
                        if (isId(c) || c == '.') {
                            buf.append(c);
                        } else if (isWs(c)) {
                            type = buf.toString();
                            buf.clear();
                            state = STATE_LOOK_FOR_NAME;
                        } else {
                            error(pos, "parameter type", c);
                        }
                        break;
                    case STATE_LOOK_FOR_NAME:
                        if (isIdStart(c)) {
                            buf.append(c);
                            state = STATE_IN_NAME;
                        } else if (!isWs(c)) {
                            error(pos, "parameter name", c);
                        }
                        break;
                    case STATE_IN_NAME:
                        if (isId(c)) {
                            buf.append(c);
                        } else if (c == ',' || isWs(c)) {
                            handler.parameterParsed(count++, type, buf.toString());
                            buf.clear();
                            type = null;
                            if (c == ',') state = STATE_LOOK_FOR_TYPE;
                            else state = STATE_LOOK_FOR_COMMA;
                        } else {
                            error(pos, "parameter name", c);
                        }
                        break;
                    case STATE_LOOK_FOR_COMMA:
                        if (c == ',') {
                            state = STATE_LOOK_FOR_TYPE;
                        } else if (!isWs(c)) {
                            error(pos, "comma", c);
                        }
                        break;
                }
            } else {
                switch (state) {
                    case STATE_LOOK_FOR_TYPE:
                        error(pos, "parameter type", (char)-1);
                    case STATE_IN_TYPE:
                    case STATE_LOOK_FOR_NAME:
                        error(pos, "parameter name", (char)-1);
                    case STATE_IN_NAME:
                        handler.parameterParsed(count++, type, buf.toString());
                }
                break;
            }
        }
        return count;
    }

    private static boolean isWs(char c) {
        return c == ' ' || c == '\n' || c == '\t';
    }

    private static boolean isIdStart(char c) {
        return c == '_'
            || (c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z');
    }

    private static boolean isId(char c) {
        return c == '_'
            || (c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9');
    }

    private static void error(int pos, String msg, char c) {
        throw BindingSupportImpl.getInstance().runtime("Invalid parameter declaration: Expected " +
            msg + ", got '" + c + "' at position " + pos);
    }

}

