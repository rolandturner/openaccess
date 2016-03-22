
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
package com.versant.testcenter.web;

import org.apache.struts.action.ActionForward;

import java.util.Map;
import java.util.Iterator;
import java.net.URLEncoder;

public class StrutsUtil {

    /**
     * Adapted from {@link org.apache.struts.util.RequestUtils}
     * Construct {@link ActionForward} from existing
     * action with extra request parameters appended.
     *
     * @param action
     * @param params Map containing parameter name-value pairs
     * @return ActionForward with extra parameters appended
     */
    public static ActionForward appendParameters(ActionForward action,
            Map params) {
        // Add dynamic parameters if requested
        if ((params == null) || (params.size() == 0)) {
            return action;
        }

        StringBuffer url = new StringBuffer(action.getPath());
        String anchor;
        boolean redirect = action.getRedirect();

        // Save any existing anchor
        String temp = url.toString();
        int hash = temp.indexOf('#');
        if (hash >= 0) {
            anchor = temp.substring(hash + 1);
            url.setLength(hash);
            temp = url.toString();
        } else {
            anchor = null;
        }

        // Define the parameter separator
        String separator = "&amp;";
        if (redirect) {
            separator = "&";
        }

        // Add the required request parameters
        boolean question = temp.indexOf('?') >= 0;
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            Object value = params.get(key);
            if (value == null) {
                if (!question) {
                    url.append('?');
                    question = true;
                } else {
                    url.append(separator);
                }
                url.append(URLEncoder.encode(key));
                url.append('='); // Interpret null as "no value"
            } else if (value instanceof String) {
                if (!question) {
                    url.append('?');
                    question = true;
                } else {
                    url.append(separator);
                }
                url.append(URLEncoder.encode(key));
                url.append('=');
                url.append(URLEncoder.encode((String)value));
            } else if (value instanceof String[]) {
                String values[] = (String[])value;
                for (int i = 0; i < values.length; i++) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }
                    url.append(URLEncoder.encode(key));
                    url.append('=');
                    url.append(URLEncoder.encode(values[i]));
                }
            } else /* Convert other objects to a string */ {
                if (!question) {
                    url.append('?');
                    question = true;
                } else {
                    url.append(separator);
                }
                url.append(URLEncoder.encode(key));
                url.append('=');
                url.append(URLEncoder.encode(value.toString()));
            }
        }

        // Re-add the saved anchor (if any)
        if (anchor != null) {
            url.append('#');
            url.append(URLEncoder.encode(anchor));
        }

        return new ActionForward(action.getName(), url.toString(), redirect,
                action.getContextRelative());
    }

}
