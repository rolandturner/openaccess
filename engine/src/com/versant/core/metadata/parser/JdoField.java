
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

import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.MDStatics;
import com.versant.core.common.Debug;
import com.versant.core.common.BindingSupportImpl;

import java.io.PrintStream;
import java.util.*;

/**
 * Field element from a .jdo file.
 */
public final class JdoField extends JdoElement {
    private final static Set VENDOR_IGNORE_SET = new HashSet();

    public String name;
    public String origName;
    public int persistenceModifier;
    public boolean primaryKey;
    public int nullValue;
    public int defaultFetchGroup;
    public int embedded;

    public String temporal = "NONE";

    /**
     * Temp list to hold extensions
     */
    public ArrayList extensionList = new ArrayList();

    public List embeddedFields;
    public JdoField parentField;
    public boolean nullIndicator;

    public JdoCollection collection;
    public JdoMap map;
    public JdoArray array;

    public JdoExtension[] extensions;

    public JdoClass parent;
    public int cascadeType;

    public JdoField() {
        VENDOR_IGNORE_SET.add(JdoExtension.toKeyString(JdoExtensionKeys.COLLECTION));
        VENDOR_IGNORE_SET.add(JdoExtension.toKeyString(JdoExtensionKeys.EMBEDDED));
        VENDOR_IGNORE_SET.add(JdoExtension.toKeyString(JdoExtensionKeys.DEFAULT_FETCH_GROUP));
        VENDOR_IGNORE_SET.add(JdoExtension.toKeyString(JdoExtensionKeys.MAP));
        VENDOR_IGNORE_SET.add(JdoExtension.toKeyString(JdoExtensionKeys.ARRAY));
    }

    public JdoField createCopy(JdoField parentField, JdoClass parentClass) {
        JdoField jf = new JdoField(parentField);
        jf.name = name;
        jf.persistenceModifier = persistenceModifier;
        jf.primaryKey = primaryKey;
        jf.nullValue = nullValue;
        jf.defaultFetchGroup = defaultFetchGroup;
        jf.embedded = embedded;

        jf.parent = parentClass;
        if (collection != null) jf.collection = collection.createCopy(jf);
        if (map != null) jf.map = map.createCopy(jf);
        if (array != null) jf.array = array.createCopy(jf);

        if (extensions != null) {
            jf.extensions = new JdoExtension[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                jf.extensions[i] = extensions[i].createCopy(jf);
            }
        }
        return jf;
    }

    public JdoField createCopy(JdoField parentField) {
        return createCopy(parentField, null);
    }

    public JdoField createCopy(JdoClass parentClass) {
        return createCopy(null, parentClass);
    }

    public JdoField(JdoField parentField) {
        this.parentField = parentField;
        if (parentField != null) parentField.addEmbeddedField(this);
    }

    public JdoElement getParent() { return parent; }

    public void addEmbeddedField(JdoField embeddedField) {
        if (embeddedFields == null) embeddedFields = new ArrayList();
        embeddedFields.add(embeddedField);
    }

	public boolean isExternalized() {
        if (extensions != null){
            for (int i = 0; i < extensions.length; i++) {
                if (extensions[i].key==JdoExtensionKeys.EXTERNALIZER) {
                    return true;
                }
            }
        }
		return false;
	}


    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public String getSubContext() {
        return "field[" + name + "]";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("field[");
        s.append(name);
        s.append("] persistenceModifier=");
        s.append(MDStaticUtils.toPersistenceModifierString(persistenceModifier));
        s.append(" primaryKey=");
        s.append(primaryKey);
        s.append(" nullValue=");
        s.append(MDStaticUtils.toNullValueString(nullValue));
        s.append(" defaultFetchGroup=");
        s.append(MDStaticUtils.toTriStateString(defaultFetchGroup));
        s.append(" embedded=");
        s.append(MDStaticUtils.toTriStateString(embedded));
        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        if (collection != null) collection.dump(out, is);
        if (map != null) map.dump(out, is);
        if (array != null) array.dump(out, is);
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                extensions[i].dump(out, is);
            }
        }
    }

    /**
     * Update this with non conflicting metadat from the supplied instance.
     */
    public void synchWith(JdoField updateFrom, Set exclude, boolean errorOnExclude) {
        if (persistenceModifier == MDStatics.NOT_SET) {
            persistenceModifier = updateFrom.persistenceModifier;
        }
        if (embedded == MDStatics.NOT_SET) {
            embedded = updateFrom.embedded;
        }
        if (defaultFetchGroup == MDStatics.NOT_SET) {
            defaultFetchGroup = updateFrom.defaultFetchGroup;
        }
        if (nullValue == MDStatics.NOT_SET) {
            nullValue = updateFrom.nullValue;
        }

        if (collection != null) {
            if (updateFrom.collection == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Not allowed to change the type of field '"
                        + updateFrom.name + "' to a 'collection'");
            } else {
                collection.synchronizeForHorizontal(updateFrom.collection);
            }
        } else if (updateFrom.collection != null) {
            collection = updateFrom.collection.createCopy(this);
        }

        if (map != null) {
            if (updateFrom.map == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Not allowed to change the type of field '"
                        + updateFrom.name + "' to a 'map'");
            } else {
                map.synchronizeForHorizontal(updateFrom.map);
            }
        } else if (updateFrom.map != null) {
            map = updateFrom.map.createCopy(this);
        }

        if (array != null) {
            if (updateFrom.array == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Not allowed to change the type of field '"
                        + updateFrom.name + "' to a 'map'");
            } else {
                array.synchronizeForHorizontal(updateFrom.array);
            }
        } else if (updateFrom.array != null) {
            array = updateFrom.array.createCopy(this);
        }

        if (updateFrom.extensions != null) {
            JdoExtension[] copy = updateFrom.getExtensionCopy(this);
            JdoExtension.synchronize3(extensions, copy, exclude, errorOnExclude);
            extensions = copy;
        }
    }

    public JdoExtension[] getExtensionCopy(JdoField owner) {
        if (extensions == null) return null;
        JdoExtension[] copy = new JdoExtension[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            copy[i] = extensions[i].createCopy(owner);
        }
        return copy;
    }

    private void processAttributeExtensions(JdoExtension exts[]) {
        if (exts == null) return;
        for (int i = 0; i < exts.length; i++) {
            JdoExtension extension = exts[i];
            if (extension.isFieldAttribute()) {
                if (extension.key == JdoExtensionKeys.DEFAULT_FETCH_GROUP) {
                    String value = extension.value;
                    if ("true".equals(value)) {
                        defaultFetchGroup = JdoField.TRUE;
                    } else if ("false".equals(value)) {
                        defaultFetchGroup = JdoField.FALSE;
                    }
                } else if (extension.key == JdoExtensionKeys.EMBEDDED) {
                    String value = extension.value;
                    if ("true".equals(value)) {
                        embedded = JdoField.TRUE;
                    } else if ("false".equals(value)) {
                        embedded = JdoField.FALSE;
                    }
                }
            }
        }
    }

    /**
     *
     * @param nested
     */
    public void applyEmbeddedExtensions(JdoExtension[] nested) {
        /**
         * We don't want the jdbc table/names that is specified in the metadata
         * of the embedded instance.
         */
        clearAllJdbcColumnNames();  //todo why is this cleared?

        processAttributeExtensions(nested);
        processVendorExtensions(nested);
        processCollectionExtensions(nested);
        processMapExtensions(nested);
        processArrayExtensions(nested);

    }

    private void clearAllJdbcColumnNames() {
        if (extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_COLUMN_NAME, 3, extensions);
        }
        if (collection != null && collection.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_COLUMN_NAME, 3, collection.extensions);
        }
        if (collection != null && collection.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_TABLE_NAME, 3, collection.extensions);
        }
        if (array != null && array.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_COLUMN_NAME, 3, array.extensions);
        }
        if (array != null && array.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_TABLE_NAME, 3, array.extensions);
        }
        if (map != null && map.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_COLUMN_NAME, 3, map.extensions);
        }
        if (map != null && map.extensions != null) {
            JdoExtension.clearKey(JdoExtensionKeys.JDBC_TABLE_NAME, 3, map.extensions);
        }
    }

    private void processVendorExtensions(JdoExtension[] nested) {
        extensions = JdoExtension.synchronize4(nested, extensions, VENDOR_IGNORE_SET);
        if (extensions != null) {
            JdoExtension ext = JdoExtension.find(JdoExtensionKeys.NULL_INDICATOR, extensions);
            if (ext != null) {
                nullIndicator = true;
            }
        }
    }

    private void processArrayExtensions(JdoExtension[] nested) {
        if (array == null) return;
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.ARRAY, nested);
        if (ext != null && ext.nested != null) {
            array.extensions = JdoExtension.synchronize4(ext.nested, array.extensions, Collections.EMPTY_SET);
        }
    }

    private void processMapExtensions(JdoExtension[] nested) {
        if (map == null) return;
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.MAP, nested);
        if (ext != null && ext.nested != null) {
            checkForTypeOverride(ext.nested, JdoExtensionKeys.VALUE_TYPE, map.valueType);
            checkForTypeOverride(ext.nested, JdoExtensionKeys.KEY_TYPE, map.keyType);
            map.extensions = JdoExtension.synchronize4(ext.nested, map.extensions, Collections.EMPTY_SET);
        }
    }

    private void processCollectionExtensions(JdoExtension[] nested) {
        if (collection == null) return;
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.COLLECTION, nested);
        if (ext != null && ext.nested != null) {
            if (ext.nested == null) return;
            checkForTypeOverride(ext.nested, JdoExtensionKeys.ELEMENT_TYPE, collection.elementType);
            collection.extensions = JdoExtension.synchronize4(ext.nested, collection.extensions, Collections.EMPTY_SET);
        }
    }

    private void checkForTypeOverride(JdoExtension[] exts, int key, String value) {
        //may not specify a different element type
        JdoExtension typeExt = JdoExtension.find(key, exts);
        if (typeExt != null && !value.equals(typeExt.value)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Key/Values types for Collection/Maps may not be changed in embedded fields");
        }
    }

    /**
     * Create the extension if it does not exist, else update it if overwrite is true
     * @param key
     * @param value
     * @param overwrite
     */
    public JdoExtension findCreate(int key, String value, boolean overwrite) {
        if (extensions == null) {
            extensions = new JdoExtension[] {createChild(key, value, this)};
            return extensions[0];
        }

        JdoExtension ext = JdoExtension.find(key, extensions);
        if (ext == null) {
            extensions = addExtension(extensions, (ext = createChild(key, value, this)));
        } else if (overwrite) {
            ext.value = value;
        }
        return ext;
    }
    
}

