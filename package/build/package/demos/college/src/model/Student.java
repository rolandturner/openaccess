
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

import java.util.List;
import java.util.ArrayList;

/**
 * A Student with Subjects.
 */
public class Student {

    private String name;
    private List registrations = new ArrayList(); // of Registration

    public Student() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getRegistrations() {
        return new ArrayList(registrations);
    }

    public Registration getRegistration(Subject subject) {
        for (int i = registrations.size() - 1; i >= 0; i--) {
            Registration r = (Registration)registrations.get(i);
            if (r.getSubject() == subject) return r;
        }
        return null;
    }

    public Registration register(Subject subject) {
        if (getRegistration(subject) != null) {
            throw new IllegalArgumentException("Already registered for " + subject);
        }
        Registration r = new Registration(this, subject);
        registrations.add(r);
        return r;
    }

    public String toString() {
        return name;
    }

}

