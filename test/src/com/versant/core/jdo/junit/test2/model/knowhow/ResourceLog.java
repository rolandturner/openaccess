
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
package com.versant.core.jdo.junit.test2.model.knowhow;

/** 
 * @keep-all
 */
public class ResourceLog {

    private User user;
    private int score;
    private Resource resource;

    public ResourceLog(User user, int score, Resource resource) {
        this.user = user;
        this.score = score;
        this.resource = resource;
    }

    public User getUser() {
        return user;
    }

    public int getScore() {
        return score;
    }

    public Resource getResource() {
        return resource;
    }

}
