
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
import com.versant.testcenter.web.StrutsUtil;
import com.versant.testcenter.model.Exam;
import com.versant.testcenter.service.TestCenterService;

import java.util.Map;
import java.util.HashMap;

/**
 * Handle update or create of {@link Exam}. If parameter {@link WebConstants#EXAM_PARAM}
 * specifiyng OID is present perform update, otherwise insert. After performing transaction
 * redirect to "success" action passing OID of Exam as parameter.
 */
public class ExamSubmitAction extends Action {

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        ExamForm f = (ExamForm)form;
        String examOID = request.getParameter(WebConstants.EXAM_PARAM);
        Exam exam;
        TestCenterService.beginTxn();

        if (examOID == null) {
            exam = TestCenterService.createExam();
        } else {
            exam = TestCenterService.findExamByOID(examOID);
        }

        exam.setName(f.getName());
        exam.setDescription(f.getDescription());
        exam.setExamCategory(
                TestCenterService.findExamCategoryByOID(f.getExamCategory()));

        TestCenterService.commitTxn();

        Map params = new HashMap();
        params.put(WebConstants.EXAM_PARAM, exam.getOID());
        ActionForward fwd = mapping.findForward("success");
        return StrutsUtil.appendParameters(fwd, params);

    }
}
