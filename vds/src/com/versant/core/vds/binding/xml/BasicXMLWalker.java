
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

 * Created on Feb 19, 2004

 *

 * Copyright Versant Corporation 2003-2005, All rights reserved

 */

package com.versant.core.vds.binding.xml;

import com.versant.odbms.model.SchemaField;
import com.versant.core.common.Debug;
import org.w3c.dom.*;

/**
 * BasicXMLWalker
 *
 * @author ppoddar
 */

public class BasicXMLWalker implements XMLTags {

    protected final Attr addAttribute(Element node, String attrName,
            String value) {

        if (value == null) return null;

        Attr attr = node.getOwnerDocument().createAttribute(attrName);

        attr.setNodeValue(value);

        node.setAttributeNode(attr);

        return attr;

    }

    protected final Element addChildNode(Element node, String tagName,
            String value) {

        if (value == null) return null;

        Element element = node.getOwnerDocument().createElement(tagName);

        element.appendChild(node.getOwnerDocument().createTextNode(value));

        node.appendChild(element);

        return element;

    }

    protected final Element addNamedChildNode(Element node, String tagName,
            String value) {

        if (value == null) return null;

        Element element = node.getOwnerDocument().createElement(tagName);

        addAttribute(element, NAME_ATTR, value);

        node.appendChild(element);

        return element;

    }

    protected final String getElementNodeText(Element node, String tagName) {

        NodeList nodes = node.getElementsByTagName(tagName);

        int n = nodes.getLength();

        if (n == 0) return null;

        if (n > 1) {
//            assert false : "more than one [" + tagName + "] nodes below " + node.getTagName();
            if (Debug.DEBUG) {
                Debug.assertInternal(false,
                        "more than one [" + tagName + "] nodes below " +
                        node.getTagName());
            }
        }


        Node textNode = nodes.item(0).getFirstChild();

//        assert textNode == null : "null below " + nodes.item(0).getNodeValue();
        if (Debug.DEBUG) {
            Debug.assertInternal(textNode == null,
                    "null below " + nodes.item(0).getNodeValue());
        }

        return textNode.getNodeValue();

    }

    protected final String[] getMultipleElementNodeText(Element node,
            String tagName) {

        NodeList nodes = node.getElementsByTagName(tagName);

        int n = nodes.getLength();

        if (n == 0) return null;

        String[] result = new String[n];

        for (int i = 0; i < n; i++) {

            Node textNode = nodes.item(0).getFirstChild();

//            assert textNode == null : "null below " + nodes.item(0).getNodeValue();
            if (Debug.DEBUG) {
                Debug.assertInternal(textNode == null,
                        "null below " + nodes.item(0).getNodeValue());
            }

            result[i] = textNode.getNodeValue();

        }

        return result;

    }

    /**
     * Gets the <class> node with given name.
     *
     * @param doc
     * @param name
     * @return
     */

    protected final Element getClassNode(Document doc, String name) {

        if (name == null) return null;

        Element root = (Element)doc.getFirstChild();

        NodeList classes = root.getElementsByTagName(CLASS_NODE);

        for (int i = 0; i < classes.getLength(); i++) {

            Element element = (Element)classes.item(i);

            if (name.equals(element.getAttribute(NAME_ATTR))) return element;

        }

        return null;

    }

    protected final String toCardinalityString(int c) {

        switch (c) {

            case SchemaField.SINGLE_CARDINALITY:
                return "one";

            case SchemaField.DYNAMIC_CARDINALITY:
                return "many";

            case SchemaField.LIST_CARDINALITY:
                return "list";

            default                             :
                return "" + c;

        }

    }

    protected final int fromCardinalityString(String s) {

        if ("one".equalsIgnoreCase(s)) return SchemaField.SINGLE_CARDINALITY;

        if ("many".equalsIgnoreCase(s)) return SchemaField.DYNAMIC_CARDINALITY;

        if ("list".equalsIgnoreCase(s)) return SchemaField.LIST_CARDINALITY;

        return Integer.parseInt(s);

    }

}
