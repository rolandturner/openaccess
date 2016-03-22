
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
package petstore.www;

import org.apache.struts.tiles.TilesRequestProcessor;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jdo.PersistenceManager;

import java.io.IOException;

import petstore.db.User;
import petstore.model.JDOSupport;

/**
 */
public class RequestProcessor extends TilesRequestProcessor {

    public static final String ORIGINAL_URL = RequestProcessor.class.getName() + ".originalURL";
    public static final String LOCALE_TAG = RequestProcessor.class.getName() + ".localeTAG";

    public void process(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException {

        super.process(request, response);

    }

    /**
     * If this action is protected by security roles, make sure that the
     * current user possesses at least one of them.  Return <code>true</code>
     * to continue normal processing, or <code>false</code> if an appropriate
     * response has been created and processing should terminate.
     *
     * If remote user is null, forwards to login page, specified as global-forward
     * "login"
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param mapping The mapping we are using
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if a servlet exception occurs
     */
    protected boolean processRoles(HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping)
            throws IOException, ServletException {

        // Is this action protected by role requirements?
        String roles[] = mapping.getRoleNames();
        if ((roles == null) || (roles.length < 1)) {
            return (true);
        }

        // Is this userAuthenticated?
        User user = RequestUtils.getCustomer(request).getUser();
        log.info(">>>> RequestProcessor.processRoles user = "+user);
        if (user == null) {

                log.info(" Unauthenticated user attemting to access protected resource, " +
                        "forwarding to login page");

            String path = request.getRequestURI().toString() + "?" + request.getQueryString();
            //String ctx = request.getContextPath();
            //path=path.substring(ctx.length());
            request.setAttribute(ORIGINAL_URL, path );
            ActionForward forward = mapping.findForward("login");
            processActionForward(request, response, forward);
            return false;
        }
        else {
            log.info(">>>> RequestProcessor.processRoles available"+user);
            return true;
        }


    }

    /*
    protected void processLocale(HttpServletRequest request,
                                 HttpServletResponse response) {

        super.processLocale(request, response);

        if (request.getAttribute(LOCALE_TAG) != null) {
            return;
        }

        request.setAttribute(LOCALE_TAG, LOCALE_TAG);

        String locStr = request.getParameter("lang");
        if (locStr == null) {
            Locale l = (Locale) request.getSession().getAttribute(Action.LOCALE_KEY);
            if (l == null) {
                locStr = "en";
            } else {
                locStr = l.getLanguage();
            }
        }

        try {
            if ("ru".equals(locStr)) {
                request.getSession().setAttribute(Action.LOCALE_KEY, new Locale(locStr,""));
                response.setContentType("text/html; charset=" + "koi8-r");

                request.setCharacterEncoding("koi8-r");
                response.setHeader("Content-Type", "text/html; charset=koi8-r");
            } else {
                request.getSession().setAttribute(Action.LOCALE_KEY, new Locale("en",""));
                response.setContentType("text/html; charset=" + "ISO-8859-1");
                request.setCharacterEncoding("ISO-8859-1");
                response.setHeader("Content-Type", "text/html; charset=ISO-8859-1");
            }
        } catch (UnsupportedEncodingException e) {
            log(e.getMessage());
        }
    }   */

}

