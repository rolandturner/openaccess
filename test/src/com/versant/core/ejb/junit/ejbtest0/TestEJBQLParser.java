
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
package com.versant.core.ejb.junit.ejbtest0;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.versant.core.ejb.query.EJBQLParser;
import com.versant.core.ejb.query.Node;

/**
 * Tests for the JavaCC EJBQL parser. These do not require a domain model or
 * database or anything like that. All the queries in sample_ejbql.txt are
 * run.
 */
public class TestEJBQLParser extends TestCase {
    private static final String SAMPLE_EJBQL = "sample_ejbql.txt";

    private String query;

    public static Test suite() throws IOException {
        TestSuite s = new TestSuite("TestEJBQLParser");
        Map m = readQueries();
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            String name = (String) e.getKey();
            String query = (String) e.getValue();
            s.addTest(new TestEJBQLParser("testParse " + name, query));
        }
        return s;
    }

    public TestEJBQLParser(String name, String query) {
        super(name);
        this.query = query;
    }

    protected void runTest() throws Throwable {
        System.out.println(query);
        System.out.println();
        StringReader in = new StringReader(query);
        EJBQLParser p = new EJBQLParser(in);
        Node n = p.ejbqlQuery();
        System.out.println(n);
    }

    /**
     * Return a map containing all queries from sample_ejbql.txt sorted by
     * name.
     */
    public static SortedMap readQueries() throws IOException {
        SortedMap map = new TreeMap();
        InputStream in = TestEJBQLParser.class.getResourceAsStream(
                SAMPLE_EJBQL);
        if (in == null) {
            throw new IllegalStateException(SAMPLE_EJBQL + " resource not found");
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        for (; ;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }
            int i = line.indexOf(':');
            if (i < 0) {
                throw new IllegalStateException("Expected query name: '" +
                        line + "'");
            }
            String name = line.substring(0, i);
            line = line.substring(i + 1).trim();
            StringBuffer q = new StringBuffer();
            q.append(line);
            for (; ;) {
                line = r.readLine();
                if (line == null || line.trim().length() == 0) {
                    break;
                }
                if (!line.startsWith("#")) {
                    q.append('\n');
                    q.append(line);
                }
            }
            map.put(name, q.toString());
        }
        return map;
    }

}


