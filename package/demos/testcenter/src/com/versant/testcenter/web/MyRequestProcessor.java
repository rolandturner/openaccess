
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
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RequestProcessor;
import com.versant.testcenter.model.SystemUser;
import com.versant.testcenter.model.Administrator;
import com.versant.testcenter.model.Student;
import com.versant.testcenter.service.Context;
import com.versant.testcenter.service.TestCenterService;
import com.versant.testcenter.web.WebConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class MyRequestProcessor extends RequestProcessor {

    /**
     * Make sure that {@link Context} is closed after request is finished
     */
    public void process(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        try {
            super.process(request, response);
        } finally {
            // make sure that pm gets closed and context gets released from current thread
            Context.getContext().close();
        }
    }

    /**
     * Perform authentication and authorisation.
     * By-pass authntication for paths starting with /public
     */
    protected boolean processRoles(HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping)
            throws IOException, ServletException {
        String path = request.getServletPath();
        if (path.startsWith("/public")) return true;
        if (!authenticate(request, response, mapping)) return false;
        return authorize(request, response, mapping);
    }

    /**
     * Make sure that user is authenticated, if not forward to login page
     * defined as global forward "login" in main struts-config. Otherwise
     * put user's OID in {@link Context}. Here context gets initialized with
     * current user
     */
    public boolean authenticate(HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping)
            throws IOException, ServletException {
        HttpSession session = request.getSession();
        String userOID = (String)session.getAttribute(
                WebConstants.USER_OID_KEY);
        if (userOID != null) {
            // initialize  context with current user
            Context ctx = Context.getContext();
            ctx.setCurrentUser(TestCenterService.findUserByOID(userOID));
            return true;
        } else {
            ActionForward forward = (ActionForward)moduleConfig.findForwardConfig(
                    "login");
            if (forward == null) {
                throw new ServletException("login page is not defined");
            }
            processActionForward(request, response, forward);
            return false;
        }
    }

    /**
     * Implements access control
     * only {@link Student} can see /studen and only
     * {@link Administrator} can see /admin
     */
    public boolean authorize(HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping)
            throws IOException, ServletException {

        SystemUser user = TestCenterService.getCurrentUser();
        String path = request.getServletPath();
        if (path.startsWith("/admin") && (user instanceof Administrator)) {
            return true;
        } else if (path.startsWith("/student") && (user instanceof Student)) {
            return true;
        } else if (path.startsWith("/user")) {
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}

