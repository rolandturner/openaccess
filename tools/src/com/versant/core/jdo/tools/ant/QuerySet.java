
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
package com.versant.core.jdo.tools.ant;

import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DirectoryScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @keep-all
 *
 */
public class QuerySet extends AbstractFileSet {
    private ArrayList packages = new ArrayList();

    public QuerySet() {
        super();
    }

    protected QuerySet(QuerySet fileset) {
        super(fileset);
    }

    public Package createPackage() {
        Package pack = new Package();
        packages.add(pack);
        return pack;
    }

    /**
     * Return a FileSet that has the same basedir and same patternsets
     * as this one.
     */
    public Object clone() {
        if (isReference()) {
            return new QuerySet((QuerySet) getRef(getProject()));
        } else {
            return new QuerySet(this);
        }
    }

    public List getQueryClasses(Project project) {
        ArrayList classlist = new ArrayList();
        final String dotClass = ".class";
        StringBuffer buf = new StringBuffer();
        for (Iterator iterator = packages.iterator(); iterator.hasNext();) {
            Package s = (Package) iterator.next();
            String pat = s.getName();
            if (pat.endsWith("*")) {
                pat = pat + "*";
            } else {
                pat = pat + ".*";
            }
            buf.append(pat.replace('.', '/'));
            buf.append(" ");

        }
        setIncludes(buf.toString().trim());
        DirectoryScanner scanner = getDirectoryScanner(project);
        String[] incFiles = scanner.getIncludedFiles();
        for (int j = 0; j < incFiles.length; j++) {
            String temp = incFiles[j];
            if (temp.endsWith(dotClass)) {
                classlist.add(temp);
            }

        }
        return classlist;
    }

    /**
     * Used to track info about the packages to be
     */
    public static class Package {
        /**
         * The package name
         */
        private String name;

        /**
         * Set the name of the package
         *
         * @param name the package name.
         */
        public void setName(String name) {
            this.name = name.trim();
        }

        /**
         * Get the package name.
         *
         * @return the package's name.
         */
        public String getName() {
            return name;
        }

        /**
         * @see java.lang.Object#toString
         */
        public String toString() {
            return getName();
        }
    }
}
