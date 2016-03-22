
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
package com.versant.core.jdo.tools.workbench.forms;

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import java.awt.*;

/**
 * View/edit properties for a class.
 */
public class ClassPropertyForm extends WorkbenchPanel {

    private JDBCClassPropertyForm jdbcForm;
    private VDSClassPropertyForm vdsForm;
    private int type;

    public ClassPropertyForm() throws Exception {
        setLayout(new BorderLayout());
    }

    public void setMdClass(MdClass mdClass) throws Exception {
        if (mdClass != null) {
            int type = getType(mdClass);
            if (type == MdDataStore.TYPE_JDBC) {
                JDBCClassPropertyForm jdbcForm = getJdbcForm();
                jdbcForm.setMdClass(mdClass);
                if (this.type != type) {
                    removeAll();
                    jdbcForm.setTitle(null);
                    add(jdbcForm, BorderLayout.CENTER);
                }
            } else if (type == MdDataStore.TYPE_VDS) {
                VDSClassPropertyForm vdsForm = getVdsForm();
                vdsForm.setMdClass(mdClass);
                if (this.type != type) {
                    removeAll();
                    vdsForm.setTitle(null);
                    add(vdsForm, BorderLayout.CENTER);
                }
            }
            this.type = type;
        } else {
            removeAll();
        }
    }

    private int getType(MdClass mdClass) {
        MdDataStore ds = mdClass.getMdDataStore();
        int type = ds.getType();
        return type;
    }

    private JDBCClassPropertyForm getJdbcForm() throws Exception {
        if (jdbcForm == null) {
            jdbcForm = new JDBCClassPropertyForm();
        }
        return jdbcForm;
    }

    private VDSClassPropertyForm getVdsForm() throws Exception {
        if (vdsForm == null) {
            vdsForm = new VDSClassPropertyForm();
        }
        return vdsForm;
    }

    public void projectChanged(MdProjectEvent ev) {
        switch (ev.getFlags()) {
            case MdProjectEvent.ID_ENGINE_STARTED:
            case MdProjectEvent.ID_ENGINE_STOPPED:
            case MdProjectEvent.ID_PARSED_DATABASE:
            case MdProjectEvent.ID_DIRTY_FLAG:
            case MdProjectEvent.ID_CLASSES_REMOVED:
//            case MdProjectEvent.ID_PARSED_META_DATA:
//            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                return;
        }
        if (jdbcForm != null) {
            jdbcForm.updateSetup();
        }
        if (vdsForm != null) {
            vdsForm.updateSetup();
        }
    }
}
