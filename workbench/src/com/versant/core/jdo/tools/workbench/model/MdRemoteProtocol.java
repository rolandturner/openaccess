
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

import com.versant.core.util.PropertiesLoader;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.common.BindingSupportImpl;

import java.util.List;
import java.util.Properties;
import java.awt.*;

/**
 * Settings for a remote protocol e.g. http, sockets etc.
 */
public class MdRemoteProtocol {

    private MdProject project;
    private String protocol;
    private MdStructSet fields;
    private String error;
    private boolean exportOnStartup;

    public MdRemoteProtocol(MdProject project, String protocol,
            Properties props) {
        this.project = project;
        this.protocol = protocol;
        fields = new MdStructSet(project);
        try {
            init(props);
        } catch (Exception e) {
            error = "<html><font color=\"red\">" + e.toString() + "</font></html>";
            project.getLogger().error(e);
        }
    }

    private void init(Properties props) throws Exception {
             	
        MdClassLoader loader = project.getProjectClassLoader();
        Properties p = PropertiesLoader.loadProperties(loader,
                "openaccess-remote", protocol);
        String clsName = p.getProperty("config");
        if (clsName == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "config property not found in resource " +
                    p.getProperty(PropertiesLoader.RES_NAME_PROP));
        }
        Class cls = ClassHelper.get().classForName(clsName, true, loader);
        fields.init(cls.newInstance(), props);
             	
    }

    public String getProtocolStr() {
        return protocol;
    }

    public MdValue getProtocol() {
        MdValue v = new MdValue(protocol);
        if (error != null) {
            v.setColor(Color.red);
        }
        return v;
    }

    public List getFieldList() {
        return fields.getFieldList();
    }

    public String getError() {
        return error;
    }

    public boolean isExportOnStartup() {
        return exportOnStartup;
    }

    public void setExportOnStartup(boolean exportOnStartup) {
        this.exportOnStartup = exportOnStartup;
    }

    public void saveProps(PropertySaver pr) {
        fields.saveProps(pr);
    }

}

