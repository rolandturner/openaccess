
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
/*
 * Created on Feb 21, 2004
 *
 * Copyright Versant Corporation 2003-2005, All rights reserved
 */
package com.versant.core.vds.tools;

import com.versant.core.common.Debug;

/**
 * Flag denotes a command-line option with all its specifcs such as
 * whether it is mandatory, case-sensitive etc. A flag may have more
 * than one alises.
 *
 * @author ppoddar
 */
public class Flag {

// possible ways a flag can be specified. Each must begin with a '-' symbol
    String[] _monikers;

    boolean _isOptional;
    int _valueConstraint;
    boolean _isCaseSensitive;
    String[] _allowedValues;
    String _defaultValue;
    String _helpMessage;

    private String _value;
    private boolean _isSet;

    public static final int VALUE_MUST = 0;
    public static final int VALUE_OPTIONAL = 1;
    public static final int VALUE_NEVER = 2;

    /**
     * Specifies a flag
     *
     * @param monikers      the multiple ways to specify the flag. Each string must
     *                      start with <code>-</code> sign.
     * @param isOptional    denotes if this flag must be present in the command
     * @param valueSpec     denotes constrains on the value of the flag. It must
     *                      be one of <code>VALUE_MUST</code>, <code>VALUE_OPTIONAL</code> or
     *                      <code>VALUE_NEVER</code>.
     * @param allowedValues one or more known values that this flag may take.
     *                      null denotes that the flag may take any value.
     * @param defualtValue  is the value of this flag when no value is specified
     *                      and <code>valueSpec</code> is set to <code>VALUE_OPTIONAL</code>
     * @param helpMessage   is the description of this flag for user information.
     */
    public Flag(final String[] monikers,
            boolean isOptional,
            int valueSpec,
            boolean isCaseSensitive,
            final String[] allowedValues,
            final String defaultValue,
            final String helpMessage) {
        _monikers = monikers;
        if (Debug.DEBUG) {
//            assert (_monikers != null)  : "null alias for flag is not allowed";
            Debug.assertInternal(_monikers != null,
                    "null alias for flag is not allowed");
//            assert (_monikers.length > 0) : "empty alias for flag is not allowed";
            Debug.assertInternal(_monikers.length > 0,
                    "empty alias for flag is not allowed");
            for (int i = 0; i < _monikers.length; i++) {
//                assert _monikers[i] != null : "null alias for flag is not allowed";
                Debug.assertInternal(_monikers[i] != null,
                        "null alias for flag is not allowed");
//                assert Flag.isFlag(_monikers[i]) :"alias [" + _monikers[i] + "] must start with a - sign";
                Debug.assertInternal(Flag.isFlag(_monikers[i]),
                        "alias [" + _monikers[i] + "] must start with a - sign");
            }
        }
        _isOptional = isOptional;
        if (valueSpec != VALUE_MUST
                && valueSpec != VALUE_OPTIONAL
                && valueSpec != VALUE_NEVER) {
            throw new IllegalArgumentException(
                    "Flag has unrecognized value constraint " + valueSpec);
        }
        _valueConstraint = valueSpec;

        _isCaseSensitive = isCaseSensitive;
        _allowedValues = allowedValues;
        _defaultValue = defaultValue;
        _helpMessage = helpMessage;
    }

    /**
     * Gets the constaint on this receiver's value.
     *
     * @return one of <code>VALUE_MUST</code>, <code>VALUE_OPTIONAL</code> or
     *         <code>VALUE_NEVER</code>.
     */
    int getValueConstraint() {
        return _valueConstraint;
    }

    /**
     * Sets value of this receiver.
     *
     * @param value is stringfied value of this receiver.
     * @throws IllegalArgumentException is no value can be set for this receiver
     *                                  or the given value is not allowed.
     */
    void setValue(String value) {
        if (value == null) return;
        if (_valueConstraint == VALUE_NEVER) {
            throw new IllegalArgumentException(
                    this + " must not have a value. Attempt to set [" + value + "]");
        }
        if (!isValueAllowed(value)) {
            throw new IllegalArgumentException(
                    "[" + value + "] is not a valid for " + this);
        }

        _value = value;
        _isSet = true;
    }

    /**
     * Answers in affirmative if the given value is allowed for tis receiver.
     * Construct this receiver with <code>null</code> for allowed values to
     * accept any value.
     *
     * @param value
     * @return
     */
    private boolean isValueAllowed(String value) {
        if (_allowedValues == null) return true;
        for (int i = 0; i < _allowedValues.length; i++) {
            if (_allowedValues[i].equals(value)) return true;
        }
        return false;

    }

    /**
     * Answers in affirmative if this receiver can be optional.
     *
     * @return true if this receiver is optional.
     */
    public boolean isOptional() {
        return _isOptional;
    }

    /**
     * Answers in affirmative if value of this receiver has been set.
     *
     * @return true if value of this receiver is set.
     */
    public boolean isSet() {
        return _isSet;
    }

    /**
     * Unsets the value of this receiver.
     */
    void clear() {
        _isSet = false;
        _value = null;
    }

    /**
     * Sets the value of this receiver to its default value.
     */
    void setDefault() {
        setValue(_defaultValue);
    }

    /**
     * Answers in affirmative if the given string can be a valid flag. The check
     * is that the given string is non-null and starts with '-' symbol.
     *
     * @param s
     * @return true if given string denotes a valid flag alias.
     */
    static boolean isFlag(String s) {
        return (s != null && s.startsWith("-"));
    }

    /**
     * Returns true if given argument matches any of the aliases of this receiver.
     *
     * @param arg
     * @return
     */
    public boolean matches(String arg) {
        for (int i = 0; i < _monikers.length; i++) {
            if (_isCaseSensitive) {
                if (_monikers[i].equals(arg)) return true;
            } else {
                if (_monikers[i].equalsIgnoreCase(arg)) return true;
            }
        }
        return false;
    }

    /**
     * Gets the value of this reciver.
     *
     * @return
     */
    public String getValue() {
        switch (_valueConstraint) {
            case VALUE_NEVER:
                throw new IllegalArgumentException(this + " never has a value");
            case VALUE_MUST:
            case VALUE_OPTIONAL:
                if (_isSet) return _value;
                return _defaultValue;
        }
        return null;
    }

    public String toString() {
        return _monikers[0];
    }

    /**
     * Prints the details of this receiver.
     */
    void print(java.io.PrintStream out) {
        out.print(_isOptional ? "    " : "[*] ");
        for (int i = 0; i < _monikers.length; i++) {
            out.print(_monikers[i]);
            out.print((i == (_monikers.length - 1)) ? " " : "|");
        }
        out.print(_helpMessage);
        if (_allowedValues != null) {
            out.print(". Allowed values are ");
            for (int i = 0; i < _allowedValues.length; i++) {
                out.print("\"" + _allowedValues[i] + "\"");
                out.print((i == (_allowedValues.length - 1)) ? " " : " or ");
            }
        }
        if (_defaultValue != null) {
            out.print(". Defaults to " + "\"" + _defaultValue + "\"");
        }
        out.print("\r\n");
    }
}
