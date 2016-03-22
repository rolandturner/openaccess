
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

import javax.jdo.JDOFatalUserException;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;


import java.awt.*;
import javax.swing.*;

import za.co.hemtech.gui.Icons;


/**
 *
 */
public class MdUtils {

    /**
     * Get the package from a Class name. Returns empty string if cname has
     * no package.
     */
    public static String getPackage(String cname) {
        int i = cname.lastIndexOf('.');
        if (i < 0) return "";
        return cname.substring(0, i);
    }

    public static String getErrorHtml(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuffer html = new StringBuffer("<html><font color=\"red\">");
        String message = throwable.getLocalizedMessage();
        if (message == null) {
            message = throwable.getClass().getName();
            throwable.printStackTrace();
        }
        StringTokenizer tok = new StringTokenizer(message, "\n", false);
        ArrayList list = new ArrayList();
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s == null || s.trim().length() == 0) {
                s = "No error message found.";
            }
            if (!s.startsWith("--> ")) {
                s = replace(s, "<", "&lt;");
                list.add(s);
            }
        }
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            String s = (String)iterator.next();
            html.append(s);
            if (iterator.hasNext()) {
                html.append("<br>");
            }
        }
        html.append("</font></html>");

        return html.toString();
    }

    /**
     * Put the class of s before the package and follow it with a space.
     */
    public static String putClassNameFirst(String s) {
        if (s == null) return s;
        int i = packageEndIndex(s);
        if (i < 0) return s;
        return s.substring(i + 1) + " " + s.substring(0, i);
    }

    /**
     * Find the position of the dot immediately following the package before
     * the class name. Returns -1 if no package.
     */
    public static int packageEndIndex(String s) {
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            // this loop detects inner classes by assuming that package names
            // start with a lower case character
            for (; ;) {
                int j = s.lastIndexOf('.', i - 1);
                if (j > 0) {
                    char c = s.charAt(j + 1);
                    if (c >= 'A' && c <= 'Z') {
                        i = j;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            return i;
        } else {
            return -1;
        }
    }

    /**
     * Add any properties from a semicolon delimited String ps to props.
     */
    public static void parseProperties(String ps, Properties props) {
        if (ps == null) return;
        StringTokenizer t = new StringTokenizer(ps, "=", false);
        for (; ;) {
            String key;
            try {
                key = t.nextToken("=");
            } catch (NoSuchElementException e) {
                break;
            }
            try {
                String value = t.nextToken(";").substring(1);
                props.put(key, value);
            } catch (NoSuchElementException e) {
                throw new JDOFatalUserException("Expected semicolon delimited " +
                        "property=value pairs: '" + ps + "'");
            }
        }
    }

    public static boolean isStringNotEmpty(String str) {
        return str != null && str.trim().length() > 0;
    }

    /**
     * Get the class name from a fully qualified Class name.
     */
    public static String getClass(String cname) {
        int i = cname.lastIndexOf('.');
        if (i < 0) return cname;
        return cname.substring(i + 1);
    }

    public static String getErrorHtml(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        StringBuffer html = new StringBuffer("<html><font color=\"red\">");

        StringTokenizer tok = new StringTokenizer(errorMessage, "\n", false);
        ArrayList list = new ArrayList();
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s == null || s.trim().length() == 0) {
                // do nothing
            } else {
                s = replace(s, "<", "&lt;");
                list.add(s);
            }
        }
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            String s = (String)iterator.next();
            html.append(s);
            if (iterator.hasNext()) {
                html.append("<br>");
            }
        }
        html.append("</font></html>");

        return html.toString();
    }

    /**
     * Create an array of URL's from a list of File's (jars and dirs).
     */
    public static URL[] toURLArray(List cp) throws MalformedURLException {
        int n = cp.size();
        URL[] a = new URL[n];
        for (int i = 0; i < n; i++) {
            File f = (File)cp.get(i);
            
            a[i] = f.toURL();
    
         
        }
        return a;
    }

    

    /**
     * Get the Versant Open Access version or null if not available.
     */
    public static String getVersion() {
        // load one of our classes or the next line does not work
        
        com.versant.core.jdo.VersantPersistenceManager.class.getName();
        Package pkg = Package.getPackage("com.versant.core.jdo");
        if (pkg != null) {
            return pkg.getImplementationVersion();
        } else {
            return null;
        }

         
    }

    /**
     * Convert an absolute path into a path a relative to a dir. The path is
     * left absolute if it has no common part with dir e.g. different drive
     * letter on windoze.
     */
    public static String toRelativePath(File fPath, File fDir) {
        String path = fPath.getAbsolutePath();
        String dir = fDir.getAbsolutePath();
        
        boolean win = isWindowsPlatform();

        
        if (!win && path.indexOf(':') > 0) return path;
        path = path.replace('\\', '/');
        dir = dir.replace('\\', '/');
        if (!dir.endsWith("/")) dir += "/";
        if (fPath.isDirectory() && !path.endsWith("/")) {
            path += "/";
        }
        int dirlen = dir.length();
        int pathlen = path.length();
        int pos = 0;
        for (; pos < dirlen && pos < pathlen; pos++) {
            char a = path.charAt(pos);
            char b = dir.charAt(pos);
            if (a != b) { // go back to last slash
                int i = Math.min(path.lastIndexOf('/', pos),
                        dir.lastIndexOf('/', pos));
                if (i >= 0) pos = i + 1;
                break;
            }
        }
        if (pos == 0) return path;
        if (pos == dirlen || pos == dirlen - 1) {
            if (pos == pathlen) return ".";
            return path.substring(pos);
        }
        StringBuffer ans = new StringBuffer(pathlen + dirlen);
        for (int p = pos; ;) {
            int i = dir.indexOf('/', p);
            if (i < 0) break;
            ans.append("../");
            p = i + 1;
        }
        ans.append(path.substring(pos));
        return ans.toString();
    }

    /**
     * Convert a path relative to dir into an absolute path. The path is
     * returned as is if it is absolute already.
     */
    public static String toAbsolutePath(String path, String dir) {
        if (path == null) return null;
        path = path.replace('\\', '/');
        if (path.startsWith("/") || path.indexOf(':') >= 0) return path;
        dir = dir.replace('\\', '/');
        if (dir.endsWith("/")) dir = dir.substring(0, dir.length() - 1);
        int dirlen = dir.length();
        int dirpos = dirlen;
        int pathpos = 0;
        for (; ;) {
            int i = path.indexOf("../", pathpos);
            if (i != pathpos) break;
            dirpos = dir.lastIndexOf('/', dirpos - 1);
            if (dirpos < 0) return path;
            pathpos += 3;
        }
        File f;
        if (dirpos == dirlen) {
            if (".".equals(path)) {
                f = new File(dir);
            } else {
                f = new File(dir, path);
            }
        } else {
            f = new File(dir.substring(0, dirpos), path.substring(pathpos));
        }
        return f.toString();
    }

    /**
     * Find the longest common prefix of all the Strings in list. If all
     * the String's are the same then the index of the last sep + 1 is
     * returned.
     */
    public static int getCommonPrefixLength(List list, char sep) {
        Collections.sort(list);
        int n = list.size();
        if (n == 0) return 0;
        int pos;
        if (n == 1) {
            pos = ((String)list.get(0)).lastIndexOf(sep) + 1;
        } else {
            char[][] a = new char[n][];
            int minlen = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                String s = (String)list.get(i);
                a[i] = s.toCharArray();
                int len = a[i].length;
                if (len < minlen) minlen = len;
            }
            for (pos = 0; pos < minlen; pos++) {
                char c = a[0][pos];
                for (int i = 1; i < n; i++) {
                    if (a[i][pos] != c) break;
                }
            }
            for (; --pos > 0 && a[0][pos] != sep;) ;
        }
        return pos;
    }

    /**
     * replace stuff in a String
     */
    public static String replace(String text, String repl, String with) {
        if (text == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(text.length());
        int start = 0, end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    /**
     * Check all MDClass'es in a to see if any of them extend a PC class in
     * this datastore and set their persistence-capable-superclass attributes
     * if so.
     */
    public static boolean fillPCSuperclassAttr(List allClasses) {
        boolean changed = false;
        for (int i = allClasses.size() - 1; i >= 0; i--) {
            Object o = allClasses.get(i);
            if (!(o instanceof MdClass)) continue;
            MdClass c = (MdClass)o;
            Class cls = c.getCls();
            if (cls == null) continue;
            Class superclass = cls.getSuperclass();
            if (superclass == null) continue;
            String scname = superclass.getName();
            for (int j = allClasses.size() - 1; j >= 0; j--) {
                MdClass q = (MdClass)allClasses.get(j);
                if (q.getQName().equals(scname)) {
                    if (q.getMdPackage() == c.getMdPackage()) {
                        scname = q.getName();
                    }
                    c.setPcSuperclass(new MdValue(scname));
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }


    public static Icon getIconWithVersion(String name) {
        final Icon icon = Icons.getIcon(name);
        String version = MdUtils.getVersion();
        if (version == null || version.length() == 0) {
            version = "(version not found)";
        }
        version = "Version: " + version;
        final String version1 = version;
        return new Icon() {
            public int getIconHeight() {
                return icon.getIconHeight();
            }

            public int getIconWidth() {
                return icon.getIconWidth();
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                icon.paintIcon(c, g, x, y);
                int width = getIconWidth();
                int height = getIconHeight();
                int textw = g.getFontMetrics().stringWidth(version1);
                g.setColor(Color.black);
                g.drawString(version1, (width - textw) / 2, height / 4 * 3);
            }
        };
    }
    

    /**
     * Are we running on windows
     */
    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith("Windows");
    }
}
