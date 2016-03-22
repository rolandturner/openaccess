
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

import com.versant.core.jdbc.sql.conv.StreamUtils;
import com.versant.core.common.Debug;

import java.net.*;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * Extends URLClassLoader to add support for discovering all classes on
 * the classpath. This also includes a horrible hack to aid unloading JDBC
 * drivers.
 *
 * @keep-all
 * @see com.versant.core.jdo.tools.workbench.DriverUnloader
 */
public class MdClassLoader extends URLClassLoader {

    public static final String DRIVER_UNLOADER_NAME =
            "com.versant.core.jdo.tools.workbench.DriverUnloader";

    private byte[] unloaderBytes;

    public MdClassLoader(URL[] urls, ClassLoader parent) throws Exception {
        super(urls, parent);
        init();
    }

    public MdClassLoader(URL[] urls) throws Exception {
        super(urls, MdClassLoader.class.getClassLoader());
        init();
    }

    /**
     * Load the bytes for the DriverUnloader class.
     */
    private void init() throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();
        String name = DRIVER_UNLOADER_NAME.replace('.', '/') + ".class";
        InputStream in = cl.getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("Unable to load res " + name);
        }
        unloaderBytes = StreamUtils.readAll(in);
    }

    /**
     * Make sure that we always load DriverUnloader ourselves i.e. do not
     * let our parent load it.
     */
    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (name.equals(DRIVER_UNLOADER_NAME)) {
            Class c = findLoadedClass(name);
            if (c == null) {
                c = defineClass(DRIVER_UNLOADER_NAME, unloaderBytes, 0,
                        unloaderBytes.length);
            }
            if (resolve) resolveClass(c);
            return c;
        } else {
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Get a list of all resources with name ending with found on the
     * classpath.
     */
    public ArrayList getAllResources(String suffix) {
        ArrayList a = new ArrayList();
        URL[] urls = getURLs();
        int len = urls.length;
        for (int i = 0; i < len; i++) {
            URL u = urls[i];
            String protocol = u.getProtocol();
            if (protocol.equals("file")) {
                File f = new File(u.getFile());
                addFiles(f, a, suffix);
            } else {
                if (Debug.DEBUG) {
                    Debug.OUT.println("Unknown protocol: " + u);
                }
            }
        }
        return a;
    }

    /**
     * Get the names of all classes on the classpath.
     */
    public ArrayList getAllClasses() {
        ArrayList a = getAllResources(".class");
        for (int i = a.size() - 1; i >= 0; i--) {
            String n = (String)a.get(i);
            a.set(i, n.replace('/', '.').replace('\\', '.'));
        }
        return a;
    }

    private void addFiles(File f, ArrayList a, String suffix) {
        addFilesImp(f.toString().length() + 1, f, a, suffix,
                suffix.length());
    }

    private void addFilesImp(int cut, File f, ArrayList a, String suffix,
            int chop) {
        File[] all = f.listFiles();
        if (all == null) return;
        int len = all.length;
        for (int i = 0; i < len; i++) {
            File file = all[i];
            if (file.isDirectory()) {
                addFilesImp(cut, file, a, suffix, chop);
            } else if (file.getName().endsWith(suffix)) {
                String s = file.toString().substring(cut);
                s = s.substring(0, s.length() - chop);
                a.add(s);
            }
        }
    }

}

