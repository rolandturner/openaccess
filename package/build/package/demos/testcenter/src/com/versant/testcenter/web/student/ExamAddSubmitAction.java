
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
package com.versant.testcenter.web.student;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.versant.testcenter.web.exam.ExamListForm;
import com.versant.testcenter.service.TestCenterService;
import com.versant.testcenter.model.Exam;
import com.versant.testcenter.model.Student;

/**
 * Assignes exams to student. Adds exams passed via {@link ExamListForm} to
 * {@link Student}
 */
public class ExamAddSubmitAction extends Action {

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        ExamListForm f = (ExamListForm)form;
        String[] exams = f.getExams();
        if (exams.length > 0) {
            TestCenterService.beginTxn();
            Student student = (Student)TestCenterService.getCurrentUser();
            for (int i = 0; i < exams.length; i++) {
                Exam exam = TestCenterService.findExamByOID(exams[i]);
                student.addExam(exam);
            }
            TestCenterService.commitTxn();
        }
        return mapping.findForward("success");
    }

}
