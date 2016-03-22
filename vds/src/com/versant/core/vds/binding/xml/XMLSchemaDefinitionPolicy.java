
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
package com.versant.core.vds.binding.xml;

import com.versant.core.vds.binding.SchemaDefinitionException;
import com.versant.core.common.Debug;
import com.versant.odbms.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the policy to define schema class (the basic type in Versant OODBMS
 * type system) given Element (basic type in Kodo type system) and Java Class
 * (basic type in Java Type system).
 * <p/>
 * The policy is based on basic type support in datastore for primitive types and
 * their arrays. Over and above these basic datastore types, this policy defines
 * following classes:
 * <OL>
 * <LI>Templated collection: One or more templated container that holds its elements
 * either as a basic supported schema types or array of other user defined schema types.
 * A naming convention is introduced to name these types that does not conflict with
 * allowed Java names for classes.
 * <LI>Templated map: One or more templated map that holds its keys and values in two
 * parallel arrays i.e. the a certain key-value pair corrspond to the identical index
 * of the two arrays.
 * <LI>BLOB: A single class named "BLOB" that stores a serialized byte stream.
 * </OL>
 * Certain Java library classes such as <code>java.math.BigDecimal</code> etcetra
 * are represented as strings in the datastore.
 */

public final class XMLSchemaDefinitionPolicy extends BasicXMLWalker {

    private static final boolean LOG = System.getProperty("versant.logging") != null;
    private static final Logger _log = Logger.getLogger(
            XMLSchemaDefinitionPolicy.class.getName());

    public synchronized UserSchemaClass getUserSchemaClass(
            final UserSchemaModel aView,
            final Object metadata) {
        if (Debug.DEBUG) {
//            assert metadata != null : "Can not get schema class for null metadata";
            Debug.assertInternal(metadata != null,
                    "Can not get schema class for null metadata");
//            assert metadata instanceof Element : metadata.getClass() +
//            " is not of type kodo.meta.ClassMetaData";
//            I think I will drop this test?
//            assert aView != null : "Can not define class with null ApplicationView";
            Debug.assertInternal(aView != null,
                    "Can not define class with null ApplicationView");

        }


        Element aClassNode = (Element)metadata;
        UserSchemaClass result = aView.getAssociatedSchemaClass(aClassNode);
        if (result != null) {
            if (result.isResolved()) {
                return result;
            } else {
                resolve(aView, result);
            }
        } else {
            result = createNewClass(aView, aClassNode);
            resolve(aView, result);
        }
//        assert result.isResolved();
        if (Debug.DEBUG) {
            Debug.assertInternal(result.isResolved(),
                    "Result is not resolved");
        }

        return result;
    }

    private void resolve(final UserSchemaModel aView,
            final UserSchemaClass aSchemaClass) {

        if (aSchemaClass.isResolved()) return;

        UserSchemaClass[] superSchemaClasses = aSchemaClass.getSuperClasses();
        for (int i = 0; i < superSchemaClasses.length; i++) {
            Element superJDOClass = (Element)superSchemaClasses[i].getUserObject();
//            assert superJDOClass != null;
            if (Debug.DEBUG) {
                Debug.assertInternal(superJDOClass != null,
                        "superJDOClass is null");
            }
            resolve(aView, superSchemaClasses[i]);
        }
        aSchemaClass.setResolved(true);
        UserSchemaField[] schemaFields = aSchemaClass.getDeclaredFields();
        for (int i = 0; i < schemaFields.length; i++) {
            UserSchemaField schemaField = schemaFields[i];
            SchemaClass domain = schemaField.getDomain();
            if (domain.isPrimitive()) continue;
            if (domain instanceof SystemSchemaClass) continue;
//            assert domain instanceof UserSchemaClass;
            if (Debug.DEBUG) {
                Debug.assertInternal(domain instanceof UserSchemaClass,
                        "domain is not a instanceof UserSchemaClass");
            }
            resolve(aView, (UserSchemaClass)domain);
        }
    }

    public UserSchemaModel getUserSchemaModel(final UserSchemaModel view,
            String uri) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(uri);
            getUserSchemaModel(view, doc);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return view;
    }

    public UserSchemaModel getUserSchemaModel(final UserSchemaModel view,
            Document doc) {
        Element root = (Element)doc.getFirstChild();
        NodeList classes = root.getElementsByTagName("class");
        for (int i = 0; i < classes.getLength(); i++) {
            String className = ((Element)classes.item(i)).getAttribute("name");
            System.out.println("Defining from XML node " + className);
            getUserSchemaClass(view, getClassNode(doc, className));
        }
        return view;
    }

    private SchemaClass getUserSchemaClassInternal(final UserSchemaModel aView,
            final Element aJDOClass) {
        UserSchemaClass result = aView.getAssociatedSchemaClass(aJDOClass);
        if (result != null) return result;
        result = createNewClass(aView, aJDOClass);
        return result;
    }

    private SchemaClass getUserSchemaClassInternal(final UserSchemaModel aView,
            final Document aRepository,
            final String aClassName) {
        SchemaClass result = PrimitiveSchemaClass.getSchemaClass(aClassName);
        if (result != null) return result;
        Element classNode = getClassNode(aRepository, aClassName);
//        assert classNode != null : "No class node [" + aClassName + "] found in doc";
        if (Debug.DEBUG) {
            Debug.assertInternal(classNode != null,
                    "No class node [" + aClassName + "] found in doc");
        }
        return getUserSchemaClassInternal(aView, classNode);
    }

    private UserSchemaClass createNewClass(final UserSchemaModel aView,
            final Element aClassNode) {

//        assert aView.getAssociatedSchemaClass(aClassNode) == null;
        if (Debug.DEBUG) {
            Debug.assertInternal(aView.getAssociatedSchemaClass(aClassNode) == null,
                    "aView.getAssociatedSchemaClass(aClassNode) != null");
        }

        UserSchemaClass[] superSchemaClasses = null;
        String[] superClassNames = getMultipleElementNodeText(aClassNode,
                SUPERCLASS_ELEMENT);
        if (superClassNames != null) {
            superSchemaClasses = new UserSchemaClass[superClassNames.length];
            for (int i = 0; i < superClassNames.length; i++) {
                superSchemaClasses[i] = (UserSchemaClass)getUserSchemaClassInternal(
                        aView,
                        aClassNode.getOwnerDocument(),
                        superClassNames[i]);
            }
        }

        String schemaClassName = aClassNode.getAttribute(NAME_ATTR);
        String applicationClassName = getElementNodeText(aClassNode,
                AUXINFO_ELEMENT);
        if (LOG) {
            _log.log(Level.FINE,
                    "Building SchemaClass for  [" + schemaClassName + "]");
        }

        UserSchemaClass result = new UserSchemaClass(schemaClassName,
                superSchemaClasses,
                aView).setAuxiliaryInfo(applicationClassName);
        result.setUserObject(aClassNode);

        NodeList declaredFields = aClassNode.getChildNodes();
        for (int i = 0; i < declaredFields.getLength(); i++) {
            Element child = (Element)declaredFields.item(i);
            if ("field".equals(child.getTagName())) {
                createNewField(result, child, aView);
            }
        }
        return result;
    }

    protected UserSchemaField createNewField(final UserSchemaClass aOwnerClass,
            final Element aFieldNode,
            UserSchemaModel aView) {

        if (LOG) {
            _log.log(Level.FINER,
                    "creating schema field for " + aFieldNode);
        }

        UserSchemaField result = null;
        String schemaFieldName = aFieldNode.getAttribute(NAME_ATTR);
        String applicationFieldName = getElementNodeText(aFieldNode,
                AUXINFO_ELEMENT);
        boolean isEmbedded = "TRUE".equalsIgnoreCase(
                aFieldNode.getAttribute(EMBEDDED_ATTR));
        String domainName = ((Element)aFieldNode.getElementsByTagName(
                DOMAIN_ELEMENT).item(0)).getAttribute(NAME_ATTR);
        int cardinality = fromCardinalityString(
                getElementNodeText(aFieldNode, CARDINALITY_ELEMENT));

        SchemaClass domain = getUserSchemaClassInternal(aView,
                aFieldNode.getOwnerDocument(), domainName);
        int nullity = getNullity(aFieldNode);

        if (isEmbedded) {
            result = aOwnerClass.newAggregateField(schemaFieldName,
                    (UserSchemaClass)domain);
        } else {
            result = aOwnerClass.newField(schemaFieldName,
                    domain,
                    cardinality,
                    nullity).setAuxiliaryInfo(applicationFieldName);
        }
        addSyntheticField(aFieldNode, result);
        return result;
    }

    Element getDomainNode(Element aFieldNode) {
        return (Element)aFieldNode.getFirstChild();
    }

    int getNullity(Element fieldNode) {
        int nullity = 0;
        if (fieldNode.getElementsByTagName(SYNTHETIC_NULL) != null) {
            nullity = SchemaField.NULL_ALLOWED;
        }
        if (fieldNode.getElementsByTagName(SYNTHETIC_NULL_ELEMENTS) != null) {
            nullity = nullity | SchemaField.NULL_ELEMENTS_ALLOWED;
        }
        return nullity;

    }

    void addSyntheticField(Element fieldNode, UserSchemaField f) {
        if (fieldNode.getElementsByTagName(SYNTHETIC_BYTES) != null) {
            f.addSyntheticField(SchemaField.BYTES_SYNTHETIC_FIELD);
        }
        if (fieldNode.getElementsByTagName(SYNTHETIC_CLASS) != null) {
            f.addSyntheticField(SchemaField.CLASS_SYNTHETIC_FIELD);
        }
        if (fieldNode.getElementsByTagName(SYNTHETIC_NULL) != null) {
            f.addSyntheticField(SchemaField.NULL_SYNTHETIC_FIELD);
        }
        if (fieldNode.getElementsByTagName(SYNTHETIC_NULL_ELEMENTS) != null) {
            f.addSyntheticField(SchemaField.NULL_ELEMENTS_SYNTHETIC_FIELD);
        }
    }

    public static void throwException(String errorCode, Object[] params) {
        SchemaDefinitionException ex = new SchemaDefinitionException(errorCode,
                params);
        _log.severe(ex.getMessage());
        throw ex;

    }
}
