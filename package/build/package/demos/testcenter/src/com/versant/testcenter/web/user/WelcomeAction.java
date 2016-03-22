
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
package com.versant.testcenter.web.user;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import com.versant.testcenter.model.SystemUser;
import com.versant.testcenter.model.Administrator;
import com.versant.testcenter.model.Student;
import com.versant.testcenter.service.TestCenterService;

/**
 * Redirect to correct welcome page depending on {@link SystemUser} class type.
 */
public class WelcomeAction extends Action {

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        SystemUser user = TestCenterService.getCurrentUser();
        if (user instanceof Administrator) {
            return mapping.findForward("admin");
        } else if (user instanceof Student) {
            return mapping.findForward("student");
        } else {
            throw new ServletException(
                    "Unexpected type: " + user.getClass().getName());
        }
    }

}
