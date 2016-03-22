
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

import com.versant.core.jdbc.metadata.JdbcRefField;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;

/**
 *
 */
public class FakeOne2ManyField extends MdField 

{

    MdField parentCol;

    public FakeOne2ManyField(MdField parentCol) {
        this.parentCol = parentCol;
        FieldMetaData fmd = parentCol.fmd;
        if (fmd != null) {
            FieldMetaData ifmd = fmd.inverseFieldMetaData;
            if (ifmd != null) {
                this.jdbcField = (JdbcField)ifmd.storeField;
            }
        }
        if (jdbcField == null) {
            jdbcField = new JdbcRefField();
        }
        category = MDStatics.CATEGORY_REF;
        refClass = parentCol.mdClass;
        MdElement colElement = parentCol.getCollectionArrayOrMapElement();
        if (colElement != null) {
            MdElement invElement = XmlUtils.findOrCreateExtension(colElement,
                    JdoExtensionKeys.INVERSE);
            this.element = invElement;
        }
    }

    public MdClass getMdClass() {
        return parentCol.getElementTypeMdClass();
    }

    public boolean isCompositePkRef() {
        return parentCol.mdClass.isCompositePrimaryKey();
    }

    public String getName() {
        return FieldMetaData.NO_FIELD_TEXT;
    }

    public String getMiniQName() {
        return "inverse FK column";
    }

    public String getMiniClassDef() {
        return "[no field]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FakeOne2ManyField)) return false;

        final FakeOne2ManyField fakeOne2ManyField = (FakeOne2ManyField)o;
        if (element != null ? !element.equals(fakeOne2ManyField.element) : fakeOne2ManyField.element != null) return false;

        return true;
    }

    public int hashCode() {
        return (element != null ? element.hashCode() : 0);
    }

}
