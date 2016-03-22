
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
package com.versant.core.jdo.tools.workbench.ant;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOException;
import java.io.File;
import java.util.ArrayList;

import com.versant.core.common.JaxpUtils;

/**
 * Ant build file parser. Currently this just extracts all the targets with
 * descriptions. If any target contains jdo-enhance it is flagged as a best
 * guess for the compile and enhance target. Targets etc are left in
 * properties after parsing completes.
 * @keep-all
 */
public class AntBuildParser extends DefaultHandler {

    private SAXParserFactory parserFactory;
    private SAXParser parser;
    private ArrayList targetList;
    private String lastTargetName;
    private String enhanceTargetName;

    public AntBuildParser() {
        parserFactory = JaxpUtils.getSAXParserFactory();
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(false);
    }

    /**
     * Parse the build file from in.
     * @exception JDOFatalUserException if invalid
     */
    public void parse(File in) throws JDOFatalUserException {
        targetList = new ArrayList();
        enhanceTargetName = null;
        parser = JaxpUtils.createSAXParser(parserFactory);
        try {
            parser.parse(in, this);
        } catch (JDOException e) {
            throw e;
        } catch (Exception e) {
            throw new JDOFatalUserException(e.toString(), e);
        }
        parser = null;
    }

    /**
     * Get all the targets found.
     * @see Target
     */
    public ArrayList getTargetList() {
        return targetList;
    }

    /**
     * Get the name of the first target found containing a jdo-enhance
     * call or null if none.
     */
    public String getEnhanceTargetName() {
        return enhanceTargetName;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("target".equals(qName)) {
            targetList.add(new Target(attributes.getValue("name"),
                    attributes.getValue("description")));
            lastTargetName = attributes.getValue("name");
        } else if ("jdo-enhance".equals(qName)) {
            if (enhanceTargetName == null) {
                enhanceTargetName = lastTargetName;
            }
        }
    }

    public void error(SAXParseException e) throws SAXException {
        throw new JDOFatalUserException(e.getMessage(), e);
    }

    public String getPossibleEnhanceTarget(){
        return enhanceTargetName;
    }
}

