
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
package com.versant.core.util;

import com.versant.core.common.BindingSupportImpl;

import java.net.MalformedURLException;

/**
 * A URL parsed into its different parts. This differs from the URL class
 * in that there does not have to be a handler for the protocol.
 */
public class ParsedURL {

    private String orginal;
    private String protocol;
    private String host;
    private int port = -1;
    private String path;
    private String query;
    private String ref;

    /**
     * Parse url and throw an user exception if invalid.
     */
    public ParsedURL(String url) {
        this.orginal = url;
        int i = url.indexOf(':');
        if (i <= 0) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                "Missing protocol: " + orginal);
        }
        protocol = url.substring(0, i);
        url = url.substring(i + 1);
        if (url.startsWith("//")) {
            url = url.substring(2);
        }
        i = url.indexOf(':');
        if (i >= 0) {
            host = url.substring(0, i);
            url = url.substring(i + 1);
            i = url.indexOf('/');
            String s;
            if (i >= 0) {
                s = url.substring(0, i);
                url = url.substring(i);
            } else {
                s = url;
                url = null;
            }
            try {
                port = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Invalid port '" + s + "' in " + orginal);
            }
        } else {
            i = url.indexOf('/');
            if (i >= 0) {
                host = url.substring(0, i);
                url = url.substring(i);
            } else {
                host = url;
                url = null;
            }
        }
        if (url != null) {
            int ind = url.indexOf('#');
            ref = ind < 0 ? null: url.substring(ind + 1);
            url = ind < 0 ? url: url.substring(0, ind);
            int q = url.lastIndexOf('?');
            if (q != -1) {
                query = url.substring(q + 1);
                path = url.substring(0, q);
            } else {
                path = url;
            }
        }
    }

    /**
     * Just extract the protocol from url.
     */
    public static String getProtocol(String url) {
        int i = url.indexOf(':');
        return i < 0 ? null : url.substring(0, i);
    }

    public String getUrl() {
        return orginal;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    /**
     * Get the port or -1 if none set.
     */
    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getRef() {
        return ref;
    }

}

