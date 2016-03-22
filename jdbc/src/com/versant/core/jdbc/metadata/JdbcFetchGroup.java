
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
package com.versant.core.jdbc.metadata;

import com.versant.core.metadata.StoreFetchGroup;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.FetchGroupField;

/**
 * Extra JDBC specific info attached to a FetchGroup.
 */
public class JdbcFetchGroup implements StoreFetchGroup {

    public static int OPT_INC_SUBS = 1;
    public static int OPT_START_OUTER = 2;

    private FetchGroup fg;
//    private FgDs[] fgDses = new FgDs[4];

    public JdbcFetchGroup() {
    }

    public void setFetchGroup(FetchGroup fg) {
        this.fg = fg;
    }

//    public FgDs getFgDs(boolean incSubClasses, boolean outer) {
//        int opts = createOpts(incSubClasses, outer);
//        FgDs fgDs = fgDses[opts];
//        if (fgDs == null) {
//            fgDs = new FgDs(null, fg, fg.name, opts);
//        }
//        return fgDs;
//    }

//    public FgDs getExistingFgDs(boolean incSubClasses, boolean outer) {
//        int opts = createOpts(incSubClasses, outer);
//        FgDs fgDs = fgDses[opts];
//        if (fgDs == null) {
//            StringBuffer msg = new StringBuffer();
//            msg.append("FgDs does not exist: " + fg.classMetaData.qname + "." +
//                    fg.name + " incSubClasses " + incSubClasses + " outer " +
//                    outer);
//            msg.append("\nincSubClasses outer FgDs");
//            msg.append("\nfalse         false " + fgDses[createOpts(false, false)]);
//            msg.append("\nfalse         true  " + fgDses[createOpts(false, true)]);
//            msg.append("\ntrue          false " + fgDses[createOpts(true, false)]);
//            msg.append("\ntrue          true  " + fgDses[createOpts(true, true)] + "\n");
//            throw BindingSupportImpl.getInstance().internal(msg.toString());
//        }
//        return fgDs;
//    }

    public static int createOpts(boolean incSubClasses, boolean outer) {
        int opt = 0;
        if (incSubClasses) opt += OPT_INC_SUBS;
        if (outer) opt += OPT_START_OUTER;
        return opt;
    }

//    public void setFgDs(FgDs fgDs) {
//        if (!fgDs.isFinished()) {
//            throw BindingSupportImpl.getInstance().internal(
//                    "The FetchPlan has not been finished");
//        }
//        fgDses[fgDs.getOpts()] = fgDs;
//    }

    public void fieldAdded(FieldMetaData fmd) {
        JdbcField jdbcField = ((JdbcField)fmd.storeField);
        if (jdbcField.mainTableCols != null) {
            fg.jdbcTotalCols += jdbcField.mainTableCols.length;
        }
    }

    public void finish() {
        FetchGroupField[] fields = fg.fields;
        int nf = fields.length;
        for (int i = nf - 1; i >= 0; i--) {
            //select a default crossjoinField
            if (fg.crossJoinedCollectionField == null
                    && fields[i].fmd.storeField instanceof JdbcCollectionField
                    && !(fields[i].fmd.storeField instanceof JdbcMapField)) {
                fg.crossJoinedCollectionField = fields[i];
            }
        }
    }

}

