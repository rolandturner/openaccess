
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

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.filter.Filter;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import com.versant.core.metadata.parser.MetaDataParser;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;

/**
 * Utility methods for working with JDom jdo meta data documents.
 */
public class XmlUtils {

    private XmlUtils() {
    }

    public static final Format XML_FORMAT = Format.getPrettyFormat().setIndent(
            "    ");

    public static boolean isEmpty(Element e) {
        if (e.getName().equals("extension")) {
            if (!e.getChildren().isEmpty()) return false;
            List attr = e.getAttributes();
            return attr.size() <= 2;
        } else {
            return e.getAttributes().isEmpty() && e.getChildren().isEmpty();
        }
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Change the Element's Document dirty flag. If the element has no
     * document then its virtual parent is checked and so on recursively.
     */
    public static void makeDirty(MdElement e) {
        Document d = e.getDocument();
        if (d == null) {
            MdElement vp = e.getVirtualParent();
            if (vp != null) makeDirty(vp);
        } else {
            //System.out.println("XmlUtils.makeDirty " + e + " doc " + d);
            ((MdDocument)d).setDirty(true);
        }
    }

    /**
     * Find the extension element of base with key or null if none.
     */
    public static MdElement findExtension(Element base, int key) {
        return findExtension(base,  JdoExtension.toKeyString(key));
    }

    /**
     * Find the extension element of base with key or null if none.
     */
    public static MdElement findExtension(Element base, String key) {
        List clist = base.getChildren("extension");
        for (Iterator i = clist.iterator(); i.hasNext();) {
            Element e = (Element)i.next();
            String vn = e.getAttributeValue("vendor-name");
            if (vn == null || !MetaDataParser.isOurVendorName(vn)) continue;
            String k = e.getAttributeValue("key");
            if (k != null && k.equals(key)) return (MdElement)e;
        }
        return null;
    }

    /**
     * Find the extension element of base with key or create one it none.
     * The created element is NOT added to base but its virtualParent is
     * set to base so if it gains any content it will be added.
     */
    public static MdElement findOrCreateExtension(MdElement base, int key) {
        MdElement e = findExtension(base, key);
        if (e == null) e = createExtension(key, base);
        return e;
    }

    public static MdElement findOrCreateExtension(MdElement base, String key) {
        MdElement e = findExtension(base, key);
        if (e == null) e = createExtension(key, base);
        return e;
    }

    /**
     * Find all the extension elements of base with key or null if none.
     */
    public static List findExtensions(Element base, int key) {
        String ks = JdoExtension.toKeyString(key);
        List clist = base.getChildren("extension");
        ArrayList a = new ArrayList();
        for (Iterator i = clist.iterator(); i.hasNext();) {
            Element e = (Element)i.next();
            String vn = e.getAttributeValue("vendor-name");
            if (vn == null || !MetaDataParser.isOurVendorName(vn)) continue;
            String k = e.getAttributeValue("key");
            if (k != null && k.equals(ks)) a.add(e);
        }
        return a;
    }

    /**
     * Create an extension element with key.
     */
    public static MdElement createExtension(int key, MdElement virtualParent) {
        String keyValue = JdoExtension.toKeyString(key);
        return createExtension(keyValue, virtualParent);
    }

    private static MdElement createExtension(String keyValue, MdElement virtualParent) {
        MdElement e = new MdElement("extension");
        e.setAttribute("vendor-name", MetaDataParser.VENDOR_NAME);
        e.setAttribute("key", keyValue);
        e.setVirtualParent(virtualParent);
        return e;
    }

    /**
     * Create an extension element with key and value.
     */
    public static MdElement createExtension(int key, String value,
            MdElement virtualParent) {
        return createExtension(key, value, virtualParent, false);
    }

    /**
     * Create an extension element with key and value. The extension will
     * be considered empty if it has no children even if it has a value.
     */
    public static MdElement createExtension(int key, String value,
            MdElement virtualParent, boolean emptyIfExtValue) {
        MdElement e = createExtension(key, virtualParent);
        e.setEmptyIfExtValue(emptyIfExtValue);
        e.setAttribute("value", value);
        return e;
    }

    /**
     * Get the value of an extension. This is null if the extension does not
     * exist.
     */
    public static String getExtension(Element base, int key) {
        Element e = XmlUtils.findExtension(base, key);
        if (e == null) return null;
        return e.getAttributeValue("value");
    }

    /**
     * Get the value of a boolean extension. This is true if the extension
     * exists but the value is null. This is null if the extension does not
     * exist.
     public static String getBoolExtension(Element base, int key) {
     Element e = XmlUtils.findExtension(base, key);
     if (e == null) return null;
     String v = e.getAttributeValue("value");
     return v == null ? "true" : v;
     }
     */

    /**
     * Get the value of an extension of an extension. This is null if either
     * extension does not exist.
     */
    public static String getExtension(Element base, int key, int key2) {
        Element e = XmlUtils.findExtension(base, key);
        if (e == null) return null;
        e = XmlUtils.findExtension(e, key2);
        if (e == null) return null;
        return e.getAttributeValue("value");
    }

    /**
     * Set the value of an extension. The extension is removed if the value
     * is null or zero length.
     */
    public static void setExtension(MdElement base, int key, String value) {
        Element e = XmlUtils.findExtension(base, key);
        if (isEmpty(value)) {
            if (e != null) {
                base.removeContent(e);
            } else {
                return;
            }
        } else {
            if (e == null) {
                e = XmlUtils.createExtension(key, base);
                base.addContent(e);
            }
            e.setAttribute("value", value);
        }
        makeDirty(base);
    }

    /**
     * Print e to System.out.
     */
    public static String toString(Element e) {
        if (e == null) return "null";
        XMLOutputter outputter = new XMLOutputter(XmlUtils.XML_FORMAT);
        return "\n" + outputter.outputString(e);
    }

    /**
     * Set the value of an extension of an extension. The extension is
     * removed if the value is null or zero length.
     */
    public static void setExtension(MdElement base, int key, int key2,
            String value) {
        MdElement e = findExtension(base, key);
        if (isEmpty(value)) {
            if (e != null) {
                setExtension(e, key2, value);
                if (isEmpty(e)) base.removeContent(e);
            }
        } else {
            if (e == null) {
                e = createExtension(key, base);
                base.addContent(e);
            }
            setExtension(e, key2, value);
        }
        makeDirty(base);
    }

    /**
     * Set the value of a boolean extension. The extension is removed if the
     * value is null or zero length or 'false'. If the value is true then
     * the key is left empty.
     public static void setBoolExtension(Element base, int key, String value) {
     Element e = XmlUtils.findExtension(base, key);
     if (isFalse(value)) {
     if (e != null) base.removeContent(e);
     else return;
     } else {
     if (e == null) {
     e = XmlUtils.createExtension(key);
     base.addContent(e);
     }
     if (value.equals("true")) e.removeAttribute("value");
     else e.setAttribute("value", value);
     }
     makeDirty(base);
     }
     */

    /**
     * Set the value of an attribute. If the value is null or empty then the
     * attribute is removed.
     */
    public static void setAttribute(MdElement base, String name, String value) {
        if (isEmpty(value)) {
            base.removeAttribute(name);
        } else {
            base.setAttribute(name, value);
        }
        makeDirty(base);
    }

    /**
     * Remove all extensions for key.
     */
    public static void removeExtensions(MdElement base, int key) {
        List l = findExtensions(base, key);
        if (l == null || l.isEmpty()) return;
        for (int i = l.size() - 1; i >= 0; i--) {
            base.removeContent((Element)l.get(i));
        }
        makeDirty(base);
    }

    /**
     * Remove all extensions for key from root down.
     */
    public static void removeAllExtensions(MdElement root, int key) {
        removeExtensions(root, key);
        for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
            removeAllExtensions((MdElement)i.next(), key);
        }
        makeDirty(root);
    }

    /**
     * Get the value of an attribute of a nested element.
     */
    public static String getAttribute(Element base, String nested, String name) {
        Element e = base.getChild(nested);
        if (e == null) return null;
        return e.getAttributeValue(name);
    }

    /**
     * Set the value of an attribute of a nested element. If the value is
     * null or empty then the attribute is removed. If the nested element
     * does not exist it is created. If the attribute is removed and the
     * nested element now has no attributes and no children it is removed.
     */
    public static void setAttribute(MdElement base, String nested, String name,
            String value) {
        boolean nv = isEmpty(value);
        Element e = base.getChild(nested);
        if (e == null) {
            if (nv) return;
            e = new MdElement(nested);
            base.addContent(e);
        }
        if (nv) {
            e.removeAttribute(name);
            if (isEmpty(e)) base.removeContent(e);
        } else {
            e.setAttribute(name, value);
        }
        makeDirty(base);
    }

    /**
     * Set the value of text of a nested element. If the value is
     * null or empty then the nested is removed. If the nested element
     * does not exist it is created. If the text is removed and the
     * nested element now has no attributes and no children it is removed.
     */
    public static void setText(MdElement base, String nested, String value) {
        boolean nv = isEmpty(value);
        Element e = base.getChild(nested);
        if (e == null) {
            if (nv) {
                return;
            } else {
                e = new MdElement(nested);
                base.addContent(e);
            }
        }
        if (nv) {
            e.setText(null);
            if (isEmpty(e)) {
                base.removeContent(e);
            }
        } else {
            e.setContent(new CDATA(value));
        }
        makeDirty(base);
    }

    /**
     * Get the text of a nested element.
     */
    public static String getText(MdElement base, String nested) {
        Element e = base.getChild(nested);
        if (e != null) {
            return e.getText();
        }
        return null;
    }

    /**
     * Set a comment of text on a element.
     */
    public static void setComment(MdElement base, String comment) {
        boolean empty = isEmpty(comment);
        if (empty) {
            removeComments(base);
            makeDirty(base);
        } else {
            removeComments(base);
            base.addContent(new Comment(comment));
            makeDirty(base);
        }

    }

    /**
     * remove all comment on a element.
     */
    public static void removeComments(MdElement base) {
        base.removeContent(new Filter() {
            public boolean matches(Object o) {
                if (o instanceof Comment) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Get the comment of a element.
     */
    public static String getComment(MdElement base) {
        if (base != null) {
            List comments = base.getContent(new Filter() {
                public boolean matches(Object o) {
                    if (o instanceof Comment) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            StringBuffer buffer = new StringBuffer();
            for (Iterator iter = comments.iterator(); iter.hasNext();) {
                Comment comment = (Comment)iter.next();
                buffer.append(comment.getText());
                if (iter.hasNext()) {
                    buffer.append("\n");
                }
            }
            String comment = buffer.toString();
            if (isEmpty(comment)) {
                return null;
            } else {
                return comment;
            }

        }
        return null;
    }

    /**
     * Get the value of an extension nested inside another element. This is
     * null if the extension does not exist.
     */
    public static String getExtension(Element base, String nested, int key) {
        Element e = base.getChild(nested);
        if (e == null) return null;
        return getExtension(e, key);
    }

    /**
     * Set the value of an extension nested inside another element. The
     * extension is removed if the value is null or zero length. If the
     * extension is removed and the nested element now has no attributes
     * and no children it is removed.
     */
    public static void setExtension(MdElement base, String nested, int key,
            String value) {
        MdElement e = (MdElement)base.getChild(nested);
        if (isEmpty(value)) {
            if (e != null) {
                setExtension(e, key, value);
                if (isEmpty(e)) base.removeContent(e);
            }
        } else {
            if (e == null) {
                e = new MdElement(nested);
                base.addContent(e);
            }
            setExtension(e, key, value);
        }
        makeDirty(base);
    }

    /**
     * Get the value of a boolean extension nested inside another element.
     * This is true if the extension exists but the value is null. This is
     * false if the extension does not exist.
     public static String getBoolExtension(Element base, String nested, int key) {
     Element e = base.getChild(nested);
     if (e == null) return null;
     return getBoolExtension(e, key);
     }
     */

    /**
     * Set the value of a boolean extension nested inside another element. The
     * extension is removed if the value is null or zero length or 'false'.
     * If the value is true then the key is left empty. If the extension is
     * removed and the nested element now has no attributes and no children it
     * is removed.
     public static void setBoolExtension(Element base, String nested, int key,
     String value) {
     Element e = base.getChild(nested);
     if (isFalse(value)) {
     if (e != null) {
     setBoolExtension(e, key, value);
     if (isEmpty(e)) base.removeContent(e);
     }
     } else {
     if (e == null) {
     e = new Element(nested);
     base.addContent(e);
     }
     setBoolExtension(e, key, value);
     }
     makeDirty(base);
     }
     */

    /**
     * Get the value of an extension nested inside an extension in another
     * element. This is null if the extension does not exist.
     */
    public static String getExtension(Element base, String nested, int key,
            int key2) {
        Element e = base.getChild(nested);
        if (e == null) return null;
        return getExtension(e, key, key2);
    }

    public static String getExtension(Element base, int nested, int key,
            int key2) {
        Element e = findExtension(base, nested);
        if (e == null) return null;
        return getExtension(e, key, key2);
    }

    /**
     * Set the value of an extension nested inside an extension in another
     * element. The extension is removed if the value is null or zero length.
     * If the extension is removed and the nested element now has no attributes
     * and no children it is removed.
     */
    public static void setExtension(MdElement base, String nested, int key,
            int key2, String value) {
        MdElement e = (MdElement)base.getChild(nested);
        if (isEmpty(value)) {
            if (e != null) {
                setExtension(e, key, key2, value);
                if (isEmpty(e)) base.removeContent(e);
            }
        } else {
            if (e == null) {
                e = new MdElement(nested);
                base.addContent(e);
            }
            setExtension(e, key, key2, value);
        }
        makeDirty(base);
    }

    public static void setExtension(MdElement base, int key, int key2,
            int key3, String value) {
        MdElement e = findExtension(base, key);
        if (isEmpty(value)) {
            if (e != null) {
                setExtension(e, key2, key3, value);
                if (isEmpty(e)) base.removeContent(e);
            }
        } else {
            if (e == null) {
                e = new MdElement(JdoExtension.toKeyString(key));
                base.addContent(e);
            }
            setExtension(e, key2, key3, value);
        }
        makeDirty(base);
    }

    /**
     * Find the jdbc-column child of container to match selectedDB and return
     * an MdColumn wrapper for it or null if none.
     */
    public static MdColumn findCol(MdElement container, String selectedDB) {
        return new MdColumn(findOrCreateColElement(container, selectedDB));
    }

    /**
     * Find the jdbc-column child of container to match selectedDB. If there
     * is none then one is created with its virtualParent set to container.
     */
    public static MdElement findOrCreateColElement(MdElement container,
            String selectedDB) {
        MdElement ans = null;
        List l = findExtensions(container, JdoExtensionKeys.JDBC_COLUMN);
        if (l != null) {
            Element all = null;
            Element match = null;
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element)i.next();
                String edb = e.getAttributeValue("value");
                if (edb == null) {
                    all = e;
                } else if (selectedDB != null && edb.equals(selectedDB)) {
                    match = e;
                    break;
                }
            }
            if (match != null) {
                ans = (MdElement)match;
            } else if (all != null) ans = (MdElement)all;
        }
        if (ans == null) {
            ans = createExtension(JdoExtensionKeys.JDBC_COLUMN, container);
        }
        return ans;
    }

    /**
     * Recursively rename extensions from root down. This will also replace
     * the old jdogenie vendor-name with versant.
     */
    public static void renameExtension(Element root, int find, int replace) {
        renameExtension(root, JdoExtension.toKeyString(find),
                JdoExtension.toKeyString(replace));
    }

    private static void renameExtension(Element root, String find,
            String replace) {
        if (root.getName().equals("extension")) {
            String vn = root.getAttributeValue("vendor-name");
            if (MetaDataParser.isOurVendorName(vn)) {
                if (MetaDataParser.VENDOR_NAME_JDOGENIE.equals(vn)) {
                    root.setAttribute("vendor-name", MetaDataParser.VENDOR_NAME);
                }
                String k = root.getAttributeValue("key");
                if (k != null && k.equals(find)) {
                    root.setAttribute("key", replace);
                }
            }
        }
        for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof Element) {
                renameExtension((Element)o, find, replace);
            }
        }
    }
}

