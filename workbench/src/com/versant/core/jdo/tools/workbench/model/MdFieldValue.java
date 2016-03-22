
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

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.metadata.JdbcRefField;

import java.util.*;

/**
 * A value that is the name of a field of a class. The picklist contains all
 * the JDO managable fields of the class (excluding primary key fields) and
 * its superclasses.
 */
public class MdFieldValue extends MdValue {

    protected MdClass mdClass;

    public MdFieldValue(String text, MdClass mdClass, String type, boolean col) {
        super(text);
        this.mdClass = mdClass;
        initPickList(mdClass, type, col);
    }

    private void initPickList(MdClass mdClass, String type, boolean col) {
        if (mdClass == null) {
            setPickList(PickLists.EMPTY);
        } else {
            ArrayList a = new ArrayList();
            for (MdClass c = mdClass;
                 c != null; c = c.getPcSuperclassMdClass()) {
                List l = c.getFieldList();
                int n = l.size();
                for (int i = 0; i < n; i++) {
                    MdField f = (MdField)l.get(i);
                    if (f instanceof MdSpecialField) continue;
                    if (f.isPrimaryKeyField()) continue;
                    String t = f.getTypeStr();
                    if (type != null && t.equals(type)) {
                        a.add(f.getName());
                    } else if (col) {
                        switch (f.category) {
                            case MDStatics.CATEGORY_ARRAY:
                                if (f.isEmbedded()) break;
                            case MDStatics.CATEGORY_COLLECTION:
                                a.add(f.getName());
                        }
                    }
                }
            }
            Collections.sort(a);
            setPickList(a);
        }
    }
}

