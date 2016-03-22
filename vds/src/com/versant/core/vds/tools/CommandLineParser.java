
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

import java.util.ArrayList;
import java.util.List;

/**
 * CommandlineParser parses a set of command-line arguments.
 *
 * @author ppoddar
 */
public class CommandLineParser {

    List _flags;
    List _params; 				// ordered list of parameters
    boolean _isIgnoreCase; 		// Flags if flags are case-sensitive
    int _minimumParamaterCount; // Limits minimum number of parameters
    String _paramInfo;			// Describes parameters
    String _usageCommand; 		// Describe usage information

    /**
     * 
     */
    public CommandLineParser() {
        super();
        _flags = new ArrayList();
        _params = new ArrayList();
        _isIgnoreCase = true;
        _minimumParamaterCount = 0;
        _paramInfo = "";
        _usageCommand = "";
    }

    /**
     * Parses the arguments for flags, their values (if any) and parameters.
     * Parsing uses current setting of the mandatory/optional flags.
     *
     * @param args list of string arguments. They are assumed to be in
     *             the prescribed format.
     */
    public void parse(String args[]) throws IllegalArgumentException {
        if (args == null || args.length == 0) {
            usage();
            System.exit(0);
        }
        clearFlags();

        _params.clear();

        int i = 0;
        int nOption = args.length;// - _minimumParamaterCount;
        for (; i < nOption; i++) {
            String arg = args[i];
            if (isCaseSensitive()) arg = arg.toLowerCase();
            Flag flag = getFlag(arg);
            if (flag == null) { // possibly a parameter
                if (Flag.isFlag(arg)) { // a wrong flag
                    parseError("Invalid " + arg);
                } else { // parameters have started discontinue flag parsing
                    break;
                }
            }
            switch (flag.getValueConstraint()) {
                case Flag.VALUE_MUST: // Next argument is the flag's value
                    if (i == (nOption - 1)) {// no more arguments to consume
                        parseError(flag + " must hava a value");
                    } else {
                        flag.setValue(args[++i]); // Consume next argument
                    }
                    break;

                case Flag.VALUE_NEVER: // does not have a value
                    flag.setDefault();
                    break;

                case Flag.VALUE_OPTIONAL: // may have a value
                    if ((i < (nOption - 1)) && !Flag.isFlag(args[i + 1])) { // next value exists and is not a flag
                        flag.setValue(args[++i]); // consume the value
                    } else {
                        flag.setDefault(); // use default value for this flag
                    }
                    break;

                default:
//                    assert false : "Unrecognized constraint of value " + flag.getValueConstraint() + " of " + flag;
                    if (Debug.DEBUG) {
                        Debug.assertInternal(false,
                                "Unrecognized constraint of value " +
                                flag.getValueConstraint() + " of " + flag);
                    }
            }
        }
        for (i = 0; i < args.length; i++) {
            _params.add(args[i]);
        }
        if (getFlag("-help").isSet()) {
            usage();
            System.exit(0);
        }
        validate();
    }

    void parseError(String s) {
        System.err.println("ERROR: " + s);
        usage();
        System.exit(0);
    }

    /**
     * Validates the parsed results.
     * All specified flags must be valid i.e. they must be either mandatory or optonal flag.
     * All mandatory flags must be specified.
     * Number of parameters must meet minimum number of parameters.
     */
    void validate() {
        for (int i = 0; i < _flags.size(); i++) {
            Flag flag = (Flag)_flags.get(i);
            if (!flag.isOptional() && !flag.isSet()) {
                parseError(flag + " is not specified");
            }
        }
        if (_params.size() < _minimumParamaterCount) {
            parseError(
                    "Found " + _params.size() + " parameters expecetd at least " + _minimumParamaterCount);
        }
    }

    /**
     * Checks if the flag is specified.
     *
     * @param flag
     * @return true if flag is valid for this reciver and specified in the
     *         command-line argument.
     */
    public boolean isFlagPresent(String arg) {
        Flag flag = getFlag(arg);

        return flag != null && flag.isSet();
    }

    /**
     * Gets current case-sensitive status.
     *
     * @return true if aliases of this reciver is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return !_isIgnoreCase;
    }

    /**
     * Sets case-sensitive status.
     *
     * @param value
     */
    public void setIgnoreCase(boolean value) {
        _isIgnoreCase = value;
    }

    /**
     * Gets the flag given any one of its aliases.
     *
     * @param arg any of the aliases of a flag.
     * @return a flag whose one of the aliases matches the given string. null
     *         if no such flag is specifed.
     */
    public Flag getFlag(String arg) {
        for (int i = 0; i < _flags.size(); i++) {
            Flag flag = (Flag)_flags.get(i);
            if (flag.matches(arg)) {
                return flag;
            }
        }
        return null;
    }

    void clearFlags() {
        for (int i = 0; i < _flags.size(); i++) {
            Flag flag = (Flag)_flags.get(i);
            flag.clear();
        }
    }

    public int getParameterCount() {
        return (_params != null) ? _params.size() : 0;
    }

    public List getParameters() {
        return _params;
    }

    public String getParameter(int index) {
        return (String)_params.get(index);
    }

    /**
     * Sets minimum number of parameters.
     *
     * @param n
     */
    public void setMinimumParamaterCount(int n) {
        _minimumParamaterCount = n;
    }

    public void setFlags(Flag[] flags) {
        for (int i = 0; i < flags.length; i++) addFlag(flags[i]);
    }

    public void addFlag(Flag f) {
        String[] aliases = f._monikers;
        for (int i = 0; i < aliases.length; i++) {
            if (getFlag(aliases[i]) != null) {
                throw new RuntimeException(aliases[i] + " is already a flag");
            }
        }
        _flags.add(f);
    }

    /**
     * Sets explanatory information about parameters to print usage information.
     *
     * @param n
     */
    public void setParameterInfo(String s) {
        _paramInfo = s;
    }

    public void setUsageCommand(String s) {
        _usageCommand = s;
    }

    public void usage() {
        if (_flags.size() > 0) System.out.println(_usageCommand);
        if (!_flags.isEmpty()) {
            System.out.println(
                    " where options are: (* denotes a mandatory option)");
        }
        for (int i = 0; i < _flags.size(); i++) {
            Flag flag = (Flag)_flags.get(i);
            flag.print(System.out);
        }
        if (_paramInfo != null && _paramInfo.length() > 1) {
            System.out.print("\r\n");
            System.out.print(_paramInfo);
        }
    }
}
