
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
package com.versant.testcenter.grinder;

import java.util.Random;

/**
 * @keep-all
 */
public class GrinderStringBean {

    private int noOfUsers = 20;
    private Random randomUser = new Random();

    private int noOfExams = 26 * 20;
    private Random randomExamId = new Random();

    private Random randomExam = new Random();
    private String lastExamSearchString = "";
    private String examClassId = "379645357";

    public GrinderStringBean() {
        System.out.println(">>> GrinderStringBean.GrinderStringBean <<<<");
    }

    public String getLoginNamePassword() {
        int r = randomUser.nextInt(noOfUsers);
        return "login=user" + r + "&password=user" + r;
    }

    public String getExamId() {
        return examClassId + "-" + randomExamId.nextInt(noOfExams);
    }

    public String getExamSearchString() {
        Character c = new Character((char)('a' + randomExam.nextInt(26)));
        return c.toString();
    }
}


