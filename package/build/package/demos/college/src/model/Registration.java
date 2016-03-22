
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
package model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Information on a Subject for a Student.
 */
public class Registration {

    private Student student;
    private Subject subject;
    private Date dateRegistered;
    private List modules = new ArrayList(); // of Module

    public Registration() {
    }

    public Registration(Student student, Subject subject) {
        this.student = student;
        this.subject = subject;
        dateRegistered = new Date();
    }

    public Student getStudent() {
        return student;
    }

    public Subject getSubject() {
        return subject;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public List getModules() {
        return new ArrayList(modules);
    }

    public void addModule(Module m) {
        if (!subject.hasModule(m)) {
            throw new IllegalArgumentException("Module " + m +
                " not part of Subject " + subject);
        }
        modules.add(m);
    }

    public String toString() {
        return student + "." + subject;
    }

    /**
     * Get a comma separated summary of the selected modules.
     */
    public String getModuleSummary() {
        StringBuffer s = new StringBuffer();
        int n = modules.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(", ");
            Module m = (Module)modules.get(i);
            s.append(m.getTitle());
        }
        return s.toString();
    }

}

