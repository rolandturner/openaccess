
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
package com.versant.core.vds.tools;

import com.versant.core.common.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Interprets command input from user.
 *
 * @author ppoddar
 * @since 1.0
 */
public class CommandHandler {

    String[] _params;
    BufferedReader _stream;
    private String _input;
    private String _prompt;
    private static final String PROMPT = "Versant> ";
    private Map _commandMap = new HashMap();

    public CommandHandler() {
        this(PROMPT);
    }

    public CommandHandler(String prompt) {
        _prompt = prompt;
        _stream = new BufferedReader(new InputStreamReader(System.in));
    }

    public void setPrompt(String s) {
        if (s != null) _prompt = s;
    }

    public void setCommand(String command, String help) {
        _commandMap.put(command.toLowerCase(), help);
    }

    public String getInputLine() {
        return _input;
    }

    public String getCommand() {
        String command = "";
        System.out.print("\n" + _prompt);
        System.out.flush();
        try {
            _input = _stream.readLine();
            String[] args = tokenize(_input);
            if (args == null || args.length < 1) {
                return getCommand();
            }
            command = args[0];
            _params = new String[args.length - 1];
            System.arraycopy(args, 1, _params, 0, args.length - 1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return getCommand();
        }
        if (!isValid(command)) {
            System.out.println(command + " is not valid");
            printHelp();
            return getCommand();
        }
        return command;
    }

    void printHelp() {
        Iterator commands = _commandMap.keySet().iterator();
        while (commands.hasNext()) {
            String command = (String)commands.next();
            String help = (String)_commandMap.get(command);
            System.err.println(command + help);
        }
    }

    void help(String s) {
        if (_commandMap.containsKey(s)) {
            System.err.println(s + _commandMap.get(s));
        } else {
            System.err.println("No help available on " + s);
        }
    }

    public boolean isValid(String command) {
        //if (!_isValidating) return true;
        return _commandMap.containsKey(command.toLowerCase());
    }

    public String[] getParameters() {
        return _params;
    }

    public String getParameter(int i) {
//        assert i < _params.length;
        if (Debug.DEBUG) {
            Debug.assertIndexOutOfBounds(i < _params.length,
                    "i >= _params.length");
        }
        if (i > _params.length) return "";
        return _params[i];
    }

    public String ask(String q) throws Exception {
        System.out.print(q);
        System.out.flush();
        return _stream.readLine();
    }

    private String removeBackslash(String s) {
        if (s == null) return null;
        StringBuffer result = new StringBuffer(s.length());
        int index = s.indexOf('\\');
        if (index != -1) {
            result.append(s.substring(0, index)).append(
                    removeBackslash(s.substring(index + 1)));
            return result.toString();
        } else {
            return s;
        }
    }

    private String[] tokenize(String s) {
        ArrayList args = new ArrayList();
        String quote = "\"";
        String quoteWithBackslash = "\\\"";
        String space = " ";
        StringTokenizer tokenizer = new StringTokenizer(s);
        while (tokenizer.hasMoreTokens()) {
            String temp = new String(tokenizer.nextToken());
            if (temp.startsWith(quote)) {
                temp = temp.substring(1);
                while (true) {
                    try {
                        String temp2 = tokenizer.nextToken();
                        temp = temp + space + temp2;
                        if (temp2.endsWith(quote) && !temp2.endsWith(
                                quoteWithBackslash)) {
                            temp = temp.substring(0, temp.length() - 1);
                            break;
                        }
                    } catch (NoSuchElementException exp) {
                        break;
                    }
                }
                temp = removeBackslash(temp);
            }
            args.add(temp.toString());
        }
        return (String[])args.toArray(new String[0]);
    }

    public int countParams() {
        return (_params != null) ? _params.length : 0;
    }

    public static void main(String[] args) {
        CommandHandler clp = new CommandHandler();
        String aCommand = clp.getCommand();
        while (!aCommand.equalsIgnoreCase("EXIT")) {
            String[] params = clp.getParameters();
            System.out.println(
                    "Command is: [" + aCommand + "] with " + params.length + " tokens");
            for (int i = 0; i < params.length; i++) {
                System.err.println("Token: [" + params[i] + "]");
            }
            aCommand = clp.getCommand();
        }
    }
}
