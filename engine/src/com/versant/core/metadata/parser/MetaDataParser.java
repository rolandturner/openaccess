
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
package com.versant.core.metadata.parser;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;


import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.versant.core.common.JaxpUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 

 

import com.versant.core.metadata.MDStatics;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;

/**
 * This produces a tree of JdoXXX objects representing the structure of a
 * JDO meta data file (.jdo). All of our vendor extensions are valdidated
 * (correct keys) as part of this process.
 */
public class MetaDataParser extends DefaultHandler
{

    public static final String VENDOR_NAME = "versant";
    public static final String VENDOR_NAME_JDOGENIE = "jdogenie";


    private SAXParserFactory parserFactory;
    private SAXParser parser;

    private int state;
    private int extState;
    private int extSkipState;
    private String elementName;
    //private int elementDepth;
    private int extensionSkipDepth;

    // these fields form a 'stack' of elements
    private JdoRoot jdoRoot;
    private JdoPackage jdoPackage;
    private JdoClass jdoClass;
    private JdoField jdoField;
    private JdoCollection jdoCollection;
    private JdoMap jdoMap;
    private JdoArray jdoArray;
    private JdoQuery jdoQuery;
    private int extStackTop;
    private JdoExtension[] extStack = new JdoExtension[32];
    private ArrayList[] extStackList = new ArrayList[32];

    private ArrayList packageList = new ArrayList();
    private ArrayList packageExtList = new ArrayList();
    private ArrayList packageClassList = new ArrayList();
    private ArrayList classElementList = new ArrayList();
//    private ArrayList fieldExtList = new ArrayList();
    private ArrayList queryExtList = new ArrayList();
    private ArrayList collectionExtList = new ArrayList();

    private boolean doneFilter;
    private boolean doneSql;
    private boolean doneDeclarations;
    private boolean doneResult;
    private StringBuffer text;

    private static final int START = 1;
    private static final int JDO = 2;
    private static final int PACKAGE = 3;
    private static final int CLASS = 4;
    private static final int FIELD = 5;
    private static final int COLLECTION = 6;
    private static final int EXTENSION = 7;
    private static final int EXTENSION_SKIP = 8;
    private static final int QUERY = 9;
    private static final int EMBEDDED = 10;
    private static final int EMBEDDED_FIELD = 11;

    private static String[] STATE_STRING = {"ERROR", "START", "JDO", "PACKAGE", "CLASS",
                                            "FIELD", "COLLECTION", "EXTENSION",
                                            "EXTENSION_SKIP", "QUERY", "EMBEDDED",
                                            "EMBEDDED_FIELD"};

    public MetaDataParser() {

        parserFactory = JaxpUtils.getSAXParserFactory();
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(false);

    }

    /**
     * Load all the jdoNames as resources using loader and return the JdoRoot's.
     */
    public JdoRoot[] parse(Collection jdoNames, ClassLoader loader) {
        int n = jdoNames.size();
        JdoRoot[] roots = new JdoRoot[n];
        int c = 0;
        for (Iterator i = jdoNames.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            InputStream in = null;
            try {


                in = loader.getResourceAsStream(
                        name.startsWith("/") ? name.substring(1) : name);
                if (in == null) {
                    throw BindingSupportImpl.getInstance().runtime("Unable to load resource: " +
                            name);
                }

                roots[c++] = parse(in, name);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException x) {
                        // ignore
                    }
                }
            }
        }
        return roots;
    }

    /**
     * Parse the supplied JDO meta data stream and create a JdoRoot tree.
     */
    public JdoRoot parse(InputStream in, String name) {
        try {
            init(name);

            parser = JaxpUtils.createSAXParser(parserFactory);
            parser.parse(in, this);
            parser = null;



            return jdoRoot;
        } catch (Exception x) {
            throw BindingSupportImpl.getInstance().runtime(
                    name + ": " + x.getMessage(), x);
        }
    }




    /**
     * Resolve an external entity.
     * <p/>
     * We override this method so we don't go looking on the web for dtd's
     */

    public InputSource resolveEntity(String publicId, String systemId) 
	{
        StringReader reader = new StringReader("");
        return new InputSource(reader);
    }


    /**
     * Parse the supplied JDO meta data file and create a JdoRoot tree.
     *
     * @throws IOException if on errors opening filename
     */
    public JdoRoot parse(String filename) throws IOException {
        FileInputStream in = null;
        try {
            return parse(in = new FileInputStream(filename), filename);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
    }

    /**
     * Prepare this parser to receive SAX events. Normally one of the parse
     * methods should be used. Use this only if you are getting SAX events
     * from somewhere else.
     *
     * @see #parse
     * @see #getJdoRoot
     */
    public void init(String name) {
        jdoRoot = new JdoRoot();
        jdoRoot.name = name;
        state = START;
        jdoPackage = null;
        jdoClass = null;
        jdoField = null;
        jdoCollection = null;
        jdoMap = null;
        jdoArray = null;
        extStackTop = -1;
    }

    /**
     * Retrieve the last parsed JdoRoot.
     */
    public JdoRoot getJdoRoot() {
        return jdoRoot;
    }

    /**
     * Decide what to do with the element based on its name and our current
     * state.
     */
    public void startElement(String uri, String localName, String name,
            Attributes attr)  throws SAXException  {
        try {
            startElementImp(uri, localName, name, attr);
        } catch (RuntimeException x) {
            throw x;
        }
    }

    /**
     * Decide what to do with the element based on its name and our current
     * state.
     */
    public void startElementImp(String uri, String localName, String name,
            Attributes attr)  throws SAXException  {
//        if (Debug.DEBUG) {
//            StringBuffer s = new StringBuffer();
//            s.append(STATE_STR[state]);
//            s.append(SPACE.substring(0, elementDepth * 2 + 1));
//            s.append('<');
//            s.append(name);
//            int n = attr.getLength();
//            for (int i = 0; i < n; i++) {
//                s.append(' ');
//                s.append(attr.getQName(i));
//                s.append("=\"");
//                s.append(attr.getValue(i));
//                s.append('"');
//            }
//            s.append('>');
//            cat.debug(s.toString());
//            elementDepth++;
//        }
        elementName = name;
        switch (state) {

            case START:
                if (name.equals("jdo") || name.equals("mapping")) {
                    packageList.clear();
                    state = JDO;
                } else {

					throwInvalidElement("<jdo>");
 
 
				}
                break;

            case JDO:
                if (name.equals("package") || name.equals("namespace")) {
                    startPackage(attr);
                } else {

					throwInvalidElement("<package>");
 
 
				}
                break;

            case PACKAGE:
                if (name.equals("class")) {
                    startClass(attr);
                } else if (name.equals("extension")) {
                    startExtension(jdoPackage, packageExtList, attr);
                } else {
                    throwInvalidElement("<class> or <extension>");
                }
                break;
            case EMBEDDED:
                if (name.equals("field")) {
                    startEmbeddedField(attr);
                } else {
                    throwInvalidElement("<field>");
                }
                break;
            case CLASS:
                if (name.equals("field")) {
                    startField(attr);
                } else if (name.equals("extension")) {
                    startExtension(jdoClass, classElementList, attr);
                } else if (name.equals("query")) {
                    startQuery(attr);
                } else {
                    throwInvalidElement("<field> or <extension>");
                }
                break;
            case EMBEDDED_FIELD:
            case FIELD:
                if (name.equals("extension")) {
                    startExtension(jdoField, jdoField.extensionList, attr);
                } else if (name.equals("collection")) {
                    startCollection(attr);
                } else if (name.equals("map")) {
                    startMap(attr);
                } else if (name.equals("array")) {
                    startArray(attr);
                } else if (name.equals("embedded")) {
                    startEmbedded(attr);
                } else {
                    throwInvalidElement(
                            "<collection>, <map>, <array> or <extension>");
                }
                break;

            case COLLECTION:
                if (name.equals("extension")) {
                    JdoElement p;
                    if (jdoCollection != null) {
                        p = jdoCollection;
                    } else if (jdoArray != null) {
                        p = jdoArray;
                    } else {
                        p = jdoMap;
                    }
                    startExtension(p, collectionExtList, attr);
                } else {
                    throwInvalidElement("<extension>");
                }
                break;

            case EXTENSION:
                if (name.equals("extension")) {
                    startExtension(extStack[extStackTop],
                            extStackList[extStackTop], attr);
                } else {
                    throwInvalidElement("<extension>");
                }
                break;

            case EXTENSION_SKIP:
                extensionSkipDepth++;
                break;

            case QUERY:
                if (name.equals("filter")) {
                    if (doneFilter) throwDuplicateElement("filter");
                    if (jdoQuery.filter != null) {
                        throw BindingSupportImpl.getInstance().runtime("You may not have a filter attribute and " +
                                "element in " + getContext());
                    }
                    doneFilter = true;
                    text = new StringBuffer();
                    // no attributes to read
                } else if (name.equals("sql")) {
                    if (doneSql) throwDuplicateElement("sql");
                    if (jdoQuery.sql != null) {
                        throw BindingSupportImpl.getInstance().runtime("You may not have a sql attribute and " +
                                "element in " + getContext());
                    }
                    doneSql = true;
                    text = new StringBuffer();
                    // no attributes to read
                } else if (name.equals("declare")) {
                    if (doneDeclarations) throwDuplicateElement("declarations");
                    doneDeclarations = true;
                    jdoQuery.imports = attr.getValue("imports");
                    jdoQuery.parameters = attr.getValue("parameters");
                    jdoQuery.variables = attr.getValue("variables");
                } else if (name.equals("result")) {
                    if (doneResult) throwDuplicateElement("result");
                    doneResult = true;
                    jdoQuery.resultClass = attr.getValue("class");
                    jdoQuery.unique = getTriState(attr, "unique");
                    jdoQuery.grouping = attr.getValue("grouping");
                    text = new StringBuffer();
                } else if (name.equals("extension")) {
                    startExtension(jdoQuery, queryExtList, attr);
                } else {
                    throwInvalidElement("<filter>, <declarations>, <result> " +
                            "or <extension>");
                }
                break;
        }
    }

    private void startEmbedded(Attributes attr) {
        jdoField.embedded = JdoField.TRUE;
        state = EMBEDDED;
    }

    public void characters(char ch[], int start, int length)
             throws SAXException  {
        if (text != null) text.append(ch, start, length);
    }

    private void throwDuplicateElement(String name) {
        throw BindingSupportImpl.getInstance().runtime("Only one '" + elementName +
                "' element is allowed in " + getContext());
    }

    private void throwInvalidElement(String valid) {
        throw BindingSupportImpl.getInstance().runtime("Invalid element '" + elementName +
                "' in " + getContext() + ", expected " + valid);
    }

    private void throwInvalidAttribute(String attrName, String value) {
        throw BindingSupportImpl.getInstance().runtime("Invalid " + attrName + " attribute '" +
                value + "' in " + getContext());
    }

    private void startPackage(Attributes attr) {
        jdoPackage = new JdoPackage();
        jdoPackage.parent = jdoRoot;
        packageList.add(jdoPackage);
        jdoPackage.name = getReqAttr(attr, "name");
        packageClassList.clear();
        packageExtList.clear();
        state = PACKAGE;
    }

    private void startClass(Attributes attr) {
        jdoClass = new JdoClass();
        jdoClass.parent = jdoPackage;
        packageClassList.add(jdoClass);
        jdoClass.name = getReqAttr(attr, "name");
        String idt = attr.getValue("identity-type");
        if (idt != null) {
            if (idt.equals("datastore")) {
                jdoClass.identityType = JdoClass.IDENTITY_TYPE_DATASTORE;
            } else if (idt.equals("application")) {
                jdoClass.identityType = JdoClass.IDENTITY_TYPE_APPLICATION;
            } else if (idt.equals("nondurable")) {
                jdoClass.identityType = JdoClass.IDENTITY_TYPE_NONDURABLE;
            } else {
                throwInvalidAttribute("indentity-type", idt);
            }
        }
        jdoClass.objectIdClass = attr.getValue("objectid-class");
        String re = attr.getValue("requires-extent");
        if (re == null || re.equals("true")) {
            jdoClass.requiresExtent = true;
        } else if (re.equals("false")) {
            jdoClass.requiresExtent = false;
        } else {
            throwInvalidAttribute("requires-extent", re);
        }
        jdoClass.pcSuperclass = attr.getValue("persistence-capable-superclass");
        classElementList.clear();
        state = CLASS;
    }

    private void startField(Attributes attr) {
        jdoField = new JdoField();
        jdoField.parent = jdoClass;
        classElementList.add(jdoField);
        jdoField.name = getReqAttr(attr, "name");
        String pm = attr.getValue("persistence-modifier");
        if (pm != null) {
            if (pm.equals("persistent")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_PERSISTENT;
            } else if (pm.equals("transactional")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_TRANSACTIONAL;
            } else if (pm.equals("none")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_NONE;
            } else {
                throwInvalidAttribute("persistence-modifier", pm);
            }
        }
        String pk = attr.getValue("primary-key");
        if (pk == null || pk.equals("false")) {
            jdoField.primaryKey = false;
        } else if (pk.equals("true")) {
            jdoField.primaryKey = true;
        } else {
            throwInvalidAttribute("primary-key", pk);
        }
        String nv = attr.getValue("null-value");
        if (nv == null || nv.equals("none")) {
            jdoField.nullValue = JdoField.NULL_VALUE_NONE;
        } else if (nv.equals("exception")) {
            jdoField.nullValue = JdoField.NULL_VALUE_EXCEPTION;
        } else if (nv.equals("default")) {
            jdoField.nullValue = JdoField.NULL_VALUE_DEFAULT;
        } else {
            throwInvalidAttribute("null-value", nv);
        }
        String df = attr.getValue("default-fetch-group");
        if (df != null) {
            if (df.equals("true")) {
                jdoField.defaultFetchGroup = JdoField.TRUE;
            } else if (df.equals("false")) {
                jdoField.defaultFetchGroup = JdoField.FALSE;
            } else {
                throwInvalidAttribute("default-fetch-group", df);
            }
        }
        String em = attr.getValue("embedded");
        if (em != null) {
            if (em.equals("true")) {
                jdoField.embedded = JdoField.TRUE;
            } else if (em.equals("false")) {
                jdoField.embedded = JdoField.FALSE;
            } else {
                throwInvalidAttribute("embedded", em);
            }
        }
        state = FIELD;
    }

    private void startEmbeddedField(Attributes attr) {
        jdoField = new JdoField(jdoField);
        jdoField.parent = jdoClass;
        jdoField.name = getReqAttr(attr, "name");

        String pm = attr.getValue("persistence-modifier");
        if (pm != null) {
            if (pm.equals("persistent")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_PERSISTENT;
            } else if (pm.equals("transactional")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_TRANSACTIONAL;
            } else if (pm.equals("none")) {
                jdoField.persistenceModifier = JdoField.PERSISTENCE_MODIFIER_NONE;
            } else {
                throwInvalidAttribute("persistence-modifier", pm);
            }
        }

        String nv = attr.getValue("null-value");
        if (nv == null || nv.equals("none")) {
            jdoField.nullValue = JdoField.NULL_VALUE_NONE;
        } else if (nv.equals("exception")) {
            jdoField.nullValue = JdoField.NULL_VALUE_EXCEPTION;
        } else if (nv.equals("default")) {
            jdoField.nullValue = JdoField.NULL_VALUE_DEFAULT;
        } else {
            throwInvalidAttribute("null-value", nv);
        }
        String df = attr.getValue("default-fetch-group");
        if (df != null) {
            if (df.equals("true")) {
                jdoField.defaultFetchGroup = JdoField.TRUE;
            } else if (df.equals("false")) {
                jdoField.defaultFetchGroup = JdoField.FALSE;
            } else {
                throwInvalidAttribute("default-fetch-group", df);
            }
        }
        String em = attr.getValue("embedded");
        if (em != null) {
            if (em.equals("true")) {
                jdoField.embedded = JdoField.TRUE;
            } else if (em.equals("false")) {
                jdoField.embedded = JdoField.FALSE;
            } else {
                throwInvalidAttribute("embedded", em);
            }
        }
        state = EMBEDDED_FIELD;
    }

    private void startCollection(Attributes attr) {
        jdoCollection = new JdoCollection();
        jdoCollection.parent = jdoField;
        jdoField.collection = jdoCollection;
        jdoCollection.elementType = attr.getValue("element-type");
        String em = attr.getValue("embedded-element");
        if (em != null) {
            if (em.equals("true")) {
                jdoCollection.embeddedElement = JdoCollection.TRUE;
            } else if (em.equals("false")) {
                jdoCollection.embeddedElement = JdoCollection.FALSE;
            } else {
                throwInvalidAttribute("embedded-element", em);
            }
        }
        collectionExtList.clear();
        state = COLLECTION;
    }

    private void startMap(Attributes attr) {
        jdoMap = new JdoMap();
        jdoMap.parent = jdoField;
        jdoField.map = jdoMap;
        jdoMap.keyType = attr.getValue("key-type");
        jdoMap.valueType = attr.getValue("value-type");
        String em = attr.getValue("embedded-key");
        if (em != null) {
            if (em.equals("true")) {
                jdoMap.embeddedKey = JdoMap.TRUE;
            } else if (em.equals("false")) {
                jdoMap.embeddedKey = JdoMap.FALSE;
            } else {
                throwInvalidAttribute("embedded-key", em);
            }
        }
        em = attr.getValue("embedded-value");
        if (em != null) {
            if (em.equals("true")) {
                jdoMap.embeddedValue = JdoMap.TRUE;
            } else if (em.equals("false")) {
                jdoMap.embeddedValue = JdoMap.FALSE;
            } else {
                throwInvalidAttribute("embedded-value", em);
            }
        }
        collectionExtList.clear();
        state = COLLECTION;
    }

    private void startArray(Attributes attr) {
        jdoArray = new JdoArray();
        jdoArray.parent = jdoField;
        jdoField.array = jdoArray;
        String em = attr.getValue("embedded-element");
        if (em != null) {
            if (em.equals("true")) {
                jdoArray.embeddedElement = JdoCollection.TRUE;
            } else if (em.equals("false")) {
                jdoArray.embeddedElement = JdoCollection.FALSE;
            } else {
                throwInvalidAttribute("embedded-element", em);
            }
        }
        collectionExtList.clear();
        state = COLLECTION;
    }

    private void startExtension(JdoElement parent, ArrayList list,
            Attributes attr) {
 

        String vn = getReqAttr(attr, "vendor-name");

        if (isOurVendorName(vn)) {
            JdoExtension e = new JdoExtension();
            e.parent = parent;
            String key = getReqAttr(attr, "key");
            int i = JdoExtension.parseKey(key);
            if (i == Integer.MIN_VALUE) {
                throw BindingSupportImpl.getInstance().runtime("Invalid key '" + key + "' in " +
                        getContext());
            }
            e.key = i;
            e.value = attr.getValue("value");
            extStack[++extStackTop] = e;
            ArrayList a = extStackList[extStackTop];
            if (a == null) {
                extStackList[extStackTop] = new ArrayList();
            } else {
                a.clear();
            }
            if (state != EXTENSION) {
                extState = state;
                state = EXTENSION;
            }
            list.add(e);
        } else {
            extSkipState = state;
            state = EXTENSION_SKIP;
            extensionSkipDepth = 0;
        }
    }

    private void startQuery(Attributes attr) {
        jdoQuery = new JdoQuery();
        jdoQuery.parent = jdoClass;
        jdoClass.addJdoQuery(jdoQuery);
        jdoQuery.name = getReqAttr(attr, "name");
        jdoQuery.language = attr.getValue("language");
        jdoQuery.ignoreCache = getTriState(attr, "ignore-cache");
        jdoQuery.includeSubclasses = getTriState(attr, "include-subclasses");
        jdoQuery.ordering = attr.getValue("ordering");
        jdoQuery.filter = attr.getValue("filter");
        jdoQuery.sql = attr.getValue("sql");
        String r = attr.getValue("range");
        if (r != null) {
            try {
                int i = r.indexOf(',');
                jdoQuery.rangeStart = Integer.parseInt(r.substring(0, i));
                jdoQuery.rangeEnd = Integer.parseInt(r.substring(i + 1));
            } catch (Exception e) {
                throwInvalidAttribute("range", r);
            }
        }
        state = QUERY;
        doneSql = false;
    }

    /**
     * Decide what to do with the element based on our current state and its
     * name.
     */
    public void endElement(String uri, String localName, String name)
             throws SAXException  {
        endElementImp(uri, localName, name);
    }

    /**
     * Decide what to do with the element based on our current state and its
     * name.
     */
    public void endElementImp(String uri, String localName, String name)
             throws SAXException  {
//        if (cat.isDebugEnabled()) {
//            elementDepth--;
//            StringBuffer s = new StringBuffer();
//            s.append(STATE_STR[state]);
//            s.append(SPACE.substring(0, elementDepth * 2 + 1));
//            s.append("</");
//            s.append(name);
//            s.append('>');
//            cat.debug(s.toString());
//        }
        elementName = name;
        int n;
        switch (state) {

            case JDO:
                jdoRoot.packages = new JdoPackage[packageList.size()];
                packageList.toArray(jdoRoot.packages);
                state = START;
                break;

            case PACKAGE:
                n = packageClassList.size();
                jdoPackage.classes = new JdoClass[n];
                packageClassList.toArray(jdoPackage.classes);
                n = packageExtList.size();
                if (n > 0) {
                    jdoPackage.extensions = new JdoExtension[n];
                    packageExtList.toArray(jdoPackage.extensions);
                }
                jdoPackage = null;
                state = JDO;
                break;

            case CLASS:
                jdoClass.elements = new JdoElement[classElementList.size()];
                classElementList.toArray(jdoClass.elements);
                jdoClass = null;
                state = PACKAGE;
                break;
            case EMBEDDED_FIELD:
                n = jdoField.extensionList.size();
                if (n > 0) {
                    jdoField.extensions = new JdoExtension[n];
                    jdoField.extensionList.toArray(jdoField.extensions);
                }
                jdoField = jdoField.parentField;
                state = EMBEDDED;
                break;
            case FIELD:
                n = jdoField.extensionList.size();
                if (n > 0) {
                    jdoField.extensions = new JdoExtension[n];
                    jdoField.extensionList.toArray(jdoField.extensions);
                }
                jdoField = null;
                jdoCollection = null;
                jdoArray = null;
                jdoMap = null;
                state = CLASS;
                break;

            case COLLECTION:
                n = collectionExtList.size();
                if (n > 0) {
                    JdoExtension[] e = new JdoExtension[n];
                    collectionExtList.toArray(e);
                    if (jdoCollection != null) {
                        jdoCollection.extensions = e;
                        jdoCollection = null;
                    } else if (jdoArray != null) {
                        jdoArray.extensions = e;
                        jdoArray = null;
                    } else {
                        jdoMap.extensions = e;
                        jdoMap = null;
                    }
                }
                state = FIELD;
                break;

            case EXTENSION:
                endExtension();
                break;

            case EXTENSION_SKIP:
                if (extensionSkipDepth == 0) {
                    state = extSkipState;
                } else {
                    extensionSkipDepth--;
                }
                break;

            case QUERY:
                if (elementName.equals("query")) {
                    jdoQuery.extensions = new JdoExtension[queryExtList.size()];
                    queryExtList.toArray(jdoQuery.extensions);
                    queryExtList.clear();
                    jdoQuery = null;
                    doneFilter = false;
                    doneDeclarations = false;
                    doneResult = false;
                    state = CLASS;
                } else if (elementName.equals("filter")) {
                    if (text != null) jdoQuery.filter = text.toString().trim();
                    text = null;
                } else if (elementName.equals("result")) {
                    if (text != null) jdoQuery.result = text.toString().trim();
                    text = null;
                } else if (elementName.equals("sql")) {
                    if (text != null) jdoQuery.sql = text.toString().trim();
                    text = null;
                }
                break;
            case EMBEDDED:
                if (jdoField.parentField == null) state = FIELD;
                else state = EMBEDDED_FIELD;
                break;
        }
    }

    private void endExtension() {
        JdoExtension top = extStack[extStackTop];
        ArrayList a = extStackList[extStackTop];
        int n = a.size();
        if (n > 0) {
            top.nested = new JdoExtension[n];
            a.toArray(top.nested);
        }
        if (--extStackTop < 0) state = extState;
    }

    /**
     * Get information about the current parser context in a String. This is
     * used to construct error messages.
     */
    private String getContext() {
        StringBuffer s = new StringBuffer();
        s.append(jdoRoot.name);
        if (jdoPackage != null) {
            s.append(":package[");
            s.append(jdoPackage.name);
            s.append(']');
            if (jdoClass != null) {
                s.append("/class[");
                s.append(jdoClass.name);
                s.append(']');
                if (jdoField != null) {
                    s.append("/field[");
                    s.append(jdoField.name);
                    s.append(']');
                    if (jdoCollection != null) {
                        s.append("/collection");
                    }
                    if (jdoMap != null) {
                        s.append("/map");
                    }
                    if (jdoArray != null) {
                        s.append("/array");
                    }
                }
                if (jdoQuery != null) {
                    s.append("/query");
                }
            }
        }
        for (int i = 0; i <= extStackTop; i++) {
            s.append("/extension[");
            JdoExtension e = extStack[i];
            s.append(JdoExtension.toKeyString(e.key));
            s.append('=');
            s.append(e.value);
            s.append(']');
        }
        return s.toString();
    }

    /**
     * Get the value of the Attribute with name.
     */
    private String getReqAttr(Attributes attr, String name)
            /*throws JDOFatalUserException*/ {
        String v = attr.getValue(name);
        if (v == null) {
            StringBuffer s = new StringBuffer();
            s.append("Expected attribute '");
            s.append(name);
            s.append("' in ");
            s.append(getContext());
            s.append('.');
            s.append(elementName);
            throw BindingSupportImpl.getInstance().runtime(s.toString());
        }
        return v;
    }

    /**
     * Get the value of the int Attribute with name. Returns -1 if not found.
     */
    private int getIntAttr(Attributes attr, String name) {
        String v = attr.getValue(name);
        if (v == null) return -1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            StringBuffer s = new StringBuffer();
            s.append("Expected int attribute '");
            s.append(name);
            s.append("' found '");
            s.append(v);
            s.append("' in ");
            s.append(getContext());
            s.append('.');
            s.append(elementName);
            throw BindingSupportImpl.getInstance().runtime(s.toString());
        }
    }

    /**
     * Get an optional boolean attribute.
     *
     * @see MDStatics.NOT_SET
     * @see MDStatics.FALSE
     * @see MDStatics.TRUE
     */
    private int getTriState(Attributes attr, String name)
            /*throws JDOFatalUserException*/ {
        String v = attr.getValue(name);
        if (v == null) return MDStatics.NOT_SET;
        if ("true".equals(v)) return MDStatics.TRUE;
        if ("false".equals(v)) return MDStatics.FALSE;
        StringBuffer s = new StringBuffer();
        s.append("Expected 'true' or 'false' for attribute '");
        s.append(name);
        s.append("', got '");
        s.append(v);
        s.append("' in ");
        s.append(getContext());
        s.append('.');
        s.append(elementName);
        throw BindingSupportImpl.getInstance().runtime(s.toString());
    }

    /**
     * Does s match our vendor name?
     */
    public static boolean isOurVendorName(String s) {
		return VENDOR_NAME.equals(s) || VENDOR_NAME_JDOGENIE.equals(s);
    }

}
