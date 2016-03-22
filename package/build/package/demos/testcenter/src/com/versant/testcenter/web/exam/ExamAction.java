
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
package com.versant.testcenter.web.exam;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.versant.testcenter.web.WebConstants;
import com.versant.testcenter.service.TestCenterService;

/**
 * Fetches {@link com.versant.testcenter.model.Exam} instance specified by
 * request parameter {@link WebConstants#EXAM_PARAM} and use it as a  bean for
 * the jsp view.
 * <p/>
 * If request parameter is not present just forward to the view, the form bean
 * will be created automatically by struts framework
 */
public class ExamAction extends Action {

    /**
     * Name of the form bean expected by the view. Should be same as in
     * struts-config.xml *
     */
    public static final String EXAM_FORM = "examForm";

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        String examOID = request.getParameter(WebConstants.EXAM_PARAM);

        if (examOID != null) {
            request.setAttribute(EXAM_FORM,
                    TestCenterService.findExamByOID(examOID));
        }

        return mapping.findForward("success");

    }
}
