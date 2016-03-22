
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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Exam list form contains array of Exams
 */
public class ExamListForm extends ActionForm {

    private String[] exams;

    public String[] getExams() {
        return exams;
    }

    public void setExams(String[] exams) {
        this.exams = exams;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        exams = new String[0];
    }

}
