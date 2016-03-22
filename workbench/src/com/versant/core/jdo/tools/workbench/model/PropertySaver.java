
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
package com.versant.core.jdo.tools.workbench.model;

import java.io.*;
import java.util.*;

/**
 * Writes properties and comments in java.util.Properties format to a
 * stream. This can also replace values that are Ant filter tokens with
 * real data from a Properties instance.
 *
 * @see Properties
 */
public class PropertySaver {

    public static final int PROP_FILL_MODE_MIN = 0;
    public static final int PROP_FILL_MODE_KEEP = 1;
    public static final int PROP_FILL_MODE_VERBOSE = 2;

    private Properties filterTokens;
    private int mode = PROP_FILL_MODE_KEEP;

    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String specialSaveChars = "=: \t\r\n\f#!";
    private static final String whiteSpaceChars = " \t\r\n\f";

    private ArrayList lines = new ArrayList();
    private HashMap values = new HashMap();

    public PropertySaver() {
    }

    public Properties getFilterTokens() {
        return filterTokens;
    }

    public void setFilterTokens(Properties filterTokens) {
        this.filterTokens = filterTokens;
    }

    private String filter(String value) {
        if (filterTokens != null && value.startsWith("@") && value.endsWith(
                "@")) {
            String rep = filterTokens.getProperty(
                    value.substring(1, value.length() - 2));
            if (rep != null) value = rep;
        }
        return value;
    }

    /**
     * Write the property to the file if value is not null.
     */
    public void add(String property, String value, String defaultValue) {
        if (value == null) {
            value = defaultValue;
        }
        if (mode != PROP_FILL_MODE_VERBOSE && defaultValue != null && defaultValue.equals(
                value)) {
            if (mode == PROP_FILL_MODE_MIN) {
                removeProperty(property);
                return;
            }
            if (mode == PROP_FILL_MODE_KEEP && !values.containsKey(property)) {
                return;
            }
        }
        add(property, value);
    }

    private void removeProperty(String property) {
        values.remove(property);
        for (int i = 0; i < lines.size(); i++) {
            PropLine propLine = (PropLine)lines.get(i);
            if (propLine.key != null && propLine.key.equals(property)) {
                removeLine(propLine);
                i--;
            }
        }
    }

    private int removeLine(PropLine propLine) {
        int c = 0;
        while (propLine != null) {
            lines.remove(propLine);
            c++;
            propLine = propLine.nextPropLine;
        }
        return c;
    }

    public void removePropertyWild(String property) {
        values.remove(property);
        for (Iterator it = values.keySet().iterator(); it.hasNext();) {
            String s = (String)it.next();
            if (s != null && s.startsWith(property)) {
                it.remove();
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            PropLine propLine = (PropLine)lines.get(i);
            if (propLine.line != null && propLine.line.startsWith(property)) {
                removeLine(propLine);
                i--;
                if (propLine.key != null) {
                    values.remove(propLine.key);
                }
            }
        }
    }

    /**
     * Write the property to the file if value is not null.
     */
    public void add(String property, String value, int defaultValue) {
        String def = Integer.toString(defaultValue);
        add(property, value, def);
    }

    /**
     * Write the property to the file if value is not null.
     */
    public void add(String property, String value, boolean defaultValue) {
        add(property, value, defaultValue ? "true" : "false");
    }

    /**
     * Write the property to the file if value is not null.
     */
    public void add(String property, String value) {
        if (mode != PROP_FILL_MODE_VERBOSE && value == null) {
            if (mode == PROP_FILL_MODE_MIN) {
                removeProperty(property);
                return;
            }
            if (mode == PROP_FILL_MODE_KEEP && !values.containsKey(property)) {
                return;
            }
        }
        setProperty(property, filter(value));
    }

    /**
     * Write the property to the file.
     */
    public void add(String property, boolean value, boolean defaultValue) {
        if (value == defaultValue) {
            if (mode == PROP_FILL_MODE_MIN) {
                removeProperty(property);
                return;
            }
            if (mode == PROP_FILL_MODE_KEEP && !values.containsKey(property)) {
                return;
            }
        }
        add(property, value ? "true" : "false");
    }

    /**
     * Write the property to the file.
     */
    public void add(String property, int value) {
        add(property, Integer.toString(value));
    }

    /**
     * Write a class name (may be null) and a Map of properties to the file.
     */
    public void add(String property, String cname, Map props, Map defaults) {
        Map temp = new HashMap(defaults.size());
        if (defaults != null) {
            temp.putAll(defaults);
        }
        temp.putAll(props);
        props = temp;
        add(property, cname);
        if (!props.isEmpty()) {
            for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry)i.next();
                String key = (String)e.getKey();
                add(property + "." + key,
                        (String)e.getValue(), (String)defaults.get(key));
            }
        }
    }

    /**
     * Write a comment to the file.
     */
    public void println(String s) {
        s = "# " + s;
        for (Iterator it = lines.iterator(); it.hasNext();) {
            PropLine propLine = (PropLine)it.next();
            if (propLine.line != null && propLine.line.equals(s)) {
                return;
            }
        }
        addLine(s);
    }

    private PropLine addLine(String s) {
        PropLine line = new PropLine();
        line.line = s;
        lines.add(line);
        return line;
    }

    /**
     * Write a blank line.
     */
    public void println() {
        addLine("");
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public synchronized void load(InputStream inStream) throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(inStream, "8859_1"));
        try {
            lines.clear();
            while (true) {
                // Get next line
                String line = in.readLine();
                if (line == null) {
                    return;
                }

                PropLine propLine = addLine(line);

                if (line.length() > 0) {

                    // Find start of key
                    int len = line.length();
                    int keyStart;
                    for (keyStart = 0; keyStart < len; keyStart++) {
                        if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1) {
                            break;
                        }
                    }

                    // Blank lines are ignored
                    if (keyStart == len) {
                        continue;
                    }

                    // Continue lines that end in slashes if they are not comments
                    char firstChar = line.charAt(keyStart);
                    if ((firstChar != '#') && (firstChar != '!')) {
                        PropLine prevPropLine = propLine;
                        while (continueLine(line)) {
                            String nextLine = in.readLine();
                            PropLine nextPropLine = addLine(nextLine);
                            prevPropLine.setNextLine(nextPropLine);
                            prevPropLine = nextPropLine;
                            if (nextLine == null) {
                                nextLine = "";
                            }
                            String loppedLine = line.substring(0, len - 1);
                            // Advance beyond whitespace on new line
                            int startIndex;
                            for (startIndex = 0;
                                 startIndex < nextLine.length(); startIndex++) {
                                if (whiteSpaceChars.indexOf(
                                        nextLine.charAt(startIndex)) == -1) {
                                    break;
                                }
                            }
                            nextLine = nextLine.substring(startIndex,
                                    nextLine.length());
                            line = new String(loppedLine + nextLine);
                            len = line.length();
                        }

                        // Find separation between key and value
                        int separatorIndex;
                        for (separatorIndex = keyStart;
                             separatorIndex < len; separatorIndex++) {
                            char currentChar = line.charAt(separatorIndex);
                            if (currentChar == '\\') {
                                separatorIndex++;
                            } else if (keyValueSeparators.indexOf(currentChar) != -1) {
                                break;
                            }
                        }
                        String key = line.substring(keyStart, separatorIndex);
                        values.put(key, null);
                        propLine.key = loadConvert(key);
                    }
                }
            }
        } finally {
            in.close();
        }
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx
     * and writes out any of the characters in specialSaveChars
     * with a preceding slash
     */
    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }

                    outBuffer.append(' ');
                    break;
                case '\\':
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        if (specialSaveChars.indexOf(aChar) != -1) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public synchronized void setProperty(String key, String value) {
        if (key != null) {
            if (value == null) {
                value = "";
            }
            if (!values.containsKey(key)) {
                PropLine propLine = new PropLine();
                lines.add(propLine);
                propLine.key = key;
            }
            values.put(key, value);
        }
    }

    public synchronized void store(OutputStream out) throws IOException {
        BufferedWriter awriter = new BufferedWriter(
                new OutputStreamWriter(out, "8859_1"));
        try {
            boolean pws = false;
            for (Iterator it = lines.iterator(); it.hasNext();) {
                PropLine propLine = (PropLine)it.next();
                String str = propLine.toString();
                if (MdUtils.isStringNotEmpty(str)) {
                    if (pws) {
                        awriter.newLine();
                        pws = false;
                    }
                    awriter.write(str);
                    awriter.newLine();
                } else {
                    pws = true;
                }
            }
            awriter.flush();
        } finally {
            awriter.close();
        }
    }

    private class PropLine {

        private String line;
        private String key;
        private PropLine nextPropLine;

        public String toString() {
            if (key != null) {
                String value = (String)values.get(key);
                if (value != null) {
                    return saveConvert(key, true) + "=" + saveConvert(value,
                            false);
                } else {
                    if (mode == PROP_FILL_MODE_VERBOSE) {
                        return saveConvert(key, true) + "=";
                    }
                }
            }
            return line;
        }

        public void setNextLine(PropLine nextPropLine) {
            this.nextPropLine = nextPropLine;
        }
    }
}

