
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
package com.versant.testcenter.web.tags;

import org.apache.struts.taglib.html.SelectTag;
import org.apache.struts.util.RequestUtils;

import javax.servlet.jsp.JspException;
import javax.jdo.JDOHelper;

import com.versant.testcenter.model.JDOUtil;

/**
 * Html select tag with support for persistent objects. If name/property
 * corresponds to a persistent object use object OID as value.
 */
public class TSSelectTag extends SelectTag {

    public int doStartTag() throws JspException {
        try {
            if (value == null) {
                Object val = RequestUtils.lookup(pageContext, name, property,
                        null);
                if (JDOHelper.isPersistent(val)) {
                    value = JDOUtil.getOID(val);
                }
            }

            return super.doStartTag();
        } finally {
            value = null;
        }
    }
}
