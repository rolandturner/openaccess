
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
package com.versant.core.jdo.tools.eclipse;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A classloader used by EnhancerBuilder to load project classes.
 *
 */
public class EnhancerClassLoader extends URLClassLoader {

    public EnhancerClassLoader(ClassLoader loader, IJavaProject project) {
        super(getProjectClasspath(project), loader);
    }

    public static URL[] getProjectClasspath(IJavaProject project) {
        List urls = new ArrayList();
        try {
            String[] paths = JavaRuntime.computeDefaultRuntimeClassPath(project);
            for (int j = 0; j < paths.length; j++) {
                if (paths[j] != null) {
                    File file = new File(paths[j]);
                    if (file.isDirectory()) {
                        paths[j] = paths[j] + "/";
                    }
                    urls.add(new URL("file:" + paths[j]));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return (URL[])urls.toArray(new URL[urls.size()]);
    }
}
