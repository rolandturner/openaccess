
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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import com.versant.core.common.BindingSupportImpl;

/** 
 * Utility methods to get hold of XML parsers.
 */
public class JaxpUtils {

    private static final String SAX_PARSER_FACTORY_SYSPROP = "versant.SAXParserFactory";
    private static final String SAX_PARSER_FACTORY_SYSPROP_OLD = "jdogenie.SAXParserFactory";

    /**
     * Get a SAXParserFactory.
     */
    public static SAXParserFactory getSAXParserFactory() {
        String parserFactoryName = System.getProperty(SAX_PARSER_FACTORY_SYSPROP);
        if (parserFactoryName == null) {
            parserFactoryName = System.getProperty(SAX_PARSER_FACTORY_SYSPROP_OLD);
        }
        SAXParserFactory parserFactory = null;
        if (parserFactoryName != null) {
            try {
                Class cls = Class.forName(parserFactoryName);
                parserFactory = (SAXParserFactory)cls.newInstance();
            } catch (Throwable t) {
                throw BindingSupportImpl.getInstance().runtime("Unable to create SAXParserFactory '" +
                    parserFactoryName + "': " + t, t);
            }
            System.out.println("Versant Open Access: Using " + parserFactory);
        } else {
            parserFactory = SAXParserFactory.newInstance();
        }
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(false);
        return parserFactory;
    }

    /**
     * Create a SAXParser from a factory. Throws a JDOFatalUserException if
     * this fails for some reason.
     */
    public static SAXParser createSAXParser(SAXParserFactory parserFactory) {
        try {
            return parserFactory.newSAXParser();
        } catch (Throwable t) {
            throw BindingSupportImpl.getInstance().runtime(
                "Unable to create SAXParser from factory: " + parserFactory + "\n" +
                "Versant Open Access requires a JAXP 1.1 compliant parser. If this is not possible\n" +
                "in your environment (e.g. WebLogic 6.0) try invoking java with\n" +
                "-D" + SAX_PARSER_FACTORY_SYSPROP + "=<name of SAXParserFactory> to bypass JAXP\n" +
                t, t);
        }
    }

}
