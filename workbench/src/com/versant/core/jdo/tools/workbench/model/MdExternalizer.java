
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

import com.versant.core.jdo.externalizer.SerializedExternalizer;

import java.util.Map;
import java.util.HashMap;

/**
 * Info on an Externalizer.
 */
public class MdExternalizer {

    private String typeName;
    private boolean enabled;
    private String externalizerName;
    private Map args = new HashMap(17);

    public MdExternalizer() {
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void clear(){
        args.clear();
    }

    public Map getArgs() {
        return args;
    }

    public void setArgs(Map args) {
        this.args = args;
    }

    public MdValue getExternalizer() {
        MdValue v = new MdClassNameValue(externalizerName);
        v.setPickList(PickLists.EXTERNALIZER);
        v.setOnlyFromPickList(false);
        v.setDefText(SerializedExternalizer.SHORT_NAME);
        return v;
    }

    public void setExternalizer(MdValue v) {
        setExternalizerStr(v.getText());
    }

    public String getExternalizerStr() {
        return externalizerName;
    }

    public void setExternalizerStr(String s ) {
        this.externalizerName = s;
    }

    public String toString() {
        return typeName + "=" + externalizerName;
    }

}

