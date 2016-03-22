
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
package com.versant.core.jdo.junit.test2.model.dc;

/**
 * 
 */
public class JobPosting {

    private String title;
    private JobDescription position;

    public JobPosting(String title, JobDescription position) {
        this.title = title;
        this.position = position;
    }

    public JobPosting() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JobDescription getPosition() {
        return position;
    }

    public void setPosition(JobDescription position) {
        this.position = position;
    }

}

