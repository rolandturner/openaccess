
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
package com.versant.core.vds.util;

import com.versant.core.common.Debug;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * Utility class to parse user-specified datastore URLs.
 * <p/>
 * Database URL syntax is:
 * db@host:port#user:password
 * <p/>
 * Default value for the hostname is the 'localhost'
 * Default value for the port is '5019'
 * Each component of the name must be less than 32 characters
 * Multiple datastores are separated by a <code>|</code> character
 */
public final class ConnectionURL {

    public static final String URL_SEPARATOR = "|";
    public static final int DEFAULT_OSCSSD_PORT = 5019;

    public static final String URL_PREFIX = "versant:";

    private String _url;
    private String _name;
    private String _host;
    private int _port;
    private String _user;
    private String _password;

    /**
     * 
     */
    public ConnectionURL(final String url) {
        if (url == null || url.length() == 0) {
            throw new RuntimeException("Null or empty URL");
        }
        _url = url.trim();

        String urlStr = _url;
        if (urlStr.startsWith("vds:")) {
            urlStr = URL_PREFIX + urlStr.substring(4);
        }
        if (urlStr.startsWith(URL_PREFIX)) {
            _url = urlStr.substring(URL_PREFIX.length());
        }

        _port = DEFAULT_OSCSSD_PORT;
        int atIdx = _url.indexOf('@');
        int colonIdx = _url.lastIndexOf(':');
        if (atIdx != -1) {
            _name = _url.substring(0, atIdx);
            if (colonIdx == -1) {
                _host = _url.substring(atIdx + 1);
            } else {
                _host = _url.substring(atIdx + 1, colonIdx);
            }
        } else if (colonIdx != -1) {
            _name = _url.substring(0, colonIdx);
        } else {
            _name = _url;
        }
        if (colonIdx != -1) {
            String portAsString = _url.substring(colonIdx + 1);
            try {
                _port = Integer.parseInt(portAsString);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        url + " specifies invalid port " + portAsString);
            }
        }

        try {
            if (_host == null) {
                _host = InetAddress.getLocalHost().getHostName();
            } else {
                _host = InetAddress.getByName(_host).getHostName();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(
                    url + " specifies unknown host " + _host);
        }

    }

    /**
     * Gets just the database name component of the connection URL..
     *
     * @return String the database name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets just the hostname component of the connection URL.
     *
     * @return String the hostname
     */
    public String getHost() {
        return _host;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _password;
    }

    /**
     * Gets the port component of the connection URL.
     *
     * @return int the port number
     */
    public int getPort() {
        return _port;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return _url.toString();
    }

    /**
     * Parses a multi part connection url string into individual urls.
     * Each URL is separated by <code>URL_SEPARATOR</code> character.
     *
     * @param multiURL a multi part URL string
     * @return array of Connection URL
     */
    public static ConnectionURL[] parse(String multiURL) {
//        assert multiURL != null;
        if (Debug.DEBUG){
            Debug.assertIllegalArgument(multiURL != null,"URL is Null");
        }
        StringTokenizer tokenizer = new StringTokenizer(multiURL,
                URL_SEPARATOR);
        int nToken = tokenizer.countTokens();
//        assert nToken > 0 : "No URL found in [" + multiURL + "]";
        if (Debug.DEBUG) {
            Debug.assertIllegalArgument(nToken > 0, "No URL found in [" + multiURL + "]");
        }

        ConnectionURL[] result = new ConnectionURL[nToken];
        for (int i = 0; i < nToken; i++) {
            result[i] = new ConnectionURL(tokenizer.nextToken());
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            ConnectionURL[] urls = parse(args[i]);
            for (int j = 0; j < urls.length; j++) {
                ConnectionURL url = urls[j];
                System.err.println(
                        "[" + url.getName() + "] [" + url.getHost() + "] [" + url.getPort() + "] [" + url.getUser() + "] [" + url.getPassword() + "]");
            }
        }
    }
}
