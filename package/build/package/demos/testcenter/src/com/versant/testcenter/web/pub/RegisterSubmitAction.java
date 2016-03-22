
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
import com.versant.testcenter.model.SystemUser;
import com.versant.testcenter.model.Student;
import com.versant.testcenter.web.WebConstants;
import com.versant.testcenter.web.student.StudentForm;

/**
 * Handle new user creation. If user with given login name altready exists
 * report error and prompt recapture, otherwise create new user and automatically
 * log in.
 */
public class RegisterSubmitAction extends Action {

    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {

        StudentForm f = (StudentForm)actionForm;
        // check if login name is in use
        SystemUser usr = TestCenterService.findUserByLogin(f.getLogin());
        if (usr != null) {
            ActionErrors errors = new ActionErrors();
            ActionError error = new ActionError("student.err.login.inuse");
            errors.add(ActionErrors.GLOBAL_ERROR, error);
            saveErrors(httpServletRequest, errors);
            return actionMapping.getInputForward();
        }

        TestCenterService.beginTxn();
        Student student = TestCenterService.createStudent();
        student.setFirstName(f.getFirstName());
        student.setSurname(f.getSurname());
        student.setLogin(f.getLogin());
        student.setPassword(f.getPassword());
        TestCenterService.commitTxn();

        // log user in
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute(WebConstants.USER_OID_KEY, student.getOID());

        return actionMapping.findForward("success");
    }
}
