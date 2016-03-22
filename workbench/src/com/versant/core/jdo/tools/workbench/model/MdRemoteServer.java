
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

import com.versant.core.util.StringListParser;
import com.versant.core.util.StringList;
import com.versant.core.jdo.VersantPersistenceManagerFactory;

import java.util.Properties;
import java.lang.reflect.Constructor;

/**
 * Info required to connect to a remote JDO Genie server.
 */
public class MdRemoteServer {

    private String url;
    private String username;
    private String password;

    public MdRemoteServer() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void load(StringListParser p) {
        url = p.nextQuotedString();
        username = p.nextQuotedString();
        password = p.nextQuotedString();
        if (p.hasNext()) {
            // must be old format
            p = new StringListParser(p.getString());
            String host = p.nextQuotedString();
            p.nextInt(); // port
            url = "socket://" + host;
            p.nextQuotedString(); // serverName
            username = p.nextQuotedString();
            password = p.nextQuotedString();
        }
    }

    public void save(StringList l) {
        l.appendQuoted(url);
        l.appendQuoted(username);
        l.appendQuoted(password);
    }

    public String toString() {
        return url;
    }

    public VersantPersistenceManagerFactory connect(ClassLoader loader)
            throws Exception {
        Properties p = new Properties();
        p.put("versant.host", url);
        Class aClass = Class.forName("com.versant.core.jdo.remote.RemotePersistenceManagerFactory");
        Constructor constructor = aClass.getConstructor(
                new Class[] {Properties.class, ClassLoader.class});
        return (VersantPersistenceManagerFactory)constructor.newInstance(
                new Object[] {p, loader});
    }

}

