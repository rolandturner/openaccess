
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
/* * Created on Feb 4, 2004 * * Copyright Versant Corporation 2003-2005, All rights reserved */
package com.versant.core.vds.binding.xml;

import com.versant.odbms.model.*;
import com.versant.odbms.model.schema.*;
import com.versant.core.common.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XMLSchemaWriter serializes a Versant schema onto a XML file.
 * <p/>
 * <p> * Versant schema is described as an XML Schema document
 * * <code>versant-schema.xsd</code>. *   * @author ppoddar
 */
public class XMLSchemaWriter extends BasicXMLWalker {

    static private ArrayList KNOWN_CLASS_LIST = new ArrayList();

    static {
        KNOWN_CLASS_LIST.add("lo_class");
        KNOWN_CLASS_LIST.add("pl_class");
        KNOWN_CLASS_LIST.add("o_genericobj");
        KNOWN_CLASS_LIST.add("o_repobj");
        KNOWN_CLASS_LIST.add("cm_smart");
        KNOWN_CLASS_LIST.add("o_ev_reg");
        KNOWN_CLASS_LIST.add("o_ev_global");
        KNOWN_CLASS_LIST.add("o_ev_msg");
        KNOWN_CLASS_LIST.add("o_ev_iter");
    }

    public Document createDocument(final UserSchemaModel model) {
        return createDocument(model.getClasses());
    }

    /**
     * Creates a <code>org.w3c.dom.Document</code> from a given set of
     *
     * @return Document that contains schema information of the given set of classes.
     *         * @throws Exception
     * @author ppoddar
     * @since 1.0
     *        <p/>
     *        DatastoreSchemaClasses.
     *        * @param classes     * @param out
     */
    public Document createDocument(final SchemaClass[] classes) {
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex.toString());
        }
        Element root = createPreamble(doc);
        for (int i = 0; i < classes.length; i++) {
            if (KNOWN_CLASS_LIST.contains(classes[i].getName())) {
                continue;
            }
            createClassNode(root, classes[i]);
        }
        return doc;
    }

    private Element createPreamble(Document doc) {
        Element root = doc.createElement(SCHEMA_NODE);
        root.setAttribute(XSD_NS, XSD_NS_URI);
        root.setAttribute(XSD_INSTANCE_NS, XSD_INSTANCE_NS_URI);
        root.setAttribute(VERSANT_XSD_NS, VERSANT_XSD_NS_URI);
        Node comment = doc.createComment(
                " Created on " + new java.util.Date() + "\n Versant Corporation www.versant.com");
        root.appendChild(comment);
        doc.appendChild(root);
        return root;
    }

    /**
     * Creates a Element node of the document with details of given class.
     *
     * @param doc
     * @param aClass
     * @return <class name="X">
     *         *   <auxInfo>x</auxInfo>
     *         *   <super>a1</super>
     *         *   <super>a2</super>
     *         *   <field name="F" cardinality="-1" embedded="true">
     *         *      <auxInfo>f</auxInfo>     *
     * @author ppoddar
     * @since 1.0
     */
    private Element createClassNode(Element root, SchemaClass aClass) {
        Element classNode = addNamedChildNode(root, CLASS_NODE,
                aClass.getName());
        addChildNode(classNode, AUXINFO_ELEMENT, aClass.getAuxiliaryInfo());
        SchemaClass[] superClasses = null;
        SchemaField[] fields = null;
        if (aClass instanceof DatastoreSchemaClass) {
            superClasses = ((DatastoreSchemaClass)aClass).getSuperClasses();
            fields = ((DatastoreSchemaClass)aClass).getDeclaredFields();
        } else if (aClass instanceof UserSchemaClass) {
            superClasses = ((UserSchemaClass)aClass).getSuperClasses();
            fields = ((UserSchemaClass)aClass).getDeclaredFields();
        } else {
//            assert false;
            if (Debug.DEBUG) {
                Debug.assertInternal(false,
                        "SchemaClass is not a instanceof DatastoreSchemaClass " +
                        "or UserSchemaClass");
            }
        }
        for (int i = 0; i < superClasses.length; i++) {
            addNamedChildNode(classNode, SUPERCLASS_ELEMENT,
                    superClasses[i].getName());
        }
        for (int i = 0; i < fields.length; i++) {
            classNode.appendChild(createFieldNode(classNode, fields[i]));
        }
        return classNode;
    }

    private Node createFieldNode(Element classNode, SchemaField f) {
        Element fieldNode = addNamedChildNode(classNode, FIELD_NODE,
                f.getName());
        if (f.isAggregation()) {
            addAttribute(fieldNode, EMBEDDED_ATTR, "true");
        }
        addChildNode(fieldNode, AUXINFO_ELEMENT, f.getAuxiliaryInfo());
        addChildNode(fieldNode, CARDINALITY_ELEMENT,
                toCardinalityString(f.getCardinality()));
        addNamedChildNode(fieldNode, DOMAIN_ELEMENT, f.getDomain().getName());
        Document doc = classNode.getOwnerDocument();
        if (f.getSyntheticField(SchemaField.BYTES_SYNTHETIC_FIELD) != null) {
            fieldNode.appendChild(doc.createElement(SYNTHETIC_BYTES));
        }
        if (f.getSyntheticField(SchemaField.CLASS_SYNTHETIC_FIELD) != null) {
            fieldNode.appendChild(doc.createElement(SYNTHETIC_CLASS));
        }
        if (f.getSyntheticField(SchemaField.NULL_SYNTHETIC_FIELD) != null) {
            fieldNode.appendChild(doc.createElement(SYNTHETIC_NULL));
        }
        if (f.getSyntheticField(SchemaField.NULL_ELEMENTS_SYNTHETIC_FIELD) != null) {
            fieldNode.appendChild(doc.createElement(SYNTHETIC_NULL_ELEMENTS));
        }
        return fieldNode;
    }

    public void addSchemaEdit(Document doc, SchemaEditCollection editSet) {
        doc.getChildNodes();
        Iterator edits = editSet.iterator();
        while (edits.hasNext()) {
            SchemaEdit edit = (SchemaEdit)edits.next();
            if (edit instanceof AddFieldEdit) {
//				SchemaField f = ((SchemaEdit.AddField)edit).getField();
//				createFieldNode(super.getClassNode(doc, f.getSchemaClass().getName()),f);
            } else if (edit instanceof AddSuperClassEdit) {
            } else if (edit instanceof RemoveFieldEdit) {
            } else if (edit instanceof RemoveSuperClassEdit) {
            } else if (edit instanceof UpdateAuxInfoClassEdit) {
            } else if (edit instanceof UpdateAuxInfoFieldEdit) {
            } else if (edit instanceof RenameClassEdit) {
            } else if (edit instanceof RenameFieldEdit) {
            } else {
            }
        }
    }

    /**
     * Writes a document to given output.
     *
     * @param doc * @param out
     * @author ppoddar
     * @since 1.0
     */
    public void write(Document doc, OutputStream out) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            try {
                transformer.setOutputProperty("indent", "yes");
                transformer.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "4");
            } catch (RuntimeException e1) {
                e1.printStackTrace();
                transformer.getOutputProperties().list(System.out);
            }
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
