
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
package com.versant.testcenter.web.pub;

import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.versant.testcenter.web.WebConstants;

/**
 * Process logout. Remove current user OID stored in HttpSession under {@link WebConstants#USER_OID_KEY}
 */
public class LogoutSubmitAction extends Action {

    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {

        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.removeAttribute(WebConstants.USER_OID_KEY);
        }
        return actionMapping.findForward("success");
    }
}
