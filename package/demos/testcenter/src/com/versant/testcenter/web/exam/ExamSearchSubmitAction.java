
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
import com.versant.testcenter.model.Exam;

import java.util.Collection;
import java.util.HashSet;

/**
 * Finds collection of exams satisfying certain search criteria.
 * Put collection on request under {@link WebConstants#EXAM_SEARCH_RESULT}
 * to be read by the view.
 */
public class ExamSearchSubmitAction extends Action {

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        Collection exams;
        String examOID = request.getParameter(WebConstants.EXAM_PARAM);
        if (examOID != null) {
            Exam exam = TestCenterService.findExamByOID(examOID);
            exams = new HashSet();
            exams.add(exam);
        } else {
            ExamForm f = (ExamForm)form;
            exams = TestCenterService.findExamsByName(f.getName());
        }
        request.setAttribute(WebConstants.EXAM_SEARCH_RESULT,
                exams);
        return mapping.findForward("success");
    }
}
