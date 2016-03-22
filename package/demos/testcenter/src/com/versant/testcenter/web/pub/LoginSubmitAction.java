
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

import com.versant.testcenter.service.TestCenterService;
import com.versant.testcenter.service.Context;
import com.versant.testcenter.model.SystemUser;
import com.versant.testcenter.web.WebConstants;

/**
 * Validates username and password. If success store user's OID on the HttpSession
 * under {@link WebConstants#USER_OID_KEY}
 */
public class LoginSubmitAction extends Action {

    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {

        LoginForm f = (LoginForm)actionForm;
        if (!TestCenterService.login(f.getLogin(), f.getPassword())) {
            ActionErrors errors = new ActionErrors();
            ActionError error = new ActionError("login.err.invalid");
            errors.add(ActionErrors.GLOBAL_ERROR, error);
            saveErrors(httpServletRequest, errors);
            return actionMapping.getInputForward();
        }

        HttpSession session = httpServletRequest.getSession();
        session.setAttribute(WebConstants.USER_OID_KEY,
                Context.getContext().getCurrentUser().getOID());
        return actionMapping.findForward("success");
    }
}
