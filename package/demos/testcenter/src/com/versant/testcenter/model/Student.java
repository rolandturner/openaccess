
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
package com.versant.testcenter.model;

import java.util.*;

/**
 * A Student with registered exams.
 *
 */
public class Student extends SystemUser {

    private List exams = new ArrayList(); // of Exam, sorted by Exam.name

    public List getExams() {
        return new ArrayList(exams);
    }

    public void addExam(Exam exam) {
        if (!exams.contains(exam)) exams.add(exam);
    }

    public boolean removeExam(Exam exam) {
        return exams.remove(exam);
    }

    public boolean removeAllExams(Collection c) {
        return exams.removeAll(c);
    }

}


