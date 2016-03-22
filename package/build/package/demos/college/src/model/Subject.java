
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

import java.util.ArrayList;
import java.util.List;

/**
 * A subject with modules.
 */
public class Subject {

    private String code;
    private String name;
    private List modules = new ArrayList(); // of Module

    public Subject() {
    }

    public Subject(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getModules() {
        return new ArrayList(modules);
    }

    public void addModule(Module module) {
        modules.add(module);
        module.setSubject(this);
    }

    public boolean hasModule(Module module) {
        return modules.contains(module);
    }

    public String toString() {
        return name;
    }

}

